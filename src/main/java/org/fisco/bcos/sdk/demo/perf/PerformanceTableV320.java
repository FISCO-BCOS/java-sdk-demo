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
import java.math.BigInteger;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.TableTestV320;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.TransactionReceiptStatus;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class PerformanceTableV320 {
    private static AtomicLong uniqueID = new AtomicLong(0);
    private static final Set<String> supportCommands =
            new HashSet<>(Arrays.asList("insert", "update", "remove", "select"));
    private static final int LIMIT_MAX = 500;

    private static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceTableV320 test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTableV320 [insert] [from] [to] [useRange] [qps] [groupId].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTableV320 [update] [from] [to] [useRange] [qps] [groupId].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTableV320 [remove] [from] [to] [useRange] [qps] [groupId].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTableV320 [select] [from] [to] [useRange] [qps] [groupId].");
    }

    public static void main(String[] args) {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = PerformanceTableV320.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }
            if (args.length < 6) {
                Usage();
                return;
            }
            String command = args[0];
            int from = Integer.parseInt(args[1]);
            int to = Integer.parseInt(args[2]);
            boolean useRange = Boolean.parseBoolean(args[3]);
            int qps = Integer.parseInt(args[4]);
            String groupId = args[5];
            int count = to - from + 1;
            uniqueID.getAndSet(from);
            System.out.println(
                    "====== PerformanceTableV320 "
                            + command
                            + ", from: "
                            + from
                            + ", to: "
                            + to
                            + ", useRange: "
                            + useRange
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
            System.out.println("====== Deploy TableTestV32 ====== ");
            TableTestV320 tableTest =
                    TableTestV320.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            if (tableTest.getDeployReceipt().getStatus()
                    != TransactionReceiptStatus.Success.getCode()) {
                throw new ContractException(
                        "deploy failed: " + tableTest.getDeployReceipt().getMessage());
            }
            System.out.println(
                    "====== Deploy TableTest success, address: "
                            + tableTest.getContractAddress()
                            + " ====== ");

            if (useRange && command.compareToIgnoreCase("insert") != 0) {
                count = count / LIMIT_MAX + (count % LIMIT_MAX == 0 ? 0 : 1);
            }
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

            System.out.println("====== PerformanceTableV320 " + command + " start ======");
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "PerformanceTableV320", Runtime.getRuntime().availableProcessors());

            Collector collector = new Collector();
            collector.setTotal(count);
            for (int i = 0; i < count; ++i) {
                limiter.acquire();
                int finalI = i;
                int finalCount = count;
                threadPoolService
                        .getThreadPool()
                        .execute(
                                () -> {
                                    long now = System.currentTimeMillis();
                                    if (useRange && command.compareToIgnoreCase("insert") != 0) {
                                        callTableOperationWithRange(
                                                command,
                                                tableTest,
                                                from + finalI * finalCount,
                                                LIMIT_MAX + finalI * finalCount,
                                                new TransactionCallback() {
                                                    @Override
                                                    public void onResponse(
                                                            TransactionReceipt receipt) {
                                                        long cost =
                                                                System.currentTimeMillis() - now;
                                                        collector.onMessage(receipt, cost);
                                                        receivedBar.step();
                                                        countDownLatch.countDown();
                                                    }
                                                });
                                    } else {
                                        callTableOperation(
                                                command,
                                                tableTest,
                                                new TransactionCallback() {
                                                    @Override
                                                    public void onResponse(
                                                            TransactionReceipt receipt) {
                                                        long cost =
                                                                System.currentTimeMillis() - now;
                                                        collector.onMessage(receipt, cost);
                                                        receivedBar.step();
                                                        countDownLatch.countDown();
                                                    }
                                                });
                                    }
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
                    "====== PerformanceTableV320 test failed, error message: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void callTableOperation(
            String command, TableTestV320 tableTest, TransactionCallback callback) {
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

    private static void callTableOperationWithRange(
            String command,
            TableTestV320 tableTest,
            int from,
            int to,
            TransactionCallback callback) {
        if (command.compareToIgnoreCase("update") == 0) {
            updateRange(tableTest, from, to, callback);
        }
        if (command.compareToIgnoreCase("remove") == 0) {
            removeRange(tableTest, from, to, callback);
        }
        if (command.compareToIgnoreCase("select") == 0) {
            selectRange(tableTest, from, to, callback);
        }
    }

    public static long getNextID() {
        return uniqueID.getAndIncrement();
    }

    private static String getId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    private static void insert(TableTestV320 tableTest, TransactionCallback callback) {
        long nextID = getNextID();
        tableTest.insert(
                BigInteger.valueOf(getNextID()),
                "apple" + getId(),
                String.valueOf(nextID),
                callback);
    }

    private static void update(TableTestV320 tableTest, TransactionCallback callback) {
        long nextID = getNextID();
        tableTest.update(
                BigInteger.valueOf(getNextID()),
                "apple" + getId(),
                String.valueOf(nextID),
                callback);
    }

    private static void updateRange(
            TableTestV320 tableTest, int from, int to, TransactionCallback callback) {
        tableTest.update(BigInteger.valueOf(from), BigInteger.valueOf(to), callback);
    }

    private static void remove(TableTestV320 tableTest, TransactionCallback callback) {
        tableTest.remove(BigInteger.valueOf(getNextID()), callback);
    }

    private static void removeRange(
            TableTestV320 tableTest, int from, int to, TransactionCallback callback) {
        tableTest.remove(BigInteger.valueOf(from), BigInteger.valueOf(to), callback);
    }

    private static void select(TableTestV320 tableTest, TransactionCallback callback) {
        TransactionReceipt receipt = new TransactionReceipt();
        try {
            tableTest.select(BigInteger.valueOf(getNextID()));
            receipt.setStatus(0);
            callback.onResponse(receipt);
        } catch (Exception e) {
            receipt.setStatus(-1);
            callback.onResponse(receipt);
        }
    }

    private static void selectRange(
            TableTestV320 tableTest, int from, int to, TransactionCallback callback) {
        TransactionReceipt receipt = new TransactionReceipt();
        try {
            tableTest.select(BigInteger.valueOf(from), BigInteger.valueOf(to));
            receipt.setStatus(0);
            callback.onResponse(receipt);
        } catch (ContractException e) {
            receipt.setStatus(-1);
            callback.onResponse(receipt);
        }
    }
}
