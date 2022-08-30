package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Event;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class ParallelCpuHeavy extends CpuHeavyContract {
    public static final String[] BINARY_ARRAY = {
        "60806040526110066000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555034801561005257600080fd5b50610799806100626000396000f3fe608060405234801561001057600080fd5b50600436106100575760003560e01c806334a18dda1461005c57806379fa913f146101215780637b395ec2146101dc578063bca926af14610214578063d39f70bc1461021e575b600080fd5b61011f6004803603604081101561007257600080fd5b810190808035906020019064010000000081111561008f57600080fd5b8201836020820111156100a157600080fd5b803590602001918460018302840111640100000000831117156100c357600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929080359060200190929190505050610228565b005b6101da6004803603602081101561013757600080fd5b810190808035906020019064010000000081111561015457600080fd5b82018360208201111561016657600080fd5b8035906020019184600183028401116401000000008311171561018857600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929050505061037d565b005b610212600480360360408110156101f257600080fd5b8101908080359060200190929190803590602001909291905050506104c9565b005b61021c6105a0565b005b6102266105e2565b005b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16630553904e3084846040518463ffffffff1660e01b8152600401808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200180602001838152602001828103825284818151815260200191508051906020019080838360005b838110156102ef5780820151818401526020810190506102d4565b50505050905090810190601f16801561031c5780820380516001836020036101000a031916815260200191505b50945050505050602060405180830381600087803b15801561033d57600080fd5b505af1158015610351573d6000803e3d6000fd5b505050506040513d602081101561036757600080fd5b8101908080519060200190929190505050505050565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166311e3f2af30836040518363ffffffff1660e01b8152600401808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200180602001828103825283818151815260200191508051906020019080838360005b8381101561043d578082015181840152602081019050610422565b50505050905090810190601f16801561046a5780820380516001836020036101000a031916815260200191505b509350505050602060405180830381600087803b15801561048a57600080fd5b505af115801561049e573d6000803e3d6000fd5b505050506040513d60208110156104b457600080fd5b81019080805190602001909291905050505050565b60608267ffffffffffffffff811180156104e257600080fd5b506040519080825280602002602001820160405280156105115781602001602082028036833780820191505090505b50905060008090505b815181101561054b5780840382828151811061053257fe5b602002602001018181525050808060010191505061051a565b5061055c8160006001845103610622565b7fd596fdad182d29130ce218f4c1590c4b5ede105bee36690727baa6592bd2bfc88383604051808381526020018281526020019250505060405180910390a1505050565b6105e06040518060400160405280601581526020017f736f72742875696e743235362c75696e743235362900000000000000000000008152506000610228565b565b6106206040518060400160405280601581526020017f736f72742875696e743235362c75696e7432353629000000000000000000000081525061037d565b565b600082905060008290508082141561063b57505061075e565b60008560028686038161064a57fe5b0586018151811061065757fe5b602002602001015190505b818313610732575b8086848151811061067757fe5b6020026020010151101561069257828060010193505061066a565b5b85828151811061069f57fe5b60200260200101518110156106bc57818060019003925050610693565b81831361072d578582815181106106cf57fe5b60200260200101518684815181106106e357fe5b60200260200101518785815181106106f757fe5b6020026020010188858151811061070a57fe5b602002602001018281525082815250505082806001019350508180600190039250505b610662565b8185121561074657610745868684610622565b5b8383121561075a57610759868486610622565b5b5050505b50505056fea26469706673582212208520439059258806bc3aed447c27565262afd831b94f386b222c0850b5820d6964736f6c634300060a0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "60806040526110066000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555034801561005257600080fd5b50610798806100626000396000f3fe608060405234801561001057600080fd5b50600436106100565760003560e01c8062f0e1331461005b578063748e7a1b1461011657806374f1c7951461012057806394618e4c14610158578063b4c653e014610162575b600080fd5b6101146004803603602081101561007157600080fd5b810190808035906020019064010000000081111561008e57600080fd5b8201836020820111156100a057600080fd5b803590602001918460018302840111640100000000831117156100c257600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610227565b005b61011e610373565b005b6101566004803603604081101561013657600080fd5b8101908080359060200190929190803590602001909291905050506103b3565b005b61016061048a565b005b6102256004803603604081101561017857600080fd5b810190808035906020019064010000000081111561019557600080fd5b8201836020820111156101a757600080fd5b803590602001918460018302840111640100000000831117156101c957600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803590602001909291905050506104cc565b005b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663714c65bd30836040518363ffffffff1660e01b8152600401808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200180602001828103825283818151815260200191508051906020019080838360005b838110156102e75780820151818401526020810190506102cc565b50505050905090810190601f1680156103145780820380516001836020036101000a031916815260200191505b509350505050602060405180830381600087803b15801561033457600080fd5b505af1158015610348573d6000803e3d6000fd5b505050506040513d602081101561035e57600080fd5b81019080805190602001909291905050505050565b6103b16040518060400160405280601581526020017f736f72742875696e743235362c75696e74323536290000000000000000000000815250610227565b565b60608267ffffffffffffffff811180156103cc57600080fd5b506040519080825280602002602001820160405280156103fb5781602001602082028036833780820191505090505b50905060008090505b81518110156104355780840382828151811061041c57fe5b6020026020010181815250508080600101915050610404565b506104468160006001845103610621565b7f24ca5594ba8a48a066178b3c63c028d9743b8f480eeaf4dc2d6295f65a1fff4a8383604051808381526020018281526020019250505060405180910390a1505050565b6104ca6040518060400160405280601581526020017f736f72742875696e743235362c75696e7432353629000000000000000000000081525060006104cc565b565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663dc536a623084846040518463ffffffff1660e01b8152600401808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200180602001838152602001828103825284818151815260200191508051906020019080838360005b83811015610593578082015181840152602081019050610578565b50505050905090810190601f1680156105c05780820380516001836020036101000a031916815260200191505b50945050505050602060405180830381600087803b1580156105e157600080fd5b505af11580156105f5573d6000803e3d6000fd5b505050506040513d602081101561060b57600080fd5b8101908080519060200190929190505050505050565b600082905060008290508082141561063a57505061075d565b60008560028686038161064957fe5b0586018151811061065657fe5b602002602001015190505b818313610731575b8086848151811061067657fe5b60200260200101511015610691578280600101935050610669565b5b85828151811061069e57fe5b60200260200101518110156106bb57818060019003925050610692565b81831361072c578582815181106106ce57fe5b60200260200101518684815181106106e257fe5b60200260200101518785815181106106f657fe5b6020026020010188858151811061070957fe5b602002602001018281525082815250505082806001019350508180600190039250505b610661565b8185121561074557610744868684610621565b5b8383121561075957610758868486610621565b5b5050505b50505056fea26469706673582212202009a53e799e3d9e1885728b21439c0105a187a1052d7db34773222ed68606ff64736f6c634300060a0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"size\",\"type\":\"uint256\"},{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"signature\",\"type\":\"uint256\"}],\"name\":\"finish\",\"type\":\"event\"},{\"inputs\":[],\"name\":\"disableParallel\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"enableParallel\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"functionName\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"criticalSize\",\"type\":\"uint256\"}],\"name\":\"registerParallelFunction\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"size\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"signature\",\"type\":\"uint256\"}],\"name\":\"sort\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"functionName\",\"type\":\"string\"}],\"name\":\"unregisterParallelFunction\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_DISABLEPARALLEL = "disableParallel";

    public static final String FUNC_ENABLEPARALLEL = "enableParallel";

    public static final String FUNC_REGISTERPARALLELFUNCTION = "registerParallelFunction";

    public static final String FUNC_SORT = "sort";

    public static final String FUNC_UNREGISTERPARALLELFUNCTION = "unregisterParallelFunction";

    public static final Event FINISH_EVENT =
            new Event(
                    "finish",
                    Arrays.<TypeReference<?>>asList(
                            new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));;

    protected ParallelCpuHeavy(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public List<FinishEventResponse> getFinishEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList =
                extractEventParametersWithLog(FINISH_EVENT, transactionReceipt);
        ArrayList<FinishEventResponse> responses =
                new ArrayList<FinishEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            FinishEventResponse typedResponse = new FinishEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.size = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.signature =
                    (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public TransactionReceipt disableParallel() {
        final Function function =
                new Function(
                        FUNC_DISABLEPARALLEL,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void disableParallel(TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_DISABLEPARALLEL,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForDisableParallel() {
        final Function function =
                new Function(
                        FUNC_DISABLEPARALLEL,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public TransactionReceipt enableParallel() {
        final Function function =
                new Function(
                        FUNC_ENABLEPARALLEL,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void enableParallel(TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_ENABLEPARALLEL,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForEnableParallel() {
        final Function function =
                new Function(
                        FUNC_ENABLEPARALLEL,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public TransactionReceipt registerParallelFunction(
            String functionName, BigInteger criticalSize) {
        final Function function =
                new Function(
                        FUNC_REGISTERPARALLELFUNCTION,
                        Arrays.<Type>asList(
                                new Utf8String(functionName), new Uint256(criticalSize)),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void registerParallelFunction(
            String functionName, BigInteger criticalSize, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_REGISTERPARALLELFUNCTION,
                        Arrays.<Type>asList(
                                new Utf8String(functionName), new Uint256(criticalSize)),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForRegisterParallelFunction(
            String functionName, BigInteger criticalSize) {
        final Function function =
                new Function(
                        FUNC_REGISTERPARALLELFUNCTION,
                        Arrays.<Type>asList(
                                new Utf8String(functionName), new Uint256(criticalSize)),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple2<String, BigInteger> getRegisterParallelFunctionInput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_REGISTERPARALLELFUNCTION,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Uint256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(
                (String) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    public TransactionReceipt sort(BigInteger size, BigInteger signature) {
        final Function function =
                new Function(
                        FUNC_SORT,
                        Arrays.<Type>asList(new Uint256(size), new Uint256(signature)),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    @Override
    public void sort(BigInteger size, BigInteger signature, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SORT,
                        Arrays.<Type>asList(new Uint256(size), new Uint256(signature)),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSort(BigInteger size, BigInteger signature) {
        final Function function =
                new Function(
                        FUNC_SORT,
                        Arrays.<Type>asList(new Uint256(size), new Uint256(signature)),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple2<BigInteger, BigInteger> getSortInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_SORT,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, BigInteger>(
                (BigInteger) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    public TransactionReceipt unregisterParallelFunction(String functionName) {
        final Function function =
                new Function(
                        FUNC_UNREGISTERPARALLELFUNCTION,
                        Arrays.<Type>asList(new Utf8String(functionName)),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void unregisterParallelFunction(String functionName, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_UNREGISTERPARALLELFUNCTION,
                        Arrays.<Type>asList(new Utf8String(functionName)),
                        Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForUnregisterParallelFunction(String functionName) {
        final Function function =
                new Function(
                        FUNC_UNREGISTERPARALLELFUNCTION,
                        Arrays.<Type>asList(new Utf8String(functionName)),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getUnregisterParallelFunctionInput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_UNREGISTERPARALLELFUNCTION,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>((String) results.get(0).getValue());
    }

    public static ParallelCpuHeavy load(
            String contractAddress, Client client, CryptoKeyPair credential) {
        return new ParallelCpuHeavy(contractAddress, client, credential);
    }

    public static ParallelCpuHeavy deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                ParallelCpuHeavy.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                null,
                null,
                null);
    }

    public static class FinishEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger size;

        public BigInteger signature;
    }
}
