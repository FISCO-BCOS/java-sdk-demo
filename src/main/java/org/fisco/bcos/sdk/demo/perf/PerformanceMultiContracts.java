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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.CpuHeavyPrecompiled;
import org.fisco.bcos.sdk.demo.contract.EvidenceSignersData;
import org.fisco.bcos.sdk.demo.contract.SmallBank;
import org.fisco.bcos.sdk.demo.contract.WeIdContract;
import org.fisco.bcos.sdk.demo.perf.model.DagTransferUser;
import org.fisco.bcos.sdk.demo.perf.model.DagUserInfo;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.precompiled.sharding.ShardingService;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

public class PerformanceMultiContracts {
    private static Client client;

    public static void usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceMultiContracts test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceMultiContracts [groupId] [count] [qps] [enableShard].");
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

            if (args.length < 3) {
                usage();
                return;
            }

            String groupId = args[0];
            Integer count = Integer.valueOf(args[1]).intValue();
            Integer qps = Integer.valueOf(args[2]).intValue();
            Boolean enableShard = false;
            if (args.length == 4) {
                enableShard = Boolean.valueOf(args[3]);
            }

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            start(groupId, count, qps, enableShard);

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void start(String groupId, int count, Integer qps, Boolean enableShard)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start test, count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId
                        + ", enableShard: "
                        + enableShard);

        RateLimiter limiter = RateLimiter.create(qps.intValue());

        ShardingService shardingService =
                new ShardingService(client, client.getCryptoSuite().getCryptoKeyPair());
        System.out.println("deploy and add 10000 users of SmallBankPrecompiled use DAG...");
        SmallBank smallBank =
                SmallBank.deploy(client, client.getCryptoSuite().getCryptoKeyPair(), true);
        smallBank.setEnableDAG(true);
        // add users of smallBank
        DagUserInfo dagUserInfo = new DagUserInfo();
        IntStream.range(0, 10000)
                .parallel()
                .forEach(
                        index -> {
                            String user = Long.toHexString(index);
                            BigInteger amount = BigInteger.valueOf(1000000000);
                            DagTransferUser dtu = new DagTransferUser();
                            dtu.setUser(user);
                            dtu.setAmount(amount);
                            smallBank.updateBalance(
                                    user,
                                    amount,
                                    new TransactionCallback() {
                                        public void onResponse(TransactionReceipt receipt) {
                                            if (!receipt.isStatusOK()) {
                                                System.out.println(
                                                        "updateBalance failed, user: "
                                                                + user
                                                                + ", amount: "
                                                                + amount
                                                                + ", receipt: "
                                                                + receipt);
                                            }
                                        }
                                    });
                            dagUserInfo.addUser(dtu);
                        });

        WeIdContract weID = WeIdContract.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
        List<String> evidenceSigners = new ArrayList<>();
        evidenceSigners.add(client.getCryptoSuite().getCryptoKeyPair().getAddress());
        System.out.println("deploy EvidenceSignersData single evidence length 32B...");

        EvidenceSignersData evidenceSignersData =
                EvidenceSignersData.deploy(
                        client, client.getCryptoSuite().getCryptoKeyPair(), evidenceSigners);
        System.out.println("load CpuHeavyPrecompiled array length 100k...");
        int arrayLength = 100000;
        CpuHeavyPrecompiled cpuHeavy =
                CpuHeavyPrecompiled.load(0, client, client.getCryptoSuite().getCryptoKeyPair());
        cpuHeavy.setEnableDAG(true);
        int contractCount = 4;
        if (enableShard) {
            shardingService.linkShard("shardWeID", weID.getContractAddress());
            shardingService.linkShard("shardSmallBank", smallBank.getContractAddress());
            shardingService.linkShard("shardEvidence", evidenceSignersData.getContractAddress());
            // shardingService.linkShard("shardCpuHeavy", cpuHeavy.getContractAddress());
        }

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

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

        CountDownLatch transactionLatch = new CountDownLatch(count);
        Collector collector = new Collector();
        collector.setTotal(count);

        IntStream.range(0, count)
                .parallel()
                .forEach(
                        index -> {
                            limiter.acquire();
                            int contractIndex = index % contractCount;
                            switch (contractIndex) {
                                case 0:
                                    // smallBank
                                    List<DagTransferUser> allUser = dagUserInfo.getUserList();
                                    DagTransferUser from = dagUserInfo.getFrom(index);
                                    DagTransferUser to = dagUserInfo.getTo(index);
                                    int r = random.nextInt(10) + 1;
                                    BigInteger amount = BigInteger.valueOf(r);
                                    long now = System.currentTimeMillis();
                                    smallBank.sendPayment(
                                            from.getUser(),
                                            to.getUser(),
                                            amount,
                                            new TransactionCallback() {
                                                public void onResponse(TransactionReceipt receipt) {
                                                    long cost = System.currentTimeMillis() - now;
                                                    collector.onMessage(receipt, cost);
                                                    receivedBar.step();
                                                    transactionLatch.countDown();
                                                }
                                            });
                                    break;
                                case 1:
                                    // weID
                                    List<String> authentication = new ArrayList<String>();
                                    authentication.add(String.valueOf(random.nextInt(1000000000)));
                                    List<String> service = new ArrayList<String>();
                                    service.add(String.valueOf(random.nextInt(1000000000)));
                                    now = System.currentTimeMillis();
                                    weID.createWeId(
                                            String.valueOf(index),
                                            "true",
                                            authentication,
                                            service,
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {

                                                    long cost = System.currentTimeMillis() - now;
                                                    collector.onMessage(receipt, cost);

                                                    receivedBar.step();
                                                    transactionLatch.countDown();
                                                }
                                            });
                                    break;
                                case 2:
                                    // evidence
                                    byte[] data = new byte[32];
                                    random.nextBytes(data);
                                    String evidence = new String(data);
                                    now = System.currentTimeMillis();
                                    evidenceSignersData.newEvidence(
                                            evidence,
                                            String.valueOf(index),
                                            String.valueOf(index),
                                            BigInteger.valueOf(1),
                                            data,
                                            data,
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {

                                                    long cost = System.currentTimeMillis() - now;
                                                    collector.onMessage(receipt, cost);

                                                    receivedBar.step();
                                                    transactionLatch.countDown();
                                                }
                                            });
                                    break;
                                case 3:
                                    // cpuHeavy
                                    now = System.currentTimeMillis();
                                    cpuHeavy.sort(
                                            BigInteger.valueOf(arrayLength),
                                            BigInteger.valueOf(index),
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {

                                                    long cost = System.currentTimeMillis() - now;
                                                    collector.onMessage(receipt, cost);

                                                    receivedBar.step();
                                                    transactionLatch.countDown();
                                                }
                                            });
                                    break;
                                default:
                                    break;
                            }

                            sendedBar.step();
                        });
        transactionLatch.await();

        sendedBar.close();
        receivedBar.close();
        collector.report();
        System.out.println("Sending transactions finished!");
    }
}
