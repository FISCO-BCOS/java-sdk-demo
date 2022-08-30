package org.fisco.bcos.sdk.demo.contract;

import static java.lang.Integer.toHexString;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;

@SuppressWarnings("unchecked")
public class CpuHeavyPrecompiled extends CpuHeavyContract {
    public static final String CONTRACT_ADDRESS_PREFIX = "0x000000000000000000000000000000000000";
    public static final Integer ADDRESS_STARTER = Integer.valueOf("5200", 16);

    public static final String[] BINARY_ARRAY = {""};

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {""};

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"size\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"signature\",\"type\":\"uint256\"}],\"name\":\"sort\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_SORT = "sort";

    protected CpuHeavyPrecompiled(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public TransactionReceipt sort(BigInteger size, BigInteger signature) {
        final Function function =
                new Function(
                        FUNC_SORT,
                        Arrays.<Type>asList(new Uint256(size), new Uint256(signature)),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    @Override
    public void sort(BigInteger size, BigInteger signature, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SORT,
                        Arrays.<Type>asList(new Uint256(size), new Uint256(signature)),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSort(BigInteger size, BigInteger signature) {
        final Function function =
                new Function(
                        FUNC_SORT,
                        Arrays.<Type>asList(new Uint256(size), new Uint256(signature)),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple2<BigInteger, BigInteger> getSortInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_SORT,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, BigInteger>(
                (BigInteger) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    public static String getAddress(int id) {
        return CONTRACT_ADDRESS_PREFIX + toHexString(ADDRESS_STARTER + id);
    };

    public static CpuHeavyPrecompiled load(int id, Client client, CryptoKeyPair credential) {
        String address = getAddress(id);
        System.out.println("Load precompiled address: " + address);
        return new CpuHeavyPrecompiled(address, client, credential);
    }
}
