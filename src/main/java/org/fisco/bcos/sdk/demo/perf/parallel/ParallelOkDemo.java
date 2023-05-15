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
package org.fisco.bcos.sdk.demo.perf.parallel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.ParallelOk;
import org.fisco.bcos.sdk.demo.perf.Collector;
import org.fisco.bcos.sdk.demo.perf.callback.ParallelOkCallback;
import org.fisco.bcos.sdk.demo.perf.collector.PerformanceCollector;
import org.fisco.bcos.sdk.demo.perf.model.DagTransferUser;
import org.fisco.bcos.sdk.demo.perf.model.DagUserInfo;
import org.fisco.bcos.sdk.v3.client.protocol.request.JsonRpcRequest;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ObjectMapperFactory;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelOkDemo {
    private static final Logger logger = LoggerFactory.getLogger(ParallelOkDemo.class);
    private static AtomicInteger sended = new AtomicInteger(0);
    private AtomicInteger getted = new AtomicInteger(0);

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final ParallelOk parallelOk;
    private final ThreadPoolService threadPoolService;
    private final PerformanceCollector collector;
    private final DagUserInfo dagUserInfo;

    public ParallelOkDemo(
            ParallelOk parallelOk, DagUserInfo dagUserInfo, ThreadPoolService threadPoolService) {
        this.threadPoolService = threadPoolService;
        this.parallelOk = parallelOk;
        this.dagUserInfo = dagUserInfo;
        this.collector = new PerformanceCollector();
    }

    public void veryTransferData(BigInteger qps) throws InterruptedException {
        RateLimiter rateLimiter = RateLimiter.create(qps.intValue());
        System.out.println("===================================================================");
        AtomicInteger verifyFailed = new AtomicInteger(0);
        AtomicInteger verifySuccess = new AtomicInteger(0);

        final List<DagTransferUser> userInfo = dagUserInfo.getUserList();
        int userSize = userInfo.size();
        for (int i = 0; i < userSize; i++) {
            rateLimiter.acquire();
            final int userIndex = i;
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String user = userInfo.get(userIndex).getUser();
                                        BigInteger balance = parallelOk.balanceOf(user);
                                        BigInteger localAmount =
                                                userInfo.get(userIndex).getAmount();
                                        if (localAmount.compareTo(balance) != 0) {
                                            logger.error(
                                                    "local balance is not the same as the remote, user: {}, local balance: {}, remote balance: {}",
                                                    user,
                                                    localAmount,
                                                    balance);
                                            verifyFailed.incrementAndGet();
                                        } else {
                                            verifySuccess.incrementAndGet();
                                        }
                                    } catch (ContractException exception) {
                                        verifyFailed.incrementAndGet();
                                        logger.error(
                                                "get remote balance failed, error info: "
                                                        + exception.getMessage());
                                    }
                                }
                            });
        }
        while (verifySuccess.get() + verifyFailed.get() < userSize) {
            Thread.sleep(40);
        }

        System.out.println("validation:");
        System.out.println(" \tuser count is " + userSize);
        System.out.println(" \tverify_success count is " + verifySuccess);
        System.out.println(" \tverify_failed count is " + verifyFailed);
    }

    public void userAdd(BigInteger userCount, BigInteger qps)
            throws InterruptedException, IOException {
        System.out.println("===================================================================");
        System.out.println("Start UserAdd test, count " + userCount);
        RateLimiter limiter = RateLimiter.create(qps.intValue());

        long currentSeconds = System.currentTimeMillis() / 1000L;
        Integer area = userCount.intValue() / 10;
        long startTime = System.currentTimeMillis();
        collector.setTotal(userCount.intValue());
        collector.setStartTimestamp(startTime);
        AtomicInteger sendFailed = new AtomicInteger(0);
        for (Integer i = 0; i < userCount.intValue(); i++) {
            final Integer index = i;
            limiter.acquire();
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    // generate the user according to currentSeconds
                                    String user =
                                            Long.toHexString(currentSeconds)
                                                    + Integer.toHexString(index);
                                    BigInteger amount = new BigInteger("1000000000");
                                    DagTransferUser dtu = new DagTransferUser();
                                    dtu.setUser(user);
                                    dtu.setAmount(amount);
                                    ParallelOkCallback callback =
                                            new ParallelOkCallback(
                                                    collector,
                                                    dagUserInfo,
                                                    ParallelOkCallback.ADD_USER_CALLBACK);
                                    callback.setTimeout(0);
                                    callback.setUser(dtu);
                                    try {
                                        callback.recordStartTime();
                                        parallelOk.set(user, amount, callback);
                                        int current = sended.incrementAndGet();

                                        if (current >= area && ((current % area) == 0)) {
                                            long elapsed = System.currentTimeMillis() - startTime;
                                            double sendSpeed = current / ((double) elapsed / 1000);
                                            System.out.println(
                                                    "Already sended: "
                                                            + current
                                                            + "/"
                                                            + userCount
                                                            + " transactions"
                                                            + ",QPS="
                                                            + sendSpeed);
                                        }

                                    } catch (Exception e) {
                                        logger.warn(
                                                "addUser failed, error info: {}", e.getMessage());
                                        sendFailed.incrementAndGet();
                                        TransactionReceipt receipt = new TransactionReceipt();
                                        receipt.setStatus(-1);
                                        receipt.setMessage(
                                                "userAdd failed, error info: " + e.getMessage());
                                        callback.onResponse(receipt);
                                    }
                                }
                            });
        }
        while (collector.getReceived().intValue() != userCount.intValue()) {
            logger.info(
                    " sendFailed: {}, received: {}, total: {}",
                    sendFailed.get(),
                    collector.getReceived().intValue(),
                    collector.getTotal());
            Thread.sleep(100);
        }
        dagUserInfo.setContractAddr(parallelOk.getContractAddress());
        dagUserInfo.writeDagTransferUser();
        System.exit(0);
    }

    public void queryAccount(BigInteger qps) throws InterruptedException {
        final List<DagTransferUser> allUsers = dagUserInfo.getUserList();
        RateLimiter rateLimiter = RateLimiter.create(qps.intValue());
        AtomicInteger sent = new AtomicInteger(0);
        for (Integer i = 0; i < allUsers.size(); i++) {
            final Integer index = i;
            rateLimiter.acquire();
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        BigInteger result =
                                                parallelOk.balanceOf(allUsers.get(index).getUser());
                                        allUsers.get(index).setAmount(result);
                                        int all = sent.incrementAndGet();
                                        if (all >= allUsers.size()) {
                                            System.out.println(
                                                    dateFormat.format(new Date())
                                                            + " Query account finished");
                                        }
                                    } catch (ContractException exception) {
                                        logger.warn(
                                                "queryAccount for {} failed, error info: {}",
                                                allUsers.get(index).getUser(),
                                                exception.getMessage());
                                        System.exit(0);
                                    }
                                }
                            });
        }
        while (sent.get() < allUsers.size()) {
            Thread.sleep(50);
        }
    }

    public void generateTransferTxs(
            String groupID, int count, String txsPath, BigInteger qps, BigInteger conflictPercent)
            throws InterruptedException, IOException {

        // if txsPath not exist, create it
        File txsDir = new File(txsPath);
        if (!txsDir.exists()) {
            txsDir.mkdir();
        }
        System.out.println(
                "ParallelOkDemo: test generateTransferTxs, count: "
                        + count
                        + ", txsPath: "
                        + txsPath);
        System.out.println("===================================================================");
        // queryAccount(qps);
        int threads = Runtime.getRuntime().availableProcessors();
        BufferedWriter[] bufferedWriters = new BufferedWriter[threads];
        Lock[] locks = new ReentrantLock[threads];
        for (int i = 0; i < threads; i++) {
            String fileName = txsPath + "/" + i + ".txt";
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(fileName, true);
            bufferedWriters[i] = new BufferedWriter(fileWriter);
            locks[i] = new ReentrantLock();
        }
        System.out.println(
                "ParallelOkDemo: start generateTransferTxs, count: "
                        + count
                        + ", txsPath: "
                        + txsPath);
        ProgressBar generatedBar =
                new ProgressBarBuilder()
                        .setTaskName("Generated   :")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        CountDownLatch transactionLatch = new CountDownLatch(count);
        for (Integer i = 0; i < count; i++) {
            final Integer index = i;
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    DagTransferUser from = dagUserInfo.getFrom(index);
                                    DagTransferUser to = dagUserInfo.getTo(index);
                                    // if ((conflictPercent.intValue() > 0)
                                    //         && (index
                                    //                 <= (conflictPercent.intValue() * count)
                                    //                         / 100)) {
                                    //     to = dagUserInfo.getNext(index);
                                    // }
                                    Random random = new Random();
                                    int r = random.nextInt(100) + 1;
                                    BigInteger amount = BigInteger.valueOf(r);
                                    String txData =
                                            parallelOk.getSignedTransactionForTransfer(
                                                    from.getUser(), to.getUser(), amount);
                                    JsonRpcRequest request =
                                            new JsonRpcRequest<>(
                                                    "sendTransaction",
                                                    Arrays.asList(groupID, "", txData, false));
                                    ObjectMapper objectMapper =
                                            ObjectMapperFactory.getObjectMapper();
                                    int fileIndex = index % threads;
                                    try {
                                        locks[fileIndex].lock();
                                        bufferedWriters[fileIndex].write(
                                                objectMapper.writeValueAsString(request));
                                        bufferedWriters[fileIndex].newLine();
                                        locks[fileIndex].unlock();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    generatedBar.step();
                                    transactionLatch.countDown();
                                }
                            });
        }
        transactionLatch.await();
        generatedBar.close();
        // Integer area = count.intValue() / 10;
        // for (Integer i = 0; i < count.intValue(); i++) {
        // final Integer index = i;

        // DagTransferUser from = dagUserInfo.getFrom(index);
        // DagTransferUser to = dagUserInfo.getTo(index);
        // if ((conflictPercent.intValue() > 0)
        // && (index <= (conflictPercent.intValue() * count.intValue()) / 100)) {
        // to = dagUserInfo.getNext(index);
        // }
        // Random random = new Random();
        // int r = random.nextInt(100) + 1;
        // BigInteger amount = BigInteger.valueOf(r);
        // String txData =
        // parallelOk.getSignedTransactionForTransfer(
        // from.getUser(), to.getUser(), amount);
        // JsonRpcRequest request =
        // new JsonRpcRequest<>(
        // "sendTransaction", Arrays.asList(groupID, "", txData, false));
        // ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        // try {
        // bufferedWriter.write(objectMapper.writeValueAsString(request));
        // bufferedWriter.newLine();
        // generated.incrementAndGet();
        // if (generated.get() >= area && ((generated.get() % area) == 0)) {
        // System.out.println(
        // "Already generated: "
        // + generated.get()
        // + "/"
        // + count
        // + " transactions");
        // }
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // }
        // while (generated.intValue() < count.intValue()) {
        // Thread.sleep(2000);
        // }
        System.out.println(
                "ParallelOkDemo: generateTransferTxs success ! count: "
                        + count
                        + ", txsPath: "
                        + txsPath);
        for (int i = 0; i < threads; i++) {
            bufferedWriters[i].close();
        }
        System.exit(0);
    }

    public void userTransfer(int count, BigInteger qps) throws InterruptedException, IOException {
        System.out.println("Querying account info...");
        queryAccount(qps);
        System.out.println("Sending transfer transactions...");
        RateLimiter limiter = RateLimiter.create(qps.intValue());
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
        Collector collector = new Collector();
        collector.setTotal(count);
        Random random = new Random();
        IntStream.range(0, count)
                .parallel()
                .forEach(
                        index -> {
                            limiter.acquire();
                            try {
                                BigInteger amount = BigInteger.valueOf(random.nextInt(100));
                                DagTransferUser from = dagUserInfo.getFrom(index);
                                DagTransferUser to = dagUserInfo.getTo(index);
                                long now = System.currentTimeMillis();
                                parallelOk.transfer(
                                        from.getUser(),
                                        to.getUser(),
                                        amount,
                                        new TransactionCallback() {
                                            @Override
                                            public void onResponse(TransactionReceipt receipt) {
                                                if (receipt.isStatusOK()) {
                                                    from.decrease(amount);
                                                    to.increase(amount);
                                                }
                                                long cost = System.currentTimeMillis() - now;
                                                collector.onMessage(receipt, cost);
                                                receivedBar.step();
                                                transactionLatch.countDown();
                                            }
                                        });
                            } catch (Exception e) {
                                logger.error(
                                        "call transfer failed, error info: {}", e.getMessage());
                                TransactionReceipt receipt = new TransactionReceipt();
                                receipt.setStatus(-1);
                                receipt.setMessage(
                                        "call transfer failed, error info: " + e.getMessage());
                                collector.onMessage(receipt, Long.valueOf(0));
                            }
                            sendedBar.step();
                        });
        transactionLatch.await();
        sendedBar.close();
        receivedBar.close();
        collector.report();
        System.out.println("Sending transactions finished!");
        veryTransferData(qps);
        System.exit(0);
    }
}
