package org.fisco.bcos.sdk.demo.perf.transactionv1;

import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.demo.contract.ComplexCodecTest;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicArray;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicStruct;
import org.fisco.bcos.sdk.v3.codec.datatypes.StaticArray;
import org.fisco.bcos.sdk.v3.codec.datatypes.StaticStruct;
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int128;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int32;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint128;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIObject;
import org.fisco.bcos.sdk.v3.crypto.signature.SignatureResult;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.AssembleTransactionService;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.ProxySignTransactionManager;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.dto.DeployTransactionRequest;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.dto.DeployTransactionRequestWithStringParams;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.dto.TransactionRequest;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.dto.TransactionRequestWithStringParams;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.utils.TransactionRequestBuilder;
import org.fisco.bcos.sdk.v3.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.v3.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.v3.transaction.tools.ContractLoader;
import org.fisco.bcos.sdk.v3.transaction.tools.JsonUtils;
import org.fisco.bcos.sdk.v3.utils.Hex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransactionManagerTest {

    private static final String CONFIG_FILE =
            "src/integration-test/resources/" + ConstantConfig.CONFIG_FILE_NAME;
    private final String abi = ComplexCodecTest.getABI();
    private final String bin;

    private static final String COMPLEX_CODEC_TEST = "ComplexCodecTest";

    private final AssembleTransactionService transactionService;


    private final Client client;

    public boolean useProxySign = true;

    public TransactionManagerTest() throws IOException {
        // init the sdk, and set the config options.
        BcosSDK sdk = BcosSDK.build(CONFIG_FILE);
        // group
        client = sdk.getClient("group0");
        transactionService = new AssembleTransactionService(client);
        if (useProxySign) {
            ProxySignTransactionManager proxySignTransactionManager = new ProxySignTransactionManager(client, (hash, transactionSignCallback) -> {
                SignatureResult sign = client.getCryptoSuite().sign(hash, client.getCryptoSuite().getCryptoKeyPair());
                transactionSignCallback.handleSignedTransaction(sign);
            });
            transactionService.setTransactionManager(proxySignTransactionManager);
        }
        bin = ComplexCodecTest.getBinary(client.getCryptoSuite());
    }

    @Override
    protected void finalize() {
        try {
            super.finalize();
            client.stop();
            client.destroy();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void test1ComplexCodecWithType() throws Exception {
        if ((client.getNegotiatedProtocol() >> 16) < 2) {
            return;
        }
        // test deploy with struct
        List<Object> deployParams = new ArrayList<>();
        {
            DynamicArray<Utf8String> array = new DynamicArray<>(Utf8String.class, new Utf8String("test"));
            DynamicArray<Bytes32> bytes32DynamicArray = new DynamicArray<>(Bytes32.class, Bytes32.DEFAULT);
            DynamicStruct structA = new DynamicStruct(array, bytes32DynamicArray);
            deployParams.add(structA);
        }

        TransactionRequestBuilder builder = new TransactionRequestBuilder(abi, bin);
        DeployTransactionRequest request = builder.buildDeployRequest(deployParams);

        TransactionResponse response = transactionService.deployContract(request);

        assert (response.getTransactionReceipt().getStatus() == 0);
        String contractAddress = response.getContractAddress();
        assert (StringUtils.isNotBlank(response.getContractAddress()));

        // test call get struct
        {
            TransactionRequest callRequest = builder.setTo(contractAddress).setMethod("getStructA").buildRequest(new ArrayList<>());
            CallResponse callResponse = transactionService.sendCall(callRequest);

            List<Object> returnObject = callResponse.getReturnObject();
            assert (returnObject.size() == 1);
            assert (callResponse.getReturnABIObject().size() == 1);
            System.out.println(JsonUtils.toJson(returnObject));

            List<Object> callParams = new ArrayList<>();
            DynamicArray<Utf8String> array = new DynamicArray<>(Utf8String.class, new Utf8String("test3125643123"));
            DynamicArray<Bytes32> bytes32DynamicArray = new DynamicArray<>(Bytes32.class, Bytes32.DEFAULT);
            DynamicStruct structA = new DynamicStruct(array, bytes32DynamicArray);
            callParams.add(structA);


            TransactionRequest callRequest2 = builder.setMethod("getStructA").buildRequest(callParams);
            CallResponse callResponse1 = transactionService.sendCall(callRequest2);

            returnObject = callResponse1.getReturnObject();
            assert (returnObject.size() == 1);
            assert (callResponse1.getReturnABIObject().size() == 1);
            System.out.println("getStructA:");
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test bytes[][] set and get
        {
            List<Object> params = new ArrayList<>();
            DynamicBytes b = new DynamicBytes("1234".getBytes());
            DynamicArray<DynamicBytes> bs = new DynamicArray<>(DynamicBytes.class, b);
            DynamicArray<DynamicArray<DynamicBytes>> bbs = new DynamicArray<>(bs);
            params.add(bbs);

            TransactionRequest transactionRequest = builder.setMethod("setBytesArrayArray").buildRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequest);
            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("setBytesArrayArray, bytes[][]");
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test bytes32[][] set and get
        {
            List<Object> params = new ArrayList<>();
            Bytes32 b = new Bytes32(Hex.decode("ffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff"));
            DynamicArray<Bytes32> bs = new DynamicArray<>(Bytes32.class, b);
            DynamicArray<DynamicArray<Bytes32>> bbs = new DynamicArray<>(bs);
            params.add(bbs);

            TransactionRequest transactionRequest = builder.setMethod("setBytes32ArrayArray").buildRequest(params);

            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequest);

            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("setBytes32ArrayArray, bytes32[][]");
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test bytes[2][] set and get
        {
            List<Object> params = new ArrayList<>();
            DynamicBytes b1 = new DynamicBytes(Hex.decode("ffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff"));
            DynamicBytes b2 = DynamicBytes.DEFAULT;
            DynamicArray<DynamicBytes> bs = new DynamicArray<>(DynamicBytes.class, b1, b2);
            DynamicArray<DynamicArray<DynamicBytes>> bbs = new DynamicArray<>(bs);
            params.add(bbs);

            TransactionRequest transactionRequest = builder.setMethod("setBytesStaticArrayArray").buildRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequest);

            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("setBytesStaticArrayArray, bytes[2][]");
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test bytes32[2][] set and get
        {
            List<Object> params = new ArrayList<>();
            Bytes32 b1 = new Bytes32(Hex.decode("ffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff"));
            Bytes32 b2 = Bytes32.DEFAULT;
            StaticArray<Bytes32> bs = new StaticArray<>(Bytes32.class, b1, b2);
            DynamicArray<StaticArray<Bytes32>> bbs = new DynamicArray<>(bs);
            params.add(bbs);

            TransactionRequest transactionRequest = builder.setMethod("setBytes32StaticArrayArray").buildRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequest);

            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("setBytes32StaticArrayArray, bytes32[2][]");
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test struct set and get
        {
            List<Object> params = new ArrayList<>();
            DynamicArray<Utf8String> array = new DynamicArray<>(Utf8String.class, new Utf8String("test2132131"));
            DynamicArray<Bytes32> bytes32DynamicArray = new DynamicArray<>(Bytes32.class, Bytes32.DEFAULT);
            DynamicStruct structA = new DynamicStruct(array, bytes32DynamicArray);
            params.add(structA);

            TransactionRequest transactionRequest = builder.setMethod("buildStructB").buildRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequest);

            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 2);
            assert (returnABIObject.size() == 2);
            System.out.println("buildStructB, StructB, StructA[]");
            System.out.println(JsonUtils.toJsonWithException(returnObject));
        }

        // test static struct set and get
        {
            List<Object> params = new ArrayList<>();
            StaticArray<Int32> staticArray = new StaticArray<>(Int32.class, 1, new Int32(1));
            Int128 int128 = new Int128(128);
            Uint128 uint128 = new Uint128(127);
            StaticStruct struct = new StaticStruct(int128, uint128, staticArray);
            params.add(struct);

            // use static struct params, get single struct
            TransactionRequest transactionRequest = builder.setMethod("buildStaticStruct").buildRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequest);
            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("buildStaticStruct, staticStruct");
            System.out.println(JsonUtils.toJsonWithException(returnObject));

            // use number params, get static struct list
            List<Object> params2 = new ArrayList<>();
            params2.add(new Int128(256));
            params2.add(new Uint128(288));
            TransactionRequest transactionRequest1 = builder.setMethod("buildStaticStruct").buildRequest(params2);
            TransactionResponse transactionResponse1 = transactionService.sendTransaction(transactionRequest1);
            assert (transactionResponse1.getTransactionReceipt().getStatus() == 0);
            returnObject = transactionResponse1.getReturnObject();
            returnABIObject = transactionResponse1.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("buildStaticStruct, staticStruct[]");
            System.out.println(JsonUtils.toJsonWithException(returnObject));
        }
    }

    public void test2ComplexCodecWithStringParams() throws Exception {
        if ((client.getNegotiatedProtocol() >> 16) < 2) {
            return;
        }
        // test deploy with struct
        List<String> deployParams = new ArrayList<>();

        deployParams.add("[[\"test\"],[\"ffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff\"]]");
        TransactionRequestBuilder requestBuilder = new TransactionRequestBuilder(abi, bin);
        DeployTransactionRequestWithStringParams deployTransactionRequestWithStringParams = requestBuilder.buildDeployStringParamsRequest(deployParams);
        TransactionResponse response = transactionService.deployContract(deployTransactionRequestWithStringParams);
        assert (response.getTransactionReceipt().getStatus() == 0);
        String contractAddress = response.getContractAddress();
        assert (StringUtils.isNotBlank(response.getContractAddress()));

        // test call send struct get struct
        {
            List<String> callParams = new ArrayList<>();
            // use no params method
            TransactionRequestWithStringParams request = requestBuilder.setMethod("getStructA").setTo(contractAddress).buildStringParamsRequest(callParams);
            CallResponse callResponse = transactionService.sendCall(request);
            List<Object> returnObject = callResponse.getReturnObject();
            assert (returnObject.size() == 1);
            assert (callResponse.getReturnABIObject().size() == 1);
            System.out.println(JsonUtils.toJson(returnObject));

            // use one params method
            callParams.add("[[\"test2312312312312\"],[\"ffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff\"]]");
            TransactionRequestWithStringParams request2 = requestBuilder.setMethod("getStructA").setTo(contractAddress).buildStringParamsRequest(callParams);
            CallResponse callResponse2 = transactionService.sendCall(request2);
            returnObject = callResponse2.getReturnObject();
            assert (returnObject.size() == 1);
            assert (callResponse2.getReturnABIObject().size() == 1);
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test bytes[][] set and get
        {
            List<String> params = new ArrayList<>();
            params.add("[[\"0xabcd\"],[\"0x1234\"]]");
            TransactionRequestWithStringParams transactionRequestWithStringParams = requestBuilder.setMethod("setBytesArrayArray").setTo(contractAddress).buildStringParamsRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequestWithStringParams);
            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
        }

        // test bytes32[][] set and get
        {
            List<String> params = new ArrayList<>();
            params.add("[[\"0xffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff\",\"0xffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff\"],[\"0xffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff\"]]");
            TransactionRequestWithStringParams transactionRequestWithStringParams = requestBuilder.setMethod("setBytes32ArrayArray").buildStringParamsRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequestWithStringParams);
            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("setBytes32ArrayArray, bytes32[][]");
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test bytes[2][] set and get
        {
            List<String> params = new ArrayList<>();
            params.add("[[\"0xabcdef\",\"0xffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff\"],[\"0xffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff\",\"0x1234\"]]");
            TransactionRequestWithStringParams transactionRequestWithStringParams = requestBuilder.setMethod("setBytesStaticArrayArray").buildStringParamsRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequestWithStringParams);
            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("setBytesStaticArrayArray, bytes[2][]");
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test bytes32[2][] set and get
        {
            List<String> params = new ArrayList<>();
            params.add("[[\"0x1234567890123456789012345678901234567890123456789012345678901234\",\"0xffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff\"],[\"0x1234567890123456789012345678901234567890123456789012345678901234\",\"0xffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff\"],[\"0xffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff\",\"0x1234567890123456789012345678901234567890123456789012345678901234\"]]");
            TransactionRequestWithStringParams transactionRequestWithStringParams = requestBuilder.setMethod("setBytes32StaticArrayArray").buildStringParamsRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequestWithStringParams);
            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("setBytes32StaticArrayArray, bytes32[2][]");
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test struct set and get
        {
            List<String> params = new ArrayList<>();
            params.add("[[\"12312314565456345test\"],[\"ffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff\"]]");
            TransactionRequestWithStringParams transactionRequestWithStringParams = requestBuilder.setMethod("buildStructB").buildStringParamsRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequestWithStringParams);

            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 2);
            assert (returnABIObject.size() == 2);
            System.out.println("buildStructB, StructB, StructA[]");
            System.out.println(JsonUtils.toJsonWithException(returnObject));
        }

        // test static struct set and get
        {
            List<String> params = new ArrayList<>();
            params.add("[-128,129,[32]]");
            // use static struct params, get single struct
            TransactionRequestWithStringParams transactionRequestWithStringParams = requestBuilder.setMethod("buildStaticStruct").buildStringParamsRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequestWithStringParams);

            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("buildStaticStruct, staticStruct");
            System.out.println(JsonUtils.toJsonWithException(returnObject));
            // use number params, get static struct list

            List<String> params2 = new ArrayList<>();
            params2.add("-256");
            params2.add("12321421");
            TransactionRequestWithStringParams transactionRequestWithStringParams2 = requestBuilder.setMethod("buildStaticStruct").buildStringParamsRequest(params2);
            TransactionResponse transactionResponse2 = transactionService.sendTransaction(transactionRequestWithStringParams2);
            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            returnObject = transactionResponse2.getReturnObject();
            returnABIObject = transactionResponse2.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("buildStaticStruct, staticStruct[]");
            System.out.println(JsonUtils.toJsonWithException(returnObject));

        }
    }

    public void test1ComplexCodecWithJavaObject() throws Exception {
        if ((client.getNegotiatedProtocol() >> 16) < 2) {
            return;
        }
        // test deploy with struct
        List<Object> deployParams = new ArrayList<>();
        {
            //    struct StructA {
            //        string[] value_str;
            //        bytes32[] bytes32_in_struct;
            //    }
            List<String> array = new ArrayList<>();
            array.add("test");
            List<byte[]> bytes = new ArrayList<>();
            byte[] b = Bytes32.DEFAULT.getValue();
            bytes.add(b);
            List<Object> structA = new ArrayList<>();
            structA.add(array);
            structA.add(bytes);
            deployParams.add(structA);
        }
        TransactionRequestBuilder requestBuilder = new TransactionRequestBuilder(abi, bin);
        DeployTransactionRequest request = requestBuilder.buildDeployRequest(deployParams);

        TransactionResponse response = transactionService.deployContract(request);

        assert (response.getTransactionReceipt().getStatus() == 0);
        String contractAddress = response.getContractAddress();
        assert (StringUtils.isNotBlank(response.getContractAddress()));

        // test call get struct
        {
            TransactionRequest transactionRequest = requestBuilder.setTo(contractAddress).setMethod("getStructA").buildRequest(new ArrayList<>());
            // not params method
            CallResponse callResponse = transactionService.sendCall(transactionRequest);
            List<Object> returnObject = callResponse.getReturnObject();
            assert (returnObject.size() == 1);
            assert (callResponse.getReturnABIObject().size() == 1);
            System.out.println(JsonUtils.toJson(returnObject));

            //    struct StructA {
            //        string[] value_str;
            //        bytes32[] bytes32_in_struct;
            //    }
            List<Object> callParams = new ArrayList<>();
            List<String> array = new ArrayList<>();
            array.add("test31241233123");
            List<byte[]> bytes = new ArrayList<>();
            byte[] b = Bytes32.DEFAULT.getValue();
            byte[] b2 = Bytes32.DEFAULT.getValue();
            bytes.add(b);
            bytes.add(b2);
            List<Object> structA = new ArrayList<>();
            structA.add(array);
            structA.add(bytes);
            callParams.add(structA);

            TransactionRequest transactionRequest2 = requestBuilder.setMethod("getStructA").buildRequest(callParams);
            CallResponse callResponse2 = transactionService.sendCall(transactionRequest2);

            returnObject = callResponse2.getReturnObject();
            assert (returnObject.size() == 1);
            assert (callResponse2.getReturnABIObject().size() == 1);
            System.out.println("getStructA:");
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test bytes[][] set and get
        {
            List<Object> params = new ArrayList<>();
            byte[] b = "1234".getBytes();
            List<byte[]> bs = new ArrayList<>();
            bs.add(b);
            List<List<byte[]>> bss = new ArrayList<>();
            bss.add(bs);
            params.add(bss);

            TransactionRequest transactionRequest = requestBuilder.setMethod("setBytesArrayArray").buildRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequest);

            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("setBytesArrayArray, bytes[][]");
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test bytes32[][] set and get
        {
            List<Object> params = new ArrayList<>();
            byte[] b = Hex.decode("ffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff");
            List<byte[]> bs = new ArrayList<>();
            bs.add(b);
            List<List<byte[]>> bss = new ArrayList<>();
            bss.add(bs);
            params.add(bss);

            TransactionRequest transactionRequest = requestBuilder.setMethod("setBytes32ArrayArray").buildRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequest);
            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("setBytes32ArrayArray, bytes32[][]");
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test bytes[2][] set and get
        {
            List<Object> params = new ArrayList<>();
            byte[] b1 = Hex.decode("ffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff");
            byte[] b2 = DynamicBytes.DEFAULT.getValue();
            List<byte[]> bs = new ArrayList<>();
            bs.add(b1);
            bs.add(b2);
            List<List<byte[]>> bss = new ArrayList<>();
            bss.add(bs);
            params.add(bss);

            TransactionRequest transactionRequest = requestBuilder.setMethod("setBytesStaticArrayArray").buildRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequest);

            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("setBytesStaticArrayArray, bytes[2][]");
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test bytes32[2][] set and get
        {
            List<Object> params = new ArrayList<>();
            byte[] b1 = Hex.decode("ffffffff1234567890123456ffffffffffffffff1234567890123456ffffffff");
            byte[] b2 = Bytes32.DEFAULT.getValue();
            List<byte[]> bs = new ArrayList<>();
            bs.add(b1);
            bs.add(b2);
            List<List<byte[]>> bss = new ArrayList<>();
            bss.add(bs);
            params.add(bss);

            TransactionRequest transactionRequest = requestBuilder.setMethod("setBytes32StaticArrayArray").buildRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequest);

            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("setBytes32StaticArrayArray, bytes32[2][]");
            System.out.println(JsonUtils.toJson(returnObject));
        }

        // test struct set and get
        {
            List<Object> params = new ArrayList<>();
            List<String> array = new ArrayList<>();
            array.add("test2132131");
            List<byte[]> bytes32DynamicArray = new ArrayList<>();
            bytes32DynamicArray.add(Bytes32.DEFAULT.getValue());
            List<Object> structA = new ArrayList<>();
            structA.add(array);
            structA.add(bytes32DynamicArray);
            params.add(structA);
            TransactionRequest transactionRequest = requestBuilder.setMethod("buildStructB").buildRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequest);

            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 2);
            assert (returnABIObject.size() == 2);
            System.out.println("buildStructB, StructB, StructA[]");
            System.out.println(JsonUtils.toJsonWithException(returnObject));
        }

        // test static struct set and get
        {
            List<Object> params = new ArrayList<>();
            List<Integer> staticArray = new ArrayList<>();
            staticArray.add(1);
            List<Object> struct = new ArrayList<>();
            struct.add(128);
            struct.add(127);
            struct.add(staticArray);
            params.add(struct);

            // use static struct params, get single struct
            TransactionRequest transactionRequest = requestBuilder.setMethod("buildStaticStruct").buildRequest(params);
            TransactionResponse transactionResponse = transactionService.sendTransaction(transactionRequest);
            assert (transactionResponse.getTransactionReceipt().getStatus() == 0);
            List<Object> returnObject = transactionResponse.getReturnObject();
            List<ABIObject> returnABIObject = transactionResponse.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("buildStaticStruct, staticStruct");
            System.out.println(JsonUtils.toJsonWithException(returnObject));

            // use number params, get static struct list
            List<Object> params2 = new ArrayList<>();
            params2.add(256);
            params2.add(288);
            TransactionRequest transactionRequest1 = requestBuilder.setMethod("buildStaticStruct").buildRequest(params2);
            TransactionResponse transactionResponse1 = transactionService.sendTransaction(transactionRequest1);
            assert (transactionResponse1.getTransactionReceipt().getStatus() == 0);
            returnObject = transactionResponse1.getReturnObject();
            returnABIObject = transactionResponse1.getReturnABIObject();
            assert (returnObject.size() == 1);
            assert (returnABIObject.size() == 1);
            System.out.println("buildStaticStruct, staticStruct[]");
            System.out.println(JsonUtils.toJsonWithException(returnObject));
        }
    }


    public static void main(String[] args) throws Exception {
        TransactionManagerTest test = new TransactionManagerTest();
        test.test1ComplexCodecWithType();
        test.test2ComplexCodecWithStringParams();
        test.test1ComplexCodecWithJavaObject();
    }
}