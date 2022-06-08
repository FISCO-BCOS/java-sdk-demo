package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Address;
import org.fisco.bcos.sdk.v3.codec.datatypes.Bool;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicArray;
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
public class DmcTransfer extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50610661806100206000396000f3fe608060405234801561001057600080fd5b50600436106100565760003560e01c8062a8efc71461005b5780635e6eacaa14610070578063a6c2f8d614610083578063b69ef8a814610096578063d91921ed146100ab575b600080fd5b61006e6100693660046103cf565b6100be565b005b61006e61007e3660046103e8565b6100d1565b61006e61009136600461044f565b610235565b60015460405190815260200160405180910390f35b61006e6100b93660046103cf565b610261565b6001546100cb908261026e565b60015550565b6100db6001610261565b816100e58161053d565b6001549093509050826100f757505050565b336000818152602081815260408083208054825181850281018501909352808352919290919083018282801561015657602002820191906000526020600020905b81546001600160a01b03168152600190910190602001808311610138575b5050505050905060005b815181101561022b57600082828151811061017d5761017d610554565b602002602001015190506000819050600084518961019b919061056a565b604051632f37565560e11b81526004810182905289151560248201529091506001600160a01b03831690635e6eacaa90604401600060405180830381600087803b1580156101e857600080fd5b505af19250505080156101f9575060015b61021557871561020857600080fd5b610212818861058c565b96505b5050508080610223906105a4565b915050610160565b5050506001555050565b6001600160a01b038216600090815260208181526040909120825161025c92840190610355565b505050565b6001546100cb90826102b7565b60006102b083836040518060400160405280601e81526020017f536166654d6174683a207375627472616374696f6e206f766572666c6f77000081525061031b565b9392505050565b6000806102c4838561058c565b9050838110156102b05760405162461bcd60e51b815260206004820152601b60248201527f536166654d6174683a206164646974696f6e206f766572666c6f77000000000060448201526064015b60405180910390fd5b6000818484111561033f5760405162461bcd60e51b815260040161031291906105bf565b50600061034c8486610614565b95945050505050565b8280548282559060005260206000209081019282156103aa579160200282015b828111156103aa57825182546001600160a01b0319166001600160a01b03909116178255602090920191600190910190610375565b506103b69291506103ba565b5090565b5b808211156103b657600081556001016103bb565b6000602082840312156103e157600080fd5b5035919050565b600080604083850312156103fb57600080fd5b823591506020830135801515811461041257600080fd5b809150509250929050565b80356001600160a01b038116811461043457600080fd5b919050565b634e487b7160e01b600052604160045260246000fd5b6000806040838503121561046257600080fd5b61046b8361041d565b915060208084013567ffffffffffffffff8082111561048957600080fd5b818601915086601f83011261049d57600080fd5b8135818111156104af576104af610439565b8060051b604051601f19603f830116810181811085821117156104d4576104d4610439565b6040529182528482019250838101850191898311156104f257600080fd5b938501935b82851015610517576105088561041d565b845293850193928501926104f7565b8096505050505050509250929050565b634e487b7160e01b600052601160045260246000fd5b60008161054c5761054c610527565b506000190190565b634e487b7160e01b600052603260045260246000fd5b60008261058757634e487b7160e01b600052601260045260246000fd5b500490565b6000821982111561059f5761059f610527565b500190565b60006000198214156105b8576105b8610527565b5060010190565b600060208083528351808285015260005b818110156105ec578581018301518582016040015282016105d0565b818111156105fe576000604083870101525b50601f01601f1916929092016040019392505050565b60008282101561062657610626610527565b50039056fea264697066735822122004c5220b9a66d2e94c66570f0ede7a97aaafd1c435243b109d3b10750eec238a64736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b5061065f806100206000396000f3fe608060405234801561001057600080fd5b50600436106100575760003560e01c80631270f54c1461005c5780633c2e91ce146100715780638193b44d14610084578063e8a6467114610099578063ef996b45146100ac575b600080fd5b61006f61006a3660046103cd565b6100bf565b005b61006f61007f3660046103cd565b6100d2565b60015460405190815260200160405180910390f35b61006f6100a73660046103e6565b6100df565b61006f6100ba36600461044d565b610243565b6001546100cc908261026f565b60015550565b6001546100cc90826102db565b6100e960016100bf565b816100f38161053b565b60015490935090508261010557505050565b336000818152602081815260408083208054825181850281018501909352808352919290919083018282801561016457602002820191906000526020600020905b81546001600160a01b03168152600190910190602001808311610146575b5050505050905060005b815181101561023957600082828151811061018b5761018b610552565b60200260200101519050600081905060008451896101a99190610568565b60405163e8a6467160e01b81526004810182905289151560248201529091506001600160a01b0383169063e8a6467190604401600060405180830381600087803b1580156101f657600080fd5b505af1925050508015610207575060015b61022357871561021657600080fd5b610220818861058a565b96505b5050508080610231906105a2565b91505061016e565b5050506001555050565b6001600160a01b038216600090815260208181526040909120825161026a92840190610353565b505050565b60008061027c838561058a565b9050838110156102d457604051636381e58960e11b815260206004820152601b60248201527f536166654d6174683a206164646974696f6e206f766572666c6f77000000000060448201526064015b60405180910390fd5b9392505050565b60006102d483836040518060400160405280601e81526020017f536166654d6174683a207375627472616374696f6e206f766572666c6f7700008152506000818484111561033d57604051636381e58960e11b81526004016102cb91906105bd565b50600061034a8486610612565b95945050505050565b8280548282559060005260206000209081019282156103a8579160200282015b828111156103a857825182546001600160a01b0319166001600160a01b03909116178255602090920191600190910190610373565b506103b49291506103b8565b5090565b5b808211156103b457600081556001016103b9565b6000602082840312156103df57600080fd5b5035919050565b600080604083850312156103f957600080fd5b823591506020830135801515811461041057600080fd5b809150509250929050565b80356001600160a01b038116811461043257600080fd5b919050565b63b95aa35560e01b600052604160045260246000fd5b6000806040838503121561046057600080fd5b6104698361041b565b915060208084013567ffffffffffffffff8082111561048757600080fd5b818601915086601f83011261049b57600080fd5b8135818111156104ad576104ad610437565b8060051b604051601f19603f830116810181811085821117156104d2576104d2610437565b6040529182528482019250838101850191898311156104f057600080fd5b938501935b82851015610515576105068561041b565b845293850193928501926104f5565b8096505050505050509250929050565b63b95aa35560e01b600052601160045260246000fd5b60008161054a5761054a610525565b506000190190565b63b95aa35560e01b600052603260045260246000fd5b6000826105855763b95aa35560e01b600052601260045260246000fd5b500490565b6000821982111561059d5761059d610525565b500190565b60006000198214156105b6576105b6610525565b5060010190565b600060208083528351808285015260005b818110156105ea578581018301518582016040015282016105ce565b818111156105fc576000604083870101525b50601f01601f1916929092016040019392505050565b60008282101561062457610624610525565b50039056fea26469706673582212205fed9e6c5d839f00156dada169b435c5fe3eb6b1410a6b98c1659ec4c755913964736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"conflictFields\":[{\"kind\":4,\"value\":[1]}],\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"addBalance\",\"outputs\":[],\"selector\":[3642302957,309392716],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"address\",\"name\":\"from\",\"type\":\"address\"},{\"internalType\":\"address[]\",\"name\":\"to\",\"type\":\"address[]\"}],\"name\":\"addNextCall\",\"outputs\":[],\"selector\":[2797795542,4019809093],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":4,\"value\":[1]}],\"inputs\":[],\"name\":\"balance\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"selector\":[3063871656,2173940813],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":4,\"value\":[1]}],\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"subBalance\",\"outputs\":[],\"selector\":[11071431,1009684942],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"},{\"internalType\":\"bool\",\"name\":\"allowRevert\",\"type\":\"bool\"}],\"name\":\"takeShare\",\"outputs\":[],\"selector\":[1584311466,3903211121],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_ADDBALANCE = "addBalance";

