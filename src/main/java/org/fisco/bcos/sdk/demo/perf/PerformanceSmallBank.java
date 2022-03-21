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
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.SmallBankPrecompiled;
import org.fisco.bcos.sdk.demo.perf.model.DagTransferUser;
import org.fisco.bcos.sdk.demo.perf.model.DagUserInfo;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class PerformanceSmallBank {
    private static Client client;
    private static DagUserInfo dagUserInfo = new DagUserInfo();

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== Performance SmallBank test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceSmallBank [groupId] [precompiled/solidity] [add] [contractsNum] [count] [qps] [file] [parallel(true/false)].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceSmallBank [groupId] [precompiled/solidity] [transfer] [contractsNum] [count] [qps] [file] [parallel(true/false)].");
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
            if (args.length < 8) {
                Usage();
                return;
            }

            String groupId = args[0];
            String type = args[1];
            String command = args[2];
            int contractsNum = Integer.valueOf(args[3]).intValue();
            Integer count = Integer.valueOf(args[4]);
            Integer qps = Integer.valueOf(args[5]);
            String userFile = args[6];
            boolean enableParallel = Boolean.valueOf(args[7]);

            if (!type.equals("precompiled") && !type.equals("solidity")) {
                Usage();
                return;
            }
            boolean isPrecompiled = type.equals("precompiled");

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            dagUserInfo.setFile(userFile);

            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "SmallBankClient", Runtime.getRuntime().availableProcessors());

            start(
                    groupId,
                    isPrecompiled,
                    command,
                    contractsNum,
                    count,
                    qps,
                    userFile,
                    enableParallel,
                    threadPoolService);

            threadPoolService.getThreadPool().awaitTermination(0, TimeUnit.SECONDS);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void start(
            String groupId,
            boolean isPrecompiled,
            String command,
            int contractsNum,
            Integer count,
            Integer qps,
            String userFile,
            boolean enableParallel,
            ThreadPoolService threadPoolService)
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
                        + enableParallel);

        RateLimiter limiter = RateLimiter.create(qps.intValue());
        SmallBankPrecompiled[] contracts = new SmallBankPrecompiled[contractsNum];
        String[] userlist = new String[count];
        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        // create multi contracts
        for (int i = 0; i < contractsNum; ++i) {
            contracts[i] =
                    SmallBankPrecompiled.load(
                            i, client, client.getCryptoSuite().getCryptoKeyPair());
            (contracts[i]).setEnableDAG(enableParallel);
        }

        // create userlist
        for (int i = 0; i < count; ++i) {
            long seconds = System.currentTimeMillis() / 1000L;
            String user = Long.toHexString(seconds) + Integer.toHexString(i);
            userlist[i] = user;
        }

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
        Collector collector = new Collector();
        collector.setTotal(count);

        switch (command) {
            case "add":

                // create user file
                for (int i = 0; i < count; ++i) {
                    long seconds = System.currentTimeMillis() / 1000L;
                    String user = Long.toHexString(seconds) + Integer.toHexString(i);
                    userlist[i] = user;
                    BigInteger amount = BigInteger.valueOf(1000000000);
                    DagTransferUser dtu = new DagTransferUser();
                    dtu.setUser(user);
                    dtu.setAmount(amount);
                }

                // save the user info
                dagUserInfo.writeDagTransferUser();

                for (int i = 0; i < count; ++i) {
                    limiter.acquire();
                    final int index = i % contracts.length;
                    final int index1 = i % count;
                    threadPoolService
                            .getThreadPool()
                            .execute(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            SmallBankPrecompiled contract = contracts[index];

                                            long now = System.currentTimeMillis();

                                            contract.updateBalance(
                                                    String.valueOf(userlist[index1]),
                                                    BigInteger.valueOf(1000000000),
                                                    new TransactionCallback() {
                                                        public void onResponse(
                                                                TransactionReceipt receipt) {
                                                            long cost =
                                                                    System.currentTimeMillis()
                                                                            - now;
                                                            collector.onMessage(receipt, cost);
                                                            receivedBar.step();
                                                            if (!receipt.isStatusOK()) {
                                                                errorBar.step();
                                                            }
                                                            transactionLatch.countDown();
                                                            totalCost.addAndGet(
                                                                    System.currentTimeMillis()
                                                                            - now);
                                                        }
                                                    });
                                            sendedBar.step();
                                        }
                                    });
                }
                break;
            case "transfer":
                dagUserInfo.loadDagTransferUser();
                final List<DagTransferUser> allUsers = dagUserInfo.getUserList();
                for (int i = 0; i < count; ++i) {
                    limiter.acquire();
                    final int index = i % contracts.length;
                    final int index1 = i % count;
                    threadPoolService
                            .getThreadPool()
                            .execute(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            SmallBankPrecompiled contract = contracts[index];

                                            long now = System.currentTimeMillis();
                                            DagTransferUser from = dagUserInfo.getFrom(index1);
                                            DagTransferUser to = dagUserInfo.getTo(index1);

                                            // String from = userlist[index1];
                                            // String to = userlist[index1+1];

                                            contract.sendPayment(
                                                    from.getUser(),
                                                    to.getUser(),
                                                    BigInteger.valueOf(1),
                                                    new TransactionCallback() {
                                                        public void onResponse(
                                                                TransactionReceipt receipt) {
                                                            long cost =
                                                                    System.currentTimeMillis()
                                                                            - now;
                                                            collector.onMessage(receipt, cost);
                                                            receivedBar.step();
                                                            if (!receipt.isStatusOK()) {
                                                                errorBar.step();
                                                            }
                                                            transactionLatch.countDown();
                                                            totalCost.addAndGet(
                                                                    System.currentTimeMillis()
                                                                            - now);
                                                        }
                                                    });
                                            sendedBar.step();
                                        }
                                    });
                }
                break;
            default:
                System.out.println("invalid command: " + command);
                Usage();
                break;
        }
        transactionLatch.await();
        System.out.println("Sending transactions finished!");
        sendedBar.close();
        receivedBar.close();
        errorBar.close();
        collector.report();
    }
}
