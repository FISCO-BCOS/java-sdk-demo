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
public class Economic extends Contract {
    public static final String[] BINARY_ARRAY = {
        "6080604052600080546001600160a01b031990811661101117909155600180549091166201000117905534801561003557600080fd5b50600280546001600160a01b03191633179055610aff806100576000396000f3fe608060405234801561001057600080fd5b50600436106100885760003560e01c8063a3ffa9cd1161005b578063a3ffa9cd146100da578063a9f2b9a8146100ed578063caf39c5114610102578063f8b2cb4f1461011557600080fd5b80632f2770db1461008d5780634705532114610097578063867bde5d146100aa578063a3907d71146100d2575b600080fd5b610095610136565b005b6100956100a5366004610959565b61024a565b6100bd6100b8366004610983565b610302565b60405190151581526020015b60405180910390f35b6100956103c9565b6100956100e8366004610959565b6104a8565b6100f561052e565b6040516100c9919061099e565b6100bd610110366004610983565b6105fb565b610128610123366004610983565b610659565b6040519081526020016100c9565b600154604051631c86b03760e31b81523360048201526001600160a01b039091169063e43581b890602401602060405180830381865afa15801561017e573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906101a291906109eb565b6101ea5760405162461bcd60e51b81526020600482015260146024820152733cb7ba9036bab9ba1031329033b7bb32b93737b960611b60448201526064015b60405180910390fd5b60005460405163afb4bfbd60e01b81523060048201526001600160a01b039091169063afb4bfbd906024015b600060405180830381600087803b15801561023057600080fd5b505af1158015610244573d6000803e3d6000fd5b50505050565b6102556003336106c8565b6102975760405162461bcd60e51b81526020600482015260136024820152723cb7ba9036bab9ba1031329031b430b933b2b960691b60448201526064016101e1565b6000546040516367c775bf60e11b81526001600160a01b038481166004830152602482018490529091169063cf8eeb7e906044015b600060405180830381600087803b1580156102e657600080fd5b505af11580156102fa573d6000803e3d6000fd5b505050505050565b6002546000906001600160a01b031633146103535760405162461bcd60e51b81526020600482015260116024820152703cb7ba9036bab9ba1031329037bbb732b960791b60448201526064016101e1565b61035e6003836106c8565b6103b65760405162461bcd60e51b815260206004820152602360248201527f6368617267657220686173206e6f74206265656e206772616e746564206265666044820152626f726560e81b60648201526084016101e1565b6103c16003836106ea565b90505b919050565b600154604051631c86b03760e31b81523360048201526001600160a01b039091169063e43581b890602401602060405180830381865afa158015610411573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061043591906109eb565b6104785760405162461bcd60e51b81526020600482015260146024820152733cb7ba9036bab9ba1031329033b7bb32b93737b960611b60448201526064016101e1565b600054604051631dcd196f60e11b81523060048201526001600160a01b0390911690633b9a32de90602401610216565b6104b36003336106c8565b6104f55760405162461bcd60e51b81526020600482015260136024820152723cb7ba9036bab9ba1031329031b430b933b2b960691b60448201526064016101e1565b6000546040516310f29c1d60e11b81526001600160a01b03848116600483015260248201849052909116906321e5383a906044016102cc565b6060600060036002015467ffffffffffffffff81111561055057610550610a0d565b604051908082528060200260200182016040528015610579578160200160208202803683370190505b5090506000806105896003610798565b90505b6004548110156105f35760006105a36003836107a5565b509050808484815181106105b9576105b9610a23565b6001600160a01b0390921660209283029190910190910152826105db81610a4f565b93506105ec915060039050826107f6565b905061058c565b509092915050565b6002546000906001600160a01b0316331461064c5760405162461bcd60e51b81526020600482015260116024820152703cb7ba9036bab9ba1031329037bbb732b960791b60448201526064016101e1565b6103c16003836001610813565b6000805460405163f8b2cb4f60e01b81526001600160a01b0384811660048301529091169063f8b2cb4f90602401602060405180830381865afa1580156106a4573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906103c19190610a6a565b6001600160a01b03811660009081526020839052604090205415155b92915050565b6001600160a01b038116600090815260208390526040812054806107125760009150506106e4565b6001600160a01b03831660009081526020859052604081209081556001908101805460ff191690558085016107478284610a83565b8154811061075757610757610a23565b600091825260208220018054921515600160a01b0260ff60a01b19909316929092179091556002850180549161078c83610a9a565b91905055505092915050565b60006103c18260006108e8565b60008060008390508460010181815481106107c2576107c2610a23565b60009182526020808320909101546001600160a01b03168083529690526040902060010154949560ff909516949350505050565b600061080c83610807846001610ab1565b6108e8565b9392505050565b6001600160a01b038216600090815260208490526040812080546001909101805460ff1916841515179055801561084e57600191505061080c565b506001808501805480830182556000919091529061086d908290610ab1565b6001600160a01b0385166000908152602087905260409020556001850180548591908390811061089f5761089f610a23565b6000918252602082200180546001600160a01b0319166001600160a01b039390931692909217909155600286018054916108d883610a4f565b9091555060009695505050505050565b60005b600183015482108015610925575082600101828154811061090e5761090e610a23565b600091825260209091200154600160a01b900460ff165b1561093c578161093481610a4f565b9250506108eb565b50919050565b80356001600160a01b03811681146103c457600080fd5b6000806040838503121561096c57600080fd5b61097583610942565b946020939093013593505050565b60006020828403121561099557600080fd5b61080c82610942565b6020808252825182820181905260009190848201906040850190845b818110156109df5783516001600160a01b0316835292840192918401916001016109ba565b50909695505050505050565b6000602082840312156109fd57600080fd5b8151801515811461080c57600080fd5b634e487b7160e01b600052604160045260246000fd5b634e487b7160e01b600052603260045260246000fd5b634e487b7160e01b600052601160045260246000fd5b6000600019821415610a6357610a63610a39565b5060010190565b600060208284031215610a7c57600080fd5b5051919050565b600082821015610a9557610a95610a39565b500390565b600081610aa957610aa9610a39565b506000190190565b60008219821115610ac457610ac4610a39565b50019056fea2646970667358221220ef0c9d7407141542168afaae63b557848171174bab4a957f72d5b6063292c57f64736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "6080604052600080546001600160a01b031990811661101117909155600180549091166201000117905534801561003557600080fd5b50600280546001600160a01b03191633179055610b01806100576000396000f3fe608060405234801561001057600080fd5b50600436106100885760003560e01c806390b56c0c1161005b57806390b56c0c146100f3578063a06cc6ae146100fb578063c74b68d914610110578063ec5975031461012357600080fd5b806319c7c71b1461008d5780633009a33c146100975780633f1baf84146100bf578063790162d7146100e0575b600080fd5b610095610136565b005b6100aa6100a536600461095b565b61024b565b60405190151581526020015b60405180910390f35b6100d26100cd36600461095b565b6102b2565b6040519081526020016100b6565b6100956100ee366004610976565b610321565b6100956103da565b6101036104ba565b6040516100b691906109a0565b61009561011e366004610976565b610587565b6100aa61013136600461095b565b61060e565b60015460405163b5f2323560e01b81523360048201526001600160a01b039091169063b5f2323590602401602060405180830381865afa15801561017e573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906101a291906109ed565b6101eb57604051636381e58960e11b81526020600482015260146024820152733cb7ba9036bab9ba1031329033b7bb32b93737b960611b60448201526064015b60405180910390fd5b6000546040516352e9076b60e11b81523060048201526001600160a01b039091169063a5d20ed6906024015b600060405180830381600087803b15801561023157600080fd5b505af1158015610245573d6000803e3d6000fd5b50505050565b6002546000906001600160a01b0316331461029d57604051636381e58960e11b81526020600482015260116024820152703cb7ba9036bab9ba1031329037bbb732b960791b60448201526064016101e2565b6102aa60038360016106cf565b90505b919050565b60008054604051630fc6ebe160e21b81526001600160a01b03848116600483015290911690633f1baf8490602401602060405180830381865afa1580156102fd573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906102aa9190610a0f565b61032c6003336107a6565b61036f57604051636381e58960e11b81526020600482015260136024820152723cb7ba9036bab9ba1031329031b430b933b2b960691b60448201526064016101e2565b6000546040516379bf5daf60e01b81526001600160a01b03848116600483015260248201849052909116906379bf5daf906044015b600060405180830381600087803b1580156103be57600080fd5b505af11580156103d2573d6000803e3d6000fd5b505050505050565b60015460405163b5f2323560e01b81523360048201526001600160a01b039091169063b5f2323590602401602060405180830381865afa158015610422573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061044691906109ed565b61048a57604051636381e58960e11b81526020600482015260146024820152733cb7ba9036bab9ba1031329033b7bb32b93737b960611b60448201526064016101e2565b60005460405163b628e93560e01b81523060048201526001600160a01b039091169063b628e93590602401610217565b6060600060036002015467ffffffffffffffff8111156104dc576104dc610a28565b604051908082528060200260200182016040528015610505578160200160208202803683370190505b50905060008061051560036107c8565b90505b60045481101561057f57600061052f6003836107d5565b5090508084848151811061054557610545610a3e565b6001600160a01b03909216602092830291909101909101528261056781610a6a565b935061057891506003905082610826565b9050610518565b509092915050565b6105926003336107a6565b6105d557604051636381e58960e11b81526020600482015260136024820152723cb7ba9036bab9ba1031329031b430b933b2b960691b60448201526064016101e2565b600054604051633d88a01f60e11b81526001600160a01b0384811660048301526024820184905290911690637b11403e906044016103a4565b6002546000906001600160a01b0316331461066057604051636381e58960e11b81526020600482015260116024820152703cb7ba9036bab9ba1031329037bbb732b960791b60448201526064016101e2565b61066b6003836107a6565b6106c457604051636381e58960e11b815260206004820152602360248201527f6368617267657220686173206e6f74206265656e206772616e746564206265666044820152626f726560e81b60648201526084016101e2565b6102aa60038361083c565b6001600160a01b038216600090815260208490526040812080546001909101805460ff1916841515179055801561070a57600191505061079f565b5060018085018054808301825560009190915290610729908290610a85565b6001600160a01b0385166000908152602087905260409020556001850180548591908390811061075b5761075b610a3e565b6000918252602082200180546001600160a01b0319166001600160a01b0393909316929092179091556002860180549161079483610a6a565b919050555060009150505b9392505050565b6001600160a01b03811660009081526020839052604090205415155b92915050565b60006102aa8260006108ea565b60008060008390508460010181815481106107f2576107f2610a3e565b60009182526020808320909101546001600160a01b03168083529690526040902060010154949560ff909516949350505050565b600061079f83610837846001610a85565b6108ea565b6001600160a01b038116600090815260208390526040812054806108645760009150506107c2565b6001600160a01b03831660009081526020859052604081209081556001908101805460ff191690558085016108998284610a9d565b815481106108a9576108a9610a3e565b600091825260208220018054921515600160a01b0260ff60a01b1990931692909217909155600285018054916108de83610ab4565b91905055505092915050565b60005b600183015482108015610927575082600101828154811061091057610910610a3e565b600091825260209091200154600160a01b900460ff165b1561093e578161093681610a6a565b9250506108ed565b50919050565b80356001600160a01b03811681146102ad57600080fd5b60006020828403121561096d57600080fd5b61079f82610944565b6000806040838503121561098957600080fd5b61099283610944565b946020939093013593505050565b6020808252825182820181905260009190848201906040850190845b818110156109e15783516001600160a01b0316835292840192918401916001016109bc565b50909695505050505050565b6000602082840312156109ff57600080fd5b8151801515811461079f57600080fd5b600060208284031215610a2157600080fd5b5051919050565b63b95aa35560e01b600052604160045260246000fd5b63b95aa35560e01b600052603260045260246000fd5b63b95aa35560e01b600052601160045260246000fd5b6000600019821415610a7e57610a7e610a54565b5060010190565b60008219821115610a9857610a98610a54565b500190565b600082821015610aaf57610aaf610a54565b500390565b600081610ac357610ac3610a54565b50600019019056fea2646970667358221220b8176d13aa5c82f59649619081305c970e9685dcdec1e281a013914922dcba8464736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"inputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"address\",\"name\":\"userAccount\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"gasValue\",\"type\":\"uint256\"}],\"name\":\"charge\",\"outputs\":[],\"selector\":[2751441357,3343608025],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"address\",\"name\":\"userAccount\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"gasValue\",\"type\":\"uint256\"}],\"name\":\"deduct\",\"outputs\":[],\"selector\":[1191531297,2030133975],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[],\"name\":\"disable\",\"outputs\":[],\"selector\":[791113947,2427808780],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[],\"name\":\"enable\",\"outputs\":[],\"selector\":[2744155505,432523035],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"address\",\"name\":\"userAccount\",\"type\":\"address\"}],\"name\":\"getBalance\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"selector\":[4172467023,1058779012],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0},{\"kind\":4,\"value\":[2]}],\"inputs\":[{\"internalType\":\"address\",\"name\":\"chargerAccount\",\"type\":\"address\"}],\"name\":\"grantCharger\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"success\",\"type\":\"bool\"}],\"selector\":[3404962897,805937980],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0},{\"kind\":4,\"value\":[4]},{\"kind\":4,\"value\":[5]}],\"inputs\":[],\"name\":\"listChargers\",\"outputs\":[{\"internalType\":\"address[]\",\"name\":\"\",\"type\":\"address[]\"}],\"selector\":[2851256744,2691483310],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0},{\"kind\":4,\"value\":[2]}],\"inputs\":[{\"internalType\":\"address\",\"name\":\"chargerAccount\",\"type\":\"address\"}],\"name\":\"revokeCharger\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"success\",\"type\":\"bool\"}],\"selector\":[2256264797,3965285635],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_CHARGE = "charge";

    public static final String FUNC_DEDUCT = "deduct";

    public static final String FUNC_DISABLE = "disable";

    public static final String FUNC_ENABLE = "enable";

    public static final String FUNC_GETBALANCE = "getBalance";

    public static final String FUNC_GRANTCHARGER = "grantCharger";

    public static final String FUNC_LISTCHARGERS = "listChargers";

    public static final String FUNC_REVOKECHARGER = "revokeCharger";

    protected Economic(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public TransactionReceipt charge(String userAccount, BigInteger gasValue) {
        final Function function =
                new Function(
                        FUNC_CHARGE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(userAccount),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(
                                        gasValue)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public Function getMethodChargeRawFunction(String userAccount, BigInteger gasValue)
            throws ContractException {
        final Function function =
                new Function(
                        FUNC_CHARGE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(userAccount),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(
                                        gasValue)),
                        Arrays.<TypeReference<?>>asList());
        return function;
    }

    public String getSignedTransactionForCharge(String userAccount, BigInteger gasValue) {
        final Function function =
                new Function(
                        FUNC_CHARGE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(userAccount),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(
                                        gasValue)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public String charge(String userAccount, BigInteger gasValue, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_CHARGE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(userAccount),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(
                                        gasValue)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple2<String, BigInteger> getChargeInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_CHARGE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(
                (String) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    public TransactionReceipt deduct(String userAccount, BigInteger gasValue) {
        final Function function =
                new Function(
                        FUNC_DEDUCT,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(userAccount),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(
                                        gasValue)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public Function getMethodDeductRawFunction(String userAccount, BigInteger gasValue)
            throws ContractException {
        final Function function =
                new Function(
                        FUNC_DEDUCT,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(userAccount),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(
                                        gasValue)),
                        Arrays.<TypeReference<?>>asList());
        return function;
    }

    public String getSignedTransactionForDeduct(String userAccount, BigInteger gasValue) {
        final Function function =
                new Function(
                        FUNC_DEDUCT,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(userAccount),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(
                                        gasValue)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public String deduct(String userAccount, BigInteger gasValue, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_DEDUCT,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(userAccount),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(
                                        gasValue)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple2<String, BigInteger> getDeductInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_DEDUCT,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(
                (String) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    public TransactionReceipt disable() {
        final Function function =
                new Function(
                        FUNC_DISABLE,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public Function getMethodDisableRawFunction() throws ContractException {
        final Function function =
                new Function(
                        FUNC_DISABLE, Arrays.<Type>asList(), Arrays.<TypeReference<?>>asList());
        return function;
    }

    public String getSignedTransactionForDisable() {
        final Function function =
                new Function(
                        FUNC_DISABLE,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public String disable(TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_DISABLE,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public TransactionReceipt enable() {
        final Function function =
                new Function(
                        FUNC_ENABLE,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public Function getMethodEnableRawFunction() throws ContractException {
        final Function function =
                new Function(FUNC_ENABLE, Arrays.<Type>asList(), Arrays.<TypeReference<?>>asList());
        return function;
    }

    public String getSignedTransactionForEnable() {
        final Function function =
                new Function(
                        FUNC_ENABLE,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public String enable(TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_ENABLE,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public BigInteger getBalance(String userAccount) throws ContractException {
        final Function function =
                new Function(
                        FUNC_GETBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(userAccount)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public Function getMethodGetBalanceRawFunction(String userAccount) throws ContractException {
        final Function function =
                new Function(
                        FUNC_GETBALANCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(userAccount)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return function;
    }

    public TransactionReceipt grantCharger(String chargerAccount) {
        final Function function =
                new Function(
                        FUNC_GRANTCHARGER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(chargerAccount)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public Function getMethodGrantChargerRawFunction(String chargerAccount)
            throws ContractException {
        final Function function =
                new Function(
                        FUNC_GRANTCHARGER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(chargerAccount)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return function;
    }

    public String getSignedTransactionForGrantCharger(String chargerAccount) {
        final Function function =
                new Function(
                        FUNC_GRANTCHARGER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(chargerAccount)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public String grantCharger(String chargerAccount, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_GRANTCHARGER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(chargerAccount)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple1<String> getGrantChargerInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_GRANTCHARGER,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>((String) results.get(0).getValue());
    }

    public Tuple1<Boolean> getGrantChargerOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_GRANTCHARGER,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<Boolean>((Boolean) results.get(0).getValue());
    }

    public List listChargers() throws ContractException {
        final Function function =
                new Function(
                        FUNC_LISTCHARGERS,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicArray<Address>>() {}));
        List<Type> result = (List<Type>) executeCallWithSingleValueReturn(function, List.class);
        return convertToNative(result);
    }

    public Function getMethodListChargersRawFunction() throws ContractException {
        final Function function =
                new Function(
                        FUNC_LISTCHARGERS,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicArray<Address>>() {}));
        return function;
    }

    public TransactionReceipt revokeCharger(String chargerAccount) {
        final Function function =
                new Function(
                        FUNC_REVOKECHARGER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(chargerAccount)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public Function getMethodRevokeChargerRawFunction(String chargerAccount)
            throws ContractException {
        final Function function =
                new Function(
                        FUNC_REVOKECHARGER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(chargerAccount)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return function;
    }

    public String getSignedTransactionForRevokeCharger(String chargerAccount) {
        final Function function =
                new Function(
                        FUNC_REVOKECHARGER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(chargerAccount)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public String revokeCharger(String chargerAccount, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_REVOKECHARGER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(chargerAccount)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple1<String> getRevokeChargerInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_REVOKECHARGER,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>((String) results.get(0).getValue());
    }

    public Tuple1<Boolean> getRevokeChargerOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_REVOKECHARGER,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<Boolean>((Boolean) results.get(0).getValue());
    }

    public static Economic load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new Economic(contractAddress, client, credential);
    }

    public static Economic deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                Economic.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(),
                null,
                null);
    }
}
