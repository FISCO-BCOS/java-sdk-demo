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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.TableTestLiquid;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class PerformanceTableLiquid {
    private static AtomicLong uniqueID = new AtomicLong(0);
    private static final Set<String> supportCommands =
            new HashSet<>(Arrays.asList("insert", "update", "remove", "select"));

    private static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceTableLiquid test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTableLiquid [insert] [count] [tps] [groupId].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTableLiquid [update] [count] [tps] [groupId].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTableLiquid [remove] [count] [tps] [groupId].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTableLiquid [select] [count] [tps] [groupId].");
    }

    public static void main(String[] args) {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl =
                    PerformanceTableLiquid.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }
            if (args.length < 4) {
                Usage();
                return;
            }
            String command = args[0];
            int count = Integer.parseInt(args[1]);
            int qps = Integer.parseInt(args[2]);
            String groupId = args[3];
            System.out.println(
                    "====== PerformanceTableLiquid "
                            + command
                            + ", count: "
                            + count
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
            String tableTestPath = "table_test" + new Random().nextInt(Integer.MAX_VALUE);
            System.out.println("====== Deploy contract address: " + tableTestPath + " ====== ");

            TableTestLiquid tableTestLiquid =
                    TableTestLiquid.deploy(
                            client, client.getCryptoSuite().getCryptoKeyPair(), tableTestPath);
            if (!tableTestLiquid.getDeployReceipt().isStatusOK()) {
                throw new ContractException(
                        "deploy failed: " + tableTestLiquid.getDeployReceipt().getMessage());
            }
            // create table
            System.out.println(
                    "====== Deploy TableTest success, address: "
                            + tableTestLiquid.getContractAddress()
                            + " ====== ");

            CountDownLatch countDownLatch = new CountDownLatch(count);
            RateLimiter limiter = RateLimiter.create(qps);
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

            System.out.println("====== PerformanceTableLiquid " + command + " start ======");
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "PerformanceTableLiquid", Runtime.getRuntime().availableProcessors());

            Collector collector = new Collector();
            collector.setTotal(count);
            for (int i = 0; i < count; ++i) {
                limiter.acquire();
                threadPoolService
                        .getThreadPool()
                        .execute(
                                () -> {
                                    long now = System.currentTimeMillis();
                                    callTableOperation(
                                            command,
                                            tableTestLiquid,
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
                    "====== PerformanceTableLiquid test failed, error message: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void callTableOperation(
            String command, TableTestLiquid tableTest, TransactionCallback callback) {
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
    }

    public static long getNextID() {
        return uniqueID.getAndIncrement();
    }

    private static String getId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    private static void insert(TableTestLiquid tableTest, TransactionCallback callback) {
        long nextID = getNextID();
        tableTest.insert("fruit" + nextID, String.valueOf(nextID), "apple" + getId(), callback);
    }

    private static void update(TableTestLiquid tableTest, TransactionCallback callback) {
        long nextID = getNextID();
        tableTest.update("fruit" + nextID, String.valueOf(nextID), "apple" + getId(), callback);
    }

    private static void remove(TableTestLiquid tableTest, TransactionCallback callback) {
        long nextID = getNextID();
        tableTest.remove("fruit" + nextID, callback);
    }

    private static void select(TableTestLiquid tableTest, TransactionCallback callback) {
        TransactionReceipt receipt = new TransactionReceipt();
        try {
            long nextID = getNextID();
            tableTest.select("fruit" + nextID);
            receipt.setStatus(0);
            callback.onResponse(receipt);
        } catch (Exception e) {
            receipt.setStatus(-1);
            receipt.setMessage(e.getMessage());
            callback.onResponse(receipt);
        }
    }
}
