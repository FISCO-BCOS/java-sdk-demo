package org.fisco.bcos.sdk.demo.eventsub;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.sdk.demo.perf.ParallelOkPerf;
import org.fisco.bcos.sdk.jni.common.JniException;
import org.fisco.bcos.sdk.v3.config.Config;
import org.fisco.bcos.sdk.v3.config.ConfigOption;
import org.fisco.bcos.sdk.v3.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.v3.eventsub.EventSubCallback;
import org.fisco.bcos.sdk.v3.eventsub.EventSubParams;
import org.fisco.bcos.sdk.v3.eventsub.EventSubscribe;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.EventLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventSub {

    private static final Logger logger = LoggerFactory.getLogger(EventSub.class);

    public static void usage() {
        System.out.println("\tUsage: ");
        System.out.println(
                "\t\tjava -cp \"conf/:lib/*:apps/*\"  org.fisco.bcos.sdk.demo.eventsub.EventSub groupId fromBlock toBlock addresses(Optional) topics<Optional>");
        System.out.println("\t addresses format:   addr1#addr2#addr3");
        System.out.println(
                "\t topics format:      0:topic0#topic1#topic2<Optional> 1:topic0#topic1#topic2<Optional> 2:topic0#topic1#topic2<Optional>");
        System.out.println("\t\n");
        System.out.println("\tExample:");
        System.out.println(
                "\t\tjava -cp \"conf/:lib/*:apps/*\"  org.fisco.bcos.sdk.demo.eventsub.EventSub group -1 -1 0x37a44585Bf1e9618FDb4C62c4c96189A07Dd4b48");
        System.out.println(
                "\t\tjava -cp \"conf/:lib/*:apps/*\"  org.fisco.bcos.sdk.demo.eventsub.EventSub group -1 -1 0x37ce8dAbeeB140FC64247c421820c5887918723c#0xDB0F7d18ef07D709DDDAbA0bD6443863125f9d3d 0:0x37ce8dAbeeB140FC64247c421820c5887918723c#0xDB0F7d18ef07D709DDDAbA0bD6443863125f9d3d 1:0x37ce8dAbeeB140FC64247c421820c5887918723c#0xDB0F7d18ef07D709DDDAbA0bD6443863125f9d3d");
        System.exit(0);
    }

    public static void main(String[] args) throws ConfigException, JniException, IOException {
        String configFileName = ConstantConfig.CONFIG_FILE_NAME;
        URL configUrl = ParallelOkPerf.class.getClassLoader().getResource(configFileName);
        if (configUrl == null) {
            System.out.println("The configFile " + configFileName + " doesn't exist!");
            return;
        }

        String configFile = configUrl.getPath();
        ConfigOption configOption = Config.load(configFile);

        if (args.length < 3) {
            usage();
        }

        String group = args[0];
        BigInteger fromBlk = BigInteger.valueOf(Long.valueOf(args[1]));
        BigInteger toBlk = BigInteger.valueOf(Long.valueOf(args[2]));
        List<String> addresses = new ArrayList<>();
        List<List<String>> topics =
                new ArrayList<List<String>>() {
                    {
                        add(null);
                        add(null);
                        add(null);
                        add(null);
                    }
                };

        if (args.length > 3) {
            String strAddr = args[3];
            String[] split = strAddr.split("#");
            if (split.length > 0) {
                for (int i = 0; i < split.length; i++) {
                    String s = split[i];
                    if (s != null && s.length() > 0) {
                        addresses.add(s);
                    }
                }
            }
        }

        if (args.length > 4) {
            for (int i = 4; i < args.length; i++) {
                String topicsInfo = args[i];

                String[] split = topicsInfo.split(":");
                if (split == null || split.length < 2) {
                    throw new RuntimeException("invalid topics info string: " + args[i]);
                }

                int index = Integer.valueOf(split[0]);
                if (index >= 4) {
                    throw new RuntimeException(
                            "invalid topic index, index must be <= 3:, info: " + args[i]);
                }

                String[] split1 = split[1].split("#");
                for (int j = 0; j < split1.length; j++) {
                    List<String> strings = topics.get(index);
                    if (strings == null) {
                        strings = new ArrayList<>();
                        topics.set(index, strings);
                    }
                    strings.add(split1[j]);
                }
            }
        }

        System.out.println("EventSub Parameters: ");
        System.out.println("\t fromBlk: " + fromBlk);
        System.out.println("\t toBlk: " + toBlk);
        System.out.println("\t toBlk: " + toBlk);
        System.out.println("\t addresses: " + addresses);
        System.out.println("\t topics: " + topics);

        EventSubscribe eventSubscribe = EventSubscribe.build(group, configOption);
        eventSubscribe.start();

        EventSubParams eventLogParams = new EventSubParams();
        eventLogParams.setFromBlock(fromBlk);
        eventLogParams.setToBlock(toBlk);
        for (int i = 0; i < addresses.size(); i++) {
            eventLogParams.addAddress(addresses.get(i));
        }

        for (int i = 0; i < topics.size(); i++) {
            if (topics.get(i) == null) {
                continue;
            }
            for (int j = 0; j < topics.get(i).size(); j++) {
                if (topics.get(i).get(j) == null) {
                    continue;
                }
                eventLogParams.addTopic(i, topics.get(i).get(j));
            }
        }

        eventSubscribe.subscribeEvent(
                eventLogParams,
                new EventSubCallback() {
                    @Override
                    public void onReceiveLog(String eventSubId, int status, List<EventLog> logs) {
                        System.out.println("event sub id: " + eventSubId);
                        System.out.println(" \t status: " + status);
                        System.out.println(" \t logs: " + logs);
                    }
                });
        System.in.read();
        System.exit(0);
    }
}
