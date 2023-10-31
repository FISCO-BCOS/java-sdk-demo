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

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import org.fisco.bcos.sdk.demo.contract.ParallelOk;
import org.fisco.bcos.sdk.demo.perf.model.DagUserInfo;
import org.fisco.bcos.sdk.demo.perf.parallel.DagPrecompiledDemo;
import org.fisco.bcos.sdk.demo.perf.parallel.ParallelOkDemo;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.precompiled.sharding.ShardingService;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class ParallelOkPerf {
    private static Client client;
    private static DagUserInfo dagUserInfo = new DagUserInfo();
    private static ShardingService shardingService;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== ParallelOk test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [parallelok] [groupId] [add] [count] [tps] [file] [isParallel].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [parallelok] [groupId] [transfer] [count] [tps] [file] [isParallel].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [parallelok] [groupId] [generate] [count] [tps] [file] [isParallel].");

        System.out.println("===== DagTransafer test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [precompiled] [groupId] [add] [count] [tps] [file].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [precompiled] [groupId] [transfer] [count] [tps] [file].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [precompiled] [groupId] [generate] [count] [tps] [file].");
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
            boolean isParallel = true;
            if (args.length < 6) {
                Usage();
                return;
            } else if (args.length == 8) {
                isParallel = Boolean.parseBoolean(args[7]);
            }
            String perfType = args[0];
            String groupId = args[1];
            String command = args[2];
            Integer count = Integer.valueOf(args[3]);
            Integer qps = Integer.valueOf(args[4]);
            String userFile = args[5];
            Integer conflictPercent = 0;
            if (args.length == 7) {
                conflictPercent = Integer.valueOf(args[6]);
            }

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            dagUserInfo.setFile(userFile);
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "ParallelOkPerf", Runtime.getRuntime().availableProcessors());
            shardingService =
                    new ShardingService(client, client.getCryptoSuite().getCryptoKeyPair());

            if (perfType.compareToIgnoreCase("parallelok") == 0) {
                parallelOkPerf(
                        groupId,
                        command,
                        count,
                        qps,
                        conflictPercent,
                        threadPoolService,
                        isParallel);
            } else if (perfType.compareToIgnoreCase("precompiled") == 0) {
                dagTransferPerf(groupId, command, count, qps, conflictPercent, threadPoolService);
            } else {
                System.out.println(
                        "invalid perf option: "
                                + perfType
                                + ", only support parallelok/precompiled now");
                Usage();
            }
        } catch (Exception e) {
            System.out.println("ParallelOkPerf test failed, error info: " + e.getMessage());
            System.exit(0);
        }
    }

    public static void parallelOkPerf(
            String groupId,
            String command,
            Integer count,
            Integer qps,
            Integer conflictPercent,
            ThreadPoolService threadPoolService,
            boolean isParallel)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== ParallelOk trans, count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId
                        + ", conflictPercent: "
                        + conflictPercent
                        + ", isParallel: "
                        + isParallel);
        ParallelOk parallelOk;
        ParallelOkDemo parallelOkDemo;
        switch (command) {
            case "add":
                // deploy ParallelOk
                parallelOk =
                        ParallelOk.deploy(
                                client, client.getCryptoSuite().getCryptoKeyPair(), isParallel);
                String shardName = "shard" + parallelOk.getContractAddress();
                try {
                    shardingService.linkShard(shardName, parallelOk.getContractAddress());
                } catch (ContractException e) {
                }

                System.out.println(
                        "====== ParallelOk userAdd, deploy success, address: " + shardName);
                parallelOkDemo = new ParallelOkDemo(parallelOk, dagUserInfo, threadPoolService);
                parallelOkDemo.userAdd(BigInteger.valueOf(count), BigInteger.valueOf(qps));
                break;
            case "transfer":
                dagUserInfo.loadDagTransferUser();
                parallelOk =
                        ParallelOk.load(
                                dagUserInfo.getContractAddr(),
                                client,
                                client.getCryptoSuite().getCryptoKeyPair());
                System.out.println(
                        "====== ParallelOk trans, load success, address: "
                                + parallelOk.getContractAddress());

                System.out.println("Start transfer...");
                parallelOkDemo = new ParallelOkDemo(parallelOk, dagUserInfo, threadPoolService);
                parallelOkDemo.userTransfer(count, BigInteger.valueOf(qps));
                break;
            case "generate":
                dagUserInfo.loadDagTransferUser();
                parallelOk =
                        ParallelOk.load(
                                dagUserInfo.getContractAddr(),
                                client,
                                client.getCryptoSuite().getCryptoKeyPair());
                parallelOkDemo = new ParallelOkDemo(parallelOk, dagUserInfo, threadPoolService);
                parallelOkDemo.generateTransferTxs(
                        client.getGroup(),
                        count,
                        "parallelOKTxs",
                        BigInteger.valueOf(qps),
                        BigInteger.valueOf(conflictPercent));
                break;
            default:
                System.out.println("invalid command: " + command);
                Usage();
                break;
        }
    }

    public static void dagTransferPerf(
            String groupId,
            String command,
            Integer count,
            Integer qps,
            Integer conflictPercent,
            ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== DagTransfer trans, count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId
                        + ", conflictPercent: "
                        + conflictPercent);

        DagPrecompiledDemo dagPrecompiledDemo;
        switch (command) {
            case "add":
                dagPrecompiledDemo = new DagPrecompiledDemo(client, dagUserInfo, threadPoolService);
                dagPrecompiledDemo.userAdd(BigInteger.valueOf(count), BigInteger.valueOf(qps));
                break;
            case "transfer":
                dagUserInfo.loadDagTransferUser();
                dagPrecompiledDemo = new DagPrecompiledDemo(client, dagUserInfo, threadPoolService);
                dagPrecompiledDemo.userTransfer(BigInteger.valueOf(count), BigInteger.valueOf(qps));
                break;
            case "generate":
                dagUserInfo.loadDagTransferUser();
                dagPrecompiledDemo = new DagPrecompiledDemo(client, dagUserInfo, threadPoolService);
                dagPrecompiledDemo.generateTransferTxs(
                        BigInteger.valueOf(count),
                        "dagTxs.txt",
                        BigInteger.valueOf(qps),
                        BigInteger.valueOf(conflictPercent));
                break;
            default:
                System.out.println("invalid command: " + command);
                Usage();
                break;
        }
    }
}
