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
import org.fisco.bcos.sdk.demo.contract.DelegateCallTest;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class PerformanceDelegateCall {
    private static Client client;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceDelegateCall test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceDelegateCall [groupId] [contractCount] [count] [qps].");
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
            int contractCount = Integer.valueOf(args[1]).intValue();
            Integer count = Integer.valueOf(args[2]).intValue();
            Integer qps = Integer.valueOf(args[3]).intValue();

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "DelegateCallClient", Runtime.getRuntime().availableProcessors());

            start(groupId, contractCount, count, qps, threadPoolService);

            threadPoolService.getThreadPool().awaitTermination(0, TimeUnit.SECONDS);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void start(
            String groupId,
            int contractCount,
            int count,
            Integer qps,
            ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start test, contract count: "
                        + contractCount
                        + ", count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId);

        RateLimiter limiter = RateLimiter.create(qps.intValue());

        DelegateCallTest[] contracts = new DelegateCallTest[contractCount];
        String[] delegateDests = new String[contractCount];
        Map<Integer, AtomicLong> summary = new ConcurrentHashMap<Integer, AtomicLong>();

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        System.out.println("Create contract...");
        CountDownLatch userLatch = new CountDownLatch(contractCount);
        for (int i = 0; i < contractCount; ++i) {
            final int index = i;
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                public void run() {
                                    DelegateCallTest contract;
                                    try {

                                        limiter.acquire();
                                        contract =
                                                DelegateCallTest.deploy(
                                                        client,
                                                        client.getCryptoSuite().getCryptoKeyPair());
                                        summary.put(index, new AtomicLong(0));
                                        delegateDests[index] = contract.delegateDest();
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

        for (int i = 0; i < count; ++i) {
            limiter.acquire();

            final int index = i % contracts.length;
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    DelegateCallTest contract = contracts[index];
                                    String delegateDest = delegateDests[index * 7 % contractCount];
                                    long now = System.currentTimeMillis();

                                    final long value = 2;
                                    contract.codesizeAt(
                                            delegateDest,
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(
                                                        TransactionReceipt receipt) {}
                                            });

                                    contract.codesizeAt(
                                            delegateDest,
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(
                                                        TransactionReceipt receipt) {}
                                            });

                                    contract.testSuccess(
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {
                                                    if (receipt.getStatus() == 0) {
                                                        AtomicLong count = summary.get(index);
                                                        count.addAndGet(value);
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
                                        BigInteger balance = contracts[index].value();
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
        System.out.println("Revert(ERROR) is ok. Reverted tx num: " + collector.getError());
        System.out.println("Checking finished!");

        // collector.
        // System.out.println("Total elapsed: " + elapsed);
        // System.out.println("TPS: " + (double) count / ((double) elapsed / 1000));
    }
}
