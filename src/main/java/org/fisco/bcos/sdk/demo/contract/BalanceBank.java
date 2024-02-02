package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Address;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class BalanceBank extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50610101806100206000396000f3fe6080604052348015600f57600080fd5b506004361060325760003560e01c80635145cbc0146037578063a9059cbb14604a575b600080fd5b4760405190815260200160405180910390f35b605960553660046095565b605b565b005b6040516001600160a01b0383169082156108fc029083906000818181858888f193505050501580156090573d6000803e3d6000fd5b505050565b6000806040838503121560a757600080fd5b82356001600160a01b038116811460bd57600080fd5b94602093909301359350505056fea264697066735822122054163009e1b2ab5fa44d665f7e6d94e255a5eaa5515cd241a8c194c5f2cd590364736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50610101806100206000396000f3fe6080604052348015600f57600080fd5b506004361060325760003560e01c806342cbaf441460375780636904e96514604a575b600080fd5b4760405190815260200160405180910390f35b605960553660046095565b605b565b005b6040516001600160a01b0383169082156108fc029083906000818181858888f193505050501580156090573d6000803e3d6000fd5b505050565b6000806040838503121560a757600080fd5b82356001600160a01b038116811460bd57600080fd5b94602093909301359350505056fea2646970667358221220a9d8569456de777fd26766ac443d6e09ee6536fe719557f6ea3061fcf219449f64736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"conflictFields\":[{\"kind\":5}],\"inputs\":[],\"name\":\"getSmartContractBalance\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"selector\":[1363528640,1120644932],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"address\",\"name\":\"_to\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[],\"selector\":[2835717307,1761929573],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_GETSMARTCONTRACTBALANCE = "getSmartContractBalance";

    public static final String FUNC_TRANSFER = "transfer";

    protected BalanceBank(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public BigInteger getSmartContractBalance() throws ContractException {
        final Function function =
                new Function(
                        FUNC_GETSMARTCONTRACTBALANCE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public Function getMethodGetSmartContractBalanceRawFunction() throws ContractException {
        final Function function =
                new Function(
                        FUNC_GETSMARTCONTRACTBALANCE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return function;
    }

    public TransactionReceipt transfer(String _to, BigInteger _value) {
        final Function function =
                new Function(
                        FUNC_TRANSFER,
                        Arrays.<Type>asList(new Address(_to), new Uint256(_value)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public Function getMethodTransferRawFunction(String _to, BigInteger _value)
            throws ContractException {
        final Function function =
                new Function(
                        FUNC_TRANSFER,
                        Arrays.<Type>asList(new Address(_to), new Uint256(_value)),
                        Arrays.<TypeReference<?>>asList());
        return function;
    }

    public String getSignedTransactionForTransfer(String _to, BigInteger _value) {
        final Function function =
                new Function(
                        FUNC_TRANSFER,
                        Arrays.<Type>asList(new Address(_to), new Uint256(_value)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public String transfer(String _to, BigInteger _value, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_TRANSFER,
                        Arrays.<Type>asList(new Address(_to), new Uint256(_value)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple2<String, BigInteger> getTransferInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_TRANSFER,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(
                (String) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    public static BalanceBank load(
            String contractAddress, Client client, CryptoKeyPair credential) {
        return new BalanceBank(contractAddress, client, credential);
    }

    public static BalanceBank deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                BalanceBank.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(),
                null,
                null);
    }
}
