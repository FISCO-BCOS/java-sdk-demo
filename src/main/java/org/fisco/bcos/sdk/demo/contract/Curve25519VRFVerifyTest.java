package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Bool;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.Utf8String;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class Curve25519VRFVerifyTest extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b506150066000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550610495806100626000396000f300608060405260043610610041576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806352ce50e814610046575b600080fd5b34801561005257600080fd5b5061006d60048036036100689190810190610218565b610084565b60405161007b929190610303565b60405180910390f35b6000806000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166352ce50e88686866040518463ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004016101019392919061032c565b6040805180830381600087803b15801561011a57600080fd5b505af115801561012e573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525061015291908101906101dc565b91509150935093915050565b600061016a82516103f2565b905092915050565b600082601f830112151561018557600080fd5b8135610198610193826103a5565b610378565b915080825260208301602083018583830111156101b457600080fd5b6101bf838284610408565b50505092915050565b60006101d482516103fe565b905092915050565b600080604083850312156101ef57600080fd5b60006101fd8582860161015e565b925050602061020e858286016101c8565b9150509250929050565b60008060006060848603121561022d57600080fd5b600084013567ffffffffffffffff81111561024757600080fd5b61025386828701610172565b935050602084013567ffffffffffffffff81111561027057600080fd5b61027c86828701610172565b925050604084013567ffffffffffffffff81111561029957600080fd5b6102a586828701610172565b9150509250925092565b6102b8816103dc565b82525050565b60006102c9826103d1565b8084526102dd816020860160208601610417565b6102e68161044a565b602085010191505092915050565b6102fd816103e8565b82525050565b600060408201905061031860008301856102af565b61032560208301846102f4565b9392505050565b6000606082019050818103600083015261034681866102be565b9050818103602083015261035a81856102be565b9050818103604083015261036e81846102be565b9050949350505050565b6000604051905081810181811067ffffffffffffffff8211171561039b57600080fd5b8060405250919050565b600067ffffffffffffffff8211156103bc57600080fd5b601f19601f8301169050602081019050919050565b600081519050919050565b60008115159050919050565b6000819050919050565b60008115159050919050565b6000819050919050565b82818337600083830152505050565b60005b8381101561043557808201518184015260208101905061041a565b83811115610444576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a72305820f0b4a69cf6d532bd4ebfa224d0b2f640275ec3529fd251b29a781ac63b2780896c6578706572696d656e74616cf50037"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b506150066000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550610495806100626000396000f300608060405260043610610041576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063226f66e014610046575b600080fd5b34801561005257600080fd5b5061006d60048036036100689190810190610218565b610084565b60405161007b929190610303565b60405180910390f35b6000806000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663226f66e08686866040518463ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004016101019392919061032c565b6040805180830381600087803b15801561011a57600080fd5b505af115801561012e573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525061015291908101906101dc565b91509150935093915050565b600061016a82516103f2565b905092915050565b600082601f830112151561018557600080fd5b8135610198610193826103a5565b610378565b915080825260208301602083018583830111156101b457600080fd5b6101bf838284610408565b50505092915050565b60006101d482516103fe565b905092915050565b600080604083850312156101ef57600080fd5b60006101fd8582860161015e565b925050602061020e858286016101c8565b9150509250929050565b60008060006060848603121561022d57600080fd5b600084013567ffffffffffffffff81111561024757600080fd5b61025386828701610172565b935050602084013567ffffffffffffffff81111561027057600080fd5b61027c86828701610172565b925050604084013567ffffffffffffffff81111561029957600080fd5b6102a586828701610172565b9150509250925092565b6102b8816103dc565b82525050565b60006102c9826103d1565b8084526102dd816020860160208601610417565b6102e68161044a565b602085010191505092915050565b6102fd816103e8565b82525050565b600060408201905061031860008301856102af565b61032560208301846102f4565b9392505050565b6000606082019050818103600083015261034681866102be565b9050818103602083015261035a81856102be565b9050818103604083015261036e81846102be565b9050949350505050565b6000604051905081810181811067ffffffffffffffff8211171561039b57600080fd5b8060405250919050565b600067ffffffffffffffff8211156103bc57600080fd5b601f19601f8301169050602081019050919050565b600081519050919050565b60008115159050919050565b6000819050919050565b60008115159050919050565b6000819050919050565b82818337600083830152505050565b60005b8381101561043557808201518184015260208101905061041a565b83811115610444576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a72305820c426d380d4d1bffe48b37aa6c05c81f64f8a167115e830781b856223a6857b906c6578706572696d656e74616cf50037"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"constant\":false,\"inputs\":[{\"name\":\"input\",\"type\":\"string\"},{\"name\":\"vrfPublicKey\",\"type\":\"string\"},{\"name\":\"vrfProof\",\"type\":\"string\"}],\"name\":\"curve25519VRFVerify\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"},{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_CURVE25519VRFVERIFY = "curve25519VRFVerify";

    protected Curve25519VRFVerifyTest(
            String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public TransactionReceipt curve25519VRFVerify(
            String input, String vrfPublicKey, String vrfProof) {
        final Function function =
                new Function(
                        FUNC_CURVE25519VRFVERIFY,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(input),
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(vrfPublicKey),
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(vrfProof)),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void curve25519VRFVerify(
            String input, String vrfPublicKey, String vrfProof, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_CURVE25519VRFVERIFY,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(input),
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(vrfPublicKey),
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(vrfProof)),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForCurve25519VRFVerify(
            String input, String vrfPublicKey, String vrfProof) {
        final Function function =
                new Function(
                        FUNC_CURVE25519VRFVERIFY,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(input),
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(vrfPublicKey),
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(vrfProof)),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple3<String, String, String> getCurve25519VRFVerifyInput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_CURVE25519VRFVERIFY,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple3<String, String, String>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (String) results.get(2).getValue());
    }

    public Tuple2<Boolean, BigInteger> getCurve25519VRFVerifyOutput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_CURVE25519VRFVERIFY,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<Boolean, BigInteger>(
                (Boolean) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    public static Curve25519VRFVerifyTest load(
            String contractAddress, Client client, CryptoKeyPair credential) {
        return new Curve25519VRFVerifyTest(contractAddress, client, credential);
    }

    public static Curve25519VRFVerifyTest deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                Curve25519VRFVerifyTest.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                null);
    }
}
