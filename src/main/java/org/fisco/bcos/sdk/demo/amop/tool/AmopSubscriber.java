package org.fisco.bcos.sdk.demo.amop.tool;

import java.net.URL;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.amop.Amop;
import org.fisco.bcos.sdk.amop.AmopCallback;

public class AmopSubscriber {

    public static void main(String[] args) throws Exception {
        URL configUrl =
                AmopSubscriber.class
                        .getClassLoader()
                        .getResource("amop/config-subscriber-for-test.toml");
        if (args.length < 1) {
            System.out.println("Param: topic");
            return;
        }
        String topic = args[0];
        // Construct a BcosSDK instance
        BcosSDK sdk = BcosSDK.build(configUrl.getPath());

        // Get the amop module instance
        Amop amop = sdk.getAmop();

        // Set callback
        AmopCallback cb = new DemoAmopCallback();
        // Set a default callback
        amop.setCallback(cb);
        // Subscriber a normal topic
        amop.subscribeTopic(topic, cb);
        System.out.println("Start test");
    }
}
