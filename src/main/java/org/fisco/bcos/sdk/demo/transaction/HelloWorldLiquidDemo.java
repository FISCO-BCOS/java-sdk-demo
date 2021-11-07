package org.fisco.bcos.sdk.demo.transaction;

import java.net.URL;
import java.util.Random;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.demo.contract.HelloWorldLiquid;
import org.fisco.bcos.sdk.demo.perf.ParallelLiquidPerf;
import org.fisco.bcos.sdk.model.ConstantConfig;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.network.NetworkException;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

public class HelloWorldLiquidDemo {
    private static Client client;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== HelloWorldLiquidDemo =====");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.transaction.HelloWorldLiquidDemo [group] [get].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.transaction.HelloWorldLiquidDemo [group] [set] [something].");
    }

    public static void main(String[] args) throws NetworkException, ContractException {
        String configFileName = ConstantConfig.CONFIG_FILE_NAME;
        URL configUrl = ParallelLiquidPerf.class.getClassLoader().getResource(configFileName);
        if (configUrl == null) {
            System.out.println("The configFile " + configFileName + " doesn't exist!");
            return;
        }
        if (args.length < 3) {
            Usage();
            return;
        }
        String groupId = args[1];
        String method = args[2];
        String configFile = configUrl.getPath();
        BcosSDK sdk = BcosSDK.build(configFile);
        client = sdk.getClient(groupId);

        String helloPath = "hello" + new Random().nextInt(1000);
        HelloWorldLiquid helloWorldLiquid =
                HelloWorldLiquid.deploy(
                        client, client.getCryptoSuite().getCryptoKeyPair(), helloPath);

        if (method.equals(HelloWorldLiquid.FUNC_GET)) {
            System.out.println(helloWorldLiquid.get());
        }
        if (method.equals(HelloWorldLiquid.FUNC_SET) && args.length == 4) {
            String value = args[3];
            helloWorldLiquid.set(
                    value,
                    new TransactionCallback() {
                        @Override
                        public void onResponse(TransactionReceipt receipt) {
                            if (receipt.getStatus() == 0) {
                                System.out.println("Set success!");
                            } else {
                                System.out.println("Set failed.");
                            }
                        }
                    });
        } else {
            Usage();
        }
    }
}
