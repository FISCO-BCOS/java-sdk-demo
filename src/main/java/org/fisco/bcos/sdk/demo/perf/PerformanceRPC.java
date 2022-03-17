package org.fisco.bcos.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
import java.net.URL;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.fisco.bcos.sdk.demo.perf.collector.PerformanceCollector;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.JsonRpcResponse;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceRPC {
    private static Logger logger = LoggerFactory.getLogger(PerformanceRPC.class);
    private static AtomicInteger sended = new AtomicInteger(0);

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceRPC totalCount qps groupID");
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
            ThreadPoolService threadPoolService = new ThreadPoolService("PerformanceRPC", 1000000);

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
                                            int random = new SecureRandom().nextInt(50000);
                                            int methodNum = 10;
                                            Long startTime = System.nanoTime();

                                            switch (random % methodNum) {
                                                    // 1. call getPendingTxSize
                                                case 0:
                                                    response = client.getPendingTxSize();
                                                    break;
                                                    // 2. call getBlockNumber
                                                case 1:
                                                    response = client.getBlockNumber();
                                                    break;
                                                    // 3. call getSyncStatus
                                                case 2:
                                                    response = client.getSyncStatus();
                                                    break;
                                                case 4:
                                                    response = client.getSealerList();
                                                    break;
                                                    // 6. call getTotalTransactionCount
                                                case 5:
                                                    response = client.getTotalTransactionCount();
                                                    break;
                                                    // . call getObserverList
                                                case 6:
                                                    response = client.getObserverList();
                                                    break;
                                                    // 9. call getSystemConfigByKey
                                                case 7:
                                                    response =
                                                            client.getSystemConfigByKey(
                                                                    "tx_count_limit");
                                                    break;
                                                    // 10. call getPbftView
                                                case 8:
                                                    response = client.getPbftView();
                                                    break;
                                                default:
                                                    // default call getPbftView
                                                    response = client.getPbftView();
                                            }
                                            Long cost = System.nanoTime() - startTime;
                                            collector.onRpcMessage(response, cost / 1000000);

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
