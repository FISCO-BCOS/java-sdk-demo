/**
 * Copyright 2014-2022 [fisco-dev]
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
package org.fisco.bcos.sdk.demo.perf.dgms;

import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.lang3.RandomStringUtils;
import org.fisco.bcos.sdk.demo.contract.dgms.Factory;
import org.fisco.bcos.sdk.demo.contract.dgms.OneToOne;
import org.fisco.bcos.sdk.demo.perf.Collector;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.ContractCodec;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

/** @author monan */
public class PerformanceDGMS {
    private static Client client;

    public static void usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceDMC test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceDMC [groupId] [userCount] [count] [qps].");
    }

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = PerformanceDGMS.class.getClassLoader().getResource(configFileName);
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

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            start(groupId, count, qps);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void start(String groupId, int count, Integer qps)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start test count: " + count + ", qps:" + qps + ", groupId: " + groupId);

        RateLimiter limiter = RateLimiter.create(qps.intValue());
        Factory factory = Factory.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
        TransactionReceipt receipt = factory.deployOne(BigInteger.valueOf(count));

        ContractCodec codec = new ContractCodec(client.getCryptoSuite(), false);
        String address;
        try {
            List<Type> result =
                    codec.decodeMethodAndGetOutputObject(
                            Factory.getABI(), "deployOne", receipt.getOutput());
            address = (String) result.get(0).getValue();
        } catch (ContractCodecException e1) {
            e1.printStackTrace();
            return;
        }

        System.out.println("Contract address: " + address);
        OneToOne oneToOne =
                OneToOne.load(address, client, client.getCryptoSuite().getCryptoKeyPair());

        // Generate users
        System.out.println("Create users...");
        User[] users = new User[count];
        IntStream.range(0, count)
                .parallel()
                .forEach(
                        i -> {
                            User user = new User();
                            user.setName("0x" + RandomStringUtils.random(40, "0123456789abcdef"));
                            user.setValue(i);

                            users[i] = user;
                        });
        System.out.println("Create users finished!");

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
                        i -> {
                            limiter.acquire();
                            long now = System.currentTimeMillis();
                            oneToOne.issueAsset(
                                    users[i].getName(),
                                    BigInteger.valueOf(users[i].getValue()),
                                    "This is a test asset",
                                    "This is a test asset data".getBytes(),
                                    new TransactionCallback() {
                                        @Override
                                        public void onResponse(TransactionReceipt receipt) {
                                            if (!receipt.isStatusOK()) {
                                                System.out.println(
                                                        "Send transaction error! "
                                                                + receipt.getStatus()
                                                                + " "
                                                                + receipt.getMessage());
                                            }

                                            long cost = System.currentTimeMillis() - now;
                                            collector.onMessage(receipt, cost);

                                            transactionLatch.countDown();
                                            receivedBar.step();
                                        }
                                    });

                            sendedBar.step();
                        });
        transactionLatch.await();

        sendedBar.close();
        receivedBar.close();
        collector.report();
        System.out.println("Sending transactions finished!");

        System.out.println("Checking result...");
        IntStream.range(0, count)
                .parallel()
                .forEach(
                        i -> {
                            limiter.acquire();
                            try {
                                String user =
                                        oneToOne.ownerOf(BigInteger.valueOf(users[i].getValue()));
                                if (!user.equals(users[i].getName())) {
                                    System.out.println("Mismatch user!!!");
                                }
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });

        System.out.println("Checking finished!");
    }
}
