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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.lang3.RandomStringUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.demo.contract.TigerHole;
import org.fisco.bcos.sdk.demo.contract.TigerHole2;
import org.fisco.bcos.sdk.model.ConstantConfig;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.utils.ThreadPoolService;

public class PerformanceTiger {
    private static Client client;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceDMC test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTiger [groupId] [count] [qps] [contractAddress].");
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
            int groupId = Integer.valueOf(args[0]).intValue();
            Integer count = Integer.valueOf(args[1]).intValue();
            Integer qps = Integer.valueOf(args[2]).intValue();

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "TigerClient", Runtime.getRuntime().availableProcessors());

            start(groupId, count, qps, threadPoolService);

            threadPoolService.getThreadPool().awaitTermination(0, TimeUnit.SECONDS);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void start(
            int groupId, int count, Integer qps, ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start test, count: " + count + ", qps:" + qps + ", groupId: " + groupId);

        RateLimiter limiter = RateLimiter.create(qps.intValue());

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        System.out.println("Create tiger hole...");
        TigerHole2 tigerHole = TigerHole2.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
        tigerHole.enableParallel();
        System.out.println("Create tiger hole finished!");

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

        final ConcurrentHashMap<Integer, String> results = new ConcurrentHashMap<Integer, String>();
        final AtomicInteger duplicateCount = new AtomicInteger(0);

        for (int i = 0; i < count; ++i) {
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    limiter.acquire();

                                    final String openID =
                                            RandomStringUtils.random(32); // random open id
                                    final int tigerID =
                                            random.nextInt(2022 * 10000); // total 2022w tigers

                                    long now = System.currentTimeMillis();

                                    tigerHole.setTiger(
                                            openID,
                                            BigInteger.valueOf(tigerID),
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {
                                                    if (receipt.isStatusOK()) {
                                                        BigInteger inserted =
                                                                new BigInteger(
                                                                        receipt.getOutput()
                                                                                .substring(2),
                                                                        16);
                                                        if (inserted.intValue() == 1) {
                                                            // Success insert
                                                            results.put(tigerID, openID);
                                                        } else {
                                                            duplicateCount.addAndGet(1);
                                                            // System.out.println(
                                                            // "Duplicate tiger: "
                                                            // + openID
                                                            // + ":"
                                                            // + tigerID);
                                                        }
                                                    } else {
                                                        System.err.println(
                                                                "Error! " + receipt.getMessage());
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
        for (Map.Entry<Integer, String> entry : results.entrySet()) {
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        limiter.acquire();

                                        String openID =
                                                tigerHole.getOpenIDByTiger(
                                                        BigInteger.valueOf(entry.getKey()));

                                        if (!openID.equals(entry.getValue())) {
                                            System.out.println(
                                                    "Check failed! tigerID: ["
                                                            + entry.getKey()
                                                            + "] Expected openID["
                                                            + entry.getValue()
                                                            + "] not equal to ["
                                                            + openID
                                                            + "]");
                                        }

                                        checkLatch.countDown();
                                    } catch (ContractException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
        }
        System.out.println("Checking finished!");
    }
}
