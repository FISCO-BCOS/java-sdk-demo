package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class RandomPrecompiled extends Contract {
    public static final String[] BINARY_ARRAY = {
        "6080604052348015600f57600080fd5b5060848061001e6000396000f3fe6080604052348015600f57600080fd5b506004361060285760003560e01c8063c6bab26714602d575b600080fd5b60336049565b6040518082815260200191505060405180910390f35b60009056fea26469706673582212202206c71fe7d9f2c5decd94801d2fd3d7dd69ace3187b9da01ee4bab5f7e59cf564736f6c634300060a0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "6080604052348015600f57600080fd5b5060848061001e6000396000f3fe6080604052348015600f57600080fd5b506004361060285760003560e01c80633884604014602d575b600080fd5b60336049565b6040518082815260200191505060405180910390f35b60009056fea2646970667358221220ce00036f3ca0673c52a8fe1bede93e9f04398ded1be9b8bc45224e0faffdf9bc64736f6c634300060a0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"inputs\":[],\"name\":\"generateRandomValue\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_GENERATERANDOMVALUE = "generateRandomValue";

    protected RandomPrecompiled(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public TransactionReceipt generateRandomValue() {
        final Function function =
                new Function(
                        FUNC_GENERATERANDOMVALUE,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void generateRandomValue(TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_GENERATERANDOMVALUE,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForGenerateRandomValue() {
        final Function function =
                new Function(
                        FUNC_GENERATERANDOMVALUE,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<BigInteger> getGenerateRandomValueOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_GENERATERANDOMVALUE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public static RandomPrecompiled load(
            String contractAddress, Client client, CryptoKeyPair credential) {
        return new RandomPrecompiled(contractAddress, client, credential);
    }

    public static RandomPrecompiled deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                RandomPrecompiled.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                null,
                null,
                null);
    }
}
