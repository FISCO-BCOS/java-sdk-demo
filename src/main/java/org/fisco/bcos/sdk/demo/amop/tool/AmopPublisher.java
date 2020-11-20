package org.fisco.bcos.sdk.demo.amop.tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.amop.Amop;
import org.fisco.bcos.sdk.amop.AmopMsgOut;
import org.fisco.bcos.sdk.amop.topic.TopicType;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.Peers;

public class AmopPublisher {
    private static final int parameterNum = 4;
    private static String publisherFile =
            AmopPublisher.class
                    .getClassLoader()
                    .getResource("amop/config-publisher-for-test.toml")
                    .getPath();

    /**
     * @param args topicName, isBroadcast, Content(Content you want to send out), Count(how many msg
     *     you want to send out)
     * @throws Exception AMOP publish exceptioned
     */
    public static void main(String[] args) throws Exception {
        if (args.length < parameterNum) {
            System.out.println("param: target topic total number of request");
            return;
        }
        String topicName = args[0];
        Boolean isBroadcast = Boolean.valueOf(args[1]);
        String content = args[2];
        Integer count = Integer.parseInt(args[3]);
        BcosSDK sdk = BcosSDK.build(publisherFile);
        Amop amop = sdk.getAmop();

        System.out.println("3s ...");
        Thread.sleep(1000);
        System.out.println("2s ...");
        Thread.sleep(1000);
        System.out.println("1s ...");
        Thread.sleep(1000);

        if (!subscribed(sdk, topicName)) {
            System.out.println("No subscriber, exist.");
        }

        System.out.println("start test");
        System.out.println("===================================================================");

        for (Integer i = 0; i < count; ++i) {
            Thread.sleep(2000);
            AmopMsgOut out = new AmopMsgOut();
            out.setType(TopicType.NORMAL_TOPIC);
            out.setContent(content.getBytes());
            out.setTimeout(6000);
            out.setTopic(topicName);
            DemoAmopResponseCallback cb = new DemoAmopResponseCallback();
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (isBroadcast) {
                amop.broadcastAmopMsg(out);
                System.out.println(
                        "Step 1: Send out msg by broadcast,  time: "
                                + df.format(LocalDateTime.now())
                                + " topic:"
                                + out.getTopic()
                                + " content:"
                                + new String(out.getContent()));
            } else {
                amop.sendAmopMsg(out, cb);
                System.out.println(
                        "Step 1: Send out msg,  time: "
                                + df.format(LocalDateTime.now())
                                + " topic:"
                                + out.getTopic()
                                + " content:"
                                + new String(out.getContent()));
            }
        }
    }

    public static boolean subscribed(BcosSDK sdk, String topicName) throws InterruptedException {
        Client client = sdk.getClient(Integer.valueOf(1));
        Boolean hasSubscriber = false;
        Peers peers = client.getPeers();
        for (int i = 0; i < 10; i++) {
            for (Peers.PeerInfo info : peers.getPeers()) {
                for (String tp : info.getTopic()) {
                    if (tp.equals(topicName)) {
                        hasSubscriber = true;
                        return hasSubscriber;
                    }
                }
            }
            if (!hasSubscriber) {
                Thread.sleep(2000);
            }
        }
        return false;
    }
}
