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
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class ParallelOk extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b506107ab806100206000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c806335ee5f87146100515780638a42ebe9146101205780639b80b050146101e5578063fad42f8714610341575b600080fd5b61010a6004803603602081101561006757600080fd5b810190808035906020019064010000000081111561008457600080fd5b82018360208201111561009657600080fd5b803590602001918460018302840111640100000000831117156100b857600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929050505061049d565b6040518082815260200191505060405180910390f35b6101e36004803603604081101561013657600080fd5b810190808035906020019064010000000081111561015357600080fd5b82018360208201111561016557600080fd5b8035906020019184600183028401116401000000008311171561018757600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192908035906020019092919050505061050f565b005b61033f600480360360608110156101fb57600080fd5b810190808035906020019064010000000081111561021857600080fd5b82018360208201111561022a57600080fd5b8035906020019184600183028401116401000000008311171561024c57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803590602001906401000000008111156102af57600080fd5b8201836020820111156102c157600080fd5b803590602001918460018302840111640100000000831117156102e357600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929080359060200190929190505050610581565b005b61049b6004803603606081101561035757600080fd5b810190808035906020019064010000000081111561037457600080fd5b82018360208201111561038657600080fd5b803590602001918460018302840111640100000000831117156103a857600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192908035906020019064010000000081111561040b57600080fd5b82018360208201111561041d57600080fd5b8035906020019184600183028401116401000000008311171561043f57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929080359060200190929190505050610674565b005b600080826040518082805190602001908083835b602083106104d457805182526020820191506020810190506020830392506104b1565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020549050919050565b806000836040518082805190602001908083835b602083106105465780518252602082019150602081019050602083039250610523565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020819055505050565b806000846040518082805190602001908083835b602083106105b85780518252602082019150602081019050602083039250610595565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008282540392505081905550806000836040518082805190602001908083835b6020831061062f578051825260208201915060208101905060208303925061060c565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008282540192505081905550505050565b806000846040518082805190602001908083835b602083106106ab5780518252602082019150602081019050602083039250610688565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008282540392505081905550806000836040518082805190602001908083835b6020831061072257805182526020820191506020810190506020830392506106ff565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008282540192505081905550606481111561077057600080fd5b50505056fea2646970667358221220ec4d391e132cdd0dc5d04126372dea72a672163f1a860340ab2112d8ee7090ab64736f6c634300060a0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b506107ab806100206000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c8063612d2bff14610051578063ab71bf09146101ad578063cd93c25d14610309578063f2f4ee6d146103d8575b600080fd5b6101ab6004803603606081101561006757600080fd5b810190808035906020019064010000000081111561008457600080fd5b82018360208201111561009657600080fd5b803590602001918460018302840111640100000000831117156100b857600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192908035906020019064010000000081111561011b57600080fd5b82018360208201111561012d57600080fd5b8035906020019184600183028401116401000000008311171561014f57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192908035906020019092919050505061049d565b005b610307600480360360608110156101c357600080fd5b81019080803590602001906401000000008111156101e057600080fd5b8201836020820111156101f257600080fd5b8035906020019184600183028401116401000000008311171561021457600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192908035906020019064010000000081111561027757600080fd5b82018360208201111561028957600080fd5b803590602001918460018302840111640100000000831117156102ab57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929080359060200190929190505050610590565b005b6103c26004803603602081101561031f57600080fd5b810190808035906020019064010000000081111561033c57600080fd5b82018360208201111561034e57600080fd5b8035906020019184600183028401116401000000008311171561037057600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610691565b6040518082815260200191505060405180910390f35b61049b600480360360408110156103ee57600080fd5b810190808035906020019064010000000081111561040b57600080fd5b82018360208201111561041d57600080fd5b8035906020019184600183028401116401000000008311171561043f57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929080359060200190929190505050610703565b005b806000846040518082805190602001908083835b602083106104d457805182526020820191506020810190506020830392506104b1565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008282540392505081905550806000836040518082805190602001908083835b6020831061054b5780518252602082019150602081019050602083039250610528565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008282540192505081905550505050565b806000846040518082805190602001908083835b602083106105c757805182526020820191506020810190506020830392506105a4565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008282540392505081905550806000836040518082805190602001908083835b6020831061063e578051825260208201915060208101905060208303925061061b565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060008282540192505081905550606481111561068c57600080fd5b505050565b600080826040518082805190602001908083835b602083106106c857805182526020820191506020810190506020830392506106a5565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020549050919050565b806000836040518082805190602001908083835b6020831061073a5780518252602082019150602081019050602083039250610717565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902081905550505056fea2646970667358221220d893a138455d2d1e001a6c79e7dbb4efa121d71e0009a7c00e0f87b98ff7de5c64736f6c634300060a0033"
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
