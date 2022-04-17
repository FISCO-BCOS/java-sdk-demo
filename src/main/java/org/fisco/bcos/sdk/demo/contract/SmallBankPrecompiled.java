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
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
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
public class SmallBankPrecompiled extends Contract {
    public static final String CONTRACT_ADDRESS_PREFIX = "0x000000000000000000000000000000000000";

    public static final Integer ADDRESS_STARTER = Integer.valueOf("6200", 16);

    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b5061029b806100206000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c8063870187eb1461003b578063ca30543514610100575b600080fd5b6100fe6004803603604081101561005157600080fd5b810190808035906020019064010000000081111561006e57600080fd5b82018360208201111561008057600080fd5b803590602001918460018302840111640100000000831117156100a257600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192908035906020019092919050505061025c565b005b61025a6004803603606081101561011657600080fd5b810190808035906020019064010000000081111561013357600080fd5b82018360208201111561014557600080fd5b8035906020019184600183028401116401000000008311171561016757600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803590602001906401000000008111156101ca57600080fd5b8201836020820111156101dc57600080fd5b803590602001918460018302840111640100000000831117156101fe57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929080359060200190929190505050610260565b005b5050565b50505056fea2646970667358221220f41242f01c6a3a0d51acbedafaa3bb7531a19360a7d7213f770d27f5cc07d89464736f6c634300060a0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b5061029b806100206000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c8063563a765f1461003b578063f977b27214610197575b600080fd5b6101956004803603606081101561005157600080fd5b810190808035906020019064010000000081111561006e57600080fd5b82018360208201111561008057600080fd5b803590602001918460018302840111640100000000831117156100a257600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192908035906020019064010000000081111561010557600080fd5b82018360208201111561011757600080fd5b8035906020019184600183028401116401000000008311171561013957600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192908035906020019092919050505061025c565b005b61025a600480360360408110156101ad57600080fd5b81019080803590602001906401000000008111156101ca57600080fd5b8201836020820111156101dc57600080fd5b803590602001918460018302840111640100000000831117156101fe57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929080359060200190929190505050610261565b005b505050565b505056fea26469706673582212209f1d3bfd3c11890c01f486049d38856836bc58ce1508d6eb558f1391f1b633a664736f6c634300060a0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"arg1\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"arg2\",\"type\":\"uint256\"}],\"name\":\"sendPayment\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"arg1\",\"type\":\"uint256\"}],\"name\":\"updateBalance\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_SENDPAYMENT = "sendPayment";

    public static final String FUNC_UPDATEBALANCE = "updateBalance";

    protected SmallBankPrecompiled(
            String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public TransactionReceipt sendPayment(String arg0, String arg1, BigInteger arg2) {
        final Function function =
                new Function(
                        FUNC_SENDPAYMENT,
                        Arrays.<Type>asList(
                                new Utf8String(arg0), new Utf8String(arg1), new Uint256(arg2)),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void sendPayment(
            String arg0, String arg1, BigInteger arg2, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SENDPAYMENT,
                        Arrays.<Type>asList(
                                new Utf8String(arg0), new Utf8String(arg1), new Uint256(arg2)),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSendPayment(String arg0, String arg1, BigInteger arg2) {
        final Function function =
                new Function(
                        FUNC_SENDPAYMENT,
                        Arrays.<Type>asList(
                                new Utf8String(arg0), new Utf8String(arg1), new Uint256(arg2)),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple3<String, String, BigInteger> getSendPaymentInput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_SENDPAYMENT,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Uint256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple3<String, String, BigInteger>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (BigInteger) results.get(2).getValue());
    }

    public TransactionReceipt updateBalance(String arg0, BigInteger arg1) {
        final Function function =
                new Function(
                        FUNC_UPDATEBALANCE,
                        Arrays.<Type>asList(new Utf8String(arg0), new Uint256(arg1)),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void updateBalance(String arg0, BigInteger arg1, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_UPDATEBALANCE,
                        Arrays.<Type>asList(new Utf8String(arg0), new Uint256(arg1)),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForUpdateBalance(String arg0, BigInteger arg1) {
        final Function function =
                new Function(
                        FUNC_UPDATEBALANCE,
                        Arrays.<Type>asList(new Utf8String(arg0), new Uint256(arg1)),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple2<String, BigInteger> getUpdateBalanceInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_UPDATEBALANCE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Uint256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(
                (String) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    public static String getAddress(int id) {
        return CONTRACT_ADDRESS_PREFIX + toHexString(ADDRESS_STARTER + id);
    }

    public static SmallBankPrecompiled load(int id, Client client, CryptoKeyPair credential) {
        String contractAddress = getAddress(id);
        System.out.println("Load precompiled address: " + contractAddress);
        return new SmallBankPrecompiled(contractAddress, client, credential);
    }

    public static SmallBankPrecompiled deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                SmallBankPrecompiled.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                null,
                null,
                null);
    }
}
