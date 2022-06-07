/**
 * copyright 2014-2020 [fisco-dev]
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fisco.bcos.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.DmcTransfer;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class DMCTransferDag {
    private static Client client;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== Executor Single Contract Integration Test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.rc4.DMCTransferDag [groupId] [count] [qps] [allowRevert].");
    }

    private static final Long INIT_BALANCE = 0L;

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = ParallelOkPerf.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }
            if (args.length < 4) {
                Usage();
                return;
            }
            if (args.length > 4) {
                System.out.println("Please set the parameters as specified!");
                return;
            }

            String groupId = args[0];
            Integer count = Integer.valueOf(args[1]);
            Integer qps = Integer.valueOf(args[2]);
            boolean allowRevert = Boolean.valueOf(args[3]);
            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "ExecutorDagContractClient",
                            Runtime.getRuntime().availableProcessors());

            start(sdk, groupId, count, qps, allowRevert, threadPoolService);
            threadPoolService.getThreadPool().awaitTermination(0, TimeUnit.SECONDS);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void start(
            BcosSDK sdk,
            String groupId,
            Integer count,
            Integer qps,
            boolean allowRevert,
            ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start "
                        + "Executor DAG Contract test, contracts num is 8 "
                        + ", tx count: "
                        + count
                        + ", groupId: "
                        + groupId);
        RateLimiter limiter = RateLimiter.create(qps.intValue());
        DmcTransfer[] contracts = new DmcTransfer[8];
        // List<String> contractsAddr = new ArrayList<>();
        String[] contractsAddr = new String[8];

        System.out.println("Create contract and generate call relationship...");
        CountDownLatch contractLatch = new CountDownLatch(8);
        for (int i = 0; i < 8; ++i) {
            final int index = i;
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                public void run() {
                                    DmcTransfer contract;
                                    try {
                                        limiter.acquire();
                                        long initBalance = INIT_BALANCE;
                                        contract =
                                                DmcTransfer.deploy(
                                                        client,
                                                        client.getCryptoSuite().getCryptoKeyPair());
                                        String sender =
                                                contract.addBalance(BigInteger.valueOf(initBalance))
                                                        .getFrom();
                                        sdk.getConfig()
                                                .getAccountConfig()
                                                .setAccountAddress(sender);
                                        contracts[index] = contract;
                                        contractsAddr[index] = contract.getContractAddress();
                                        contractLatch.countDown();
                                    } catch (ContractException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
        }
        contractLatch.await();
        System.out.println("Create " + 8 + " contracts finished!");

        String userAddress = sdk.getConfig().getAccountConfig().getAccountAddress();
        List<String> DAGCenterAddr = new ArrayList<>();
        List<String> DAGCenterAddr1 = new ArrayList<>();
        List<String> DAGIn = new ArrayList<>();
        DAGCenterAddr.add(contractsAddr[5]);
        DAGCenterAddr.add(contractsAddr[6]);
        DAGCenterAddr1.add(contractsAddr[6]);
        DAGCenterAddr1.add(contractsAddr[7]);
        DAGIn.add(contractsAddr[3]);
        DAGIn.add(contractsAddr[4]);

        contracts[0].addNextCall(userAddress, Arrays.asList(contractsAddr[3]));
        contracts[1].addNextCall(userAddress, DAGIn);
        contracts[2].addNextCall(userAddress, Arrays.asList(contractsAddr[4]));
        contracts[3].addNextCall(contractsAddr[0], DAGCenterAddr);
        contracts[3].addNextCall(contractsAddr[1], DAGCenterAddr);
        contracts[4].addNextCall(contractsAddr[1], DAGCenterAddr1);
        contracts[4].addNextCall(contractsAddr[2], DAGCenterAddr1);

        System.out.println("Create contract and generate call relationship finished!");

        System.out.println("Sending transactions...");
        ProgressBar sendedBar =
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
        ProgressBar errorBar =
                new ProgressBarBuilder()
                        .setTaskName("Errors :")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        CountDownLatch transactionLatch = new CountDownLatch(count);
        AtomicLong totalCost = new AtomicLong(0);
        Collector collector = new Collector();
        collector.setTotal(count);
        final Random random = new Random();
        AtomicInteger expectBalance = new AtomicInteger(0);

        for (int i = 0; i < count; ) {
            for (int j = 0; j < 3 && i < count; ++j) {
                if (j != 1) {
                    int finalJ = j;
                    threadPoolService
                            .getThreadPool()
                            .execute(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            limiter.acquire();
                                            long now = System.currentTimeMillis();
                                            contracts[finalJ].takeShare(
                                                    BigInteger.valueOf(4),
                                                    allowRevert,
                                                    new TransactionCallback() {
                                                        @Override
                                                        public void onResponse(
                                                                TransactionReceipt receipt) {
                                                            long cost =
                                                                    System.currentTimeMillis()
                                                                            - now;
                                                            collector.onMessage(receipt, cost);

                                                            receivedBar.step();
                                                            if (!receipt.isStatusOK()) {
                                                                errorBar.step();
                                                            }
                                                            transactionLatch.countDown();
                                                            totalCost.addAndGet(
                                                                    System.currentTimeMillis()
                                                                            - now);
                                                            expectBalance.addAndGet(4);
                                                        }
                                                    });
                                            sendedBar.step();
                                        }
                                    });
                    ++i;
                } else {
                    int finalJ = j;
                    threadPoolService
                            .getThreadPool()
                            .execute(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            limiter.acquire();
                                            long now = System.currentTimeMillis();
                                            contracts[finalJ].takeShare(
                                                    BigInteger.valueOf(7),
                                                    allowRevert,
                                                    new TransactionCallback() {
                                                        @Override
                                                        public void onResponse(
                                                                TransactionReceipt receipt) {
                                                            long cost =
                                                                    System.currentTimeMillis()
                                                                            - now;
                                                            collector.onMessage(receipt, cost);

                                                            receivedBar.step();
                                                            if (!receipt.isStatusOK()) {
                                                                errorBar.step();
                                                            }
                                                            transactionLatch.countDown();
                                                            totalCost.addAndGet(
                                                                    System.currentTimeMillis()
                                                                            - now);
                                                            expectBalance.addAndGet(7);
                                                        }
                                                    });
                                            sendedBar.step();
                                        }
                                    });
                    ++i;
                }
            }
        }
        transactionLatch.await();
        sendedBar.close();
        receivedBar.close();
        errorBar.close();
        collector.report();

        System.out.println("Sending transactions finished!");

        AtomicInteger total = new AtomicInteger();
        CountDownLatch checkLatch = new CountDownLatch(8);
        for (int j = 0; j < 8; ++j) {
            limiter.acquire();
            int finalJ = j;
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        BigInteger balance = contracts[finalJ].balance();
                                        total.addAndGet(balance.intValue());
                                        checkLatch.countDown();
                                    } catch (ContractException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
        }
        checkLatch.await();
        if (total.intValue() != expectBalance.intValue()) {
            System.out.println(
                    "check total failed! total balance is "
                            + total.toString()
                            + ", expectBalance is "
                            + expectBalance.toString());
        }
        System.out.println(
                "check finished, balance is "
                        + contracts[0].balance().toString()
                        + " , "
                        + contracts[1].balance().toString()
                        + " , "
                        + contracts[2].balance().toString()
                        + " , "
                        + contracts[3].balance().toString()
                        + " , "
                        + contracts[4].balance().toString()
                        + " , "
                        + contracts[5].balance().toString()
                        + " , "
                        + contracts[6].balance().toString()
                        + " , "
                        + contracts[7].balance().toString()
                        + " ! ");
    }
}
