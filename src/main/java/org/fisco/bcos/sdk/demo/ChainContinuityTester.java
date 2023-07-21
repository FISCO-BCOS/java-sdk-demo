package org.fisco.bcos.sdk.demo;

import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.v3.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.v3.client.protocol.response.SealerList;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.Response;
import org.fisco.bcos.sdk.v3.model.callback.RespCallback;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class ChainContinuityTester {
    private static Client client;
    private static BigInteger toBlock;
    private static final ConcurrentHashMap<String, ConcurrentHashMap<Long, BcosBlock.Block>>
            nodeBlockMap = new ConcurrentHashMap<>();

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("========== Chain Testers ==========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.ChainContinuityTester [groupId] [fromBlock] [toBlock] [nodeId...].");
    }

    public static void main(String[] args) throws InterruptedException {
        String configFileName = ConstantConfig.CONFIG_FILE_NAME;
        URL configUrl = ChainContinuityTester.class.getClassLoader().getResource(configFileName);
        if (configUrl == null) {
            System.out.println("The configFile " + configFileName + " doesn't exist!");
            return;
        }
        if (args.length < 3) {
            Usage();
            return;
        }
        String groupId = args[0];
        BigInteger fromBlock = new BigInteger(args[1]);
        toBlock = new BigInteger(args[2]);
        if (fromBlock.compareTo(BigInteger.ZERO) < 0) {
            System.out.println("fromBlock must be greater than 0");
            return;
        }
        if (toBlock.compareTo(fromBlock) < 0 && !toBlock.equals(BigInteger.valueOf(-1))) {
            System.out.println("toBlock must be greater than fromBlock");
            return;
        }
        List<String> nodeIds = new ArrayList<>();
        if (args.length > 3) {
            nodeIds.addAll(Arrays.asList(args).subList(3, args.length));
        }
        String configFile = configUrl.getPath();
        BcosSDK sdk = BcosSDK.build(configFile);
        client = sdk.getClient(groupId);
        ThreadPoolService threadPoolService =
                new ThreadPoolService(
                        "ChainContinuityTester", Runtime.getRuntime().availableProcessors());
        if (nodeIds.isEmpty()) {
            SealerList sealerList = client.getSealerList();
            SealerList.Sealer sealer =
                    sealerList
                            .getSealerList()
                            .get(new Random().nextInt(sealerList.getSealerList().size()));
            getNodeBlocks(sealer.getNodeID(), fromBlock, threadPoolService);
        } else {
            for (String nodeId : nodeIds) {
                getNodeBlocks(nodeId, fromBlock, threadPoolService);
            }
        }
        System.out.println("Finish get node blocks, begin to check continuity...");
        System.out.println("===================================================");
        for (Map.Entry<String, ConcurrentHashMap<Long, BcosBlock.Block>> concurrentHashMapEntry :
                nodeBlockMap.entrySet()) {
            checkBlockContinuity(concurrentHashMapEntry);
        }
        System.out.println("Check continuity finished.");
        System.out.println("===================================================");
        if (nodeIds.isEmpty()) {
            System.exit(0);
        }
        System.out.println("Begin to check block consistency...");
        List<ConcurrentHashMap<Long, BcosBlock.Block>> values =
                new ArrayList<>(nodeBlockMap.values());

        ProgressBar checkBar =
                new ProgressBarBuilder()
                        .setTaskName("Checking   :")
                        .setInitialMax(toBlock.intValue())
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        for (long i = fromBlock.longValue(); i < toBlock.longValue(); i++) {
            BcosBlock.Block block = values.get(0).get(i);
            if (block == null) {
                checkBar.close();
                break;
            }
            for (int j = 1; j < nodeBlockMap.size(); j++) {
                BcosBlock.Block block1 = values.get(j).get(i);
                if (block1 == null) {
                    checkBar.step();
                    break;
                }
                boolean equals = block1.equals(block);
                equals = equals && (block1.getTimestamp() == block.getTimestamp());
                equals =
                        equals
                                && (Objects.equals(
                                        block1.getReceiptsRoot(), block.getReceiptsRoot()));
                equals =
                        equals
                                && (Objects.equals(
                                        block1.getTransactionsRoot(), block.getTransactionsRoot()));
                equals = equals && (Objects.equals(block.getStateRoot(), block1.getStateRoot()));
                equals = equals && block1.getHash().equals(block.getHash());
                equals = equals && block1.getGasUsed().equals(block.getGasUsed());
                if (!equals) {
                    System.out.println("ERROR: block continuity check failed, blockNumber:" + i);
                }
                checkBar.step();
            }
            checkBar.close();
        }
        System.out.println("===================================================");
        System.exit(0);
    }

    private static void checkBlockContinuity(
            Map.Entry<String, ConcurrentHashMap<Long, BcosBlock.Block>> concurrentHashMapEntry) {
        String nodeId = concurrentHashMapEntry.getKey();
        ConcurrentHashMap<Long, BcosBlock.Block> blockConcurrentHashMap =
                concurrentHashMapEntry.getValue();
        ConcurrentSkipListSet<String> transactionHashes = new ConcurrentSkipListSet<>();
        final AtomicLong lastTimestamp = new AtomicLong(0);
        System.out.println("Checking " + nodeId.substring(0, 8) + "... continuity...");
        blockConcurrentHashMap.forEach(
                (blockNumber, block) -> {
                    if (block.getTimestamp() < lastTimestamp.get()) {
                        System.out.println(
                                "block continuity timestamp check failed, nodeId:"
                                        + nodeId
                                        + ", blockNumber:"
                                        + blockNumber
                                        + ", timestamp:"
                                        + block.getTimestamp()
                                        + ", lastTimestamp:"
                                        + lastTimestamp);
                    }
                    lastTimestamp.set(block.getTimestamp());
                    block.getTransactionHashes()
                            .forEach(
                                    transactionHash -> {
                                        if (!transactionHashes.add(transactionHash.get())) {
                                            System.out.println(
                                                    "ERROR: block continuity transactionHash check failed, nodeId:"
                                                            + nodeId
                                                            + ", blockNumber:"
                                                            + blockNumber
                                                            + ", transactionHash:"
                                                            + transactionHash.get());
                                        }
                                    });
                });
    }

    public static void getNodeBlocks(
            String nodeId, BigInteger fromBlk, ThreadPoolService threadPoolService)
            throws InterruptedException {
        BlockNumber blockNumber = client.getBlockNumber(nodeId);
        if (toBlock.equals(BigInteger.valueOf(-1))
                || toBlock.compareTo(blockNumber.getBlockNumber()) > 0) {
            toBlock = blockNumber.getBlockNumber();
        }
        ConcurrentHashMap<Long, BcosBlock.Block> blockConcurrentHashMap = new ConcurrentHashMap<>();
        System.out.println(
                "Get node blocks..., nodeId:"
                        + nodeId
                        + ", blockNumber:"
                        + blockNumber.getBlockNumber().intValue()
                        + ", fromBlock:"
                        + fromBlk.intValue()
                        + ", toBlock:"
                        + toBlock.intValue());

        CountDownLatch countDownLatch = new CountDownLatch(toBlock.intValue());
        ProgressBar sentBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(toBlock.intValue())
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(toBlock.intValue())
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        for (long i = fromBlk.longValue(); i < toBlock.longValue(); i++) {
            long finalI = i;
            threadPoolService
                    .getThreadPool()
                    .execute(
                            () -> {
                                client.getBlockByNumberAsync(
                                        nodeId,
                                        BigInteger.valueOf(finalI),
                                        false,
                                        true,
                                        new RespCallback<BcosBlock>() {
                                            @Override
                                            public void onResponse(BcosBlock bcosBlock) {
                                                blockConcurrentHashMap.put(
                                                        finalI, bcosBlock.getBlock());
                                                countDownLatch.countDown();
                                                receivedBar.step();
                                            }

                                            @Override
                                            public void onError(Response errorResponse) {
                                                blockConcurrentHashMap.put(
                                                        finalI, new BcosBlock.Block());
                                                countDownLatch.countDown();
                                                receivedBar.step();
                                            }
                                        });
                                sentBar.step();
                            });
        }
        countDownLatch.await();
        sentBar.close();
        receivedBar.close();
        nodeBlockMap.put(nodeId, blockConcurrentHashMap);
    }
}
