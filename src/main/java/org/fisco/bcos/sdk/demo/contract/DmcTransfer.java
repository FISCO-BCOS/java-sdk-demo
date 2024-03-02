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
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b506106d2806100206000396000f3fe608060405234801561001057600080fd5b50600436106100565760003560e01c8062a8efc71461005b5780635e6eacaa14610070578063a6c2f8d614610083578063b69ef8a814610096578063d91921ed146100ab575b600080fd5b61006e610069366004610421565b6100be565b005b61006e61007e36600461043a565b6100d1565b61006e6100913660046104a1565b610287565b60015460405190815260200160405180910390f35b61006e6100b9366004610421565b6102b3565b6001546100cb90826102c0565b60015550565b816100db8161058f565b9250506100e860016102b3565b816100f1575050565b336000818152602081815260408083208054825181850281018501909352808352919290919083018282801561015057602002820191906000526020600020905b81546001600160a01b03168152600190910190602001808311610132575b50505050509050600081518561016691906105a6565b9050600082518261017791906105c8565b61018190876105e7565b905061018c816102b3565b60015460005b845181101561027b5760008582815181106101af576101af6105fe565b6020908102919091010151604051632f37565560e11b815260048101879052891515602482015290915081906001600160a01b03821690635e6eacaa90604401600060405180830381600087803b15801561020957600080fd5b505af192505050801561021a575060015b61025157881561022957600080fd5b6102338685610614565b93506001600160a01b03821630141561024c5760018490555b610268565b6001600160a01b0382163014156102685760015493505b5050806102749061062c565b9050610192565b50600155505050505050565b6001600160a01b03821660009081526020818152604090912082516102ae928401906103a7565b505050565b6001546100cb9082610309565b600061030283836040518060400160405280601e81526020017f536166654d6174683a207375627472616374696f6e206f766572666c6f77000081525061036d565b9392505050565b6000806103168385610614565b9050838110156103025760405162461bcd60e51b815260206004820152601b60248201527f536166654d6174683a206164646974696f6e206f766572666c6f77000000000060448201526064015b60405180910390fd5b600081848411156103915760405162461bcd60e51b81526004016103649190610647565b50600061039e84866105e7565b95945050505050565b8280548282559060005260206000209081019282156103fc579160200282015b828111156103fc57825182546001600160a01b0319166001600160a01b039091161782556020909201916001909101906103c7565b5061040892915061040c565b5090565b5b80821115610408576000815560010161040d565b60006020828403121561043357600080fd5b5035919050565b6000806040838503121561044d57600080fd5b823591506020830135801515811461046457600080fd5b809150509250929050565b80356001600160a01b038116811461048657600080fd5b919050565b634e487b7160e01b600052604160045260246000fd5b600080604083850312156104b457600080fd5b6104bd8361046f565b915060208084013567ffffffffffffffff808211156104db57600080fd5b818601915086601f8301126104ef57600080fd5b8135818111156105015761050161048b565b8060051b604051601f19603f830116810181811085821117156105265761052661048b565b60405291825284820192508381018501918983111561054457600080fd5b938501935b828510156105695761055a8561046f565b84529385019392850192610549565b8096505050505050509250929050565b634e487b7160e01b600052601160045260246000fd5b60008161059e5761059e610579565b506000190190565b6000826105c357634e487b7160e01b600052601260045260246000fd5b500490565b60008160001904831182151516156105e2576105e2610579565b500290565b6000828210156105f9576105f9610579565b500390565b634e487b7160e01b600052603260045260246000fd5b6000821982111561062757610627610579565b500190565b600060001982141561064057610640610579565b5060010190565b600060208083528351808285015260005b8181101561067457858101830151858201604001528201610658565b81811115610686576000604083870101525b50601f01601f191692909201604001939250505056fea2646970667358221220a6ed16977372430ce4c6f37746399b0289ad8d2136f625c694fe2868b65257d864736f6c634300080b0033"};

    public static final String BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"608060405234801561001057600080fd5b506106d0806100206000396000f3fe608060405234801561001057600080fd5b50600436106100575760003560e01c80631270f54c1461005c5780633c2e91ce146100715780638193b44d14610084578063e8a6467114610099578063ef996b45146100ac575b600080fd5b61006f61006a36600461041f565b6100bf565b005b61006f61007f36600461041f565b6100d2565b60015460405190815260200160405180910390f35b61006f6100a7366004610438565b6100df565b61006f6100ba36600461049f565b610295565b6001546100cc90826102c1565b60015550565b6001546100cc908261032d565b816100e98161058d565b9250506100f660016100bf565b816100ff575050565b336000818152602081815260408083208054825181850281018501909352808352919290919083018282801561015e57602002820191906000526020600020905b81546001600160a01b03168152600190910190602001808311610140575b50505050509050600081518561017491906105a4565b9050600082518261018591906105c6565b61018f90876105e5565b905061019a816100bf565b60015460005b84518110156102895760008582815181106101bd576101bd6105fc565b602090810291909101015160405163e8a6467160e01b815260048101879052891515602482015290915081906001600160a01b0382169063e8a6467190604401600060405180830381600087803b15801561021757600080fd5b505af1925050508015610228575060015b61025f57881561023757600080fd5b6102418685610612565b93506001600160a01b03821630141561025a5760018490555b610276565b6001600160a01b0382163014156102765760015493505b5050806102829061062a565b90506101a0565b50600155505050505050565b6001600160a01b03821660009081526020818152604090912082516102bc928401906103a5565b505050565b6000806102ce8385610612565b90508381101561032657604051636381e58960e11b815260206004820152601b60248201527f536166654d6174683a206164646974696f6e206f766572666c6f77000000000060448201526064015b60405180910390fd5b9392505050565b600061032683836040518060400160405280601e81526020017f536166654d6174683a207375627472616374696f6e206f766572666c6f7700008152506000818484111561038f57604051636381e58960e11b815260040161031d9190610645565b50600061039c84866105e5565b95945050505050565b8280548282559060005260206000209081019282156103fa579160200282015b828111156103fa57825182546001600160a01b0319166001600160a01b039091161782556020909201916001909101906103c5565b5061040692915061040a565b5090565b5b80821115610406576000815560010161040b565b60006020828403121561043157600080fd5b5035919050565b6000806040838503121561044b57600080fd5b823591506020830135801515811461046257600080fd5b809150509250929050565b80356001600160a01b038116811461048457600080fd5b919050565b63b95aa35560e01b600052604160045260246000fd5b600080604083850312156104b257600080fd5b6104bb8361046d565b915060208084013567ffffffffffffffff808211156104d957600080fd5b818601915086601f8301126104ed57600080fd5b8135818111156104ff576104ff610489565b8060051b604051601f19603f8301168101818110858211171561052457610524610489565b60405291825284820192508381018501918983111561054257600080fd5b938501935b82851015610567576105588561046d565b84529385019392850192610547565b8096505050505050509250929050565b63b95aa35560e01b600052601160045260246000fd5b60008161059c5761059c610577565b506000190190565b6000826105c15763b95aa35560e01b600052601260045260246000fd5b500490565b60008160001904831182151516156105e0576105e0610577565b500290565b6000828210156105f7576105f7610577565b500390565b63b95aa35560e01b600052603260045260246000fd5b6000821982111561062557610625610577565b500190565b600060001982141561063e5761063e610577565b5060010190565b600060208083528351808285015260005b8181101561067257858101830151858201604001528201610656565b81811115610684576000604083870101525b50601f01601f191692909201604001939250505056fea26469706673582212207747ecb6921432258f311e36330e177c93fcba4f7054bc1e0c76be893548d5d664736f6c634300080b0033"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"conflictFields\":[{\"kind\":4,\"value\":[1]}],\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"addBalance\",\"outputs\":[],\"selector\":[3642302957,309392716],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"address\",\"name\":\"from\",\"type\":\"address\"},{\"internalType\":\"address[]\",\"name\":\"to\",\"type\":\"address[]\"}],\"name\":\"addNextCall\",\"outputs\":[],\"selector\":[2797795542,4019809093],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":4,\"value\":[1]}],\"inputs\":[],\"name\":\"balance\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"selector\":[3063871656,2173940813],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":4,\"value\":[1]}],\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"subBalance\",\"outputs\":[],\"selector\":[11071431,1009684942],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"},{\"internalType\":\"bool\",\"name\":\"allowRevert\",\"type\":\"bool\"}],\"name\":\"takeShare\",\"outputs\":[],\"selector\":[1584311466,3903211121],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"};

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
        final Function function = new Function(
                FUNC_ADDBALANCE, 
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public Function getMethodAddBalanceRawFunction(BigInteger num) throws ContractException {
        final Function function = new Function(FUNC_ADDBALANCE, 
                Arrays.<Type>asList(new Uint256(num)),
                Arrays.<TypeReference<?>>asList());
        return function;
    }

    public String getSignedTransactionForAddBalance(BigInteger num) {
        final Function function = new Function(
                FUNC_ADDBALANCE, 
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public String addBalance(BigInteger num, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_ADDBALANCE, 
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple1<BigInteger> getAddBalanceInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_ADDBALANCE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
                );
    }

    public TransactionReceipt addNextCall(String from, List<String> to) {
        final Function function = new Function(
                FUNC_ADDNEXTCALL, 
                Arrays.<Type>asList(new Address(from),
                new DynamicArray<Address>(
                        Address.class,
                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(to, Address.class))),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public Function getMethodAddNextCallRawFunction(String from, List<String> to) throws
            ContractException {
        final Function function = new Function(FUNC_ADDNEXTCALL, 
                Arrays.<Type>asList(new Address(from),
                new DynamicArray<Address>(
                        Address.class,
                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(to, Address.class))),
                Arrays.<TypeReference<?>>asList());
        return function;
    }

    public String getSignedTransactionForAddNextCall(String from, List<String> to) {
        final Function function = new Function(
                FUNC_ADDNEXTCALL, 
                Arrays.<Type>asList(new Address(from),
                new DynamicArray<Address>(
                        Address.class,
                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(to, Address.class))),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public String addNextCall(String from, List<String> to, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_ADDNEXTCALL, 
                Arrays.<Type>asList(new Address(from),
                new DynamicArray<Address>(
                        Address.class,
                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(to, Address.class))),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple2<String, List<String>> getAddNextCallInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_ADDNEXTCALL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<DynamicArray<Address>>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, List<String>>(

                (String) results.get(0).getValue(), 
                convertToNative((List<Address>) results.get(1).getValue())
                );
    }

    public BigInteger balance() throws ContractException {
        final Function function = new Function(FUNC_BALANCE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public Function getMethodBalanceRawFunction() throws ContractException {
        final Function function = new Function(FUNC_BALANCE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return function;
    }

    public TransactionReceipt subBalance(BigInteger num) {
        final Function function = new Function(
                FUNC_SUBBALANCE, 
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public Function getMethodSubBalanceRawFunction(BigInteger num) throws ContractException {
        final Function function = new Function(FUNC_SUBBALANCE, 
                Arrays.<Type>asList(new Uint256(num)),
                Arrays.<TypeReference<?>>asList());
        return function;
    }

    public String getSignedTransactionForSubBalance(BigInteger num) {
        final Function function = new Function(
                FUNC_SUBBALANCE, 
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public String subBalance(BigInteger num, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_SUBBALANCE, 
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple1<BigInteger> getSubBalanceInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SUBBALANCE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
                );
    }

    public TransactionReceipt takeShare(BigInteger num, Boolean allowRevert) {
        final Function function = new Function(
                FUNC_TAKESHARE, 
                Arrays.<Type>asList(new Uint256(num),
                new Bool(allowRevert)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public Function getMethodTakeShareRawFunction(BigInteger num, Boolean allowRevert) throws
            ContractException {
        final Function function = new Function(FUNC_TAKESHARE, 
                Arrays.<Type>asList(new Uint256(num),
                new Bool(allowRevert)),
                Arrays.<TypeReference<?>>asList());
        return function;
    }

    public String getSignedTransactionForTakeShare(BigInteger num, Boolean allowRevert) {
        final Function function = new Function(
                FUNC_TAKESHARE, 
                Arrays.<Type>asList(new Uint256(num),
                new Bool(allowRevert)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public String takeShare(BigInteger num, Boolean allowRevert, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_TAKESHARE, 
                Arrays.<Type>asList(new Uint256(num),
                new Bool(allowRevert)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple2<BigInteger, Boolean> getTakeShareInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_TAKESHARE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Bool>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, Boolean>(

                (BigInteger) results.get(0).getValue(), 
                (Boolean) results.get(1).getValue()
                );
    }

    public static DmcTransfer load(String contractAddress, Client client,
            CryptoKeyPair credential) {
        return new DmcTransfer(contractAddress, client, credential);
    }

    public static DmcTransfer deploy(Client client, CryptoKeyPair credential) throws
            ContractException {
        return deploy(DmcTransfer.class, client, credential, getBinary(client.getCryptoSuite()), getABI(), null, null);
    }
}
