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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.lang3.RandomStringUtils;
import org.fisco.bcos.sdk.demo.contract.TigerHoleV2;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple5;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple6;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

public class PerformanceTiger {
    private static Client client;
    private static RateLimiter limiter;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceDMC test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTiger [groupId] [count] [qps].");
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
            boolean isParallel = true;
            if (args.length < 3) {
                Usage();
                return;
            } else {
                if (args.length == 4) {
                    isParallel = Boolean.parseBoolean(args[3]);
                }
            }
            String groupId = args[0];
            Integer count = Integer.valueOf(args[1]).intValue();
            Integer qps = Integer.valueOf(args[2]).intValue();

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            ExecutorService executor =
                    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            limiter = RateLimiter.create(qps.intValue());

            start(groupId, count, qps, executor, isParallel);

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static Map<String, Integer> createSeedUsers(
            TigerHoleV2 tigerHole, int count, ExecutorService executor)
            throws InterruptedException {
        AtomicLong totalCost = new AtomicLong(0);
        Collector collector = new Collector();
        collector.setTotal(count);

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

        Map<String, Integer> seedUsers = new ConcurrentHashMap<String, Integer>();
        final Random random = new Random();

        CountDownLatch transactionLatch = new CountDownLatch(count);
        AtomicInteger tigerIDStart = new AtomicInteger(random.nextInt(22 * 10000));

        System.out.println("Creating seed users...");
        for (int i = 0; i < count; ++i) {
            executor.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            limiter.acquire();

                            final String openID = RandomStringUtils.random(32);
                            int tigerID = tigerIDStart.addAndGet(1);

                            String randomStr1 = RandomStringUtils.random(32);
                            String randomStr2 = RandomStringUtils.random(32);

                            long now = System.currentTimeMillis();

                            tigerHole.tradeTiger(
                                    openID,
                                    BigInteger.valueOf(tigerID),
                                    randomStr1,
                                    randomStr2,
                                    BigInteger.valueOf(0),
                                    BigInteger.valueOf(0),
                                    "",
                                    new TransactionCallback() {
                                        @Override
                                        public void onResponse(TransactionReceipt receipt) {
                                            if (receipt.isStatusOK()) {
                                                BigInteger code =
                                                        new BigInteger(
                                                                receipt.getOutput().substring(2),
                                                                16);
                                                if (code.intValue() == 0) {
                                                    // Success insert
                                                    seedUsers.put(openID, tigerID);
                                                } else {
                                                    System.err.println(
                                                            "Error, code: "
                                                                    + code.intValue()
                                                                    + ", "
                                                                    + tigerID);
                                                }
                                            } else {
                                                System.err.println(
                                                        "Error! "
                                                                + receipt.getMessage()
                                                                + "status:"
                                                                + receipt.getStatus());
                                            }

                                            long cost = System.currentTimeMillis() - now;
                                            collector.onMessage(receipt, cost);
                                            receivedBar.step();
                                            transactionLatch.countDown();
                                            totalCost.addAndGet(System.currentTimeMillis() - now);
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

        System.out.println("Create seed user finished!");
        return seedUsers;
    }

    public static void publishCards(
            TigerHoleV2 tigerHole, Map<String, Integer> seedUsers, ExecutorService executor)
            throws InterruptedException {
        AtomicLong totalCost = new AtomicLong(0);
        Collector collector = new Collector();

        int count = seedUsers.size() * 10;
        collector.setTotal(count);

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

        final Random random = new Random();
        CountDownLatch transactionLatch = new CountDownLatch(count);
        AtomicInteger tigerIDStart = new AtomicInteger(22 * 10000);

        System.out.println("Publish card...");
        for (int i = 0; i < count; ++i) {
            executor.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            limiter.acquire();

                            int index = random.nextInt(seedUsers.size());
                            Entry<String, Integer> entry =
                                    (Entry<String, Integer>) seedUsers.entrySet().toArray()[index];
                            final String fromOpenID = entry.getKey();
                            final String toOpenID = RandomStringUtils.random(32);
                            final String cardID = RandomStringUtils.random(32);
                            int tigerID = tigerIDStart.addAndGet(1);

                            long now = System.currentTimeMillis();

                            tigerHole.tradeTiger(
                                    toOpenID,
                                    BigInteger.valueOf(tigerID),
                                    fromOpenID,
                                    cardID,
                                    BigInteger.valueOf(1),
                                    BigInteger.valueOf(10),
                                    "",
                                    new TransactionCallback() {
                                        @Override
                                        public void onResponse(TransactionReceipt receipt) {
                                            if (receipt.isStatusOK()) {
                                                BigInteger code =
                                                        new BigInteger(
                                                                receipt.getOutput().substring(2),
                                                                16);
                                                if (code.intValue() == 0) {
                                                    // Success get
                                                } else {
                                                    if (code.intValue() == -3) {
                                                        // Tiger own limit
                                                        Tuple6<
                                                                        List<BigInteger>,
                                                                        List<String>,
                                                                        List<String>,
                                                                        BigInteger,
                                                                        BigInteger,
                                                                        Boolean>
                                                                result;
                                                        try {
                                                            result = tigerHole.getUser(toOpenID);
                                                        } catch (ContractException e) {
                                                            e.printStackTrace();
                                                            return;
                                                        }

                                                        if (result.getValue1().size() != 1024) {
                                                            System.err.println(
                                                                    "User tigers limit mismatch!");
                                                        }
                                                    } else if (code.intValue() == -5) {
                                                        Tuple5<
                                                                        String,
                                                                        BigInteger,
                                                                        BigInteger,
                                                                        List<String>,
                                                                        List<String>>
                                                                result;
                                                        try {
                                                            result = tigerHole.getCard(fromOpenID);
                                                        } catch (ContractException e) {
                                                            e.printStackTrace();
                                                            return;
                                                        }

                                                        if (result.getValue2().intValue()
                                                                != result.getValue3().intValue()) {
                                                            System.err.println(
                                                                    "Card limit mismatch!");
                                                        }
                                                    } else {
                                                        System.err.println(
                                                                "Unknown error! "
                                                                        + code.intValue());
                                                    }
                                                }
                                            } else {
                                                System.err.println(
                                                        "Error! "
                                                                + receipt.getMessage()
                                                                + "status:"
                                                                + receipt.getStatus());
                                            }

                                            long cost = System.currentTimeMillis() - now;
                                            collector.onMessage(receipt, cost);
                                            receivedBar.step();
                                            transactionLatch.countDown();
                                            totalCost.addAndGet(System.currentTimeMillis() - now);
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

        System.out.println("Publish card finished!");
    }

    public static void mergeTigers(
            TigerHoleV2 tigerHole, Map<String, Integer> seedUsers, ExecutorService executor)
            throws InterruptedException {
        AtomicLong totalCost = new AtomicLong(0);
        Collector collector = new Collector();

        int count = seedUsers.size() * 10;
        collector.setTotal(count);

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

        final Random random = new Random();
        CountDownLatch transactionLatch = new CountDownLatch(count);
        AtomicInteger tigerIDStart = new AtomicInteger(2 * 22 * 10000);

        System.out.println("Merging tiger...");
        for (int i = 0; i < count; ++i) {
            executor.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            limiter.acquire();

                            int index = random.nextInt(seedUsers.size());
                            Entry<String, Integer> entry =
                                    (Entry<String, Integer>) seedUsers.entrySet().toArray()[index];
                            final String fromOpenID = entry.getKey();
                            final String toOpenID = RandomStringUtils.random(32);
                            int tigerID = tigerIDStart.addAndGet(1);

                            long now = System.currentTimeMillis();

                            List<BigInteger> tigerIDs = new ArrayList<BigInteger>();
                            int mergeCount = random.nextInt(10);
                            if (mergeCount < 2) {
                                mergeCount = 2;
                            }

                            for (int i = 0; i < mergeCount; ++i) {
                                int indexTiger = random.nextInt(seedUsers.size());
                                Entry<String, Integer> tigerEntry =
                                        (Entry<String, Integer>)
                                                seedUsers.entrySet().toArray()[index];
                                tigerIDs.add(BigInteger.valueOf(tigerEntry.getValue()));
                            }

                            tigerHole.tradeTiger(
                                    toOpenID,
                                    BigInteger.valueOf(tigerID),
                                    fromOpenID,
                                    fromOpenID,
                                    BigInteger.valueOf(2),
                                    BigInteger.valueOf(10),
                                    "",
                                    new TransactionCallback() {
                                        @Override
                                        public void onResponse(TransactionReceipt receipt) {
                                            if (receipt.isStatusOK()) {
                                                BigInteger code =
                                                        new BigInteger(
                                                                receipt.getOutput().substring(2),
                                                                16);
                                                if (code.intValue() == 0) {
                                                    // Success get
                                                } else {
                                                    if (code.intValue() == -3) {
                                                        // Tiger own limit
                                                        Tuple6<
                                                                        List<BigInteger>,
                                                                        List<String>,
                                                                        List<String>,
                                                                        BigInteger,
                                                                        BigInteger,
                                                                        Boolean>
                                                                result;
                                                        try {
                                                            result = tigerHole.getUser(toOpenID);
                                                        } catch (ContractException e) {
                                                            e.printStackTrace();
                                                            return;
                                                        }

                                                        if (result.getValue1().size() != 1024) {
                                                            System.err.println(
                                                                    "User tigers limit mismatch!");
                                                        }
                                                    } else if (code.intValue() == -5) {
                                                        Tuple5<
                                                                        String,
                                                                        BigInteger,
                                                                        BigInteger,
                                                                        List<String>,
                                                                        List<String>>
                                                                result;
                                                        try {
                                                            result = tigerHole.getCard(fromOpenID);
                                                        } catch (ContractException e) {
                                                            e.printStackTrace();
                                                            return;
                                                        }

                                                        if (result.getValue2().intValue()
                                                                != result.getValue3().intValue()) {
                                                            System.err.println(
                                                                    "Card limit mismatch!");
                                                        }
                                                    } else {
                                                        System.err.println(
                                                                "Unknown error! "
                                                                        + code.intValue());
                                                    }
                                                }
                                            } else {
                                                System.err.println(
                                                        "Error! "
                                                                + receipt.getMessage()
                                                                + "status:"
                                                                + receipt.getStatus());
                                            }

                                            long cost = System.currentTimeMillis() - now;
                                            collector.onMessage(receipt, cost);
                                            receivedBar.step();
                                            transactionLatch.countDown();
                                            totalCost.addAndGet(System.currentTimeMillis() - now);
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

        System.out.println("mergeTigers finished!");
    }

    public static void start(
            String groupId, int count, Integer qps, ExecutorService executor, boolean isParallel)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start test, count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId
                        + ", isParallel: "
                        + isParallel);

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        System.out.println("Create tiger hole...");
        TigerHoleV2 tigerHole =
                TigerHoleV2.deploy(client, client.getCryptoSuite().getCryptoKeyPair(), isParallel);
        // tigerHole.enableParallel();
        System.out.println("Create tiger v2 hole finished!");

        Map<String, Integer> seedUsers = createSeedUsers(tigerHole, count, executor);

        publishCards(tigerHole, seedUsers, executor);
        mergeTigers(tigerHole, seedUsers, executor);
    }
}
