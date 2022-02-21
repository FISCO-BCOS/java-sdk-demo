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
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.codec.datatypes.Bool;
import org.fisco.bcos.sdk.demo.contract.Account;
import org.fisco.bcos.sdk.demo.contract.ParallelCpuHeavy;
import org.fisco.bcos.sdk.model.ConstantConfig;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.utils.ThreadPoolService;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceCpuHeavy {
    private final static int DEFAULT_SORT_ARRAY_SIZE = 32;
    private static Client client;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== Performance Cpu Heavy test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceCpuHeavy [groupId] [contractsNum] [count] [qps] [parallel(true/false)].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceCpuHeavy [groupId] [contractsNum] [count] [qps] [parallel(true/false)] [sort array size(default " + DEFAULT_SORT_ARRAY_SIZE + ")].");
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

            if (args.length < 5) {
                Usage();
                return;
            }
            String groupId = args[0];
            int contractsNum = Integer.valueOf(args[1]).intValue();
            Integer count = Integer.valueOf(args[2]).intValue();
            Integer qps = Integer.valueOf(args[3]).intValue();
            boolean enableParallel = Boolean.valueOf(args[4]);
            Integer sortArraySize = DEFAULT_SORT_ARRAY_SIZE;
            if (args.length == 6) {
                sortArraySize = Integer.valueOf(args[5]).intValue();
            }


            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            ThreadPoolService threadPoolService =
                    new ThreadPoolService("DMCClient", Runtime.getRuntime().availableProcessors());

            start(groupId, contractsNum, count, qps, enableParallel, sortArraySize, threadPoolService);

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
            int count,
            Integer qps,
            boolean enableParallel,
            Integer sortArraySize,
            ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start test, contracts num: "
                        + contractsNum
                        + ", count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId
                        + ", enableParallel: "
                        + enableParallel);

        RateLimiter limiter = RateLimiter.create(qps.intValue());

        ParallelCpuHeavy[] contracts = new ParallelCpuHeavy[contractsNum];

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        System.out.println("Create contract...");
        CountDownLatch userLatch = new CountDownLatch(contractsNum);
        for (int i = 0; i < contractsNum; ++i) {
            final int index = i;
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                public void run() {
                                    ParallelCpuHeavy contract;
                                    try {
                                        long initBalance = Math.abs(random.nextLong());

                                        limiter.acquire();
                                        contract =
                                                ParallelCpuHeavy.deploy(
                                                        client,
                                                        client.getCryptoSuite().getCryptoKeyPair());
                                        if (enableParallel) {
                                            contract.enableParallel();
                                        }

                                        contracts[index] = contract;
                                        userLatch.countDown();
                                    } catch (ContractException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
        }
        userLatch.await();
        System.out.println("Create contract finished!");

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
        client.setDAG(enableParallel);

        for (int i = 0; i < count; ++i) {
            limiter.acquire();

            final int index = i % contracts.length;
            final long signature = i; // See ParallelCpuHeavy.sol
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    ParallelCpuHeavy contract = contracts[index];

                                    long now = System.currentTimeMillis();

                                    final long value = Math.abs(random.nextLong() % 1000);


                                    contract.sort(BigInteger.valueOf(sortArraySize.longValue()), BigInteger.valueOf(signature), new TransactionCallback() {
                                        @Override
                                        public void onResponse(TransactionReceipt receipt) {
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

        System.out.println("Sending transactions finished!");

        sendedBar.close();
        receivedBar.close();
        collector.report();

        // collector.
        // System.out.println("Total elapsed: " + elapsed);
        // System.out.println("TPS: " + (double) count / ((double) elapsed / 1000));
    }
}