    public static final String FUNC_ADDNEXTCALL = "addNextCall";

    public static final String FUNC_BALANCE = "balance";

    public static final String FUNC_SUBBALANCE = "subBalance";

    public static final String FUNC_TAKESHARE = "takeShare";

    protected DmcTransfer(String contractAddress, Client client, CryptoKeyPair credential) {
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
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String addBalance(BigInteger num, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_ADDBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForAddBalance(BigInteger num) {
        final Function function =
                new Function(
                        FUNC_ADDBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
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

    public TransactionReceipt addNextCall(String from, List<String> to) {
        final Function function =
                new Function(
                        FUNC_ADDNEXTCALL,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(from),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.DynamicArray<
                                        org.fisco.bcos.sdk.v3.codec.datatypes.Address>(
                                        org.fisco.bcos.sdk.v3.codec.datatypes.Address.class,
                                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(
                                                to,
                                                org.fisco.bcos.sdk.v3.codec.datatypes.Address
                                                        .class))),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String addNextCall(String from, List<String> to, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_ADDNEXTCALL,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(from),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.DynamicArray<
                                        org.fisco.bcos.sdk.v3.codec.datatypes.Address>(
                                        org.fisco.bcos.sdk.v3.codec.datatypes.Address.class,
                                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(
                                                to,
                                                org.fisco.bcos.sdk.v3.codec.datatypes.Address
                                                        .class))),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForAddNextCall(String from, List<String> to) {
        final Function function =
                new Function(
                        FUNC_ADDNEXTCALL,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(from),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.DynamicArray<
                                        org.fisco.bcos.sdk.v3.codec.datatypes.Address>(
                                        org.fisco.bcos.sdk.v3.codec.datatypes.Address.class,
                                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(
                                                to,
                                                org.fisco.bcos.sdk.v3.codec.datatypes.Address
                                                        .class))),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple2<String, List<String>> getAddNextCallInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_ADDNEXTCALL,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Address>() {},
                                new TypeReference<DynamicArray<Address>>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, List<String>>(
                (String) results.get(0).getValue(),
                convertToNative((List<Address>) results.get(1).getValue()));
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
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String subBalance(BigInteger num, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SUBBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSubBalance(BigInteger num) {
        final Function function =
                new Function(
                        FUNC_SUBBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
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

    public TransactionReceipt takeShare(BigInteger num, Boolean allowRevert) {
        final Function function =
                new Function(
                        FUNC_TAKESHARE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Bool(allowRevert)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String takeShare(BigInteger num, Boolean allowRevert, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_TAKESHARE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Bool(allowRevert)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForTakeShare(BigInteger num, Boolean allowRevert) {
        final Function function =
                new Function(
                        FUNC_TAKESHARE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Bool(allowRevert)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple2<BigInteger, Boolean> getTakeShareInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_TAKESHARE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Uint256>() {}, new TypeReference<Bool>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, Boolean>(
                (BigInteger) results.get(0).getValue(), (Boolean) results.get(1).getValue());
    }

    public static DmcTransfer load(
            String contractAddress, Client client, CryptoKeyPair credential) {
        return new DmcTransfer(contractAddress, client, credential);
    }

    public static DmcTransfer deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                DmcTransfer.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(),
                null,
                null);
    }
}
