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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.MultiMapTest;
import org.fisco.bcos.sdk.demo.contract.MultiTableTest;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.BcosSDKException;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class PerformanceCRUDTable {
    private static AtomicLong atomicLong = new AtomicLong(0);
    private static ThreadPoolService threadPoolService =
            new ThreadPoolService("PerformanceCRUDTable", 1000000);
    private static Client client;
    private static MultiTableTest multiTableTest = null;
    private static MultiMapTest mapTest = null;
    private static int length = 256;
    private static String PrimaryKey;

    private static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceCRUDTable test===========");
        System.out.println(
                " \t java -cp \'conf/:lib/*:apps/*\' org.fisco.bcos.sdk.demo.perf.PerformanceCRUDTable [count] [tps] [groupId] [useTable] [valueLength].");
    }

    public static void main(String[] args) {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = PerformanceCRUDTable.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }
            if (args.length < 4) {
                Usage();
                return;
            }
            int count = Integer.parseInt(args[0]);
            int qps = Integer.parseInt(args[1]);
            String groupId = args[2];
            boolean useTable = Boolean.parseBoolean(args[3]);
            if (args.length == 5) {
                length = Integer.parseInt(args[4]);
            }
            PrimaryKey = String.valueOf(new Random().nextInt());
            System.out.println(
                    "====== PerformanceCRUDTable "
                            + "count: "
                            + count
                            + ", qps:"
                            + qps
                            + ", groupId: "
                            + groupId
                            + ", useTable:"
                            + useTable
                            + ", valueLength:"
                            + length
                            + ", PrimaryKey:"
                            + PrimaryKey);

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);

            // build the client
            client = sdk.getClient(groupId);
            if (client == null) {
                System.out.println("client is null");
                return;
            }

            start(groupId, count, qps, useTable);
            threadPoolService.stop();
            System.exit(0);
        } catch (BcosSDKException | ContractException | InterruptedException | IOException e) {
            System.out.println(
                    "====== PerformanceCRUDTable test failed, error message: " + e.getMessage());
            System.exit(0);
        }
    }

    public static void start(String groupId, int count, int qps, boolean useKV)
            throws IOException, InterruptedException, ContractException {

        String address;

        if (useKV) {
            System.out.println("====== Deploy TableTest ====== ");
            multiTableTest =
                    MultiTableTest.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            if (!multiTableTest.getDeployReceipt().isStatusOK()) {
                throw new ContractException(
                        "deploy failed: " + multiTableTest.getDeployReceipt().getMessage());
            }
            address = multiTableTest.getContractAddress();
            System.out.println(
                    "====== Deploy TableTest success, address: "
                            + multiTableTest.getContractAddress()
                            + " ====== ");
        } else {
            System.out.println("====== Deploy MultiMapTest ====== ");

            mapTest = MultiMapTest.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            if (!mapTest.getDeployReceipt().isStatusOK()) {
                throw new ContractException(
                        "deploy failed: " + mapTest.getDeployReceipt().getMessage());
            }
            address = mapTest.getContractAddress();
            System.out.println(
                    "====== Deploy MultiMapTest success, address: "
                            + mapTest.getContractAddress()
                            + " ====== ");
        }

        System.out.println(
                "====== Start test, count: " + count + ", qps:" + qps + ", groupId: " + groupId);

        AssembleTransactionProcessor assembleTransactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                        client,
                        client.getCryptoSuite().getCryptoKeyPair(),
                        useKV ? "MultiTableTest" : "MultiMapTest",
                        useKV ? MultiTableTest.getABI() : MultiMapTest.getABI(),
                        useKV
                                ? MultiTableTest.getBinary(client.getCryptoSuite())
                                : MultiMapTest.getBinary(client.getCryptoSuite()));

        set(count, qps, useKV, address, assembleTransactionProcessor);

        get(qps, useKV, address, assembleTransactionProcessor);
    }

    private static void set(
            int count,
            int qps,
            boolean useKV,
            String address,
            AssembleTransactionProcessor assembleTransactionProcessor)
            throws InterruptedException {
        RateLimiter limiter = RateLimiter.create(qps);
        String formatter = "%" + length + "s";
        CountDownLatch setLatch = new CountDownLatch(count);
        System.out.println("Setting data...");
        ProgressBar sentBar =
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

        Collector collector = new Collector();
        collector.setTotal(count);

        for (int i = 0; i < count; ++i) {
            limiter.acquire();
            long key = atomicLong.getAndIncrement();

            List<Object> params = new ArrayList<>(2);
            params.add(PrimaryKey + key);
            params.add(String.format(formatter, PrimaryKey + key));
            threadPoolService
                    .getThreadPool()
                    .execute(
                            () -> {
                                try {
                                    long now = System.currentTimeMillis();
                                    assembleTransactionProcessor.sendTransactionAsync(
                                            address,
                                            useKV ? MultiTableTest.getABI() : MultiMapTest.getABI(),
                                            "set",
                                            params,
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {
                                                    long cost = System.currentTimeMillis() - now;
                                                    collector.onMessage(receipt, cost);
                                                    setLatch.countDown();
                                                    receivedBar.step();
                                                }
                                            });
                                } catch (ContractCodecException e) {
                                    setLatch.countDown();
                                    atomicLong.getAndDecrement();
                                    e.printStackTrace();
                                }
                                sentBar.step();
                            });
        }
        setLatch.await();
        sentBar.close();
        receivedBar.close();
        collector.report();
        System.out.println("Set data finished!");
    }

    private static void get(
            int qps,
            boolean useKV,
            String address,
            AssembleTransactionProcessor assembleTransactionProcessor)
            throws InterruptedException {
        System.out.println("Getting data...");
        int callCount = Math.toIntExact(atomicLong.get());
        ProgressBar callBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(callCount)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar callbackBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(callCount)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        CountDownLatch getLatch = new CountDownLatch(callCount);
        RateLimiter callLimiter = RateLimiter.create(qps);
        Collector getCollector = new Collector();
        getCollector.setTotal(callCount);

        for (int i = 0; i < callCount; ++i) {
            List<Object> params = new ArrayList<>(1);
            long key = atomicLong.decrementAndGet();
            params.add(PrimaryKey + key);
            threadPoolService
                    .getThreadPool()
                    .execute(
                            () -> {
                                try {
                                    callLimiter.acquire();
                                    long now = System.currentTimeMillis();

                                    assembleTransactionProcessor.sendTransactionAsync(
                                            address,
                                            useKV ? MultiTableTest.getABI() : MultiMapTest.getABI(),
                                            "get",
                                            params,
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {
                                                    long cost = System.currentTimeMillis() - now;
                                                    getCollector.onMessage(receipt, cost);
                                                    callbackBar.step();
                                                    getLatch.countDown();
                                                }
                                            });
                                } catch (ContractCodecException e) {
                                    e.printStackTrace();
                                    getLatch.countDown();
                                }
                                callBar.step();
                            });
        }
        getLatch.await();

        callBar.close();
        callbackBar.close();
        getCollector.report();

        System.out.println("Getting data finished!");
    }
}
