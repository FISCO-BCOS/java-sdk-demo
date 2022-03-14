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
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.fisco.bcos.sdk.demo.contract.EvidenceVerify;
import org.fisco.bcos.sdk.demo.perf.callback.PerformanceCallback;
import org.fisco.bcos.sdk.demo.perf.collector.PerformanceCollector;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.BcosSDKException;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceEvidenceVerify {
    private static Logger logger = LoggerFactory.getLogger(PerformanceEvidenceVerify.class);
    private static AtomicInteger sendedTransactions = new AtomicInteger(0);

    private static void Usage() {
        System.out.println(" Usage:");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceEvidenceVerify [count] [tps] [groupId].");
    }

    public static void main(String[] args) {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl =
                    PerformanceEvidenceVerify.class.getClassLoader().getResource(configFileName);

            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }
            if (args.length < 3) {
                Usage();
                return;
            }
            Integer count = Integer.valueOf(args[0]);
            Integer qps = Integer.valueOf(args[1]);
            String groupId = args[2];
            System.out.println(
                    "====== PerformanceEvidenceVerify trans, count: "
                            + count
                            + ", qps:"
                            + qps
                            + ", groupId: "
                            + groupId);

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);

            // build the client
            Client client = sdk.getClient(groupId);

            // deploy the HelloWorld
            System.out.println("====== Deploy EvidenceVerify ====== ");
            EvidenceVerify evidenceVerify =
                    EvidenceVerify.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            System.out.println(
                    "====== Deploy EvidenceVerify success, address: "
                            + evidenceVerify.getContractAddress()
                            + " ====== ");

            PerformanceCollector collector = new PerformanceCollector();
            collector.setTotal(count);
            RateLimiter limiter = RateLimiter.create(qps);
            Integer area = count / 10;
            final Integer total = count;

            System.out.println("====== PerformanceEvidenceVerify trans start ======");

            ThreadPoolService threadPoolService =
                    new ThreadPoolService("PerformanceEvidenceVerify", 1000000);

            CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
            for (Integer i = 0; i < count; ++i) {
                limiter.acquire();
                threadPoolService
                        .getThreadPool()
                        .execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        PerformanceCallback callback = new PerformanceCallback();
                                        callback.setTimeout(0);
                                        callback.setCollector(collector);
                                        try {
                                            String evi = "test";
                                            String evInfo = "test_info";
                                            int random = new SecureRandom().nextInt(50000);
                                            String eviId = String.valueOf(random);
                                            // sign to evi
                                            byte[] message = ecdsaCryptoSuite.hash(evi.getBytes());
                                            CryptoKeyPair cryptoKeyPair =
                                                    ecdsaCryptoSuite.generateRandomKeyPair();
                                            // sign with secp256k1
                                            ECDSASignatureResult signatureResult =
                                                    (ECDSASignatureResult)
                                                            ecdsaCryptoSuite.sign(
                                                                    message, cryptoKeyPair);
                                            String signAddr = cryptoKeyPair.getAddress();
                                            evidenceVerify.insertEvidence(
                                                    evi,
                                                    evInfo,
                                                    eviId,
                                                    signAddr,
                                                    message,
                                                    BigInteger.valueOf(signatureResult.getV() + 27),
                                                    signatureResult.getR(),
                                                    signatureResult.getS(),
                                                    callback);
                                        } catch (Exception e) {
                                            TransactionReceipt receipt = new TransactionReceipt();
                                            receipt.setStatus(-1);
                                            callback.onResponse(receipt);
                                            logger.info(e.getMessage());
                                        }
                                        int current = sendedTransactions.incrementAndGet();
                                        if (current >= area && ((current % area) == 0)) {
                                            System.out.println(
                                                    "Already send: "
                                                            + current
                                                            + "/"
                                                            + total
                                                            + " transactions");
                                        }
                                    }
                                });
            }
            // wait to collect all the receipts
            while (!collector.getReceived().equals(count)) {
                Thread.sleep(1000);
            }
            threadPoolService.stop();
            System.exit(0);
        } catch (BcosSDKException | ContractException | InterruptedException e) {
            System.out.println(
                    "====== PerformanceEvidenceVerify test failed, error message: "
                            + e.getMessage());
            System.exit(0);
        }
    }
}
