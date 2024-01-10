package org.fisco.bcos.sdk.demo.contractTest;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import org.fisco.bcos.sdk.demo.contract.Economic;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

public class EconomicTest {
    private static Client client;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== Economic.sol test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.contractTest.EconomicTest [groupId] [committeeAddr].");
    }

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl =
                    EconomicTest.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                throw new IOException("The configFile " + configFileName + " doesn't exist!");
            }

            if (args.length < 2) {
                Usage();
                return;
            }
            String groupId = args[0];
            String committeePath = args[1];

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            BlockNumber blockNumber = client.getBlockNumber();
            System.out.println("Current BlockNumber : " + blockNumber.getBlockNumber());

            CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
            cryptoSuite.loadAccount("pem", committeePath, "");
            CryptoKeyPair committee = cryptoSuite.getCryptoKeyPair();
            // CryptoKeyPair deployOwner = client.getCryptoSuite().getCryptoKeyPair();
            CryptoKeyPair charger1 = cryptoSuite.generateRandomKeyPair();
            CryptoKeyPair charger2 = cryptoSuite.generateRandomKeyPair();
            CryptoKeyPair user1 = cryptoSuite.generateRandomKeyPair();
            CryptoKeyPair user2 = cryptoSuite.generateRandomKeyPair();

            // System.out.println("Account: " + deployOwner.getAddress() + " deploy Economic Contract.");
            System.out.println("Account: " + committee.getAddress() + " deploy Economic Contract.");
            client.getCryptoSuite().setCryptoKeyPair(committee);
            System.out.println("Deploy contracts...");
            // Economic ecoIns = Economic.deploy(client, deployOwner);
            Economic ecoIns = Economic.deploy(client, committee);
            System.out.println("Deploy contract finish.");
            System.out.println("Contract Address: " + ecoIns.getContractAddress());
            System.out.println("---------------------------------------");

            BigInteger committeeBalance = ecoIns.getBalance(committee.getAddress());
            System.out.println("committee balance: " + committeeBalance);

            System.out.println("Committee: " + committee.getAddress() + " to register economic contract.");
            // client.getCryptoSuite().setCryptoKeyPair(committee);
            TransactionReceipt enbleReceipt = ecoIns.enable();
            System.out.println("Tx status: " + enbleReceipt.isStatusOK());
            System.out.println("TX hash: " + enbleReceipt.getTransactionHash());
            System.out.println("---------------------------------------");

            System.out.println("Current granted chargers info:");
            List<String> chargerLists1 = ecoIns.listChargers();
            System.out.println("Total granted count : " + chargerLists1.size());
            if(chargerLists1.size() != 0) {
                System.out.println("granted chargers list:");
                chargerLists1.forEach(System.out::println);
            }

            System.out.println("---------------------------------------");
            System.out.println("Grant committee:" + committee.getAddress());
            // client.getCryptoSuite().setCryptoKeyPair(deployOwner);
            TransactionReceipt grantCommitteeReceipt = ecoIns.grantCharger(committee.getAddress());
            System.out.println("Tx status: " + grantCommitteeReceipt.isStatusOK());
            System.out.println("TX hash: " + grantCommitteeReceipt.getTransactionHash());
            System.out.println("---------------------------------------");

            System.out.println("Grant charger1:" + charger1.getAddress());
            // client.getCryptoSuite().setCryptoKeyPair(deployOwner);
            TransactionReceipt grantReceipt = ecoIns.grantCharger(charger1.getAddress());
            System.out.println("Tx status: " + grantReceipt.isStatusOK());
            System.out.println("TX hash: " + grantReceipt.getTransactionHash());
            System.out.println("---------------------------------------");

            System.out.println("Grant charger2:" + charger2.getAddress());
            TransactionReceipt grantReceipt1 = ecoIns.grantCharger(charger2.getAddress());
            System.out.println("Tx status: " + grantReceipt1.isStatusOK());
            System.out.println("TX hash: " + grantReceipt1.getTransactionHash());
            System.out.println("---------------------------------------");

            System.out.println("Current granted charger:");
            List<String> chargerLists2 = ecoIns.listChargers();
            System.out.println("Total granted count : " + chargerLists2.size());
            System.out.println("granted chargers list:");
            chargerLists2.forEach(System.out::println);
            System.out.println("---------------------------------------");

            System.out.println("Committee charge 888888 gas for charger1:" + charger1.getAddress());
            TransactionReceipt addBalance = ecoIns.charge(charger1.getAddress(), BigInteger.valueOf(888888));
            System.out.println("Tx status: " + addBalance.isStatusOK());
            System.out.println("TX hash: " + addBalance.getTransactionHash());
            BigInteger charger1Balance = ecoIns.getBalance(charger1.getAddress());
            System.out.println("charger1 balance: " + charger1Balance);
            System.out.println("---------------------------------------");

            BigInteger user1Balance = ecoIns.getBalance(user1.getAddress());
            System.out.println("user1 balance: " + user1Balance);
            System.out.println("Use charger :" + charger1.getAddress() + 
                                        " to charge user1:" + user1.getAddress());
            client.getCryptoSuite().setCryptoKeyPair(charger1);
            Economic ecoInsLoad = Economic.load(ecoIns.getContractAddress(), client, charger1);
            TransactionReceipt chargeReceipt = ecoInsLoad.charge(user1.getAddress(), BigInteger.valueOf(123));
            System.out.println("Tx status: " + chargeReceipt.isStatusOK());
            System.out.println("TX hash: " + chargeReceipt.getTransactionHash());

            user1Balance = ecoInsLoad.getBalance(user1.getAddress());
            System.out.println("user1 balance: " + user1Balance);
            System.out.println("---------------------------------------");

            System.out.println("use non granted user: user2(" + user2.getAddress() + 
                                                                        ") to charge user1");
            
            client.getCryptoSuite().setCryptoKeyPair(user2);
            Economic ecoInsLoad2 = Economic.load(ecoIns.getContractAddress(), client, user2);
            TransactionReceipt chargeReceipt2 = ecoInsLoad2.charge(user1.getAddress(), BigInteger.valueOf(123));
            System.out.println("Tx status: " + chargeReceipt2.isStatusOK());
            System.out.println("TX hash: " + chargeReceipt2.getTransactionHash());
            System.out.println("---------------------------------------");

            blockNumber = client.getBlockNumber();
            System.out.println("Current BlockNumber : " + blockNumber.getBlockNumber());

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
