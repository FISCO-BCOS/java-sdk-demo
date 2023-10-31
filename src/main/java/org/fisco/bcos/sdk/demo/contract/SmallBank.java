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
public class SmallBank extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50610631806100206000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c80630b488b37146100675780630be8374d1461007c5780633a51d2461461008f578063870187eb146100b4578063901d706f146100c7578063ca305435146100da575b600080fd5b61007a610075366004610430565b6100ed565b005b61007a61008a366004610430565b610141565b6100a261009d366004610475565b610206565b60405190815260200160405180910390f35b61007a6100c2366004610430565b610260565b61007a6100d53660046104aa565b61029f565b61007a6100e836600461050e565b610320565b600080836040516100fe919061057b565b9081526040519081900360200190205490508161011b81836105cc565b60008560405161012b919061057b565b9081526040519081900360200190205550505050565b6000600183604051610153919061057b565b908152602001604051809103902054905060008084604051610175919061057b565b9081526040519081900360200190205490508261019282846105cc565b8110156101d45760016101a582856105e4565b6101af91906105e4565b6001866040516101bf919061057b565b908152604051908190036020019020556101ff565b6101de81846105e4565b6001866040516101ee919061057b565b908152604051908190036020019020555b5050505050565b600080600083604051610219919061057b565b9081526020016040518091039020549050600060018460405161023c919061057b565b90815260405190819003602001902054905061025881836105cc565b949350505050565b6000600183604051610272919061057b565b9081526040519081900360200190205490508161028f81836105cc565b60018560405161012b919061057b565b600080836040516102b0919061057b565b908152602001604051809103902054905060006001836040516102d3919061057b565b908152602001604051809103902054905060006001856040516102f6919061057b565b9081526040519081900360200190205561031081836105cc565b60008460405161012b919061057b565b80600184604051610331919061057b565b9081526020016040518091039020600082825461034e91906105e4565b9250508190555080600183604051610366919061057b565b9081526020016040518091039020600082825461038391906105cc565b9091555050505050565b634e487b7160e01b600052604160045260246000fd5b600082601f8301126103b457600080fd5b813567ffffffffffffffff808211156103cf576103cf61038d565b604051601f8301601f19908116603f011681019082821181831017156103f7576103f761038d565b8160405283815286602085880101111561041057600080fd5b836020870160208301376000602085830101528094505050505092915050565b6000806040838503121561044357600080fd5b823567ffffffffffffffff81111561045a57600080fd5b610466858286016103a3565b95602094909401359450505050565b60006020828403121561048757600080fd5b813567ffffffffffffffff81111561049e57600080fd5b610258848285016103a3565b600080604083850312156104bd57600080fd5b823567ffffffffffffffff808211156104d557600080fd5b6104e1868387016103a3565b935060208501359150808211156104f757600080fd5b50610504858286016103a3565b9150509250929050565b60008060006060848603121561052357600080fd5b833567ffffffffffffffff8082111561053b57600080fd5b610547878388016103a3565b9450602086013591508082111561055d57600080fd5b5061056a868287016103a3565b925050604084013590509250925092565b6000825160005b8181101561059c5760208186018101518583015201610582565b818111156105ab576000828501525b509190910192915050565b634e487b7160e01b600052601160045260246000fd5b600082198211156105df576105df6105b6565b500190565b6000828210156105f6576105f66105b6565b50039056fea2646970667358221220aeddb57ccf670523b3dcc3d5f28cd0ac685f3e20cdae248ac18b80158fac35bf64736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50610631806100206000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c8063505deccf14610067578063563a765f1461008c578063654d1165146100a1578063b484c3e9146100b4578063d5023931146100c7578063f977b272146100da575b600080fd5b61007a610075366004610430565b6100ed565b60405190815260200160405180910390f35b61009f61009a366004610465565b610147565b005b61009f6100af3660046104d2565b6101b4565b61009f6100c2366004610536565b61024b565b61009f6100d5366004610536565b610310565b61009f6100e8366004610536565b61034e565b600080600083604051610100919061057b565b90815260200160405180910390205490506000600184604051610123919061057b565b90815260405190819003602001902054905061013f81836105cc565b949350505050565b80600184604051610158919061057b565b9081526020016040518091039020600082825461017591906105e4565b925050819055508060018360405161018d919061057b565b908152602001604051809103902060008282546101aa91906105cc565b9091555050505050565b600080836040516101c5919061057b565b908152602001604051809103902054905060006001836040516101e8919061057b565b9081526020016040518091039020549050600060018560405161020b919061057b565b9081526040519081900360200190205561022581836105cc565b600084604051610235919061057b565b9081526040519081900360200190205550505050565b600060018360405161025d919061057b565b90815260200160405180910390205490506000808460405161027f919061057b565b9081526040519081900360200190205490508261029c82846105cc565b8110156102de5760016102af82856105e4565b6102b991906105e4565b6001866040516102c9919061057b565b90815260405190819003602001902055610309565b6102e881846105e4565b6001866040516102f8919061057b565b908152604051908190036020019020555b5050505050565b60008083604051610321919061057b565b9081526040519081900360200190205490508161033e81836105cc565b600085604051610235919061057b565b6000600183604051610360919061057b565b9081526040519081900360200190205490508161037d81836105cc565b600185604051610235919061057b565b63b95aa35560e01b600052604160045260246000fd5b600082601f8301126103b457600080fd5b813567ffffffffffffffff808211156103cf576103cf61038d565b604051601f8301601f19908116603f011681019082821181831017156103f7576103f761038d565b8160405283815286602085880101111561041057600080fd5b836020870160208301376000602085830101528094505050505092915050565b60006020828403121561044257600080fd5b813567ffffffffffffffff81111561045957600080fd5b61013f848285016103a3565b60008060006060848603121561047a57600080fd5b833567ffffffffffffffff8082111561049257600080fd5b61049e878388016103a3565b945060208601359150808211156104b457600080fd5b506104c1868287016103a3565b925050604084013590509250925092565b600080604083850312156104e557600080fd5b823567ffffffffffffffff808211156104fd57600080fd5b610509868387016103a3565b9350602085013591508082111561051f57600080fd5b5061052c858286016103a3565b9150509250929050565b6000806040838503121561054957600080fd5b823567ffffffffffffffff81111561056057600080fd5b61056c858286016103a3565b95602094909401359450505050565b6000825160005b8181101561059c5760208186018101518583015201610582565b818111156105ab576000828501525b509190910192915050565b63b95aa35560e01b600052601160045260246000fd5b600082198211156105df576105df6105b6565b500190565b6000828210156105f6576105f66105b6565b50039056fea2646970667358221220609a10f3bb3929ee946ebc0e7a4af5cb308a4a64e5a2be9d521c3bac10d7d20664736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"arg1\",\"type\":\"string\"}],\"name\":\"almagate\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"}],\"name\":\"getBalance\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"balance\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"arg1\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"arg2\",\"type\":\"uint256\"}],\"name\":\"sendPayment\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"arg1\",\"type\":\"uint256\"}],\"name\":\"updateBalance\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"arg1\",\"type\":\"uint256\"}],\"name\":\"updateSaving\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"arg1\",\"type\":\"uint256\"}],\"name\":\"writeCheck\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String[] ABI_ARRAY_PARALLEL = {
        "[{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]},{\"kind\":3,\"slot\":0,\"value\":[1]},{\"kind\":3,\"slot\":1,\"value\":[0]},{\"kind\":3,\"slot\":1,\"value\":[1]}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"arg1\",\"type\":\"string\"}],\"name\":\"almagate\",\"outputs\":[],\"selector\":[2417848431,1699549541],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]},{\"kind\":3,\"slot\":1,\"value\":[0]}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"}],\"name\":\"getBalance\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"balance\",\"type\":\"uint256\"}],\"selector\":[978440774,1348332751],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":1,\"value\":[0]},{\"kind\":3,\"slot\":1,\"value\":[1]}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"arg1\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"arg2\",\"type\":\"uint256\"}],\"name\":\"sendPayment\",\"outputs\":[],\"selector\":[3392164917,1446671967],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":1,\"value\":[0]}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"arg1\",\"type\":\"uint256\"}],\"name\":\"updateBalance\",\"outputs\":[],\"selector\":[2265024491,4185371250],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"arg1\",\"type\":\"uint256\"}],\"name\":\"updateSaving\",\"outputs\":[],\"selector\":[189303607,3573692721],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]},{\"kind\":3,\"slot\":1,\"value\":[0]}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"arg0\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"arg1\",\"type\":\"uint256\"}],\"name\":\"writeCheck\",\"outputs\":[],\"selector\":[199767885,3028599785],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);
    public static final String PARALLEL_ABI =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY_PARALLEL);

    public static final String FUNC_ALMAGATE = "almagate";

    public static final String FUNC_GETBALANCE = "getBalance";

    public static final String FUNC_SENDPAYMENT = "sendPayment";

    public static final String FUNC_UPDATEBALANCE = "updateBalance";

    public static final String FUNC_UPDATESAVING = "updateSaving";

    public static final String FUNC_WRITECHECK = "writeCheck";

    protected SmallBank(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI(boolean isParallel) {
        return isParallel ? PARALLEL_ABI : ABI;
    }

    public TransactionReceipt almagate(String arg0, String arg1) {
        final Function function =
                new Function(
                        FUNC_ALMAGATE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg1)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return executeTransaction(function);
    }

    public String almagate(String arg0, String arg1, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_ALMAGATE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg1)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForAlmagate(String arg0, String arg1) {
        final Function function =
                new Function(
                        FUNC_ALMAGATE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg1)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return createSignedTransaction(function);
    }

    public Tuple2<String, String> getAlmagateInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_ALMAGATE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, String>(
                (String) results.get(0).getValue(), (String) results.get(1).getValue());
    }

    public BigInteger getBalance(String arg0) throws ContractException {
        final Function function =
                new Function(
                        FUNC_GETBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public void getBalance(String arg0, CallCallback callback) throws ContractException {
        final Function function =
                new Function(
                        FUNC_GETBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        asyncExecuteCall(function, callback);
    }

    public TransactionReceipt sendPayment(String arg0, String arg1, BigInteger arg2) {
        final Function function =
                new Function(
                        FUNC_SENDPAYMENT,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg1),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(arg2)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return executeTransaction(function);
    }

    public String sendPayment(
            String arg0, String arg1, BigInteger arg2, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SENDPAYMENT,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg1),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(arg2)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSendPayment(String arg0, String arg1, BigInteger arg2) {
        final Function function =
                new Function(
                        FUNC_SENDPAYMENT,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg1),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(arg2)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
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
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(arg1)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return executeTransaction(function);
    }

    public String updateBalance(String arg0, BigInteger arg1, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_UPDATEBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(arg1)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForUpdateBalance(String arg0, BigInteger arg1) {
        final Function function =
                new Function(
                        FUNC_UPDATEBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(arg1)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
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

    public TransactionReceipt updateSaving(String arg0, BigInteger arg1) {
        final Function function =
                new Function(
                        FUNC_UPDATESAVING,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(arg1)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return executeTransaction(function);
    }

    public String updateSaving(String arg0, BigInteger arg1, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_UPDATESAVING,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(arg1)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForUpdateSaving(String arg0, BigInteger arg1) {
        final Function function =
                new Function(
                        FUNC_UPDATESAVING,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(arg1)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return createSignedTransaction(function);
    }

    public Tuple2<String, BigInteger> getUpdateSavingInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_UPDATESAVING,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Uint256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(
                (String) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    public TransactionReceipt writeCheck(String arg0, BigInteger arg1) {
        final Function function =
                new Function(
                        FUNC_WRITECHECK,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(arg1)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return executeTransaction(function);
    }

    public String writeCheck(String arg0, BigInteger arg1, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_WRITECHECK,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(arg1)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForWriteCheck(String arg0, BigInteger arg1) {
        final Function function =
                new Function(
                        FUNC_WRITECHECK,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(arg0),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(arg1)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return createSignedTransaction(function);
    }

    public Tuple2<String, BigInteger> getWriteCheckInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_WRITECHECK,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Uint256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(
                (String) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    public static SmallBank load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new SmallBank(contractAddress, client, credential);
    }

    public static SmallBank deploy(Client client, CryptoKeyPair credential, boolean isParallel)
            throws ContractException {
        return deploy(
                SmallBank.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(isParallel),
                null,
                null);
    }
}
