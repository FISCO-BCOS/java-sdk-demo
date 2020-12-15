package org.fisco.bcos.sdk.demo.amop.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.amop.Amop;
import org.fisco.bcos.sdk.amop.AmopCallback;
import org.fisco.bcos.sdk.amop.AmopMsgOut;
import org.fisco.bcos.sdk.amop.topic.TopicType;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.Peers;
import org.fisco.bcos.sdk.crypto.keypair.ECDSAKeyPair;

public class AmopSubscriberPrivateByKey {
    private static String subscriberConfigFile =
            AmopSubscriberPrivate.class
                    .getClassLoader()
                    .getResource("amop/config-subscriber-for-test.toml")
                    .getPath();

    private static String publisherFile =
            AmopPublisherPrivate.class
                    .getClassLoader()
                    .getResource("amop/config-publisher-for-test.toml")
                    .getPath();

    public static void Usage() {
        System.out.println(
                "java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.amop.tool.AmopSubscriberPrivateByKey generateKeyFile keyFileName");
        System.out.println(
                "java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.amop.tool.AmopSubscriberPrivateByKey subscribe topicName privateKeyFile");
        System.out.println(
                "java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.amop.tool.AmopSubscriberPrivateByKey publish [topicName] [isBroadcast: true/false] [sendedContent] [count] [publicKeyFile1] [publicKeyFile2] ...");
        System.out.println();
        System.exit(0);
    }
    /**
     * @param args topic, privateKeyFile, password(Option)
     * @throws Exception AMOP exceptioned
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            Usage();
        }
        String command = args[0];
        switch (command) {
            case "generateKeyFile":
                generateKeyFile(args);
                break;
            case "subscribe":
                subscribePrivateTopic(args);
                break;
            case "publish":
                publishPrivateTopics(args);
                break;
            default:
                System.out.println("Unknown command " + command);
                Usage();
        }
    }

    public static void generateKeyFile(String[] args) throws IOException {
        ECDSAKeyPair ecdsaKeyPair = new ECDSAKeyPair();
        String privateKey = ecdsaKeyPair.getHexPrivateKey();
        String publicKey = ecdsaKeyPair.getHexPublicKey();
        String fileName = args[1];
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
        out.write(privateKey);
        out.close();
        System.out.println("Generate And save privateKey Success: " + privateKey);

        String publicKeyFileName = fileName + ".pub";
        BufferedWriter publicKeyOut = new BufferedWriter(new FileWriter(publicKeyFileName));
        publicKeyOut.write(publicKey);
        publicKeyOut.close();

        System.out.println("Generate And save publicKey Success: " + publicKey);
    }

    public static void subscribePrivateTopic(String[] args) throws IOException {
        if (args.length < 3) {
            Usage();
        }
        String topic = args[1];
        String hexPrivateKey = readContent(args[2]);
        if (hexPrivateKey == null) {
            return;
        }
        BcosSDK sdk = BcosSDK.build(subscriberConfigFile);
        Amop amop = sdk.getAmop();
        AmopCallback cb = new DemoAmopCallback();

        System.out.println("Start test");
        amop.setCallback(cb);
        // Subscriber a private topic.
        amop.subscribePrivateTopics(topic, hexPrivateKey, cb);
    }

    public static String readContent(String filePath) throws IOException {
        File keyFile = new File(filePath);
        if (!keyFile.exists()) {
            System.out.println("The file " + filePath + " doesn't exist!");
            return null;
        }
        return FileUtils.readFileToString(keyFile);
    }

    public static void publishPrivateTopics(String[] args)
            throws InterruptedException, IOException {
        if (args.length < 6) {
            Usage();
        }
        String topicName = args[1];
        Boolean isBroadcast = Boolean.valueOf(args[2]);
        String content = args[3];
        Integer count = Integer.parseInt(args[4]);
        List<String> publicKeyList = new ArrayList<>();
        for (int i = 5; i < args.length; i++) {
            String publicKey = readContent(args[i]);
            if (publicKey == null) {
                System.out.println("public key file " + args[i] + " doesn't exist.");
                continue;
            }
            publicKeyList.add(publicKey);
            System.out.println("Load public key file " + args[i] + " success.");
        }
        if (publicKeyList.size() == 0) {
            System.out.println(
                    "The number of the public keys are 0, Please add at least one public key file");
            return;
        }
        BcosSDK sdk = BcosSDK.build(publisherFile);
        Amop amop = sdk.getAmop();
        if (!subscribed(sdk, topicName)) {
            System.out.println("No subscriber, exist.");
        }

        System.out.println("start test");
        System.out.println("===================================================================");
        System.out.println("set up private topic");
        // Publish a private topic
        amop.publishPrivateTopicWithHexPublicKeyList(topicName, publicKeyList);
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
