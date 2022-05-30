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
        "60806040523480156200001157600080fd5b50600080546001600160a01b031916611002908117825560408051808201825260098152681d17dadd97dd195cdd60ba1b6020820152905163f23f63c960e01b815263f23f63c9916200006791600401620002df565b602060405180830381865afa15801562000085573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190620000ab9190620002fb565b90506001600160a01b03811615620000e357600180546001600160a01b0319166001600160a01b0392909216919091179055620003a8565b6000805460408051808201825260098152681d17dadd97dd195cdd60ba1b6020820152905163b0e89adb60e01b81526001600160a01b039092169163b0e89adb91620001329160040162000326565b6020604051808303816000875af115801562000152573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019062000178919062000383565b90508060030b600014620001d25760405162461bcd60e51b815260206004820152601360248201527f637265617465207461626c65206661696c656400000000000000000000000000604482015260640160405180910390fd5b6000805460408051808201825260098152681d17dadd97dd195cdd60ba1b6020820152905163f23f63c960e01b81526001600160a01b039092169163f23f63c9916200022191600401620002df565b602060405180830381865afa1580156200023f573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190620002659190620002fb565b600180546001600160a01b0319166001600160a01b039290921691909117905550620003a8915050565b6000815180845260005b81811015620002b75760208185018101518683018201520162000299565b81811115620002ca576000602083870101525b50601f01601f19169290920160200192915050565b602081526000620002f460208301846200028f565b9392505050565b6000602082840312156200030e57600080fd5b81516001600160a01b0381168114620002f457600080fd5b6060815260006200033b60608301846200028f565b82810380602085015260028252611a5960f21b60208301526040810160408501525060096040820152686974656d5f6e616d6560b81b60608201526080810191505092915050565b6000602082840312156200039657600080fd5b81518060030b8114620002f457600080fd5b61087f80620003b86000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c806355f150f1146100515780635d52d4d814610070578063693ec85e14610096578063e942b516146100b7575b600080fd5b6100596100ca565b6040516100679291906103b5565b60405180910390f35b61008361007e3660046104c8565b610192565b60405160039190910b8152602001610067565b6100a96100a4366004610550565b610213565b60405161006792919061058d565b6100836100c53660046105a8565b61029e565b6000805460408051808201825260098152681d17dadd97dd195cdd60ba1b602082015290516317435b5560e21b8152606093849390926001600160a01b0390911691635d0d6d549161011e9160040161060c565b600060405180830381865afa15801561013b573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f19168201604052610163919081019061066b565b90508060000151816020015160008151811061018157610181610781565b602002602001015192509250509091565b6000805460405163b0e89adb60e01b815282916001600160a01b03169063b0e89adb906101c790889088908890600401610797565b6020604051808303816000875af11580156101e6573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061020a91906107da565b95945050505050565b60015460405163349f642f60e11b8152600091606091839183916001600160a01b039091169063693ec85e9061024d90889060040161060c565b600060405180830381865afa15801561026a573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f1916820160405261029291908101906107fd565b90969095509350505050565b6001546040516374a15a8b60e11b815260009182916001600160a01b039091169063e942b516906102d590879087906004016103b5565b6020604051808303816000875af11580156102f4573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061031891906107da565b604051600382900b81529091507f0cf385eb927670d0229706269f568122463b7535b52bd98e52d4787771855d0d9060200160405180910390a19392505050565b60005b8381101561037457818101518382015260200161035c565b83811115610383576000848401525b50505050565b600081518084526103a1816020860160208601610359565b601f01601f19169290920160200192915050565b6040815260006103c86040830185610389565b828103602084015261020a8185610389565b634e487b7160e01b600052604160045260246000fd5b6040805190810167ffffffffffffffff81118282101715610413576104136103da565b60405290565b604051601f8201601f1916810167ffffffffffffffff81118282101715610442576104426103da565b604052919050565b600067ffffffffffffffff821115610464576104646103da565b50601f01601f191660200190565b600082601f83011261048357600080fd5b81356104966104918261044a565b610419565b8181528460208386010111156104ab57600080fd5b816020850160208301376000918101602001919091529392505050565b6000806000606084860312156104dd57600080fd5b833567ffffffffffffffff808211156104f557600080fd5b61050187838801610472565b9450602086013591508082111561051757600080fd5b61052387838801610472565b9350604086013591508082111561053957600080fd5b5061054686828701610472565b9150509250925092565b60006020828403121561056257600080fd5b813567ffffffffffffffff81111561057957600080fd5b61058584828501610472565b949350505050565b82151581526040602082015260006105856040830184610389565b600080604083850312156105bb57600080fd5b823567ffffffffffffffff808211156105d357600080fd5b6105df86838701610472565b935060208501359150808211156105f557600080fd5b5061060285828601610472565b9150509250929050565b60208152600061061f6020830184610389565b9392505050565b600082601f83011261063757600080fd5b81516106456104918261044a565b81815284602083860101111561065a57600080fd5b610585826020830160208701610359565b6000602080838503121561067e57600080fd5b825167ffffffffffffffff8082111561069657600080fd5b90840190604082870312156106aa57600080fd5b6106b26103f0565b8251828111156106c157600080fd5b6106cd88828601610626565b82525083830151828111156106e157600080fd5b80840193505086601f8401126106f657600080fd5b825182811115610708576107086103da565b8060051b610717868201610419565b918252848101860191868101908a84111561073157600080fd5b87870192505b8383101561076d5782518681111561074f5760008081fd5b61075d8c8a838b0101610626565b8352509187019190870190610737565b968401969096525090979650505050505050565b634e487b7160e01b600052603260045260246000fd5b6060815260006107aa6060830186610389565b82810360208401526107bc8186610389565b905082810360408401526107d08185610389565b9695505050505050565b6000602082840312156107ec57600080fd5b81518060030b811461061f57600080fd5b6000806040838503121561081057600080fd5b8251801515811461082057600080fd5b602084015190925067ffffffffffffffff81111561083d57600080fd5b6106028582860161062656fea2646970667358221220823002d48d97b52b2f7fe674037e1ad453852fefd144b461872ad455e5265caa64736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "60806040523480156200001157600080fd5b50600080546001600160a01b031916611002908117825560408051808201825260098152681d17dadd97dd195cdd60ba1b602082015290516359a48b6560e01b81526359a48b65916200006791600401620002e0565b602060405180830381865afa15801562000085573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190620000ab9190620002fc565b90506001600160a01b03811615620000e357600180546001600160a01b0319166001600160a01b0392909216919091179055620003a9565b6000805460408051808201825260098152681d17dadd97dd195cdd60ba1b602082015290516394558dab60e01b81526001600160a01b03909216916394558dab91620001329160040162000327565b6020604051808303816000875af115801562000152573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019062000178919062000384565b90508060030b600014620001d357604051636381e58960e11b815260206004820152601360248201527f637265617465207461626c65206661696c656400000000000000000000000000604482015260640160405180910390fd5b6000805460408051808201825260098152681d17dadd97dd195cdd60ba1b602082015290516359a48b6560e01b81526001600160a01b03909216916359a48b65916200022291600401620002e0565b602060405180830381865afa15801562000240573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190620002669190620002fc565b600180546001600160a01b0319166001600160a01b039290921691909117905550620003a9915050565b6000815180845260005b81811015620002b8576020818501810151868301820152016200029a565b81811115620002cb576000602083870101525b50601f01601f19169290920160200192915050565b602081526000620002f5602083018462000290565b9392505050565b6000602082840312156200030f57600080fd5b81516001600160a01b0381168114620002f557600080fd5b6060815260006200033c606083018462000290565b82810380602085015260028252611a5960f21b60208301526040810160408501525060096040820152686974656d5f6e616d6560b81b60608201526080810191505092915050565b6000602082840312156200039757600080fd5b81518060030b8114620002f557600080fd5b61087b80620003b96000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c80631a391cb41461005157806326097cb51461007c578063753582a41461008f5780637b1b8e03146100a5575b600080fd5b61006461005f366004610443565b6100c6565b60405160039190910b81526020015b60405180910390f35b61006461008a3660046104a7565b610181565b610097610202565b60405161007392919061058b565b6100b86100b33660046105b0565b6102ca565b6040516100739291906105ed565b60015460405163068e472d60e21b815260009182916001600160a01b0390911690631a391cb4906100fd908790879060040161058b565b6020604051808303816000875af115801561011c573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906101409190610608565b604051600382900b81529091507fc044934b9c0dc8f2e96c0797246bde787cfafd3e8474b4ac3271c1d3ac7b819a9060200160405180910390a19392505050565b600080546040516394558dab60e01b815282916001600160a01b0316906394558dab906101b690889088908890600401610632565b6020604051808303816000875af11580156101d5573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906101f99190610608565b95945050505050565b6000805460408051808201825260098152681d17dadd97dd195cdd60ba1b60208201529051632e21756b60e21b8152606093849390926001600160a01b039091169163b885d5ac9161025691600401610675565b600060405180830381865afa158015610273573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f1916820160405261029b91908101906106cd565b9050806000015181602001516000815181106102b9576102b96107e3565b602002602001015192509250509091565b600154604051637b1b8e0360e01b8152600091606091839183916001600160a01b0390911690637b1b8e0390610304908890600401610675565b600060405180830381865afa158015610321573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f1916820160405261034991908101906107f9565b90969095509350505050565b63b95aa35560e01b600052604160045260246000fd5b6040805190810167ffffffffffffffff8111828210171561038e5761038e610355565b60405290565b604051601f8201601f1916810167ffffffffffffffff811182821017156103bd576103bd610355565b604052919050565b600067ffffffffffffffff8211156103df576103df610355565b50601f01601f191660200190565b600082601f8301126103fe57600080fd5b813561041161040c826103c5565b610394565b81815284602083860101111561042657600080fd5b816020850160208301376000918101602001919091529392505050565b6000806040838503121561045657600080fd5b823567ffffffffffffffff8082111561046e57600080fd5b61047a868387016103ed565b9350602085013591508082111561049057600080fd5b5061049d858286016103ed565b9150509250929050565b6000806000606084860312156104bc57600080fd5b833567ffffffffffffffff808211156104d457600080fd5b6104e0878388016103ed565b945060208601359150808211156104f657600080fd5b610502878388016103ed565b9350604086013591508082111561051857600080fd5b50610525868287016103ed565b9150509250925092565b60005b8381101561054a578181015183820152602001610532565b83811115610559576000848401525b50505050565b6000815180845261057781602086016020860161052f565b601f01601f19169290920160200192915050565b60408152600061059e604083018561055f565b82810360208401526101f9818561055f565b6000602082840312156105c257600080fd5b813567ffffffffffffffff8111156105d957600080fd5b6105e5848285016103ed565b949350505050565b82151581526040602082015260006105e5604083018461055f565b60006020828403121561061a57600080fd5b81518060030b811461062b57600080fd5b9392505050565b606081526000610645606083018661055f565b8281036020840152610657818661055f565b9050828103604084015261066b818561055f565b9695505050505050565b60208152600061062b602083018461055f565b600082601f83011261069957600080fd5b81516106a761040c826103c5565b8181528460208386010111156106bc57600080fd5b6105e582602083016020870161052f565b600060208083850312156106e057600080fd5b825167ffffffffffffffff808211156106f857600080fd5b908401906040828703121561070c57600080fd5b61071461036b565b82518281111561072357600080fd5b61072f88828601610688565b825250838301518281111561074357600080fd5b80840193505086601f84011261075857600080fd5b82518281111561076a5761076a610355565b8060051b610779868201610394565b918252848101860191868101908a84111561079357600080fd5b87870192505b838310156107cf578251868111156107b15760008081fd5b6107bf8c8a838b0101610688565b8352509187019190870190610799565b968401969096525090979650505050505050565b63b95aa35560e01b600052603260045260246000fd5b6000806040838503121561080c57600080fd5b8251801515811461081c57600080fd5b602084015190925067ffffffffffffffff81111561083957600080fd5b61049d8582860161068856fea2646970667358221220d8d38ba7a33ec34423fe7724277a2c2b7ddd394c62762a81b0ab9345deb5107e64736f6c634300080b0033"
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
