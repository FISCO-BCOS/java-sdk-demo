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
import java.util.Random;
import org.fisco.bcos.sdk.demo.contract.ParallelOkLiquid;
import org.fisco.bcos.sdk.demo.perf.model.DagUserInfo;
import org.fisco.bcos.sdk.demo.perf.parallel.ParallelLiquidDemo;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class ParallelLiquidPerf {
    private static Client client;
    private static final DagUserInfo dagUserInfo = new DagUserInfo();

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== ParallelLiquid test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelLiquidPerf [groupId] [add] [count] [tps] [file].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelLiquidPerf [groupId] [transfer] [count] [tps] [file].");
    }

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = ParallelLiquidPerf.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }
            if (args.length < 5) {
                Usage();
                return;
            }
            String groupId = args[0];
            String command = args[1];
            Integer count = Integer.valueOf(args[2]);
            Integer qps = Integer.valueOf(args[3]);
            String userFile = args[4];

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            dagUserInfo.setFile(userFile);
            ThreadPoolService threadPoolService = new ThreadPoolService("ParallelLiquid", 1000000);
            parallelLiquidPerf(groupId, command, count, qps, threadPoolService);
        } catch (Exception e) {
            System.out.println("ParallelLiquid test failed, error info: " + e.getMessage());
            System.exit(0);
        }
    }

    public static void parallelLiquidPerf(
            String groupId,
            String command,
            Integer count,
            Integer qps,
            ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== ParallelLiquid trans, count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId);
        ParallelOkLiquid parallelOkLiquid;
        String parallelOkPath = "parallelOk" + new Random().nextInt(Integer.MAX_VALUE);
        ParallelLiquidDemo parallelLiquidDemo;
        switch (command) {
            case "add":
                // deploy ParallelOk
                parallelOkLiquid =
                        ParallelOkLiquid.deploy(
                                client, client.getCryptoSuite().getCryptoKeyPair(), parallelOkPath);
                System.out.println(
                        "====== ParallelLiquid userAdd, deploy success, address: "
                                + parallelOkLiquid.getContractAddress());
                parallelLiquidDemo =
                        new ParallelLiquidDemo(parallelOkLiquid, dagUserInfo, threadPoolService);
                parallelLiquidDemo.userAdd(BigInteger.valueOf(count), BigInteger.valueOf(qps));
                break;
            case "transfer":
                dagUserInfo.loadDagTransferUser();
                parallelOkLiquid =
                        ParallelOkLiquid.load(
                                dagUserInfo.getContractAddr(),
                                client,
                                client.getCryptoSuite().getCryptoKeyPair());
                System.out.println(
                        "====== ParallelLiquid trans, load success, address: "
                                + parallelOkLiquid.getContractAddress());
                parallelLiquidDemo =
                        new ParallelLiquidDemo(parallelOkLiquid, dagUserInfo, threadPoolService);
                parallelLiquidDemo.userTransfer(BigInteger.valueOf(count), BigInteger.valueOf(qps));
                break;

            default:
                System.out.println("invalid command: " + command);
                Usage();
                break;
        }
    }
}
