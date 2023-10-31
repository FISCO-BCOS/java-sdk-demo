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
import java.security.KeyPair;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.PaillierPrecompiled;
import org.fisco.bcos.sdk.demo.perf.paillier.PaillierCipher;
import org.fisco.bcos.sdk.demo.perf.paillier.PaillierKeyPair;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

public class PerformancePaillierPrecompiled {
    private static Client client;
    private static final String PAILLIER_ADDR = "0x0000000000000000000000000000000000005003";

    public static void usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformancePaillierPrecompiled test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformancePaillierPrecompiled [add] [groupId] [PrivateKeyLen(512/1024)] [count] [qps].");
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
            Integer pkLen = Integer.valueOf(args[2]).intValue();
            if (pkLen != 512 && pkLen != 1024 && pkLen != 2048) {
                System.out.println("valid pkLen are 512/1024/2048, got : " + pkLen);
                return;
            }
            Integer count = Integer.valueOf(args[3]).intValue();
            Integer qps = Integer.valueOf(args[4]).intValue();

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            switch (command) {
                case "add":
                    add(groupId, pkLen, count, qps);
                    break;
                default:
                    System.out.println("valid command are add, got : " + command);
                    break;
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void add(String groupId, Integer pklen, int count, Integer qps)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start test, count: " + count + ", qps:" + qps + ", groupId: " + groupId);

        RateLimiter limiter = RateLimiter.create(qps.intValue());

        PaillierPrecompiled paillierPrecompiled =
                PaillierPrecompiled.load(
                        PAILLIER_ADDR, client, client.getCryptoSuite().getCryptoKeyPair());

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        System.out.println("Prepare parameters...");

        int numbers = 10000;
        String[] data = new String[numbers];
        BigInteger[] bigs = new BigInteger[numbers];
        KeyPair paillierKeyPair = PaillierKeyPair.generateKeyPair(pklen);
        for (int i = 0; i < numbers; i++) {
            bigs[i] = new BigInteger(250, random);
            data[i] = PaillierCipher.encrypt(bigs[i], paillierKeyPair.getPublic());
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
                            int firstIndex = index % numbers;
                            int secondIndex = (index + numbers / 2) % numbers;
                            String first = data[firstIndex];
                            String second = data[secondIndex];
                            BigInteger result = bigs[firstIndex].add(bigs[secondIndex]);
                            paillierPrecompiled.paillierAdd(
                                    first,
                                    second,
                                    new TransactionCallback() {
                                        @Override
                                        public void onResponse(TransactionReceipt receipt) {
                                            long cost = System.currentTimeMillis() - now;
                                            collector.onMessage(receipt, cost);
                                            receivedBar.step();
                                            transactionLatch.countDown();
                                            totalCost.addAndGet(System.currentTimeMillis() - now);
                                            String output =
                                                    paillierPrecompiled
                                                            .getPaillierAddOutput(receipt)
                                                            .getValue1();
                                            BigInteger ret =
                                                    PaillierCipher.decrypt(
                                                            output, paillierKeyPair.getPrivate());
                                            if (!ret.equals(result)) {
                                                System.out.println(
                                                        "result not equal, return: "
                                                                + ret
                                                                + " expect: "
                                                                + result);
                                            }
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
