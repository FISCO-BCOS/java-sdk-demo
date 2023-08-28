package org.fisco.bcos.sdk.demo.transaction;

import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.v3.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.v3.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.v3.transaction.model.exception.NoSuchTransactionFileException;
import org.fisco.bcos.sdk.v3.transaction.model.exception.TransactionBaseException;

public class AssembleTransactionDemo {
    BcosClientWrapper bcosClientWrapper;

    public void init() throws Exception {
        bcosClientWrapper = new BcosClientWrapper();
        bcosClientWrapper.init("test_group");
    }

    String contractAddress = "0x31231c2abad03b071b3440268f28194029dad743";

    public void testDeploy() throws Exception {
        // 部署HelloWorld合约。第一个参数为合约名称，第二个参数为合约构造函数的列表，是List<Object>类型。
        TransactionResponse response =
                bcosClientWrapper.transactionProcessor.deployByContractLoader(
                        "HelloWorld", new ArrayList<>());
        System.out.println("deploy result:" + response.getContractAddress());
        contractAddress = response.getContractAddress();
    }

    public void testABIInfo() throws NoSuchTransactionFileException {
        String contractName = "HelloWorld";
        List<ABIDefinition> abilst =
                bcosClientWrapper
                        .transactionProcessor
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

    public void testCall() throws TransactionBaseException, ContractCodecException {
        // 查询HelloWorld合约的『name』函数，合约地址为helloWorldAddress，参数为空
        String contractName = "HelloWorld";
        // contractAddress = "0x6aa57232d12f4b516cafc4919219ed56f996613e";
        System.out.println("call address: " + contractAddress);
        CallResponse callResponse =
                bcosClientWrapper.transactionProcessor.sendCallByContractLoader(
                        contractName, contractAddress, "get", new ArrayList<>());

        System.out.println(callResponse.getValues());
    }

    public void testTx() throws ContractCodecException, TransactionBaseException {
        String contractName = "HelloWorld";
        String contractAddress = "0x31231c2abad03b071b3440268f28194029dad743";
        List<Object> params = new ArrayList<Object>();
        params.add("my test");
        TransactionResponse txResp =
                bcosClientWrapper.transactionProcessor
                        .sendTransactionAndGetResponseByContractLoader(
                                contractName, contractAddress, "set", params);
        System.out.println(txResp.getEvents());
        System.out.println(txResp.getReturnMessage());
        System.out.println(txResp.getReturnCode());

        testCall();
    }

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        AssembleTransactionDemo demo = new AssembleTransactionDemo();
        demo.init();
        demo.testDeploy();
        demo.testCall();
        demo.bcosClientWrapper.finish();
    }
}
