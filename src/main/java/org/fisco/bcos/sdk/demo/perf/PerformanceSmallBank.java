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
import org.fisco.bcos.sdk.demo.contract.SmallBank;
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
    private static DagUserInfo dagUserInfo = new DagUserInfo();;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== Performance SmallBank test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceSmallBank [groupId] [add] [count] [qps] [file] [parallel(true/false)].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceSmallBank [groupId] [transfer] [count] [qps] [file] [parallel(true/false)].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceSmallBank [groupId] [save] [count] [qps] [file] [parallel(true/false)].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceSmallBank [groupId] [generate] [count] [qps] [file] [parallel(true/false)].");
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
            if (args.length < 6) {
                Usage();
                return;
            }

            String groupId = args[0];
            String command = args[1];
            Integer count = Integer.valueOf(args[2]);
            Integer qps = Integer.valueOf(args[3]);
            String userFile = args[4];
            boolean enableParallel = Boolean.valueOf(args[5]);

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            dagUserInfo.setFile(userFile);

            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "SmallBankClient", Runtime.getRuntime().availableProcessors());
            start(groupId, command, count, qps, userFile, enableParallel, threadPoolService);

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
            Integer count,
            Integer qps,
            String userFile,
            boolean enableParallel,
            ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start "
                        + ", count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId
                        + ", enableParallel: "
                        + enableParallel);

        RateLimiter limiter = RateLimiter.create(qps.intValue());
        SmallBank smallBank;

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        if (Objects.equals(command, "add")) {
            smallBank =
                    SmallBank.deploy(
                            client, client.getCryptoSuite().getCryptoKeyPair(), enableParallel);
            dagUserInfo.setContractAddr(smallBank.getContractAddress());
            smallBank.setEnableDAG(enableParallel);
        } else {
            dagUserInfo.loadDagTransferUser();
            smallBank =
                    SmallBank.load(
                            dagUserInfo.getContractAddr(),
                            client,
                            client.getCryptoSuite().getCryptoKeyPair());
            smallBank.setEnableDAG(enableParallel);
        }
        long txtotal = count;

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

                    threadPoolService
                            .getThreadPool()
                            .execute(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            long now = System.currentTimeMillis();
                                            smallBank.updateBalance(
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

                    IntStream.range(0, (int) txtotal)
                            .parallel()
                            .forEach(
                                    index -> {
                                        limiter.acquire();
                                        long now = System.currentTimeMillis();
                                        try {
                                            DagTransferUser from = dagUserInfo.getFrom(index);
                                            DagTransferUser to = dagUserInfo.getTo(index);
                                            BigInteger amount =
                                                    BigInteger.valueOf(random.nextInt(10));
                                            smallBank.sendPayment(
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
                        threadPoolService
                                .getThreadPool()
                                .execute(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                long now = System.currentTimeMillis();
                                                try {
                                                    DagTransferUser from =
                                                            dagUserInfo.getFrom(userIdx);
                                                    BigInteger amount =
                                                            BigInteger.valueOf(1000000000);
                                                    smallBank.updateBalance(
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
                                                                    transactionLatch.countDown();
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
                    transactionLatch.await();
                    System.out.println("Sending transactions finished!");
                    break;
                }
            case "generate":
                {
                    String txsFile = "smallbank." + "." + count + ".txt";
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
                        try {
                            DagTransferUser from = dagUserInfo.getFrom(i);
                            DagTransferUser to = dagUserInfo.getTo(i);
                            int r = random.nextInt(10) + 1;
                            BigInteger amount = BigInteger.valueOf(r);

                            String txBytes =
                                    smallBank.getSignedTransactionForSendPayment(
                                            from.getUser(), to.getUser(), amount);

                            bufferedWriter.write(txBytes);
                            bufferedWriter.newLine();

                            sendedBar.step();
                            transactionLatch.countDown();
                        } catch (Exception e) {
                            e.printStackTrace();
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
