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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.TableTest;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.TransactionReceiptStatus;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class PerformanceTable {
    private static AtomicLong uniqueID = new AtomicLong(0);
    private static final Set<String> supportCommands =
            new HashSet<>(
                    Arrays.asList("insert", "update", "remove", "select", "create", "batchCreate"));
    private static int FLAG_NUMBER = 100;

    private static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceTable test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [insert] [count] [tps] [groupId].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [update] [count] [tps] [groupId].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [remove] [count] [tps] [groupId].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [select] [count] [tps] [groupId].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [create] [count] [tps] [groupId] [mod].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [batchCreate] [count] [tps] [groupId] [batchNumber].");
    }

    public static void main(String[] args) {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = PerformanceTable.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }
            if (args.length < 4) {
                Usage();
                return;
            }
            String command = args[0];
            int sendCount = Integer.parseInt(args[1]);
            int qps = Integer.parseInt(args[2]);
            String groupId = args[3];
            if (args.length == 5) {
                FLAG_NUMBER = Integer.parseInt(args[4]);
            }
            System.out.println(
                    "====== PerformanceTable "
                            + command
                            + ", count: "
                            + sendCount
                            + ", qps:"
                            + qps
                            + ", groupId: "
                            + groupId);

            if (!supportCommands.contains(command)) {
                System.out.println("Command " + command + " not supported!");
                Usage();
                return;
            }

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);

            // build the client
            Client client = sdk.getClient(groupId);
            if (client == null) {
                System.out.println("client is null");
                return;
            }

            // deploy the TableTest
            System.out.println("====== Deploy TableTest ====== ");
            TableTest tableTest =
                    TableTest.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            if (tableTest.getDeployReceipt().getStatus()
                    != TransactionReceiptStatus.Success.getCode()) {
                throw new ContractException(
                        "deploy failed: " + tableTest.getDeployReceipt().getMessage());
            }
            System.out.println(
                    "====== Deploy TableTest success, address: "
                            + tableTest.getContractAddress()
                            + " ====== ");

            int totalCount = sendCount;
            if (command.equals("batchCreate")) {
                sendCount = sendCount / FLAG_NUMBER;
                totalCount = sendCount * FLAG_NUMBER;
                System.out.println(
                        "====== batchCreate, "
                                + "batchSendCount: "
                                + sendCount
                                + ", eachBatchSendTx:"
                                + FLAG_NUMBER
                                + ", totalCount: "
                                + totalCount);
            }
            CountDownLatch countDownLatch = new CountDownLatch(totalCount);
            RateLimiter limiter = RateLimiter.create(qps);
            ProgressBar sentBar =
                    new ProgressBarBuilder()
                            .setTaskName("Send   :")
                            .setInitialMax(sendCount)
                            .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                            .build();
            ProgressBar receivedBar =
                    new ProgressBarBuilder()
                            .setTaskName("Receive:")
                            .setInitialMax(totalCount)
                            .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                            .build();

            System.out.println("====== PerformanceTable " + command + " start ======");
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "PerformanceTable", Runtime.getRuntime().availableProcessors());

            Collector collector = new Collector();
            collector.setTotal(totalCount);
            for (int i = 0; i < sendCount; ++i) {
                limiter.acquire();
                threadPoolService
                        .getThreadPool()
                        .execute(
                                () -> {
                                    long now = System.currentTimeMillis();
                                    callTableOperation(
                                            command,
                                            tableTest,
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {
                                                    long cost = System.currentTimeMillis() - now;
                                                    collector.onMessage(receipt, cost);
                                                    receivedBar.step();
                                                    countDownLatch.countDown();
                                                }
                                            });
                                    sentBar.step();
                                });
            }
            // wait to collect all the receipts
            countDownLatch.await();
            receivedBar.close();
            sentBar.close();
            collector.report();
            threadPoolService.stop();
            System.exit(0);
        } catch (Exception e) {
            System.out.println(
                    "====== PerformanceTable test failed, error message: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void callTableOperation(
            String command, TableTest tableTest, TransactionCallback callback) {
        if (command.compareToIgnoreCase("insert") == 0) {
            insert(tableTest, callback);
        }
        if (command.compareToIgnoreCase("update") == 0) {
            update(tableTest, callback);
        }
        if (command.compareToIgnoreCase("remove") == 0) {
            remove(tableTest, callback);
        }
        if (command.compareToIgnoreCase("select") == 0) {
            select(tableTest, callback);
        }
        if (command.compareToIgnoreCase("create") == 0) {
            create(tableTest, callback);
        }
        if (command.compareToIgnoreCase("batchCreate") == 0) {
            batchCreate(tableTest, callback);
        }
    }

    public static long getNextID() {
        return uniqueID.getAndIncrement();
    }

    private static String getId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    private static void insert(TableTest tableTest, TransactionCallback callback) {
        long nextID = getNextID();
        tableTest.insert("fruit" + nextID, String.valueOf(nextID), "apple" + getId(), callback);
    }

    private static void update(TableTest tableTest, TransactionCallback callback) {
        long nextID = getNextID();
        tableTest.update("fruit" + nextID, String.valueOf(nextID), "apple" + getId(), callback);
    }

    private static void remove(TableTest tableTest, TransactionCallback callback) {
        long nextID = getNextID();
        tableTest.remove("fruit" + nextID, callback);
    }

    private static void select(TableTest tableTest, TransactionCallback callback) {
        TransactionReceipt receipt = new TransactionReceipt();
        try {
            long nextID = getNextID();
            tableTest.select("fruit" + nextID);
            receipt.setStatus(0);
            callback.onResponse(receipt);
        } catch (Exception e) {
            receipt.setStatus(-1);
            callback.onResponse(receipt);
        }
    }

    private static void create(TableTest tableTest, TransactionCallback callback) {
        long nextID = new Random().nextInt();
        String tableName = "t_test" + Math.abs(nextID);
        String key = "key" + nextID;
        List<String> fields = new ArrayList<>();
        fields.add("name" + nextID);
        fields.add("age" + nextID);
        tableTest.createTable(tableName, key, fields, callback);
    }

    private static void batchCreate(TableTest tableTest, TransactionCallback callback) {
        int nextID = new Random().nextInt();
        for (int i = 0; i < FLAG_NUMBER; i++) {
            String tableName = "t_test" + Math.abs(nextID);
            String key = "key" + nextID;
            List<String> fields = new ArrayList<>();
            fields.add("name" + nextID);
            fields.add("age" + nextID);
            tableTest.createTable(tableName, key, fields, callback);
        }
    }
}
