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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.WeIdContract;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.precompiled.sharding.ShardingService;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

public class PerformanceWeID {
    private static Client client;

    public static void usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceWeID test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceWeID [groupId] [count] [qps] [shards].");
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
            Integer shards = 1;
            if (args.length == 4) {
                shards = Integer.valueOf(args[3]).intValue();
            }

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            start(groupId, count, qps, shards);

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void start(String groupId, int count, Integer qps, int shards)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start test, count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId
                        + ", shards: "
                        + shards);

        RateLimiter limiter = RateLimiter.create(qps.intValue());

        ShardingService shardingService =
                new ShardingService(client, client.getCryptoSuite().getCryptoKeyPair());
        WeIdContract[] contracts = new WeIdContract[shards];
        for (int i = 0; i < shards; i++) {
            contracts[i] = WeIdContract.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            shardingService.linkShard("testShard" + i, contracts[i].getContractAddress());
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
        AtomicLong totalCost = new AtomicLong(0);
        Collector collector = new Collector();
        collector.setTotal(count);

        IntStream.range(0, count)
                .parallel()
                .forEach(
                        index -> {
                            limiter.acquire();
                            long now = System.currentTimeMillis();
                            List<String> authentication = new ArrayList<String>();
                            authentication.add(String.valueOf(random.nextInt(1000000000)));
                            List<String> service = new ArrayList<String>();
                            service.add(String.valueOf(random.nextInt(1000000000)));
                            int contractIndex = index % shards;
                            // 生成长度为32的随机字符串
                            contracts[contractIndex].createWeId(
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
                                            totalCost.addAndGet(System.currentTimeMillis() - now);
                                        }
                                    });
                            sendedBar.step();
                        });
        transactionLatch.await();

        sendedBar.close();
        receivedBar.close();
        collector.report();
        System.out.println("Sending transactions finished!");
    }
}
