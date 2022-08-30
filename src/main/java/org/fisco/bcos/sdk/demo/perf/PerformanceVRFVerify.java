package org.fisco.bcos.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import org.fisco.bcos.sdk.demo.contract.Curve25519VRFVerifyTest;
import org.fisco.bcos.sdk.demo.perf.callback.PerformanceCallback;
import org.fisco.bcos.sdk.demo.perf.collector.PerformanceCollector;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.crypto.vrf.Curve25519VRF;
import org.fisco.bcos.sdk.v3.crypto.vrf.VRFInterface;
import org.fisco.bcos.sdk.v3.crypto.vrf.VRFKeyPair;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.utils.Hex;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceVRFVerify {
    private static Logger logger = LoggerFactory.getLogger(PerformanceVRFVerify.class);
    private static AtomicInteger sended = new AtomicInteger(0);

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceVRFVerify totalCount qps groupID");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = PerformanceVRFVerify.class.getClassLoader().getResource(configFileName);

            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }

            if (args.length < 3) {
                Usage();
            }

            Integer count = Integer.parseInt(args[0]);
            Integer qps = Integer.parseInt(args[1]);
            String groupId = args[2];
            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            Client client = sdk.getClient(groupId);

            System.out.println("Start test...");
            System.out.println(
                    "===================================================================");

            PerformanceCollector collector = new PerformanceCollector();
            collector.setTotal(count);

            RateLimiter limiter = RateLimiter.create(qps);
            Integer area = count / 10;
            final Integer total = count;
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "PerformanceVRFVerify", Runtime.getRuntime().availableProcessors());

            Curve25519VRFVerifyTest curve25519VRFVerifyTest =
                    Curve25519VRFVerifyTest.deploy(
                            client, client.getCryptoSuite().generateRandomKeyPair());

            VRFInterface vrfInterface = new Curve25519VRF();
            VRFKeyPair vrfKeyPair = vrfInterface.createKeyPair();
            System.out.println("Start test, total: " + count);
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
                                            String input = "vrf-test";
                                            String vrfProof =
                                                    vrfInterface.generateVRFProof(
                                                            vrfKeyPair.getVrfPrivateKey(), input);
                                            curve25519VRFVerifyTest.curve25519VRFVerify(
                                                    input.getBytes(),
                                                    Hex.decode(vrfKeyPair.getVrfPublicKey()),
                                                    Hex.decode(vrfProof),
                                                    callback);
                                        } catch (Exception e) {
                                            TransactionReceipt receipt = new TransactionReceipt();
                                            receipt.setStatus(-1);
                                            callback.onResponse(receipt);
                                            logger.info(e.getMessage());
                                        }

                                        int current = sended.incrementAndGet();
                                        if (current >= area && ((current % area) == 0)) {
                                            System.out.println(
                                                    "Already sended: " + current + "/" + total);
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
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
