package org.fisco.bcos.sdk.demo.transaction;

import java.net.URL;
import org.fisco.bcos.sdk.demo.contract.HelloWorldLiquid;
import org.fisco.bcos.sdk.demo.perf.ParallelLiquidPerf;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

public class HelloWorldLiquidDemo {
    private static Client client;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== HelloWorldLiquidDemo =====");
        System.out.println("Note: you SHOULD deploy first.");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.transaction.HelloWorldLiquidDemo [group] [path] [deploy].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.transaction.HelloWorldLiquidDemo [group] [path] [get].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.transaction.HelloWorldLiquidDemo [group] [path] [set] [something].");
    }

    public static void main(String[] args) throws ContractException {
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
        String groupId = args[0];
        String path = args[1];
        String method = args[2];
        String configFile = configUrl.getPath();
        BcosSDK sdk = BcosSDK.build(configFile);
        client = sdk.getClient(groupId);

        if (method.equals(HelloWorldLiquid.FUNC_GET)) {
            HelloWorldLiquid helloWorldLiquid =
                    HelloWorldLiquid.load(path, client, client.getCryptoSuite().getCryptoKeyPair());
            System.out.println(helloWorldLiquid.get());
        } else if (method.equals("deploy")) {
            try {
                HelloWorldLiquid helloWorldLiquid =
                        HelloWorldLiquid.deploy(
                                client,
                                client.getCryptoSuite().getCryptoKeyPair(),
                                path,
                                "Hello World!");
                if (helloWorldLiquid != null
                        && helloWorldLiquid.getDeployReceipt() != null
                        && helloWorldLiquid.getDeployReceipt().getStatus() == 0) {
                    System.out.println("Deploy success!");
                } else {
                    System.out.println("Deploy failed!");
                    assert helloWorldLiquid != null;
                    System.out.println(
                            "Receipt status code: "
                                    + helloWorldLiquid.getDeployReceipt().getStatus());
                }
            } catch (ContractException e) {
                System.out.println(
                        "Deploy error, error code: {"
                                + e.getErrorCode()
                                + "}, error msg: {"
                                + e.getMessage()
                                + "}");
                System.exit(0);
            }
        } else if (method.equals(HelloWorldLiquid.FUNC_SET) && args.length == 4) {
            HelloWorldLiquid helloWorldLiquid =
                    HelloWorldLiquid.load(path, client, client.getCryptoSuite().getCryptoKeyPair());
            String value = args[3];
            TransactionReceipt receipt = helloWorldLiquid.set(value);
            if (receipt.getStatus() == 0) {
                System.out.println("Set success!");
            } else {
                System.out.println("Set failed.");
                System.out.println("Receipt status code: " + receipt.getStatus());
            }
        } else {
            Usage();
        }
        System.exit(0);
    }
}
