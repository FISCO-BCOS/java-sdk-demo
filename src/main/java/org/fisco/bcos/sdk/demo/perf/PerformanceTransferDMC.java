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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
import org.fisco.bcos.sdk.demo.contract.Account;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class PerformanceTransferDMC {
    private static Client client;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceDMC test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTransferDMC [groupId] [userCount] [count] [qps]."
                        + " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTransferDMC [groupId] [userCount] [count] [generate].");
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

            if (args.length < 4) {
                Usage();
                return;
            }
            String groupId = args[0];
            int userCount = Integer.valueOf(args[1]).intValue();
            Integer count = Integer.valueOf(args[2]).intValue();

            Integer qps = 100000;
            boolean isGenerate = false;

            if (args[3].equals("generate")) {
                isGenerate = true;
            } else {
                qps = Integer.valueOf(args[3]).intValue();
            }

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            ThreadPoolService threadPoolService =
                    new ThreadPoolService("DMCClient", Runtime.getRuntime().availableProcessors());

            start(groupId, userCount, count, qps, isGenerate, threadPoolService);

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
            boolean isGenerate,
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
                        + groupId
                        + ", generate: "
                        + isGenerate);

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
                                        long initBalance = INIT_BALANCE;

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

        if (isGenerate) {
            String accountTxFile =
                    "dmcAccountTransferTx."
                            + userCount
                            + "."
                            + count
                            + "."
                            + System.currentTimeMillis()
                            + ".txt";
            System.out.println("Generating transactions -> " + accountTxFile);
            ProgressBar generateBar =
                    new ProgressBarBuilder()
                            .setTaskName("Generate   :")
                            .setInitialMax(count)
                            .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                            .build();

            File accountFile = new File(accountTxFile);
            if (!accountFile.exists()) {
                accountFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(accountFile.getName(), true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (int i = 0; i < count; ++i) {
                final int fromIndex = Math.abs(random.nextInt()) % accounts.length;
                final int toIndex = Math.abs(random.nextInt()) % accounts.length;

                Account account = accounts[fromIndex];

                final long value = Math.abs(random.nextLong() % 1000);

                String txBytes =
                        account.getSignedTransactionForTransfer(
                                accounts[toIndex].getContractAddress(), BigInteger.valueOf(value));

                bufferedWriter.write(txBytes);
                bufferedWriter.newLine();

                generateBar.step();
            }
            generateBar.close();

            System.out.println("Write DMC accounts transfer into file: " + accountTxFile);
            bufferedWriter.close();

        } else {
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

                final int fromIndex = Math.abs(random.nextInt()) % accounts.length;
                final int toIndex = Math.abs(random.nextInt()) % accounts.length;
                // final int toIndex = (fromIndex + 1) % accounts.length;
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
                                            BigInteger balance = accounts[index].balance();
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
            /*
            for (int i = 0; i < accounts.length; i++) {
                System.out.println(
                        "Account: " + accounts[i].getContractAddress() + " | " + accounts[i].balance());
            }
            */

            Long expect = userCount * INIT_BALANCE;

            System.out.println("Reverted transactions: " + collector.getError());
            System.out.println("Total balance: " + totalBalance + " expect: " + expect);
            System.out.println(
                    "Check " + (totalBalance.get() == expect.longValue() ? "OK!" : "Failed."));
            // System.exit(0);
            // collector.
            // System.out.println("Total elapsed: " + elapsed);
            // System.out.println("TPS: " + (double) count / ((double) elapsed / 1000));
        }
    }
}
