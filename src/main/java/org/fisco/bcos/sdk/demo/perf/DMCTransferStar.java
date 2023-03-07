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

public class DMCTransferStar {
    private static Client client;
    private static ShardingService shardingService;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== Executor Single Contract Integration Test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.DMCTransferStar [groupId] [starNode] [count] [qps] [allowRevert]");
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
            Integer nodeNum = Integer.valueOf(args[1]);
            Integer count = Integer.valueOf(args[2]);
            Integer qps = Integer.valueOf(args[3]);
            boolean allowRevert = Boolean.valueOf(args[4]);

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            shardingService =
                    new ShardingService(client, client.getCryptoSuite().getCryptoKeyPair());
            String userAddr = sdk.getConfig().getAccountConfig().getAccountAddress();
            if (nodeNum < 3) {
                System.out.println(
                        "The number of nodes is too small to form a star network, and a larger number of nodes is required! ");
                return;
            }

            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "DMCTransferStarClient", Runtime.getRuntime().availableProcessors());

            start(sdk, groupId, nodeNum, count, qps, allowRevert, threadPoolService);
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
            Integer nodeNum,
            Integer count,
            Integer qps,
            boolean allowRevert,
            ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start "
                        + "Executor DMCTransfer StarTest, stars node num:"
                        + nodeNum
                        + ", tx count: "
                        + count
                        + ", groupId: "
                        + groupId);
        RateLimiter limiter = RateLimiter.create(qps.intValue());
        DmcTransfer[] contracts = new DmcTransfer[nodeNum];
        String[] contractsAddr = new String[nodeNum];

        System.out.println("Create contract and generate call relationship...");
        CountDownLatch contractLatch = new CountDownLatch(nodeNum);
        for (int i = 0; i < nodeNum; ++i) {
            final int index = i;
            limiter.acquire();
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                public void run() {
                                    DmcTransfer contract;
                                    try {
                                        long initBalance = INIT_BALANCE;
                                        contract =
                                                DmcTransfer.deploy(
                                                        client,
                                                        client.getCryptoSuite().getCryptoKeyPair());
                                        String address = contract.getContractAddress();
                                        try {
                                            shardingService.linkShard(
                                                    "dmctest" + address.substring(0, 4), address);
                                        } catch (ContractException e) {
                                        }
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
        String userAddress = sdk.getConfig().getAccountConfig().getAccountAddress();

        List<String> starCenterAddr = new ArrayList<>();
        starCenterAddr.add(contractsAddr[0]);
        CountDownLatch callRelationshipLatch = new CountDownLatch(nodeNum - 1);
        for (int i = 1; i < nodeNum; ++i) {
            final int index = i;
            limiter.acquire();
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {

                                public void run() {

                                    contracts[index].addNextCall(userAddress, starCenterAddr);
                                    callRelationshipLatch.countDown();
                                }
                            });
        }
        callRelationshipLatch.await();
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
        AtomicInteger expectBalance = new AtomicInteger(0);
        Collector collector = new Collector();
        collector.setTotal(count);
        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        for (int i = 0; i < count; ) {
            for (int j = 1; j < nodeNum && i < count; ++j, ++i) {
                limiter.acquire();
                final int fromIndex = j;
                threadPoolService
                        .getThreadPool()
                        .execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        DmcTransfer contract = contracts[fromIndex];
                                        long now = System.currentTimeMillis();
                                        contract.takeShare(
                                                BigInteger.valueOf(2),
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
                                                        expectBalance.addAndGet(2);
                                                    }
                                                });

                                        sendedBar.step();
                                    }
                                });
            }
        }

        transactionLatch.await();
        sendedBar.close();
        receivedBar.close();
        errorBar.close();
        collector.report();
        System.out.println("Sending transactions finished!");

        AtomicInteger total = new AtomicInteger();
        CountDownLatch checkLatch = new CountDownLatch(nodeNum);
        for (int j = 0; j < nodeNum; ++j) {
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
                    "check total failed! total value is: "
                            + total.intValue()
                            + ", expectBalance is "
                            + expectBalance.intValue());
        }
        System.out.println("check finished, total balance equal expectBalance !");
    }
}
