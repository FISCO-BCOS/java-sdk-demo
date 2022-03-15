package org.fisco.bcos.sdk.demo.amop;

import java.net.URL;
import org.fisco.bcos.sdk.demo.perf.ParallelOkPerf;
import org.fisco.bcos.sdk.jni.common.JniException;
import org.fisco.bcos.sdk.v3.amop.Amop;
import org.fisco.bcos.sdk.v3.config.Config;
import org.fisco.bcos.sdk.v3.config.ConfigOption;
import org.fisco.bcos.sdk.v3.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Subscribe {

    private static final Logger logger = LoggerFactory.getLogger(Subscribe.class);

    public static void usage() {
        System.out.println("\tUsage: ");
        System.out.println(
                "\t\tjava -cp \"conf/:lib/*:apps/*\"  org.fisco.bcos.sdk.demo.amop.Subscribe topic");
        System.out.println("\tExample:");
        System.out.println(
                "\t\tjava -cp \"conf/:lib/*:apps/*\"  org.fisco.bcos.sdk.demo.amop.Subscribe topic");
        System.exit(0);
    }

    public static void main(String[] args)
            throws InterruptedException, JniException, ConfigException {

        String configFileName = ConstantConfig.CONFIG_FILE_NAME;
        URL configUrl = ParallelOkPerf.class.getClassLoader().getResource(configFileName);
        if (configUrl == null) {
            System.out.println("The configFile " + configFileName + " doesn't exist!");
            return;
        }

        if (args.length < 1) {
            usage();
        }

        String topic = args[0];

        System.out.println(" ====== AMOP subscribe, topic: " + topic);

        String configFile = configUrl.getPath();
        ConfigOption configOption = Config.load(configFile);

        Amop amop = Amop.build(configOption);
        amop.start();

        amop.subscribeTopic(
                topic,
                (endpoint, seq, data) -> {
                    System.out.println(" ==> receive message from client");
                    System.out.println(" \t==> endpoint: " + endpoint);
                    System.out.println(" \t==> seq: " + seq);
                    System.out.println(" \t==> data: " + new String(data));

                    amop.sendResponse(endpoint, seq, data);
                });

        while (true) {

            Thread.sleep(10000);
        }
    }
}
