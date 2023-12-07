package org.fisco.bcos.sdk.demo.contract;

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
import org.fisco.bcos.sdk.v3.model.callback.CallCallback;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class ParallelOk extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b5061042d806100206000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c806335ee5f87146100515780638a42ebe9146100765780639b80b0501461008b578063fad42f871461009e575b600080fd5b61006461005f366004610288565b6100b1565b60405190815260200160405180910390f35b6100896100843660046102c5565b6100d8565b005b61008961009936600461030a565b6100fd565b6100896100ac36600461030a565b61016a565b600080826040516100c29190610377565b9081526020016040518091039020549050919050565b806000836040516100e99190610377565b908152604051908190036020019020555050565b8060008460405161010e9190610377565b9081526020016040518091039020600082825461012b91906103c8565b92505081905550806000836040516101439190610377565b9081526020016040518091039020600082825461016091906103df565b9091555050505050565b8060008460405161017b9190610377565b9081526020016040518091039020600082825461019891906103c8565b92505081905550806000836040516101b09190610377565b908152602001604051809103902060008282546101cd91906103df565b909155505060648111156101e057600080fd5b505050565b634e487b7160e01b600052604160045260246000fd5b600082601f83011261020c57600080fd5b813567ffffffffffffffff80821115610227576102276101e5565b604051601f8301601f19908116603f0116810190828211818310171561024f5761024f6101e5565b8160405283815286602085880101111561026857600080fd5b836020870160208301376000602085830101528094505050505092915050565b60006020828403121561029a57600080fd5b813567ffffffffffffffff8111156102b157600080fd5b6102bd848285016101fb565b949350505050565b600080604083850312156102d857600080fd5b823567ffffffffffffffff8111156102ef57600080fd5b6102fb858286016101fb565b95602094909401359450505050565b60008060006060848603121561031f57600080fd5b833567ffffffffffffffff8082111561033757600080fd5b610343878388016101fb565b9450602086013591508082111561035957600080fd5b50610366868287016101fb565b925050604084013590509250925092565b6000825160005b81811015610398576020818601810151858301520161037e565b818111156103a7576000828501525b509190910192915050565b634e487b7160e01b600052601160045260246000fd5b6000828210156103da576103da6103b2565b500390565b600082198211156103f2576103f26103b2565b50019056fea264697066735822122069589064e2e7be05f631d130c81bf75bb07e0e944316834159b50300458cd32464736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b5061042d806100206000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c8063612d2bff14610051578063ab71bf0914610066578063cd93c25d14610079578063f2f4ee6d1461009e575b600080fd5b61006461005f366004610288565b6100b1565b005b610064610074366004610288565b61011e565b61008c6100873660046102f5565b610199565b60405190815260200160405180910390f35b6100646100ac366004610332565b6101c0565b806000846040516100c29190610377565b908152602001604051809103902060008282546100df91906103c8565b92505081905550806000836040516100f79190610377565b9081526020016040518091039020600082825461011491906103df565b9091555050505050565b8060008460405161012f9190610377565b9081526020016040518091039020600082825461014c91906103c8565b92505081905550806000836040516101649190610377565b9081526020016040518091039020600082825461018191906103df565b9091555050606481111561019457600080fd5b505050565b600080826040516101aa9190610377565b9081526020016040518091039020549050919050565b806000836040516101d19190610377565b908152604051908190036020019020555050565b63b95aa35560e01b600052604160045260246000fd5b600082601f83011261020c57600080fd5b813567ffffffffffffffff80821115610227576102276101e5565b604051601f8301601f19908116603f0116810190828211818310171561024f5761024f6101e5565b8160405283815286602085880101111561026857600080fd5b836020870160208301376000602085830101528094505050505092915050565b60008060006060848603121561029d57600080fd5b833567ffffffffffffffff808211156102b557600080fd5b6102c1878388016101fb565b945060208601359150808211156102d757600080fd5b506102e4868287016101fb565b925050604084013590509250925092565b60006020828403121561030757600080fd5b813567ffffffffffffffff81111561031e57600080fd5b61032a848285016101fb565b949350505050565b6000806040838503121561034557600080fd5b823567ffffffffffffffff81111561035c57600080fd5b610368858286016101fb565b95602094909401359450505050565b6000825160005b81811015610398576020818601810151858301520161037e565b818111156103a7576000828501525b509190910192915050565b63b95aa35560e01b600052601160045260246000fd5b6000828210156103da576103da6103b2565b500390565b600082198211156103f2576103f26103b2565b50019056fea264697066735822122017b8fba41d885b84d2439faacd056921503c095aca35a78aced403a78eaa01d564736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY_PARALLEL = {
        "[{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"name\",\"type\":\"string\"}],\"name\":\"balanceOf\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"selector\":[904814471,3449012829],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"name\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"selector\":[2319641577,4076138093],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]},{\"kind\":3,\"slot\":0,\"value\":[1]}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"from\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"to\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[],\"selector\":[2608902224,1630350335],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]},{\"kind\":3,\"slot\":0,\"value\":[1]}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"from\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"to\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"transferWithRevert\",\"outputs\":[],\"selector\":[4208209799,2876358409],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String[] ABI_ARRAY = {
        "[{\"constant\":false,\"inputs\":[{\"name\":\"functionName\",\"type\":\"string\"},{\"name\":\"criticalSize\",\"type\":\"uint256\"}],\"name\":\"registerParallelFunction\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"functionName\",\"type\":\"string\"}],\"name\":\"unregisterParallelFunction\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"from\",\"type\":\"string\"},{\"name\":\"to\",\"type\":\"string\"},{\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"enableParallel\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"disableParallel\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"from\",\"type\":\"string\"},{\"name\":\"to\",\"type\":\"string\"},{\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"transferWithRevert\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);
    public static final String PARALLEL_ABI =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY_PARALLEL);

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_SET = "set";

    public static final String FUNC_TRANSFER = "transfer";

    public static final String FUNC_TRANSFERWITHREVERT = "transferWithRevert";

    protected ParallelOk(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI(boolean isParallel) {
        return isParallel ? PARALLEL_ABI : ABI;
    }

    public BigInteger balanceOf(String name) throws ContractException {
        final Function function =
                new Function(
                        FUNC_BALANCEOF,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(name)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public void balanceOf(String name, CallCallback callback) throws ContractException {
        final Function function =
                new Function(
                        FUNC_BALANCEOF,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(name)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        asyncExecuteCall(function, callback);
    }

    public TransactionReceipt set(String name, BigInteger num) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(name),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return executeTransaction(function);
    }

    public String getSignedTransactionForSet(String name, BigInteger num) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(name),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return createSignedTransaction(function);
    }

    public String set(String name, BigInteger num, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(name),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple2<String, BigInteger> getSetInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Uint256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(
                (String) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    public TransactionReceipt transfer(String from, String to, BigInteger num) {
        final Function function =
                new Function(
                        FUNC_TRANSFER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(from),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(to),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return executeTransaction(function);
    }

    public String getSignedTransactionForTransfer(String from, String to, BigInteger num) {
        final Function function =
                new Function(
                        FUNC_TRANSFER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(from),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(to),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return createSignedTransaction(function);
    }

    public String transfer(String from, String to, BigInteger num, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_TRANSFER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(from),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(to),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple3<String, String, BigInteger> getTransferInput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_TRANSFER,
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

    public TransactionReceipt transferWithRevert(String from, String to, BigInteger num) {
        final Function function =
                new Function(
                        FUNC_TRANSFERWITHREVERT,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(from),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(to),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return executeTransaction(function);
    }

    public String getSignedTransactionForTransferWithRevert(
            String from, String to, BigInteger num) {
        final Function function =
                new Function(
                        FUNC_TRANSFERWITHREVERT,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(from),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(to),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return createSignedTransaction(function);
    }

    public String transferWithRevert(
            String from, String to, BigInteger num, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_TRANSFERWITHREVERT,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(from),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(to),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(num)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple3<String, String, BigInteger> getTransferWithRevertInput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_TRANSFERWITHREVERT,
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

    public static ParallelOk load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new ParallelOk(contractAddress, client, credential);
    }

    public static ParallelOk deploy(Client client, CryptoKeyPair credential, boolean isParallel)
            throws ContractException {
        return deploy(
                ParallelOk.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(isParallel),
                null,
                null);
    }
}
