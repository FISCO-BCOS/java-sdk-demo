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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.ParallelOk;
import org.fisco.bcos.sdk.demo.perf.model.DagTransferUser;
import org.fisco.bcos.sdk.demo.perf.model.DagUserInfo;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.precompiled.sharding.ShardingService;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class ShardingOkPerf {
    private static Client client;
    private static ShardingService shardingService;
    private static DagUserInfo dagUserInfo = new DagUserInfo();

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== ShardingOk test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ShardingOkPerf [groupId] [shardNum] [add] [count] [tps] [file].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ShardingOkPerf [groupId] [shardNum] [transfer] [count] [tps] [file].");
    }

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = ShardingOkPerf.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }
            boolean isParallel = true;
            if (args.length != 6) {
                Usage();
                return;
            }

            String groupId = args[0];
            Integer shardNum = Integer.valueOf(args[1]);
            String command = args[2];
            Integer count = Integer.valueOf(args[3]);
            Integer qps = Integer.valueOf(args[4]);
            String userFile = args[5];
            Integer conflictPercent = 0;

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            shardingService =
                    new ShardingService(client, client.getCryptoSuite().getCryptoKeyPair());
            dagUserInfo.setFile(userFile);
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "ShardingOkPerf", Runtime.getRuntime().availableProcessors());

            shardingOkPerf(
                    groupId,
                    shardNum,
                    command,
                    count,
                    qps,
                    conflictPercent,
                    threadPoolService,
                    isParallel);
            System.exit(0);
        } catch (Exception e) {
            System.out.println("ShardingOkPerf test failed, error info: " + e.getMessage());
            System.exit(0);
        }
    }

    public static void addOneShard(
            Integer shardNum,
            Integer count,
            Integer qps,
            Integer conflictPercent,
            ThreadPoolService threadPoolService,
            boolean isParallel)
            throws IOException, InterruptedException, ContractException {}

    public static void add(
            Integer shardNum,
            Integer count,
            Integer qps,
            Integer conflictPercent,
            ThreadPoolService threadPoolService,
            boolean isParallel)
            throws IOException, InterruptedException, ContractException {

        int txtotal = count * shardNum;
        ParallelOk[] contracts = new ParallelOk[shardNum];
        CountDownLatch transactionLatch = new CountDownLatch((int) txtotal);
        AtomicLong totalCost = new AtomicLong(0);

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

        // deploy ParallelOk
        for (int i = 0; i < shardNum; i++) {
            String shardName = "testShard" + i;
            ParallelOk parallelOk =
                    ParallelOk.deploy(
                            client, client.getCryptoSuite().getCryptoKeyPair(), isParallel);
            contracts[i] = parallelOk;
            shardingService.linkShard("testShard" + i, parallelOk.getContractAddress());
            System.out.println(
                    "====== ShardingOk userAdd, deploy success to shard: "
                            + shardName
                            + ", address: "
                            + parallelOk.getContractAddress());
        }
        Collector collector = new Collector();
        collector.setTotal((int) txtotal);

        System.out.println("Start userAdd test...");
        System.out.println("===================================================================");

        RateLimiter limiter = RateLimiter.create(qps.intValue());
        for (int i = 0; i < count; ++i) {
            limiter.acquire();
            long seconds = System.currentTimeMillis() / 1000L;
            String user = Long.toHexString(seconds) + Integer.toHexString(i);
            BigInteger amount = BigInteger.valueOf(1000000000);
            DagTransferUser dtu = new DagTransferUser();
            dtu.setUser(user);
            dtu.setAmount(amount);

            for (int j = 0; j < shardNum; j++) {
                final int index = j;
                threadPoolService
                        .getThreadPool()
                        .execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        ParallelOk contract = contracts[index];
                                        long now = System.currentTimeMillis();
                                        contract.set(
                                                user,
                                                amount,
                                                new TransactionCallback() {
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
                                        sendedBar.step();
                                    }
                                });
            }
            dagUserInfo.addUser(dtu);
        }
        transactionLatch.await();
        System.out.println("Sending transactions finished!");
        dagUserInfo.writeDagTransferUser();
        collector.report();
        sendedBar.close();
        receivedBar.close();
    }

    public static void transferOneShard(
            Integer shardNum,
            Integer count,
            Integer qps,
            Integer conflictPercent,
            ThreadPoolService threadPoolService,
            boolean isParallel)
            throws IOException, InterruptedException, ContractException {}

    public static void shardingOkPerf(
            String groupId,
            Integer shardNum,
            String command,
            Integer count,
            Integer qps,
            Integer conflictPercent,
            ThreadPoolService threadPoolService,
            boolean isParallel)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== ShardingOk trans, count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId
                        + ", conflictPercent: "
                        + conflictPercent
                        + ", isParallel: "
                        + isParallel);

        switch (command) {
            case "add":
                add(shardNum, count, qps, conflictPercent, threadPoolService, isParallel);
                break;
            case "transfer":
                break;
            default:
                System.out.println("invalid command: " + command);
                Usage();
                break;
        }
    }
}
