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
import java.util.Arrays;
import java.util.Random;
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

public class DMCTransferMyself {
    private static Client client;
    private static ShardingService shardingService;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== Executor Single Contract Integration Test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.DMCTransferMyself [groupId] [contractsNum] [count] [qps] [allowRevert].");
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
            int contractsNum = Integer.valueOf(args[1]).intValue();
            Integer count = Integer.valueOf(args[2]).intValue();
            Integer qps = Integer.valueOf(args[3]).intValue();
            boolean allowRevert = Boolean.valueOf(args[4]);

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            shardingService =
                    new ShardingService(client, client.getCryptoSuite().getCryptoKeyPair());
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "DMCTransferMyselfClient", Runtime.getRuntime().availableProcessors());

            start(sdk, groupId, contractsNum, count, qps, allowRevert, threadPoolService);
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
            int contractsNum,
            Integer count,
            Integer qps,
            boolean allowRevert,
            ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start "
                        + "Executor DMCTransferMyself test, contracts num: "
                        + contractsNum
                        + ", Txcount: "
                        + count
                        + ", groupId: "
                        + groupId);
        RateLimiter limiter = RateLimiter.create(qps.intValue());

        DmcTransfer[] contracts = new DmcTransfer[contractsNum];
        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        System.out.println("Create contract and generate call relationship...");
        CountDownLatch contractLatch = new CountDownLatch(contractsNum);
        for (int i = 0; i < contractsNum; ++i) {
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
                                        contractLatch.countDown();
                                    } catch (ContractException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
        }
        contractLatch.await();
        System.out.println("Deploy " + contractsNum + " contract Finished!");

        String userAddress = sdk.getConfig().getAccountConfig().getAccountAddress();

        CountDownLatch callRelationshipLatch = new CountDownLatch(contractsNum);
        for (int i = 0; i < contractsNum; ++i) {
            final int index = i;
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                public void run() {
                                    limiter.acquire();
                                    DmcTransfer contract = contracts[index];
                                    contract.addNextCall(
                                            userAddress,
                                            Arrays.asList(contract.getContractAddress()));
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
        for (int i = 0; i < count; ) {
            for (int j = 0; j < contractsNum && i < count; ++j, ++i) {
                limiter.acquire();
                final int index = j;
                threadPoolService
                        .getThreadPool()
                        .execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        // DmcTransfer contract = contracts[index];
                                        long now = System.currentTimeMillis();
                                        contracts[index].takeShare(
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

        System.out.println("Sending transactions finished!");
        sendedBar.close();
        receivedBar.close();
        errorBar.close();
        collector.report();

        AtomicInteger total = new AtomicInteger();
        CountDownLatch checkLatch = new CountDownLatch(contractsNum);
        for (int j = 0; j < contractsNum; ++j) {
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
        System.out.println("check finished! check finished, total balance equal expectBalance! ");
    }
}
