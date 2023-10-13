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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.SimpleEvidenceFactory;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.request.JsonRpcRequest;
import org.fisco.bcos.sdk.v3.contract.precompiled.sharding.ShardingService;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ObjectMapperFactory;

public class PerformanceSimpleEvidence {
    private static Client client;

    public static void usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceSimpleEvidence test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceSimpleEvidence [new/generate] [groupId] [count] [qps] [evidenceLength] [shards].");
    }

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException, ContractException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = ParallelOkPerf.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }

            if (args.length < 4) {
                usage();
                return;
            }

            String command = args[0];
            String groupId = args[1];
            Integer count = Integer.valueOf(args[2]).intValue();
            Integer qps = Integer.valueOf(args[3]).intValue();
            Integer evidenceLength = Integer.valueOf(args[4]).intValue();
            Integer shards = 1;
            if (args.length == 6) {
                shards = Integer.valueOf(args[5]).intValue();
            }

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            switch (command) {
                case "new":
                    newEvidence(groupId, count, qps, evidenceLength, shards);
                    break;
                case "generate":
                    generate(count, qps, evidenceLength, shards);
                    break;
                default:
                    System.out.println("valid command are new/generate, got : " + command);
                    break;
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void newEvidence(
            String groupId, int count, Integer qps, int evidenceLength, int shards)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start test, count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId
                        + ", evidenceLength: "
                        + evidenceLength
                        + ", shards: "
                        + shards);

        RateLimiter limiter = RateLimiter.create(qps.intValue());

        ShardingService shardingService =
                new ShardingService(client, client.getCryptoSuite().getCryptoKeyPair());
        SimpleEvidenceFactory[] contracts = new SimpleEvidenceFactory[shards];
        for (int i = 0; i < shards; i++) {
            contracts[i] =
                    SimpleEvidenceFactory.deploy(
                            client, client.getCryptoSuite().getCryptoKeyPair());
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
                            byte[] evidenceBuffer = new byte[evidenceLength];
                            random.nextBytes(evidenceBuffer);
                            String evidence = new String(evidenceBuffer);
                            int contractIndex = index % shards;
                            long now = System.currentTimeMillis();
                            // 生成长度为32的随机字符串
                            contracts[contractIndex].newEvidence(
                                    evidence,
                                    BigInteger.valueOf(1),
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

    public static void generate(int count, Integer qps, int evidenceLength, int shards)
            throws IOException, InterruptedException, ContractException {

        // if txsPath not exist, create it
        String txsPath = "newSimpleEvidenceTxs";
        File txsDir = new File(txsPath);
        if (!txsDir.exists()) {
            txsDir.mkdir();
        }
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
                "====== Generating, count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + client.getGroup()
                        + ", evidenceLength: "
                        + evidenceLength
                        + ", shards: "
                        + shards);

        ShardingService shardingService =
                new ShardingService(client, client.getCryptoSuite().getCryptoKeyPair());
        SimpleEvidenceFactory[] contracts = new SimpleEvidenceFactory[shards];
        for (int i = 0; i < shards; i++) {
            contracts[i] =
                    SimpleEvidenceFactory.deploy(
                            client, client.getCryptoSuite().getCryptoKeyPair());
            shardingService.linkShard("testShard" + i, contracts[i].getContractAddress());
        }

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        ProgressBar sendedBar =
                new ProgressBarBuilder()
                        .setTaskName("Generated   :")
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
                            byte[] evidenceBuffer = new byte[evidenceLength];
                            random.nextBytes(evidenceBuffer);
                            String evidence = new String(evidenceBuffer);
                            int contractIndex = index % shards;
                            long now = System.currentTimeMillis();
                            // 生成长度为32的随机字符串
                            String txData =
                                    contracts[contractIndex].getSignedTransactionForNewEvidence(
                                            evidence, BigInteger.valueOf(1));
                            JsonRpcRequest request =
                                    new JsonRpcRequest<>(
                                            "sendTransaction",
                                            Arrays.asList(client.getGroup(), "", txData, false));
                            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
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
                            sendedBar.step();
                            transactionLatch.countDown();
                        });
        transactionLatch.await();
        sendedBar.close();
        for (int i = 0; i < threads; i++) {
            bufferedWriters[i].close();
        }
        System.out.println("Generating transactions finished! Transactions saved in " + txsPath);
    }
}
