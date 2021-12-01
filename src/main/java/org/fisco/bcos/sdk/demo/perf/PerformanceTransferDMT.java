/**
 * Copyright 2014-2020 [fisco-dev]
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
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.demo.contract.Account;
import org.fisco.bcos.sdk.model.ConstantConfig;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.utils.ThreadPoolService;

public class PerformanceTransferDMT {
    private static Client client;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceDMT test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTransferDMT [groupId] [userCount] [count] [qps].");
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

            if (args.length < 4) {
                Usage();
                return;
            }
            String groupId = args[0];
            int userCount = Integer.valueOf(args[1]).intValue();
            Integer count = Integer.valueOf(args[2]).intValue();
            Integer qps = Integer.valueOf(args[3]).intValue();

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            ThreadPoolService threadPoolService =
                    new ThreadPoolService("DMTClient", Runtime.getRuntime().availableProcessors());

            start(groupId, userCount, count, qps, threadPoolService);

            threadPoolService.getThreadPool().awaitTermination(0, TimeUnit.SECONDS);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void start(
            String groupId,
            int userCount,
            int count,
            Integer qps,
            ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start test, user count: "
                        + userCount
                        + ", count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId);

        RateLimiter limiter = RateLimiter.create(qps.intValue());

        Account[] accounts = new Account[userCount];
        Map<Integer, AtomicLong> summary = new ConcurrentHashMap<Integer, AtomicLong>();

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        System.out.println("Create account...");
        CountDownLatch userLatch = new CountDownLatch(userCount);
        for (int i = 0; i < userCount; ++i) {
            final int index = i;
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                public void run() {
                                    Account account;
                                    try {
                                        long initBalance = Math.abs(random.nextLong());

                                        limiter.acquire();
                                        account =
                                                Account.deploy(
                                                        client,
                                                        client.getCryptoSuite().getCryptoKeyPair());
                                        account.addBalance(BigInteger.valueOf(initBalance));

                                        accounts[index] = account;
                                        summary.put(index, new AtomicLong(initBalance));
                                        userLatch.countDown();
                                    } catch (ContractException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
        }
        userLatch.await();
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

        CountDownLatch transactionLatch = new CountDownLatch(count);
        AtomicLong totalCost = new AtomicLong(0);
        Collector collector = new Collector();
        collector.setTotal(count);

        for (int i = 0; i < count; ++i) {
            limiter.acquire();

            final int fromIndex = random.nextInt() % accounts.length;
            final int toIndex = random.nextInt() % accounts.length;
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Account account = accounts[fromIndex];
                                    long now = System.currentTimeMillis();

                                    final long value = Math.abs(random.nextLong() % 1000);

                                    account.transfer(
                                            accounts[toIndex].getContractAddress(),
                                            BigInteger.valueOf(value),
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {
                                                    if (receipt.getStatus() == 0) {
                                                        AtomicLong fromBalance =
                                                                summary.get(fromIndex);
                                                        fromBalance.addAndGet(-value);

                                                        AtomicLong toBalance = summary.get(toIndex);
                                                        toBalance.addAndGet(value);
                                                    }
                                                    long cost = System.currentTimeMillis() - now;
                                                    collector.onMessage(receipt, cost);
                                                    receivedBar.step();
                                                    transactionLatch.countDown();
                                                    totalCost.addAndGet(
                                                            System.currentTimeMillis() - now);
                                                }
                                            });
                                    sendedBar.step();
                                }
                            });
        }
        transactionLatch.await();

        sendedBar.close();
        receivedBar.close();
        collector.report();

        System.out.println("Sending transactions finished!");

        System.out.println("Checking result...");
        CountDownLatch checkLatch = new CountDownLatch(count);
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
                                        BigInteger balance = accounts[index].balance();
                                        if (balance.longValue() != expectBalance) {
                                            System.out.println(
                                                    "Check failed! Account["
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
        System.out.println("Checking finished!");

        // collector.
        // System.out.println("Total elapsed: " + elapsed);
        // System.out.println("TPS: " + (double) count / ((double) elapsed / 1000));
    }
}
