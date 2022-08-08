package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Bool;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class Curve25519VRFVerifyTest extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50600080546001600160a01b03191661100a179055610319806100346000396000f3fe608060405234801561001057600080fd5b506004361061002b5760003560e01c8063c02a172f14610030575b600080fd5b61004361003e366004610198565b61005e565b60408051921515835260208301919091520160405180910390f35b6000805460405163c02a172f60e01b81528291829182916001600160a01b03169063c02a172f90610097908a908a908a9060040161026d565b6040805180830381865afa1580156100b3573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906100d791906102b0565b90925090506001821515146100eb57600080fd5b5050935093915050565b634e487b7160e01b600052604160045260246000fd5b600082601f83011261011c57600080fd5b813567ffffffffffffffff80821115610137576101376100f5565b604051601f8301601f19908116603f0116810190828211818310171561015f5761015f6100f5565b8160405283815286602085880101111561017857600080fd5b836020870160208301376000602085830101528094505050505092915050565b6000806000606084860312156101ad57600080fd5b833567ffffffffffffffff808211156101c557600080fd5b6101d18783880161010b565b945060208601359150808211156101e757600080fd5b6101f38783880161010b565b9350604086013591508082111561020957600080fd5b506102168682870161010b565b9150509250925092565b6000815180845260005b818110156102465760208185018101518683018201520161022a565b81811115610258576000602083870101525b50601f01601f19169290920160200192915050565b6060815260006102806060830186610220565b82810360208401526102928186610220565b905082810360408401526102a68185610220565b9695505050505050565b600080604083850312156102c357600080fd5b825180151581146102d357600080fd5b602093909301519294929350505056fea26469706673582212206b16f6019d3c230bc937bff987e11b104d3cf2e8d4cef8e8d072674ec62ffb5c64736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50600080546001600160a01b03191661100a179055610319806100346000396000f3fe608060405234801561001057600080fd5b506004361061002b5760003560e01c80634faabf6e14610030575b600080fd5b61004361003e366004610198565b61005e565b60408051921515835260208301919091520160405180910390f35b600080546040516327d55fb760e11b81528291829182916001600160a01b031690634faabf6e90610097908a908a908a9060040161026d565b6040805180830381865afa1580156100b3573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906100d791906102b0565b90925090506001821515146100eb57600080fd5b5050935093915050565b63b95aa35560e01b600052604160045260246000fd5b600082601f83011261011c57600080fd5b813567ffffffffffffffff80821115610137576101376100f5565b604051601f8301601f19908116603f0116810190828211818310171561015f5761015f6100f5565b8160405283815286602085880101111561017857600080fd5b836020870160208301376000602085830101528094505050505092915050565b6000806000606084860312156101ad57600080fd5b833567ffffffffffffffff808211156101c557600080fd5b6101d18783880161010b565b945060208601359150808211156101e757600080fd5b6101f38783880161010b565b9350604086013591508082111561020957600080fd5b506102168682870161010b565b9150509250925092565b6000815180845260005b818110156102465760208185018101518683018201520161022a565b81811115610258576000602083870101525b50601f01601f19169290920160200192915050565b6060815260006102806060830186610220565b82810360208401526102928186610220565b905082810360408401526102a68185610220565b9695505050505050565b600080604083850312156102c357600080fd5b825180151581146102d357600080fd5b602093909301519294929350505056fea264697066735822122025a44312277211e0f4f7e14559d57ba3fd80cb3827b3619233c5cd262dd966b264736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"inputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"bytes\",\"name\":\"input\",\"type\":\"bytes\"},{\"internalType\":\"bytes\",\"name\":\"vrfPublicKey\",\"type\":\"bytes\"},{\"internalType\":\"bytes\",\"name\":\"vrfProof\",\"type\":\"bytes\"}],\"name\":\"curve25519VRFVerify\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"},{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"selector\":[3223983919,1336590190],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_CURVE25519VRFVERIFY = "curve25519VRFVerify";

    protected Curve25519VRFVerifyTest(
            String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public TransactionReceipt curve25519VRFVerify(
            byte[] input, byte[] vrfPublicKey, byte[] vrfProof) {
        final Function function =
                new Function(
                        FUNC_CURVE25519VRFVERIFY,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes(input),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes(
                                        vrfPublicKey),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes(vrfProof)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String curve25519VRFVerify(
            byte[] input, byte[] vrfPublicKey, byte[] vrfProof, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_CURVE25519VRFVERIFY,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes(input),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes(
                                        vrfPublicKey),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes(vrfProof)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForCurve25519VRFVerify(
            byte[] input, byte[] vrfPublicKey, byte[] vrfProof) {
        final Function function =
                new Function(
                        FUNC_CURVE25519VRFVERIFY,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes(input),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes(
                                        vrfPublicKey),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes(vrfProof)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple3<byte[], byte[], byte[]> getCurve25519VRFVerifyInput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_CURVE25519VRFVERIFY,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicBytes>() {},
                                new TypeReference<DynamicBytes>() {},
                                new TypeReference<DynamicBytes>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple3<byte[], byte[], byte[]>(
                (byte[]) results.get(0).getValue(),
                (byte[]) results.get(1).getValue(),
                (byte[]) results.get(2).getValue());
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
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
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
                getABI(),
                null,
                null);
    }
}
