package org.fisco.bcos.sdk.demo.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.sdk.jni.common.JniException;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.codec.decode.TransactionDecoderInterface;
import org.fisco.bcos.sdk.v3.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.v3.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.v3.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.v3.transaction.model.exception.NoSuchTransactionFileException;
import org.fisco.bcos.sdk.v3.transaction.model.exception.TransactionBaseException;
import org.fisco.bcos.sdk.v3.transaction.model.exception.TransactionException;

// 演示用外部签名接口实现交易签名和发送，回调。
public class LegoTransactionDemo {

    LegoTransaction lt = new LegoTransaction();

    // String sampleContractAddress = "0x31231c2abad03b071b3440268f28194029dad743";
    // String sampleContractAddress = "0x3b009b961fe57549a81c3eed5aa13550ee6bcecf";
    String sampleContractAddress = "0xac7562491ea6791fedf36a4c3ba54344bd266ad6";

    public BasicAbiTransaction makeHelloWorldSetTest() throws NoSuchTransactionFileException {
        BasicAbiTransaction abiTx = new BasicAbiTransaction();
        String contractName = "HelloWorld";
        String abiContent = lt.bcosClientWrapper.contractLoader.getABIByContractName(contractName);
        List<Object> params = new ArrayList<Object>();
        params.add("my test:" + System.currentTimeMillis() % 1000);
        System.out.println("test input param:" + params.toString());
        abiTx.setContractName(contractName)
                .setAbiContent(abiContent)
                .setMethodName("set")
                .setParams(params)
                .setTo(sampleContractAddress)
                .setTools(lt.bcosClientWrapper.getTxCryptoSuite(), null, null);
        return abiTx;
    }

    public BasicAbiTransaction makeHelloWorldDeployTest() throws NoSuchTransactionFileException {
        BasicAbiTransaction abiTx = new BasicAbiTransaction();
        String contractName = "HelloWorld";
        String abiContent = lt.bcosClientWrapper.contractLoader.getABIByContractName(contractName);
        String contractBin =
                lt.bcosClientWrapper.contractLoader.getBinaryByContractName(contractName);
        List<Object> params = new ArrayList<Object>();
        abiTx.setContractName(contractName)
                .setAbiContent(abiContent)
                .setParams(params)
                .setBinContent(contractBin)
                .setDeployTransaction(true)
                .setTools(lt.bcosClientWrapper.getTxCryptoSuite(), null, null);
        return abiTx;
    }

    // 同步部署演示
    public void testDeploy()
            throws NoSuchTransactionFileException, JsonProcessingException, ContractCodecException,
                    TransactionException, IOException, JniException {
        BasicAbiTransaction abiTx = makeHelloWorldDeployTest();
        ISignTransaction signTxImpl =
                new KeyToolSignTransaction(lt.bcosClientWrapper.getTxCryptoSuite());
        TransactionResponse response =
                lt.sendTransactionAndGetResponse("test_chain", abiTx, signTxImpl);
        System.out.println("deploy result : " + response.getContractAddress());
        sampleContractAddress = response.getContractAddress();
    }

    // 同步交易演示
    public TransactionResponse testTx()
            throws NoSuchTransactionFileException, JsonProcessingException, ContractCodecException,
                    TransactionException, IOException, JniException {
        // 构建一个调用接口的测试交易
        BasicAbiTransaction abiTx = makeHelloWorldSetTest();
        // 实例化一个签名服务的实现
        ISignTransaction signTxImpl =
                new KeyToolSignTransaction(lt.bcosClientWrapper.getTxCryptoSuite());
        // 发送后同步得到结果
        TransactionResponse response =
                lt.sendTransactionAndGetResponse("test_chain", abiTx, signTxImpl);
        return response;
    }

    // 异步交易演示
    public void testTxAsyn()
            throws NoSuchTransactionFileException, ContractCodecException, JsonProcessingException,
                    TransactionException, IOException, JniException {
        // 构建一个调用接口的测试交易
        BasicAbiTransaction abiTx = makeHelloWorldSetTest();
        // 实例化一个签名服务的实现
        ISignTransaction signTxImpl =
                new KeyToolSignTransaction(lt.bcosClientWrapper.getTxCryptoSuite());
        // 用异步方式发送
        lt.sendTransactionAsync("test_chain", abiTx, signTxImpl, new TestCallback(abiTx));
    }

    // 异步交易回调
    class TestCallback extends TransactionCallback {
        BasicAbiTransaction abiTx;

        public TestCallback(BasicAbiTransaction abiTx_) {
            abiTx = abiTx_;
        }

        @Override
        public void onResponse(TransactionReceipt receipt) {

            TransactionDecoderInterface transactionDecoder =
                    new TransactionDecoderService(
                            lt.bcosClientWrapper.getClient().getCryptoSuite(),
                            lt.bcosClientWrapper.getClient().isWASM());
            TransactionResponse response;
            try {
                response =
                        transactionDecoder.decodeReceiptWithValues(
                                abiTx.abiContent, abiTx.methodName, receipt);
                System.out.println("5:[in TxCallback]response decode: " + response.getEvents());
            } catch (ContractCodecException e) {
                e.printStackTrace();
            }
        }
    }

    // 同步调用call，call无需签名，直接用transactionProcessor封装的接口即可
    public CallResponse testCall() throws ContractCodecException, TransactionBaseException {
        int chainId = 1;
        BasicAbiTransaction abiTx =
                new BasicAbiTransaction(
                        "HelloWorld", "", "get", sampleContractAddress, new ArrayList<>());
        CallResponse callResponse =
                lt.bcosClientWrapper.transactionProcessor.sendCallByContractLoader(
                        abiTx.getContractName(),
                        abiTx.getTo(),
                        abiTx.getMethodName(),
                        abiTx.getParams());
        System.out.println("on call result:" + callResponse.getValues());
        return callResponse;
    }

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub

        LegoTransactionDemo ltd = new LegoTransactionDemo();
        ltd.lt.init();
        // lt.testTxAsyn();
        // Thread.sleep(2000);
        // lt.testCall();
        // lt.testTx();
        // lt.testCall();
        ltd.testDeploy();
        ltd.testCall();
        ltd.lt.bcosClientWrapper.finish();
    }
}
