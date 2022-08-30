package org.fisco.bcos.sdk.demo.amop;

import java.net.URL;
import org.fisco.bcos.sdk.demo.perf.ParallelOkPerf;
import org.fisco.bcos.sdk.jni.amop.AmopResponseCallback;
import org.fisco.bcos.sdk.jni.common.JniException;
import org.fisco.bcos.sdk.jni.common.Response;
import org.fisco.bcos.sdk.v3.amop.Amop;
import org.fisco.bcos.sdk.v3.config.Config;
import org.fisco.bcos.sdk.v3.config.ConfigOption;
import org.fisco.bcos.sdk.v3.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Publish {
    private static final Logger logger = LoggerFactory.getLogger(Publish.class);

    public static void usage() {
        System.out.println("\tUsage: ");
        System.out.println(
                "\t\tjava -cp \"conf/:lib/*:apps/*\"  org.fisco.bcos.sdk.demo.amop.Publish topic msg");
        System.out.println("\tExample:");
        System.out.println(
                "\t\tjava -cp \"conf/:lib/*:apps/*\"  org.fisco.bcos.sdk.demo.amop.Publish topic HelloWorld");
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

        if (args.length < 2) {
            usage();
        }

        String topic = args[0];
        String msg = args[1];

        System.out.println(" ====== AMOP publish, topic: " + topic + " ,msg: " + msg);

        String configFile = configUrl.getPath();
        ConfigOption configOption = Config.load(configFile);

        org.fisco.bcos.sdk.v3.amop.Amop amop = Amop.build(configOption);
        amop.start();

        while (true) {
            System.out.println(" ====== AMOP publish send message");
            amop.sendAmopMsg(
                    topic,
                    msg.getBytes(),
                    0,
                    new AmopResponseCallback() {
                        @Override
                        public void onResponse(Response response) {
                            System.out.println(" ==> receive response message from server");
                            if (response.getErrorCode() == 0) {
                                System.out.println(
                                        " \t responseData: " + new String(response.getData()));
                            } else {
                                System.out.println(" \t errorCode: " + response.getErrorCode());
                                System.out.println(
                                        " \t errorMessage: " + response.getErrorMessage());
                            }
                        }
                    });

            Thread.sleep(5000);
        }
    }
}
