package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Bool;
import org.fisco.bcos.sdk.v3.codec.datatypes.Event;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int32;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple1;
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
public class KVTableTest extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50600080546001600160a01b031916611002908117825560408051808201825260098152681d17dadd97dd195cdd60ba1b6020820152905163b0e89adb60e01b815263b0e89adb9161006491600401610203565b6020604051808303816000875af1158015610083573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906100a7919061025e565b90508060030b6000146101005760405162461bcd60e51b815260206004820152601360248201527f637265617465207461626c65206661696c656400000000000000000000000000604482015260640160405180910390fd5b6000805460408051808201825260098152681d17dadd97dd195cdd60ba1b6020820152905163f23f63c960e01b81526001600160a01b039092169163f23f63c99161014d91600401610288565b602060405180830381865afa15801561016a573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061018e919061029b565b600180546001600160a01b0319166001600160a01b0392909216919091179055506102c49050565b6000815180845260005b818110156101dc576020818501810151868301820152016101c0565b818111156101ee576000602083870101525b50601f01601f19169290920160200192915050565b60608152600061021660608301846101b6565b82810380602085015260028252611a5960f21b60208301526040810160408501525060096040820152686974656d5f6e616d6560b81b60608201526080810191505092915050565b60006020828403121561027057600080fd5b81518060030b811461028157600080fd5b9392505050565b60208152600061028160208301846101b6565b6000602082840312156102ad57600080fd5b81516001600160a01b038116811461028157600080fd5b610866806102d36000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c806355f150f1146100515780635d52d4d814610070578063693ec85e14610096578063e942b516146100b7575b600080fd5b6100596100ca565b60405161006792919061039c565b60405180910390f35b61008361007e3660046104af565b610179565b60405160039190910b8152602001610067565b6100a96100a4366004610537565b6101fa565b604051610067929190610574565b6100836100c536600461058f565b610285565b6060806000600160009054906101000a90046001600160a01b03166001600160a01b03166355f150f16040518163ffffffff1660e01b8152600401600060405180830381865afa158015610122573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f1916820160405261014a9190810190610638565b9050806000015181602001516000815181106101685761016861074e565b602002602001015192509250509091565b6000805460405163b0e89adb60e01b815282916001600160a01b03169063b0e89adb906101ae90889088908890600401610764565b6020604051808303816000875af11580156101cd573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906101f191906107a7565b95945050505050565b60015460405163349f642f60e11b8152600091606091839183916001600160a01b039091169063693ec85e906102349088906004016107d1565b600060405180830381865afa158015610251573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f1916820160405261027991908101906107e4565b90969095509350505050565b6001546040516374a15a8b60e11b815260009182916001600160a01b039091169063e942b516906102bc908790879060040161039c565b6020604051808303816000875af11580156102db573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906102ff91906107a7565b604051600382900b81529091507f0cf385eb927670d0229706269f568122463b7535b52bd98e52d4787771855d0d9060200160405180910390a19392505050565b60005b8381101561035b578181015183820152602001610343565b8381111561036a576000848401525b50505050565b60008151808452610388816020860160208601610340565b601f01601f19169290920160200192915050565b6040815260006103af6040830185610370565b82810360208401526101f18185610370565b634e487b7160e01b600052604160045260246000fd5b6040805190810167ffffffffffffffff811182821017156103fa576103fa6103c1565b60405290565b604051601f8201601f1916810167ffffffffffffffff81118282101715610429576104296103c1565b604052919050565b600067ffffffffffffffff82111561044b5761044b6103c1565b50601f01601f191660200190565b600082601f83011261046a57600080fd5b813561047d61047882610431565b610400565b81815284602083860101111561049257600080fd5b816020850160208301376000918101602001919091529392505050565b6000806000606084860312156104c457600080fd5b833567ffffffffffffffff808211156104dc57600080fd5b6104e887838801610459565b945060208601359150808211156104fe57600080fd5b61050a87838801610459565b9350604086013591508082111561052057600080fd5b5061052d86828701610459565b9150509250925092565b60006020828403121561054957600080fd5b813567ffffffffffffffff81111561056057600080fd5b61056c84828501610459565b949350505050565b821515815260406020820152600061056c6040830184610370565b600080604083850312156105a257600080fd5b823567ffffffffffffffff808211156105ba57600080fd5b6105c686838701610459565b935060208501359150808211156105dc57600080fd5b506105e985828601610459565b9150509250929050565b600082601f83011261060457600080fd5b815161061261047882610431565b81815284602083860101111561062757600080fd5b61056c826020830160208701610340565b6000602080838503121561064b57600080fd5b825167ffffffffffffffff8082111561066357600080fd5b908401906040828703121561067757600080fd5b61067f6103d7565b82518281111561068e57600080fd5b61069a888286016105f3565b82525083830151828111156106ae57600080fd5b80840193505086601f8401126106c357600080fd5b8251828111156106d5576106d56103c1565b8060051b6106e4868201610400565b918252848101860191868101908a8411156106fe57600080fd5b87870192505b8383101561073a5782518681111561071c5760008081fd5b61072a8c8a838b01016105f3565b8352509187019190870190610704565b968401969096525090979650505050505050565b634e487b7160e01b600052603260045260246000fd5b6060815260006107776060830186610370565b82810360208401526107898186610370565b9050828103604084015261079d8185610370565b9695505050505050565b6000602082840312156107b957600080fd5b81518060030b81146107ca57600080fd5b9392505050565b6020815260006107ca6020830184610370565b600080604083850312156107f757600080fd5b8251801515811461080757600080fd5b602084015190925067ffffffffffffffff81111561082457600080fd5b6105e9858286016105f356fea264697066735822122001357cbd55d701f8d0b25d247caa97ce2af9138484673bf96d19dece5e25f2ba64736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50600080546001600160a01b031916611002908117825560408051808201825260098152681d17dadd97dd195cdd60ba1b602082015290516394558dab60e01b81526394558dab9161006491600401610204565b6020604051808303816000875af1158015610083573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906100a7919061025f565b90508060030b60001461010157604051636381e58960e11b815260206004820152601360248201527f637265617465207461626c65206661696c656400000000000000000000000000604482015260640160405180910390fd5b6000805460408051808201825260098152681d17dadd97dd195cdd60ba1b602082015290516359a48b6560e01b81526001600160a01b03909216916359a48b659161014e91600401610289565b602060405180830381865afa15801561016b573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061018f919061029c565b600180546001600160a01b0319166001600160a01b0392909216919091179055506102c59050565b6000815180845260005b818110156101dd576020818501810151868301820152016101c1565b818111156101ef576000602083870101525b50601f01601f19169290920160200192915050565b60608152600061021760608301846101b7565b82810380602085015260028252611a5960f21b60208301526040810160408501525060096040820152686974656d5f6e616d6560b81b60608201526080810191505092915050565b60006020828403121561027157600080fd5b81518060030b811461028257600080fd5b9392505050565b60208152600061028260208301846101b7565b6000602082840312156102ae57600080fd5b81516001600160a01b038116811461028257600080fd5b610862806102d46000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c80631a391cb41461005157806326097cb51461007c578063753582a41461008f5780637b1b8e03146100a5575b600080fd5b61006461005f36600461042a565b6100c6565b60405160039190910b81526020015b60405180910390f35b61006461008a36600461048e565b610181565b610097610202565b604051610073929190610572565b6100b86100b3366004610597565b6102b1565b6040516100739291906105d4565b60015460405163068e472d60e21b815260009182916001600160a01b0390911690631a391cb4906100fd9087908790600401610572565b6020604051808303816000875af115801561011c573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061014091906105ef565b604051600382900b81529091507fc044934b9c0dc8f2e96c0797246bde787cfafd3e8474b4ac3271c1d3ac7b819a9060200160405180910390a19392505050565b600080546040516394558dab60e01b815282916001600160a01b0316906394558dab906101b690889088908890600401610619565b6020604051808303816000875af11580156101d5573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906101f991906105ef565b95945050505050565b6060806000600160009054906101000a90046001600160a01b03166001600160a01b031663753582a46040518163ffffffff1660e01b8152600401600060405180830381865afa15801561025a573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f1916820160405261028291908101906106a1565b9050806000015181602001516000815181106102a0576102a06107b7565b602002602001015192509250509091565b600154604051637b1b8e0360e01b8152600091606091839183916001600160a01b0390911690637b1b8e03906102eb9088906004016107cd565b600060405180830381865afa158015610308573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f1916820160405261033091908101906107e0565b90969095509350505050565b63b95aa35560e01b600052604160045260246000fd5b6040805190810167ffffffffffffffff811182821017156103755761037561033c565b60405290565b604051601f8201601f1916810167ffffffffffffffff811182821017156103a4576103a461033c565b604052919050565b600067ffffffffffffffff8211156103c6576103c661033c565b50601f01601f191660200190565b600082601f8301126103e557600080fd5b81356103f86103f3826103ac565b61037b565b81815284602083860101111561040d57600080fd5b816020850160208301376000918101602001919091529392505050565b6000806040838503121561043d57600080fd5b823567ffffffffffffffff8082111561045557600080fd5b610461868387016103d4565b9350602085013591508082111561047757600080fd5b50610484858286016103d4565b9150509250929050565b6000806000606084860312156104a357600080fd5b833567ffffffffffffffff808211156104bb57600080fd5b6104c7878388016103d4565b945060208601359150808211156104dd57600080fd5b6104e9878388016103d4565b935060408601359150808211156104ff57600080fd5b5061050c868287016103d4565b9150509250925092565b60005b83811015610531578181015183820152602001610519565b83811115610540576000848401525b50505050565b6000815180845261055e816020860160208601610516565b601f01601f19169290920160200192915050565b6040815260006105856040830185610546565b82810360208401526101f98185610546565b6000602082840312156105a957600080fd5b813567ffffffffffffffff8111156105c057600080fd5b6105cc848285016103d4565b949350505050565b82151581526040602082015260006105cc6040830184610546565b60006020828403121561060157600080fd5b81518060030b811461061257600080fd5b9392505050565b60608152600061062c6060830186610546565b828103602084015261063e8186610546565b905082810360408401526106528185610546565b9695505050505050565b600082601f83011261066d57600080fd5b815161067b6103f3826103ac565b81815284602083860101111561069057600080fd5b6105cc826020830160208701610516565b600060208083850312156106b457600080fd5b825167ffffffffffffffff808211156106cc57600080fd5b90840190604082870312156106e057600080fd5b6106e8610352565b8251828111156106f757600080fd5b6107038882860161065c565b825250838301518281111561071757600080fd5b80840193505086601f84011261072c57600080fd5b82518281111561073e5761073e61033c565b8060051b61074d86820161037b565b918252848101860191868101908a84111561076757600080fd5b87870192505b838310156107a3578251868111156107855760008081fd5b6107938c8a838b010161065c565b835250918701919087019061076d565b968401969096525090979650505050505050565b63b95aa35560e01b600052603260045260246000fd5b6020815260006106126020830184610546565b600080604083850312156107f357600080fd5b8251801515811461080357600080fd5b602084015190925067ffffffffffffffff81111561082057600080fd5b6104848582860161065c56fea264697066735822122082625b755ef79b79c60f32d9be1aa21bef7620899ae1a82882690907bc64a98464736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"inputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"count\",\"type\":\"int256\"}],\"name\":\"SetEvent\",\"type\":\"event\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"_tableName\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"keyName\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"fieldName\",\"type\":\"string\"}],\"name\":\"createKVTableTest\",\"outputs\":[{\"internalType\":\"int32\",\"name\":\"\",\"type\":\"int32\"}],\"selector\":[1565709528,638155957],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"desc\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"selector\":[1441878257,1966441124],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"}],\"name\":\"get\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"},{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"selector\":[1765722206,2065403395],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"item_name\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[{\"internalType\":\"int32\",\"name\":\"\",\"type\":\"int32\"}],\"selector\":[3913463062,439950516],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_CREATEKVTABLETEST = "createKVTableTest";

    public static final String FUNC_DESC = "desc";

    public static final String FUNC_GET = "get";

    public static final String FUNC_SET = "set";

    public static final Event SETEVENT_EVENT =
            new Event("SetEvent", Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));;

    protected KVTableTest(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public List<SetEventEventResponse> getSetEventEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList =
                extractEventParametersWithLog(SETEVENT_EVENT, transactionReceipt);
        ArrayList<SetEventEventResponse> responses =
                new ArrayList<SetEventEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            SetEventEventResponse typedResponse = new SetEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.count = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public TransactionReceipt createKVTableTest(
            String _tableName, String keyName, String fieldName) {
        final Function function =
                new Function(
                        FUNC_CREATEKVTABLETEST,
                        Arrays.<Type>asList(
                                new Utf8String(_tableName),
                                new Utf8String(keyName),
                                new Utf8String(fieldName)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String createKVTableTest(
            String _tableName, String keyName, String fieldName, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_CREATEKVTABLETEST,
                        Arrays.<Type>asList(
                                new Utf8String(_tableName),
                                new Utf8String(keyName),
                                new Utf8String(fieldName)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForCreateKVTableTest(
            String _tableName, String keyName, String fieldName) {
        final Function function =
                new Function(
                        FUNC_CREATEKVTABLETEST,
                        Arrays.<Type>asList(
                                new Utf8String(_tableName),
                                new Utf8String(keyName),
                                new Utf8String(fieldName)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple3<String, String, String> getCreateKVTableTestInput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_CREATEKVTABLETEST,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple3<String, String, String>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (String) results.get(2).getValue());
    }

    public Tuple1<BigInteger> getCreateKVTableTestOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_CREATEKVTABLETEST,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int32>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public Tuple2<String, String> desc() throws ContractException {
        final Function function =
                new Function(
                        FUNC_DESC,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple2<String, String>(
                (String) results.get(0).getValue(), (String) results.get(1).getValue());
    }

    public Tuple2<Boolean, String> get(String id) throws ContractException {
        final Function function =
                new Function(
                        FUNC_GET,
                        Arrays.<Type>asList(new Utf8String(id)),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Bool>() {}, new TypeReference<Utf8String>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple2<Boolean, String>(
                (Boolean) results.get(0).getValue(), (String) results.get(1).getValue());
    }

    public TransactionReceipt set(String id, String item_name) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(new Utf8String(id), new Utf8String(item_name)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String set(String id, String item_name, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(new Utf8String(id), new Utf8String(item_name)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSet(String id, String item_name) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(new Utf8String(id), new Utf8String(item_name)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple2<String, String> getSetInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, String>(
                (String) results.get(0).getValue(), (String) results.get(1).getValue());
    }

    public Tuple1<BigInteger> getSetOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int32>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public static KVTableTest load(
            String contractAddress, Client client, CryptoKeyPair credential) {
        return new KVTableTest(contractAddress, client, credential);
    }

    public static KVTableTest deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                KVTableTest.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(),
                null,
                null);
    }

    public static class SetEventEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger count;
    }
}
