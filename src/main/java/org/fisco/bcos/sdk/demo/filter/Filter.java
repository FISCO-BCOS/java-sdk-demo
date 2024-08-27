package org.fisco.bcos.sdk.demo.filter;

import org.fisco.bcos.sdk.jni.common.JniException;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.request.DefaultBlockParameter;
import org.fisco.bcos.sdk.v3.client.protocol.request.DefaultBlockParameterName;
import org.fisco.bcos.sdk.v3.client.protocol.request.LogFilterRequest;
import org.fisco.bcos.sdk.v3.client.protocol.response.Log;
import org.fisco.bcos.sdk.v3.codec.EventEncoder;
import org.fisco.bcos.sdk.v3.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.v3.filter.FilterException;
import org.fisco.bcos.sdk.v3.filter.FilterSystem;
import org.fisco.bcos.sdk.v3.filter.Subscription;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Filter {

    private static final Logger logger = LoggerFactory.getLogger(Filter.class);

    public static void usage() {
        System.out.println("\tUsage: ");
        System.out.println("\t\tjava -cp \"conf/:lib/*:apps/*\"  org.fisco.bcos.sdk.demo.filter.Filter groupId type fromBlock toBlock addresses<Optional> topics<Optional>");
        System.out.println("\t type:               choices[onlyBlock,onlyTx,onlyLog,All]");
        System.out.println("\t addresses format:   [addr1,addr2,...]");
        System.out.println("\t topics format:      [[topic1,topic2],[topic1,topic2],...]");
        System.out.println("\t\n");
        System.out.println("\tExample:");
        System.out.println("\t\tjava -cp \"conf/:lib/*:apps/*\"  org.fisco.bcos.sdk.demo.filter.Filter group All latest latest 0x37a44585Bf1e9618FDb4C62c4c96189A07Dd4b48");
        System.out.println("\t\tjava -cp \"conf/:lib/*:apps/*\"  org.fisco.bcos.sdk.demo.filter.Filter group All latest latest [0x37ce8dAbeeB140FC64247c421820c5887918723c,0xDB0F7d18ef07D709DDDAbA0bD6443863125f9d3d] [[TransferEvent(int256,string,string,uint256),UpdateEvent(int256,string,uint256)],[test1,test2]]");
        System.exit(0);
    }

    public static DefaultBlockParameter getBlockNumber(String arg) {
        if (arg.equals("latest")) {
            return DefaultBlockParameterName.LATEST;
        } else if (arg.equals("earliest")) {
            return DefaultBlockParameterName.EARLIEST;
        }
        return DefaultBlockParameter.valueOf(Integer.valueOf(arg));
    }

    public static List<String> parseAddress(String arg) {
        String input = arg;
        if (arg.startsWith("[") && arg.endsWith("]")) {
            input = arg.substring(1, arg.length() - 1);
        }
        String[] items = input.split(",");
        List<String> resultList = new ArrayList<>();
        for (String item : items) {
            item = item.trim().toLowerCase();
            resultList.add(item);
        }
        return resultList;
    }

    public static List<List<String>> parseTopic(String arg, EventEncoder encoder) {
        String input = arg;
        if (arg.startsWith("[[") && arg.endsWith("]]")) {
            input = arg.substring(2, arg.length() - 2).trim();
        }
        String[] groups = input.split("\\],\\[");
        List<List<String>> resultList = new ArrayList<>();
        for (String group : groups) {
            String[] items = group.split(",");
            List<String> currentGroup = new ArrayList<>();
            Boolean matchFunc = false;
            StringBuilder funcBuilder = new StringBuilder();
            for (String item : items) {
                String res = item;
                if (!matchFunc && item.contains("(")) {
                    matchFunc = true;
                }
                if (matchFunc) {
                    if (item.contains(")")) {
                        matchFunc = false;
                        funcBuilder.append(item);
                        res = funcBuilder.toString();
                        funcBuilder = new StringBuilder();
                    } else {
                        funcBuilder.append(item);
                        funcBuilder.append(',');
                    }
                }
                currentGroup.add(encoder.buildEventSignature(res));
            }
            resultList.add(currentGroup);
        }
//        if (resultList.size() > 4) {
//            throw new RuntimeException(
//                    "invalid topics, topics size must be <= 4");
//        }
        return resultList;
    }

    public static LogFilterRequest getEthFilter(String[] args, Client client) {
        DefaultBlockParameter fromBlk = getBlockNumber(args[2]);
        DefaultBlockParameter toBlk = getBlockNumber(args[3]);

        if (args.length <= 4) {
            return new LogFilterRequest(fromBlk, toBlk);
        }

        LogFilterRequest ethFilter = new LogFilterRequest(fromBlk, toBlk, parseAddress(args[4]));
        if (args.length <= 5) {
            return ethFilter;
        }

        EventEncoder encoder = new EventEncoder(client.getCryptoSuite().getHashImpl());
        List<List<String>> topics = parseTopic(args[5], encoder);
        for (int i = 0; i < topics.size(); i++) {
            if (topics.get(i).size() == 0) {
                ethFilter.addNullTopic();
            } else {
                ethFilter.addOptionalTopics(topics.get(i).toArray(new String[0]));
            }
        }
        return ethFilter;
    }

    public static void runOnlyBlock(String[] args, BcosSDK sdk) throws IOException {
        System.out.println("runOnlyBlock...");
        Client client = sdk.getClient(args[0]);
        FilterSystem filterSystem = sdk.getFilterSystem(client, 1, 1000);
        Subscription<String> blockSub = filterSystem.blockHashPublisher().subscribe(block -> {
            System.out.println("blockHash: " + block);
        });
        System.in.read();
        blockSub.unsubscribe();
        filterSystem.stop();
        System.exit(0);
    }

    public static void runOnlyTx(String[] args, BcosSDK sdk) throws IOException {
        System.out.println("runOnlyTx...");
        Client client = sdk.getClient(args[0]);
        FilterSystem filterSystem = sdk.getFilterSystem(client, 1, 1000);

        Subscription<String> txSub = filterSystem.transactionHashPublisher().subscribe(tx -> {
            System.out.println("txHash: " + tx);
        });

        System.in.read();
        txSub.unsubscribe();
        filterSystem.stop();
        System.exit(0);
    }

    public static void runOnlyLog(String[] args, BcosSDK sdk) throws IOException {
        System.out.println("runOnlyLog...");
        if (args.length < 4) {
            usage();
        }

        try {
            Client client = sdk.getClient(args[0]);
            FilterSystem filterSystem = sdk.getFilterSystem(client, 1, 1000);
            LogFilterRequest ethFilter = getEthFilter(args, client);
            Subscription<Log> logSub = filterSystem.logPublisher(ethFilter).subscribe(log -> {
                System.out.println("logs: " + log);
            });

            System.in.read();
            logSub.unsubscribe();
            filterSystem.stop();
            System.exit(0);
        } catch (FilterException ex) {
            System.out.println(ex.getCause().getMessage());
            System.exit(-1);
        }
    }

    public static void runAll(String[] args, BcosSDK sdk) throws IOException {
        System.out.println("runAll...");
        if (args.length < 4) {
            usage();
        }
        try {
            Client client = sdk.getClient(args[0]);
            FilterSystem filterSystem = sdk.getFilterSystem(client, 1, 1000);
            LogFilterRequest ethFilter = getEthFilter(args, client);
            Subscription<String> blockSub = filterSystem.blockHashPublisher().subscribe(block -> {
                System.out.println("blockHash: " + block);
            });

            Subscription<String> txSub = filterSystem.transactionHashPublisher().subscribe(tx -> {
                System.out.println("txHash: " + tx);
            });

            Subscription<Log> logSub = filterSystem.logPublisher(ethFilter).subscribe(log -> {
                System.out.println("logs: " + log);
            });

            System.in.read();
            blockSub.unsubscribe();
            txSub.unsubscribe();
            logSub.unsubscribe();
            filterSystem.stop();
            System.exit(0);
        } catch (FilterException ex) {
            System.out.println(ex.getCause().getMessage());
            System.exit(-1);
        }
    }

    public static void main(String[] args) throws ConfigException, JniException, IOException {
        String configFileName = ConstantConfig.CONFIG_FILE_NAME;
        URL configUrl = Filter.class.getClassLoader().getResource(configFileName);
        if (configUrl == null) {
            System.out.println("The configFile " + configFileName + " doesn't exist!");
            return;
        }

        String configFile = configUrl.getPath();
        BcosSDK sdk = BcosSDK.build(configFile);

        System.out.println(args.length);
        if (args.length < 2) {
            usage();
        }

        if (args[1].equals("onlyBlock")) {
            runOnlyBlock(args, sdk);
        } else if (args[1].equals("onlyTx")) {
            runOnlyTx(args, sdk);
        } else if (args[1].equals("onlyLog")) {
            runOnlyLog(args, sdk);
        } else if (args[1].equals("All")) {
            runAll(args, sdk);
        } else {
            System.out.println("Invalid type: " + args[1]);
        }
    }
}