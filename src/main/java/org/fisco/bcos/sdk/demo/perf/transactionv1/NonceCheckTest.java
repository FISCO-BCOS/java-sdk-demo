package org.fisco.bcos.sdk.demo.perf.transactionv1;

import com.google.common.util.concurrent.RateLimiter;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.transactionv1.Incremental;
import org.fisco.bcos.sdk.demo.perf.Collector;
import org.fisco.bcos.sdk.demo.perf.ParallelOkPerf;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.ProxySignTransactionManager;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.transaction.nonce.NonceAndBlockLimitProvider;
import org.fisco.bcos.sdk.v3.transaction.nonce.RemoteBlockLimitCallbackInterface;
import org.fisco.bcos.sdk.v3.transaction.nonce.RemoteNonceCallbackInterface;
import org.fisco.bcos.sdk.v3.utils.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class NonceCheckTest {

    static Map<String, String> nonceMap = new ConcurrentHashMap<>();
    static Map<String, String> txToNonceMap = new ConcurrentHashMap<>();

    static class PerSecNonceProvider implements NonceAndBlockLimitProvider {
        @Override
        public String getNonce() {
            long currentTimeMillis = System.currentTimeMillis();
            long second = currentTimeMillis / 1000;
            String hexString = Long.toHexString(second);
            nonceMap.put(hexString, "nonce");
            return hexString;
        }

        @Override
        public void getNonceAsync(RemoteNonceCallbackInterface remoteNonceCallbackInterface) {
            remoteNonceCallbackInterface.handleNonce(getNonce());
        }

        @Override
        public BigInteger getBlockLimit(Client client) {
            return client.getBlockLimit();
        }

        @Override
        public void getBlockLimitAsync(Client client, RemoteBlockLimitCallbackInterface remoteBlockLimitCallbackInterface) {
            remoteBlockLimitCallbackInterface.handleBlockLimit(client.getBlockLimit());
        }
    }

    private static Client client;

    public static void usage() {
        System.out.println(" Usage:");
        System.out.println("===== NonceCheckTest test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.transactionv1.NonceCheckTest [groupId] [count] [qps] [contractAddress] [strictCheck].");
    }

    public static void main(String[] args) throws ContractException, IOException, InterruptedException {
        String configFileName = ConstantConfig.CONFIG_FILE_NAME;
        URL configUrl = ParallelOkPerf.class.getClassLoader().getResource(configFileName);
        if (configUrl == null) {
            System.out.println("The configFile " + configFileName + " doesn't exist!");
            return;
        }
        if (args.length < 3) {
            usage();
            return;
        }

        String groupId = args[0];
        Integer count = Integer.valueOf(args[1]);
        Integer qps = Integer.valueOf(args[2]);
        String contractAddress = null;
        boolean strictCheck = false;

        if (args.length >= 4) {
            contractAddress = args[3];
        }
        if (args.length >= 5) {
            strictCheck = Boolean.parseBoolean(args[4]);
        }

        String configFile = configUrl.getPath();
        BcosSDK sdk = BcosSDK.build(configFile);
        client = sdk.getClient(groupId);

        System.out.println(
                "====== Start "
                        + ", count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId);

        RateLimiter limiter = RateLimiter.create(qps);
        Incremental incremental;
        if (contractAddress != null && !contractAddress.isEmpty()) {
            incremental = Incremental.load(contractAddress, client);
            System.out.println("Load finished, contract address: " + contractAddress);
        } else {
            incremental = Incremental.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            contractAddress = incremental.getContractAddress();
            System.out.println("Deploy finished, contract address: " + contractAddress);
        }
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

        CountDownLatch transactionLatch = new CountDownLatch(count);
        AtomicLong totalCost = new AtomicLong(0);
        Collector collector = new Collector();
        collector.setTotal(count);

        ProxySignTransactionManager proxySignTransactionManager = new ProxySignTransactionManager(client);
        proxySignTransactionManager.setNonceProvider(new PerSecNonceProvider());
        incremental.setTransactionManager(proxySignTransactionManager);

        IntStream.range(0, count)
                .parallel()
                .forEach(
                        index -> {
                            limiter.acquire();
                            long now = System.currentTimeMillis();
                            try {
                                incremental.inc(UUID.randomUUID().toString(),
                                        new TransactionCallback() {
                                            public void onResponse(TransactionReceipt receipt) {
                                                long cost = System.currentTimeMillis() - now;
                                                collector.onMessage(receipt, cost);
                                                if (receipt.isStatusOK()) {
                                                    txToNonceMap.put(receipt.getTransactionHash(), "");
                                                }
                                                receivedBar.step();
                                                transactionLatch.countDown();
                                                totalCost.addAndGet(System.currentTimeMillis() - now);
                                            }
                                        });
                                sentBar.step();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
        transactionLatch.await();
        System.out.println("Sending transactions finished!");
        sentBar.close();
        receivedBar.close();
        collector.report();
        System.out.println("===================================================================");
        System.out.println("Nonce size: " + nonceMap.size());
        BigInteger value = incremental.value();
        System.out.println("Inc counts: " + value);
        if (strictCheck) {
            if (nonceMap.size() != value.intValue()) {
                System.out.println("Nonce size not equal to inc counts!");
                System.exit(1);
            }
            System.out.println("Tx size: " + txToNonceMap.size());
            System.out.println("===================================================================");
            System.out.println("Check nonce...");
            for (String key : txToNonceMap.keySet()) {
                JsonTransactionResponse response = client.getTransaction(key, false).getTransaction().get();
                String nonce = new String(Hex.decode(response.getNonce()));
                if (nonceMap.get(nonce) == null) {
                    System.out.println("Nonce " + nonce + " not found!");
                    System.exit(1);
                }
            }
            System.out.println("Check nonce finished!");
        }
        System.exit(0);
    }
}
