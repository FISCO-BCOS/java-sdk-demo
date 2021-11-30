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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.fisco.bcos.sdk.demo.contract.TableTestLiquid;
import org.fisco.bcos.sdk.demo.perf.callback.PerformanceCallback;
import org.fisco.bcos.sdk.demo.perf.collector.PerformanceCollector;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class PerformanceTableLiquid {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceTableLiquid.class);
    private static AtomicInteger sentTransactions = new AtomicInteger(0);
    private static AtomicLong uniqueID = new AtomicLong(0);

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
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTableLiquid [query] [count] [tps] [groupId].");
    }

    public static void main(String[] args) {
        /*
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
            Integer count = Integer.valueOf(args[1]);
            Integer qps = Integer.valueOf(args[2]);
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

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);

            // build the client
            Client client = sdk.getClient(groupId);
            if (client == null) {
                System.out.println("client is null");
                return;
            }

            // deploy the HelloWorld
            System.out.println("====== Deploy TableTest ====== ");
            String tableTestPath = "table_test" + new Random().nextInt(1000);
            TableTestLiquid tableTestLiquid =
                    TableTestLiquid.deploy(
                            client, client.getCryptoSuite().getCryptoKeyPair(), tableTestPath);
            // create table
            System.out.println(
                    "====== Deploy TableTest success, address: "
                            + tableTestLiquid.getContractAddress()
                            + " ====== ");

            PerformanceCollector collector = new PerformanceCollector();
            collector.setTotal(count);
            RateLimiter limiter = RateLimiter.create(qps);
            int area = count / 10;
            final Integer total = count;

            System.out.println("====== PerformanceTableLiquid " + command + " start ======");
            ThreadPoolService threadPoolService =
                    new ThreadPoolService("PerformanceTableLiquid", 1000000);
            for (int i = 0; i < count; ++i) {
                limiter.acquire();
                threadPoolService
                        .getThreadPool()
                        .execute(
                                () -> {
                                    callTableOperation(command, tableTestLiquid, collector);
                                    int current = sentTransactions.incrementAndGet();
                                    if (current >= area && ((current % area) == 0)) {
                                        System.out.println(
                                                "Already sended: "
                                                        + current
                                                        + "/"
                                                        + total
                                                        + " transactions");
                                    }
                                });
            }
            // wait to collect all the receipts
            while (!collector.getReceived().equals(count)) {
                Thread.sleep(1000);
            }
            threadPoolService.stop();
            System.exit(0);
        } catch (BcosSDKException | ContractException | InterruptedException e) {
            System.out.println(
                    "====== PerformanceTableLiquid test failed, error message: " + e.getMessage());
            System.exit(0);
        }
         */
    }

    private static void callTableOperation(
            String command, TableTestLiquid tableTest, PerformanceCollector collector) {
        if (command.compareToIgnoreCase("insert") == 0) {
            insert(tableTest, collector);
        }

        if (command.compareToIgnoreCase("update") == 0) {
            update(tableTest, collector);
        }
        if (command.compareToIgnoreCase("remove") == 0) {
            remove(tableTest, collector);
        }
        if (command.compareToIgnoreCase("query") == 0) {
            query(tableTest, collector);
        }
    }

    public static long getNextID() {
        return uniqueID.getAndIncrement();
    }

    private static String getId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    private static PerformanceCallback createCallback(PerformanceCollector collector) {
        PerformanceCallback callback = new PerformanceCallback();
        callback.setTimeout(0);
        callback.setCollector(collector);
        return callback;
    }

    private static void sendTransactionException(
            Exception e, String command, PerformanceCallback callback) {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus(-1);
        callback.onResponse(receipt);
        logger.info("call command {} failed, error info: {}", command, e.getMessage());
    }

    private static void insert(TableTestLiquid tableTest, PerformanceCollector collector) {
        PerformanceCallback callback = createCallback(collector);
        try {
            long nextID = getNextID();
            tableTest.insert("fruit" + nextID, String.valueOf(nextID), "apple" + getId(), callback);
        } catch (Exception e) {
            sendTransactionException(e, "insert", callback);
        }
    }

    private static void update(TableTestLiquid tableTest, PerformanceCollector collector) {
        PerformanceCallback callback = createCallback(collector);
        try {
            long nextID = getNextID();
            tableTest.update("fruit" + nextID, String.valueOf(nextID), "apple" + getId(), callback);
        } catch (Exception e) {
            sendTransactionException(e, "update", callback);
        }
    }

    private static void remove(TableTestLiquid tableTest, PerformanceCollector collector) {
        PerformanceCallback callback = createCallback(collector);
        try {
            long nextID = getNextID();
            tableTest.remove("fruit" + nextID, callback);

        } catch (Exception e) {
            sendTransactionException(e, "remove", callback);
        }
    }

    private static void query(TableTestLiquid tableTest, PerformanceCollector collector) {
        try {
            Long timeBefore = System.currentTimeMillis();
            long nextID = getNextID();
            tableTest.select("fruit" + nextID);
            Long timeAfter = System.currentTimeMillis();
            TransactionReceipt receipt = new TransactionReceipt();
            receipt.setStatus(0x0);
            collector.onMessage(receipt, timeAfter - timeBefore);
        } catch (Exception e) {
            TransactionReceipt receipt = new TransactionReceipt();
            receipt.setStatus(-1);
            collector.onMessage(receipt, (long) (0));
            logger.error("query error: {}", e);
        }
    }
}
