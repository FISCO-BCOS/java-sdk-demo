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
        "608060405234801561001057600080fd5b506105ad806100206000396000f3fe608060405234801561001057600080fd5b506004361061004b5760003560e01c8062a8efc714610050578063a9059cbb1461006c578063b69ef8a814610088578063d91921ed146100a6575b600080fd5b61006a6004803603810190610065919061039f565b6100c2565b005b61008660048036038101906100819190610363565b6100e0565b005b610090610218565b60405161009d9190610492565b60405180910390f35b6100c060048036038101906100bb919061039f565b610221565b005b6100d78160005461023f90919063ffffffff16565b60008190555050565b3073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff16146101995760008054905060008390508073ffffffffffffffffffffffffffffffffffffffff1663d91921ed846040518263ffffffff1660e01b81526004016101579190610492565b600060405180830381600087803b15801561017157600080fd5b505af1158015610185573d6000803e3d6000fd5b505050508282036000819055505050610214565b6101a2816100c2565b60008290508073ffffffffffffffffffffffffffffffffffffffff1663d91921ed836040518263ffffffff1660e01b81526004016101e09190610492565b600060405180830381600087803b1580156101fa57600080fd5b505af115801561020e573d6000803e3d6000fd5b50505050505b5050565b60008054905090565b6102368160005461028990919063ffffffff16565b60008190555050565b600061028183836040518060400160405280601e81526020017f536166654d6174683a207375627472616374696f6e206f766572666c6f7700008152506102de565b905092915050565b6000808284019050838110156102d4576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016102cb90610472565b60405180910390fd5b8091505092915050565b6000838311158290610326576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161031d9190610450565b60405180910390fd5b5060008385039050809150509392505050565b60008135905061034881610549565b92915050565b60008135905061035d81610560565b92915050565b6000806040838503121561037657600080fd5b600061038485828601610339565b92505060206103958582860161034e565b9150509250929050565b6000602082840312156103b157600080fd5b60006103bf8482850161034e565b91505092915050565b60006103d3826104ad565b6103dd81856104b8565b93506103ed818560208601610505565b6103f681610538565b840191505092915050565b600061040e601b836104b8565b91507f536166654d6174683a206164646974696f6e206f766572666c6f7700000000006000830152602082019050919050565b61044a816104fb565b82525050565b6000602082019050818103600083015261046a81846103c8565b905092915050565b6000602082019050818103600083015261048b81610401565b9050919050565b60006020820190506104a76000830184610441565b92915050565b600081519050919050565b600082825260208201905092915050565b60006104d4826104db565b9050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b60005b83811015610523578082015181840152602081019050610508565b83811115610532576000848401525b50505050565b6000601f19601f8301169050919050565b610552816104c9565b811461055d57600080fd5b50565b610569816104fb565b811461057457600080fd5b5056fea2646970667358221220e9ddfe5545c1b64909c0799fd0f3870965ee87feb772f5491945a80ca7ddbe6164736f6c634300060a0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b506105ae806100206000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c80631270f54c146100515780633c2e91ce1461006d5780636904e965146100895780638193b44d146100a5575b600080fd5b61006b600480360381019061006691906103a0565b6100c3565b005b610087600480360381019061008291906103a0565b6100e1565b005b6100a3600480360381019061009e9190610364565b6100ff565b005b6100ad610237565b6040516100ba9190610493565b60405180910390f35b6100d88160005461024090919063ffffffff16565b60008190555050565b6100f68160005461029590919063ffffffff16565b60008190555050565b3073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff16146101b85760008054905060008390508073ffffffffffffffffffffffffffffffffffffffff16631270f54c846040518263ffffffff1660e01b81526004016101769190610493565b600060405180830381600087803b15801561019057600080fd5b505af11580156101a4573d6000803e3d6000fd5b505050508282036000819055505050610233565b6101c1816100e1565b60008290508073ffffffffffffffffffffffffffffffffffffffff16631270f54c836040518263ffffffff1660e01b81526004016101ff9190610493565b600060405180830381600087803b15801561021957600080fd5b505af115801561022d573d6000803e3d6000fd5b50505050505b5050565b60008054905090565b60008082840190508381101561028b576040517fc703cb1200000000000000000000000000000000000000000000000000000000815260040161028290610473565b60405180910390fd5b8091505092915050565b60006102d783836040518060400160405280601e81526020017f536166654d6174683a207375627472616374696f6e206f766572666c6f7700008152506102df565b905092915050565b6000838311158290610327576040517fc703cb1200000000000000000000000000000000000000000000000000000000815260040161031e9190610451565b60405180910390fd5b5060008385039050809150509392505050565b6000813590506103498161054a565b92915050565b60008135905061035e81610561565b92915050565b6000806040838503121561037757600080fd5b60006103858582860161033a565b92505060206103968582860161034f565b9150509250929050565b6000602082840312156103b257600080fd5b60006103c08482850161034f565b91505092915050565b60006103d4826104ae565b6103de81856104b9565b93506103ee818560208601610506565b6103f781610539565b840191505092915050565b600061040f601b836104b9565b91507f536166654d6174683a206164646974696f6e206f766572666c6f7700000000006000830152602082019050919050565b61044b816104fc565b82525050565b6000602082019050818103600083015261046b81846103c9565b905092915050565b6000602082019050818103600083015261048c81610402565b9050919050565b60006020820190506104a86000830184610442565b92915050565b600081519050919050565b600082825260208201905092915050565b60006104d5826104dc565b9050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b60005b83811015610524578082015181840152602081019050610509565b83811115610533576000848401525b50505050565b6000601f19601f8301169050919050565b610553816104ca565b811461055e57600080fd5b50565b61056a816104fc565b811461057557600080fd5b5056fea264697066735822122085326869f0884578ff15b0bbfb1b62e64cce855e0dcff593689561208882eb3364736f6c634300060a0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"conflictFields\":[{\"kind\":4,\"value\":[0]}],\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"addBalance\",\"outputs\":[],\"selector\":[3642302957,309392716],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":4,\"value\":[0]}],\"inputs\":[],\"name\":\"balance\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"selector\":[3063871656,2173940813],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":4,\"value\":[0]}],\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"subBalance\",\"outputs\":[],\"selector\":[11071431,1009684942],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"address\",\"name\":\"to\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[],\"selector\":[2835717307,1761929573],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
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
                        Arrays.<Type>asList(new Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String addBalance(BigInteger num, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_ADDBALANCE,
                        Arrays.<Type>asList(new Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForAddBalance(BigInteger num) {
        final Function function =
                new Function(
                        FUNC_ADDBALANCE,
                        Arrays.<Type>asList(new Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
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
                        Arrays.<Type>asList(new Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String subBalance(BigInteger num, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SUBBALANCE,
                        Arrays.<Type>asList(new Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSubBalance(BigInteger num) {
        final Function function =
                new Function(
                        FUNC_SUBBALANCE,
                        Arrays.<Type>asList(new Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
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
                        Arrays.<Type>asList(new Address(to), new Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String transfer(String to, BigInteger num, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_TRANSFER,
                        Arrays.<Type>asList(new Address(to), new Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForTransfer(String to, BigInteger num) {
        final Function function =
                new Function(
                        FUNC_TRANSFER,
                        Arrays.<Type>asList(new Address(to), new Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
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
                getABI(),
                null,
                null);
    }

    public Client getClient() {
        return this.client;
    }

    public String getContractSender() {
        return this.client.getCryptoSuite().getCryptoKeyPair().getAddress();
    }
}
