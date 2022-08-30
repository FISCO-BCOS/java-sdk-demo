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
        "608060405234801561001057600080fd5b506106bb806100206000396000f3fe608060405234801561001057600080fd5b50600436106100565760003560e01c8062a8efc71461005b5780635e6eacaa14610070578063a6c2f8d614610083578063b69ef8a814610096578063d91921ed146100ab575b600080fd5b61006e61006936600461040a565b6100be565b005b61006e61007e366004610423565b6100d1565b61006e61009136600461048a565b610270565b60015460405190815260200160405180910390f35b61006e6100b936600461040a565b61029c565b6001546100cb90826102a9565b60015550565b816100db81610578565b9250506100e8600161029c565b816100f1575050565b336000818152602081815260408083208054825181850281018501909352808352919290919083018282801561015057602002820191906000526020600020905b81546001600160a01b03168152600190910190602001808311610132575b505050505090506000815185610166919061058f565b9050600082518261017791906105b1565b61018190876105d0565b905061018c8161029c565b60015460005b84518110156102645760008582815181106101af576101af6105e7565b6020908102919091010151604051632f37565560e11b815260048101879052891515602482015290915081906001600160a01b03821690635e6eacaa90604401600060405180830381600087803b15801561020957600080fd5b505af192505050801561021a575060015b61023a57881561022957600080fd5b61023386856105fd565b9350610251565b6001600160a01b0382163014156102515760015493505b50508061025d90610615565b9050610192565b50600155505050505050565b6001600160a01b038216600090815260208181526040909120825161029792840190610390565b505050565b6001546100cb90826102f2565b60006102eb83836040518060400160405280601e81526020017f536166654d6174683a207375627472616374696f6e206f766572666c6f770000815250610356565b9392505050565b6000806102ff83856105fd565b9050838110156102eb5760405162461bcd60e51b815260206004820152601b60248201527f536166654d6174683a206164646974696f6e206f766572666c6f77000000000060448201526064015b60405180910390fd5b6000818484111561037a5760405162461bcd60e51b815260040161034d9190610630565b50600061038784866105d0565b95945050505050565b8280548282559060005260206000209081019282156103e5579160200282015b828111156103e557825182546001600160a01b0319166001600160a01b039091161782556020909201916001909101906103b0565b506103f19291506103f5565b5090565b5b808211156103f157600081556001016103f6565b60006020828403121561041c57600080fd5b5035919050565b6000806040838503121561043657600080fd5b823591506020830135801515811461044d57600080fd5b809150509250929050565b80356001600160a01b038116811461046f57600080fd5b919050565b634e487b7160e01b600052604160045260246000fd5b6000806040838503121561049d57600080fd5b6104a683610458565b915060208084013567ffffffffffffffff808211156104c457600080fd5b818601915086601f8301126104d857600080fd5b8135818111156104ea576104ea610474565b8060051b604051601f19603f8301168101818110858211171561050f5761050f610474565b60405291825284820192508381018501918983111561052d57600080fd5b938501935b828510156105525761054385610458565b84529385019392850192610532565b8096505050505050509250929050565b634e487b7160e01b600052601160045260246000fd5b60008161058757610587610562565b506000190190565b6000826105ac57634e487b7160e01b600052601260045260246000fd5b500490565b60008160001904831182151516156105cb576105cb610562565b500290565b6000828210156105e2576105e2610562565b500390565b634e487b7160e01b600052603260045260246000fd5b6000821982111561061057610610610562565b500190565b600060001982141561062957610629610562565b5060010190565b600060208083528351808285015260005b8181101561065d57858101830151858201604001528201610641565b8181111561066f576000604083870101525b50601f01601f191692909201604001939250505056fea2646970667358221220f251accd35ac5422fce7a14f4bb7c98a95a8119a0e46010d983e76a725a0380264736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b506106b9806100206000396000f3fe608060405234801561001057600080fd5b50600436106100575760003560e01c80631270f54c1461005c5780633c2e91ce146100715780638193b44d14610084578063e8a6467114610099578063ef996b45146100ac575b600080fd5b61006f61006a366004610408565b6100bf565b005b61006f61007f366004610408565b6100d2565b60015460405190815260200160405180910390f35b61006f6100a7366004610421565b6100df565b61006f6100ba366004610488565b61027e565b6001546100cc90826102aa565b60015550565b6001546100cc9082610316565b816100e981610576565b9250506100f660016100bf565b816100ff575050565b336000818152602081815260408083208054825181850281018501909352808352919290919083018282801561015e57602002820191906000526020600020905b81546001600160a01b03168152600190910190602001808311610140575b505050505090506000815185610174919061058d565b9050600082518261018591906105af565b61018f90876105ce565b905061019a816100bf565b60015460005b84518110156102725760008582815181106101bd576101bd6105e5565b602090810291909101015160405163e8a6467160e01b815260048101879052891515602482015290915081906001600160a01b0382169063e8a6467190604401600060405180830381600087803b15801561021757600080fd5b505af1925050508015610228575060015b61024857881561023757600080fd5b61024186856105fb565b935061025f565b6001600160a01b03821630141561025f5760015493505b50508061026b90610613565b90506101a0565b50600155505050505050565b6001600160a01b03821660009081526020818152604090912082516102a59284019061038e565b505050565b6000806102b783856105fb565b90508381101561030f57604051636381e58960e11b815260206004820152601b60248201527f536166654d6174683a206164646974696f6e206f766572666c6f77000000000060448201526064015b60405180910390fd5b9392505050565b600061030f83836040518060400160405280601e81526020017f536166654d6174683a207375627472616374696f6e206f766572666c6f7700008152506000818484111561037857604051636381e58960e11b8152600401610306919061062e565b50600061038584866105ce565b95945050505050565b8280548282559060005260206000209081019282156103e3579160200282015b828111156103e357825182546001600160a01b0319166001600160a01b039091161782556020909201916001909101906103ae565b506103ef9291506103f3565b5090565b5b808211156103ef57600081556001016103f4565b60006020828403121561041a57600080fd5b5035919050565b6000806040838503121561043457600080fd5b823591506020830135801515811461044b57600080fd5b809150509250929050565b80356001600160a01b038116811461046d57600080fd5b919050565b63b95aa35560e01b600052604160045260246000fd5b6000806040838503121561049b57600080fd5b6104a483610456565b915060208084013567ffffffffffffffff808211156104c257600080fd5b818601915086601f8301126104d657600080fd5b8135818111156104e8576104e8610472565b8060051b604051601f19603f8301168101818110858211171561050d5761050d610472565b60405291825284820192508381018501918983111561052b57600080fd5b938501935b828510156105505761054185610456565b84529385019392850192610530565b8096505050505050509250929050565b63b95aa35560e01b600052601160045260246000fd5b60008161058557610585610560565b506000190190565b6000826105aa5763b95aa35560e01b600052601260045260246000fd5b500490565b60008160001904831182151516156105c9576105c9610560565b500290565b6000828210156105e0576105e0610560565b500390565b63b95aa35560e01b600052603260045260246000fd5b6000821982111561060e5761060e610560565b500190565b600060001982141561062757610627610560565b5060010190565b600060208083528351808285015260005b8181101561065b5785810183015185820160400152820161063f565b8181111561066d576000604083870101525b50601f01601f191692909201604001939250505056fea2646970667358221220a9d54cd8df5c16216173e7169eb238b648ca28264e2a5a39faf227934ad423fb64736f6c634300080b0033"
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
