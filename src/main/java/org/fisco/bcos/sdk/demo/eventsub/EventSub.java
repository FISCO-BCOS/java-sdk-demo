package org.fisco.bcos.sdk.demo.eventsub;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.sdk.config.Config;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.demo.amop.Subscribe;
import org.fisco.bcos.sdk.demo.perf.ParallelOkPerf;
import org.fisco.bcos.sdk.eventsub.EventSubCallback;
import org.fisco.bcos.sdk.eventsub.EventSubParams;
import org.fisco.bcos.sdk.eventsub.EventSubscribe;
import org.fisco.bcos.sdk.jni.common.JniException;
import org.fisco.bcos.sdk.model.ConstantConfig;
import org.fisco.bcos.sdk.model.EventLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventSub {

    private static final Logger logger = LoggerFactory.getLogger(Subscribe.class);

    public static void usage() {
        System.out.println("\tUsage: ");
        System.out.println(
                "\t\tjava -cp \"conf/:lib/*:apps/*\"  org.fisco.bcos.sdk.demo.eventsub.EventSub group fromBlock toBlock addr1#addr2#addr3 topics<Optional>");
        System.out.println("\tExample:");
        System.out.println(
                "\t\tjava -cp \"conf/:lib/*:apps/*\"  org.fisco.bcos.sdk.demo.eventsub.EventSub group -1 -1 0x37a44585Bf1e9618FDb4C62c4c96189A07Dd4b48");
        System.out.println(
                "\t\tjava -cp \"conf/:lib/*:apps/*\"  org.fisco.bcos.sdk.demo.eventsub.EventSub group -1 -1 0x37a44585Bf1e9618FDb4C62c4c96189A07Dd4b48");
        System.exit(0);
    }

    public static void main(String[] args) throws ConfigException, JniException {

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

        System.out.println("EventSub Parameters: ");
        System.out.println("\t fromBlk: " + fromBlk);
        System.out.println("\t toBlk: " + toBlk);
        System.out.println("\t toBlk: " + toBlk);
        System.out.println("\t addresses: " + addresses);

        EventSubscribe eventSubscribe = EventSubscribe.build(group, configOption);
        eventSubscribe.start();

        EventSubParams eventLogParams = new EventSubParams();
        eventLogParams.setFromBlock(fromBlk);
        eventLogParams.setToBlock(toBlk);
        for (int i = 0; i < addresses.size(); i++) {
            eventLogParams.addAddress(addresses.get(i));
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
    }
}
