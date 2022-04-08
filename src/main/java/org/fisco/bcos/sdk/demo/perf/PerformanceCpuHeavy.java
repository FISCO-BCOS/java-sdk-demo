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
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.CpuHeavyContract;
import org.fisco.bcos.sdk.demo.contract.CpuHeavyPrecompiled;
import org.fisco.bcos.sdk.demo.contract.ParallelCpuHeavy;
import org.fisco.bcos.sdk.demo.contract.RandomPrecompiled;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class PerformanceCpuHeavy {
    private static final int DEFAULT_SORT_ARRAY_SIZE = 100000;
    private static Client client;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== Performance Cpu Heavy test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceCpuHeavy [groupId] [precompiled/solidity] [contractsNum] [count] [qps] [parallel(true/false)].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceCpuHeavy [groupId] [precompiled/solidity] [contractsNum] [count] [qps] [parallel(true/false)] [sort array size(default "
                        + DEFAULT_SORT_ARRAY_SIZE
                        + ")] [undeterministicTxPercent(Default is 0)].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceCpuHeavy generate [groupID] [contractsNum] [count] [parallel(true/false)] [sort array size(default "
                        + DEFAULT_SORT_ARRAY_SIZE
                        + ")] [undeterministicTxPercent(Default is 0)].");
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
            generatePrecompiledTxs(args, configUrl);
            if (args.length < 6) {
                Usage();
                return;
            }
            String groupId = args[0];
            String type = args[1];
            int contractsNum = Integer.valueOf(args[2]).intValue();
            Integer count = Integer.valueOf(args[3]).intValue();
            Integer qps = Integer.valueOf(args[4]).intValue();
            boolean enableParallel = Boolean.valueOf(args[5]);
            Integer sortArraySize = DEFAULT_SORT_ARRAY_SIZE;
            if (args.length >= 7) {
                sortArraySize = Integer.valueOf(args[6]).intValue();
            }
            Integer undeterministicTxsPercent = 0;
            if (args.length >= 8) {
                undeterministicTxsPercent = Integer.valueOf(args[7]).intValue();
            }
            if (!type.equals("precompiled") && !type.equals("solidity")) {
                Usage();
                return;
            }
            boolean isPrecompiled = type.equals("precompiled");

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "CpuHeavyClient", Runtime.getRuntime().availableProcessors());

            start(
                    groupId,
                    isPrecompiled,
                    contractsNum,
                    count,
                    qps,
                    enableParallel,
                    sortArraySize,
                    threadPoolService,
                    undeterministicTxsPercent);

            threadPoolService.getThreadPool().awaitTermination(0, TimeUnit.SECONDS);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void generatePrecompiledTxs(String[] args, URL configUrl) throws IOException {
        String command = args[0];
        if (!command.equals("generate")) {
            return;
        }
        String txsFile = "cpuHeavyPrecompiledTxs.txt";
        String groupId = args[1];
        int contractsNum = Integer.valueOf(args[2]).intValue();
        Integer count = Integer.valueOf(args[3]).intValue();

        String configFile = configUrl.getPath();
        BcosSDK sdk = BcosSDK.build(configFile);
        client = sdk.getClient(groupId);
        boolean enableDAG = Boolean.valueOf(args[4]);
        Integer sortArraySize = DEFAULT_SORT_ARRAY_SIZE;
        if (args.length >= 6) {
            sortArraySize = Integer.valueOf(args[5]).intValue();
        }
        Integer undeterministicTxsPercent = 0;
        if (args.length >= 7) {
            undeterministicTxsPercent = Integer.valueOf(args[6]).intValue();
        }
        File file = new File(txsFile);
        if (!file.exists()) {
            file.createNewFile();
        }
        CpuHeavyPrecompiled[] contracts = new CpuHeavyPrecompiled[contractsNum];

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        ProgressBar generateBar =
                new ProgressBarBuilder()
                        .setTaskName("Generate   :")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        RandomPrecompiled randomPrecompiledContract =
                RandomPrecompiled.load(
                        "0x0000000000000000000000000000000000040001",
                        client,
                        client.getCryptoSuite().getCryptoKeyPair());
        // precompiled
        for (int i = 0; i < contractsNum; ++i) {
            contracts[i] =
                    CpuHeavyPrecompiled.load(i, client, client.getCryptoSuite().getCryptoKeyPair());
            (contracts[i]).setEnableDAG(enableDAG);
        }
        FileWriter fileWriter = new FileWriter(file.getName(), true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        int undeterministicTxs = 0;
        if (undeterministicTxsPercent > 0) {
            undeterministicTxs =
                    (int) ((double) count * (double) undeterministicTxsPercent / (double) 100);
        }
        System.out.println(
                "PerformanceCpuHeavy: test generateTransferTxs, count: "
                        + count
                        + ", txsFile: "
                        + txsFile
                        + ", enableDag: "
                        + enableDAG
                        + ", sortArraySize: "
                        + sortArraySize
                        + ", undeterministicTxsPercent:"
                        + undeterministicTxsPercent
                        + "%"
                        + ", undeterministicTxs: "
                        + undeterministicTxs);
        System.out.println("===================================================================");

        for (int i = 0; i < count; ++i) {
            final int index = i % contracts.length;
            String txData;
            if (i <= undeterministicTxs) {
                txData = randomPrecompiledContract.getSignedTransactionForGenerateRandomValue();
            } else {
                final long signature = i; // See ParallelCpuHeavy.sol
                CpuHeavyPrecompiled contract = contracts[index];
                txData =
                        contract.getSignedTransactionForSort(
                                BigInteger.valueOf(sortArraySize.longValue()),
                                BigInteger.valueOf(signature));
            }
            bufferedWriter.write(txData);
            bufferedWriter.newLine();
            generateBar.step();
        }
        generateBar.close();
        bufferedWriter.close();
        System.exit(0);
        System.out.println("===================================================================");
    }

    public static void start(
            String groupId,
            boolean isPrecompiled,
            int contractsNum,
            int count,
            Integer qps,
            boolean enableParallel,
            Integer sortArraySize,
            ThreadPoolService threadPoolService,
            Integer undeterministicTxsPercent)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start "
                        + (isPrecompiled ? "precompiled" : "solidity")
                        + " test, contracts num: "
                        + contractsNum
                        + ", count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId
                        + ", enableParallel: "
                        + enableParallel
                        + ", undeterministicTxsPercent:"
                        + undeterministicTxsPercent
                        + "%");

        RateLimiter limiter = RateLimiter.create(qps.intValue());

        CpuHeavyContract[] contracts = new CpuHeavyContract[contractsNum];

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        if (!isPrecompiled) {
            // solidity
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
                                                            client.getCryptoSuite()
                                                                    .getCryptoKeyPair());
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
        } else {
            // precompiled
            for (int i = 0; i < contractsNum; ++i) {
                contracts[i] =
                        CpuHeavyPrecompiled.load(
                                i, client, client.getCryptoSuite().getCryptoKeyPair());
                (contracts[i]).setEnableDAG(enableParallel);
            }
        }
        RandomPrecompiled randomPrecompiledContract =
                RandomPrecompiled.load(
                        "0x0000000000000000000000000000000000040001",
                        client,
                        client.getCryptoSuite().getCryptoKeyPair());
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
        AtomicLong undeterministicTxsCount = new AtomicLong(0);
        Collector collector = new Collector();
        collector.setTotal(count);

        int undeterministicTxs = 0;
        if (undeterministicTxsPercent > 0) {
            undeterministicTxs =
                    (int) ((double) count * (double) undeterministicTxsPercent / (double) 100);
        }
        final int undeterministicTxsNum = undeterministicTxs;
        for (int i = 0; i < count; ++i) {
            final int index = i % contracts.length;
            final int num = i;
            final long signature = i; // See ParallelCpuHeavy.sol
            limiter.acquire();
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    long now = System.currentTimeMillis();
                                    // send undeterministic txs
                                    if (num <= undeterministicTxsNum) {
                                        randomPrecompiledContract.generateRandomValue(
                                                new TransactionCallback() {
                                                    @Override
                                                    public void onResponse(
                                                            TransactionReceipt receipt) {
                                                        long cost =
                                                                System.currentTimeMillis() - now;
                                                        collector.onMessage(receipt, cost);
                                                        receivedBar.step();
                                                        transactionLatch.countDown();
                                                        totalCost.addAndGet(
                                                                System.currentTimeMillis() - now);
                                                    }
                                                });
                                        undeterministicTxsCount.incrementAndGet();
                                        sendedBar.step();
                                        return;
                                    }
                                    CpuHeavyContract contract = contracts[index];

                                    final long value = Math.abs(random.nextLong() % 1000);

                                    contract.sort(
                                            BigInteger.valueOf(sortArraySize.longValue()),
                                            BigInteger.valueOf(signature),
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {
                                                    long cost = System.currentTimeMillis() - now;
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
        transactionLatch.await();

        sendedBar.close();
        receivedBar.close();
        errorBar.close();
        collector.report();
        System.out.println("====== Sending transactions finished! =========");
        System.out.println(
                "undeterministic transaction: " + String.valueOf(undeterministicTxsCount));
        // collector.
        // System.out.println("Total elapsed: " + elapsed);
        // System.out.println("TPS: " + (double) count / ((double) elapsed / 1000));
    }
}
