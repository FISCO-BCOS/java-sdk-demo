package org.fisco.bcos.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
import java.math.BigInteger;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.vrf.Curve25519VRF;
import org.fisco.bcos.sdk.crypto.vrf.VRFInterface;
import org.fisco.bcos.sdk.crypto.vrf.VRFKeyPair;
import org.fisco.bcos.sdk.demo.contract.Curve25519VRFVerifyTest;
import org.fisco.bcos.sdk.demo.perf.collector.PerformanceCollector;
import org.fisco.bcos.sdk.model.ConstantConfig;
import org.fisco.bcos.sdk.model.JsonRpcResponse;
import org.fisco.bcos.sdk.utils.ThreadPoolService;
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
            URL configUrl = PerformanceOk.class.getClassLoader().getResource(configFileName);

            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }

            if (args.length < 3) {
                Usage();
            }

            Integer count = Integer.parseInt(args[0]);
            Integer qps = Integer.parseInt(args[1]);
            int groupId = Integer.valueOf(args[2]);
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
                            "PerformanceVRFVerify",
                            sdk.getConfig().getThreadPoolConfig().getMaxBlockingQueueSize());

            Curve25519VRFVerifyTest curve25519VRFVerifyTest =
                    Curve25519VRFVerifyTest.deploy(client, client.getCryptoSuite().createKeyPair());

            VRFInterface vrfInterface = new Curve25519VRF();
            VRFKeyPair vrfKeyPair = vrfInterface.createKeyPair();
            System.out.println("Start test, total: " + count);
            for (Integer i = 0; i < count; ++i) {
                threadPoolService
                        .getThreadPool()
                        .execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        limiter.acquire();
                                        JsonRpcResponse response = new JsonRpcResponse();
                                        try {
                                            Long startTime = System.nanoTime();
                                            String input =
                                                    "testVrf" + String.valueOf(System.nanoTime());
                                            String vrfProof =
                                                    vrfInterface.generateVRFProof(
                                                            vrfKeyPair.getVrfPrivateKey(), input);
                                            startTime = System.nanoTime();
                                            Tuple2<Boolean, BigInteger> result =
                                                    curve25519VRFVerifyTest.curve25519VRFVerify(
                                                            input,
                                                            vrfKeyPair.getVrfPublicKey(),
                                                            vrfProof);
                                            Long cost = System.nanoTime() - startTime;
                                            if (result.getValue1() == true) {
                                                collector.onRpcMessage(response, cost / 1000000);
                                            } else {
                                                JsonRpcResponse.Error error =
                                                        new JsonRpcResponse.Error();
                                                error.setCode(1);
                                                response.setError(error);
                                                collector.onRpcMessage(response, cost / 1000000);
                                            }

                                        } catch (Exception e) {
                                            logger.error(
                                                    "test rpc interface failed, error info: {}",
                                                    e.getMessage());
                                            JsonRpcResponse.Error error =
                                                    new JsonRpcResponse.Error();
                                            error.setCode(1);
                                            response.setError(error);
                                            collector.onRpcMessage(response, 0L);
                                        }

                                        int current = sended.incrementAndGet();

                                        if (current >= area && ((current % area) == 0)) {
                                            System.out.println(
                                                    "Already sended: "
                                                            + current
                                                            + "/"
                                                            + total
                                                            + " RPC Requests");
                                        }
                                    }
                                });
            }
            while (collector.getReceived().longValue() < collector.getTotal().longValue()) {
                Thread.sleep(50);
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
