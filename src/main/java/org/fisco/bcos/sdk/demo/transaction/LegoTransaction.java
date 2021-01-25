package org.fisco.bcos.sdk.demo.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.transaction.model.exception.NoSuchTransactionFileException;
import org.fisco.bcos.sdk.transaction.model.exception.TransactionBaseException;
import org.fisco.bcos.sdk.transaction.model.exception.TransactionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LegoTransaction {

    BcosSDK bcosSDK;
    String abipath = "src/main/resources/abi/";
    String binpath = "src/main/resources/bin/";
    Client client;
    CryptoKeyPair keyPair;
    int groupId = 1;
    AssembleTransactionProcessor transactionProcessor;

    public void init() throws Exception {
        // 初始化BcosSDK对象
        ApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        bcosSDK = context.getBean(BcosSDK.class);
        client = bcosSDK.getClient(Integer.valueOf(groupId));
        // 构造AssembleTransactionProcessor对象，需要传入client对象，CryptoKeyPair对象和abi、binary文件存放的路径。abi和binary文件需要在上一步复制到定义的文件夹中。
        keyPair = client.getCryptoSuite().createKeyPair();
        transactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                        client, keyPair, abipath, binpath);
    }

    public void finish() {
        bcosSDK.stopAll();
    }

    public void testDeploy() throws Exception {

        AssembleTransactionProcessor transactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                        client, keyPair, abipath, binpath);
        // 部署HelloWorld合约。第一个参数为合约名称，第二个参数为合约构造函数的列表，是List<Object>类型。
        TransactionResponse response =
                transactionProcessor.deployByContractLoader("HelloWorld", new ArrayList<>());
        System.out.println(response.getContractAddress());
        // 0xc895b8786b6615b1438a5a371ee42c15ad0dfa67

    }

    public void testABIInfo() throws NoSuchTransactionFileException {
        String contractName = "HelloWorld";
        List<ABIDefinition> abilst =
                transactionProcessor
                        .getContractLoader()
                        .getFunctionABIListByContractName(contractName);
        for (ABIDefinition de : abilst) {
            System.out.println(
                    "name:"
                            + de.getName()
                            + ",type:"
                            + de.getType()
                            + ",mutability:"
                            + de.getStateMutability());
        }
    }

    public void testCall() throws TransactionBaseException, ABICodecException {
        // 查询HelloWorld合约的『name』函数，合约地址为helloWorldAddress，参数为空
        String contractName = "HelloWorld";
        String contractAddress = "0x31231c2abad03b071b3440268f28194029dad743";
        CallResponse callResponse =
                transactionProcessor.sendCallByContractLoader(
                        contractName, contractAddress, "get", new ArrayList<>());
        System.out.println(callResponse.getValues());
    }

    public void testTx() throws ABICodecException, TransactionBaseException {
        String contractName = "HelloWorld";
        String contractAddress = "0x31231c2abad03b071b3440268f28194029dad743";
        List<Object> params = new ArrayList<Object>();
        params.add("my test");
        TransactionResponse txResp =
                transactionProcessor.sendTransactionAndGetResponseByContractLoader(
                        contractName, contractAddress, "set", params);
        System.out.println(txResp.getEvents());
        System.out.println(txResp.getReturnMessage());
        System.out.println(txResp.getReturnCode());

        testCall();
    }

    public void testTxMaker()
            throws NoSuchTransactionFileException, ABICodecException, JsonProcessingException,
                    TransactionException, IOException {
        TransactionMaker maker = new TransactionMaker();
        maker.setClient(client);
        maker.setSDK(bcosSDK);
        String contractName = "HelloWorld";
        String abiContent =
                transactionProcessor.getContractLoader().getABIByContractName(contractName);
        String methodName = "set";
        String contractAddress = "0x31231c2abad03b071b3440268f28194029dad743";
        List<Object> params = new ArrayList<Object>();
        params.add("my test:" + System.currentTimeMillis() % 1000);
        System.out.println("test input param:" + params.toString());
        maker.makeAndSendSignedTransaction(abiContent, methodName, contractAddress, params);
    }

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub

        LegoTransaction lt = new LegoTransaction();
        lt.init();
        // lt.testTx();
        lt.testTxMaker();
        lt.testCall();
        lt.finish();
    }
}
