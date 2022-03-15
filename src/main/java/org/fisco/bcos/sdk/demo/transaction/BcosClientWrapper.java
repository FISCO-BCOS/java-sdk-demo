package org.fisco.bcos.sdk.demo.transaction;

import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.v3.transaction.tools.ContractLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BcosClientWrapper {
    private BcosSDK bcosSDK;
    private String abipath = "bin/main/abi";
    private String binpath = "bin/main/bin";
    private Client client;
    private CryptoSuite txCryptoSuite;
    private String groupId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    AssembleTransactionProcessor transactionProcessor;
    ContractLoader contractLoader;

    public BcosSDK getBcosSDK() {
        return bcosSDK;
    }

    public void setBcosSDK(BcosSDK bcosSDK) {
        this.bcosSDK = bcosSDK;
    }

    public String getAbipath() {
        return abipath;
    }

    public void setAbipath(String abipath) {
        this.abipath = abipath;
    }

    public String getBinpath() {
        return binpath;
    }

    public void setBinpath(String binpath) {
        this.binpath = binpath;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public CryptoSuite getTxCryptoSuite() {
        return txCryptoSuite;
    }

    public void setTxCryptoSuite(CryptoSuite txCryptoSuite) {
        this.txCryptoSuite = txCryptoSuite;
    }

    public AssembleTransactionProcessor getTransactionProcessor() {
        return transactionProcessor;
    }

    public void setTransactionProcessor(AssembleTransactionProcessor transactionProcessor) {
        this.transactionProcessor = transactionProcessor;
    }

    public void init(String groupId) throws Exception {
        // 初始化BcosSDK对象
        // String realPath =BcosClientWrapper.class.getClassLoader().getResource("").getFile();
        // System.out.println("运行目录: " + realPath);
        ApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        bcosSDK = context.getBean(BcosSDK.class);
        client = bcosSDK.getClient(groupId);

        String eccPrivateKeySample =
                "28018238ac7eec853401dfc3f31133330e78ac27a2f53481270083abb1a126f9";
        txCryptoSuite = new CryptoSuite(client.getCryptoType());
        txCryptoSuite.loadKeyPair(eccPrivateKeySample);
        txCryptoSuite = client.getCryptoSuite();

        transactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                        client, txCryptoSuite.getCryptoKeyPair(), abipath, binpath);

        contractLoader = new ContractLoader(abipath, binpath);
    }

    public void finish() {
        bcosSDK.stopAll();
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }
}
