package org.fisco.bcos.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.HelloExternal;
import org.fisco.bcos.sdk.demo.contract.HelloWorld;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class PerformanceDeployTest {
    private static Client client;
    private static final Random random = new Random();

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceDeployTest test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceDeployTest [groupId] [count] [qps] [isExternal].");
    }

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = ParallelOkPerf.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }

            if (args.length < 3) {
                Usage();
                return;
            }
            String groupId = args[0];
            int count = Integer.parseInt(args[1]);
            Integer qps = Integer.parseInt(args[2]);
            boolean isExternal = false;
            if (args.length == 4) {
                isExternal = Boolean.parseBoolean(args[3]);
            }

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "PerformanceDeployTest", Runtime.getRuntime().availableProcessors());

            if (isExternal) {
                externalDeploy(groupId, count, qps, threadPoolService);
            } else {
                localDeploy(groupId, count, qps, threadPoolService);
            }
            threadPoolService.getThreadPool().awaitTermination(0, TimeUnit.SECONDS);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void localDeploy(
            String groupId, int count, Integer qps, ThreadPoolService threadPoolService)
            throws InterruptedException {
        System.out.println(
                "====== Start test, count: " + count + ", qps:" + qps + ", groupId: " + groupId);

        String abi = HelloWorld.getABI();
        String binary = HelloWorld.getBinary(client.getCryptoSuite());
        deploy(count, qps, threadPoolService, abi, binary);
    }

    public static void externalDeploy(
            String groupId, int count, Integer qps, ThreadPoolService threadPoolService)
            throws InterruptedException {
        System.out.println(
                "====== Start test, count: " + count + ", qps:" + qps + ", groupId: " + groupId);

        String abi = HelloExternal.getABI();
        String binary = HelloExternal.getBinary(client.getCryptoSuite());
        deploy(count, qps, threadPoolService, abi, binary);
    }

    private static void deploy(
            int count, Integer qps, ThreadPoolService threadPoolService, String abi, String binary)
            throws InterruptedException {
        AssembleTransactionProcessor assembleTransactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                        client,
                        client.getCryptoSuite().getCryptoKeyPair(),
                        "HelloExternal",
                        abi,
                        binary);

        RateLimiter limiter = RateLimiter.create(qps);
        random.setSeed(System.currentTimeMillis());

        System.out.println("Sending transactions...");
        ProgressBar sentBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        CountDownLatch countDownLatch = new CountDownLatch(count);
        Collector collector = new Collector();
        collector.setTotal(count);

        for (int i = 0; i < count; ++i) {
            limiter.acquire();
            threadPoolService
                    .getThreadPool()
                    .execute(
                            () -> {
                                long now = System.currentTimeMillis();
                                try {
                                    assembleTransactionProcessor.deployAsync(
                                            HelloWorld.getABI(),
                                            binary,
                                            new ArrayList<>(),
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {
                                                    long cost = System.currentTimeMillis() - now;
                                                    collector.onMessage(receipt, cost);
                                                    receivedBar.step();
                                                    countDownLatch.countDown();
                                                }
                                            });
                                    sentBar.step();
                                } catch (ContractCodecException e) {
                                    e.printStackTrace();
                                }
                            });
        }
        countDownLatch.await();
        sentBar.close();
        receivedBar.close();
        collector.report();

        System.out.println("Create contracts finished!");
    }
}
