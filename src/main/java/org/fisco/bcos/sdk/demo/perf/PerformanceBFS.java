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
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.HelloWorld;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSPrecompiled;
import org.fisco.bcos.sdk.v3.contract.precompiled.model.PrecompiledAddress;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.EnumNodeVersion;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class PerformanceBFS {
    private static final Set<String> supportCommands =
            new HashSet<>(Arrays.asList("mkdir", "link"));
    private static String contractAddress;
    private static EnumNodeVersion.Version version;

    private static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceBFS test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceBFS [mkdir] [count] [tps] [groupId].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceBFS [link] [count] [tps] [groupId].");
    }

    public static void main(String[] args) {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = PerformanceBFS.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }
            if (args.length < 4) {
                Usage();
                return;
            }
            String command = args[0];
            int sendCount = Integer.parseInt(args[1]);
            int qps = Integer.parseInt(args[2]);
            String groupId = args[3];
            System.out.println(
                    "====== PerformanceBFS "
                            + command
                            + ", count: "
                            + sendCount
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

            version = client.getChainCompatibilityVersion();

            HelloWorld helloWorld =
                    HelloWorld.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            contractAddress = helloWorld.getContractAddress();

            BFSPrecompiled bfsPrecompiled =
                    BFSPrecompiled.load(
                            PrecompiledAddress.BFS_PRECOMPILED_ADDRESS,
                            client,
                            client.getCryptoSuite().getCryptoKeyPair());

            int totalCount = sendCount;
            CountDownLatch countDownLatch = new CountDownLatch(totalCount);
            RateLimiter limiter = RateLimiter.create(qps);
            ProgressBar sentBar =
                    new ProgressBarBuilder()
                            .setTaskName("Send   :")
                            .setInitialMax(sendCount)
                            .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                            .build();
            ProgressBar receivedBar =
                    new ProgressBarBuilder()
                            .setTaskName("Receive:")
                            .setInitialMax(totalCount)
                            .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                            .build();

            System.out.println("====== PerformanceBFS " + command + " start ======");
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "PerformanceBFS", Runtime.getRuntime().availableProcessors());

            Collector collector = new Collector();
            collector.setTotal(totalCount);
            for (int i = 0; i < sendCount; ++i) {
                limiter.acquire();
                threadPoolService
                        .getThreadPool()
                        .execute(
                                () -> {
                                    long now = System.currentTimeMillis();
                                    callBFSOperation(
                                            command,
                                            bfsPrecompiled,
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {
                                                    long cost = System.currentTimeMillis() - now;
                                                    collector.onMessage(receipt, cost);
                                                    receivedBar.step();
                                                    countDownLatch.countDown();
                                                }
                                            });
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
                    "====== PerformanceBFS test failed, error message: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void callBFSOperation(
            String command, BFSPrecompiled bfsPrecompiled, TransactionCallback callback) {
        if (command.compareToIgnoreCase("mkdir") == 0) {
            mkdir(bfsPrecompiled, callback);
        }
        if (command.compareToIgnoreCase("link") == 0) {
            link(bfsPrecompiled, callback);
        }
    }

    public static String getNextID() {
        return String.valueOf(Math.abs(new Random().nextInt())) + System.currentTimeMillis();
    }

    private static void mkdir(BFSPrecompiled bfsPrecompiled, TransactionCallback callback) {
        String nextID = getNextID();
        bfsPrecompiled.mkdir("/apps/fruit" + nextID, callback);
    }

    private static void link(BFSPrecompiled bfsPrecompiled, TransactionCallback callback) {
        String nextID = getNextID();
        if (version.compareTo(EnumNodeVersion.BCOS_3_1_0.toVersionObj()) >= 0) {
            bfsPrecompiled.link("/apps/link" + nextID, contractAddress, "", callback);
        } else {
            bfsPrecompiled.link("link", nextID, contractAddress, "", callback);
        }
    }
}
