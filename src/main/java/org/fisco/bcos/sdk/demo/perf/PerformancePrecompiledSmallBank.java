/**
 * copyright 2014-2020 [fisco-dev]
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
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
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

public class PerformancePrecompiledSmallBank {
    private static Client client;
    private static DagUserInfo dagUserInfo = new DagUserInfo();

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== Performance SmallBank test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceSmallBank [groupId] [add] [contractsNum] [count] [qps] [file] [parallel(true/false)].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceSmallBank [groupId] [transfer] [contractsNum] [count] [qps] [file] [parallel(true/false)].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceSmallBank [groupId] [save] [contractsNum] [count] [qps] [file] [parallel(true/false)].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceSmallBank [groupId] [generate] [contractsNum] [count] [qps] [file] [parallel(true/false)].");
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
            if (args.length < 7) {
                Usage();
                return;
            }

            String groupId = args[0];
            String command = args[1];
            int contractsNum = Integer.valueOf(args[2]).intValue();
            Integer count = Integer.valueOf(args[3]);
            Integer qps = Integer.valueOf(args[4]);
            String userFile = args[5];
            boolean enableParallel = Boolean.valueOf(args[6]);

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            dagUserInfo.setFile(userFile);

            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "SmallBankClient", Runtime.getRuntime().availableProcessors());

            start(
                    groupId,
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

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        for (int i = 0; i < contractsNum; ++i) {
            contracts[i] =
                    SmallBankPrecompiled.load(
                            i, client, client.getCryptoSuite().getCryptoKeyPair());
            (contracts[i]).setEnableDAG(enableParallel);
        }
        long txtotal = 0;
        if (Objects.equals(command, "add")) {
            txtotal = count * contractsNum;
        } else if (Objects.equals(command, "transfer")) {
            txtotal = count;
        } else if (Objects.equals(command, "save")) {
            txtotal = count * contractsNum;
        } else if (Objects.equals(command, "generate")) {
            txtotal = count;
        }

        ProgressBar sendedBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(txtotal)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(txtotal)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        CountDownLatch transactionLatch = new CountDownLatch((int) txtotal);
        AtomicLong totalCost = new AtomicLong(0);
        Collector collector = new Collector();
        collector.setTotal((int) txtotal);

        switch (command) {
            case "add":
                // create user file
                System.out.println("Start userAdd test...");
                System.out.println(
                        "===================================================================");

                for (int i = 0; i < count; ++i) {
                    limiter.acquire();
                    long seconds = System.currentTimeMillis() / 1000L;
                    String user = Long.toHexString(seconds) + Integer.toHexString(i);
                    BigInteger amount = BigInteger.valueOf(1000000000);
                    DagTransferUser dtu = new DagTransferUser();
                    dtu.setUser(user);
                    dtu.setAmount(amount);

                    for (int j = 0; j < contractsNum; j++) {
                        final int index = j;
                        threadPoolService
                                .getThreadPool()
                                .execute(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                SmallBankPrecompiled contract = contracts[index];
                                                long now = System.currentTimeMillis();
                                                contract.updateBalance(
                                                        user,
                                                        amount,
                                                        new TransactionCallback() {
                                                            public void onResponse(
                                                                    TransactionReceipt receipt) {
                                                                long cost =
                                                                        System.currentTimeMillis()
                                                                                - now;
                                                                collector.onMessage(receipt, cost);
                                                                receivedBar.step();
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
                    dagUserInfo.addUser(dtu);
                }
                transactionLatch.await();
                System.out.println("Sending transactions finished!");
                dagUserInfo.writeDagTransferUser();
                break;

            case "transfer":
                {
                    System.out.println("Start userTransfer test...");
                    System.out.println(
                            "===================================================================");

                    dagUserInfo.loadDagTransferUser();
                    List<DagTransferUser> allUser = dagUserInfo.getUserList();

                    IntStream.range(0, (int) txtotal)
                            .parallel()
                            .forEach(
                                    i -> {
                                        limiter.acquire();

                                        int contractIndex = i % contracts.length;
                                        SmallBankPrecompiled contract = contracts[contractIndex];

                                        int userIndex = (i / contracts.length) % allUser.size();
                                        long now = System.currentTimeMillis();
                                        try {
                                            DagTransferUser from = dagUserInfo.getFrom(userIndex);
                                            DagTransferUser to = dagUserInfo.getTo(userIndex);
                                            int r = random.nextInt(10) + 1;
                                            BigInteger amount = BigInteger.valueOf(r);
                                            contract.sendPayment(
                                                    from.getUser(),
                                                    to.getUser(),
                                                    amount,
                                                    new TransactionCallback() {
                                                        public void onResponse(
                                                                TransactionReceipt receipt) {
                                                            long cost =
                                                                    System.currentTimeMillis()
                                                                            - now;
                                                            collector.onMessage(receipt, cost);
                                                            receivedBar.step();
                                                            transactionLatch.countDown();
                                                            totalCost.addAndGet(
                                                                    System.currentTimeMillis()
                                                                            - now);
                                                        }
                                                    });
                                            sendedBar.step();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });

                    transactionLatch.await();
                    System.out.println("Sending transactions finished!");
                    break;
                }
            case "save":
                {
                    System.out.println("Start userSave test...");
                    System.out.println(
                            "===================================================================");
                    dagUserInfo.loadDagTransferUser();
                    List<DagTransferUser> allUser = dagUserInfo.getUserList();
                    for (int i = 0; i < allUser.size(); i++) {
                        final int userIdx = i;
                        limiter.acquire();
                        for (int j = 0; j < contracts.length; j++) {
                            final int contractIdx = j;
                            limiter.acquire();
                            threadPoolService
                                    .getThreadPool()
                                    .execute(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    SmallBankPrecompiled contract =
                                                            contracts[contractIdx];
                                                    long now = System.currentTimeMillis();
                                                    try {
                                                        DagTransferUser from =
                                                                dagUserInfo.getFrom(userIdx);
                                                        BigInteger amount =
                                                                BigInteger.valueOf(1000000000);
                                                        contract.updateBalance(
                                                                from.getUser(),
                                                                amount,
                                                                new TransactionCallback() {
                                                                    public void onResponse(
                                                                            TransactionReceipt
                                                                                    receipt) {
                                                                        long cost =
                                                                                System
                                                                                                .currentTimeMillis()
                                                                                        - now;
                                                                        collector.onMessage(
                                                                                receipt, cost);
                                                                        receivedBar.step();
                                                                        transactionLatch
                                                                                .countDown();
                                                                        totalCost.addAndGet(
                                                                                System
                                                                                                .currentTimeMillis()
                                                                                        - now);
                                                                    }
                                                                });
                                                        sendedBar.step();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                        }
                    }
                    transactionLatch.await();
                    System.out.println("Sending transactions finished!");
                    break;
                }
            case "generate":
                {
                    String txsFile = "smallbank." + contractsNum + "." + count + ".txt";
                    System.out.println("Generating transactions -> " + txsFile);
                    dagUserInfo.loadDagTransferUser();

                    File accountFile = new File(txsFile);
                    if (!accountFile.exists()) {
                        accountFile.createNewFile();
                    }
                    FileWriter fileWriter = new FileWriter(accountFile.getName(), true);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                    List<DagTransferUser> allUser = dagUserInfo.getUserList();
                    for (int i = 0; i < txtotal; ) {
                        final int userindex = (i / contracts.length) % allUser.size();
                        for (int j = 0; j < contracts.length && i < txtotal; j++, i++) {
                            final int index = j % contracts.length;

                            SmallBankPrecompiled contract = contracts[index];
                            try {
                                DagTransferUser from = dagUserInfo.getFrom(userindex);
                                DagTransferUser to = dagUserInfo.getTo(userindex);
                                int r = random.nextInt(10) + 1;
                                BigInteger amount = BigInteger.valueOf(r);

                                String txBytes =
                                        contract.getSignedTransactionForSendPayment(
                                                from.getUser(), to.getUser(), amount);

                                bufferedWriter.write(txBytes);
                                bufferedWriter.newLine();

                                sendedBar.step();
                                transactionLatch.countDown();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    transactionLatch.await();
                    System.out.println("Generate transactions finished! File name: " + txsFile);
                    break;
                }
            default:
                System.out.println("invalid command: " + command);
                Usage();
                break;
        }
        sendedBar.close();
        receivedBar.close();
        collector.report();
    }
}
