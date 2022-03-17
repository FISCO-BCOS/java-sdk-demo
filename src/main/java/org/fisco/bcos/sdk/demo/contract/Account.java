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
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class Account extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b506104f3806100206000396000f3fe608060405234801561001057600080fd5b506004361061004b5760003560e01c8062a8efc714610050578063a9059cbb1461006c578063b69ef8a814610088578063d91921ed146100a6575b600080fd5b61006a600480360381019061006591906102e5565b6100c2565b005b610086600480360381019061008191906102a9565b6100e0565b005b61009061015e565b60405161009d91906103d8565b60405180910390f35b6100c060048036038101906100bb91906102e5565b610167565b005b6100d78160005461018590919063ffffffff16565b60008190555050565b6100e9816100c2565b60008290508073ffffffffffffffffffffffffffffffffffffffff1663d91921ed836040518263ffffffff1660e01b815260040161012791906103d8565b600060405180830381600087803b15801561014157600080fd5b505af1158015610155573d6000803e3d6000fd5b50505050505050565b60008054905090565b61017c816000546101cf90919063ffffffff16565b60008190555050565b60006101c783836040518060400160405280601e81526020017f536166654d6174683a207375627472616374696f6e206f766572666c6f770000815250610224565b905092915050565b60008082840190508381101561021a576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610211906103b8565b60405180910390fd5b8091505092915050565b600083831115829061026c576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016102639190610396565b60405180910390fd5b5060008385039050809150509392505050565b60008135905061028e8161048f565b92915050565b6000813590506102a3816104a6565b92915050565b600080604083850312156102bc57600080fd5b60006102ca8582860161027f565b92505060206102db85828601610294565b9150509250929050565b6000602082840312156102f757600080fd5b600061030584828501610294565b91505092915050565b6000610319826103f3565b61032381856103fe565b935061033381856020860161044b565b61033c8161047e565b840191505092915050565b6000610354601b836103fe565b91507f536166654d6174683a206164646974696f6e206f766572666c6f7700000000006000830152602082019050919050565b61039081610441565b82525050565b600060208201905081810360008301526103b0818461030e565b905092915050565b600060208201905081810360008301526103d181610347565b9050919050565b60006020820190506103ed6000830184610387565b92915050565b600081519050919050565b600082825260208201905092915050565b600061041a82610421565b9050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b60005b8381101561046957808201518184015260208101905061044e565b83811115610478576000848401525b50505050565b6000601f19601f8301169050919050565b6104988161040f565b81146104a357600080fd5b50565b6104af81610441565b81146104ba57600080fd5b5056fea26469706673582212206180c2eb72d4009a003fd071f7c16728cbe8a4f64068368c7d0fd22202b3943064736f6c634300060a0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b506104f4806100206000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c80631270f54c146100515780633c2e91ce1461006d5780636904e965146100895780638193b44d146100a5575b600080fd5b61006b600480360381019061006691906102e6565b6100c3565b005b610087600480360381019061008291906102e6565b6100e1565b005b6100a3600480360381019061009e91906102aa565b6100ff565b005b6100ad61017d565b6040516100ba91906103d9565b60405180910390f35b6100d88160005461018690919063ffffffff16565b60008190555050565b6100f6816000546101db90919063ffffffff16565b60008190555050565b610108816100e1565b60008290508073ffffffffffffffffffffffffffffffffffffffff16631270f54c836040518263ffffffff1660e01b815260040161014691906103d9565b600060405180830381600087803b15801561016057600080fd5b505af1158015610174573d6000803e3d6000fd5b50505050505050565b60008054905090565b6000808284019050838110156101d1576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004016101c8906103b9565b60405180910390fd5b8091505092915050565b600061021d83836040518060400160405280601e81526020017f536166654d6174683a207375627472616374696f6e206f766572666c6f770000815250610225565b905092915050565b600083831115829061026d576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004016102649190610397565b60405180910390fd5b5060008385039050809150509392505050565b60008135905061028f81610490565b92915050565b6000813590506102a4816104a7565b92915050565b600080604083850312156102bd57600080fd5b60006102cb85828601610280565b92505060206102dc85828601610295565b9150509250929050565b6000602082840312156102f857600080fd5b600061030684828501610295565b91505092915050565b600061031a826103f4565b61032481856103ff565b935061033481856020860161044c565b61033d8161047f565b840191505092915050565b6000610355601b836103ff565b91507f536166654d6174683a206164646974696f6e206f766572666c6f7700000000006000830152602082019050919050565b61039181610442565b82525050565b600060208201905081810360008301526103b1818461030f565b905092915050565b600060208201905081810360008301526103d281610348565b9050919050565b60006020820190506103ee6000830184610388565b92915050565b600081519050919050565b600082825260208201905092915050565b600061041b82610422565b9050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b60005b8381101561046a57808201518184015260208101905061044f565b83811115610479576000848401525b50505050565b6000601f19601f8301169050919050565b61049981610410565b81146104a457600080fd5b50565b6104b081610442565b81146104bb57600080fd5b5056fea26469706673582212202aeea2ec37cefbbbeccbb236583cb9e329b4184b44e727065c7893a4130a949d64736f6c634300060a0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"addBalance\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"balance\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"subBalance\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"to\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_ADDBALANCE = "addBalance";

    public static final String FUNC_BALANCE = "balance";

    public static final String FUNC_SUBBALANCE = "subBalance";

    public static final String FUNC_TRANSFER = "transfer";

    protected Account(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public TransactionReceipt addBalance(BigInteger num) {
        final Function function =
                new Function(
                        FUNC_ADDBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void addBalance(BigInteger num, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_ADDBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForAddBalance(BigInteger num) {
        final Function function =
                new Function(
                        FUNC_ADDBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<BigInteger> getAddBalanceInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_ADDBALANCE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public BigInteger balance() throws ContractException {
        final Function function =
                new Function(
                        FUNC_BALANCE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public TransactionReceipt subBalance(BigInteger num) {
        final Function function =
                new Function(
                        FUNC_SUBBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void subBalance(BigInteger num, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SUBBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSubBalance(BigInteger num) {
        final Function function =
                new Function(
                        FUNC_SUBBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<BigInteger> getSubBalanceInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_SUBBALANCE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public TransactionReceipt transfer(String to, BigInteger num) {
        final Function function =
                new Function(
                        FUNC_TRANSFER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(to),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void transfer(String to, BigInteger num, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_TRANSFER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(to),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForTransfer(String to, BigInteger num) {
        final Function function =
                new Function(
                        FUNC_TRANSFER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(to),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
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

    public static Account load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new Account(contractAddress, client, credential);
    }

    public static Account deploy(Client client, CryptoKeyPair credential) throws ContractException {
        return deploy(
                Account.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                null,
                null,
                null);
    }
}
