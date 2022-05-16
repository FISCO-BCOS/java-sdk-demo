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
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.ExecutorIntegrationTest;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class ExecutorStarContract {

    private static Client client;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== Executor Single Contract Integration Test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.rc4.SingleContractTest [groupId] [contractsNum] [starNode] [count] [qps].");
    }

    private static final Long INIT_BALANCE = 1000000000L;

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = ParallelOkPerf.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }
            if (args.length < 6) {
                Usage();
                return;
            }

            String groupId = args[0];
            int contractsNum = Integer.valueOf(args[1]).intValue();
            Integer nodeNum = Integer.valueOf(args[2]);
            Integer count = Integer.valueOf(args[3]);
            Integer qps = Integer.valueOf(args[4]);

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "ExecytorSingleContractClient",
                            Runtime.getRuntime().availableProcessors());

            start(groupId, contractsNum, nodeNum, count, qps, threadPoolService);
            threadPoolService.getThreadPool().awaitTermination(0, TimeUnit.SECONDS);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void start(
            String groupId,
            int contractsNum,
            Integer nodeNum,
            Integer count,
            Integer qps,
            ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start "
                        + "Executor Stars Contract test, contracts num: "
                        + contractsNum
                        + ", stars node num :"
                        + nodeNum
                        + ", Txcount: "
                        + count
                        + ", groupId: "
                        + groupId);
        RateLimiter limiter = RateLimiter.create(qps.intValue());

        ExecutorIntegrationTest[] contracts = new ExecutorIntegrationTest[nodeNum];
        Map<Integer, AtomicLong> summary = new ConcurrentHashMap<Integer, AtomicLong>();

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        System.out.println("Create contract...");
        CountDownLatch contractLatch = new CountDownLatch(nodeNum);
        for (int i = 0; i < nodeNum; ++i) {
            final int index = i;
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                public void run() {
                                    ExecutorIntegrationTest contract;
                                    try {
                                        long initBalance = INIT_BALANCE;

                                        limiter.acquire();
                                        contract =
                                                ExecutorIntegrationTest.deploy(
                                                        client,
                                                        client.getCryptoSuite().getCryptoKeyPair());
                                        contract.addBalance(BigInteger.valueOf(initBalance));

                                        contracts[index] = contract;
                                        summary.put(index, new AtomicLong(initBalance));
                                        contractLatch.countDown();
                                    } catch (ContractException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
        }
        contractLatch.await();
        System.out.println("Create account finished!");

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
        for (int j = 0; j < count; ) {
            final int toIndex = 0;
            for (int i = 1; i < nodeNum; ++i, ++j) {
                limiter.acquire();
                final int fromIndex = i;
                threadPoolService
                        .getThreadPool()
                        .execute(
                                new Runnable() {
                                    @Override
                                    public void run() {

                                        ExecutorIntegrationTest contract = contracts[fromIndex];

                                        long now = System.currentTimeMillis();

                                        final long value = Math.abs(random.nextLong() % 1000);

                                        contract.transfer(
                                                contracts[toIndex].getContractAddress(),
                                                BigInteger.valueOf(value),
                                                new TransactionCallback() {
                                                    @Override
                                                    public void onResponse(
                                                            TransactionReceipt receipt) {
                                                        if (receipt.getStatus() == 0) {
                                                            AtomicLong fromBalance =
                                                                    summary.get(fromIndex);
                                                            fromBalance.addAndGet(-value);

                                                            AtomicLong toBalance =
                                                                    summary.get(toIndex);
                                                            toBalance.addAndGet(value);
                                                        }

                                                        long cost =
                                                                System.currentTimeMillis() - now;
                                                        collector.onMessage(receipt, cost);

                                                        receivedBar.step();
                                                        if (!receipt.isStatusOK()) {
                                                            errorBar.step();
                                                            // System.out.println(receipt.getStatus());
                                                        }

                                                        transactionLatch.countDown();
                                                        totalCost.addAndGet(
                                                                System.currentTimeMillis() - now);
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

        System.out.println("Checking result...");
        CountDownLatch checkLatch = new CountDownLatch(summary.size());
        AtomicLong totalBalance = new AtomicLong(0);

        for (Map.Entry<Integer, AtomicLong> entry : summary.entrySet()) {
            limiter.acquire();
            final int index = entry.getKey().intValue();
            final long expectBalance = entry.getValue().longValue();
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        limiter.acquire();
                                        BigInteger balance = contracts[index].balance();
                                        totalBalance.addAndGet(balance.longValue());
                                        if (balance.longValue() != expectBalance) {
                                            System.out.println(
                                                    "[x] Check failed! Account["
                                                            + index
                                                            + "] balance: "
                                                            + balance
                                                            + " not equal to expected: "
                                                            + expectBalance);
                                        }
                                        checkLatch.countDown();
                                    } catch (ContractException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
        }
        checkLatch.await();
        System.out.println("Checking finished!");

        Long expect = nodeNum * INIT_BALANCE;

        System.out.println("Reverted transactions: " + collector.getError());
        System.out.println("Total balance: " + totalBalance + " expect: " + expect);
        System.out.println(
                "Check " + (totalBalance.get() == expect.longValue() ? "OK!" : "Failed."));
    }
}
