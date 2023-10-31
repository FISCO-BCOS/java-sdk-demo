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
import org.fisco.bcos.sdk.v3.contract.precompiled.sharding.ShardingService;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class DMCTransferDag {
    private static Client client;
    private static ShardingService shardingService;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== Executor Single Contract Integration Test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.DMCTransferDag [groupId] [startNodeNum] [count] [qps] [allowRevert].");
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
            if (args.length < 5) {
                Usage();
                return;
            }
            if (args.length > 5) {
                System.out.println("Please set the parameters as specified!");
                return;
            }

            String groupId = args[0];
            Integer startNodeNum = Integer.valueOf(args[1]);
            Integer count = Integer.valueOf(args[2]);
            Integer qps = Integer.valueOf(args[3]);
            boolean allowRevert = Boolean.valueOf(args[4]);
            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            shardingService =
                    new ShardingService(client, client.getCryptoSuite().getCryptoKeyPair());
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "ExecutorDagContractClient",
                            Runtime.getRuntime().availableProcessors());

            start(sdk, groupId, startNodeNum, count, qps, allowRevert, threadPoolService);
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
            Integer startNodeNum,
            Integer count,
            Integer qps,
            boolean allowRevert,
            ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start "
                        + "Executor DAG Contract test, contracts num is "
                        + (startNodeNum + 5)
                        + ", tx count: "
                        + count
                        + ", groupId: "
                        + groupId);
        RateLimiter limiter = RateLimiter.create(qps.intValue());
        DmcTransfer[] contracts = new DmcTransfer[startNodeNum + 5];
        // List<String> contractsAddr = new ArrayList<>();
        String[] contractsAddr = new String[startNodeNum + 5];

        System.out.println("Create contract and generate call relationship...");
        CountDownLatch contractLatch = new CountDownLatch(startNodeNum + 5);
        for (int i = 0; i < (startNodeNum + 5); ++i) {
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
                                        String address = contract.getContractAddress();
                                        contractsAddr[index] = address;
                                        try {
                                            shardingService.linkShard(
                                                    "dmctest" + address.substring(0, 4), address);
                                        } catch (ContractException e) {
                                        }
                                        contractLatch.countDown();
                                    } catch (ContractException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
        }
        contractLatch.await();
        System.out.println("Create " + (startNodeNum + 5) + " contracts finished!");

        String userAddress = sdk.getConfig().getAccountConfig().getAccountAddress();
        List<String> DAGCenterAddr = new ArrayList<>();
        List<String> DAGOut = new ArrayList<>();
        List<String> DAGIn = new ArrayList<>();
        for (int i = 0; i < startNodeNum; ++i) {
            DAGIn.add(contractsAddr[i]);
        }
        DAGCenterAddr.add(contractsAddr[startNodeNum]);
        DAGCenterAddr.add(contractsAddr[startNodeNum + 1]);
        DAGOut.add(contractsAddr[startNodeNum + 2]);
        DAGOut.add(contractsAddr[startNodeNum + 3]);
        DAGOut.add(contractsAddr[startNodeNum + 4]);

        for (int i = 0; i < startNodeNum; ++i) {
            // userAddr -> centerNode
            contracts[i].addNextCall(userAddress, DAGCenterAddr);
        }
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < startNodeNum; ++j) {
                // In -> Out
                contracts[startNodeNum + i].addNextCall(contractsAddr[j], DAGOut);
            }
        }

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
            for (int j = 0; j < startNodeNum && i < count; ++j) {
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
                                                BigInteger.valueOf(9),
                                                allowRevert,
                                                new TransactionCallback() {
                                                    @Override
                                                    public void onResponse(
                                                            TransactionReceipt receipt) {
                                                        long cost =
                                                                System.currentTimeMillis() - now;
                                                        collector.onMessage(receipt, cost);

                                                        receivedBar.step();
                                                        if (!receipt.isStatusOK()) {
                                                            errorBar.step();
                                                        }
                                                        transactionLatch.countDown();
                                                        totalCost.addAndGet(
                                                                System.currentTimeMillis() - now);
                                                        expectBalance.addAndGet(9);
                                                    }
                                                });
                                        sendedBar.step();
                                    }
                                });
                ++i;
            }
        }
        transactionLatch.await();
        sendedBar.close();
        receivedBar.close();
        errorBar.close();
        collector.report();

        System.out.println("Sending transactions finished!");

        AtomicInteger total = new AtomicInteger();
        CountDownLatch checkLatch = new CountDownLatch(startNodeNum + 5);
        for (int j = 0; j < startNodeNum + 5; ++j) {
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
        System.out.println("check finished, total balance equal expectBalance!");
    }
}
