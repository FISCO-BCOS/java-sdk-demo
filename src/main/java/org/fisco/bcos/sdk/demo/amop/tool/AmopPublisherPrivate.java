package org.fisco.bcos.sdk.demo.amop.tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.amop.Amop;
import org.fisco.bcos.sdk.amop.AmopMsgOut;
import org.fisco.bcos.sdk.amop.topic.TopicType;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.Peers;
import org.fisco.bcos.sdk.crypto.keystore.KeyTool;
import org.fisco.bcos.sdk.crypto.keystore.PEMKeyStore;

public class AmopPublisherPrivate {
    private static final int parameterNum = 6;
    private static String publisherFile =
            AmopPublisherPrivate.class
                    .getClassLoader()
                    .getResource("amop/config-publisher-for-test.toml")
                    .getPath();

    /**
     * @param args topicName, pubKey1, pubKey2, isBroadcast: true/false, content, count. if only one
     *     public key please fill pubKey2 with null
     * @throws Exception AMOP exceptioned
     */
    public static void main(String[] args) throws Exception {
        if (args.length < parameterNum) {
            System.out.println(
                    "java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.amop.tool.AmopPublisherPrivate <topicName> <pubKey1> <pubKey2> <isBroadcast: true/false> <content> <count>");
            return;
        }
        String topicName = args[0];
        String pubkey1 = args[1];
        String pubkey2 = args[2];
        Boolean isBroadcast = Boolean.valueOf(args[3]);
        String content = args[4];
        Integer count = Integer.parseInt(args[5]);
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
        System.out.println("set up private topic");
        List<KeyTool> keyToolList = new ArrayList<>();

        // Read public key files.
        KeyTool keyTool = new PEMKeyStore(pubkey1);
        keyToolList.add(keyTool);
        if (!pubkey2.equals("null")) {
            KeyTool keyTool1 = new PEMKeyStore(pubkey2);
            keyToolList.add(keyTool1);
        }
        // Publish a private topic
        amop.publishPrivateTopic(topicName, keyToolList);
        System.out.println("wait until finish private topic verify");
        System.out.println("3s ...");
        Thread.sleep(1000);
        System.out.println("2s ...");
        Thread.sleep(1000);
        System.out.println("1s ...");
        Thread.sleep(1000);

        for (Integer i = 0; i < count; ++i) {
            Thread.sleep(2000);
            AmopMsgOut out = new AmopMsgOut();
            // It is a private topic.
            out.setType(TopicType.PRIVATE_TOPIC);
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
