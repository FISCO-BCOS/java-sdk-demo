package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Bool;
import org.fisco.bcos.sdk.abi.datatypes.DynamicArray;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.Utf8String;
import org.fisco.bcos.sdk.abi.datatypes.generated.Int256;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class TigerHole2 extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b50610710806100206000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c80630ed463c11461005157806395c2ecbb14610161578063bca926af1461023e578063f5766de014610248575b600080fd5b61010a6004803603602081101561006757600080fd5b810190808035906020019064010000000081111561008457600080fd5b82018360208201111561009657600080fd5b803590602001918460018302840111640100000000831117156100b857600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192905050506102ef565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b8381101561014d578082015181840152602081019050610132565b505050509050019250505060405180910390f35b6102246004803603604081101561017757600080fd5b810190808035906020019064010000000081111561019457600080fd5b8201836020820111156101a657600080fd5b803590602001918460018302840111640100000000831117156101c857600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803590602001909291905050506103b3565b604051808215151515815260200191505060405180910390f35b610246610479565b005b6102746004803603602081101561025e57600080fd5b8101908080359060200190929190505050610580565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156102b4578082015181840152602081019050610299565b50505050905090810190601f1680156102e15780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b60606000826040518082805190602001908083835b602083106103275780518252602082019150602081019050602083039250610304565b6001836020036101000a03801982511681845116808217855250505050505090500191505090815260200160405180910390206000018054806020026020016040519081016040528092919081815260200182805480156103a757602002820191906000526020600020905b815481526020019060010190808311610393575b50505050509050919050565b600080836040518082805190602001908083835b602083106103ea57805182526020820191506020810190506020830392506103c7565b6001836020036101000a03801982511681845116808217855250505050505090500191505090815260200160405180910390206000018290806001815401808255809150506001900390600052602060002001600090919091909150558260016000848152602001908152602001600020908051906020019061046e929190610635565b506001905092915050565b600061100690508073ffffffffffffffffffffffffffffffffffffffff16630553904e3060026040518363ffffffff1660e01b8152600401808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200180602001838152602001828103825260178152602001807f736574546967657228737472696e672c696e74323536290000000000000000008152506020019350505050602060405180830381600087803b15801561054157600080fd5b505af1158015610555573d6000803e3d6000fd5b505050506040513d602081101561056b57600080fd5b81019080805190602001909291905050505050565b6060600160008381526020019081526020016000208054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156106295780601f106105fe57610100808354040283529160200191610629565b820191906000526020600020905b81548152906001019060200180831161060c57829003601f168201915b50505050509050919050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061067657805160ff19168380011785556106a4565b828001600101855582156106a4579182015b828111156106a3578251825591602001919060010190610688565b5b5090506106b191906106b5565b5090565b6106d791905b808211156106d35760008160009055506001016106bb565b5090565b9056fea2646970667358221220967fcb4cd09c56343c4963d9a543c5edfada1ea6047db42eb618a2f9ec54417264736f6c634300060a0033"};

    public static final String BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"608060405234801561001057600080fd5b50610710806100206000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c806304c4399e146100515780632d2f985b1461012e57806394618e4c146101d5578063c72d4259146101df575b600080fd5b6101146004803603604081101561006757600080fd5b810190808035906020019064010000000081111561008457600080fd5b82018360208201111561009657600080fd5b803590602001918460018302840111640100000000831117156100b857600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803590602001909291905050506102ef565b604051808215151515815260200191505060405180910390f35b61015a6004803603602081101561014457600080fd5b81019080803590602001909291905050506103b5565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561019a57808201518184015260208101905061017f565b50505050905090810190601f1680156101c75780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6101dd61046a565b005b610298600480360360208110156101f557600080fd5b810190808035906020019064010000000081111561021257600080fd5b82018360208201111561022457600080fd5b8035906020019184600183028401116401000000008311171561024657600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610571565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b838110156102db5780820151818401526020810190506102c0565b505050509050019250505060405180910390f35b600080836040518082805190602001908083835b602083106103265780518252602082019150602081019050602083039250610303565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020600001829080600181540180825580915050600190039060005260206000200160009091909190915055826001600084815260200190815260200160002090805190602001906103aa929190610635565b506001905092915050565b6060600160008381526020019081526020016000208054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561045e5780601f106104335761010080835404028352916020019161045e565b820191906000526020600020905b81548152906001019060200180831161044157829003601f168201915b50505050509050919050565b600061100690508073ffffffffffffffffffffffffffffffffffffffff1663dc536a623060026040518363ffffffff1660e01b8152600401808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200180602001838152602001828103825260178152602001807f736574546967657228737472696e672c696e74323536290000000000000000008152506020019350505050602060405180830381600087803b15801561053257600080fd5b505af1158015610546573d6000803e3d6000fd5b505050506040513d602081101561055c57600080fd5b81019080805190602001909291905050505050565b60606000826040518082805190602001908083835b602083106105a95780518252602082019150602081019050602083039250610586565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060000180548060200260200160405190810160405280929190818152602001828054801561062957602002820191906000526020600020905b815481526020019060010190808311610615575b50505050509050919050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061067657805160ff19168380011785556106a4565b828001600101855582156106a4579182015b828111156106a3578251825591602001919060010190610688565b5b5090506106b191906106b5565b5090565b6106d791905b808211156106d35760008160009055506001016106bb565b5090565b9056fea26469706673582212205ef703ba91bb59ae4b9b0ac2661a94900c2d55cdabaec43748b1d2b6c113c2ef64736f6c634300060a0033"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"inputs\":[],\"name\":\"enableParallel\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"int256\",\"name\":\"tiger\",\"type\":\"int256\"}],\"name\":\"getOpenIDByTiger\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"openid\",\"type\":\"string\"}],\"name\":\"getTigersByOpenID\",\"outputs\":[{\"internalType\":\"int256[]\",\"name\":\"\",\"type\":\"int256[]\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"openid\",\"type\":\"string\"},{\"internalType\":\"int256\",\"name\":\"tiger\",\"type\":\"int256\"}],\"name\":\"setTiger\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_ENABLEPARALLEL = "enableParallel";

    public static final String FUNC_GETOPENIDBYTIGER = "getOpenIDByTiger";

    public static final String FUNC_GETTIGERSBYOPENID = "getTigersByOpenID";

    public static final String FUNC_SETTIGER = "setTiger";

    protected TigerHole2(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public TransactionReceipt enableParallel() {
        final Function function = new Function(
                FUNC_ENABLEPARALLEL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] enableParallel(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_ENABLEPARALLEL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForEnableParallel() {
        final Function function = new Function(
                FUNC_ENABLEPARALLEL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public String getOpenIDByTiger(BigInteger tiger) throws ContractException {
        final Function function = new Function(FUNC_GETOPENIDBYTIGER, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Int256(tiger)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public List getTigersByOpenID(String openid) throws ContractException {
        final Function function = new Function(FUNC_GETTIGERSBYOPENID, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.Utf8String(openid)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Int256>>() {}));
        List<Type> result = (List<Type>) executeCallWithSingleValueReturn(function, List.class);
        return convertToNative(result);
    }

    public TransactionReceipt setTiger(String openid, BigInteger tiger) {
        final Function function = new Function(
                FUNC_SETTIGER, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.Utf8String(openid), 
                new org.fisco.bcos.sdk.abi.datatypes.generated.Int256(tiger)), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] setTiger(String openid, BigInteger tiger, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_SETTIGER, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.Utf8String(openid), 
                new org.fisco.bcos.sdk.abi.datatypes.generated.Int256(tiger)), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSetTiger(String openid, BigInteger tiger) {
        final Function function = new Function(
                FUNC_SETTIGER, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.Utf8String(openid), 
                new org.fisco.bcos.sdk.abi.datatypes.generated.Int256(tiger)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple2<String, BigInteger> getSetTigerInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SETTIGER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Int256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(

                (String) results.get(0).getValue(), 
                (BigInteger) results.get(1).getValue()
                );
    }

    public Tuple1<Boolean> getSetTigerOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_SETTIGER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<Boolean>(

                (Boolean) results.get(0).getValue()
                );
    }

    public static TigerHole2 load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new TigerHole2(contractAddress, client, credential);
    }

    public static TigerHole2 deploy(Client client, CryptoKeyPair credential) throws ContractException {
        return deploy(TigerHole2.class, client, credential, getBinary(client.getCryptoSuite()), "");
    }
}
