package org.fisco.bcos.sdk.demo.contract;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class MapTest extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b5061042d806100206000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c8063693ec85e1461003b578063e942b51614610064575b600080fd5b61004e61004936600461029c565b610079565b60405161005b9190610309565b60405180910390f35b61007761007236600461033c565b610129565b005b606060008260405161008b91906103a0565b908152602001604051809103902080546100a4906103bc565b80601f01602080910402602001604051908101604052809291908181526020018280546100d0906103bc565b801561011d5780601f106100f25761010080835404028352916020019161011d565b820191906000526020600020905b81548152906001019060200180831161010057829003601f168201915b50505050509050919050565b8060008360405161013a91906103a0565b9081526020016040518091039020908051906020019061015b929190610160565b505050565b82805461016c906103bc565b90600052602060002090601f01602090048101928261018e57600085556101d4565b82601f106101a757805160ff19168380011785556101d4565b828001600101855582156101d4579182015b828111156101d45782518255916020019190600101906101b9565b506101e09291506101e4565b5090565b5b808211156101e057600081556001016101e5565b634e487b7160e01b600052604160045260246000fd5b600082601f83011261022057600080fd5b813567ffffffffffffffff8082111561023b5761023b6101f9565b604051601f8301601f19908116603f01168101908282118183101715610263576102636101f9565b8160405283815286602085880101111561027c57600080fd5b836020870160208301376000602085830101528094505050505092915050565b6000602082840312156102ae57600080fd5b813567ffffffffffffffff8111156102c557600080fd5b6102d18482850161020f565b949350505050565b60005b838110156102f45781810151838201526020016102dc565b83811115610303576000848401525b50505050565b60208152600082518060208401526103288160408501602087016102d9565b601f01601f19169190910160400192915050565b6000806040838503121561034f57600080fd5b823567ffffffffffffffff8082111561036757600080fd5b6103738683870161020f565b9350602085013591508082111561038957600080fd5b506103968582860161020f565b9150509250929050565b600082516103b28184602087016102d9565b9190910192915050565b600181811c908216806103d057607f821691505b602082108114156103f157634e487b7160e01b600052602260045260246000fd5b5091905056fea26469706673582212200920867228cd6d4c39870c045168e9a7f40b55908c39755b3a2a20d7235abd7164736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b5061042d806100206000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c80631a391cb41461003b5780637b1b8e0314610050575b600080fd5b61004e61004936600461029c565b610079565b005b61006361005e366004610300565b6100b0565b604051610070919061036d565b60405180910390f35b8060008360405161008a91906103a0565b908152602001604051809103902090805190602001906100ab929190610160565b505050565b60606000826040516100c291906103a0565b908152602001604051809103902080546100db906103bc565b80601f0160208091040260200160405190810160405280929190818152602001828054610107906103bc565b80156101545780601f1061012957610100808354040283529160200191610154565b820191906000526020600020905b81548152906001019060200180831161013757829003601f168201915b50505050509050919050565b82805461016c906103bc565b90600052602060002090601f01602090048101928261018e57600085556101d4565b82601f106101a757805160ff19168380011785556101d4565b828001600101855582156101d4579182015b828111156101d45782518255916020019190600101906101b9565b506101e09291506101e4565b5090565b5b808211156101e057600081556001016101e5565b63b95aa35560e01b600052604160045260246000fd5b600082601f83011261022057600080fd5b813567ffffffffffffffff8082111561023b5761023b6101f9565b604051601f8301601f19908116603f01168101908282118183101715610263576102636101f9565b8160405283815286602085880101111561027c57600080fd5b836020870160208301376000602085830101528094505050505092915050565b600080604083850312156102af57600080fd5b823567ffffffffffffffff808211156102c757600080fd5b6102d38683870161020f565b935060208501359150808211156102e957600080fd5b506102f68582860161020f565b9150509250929050565b60006020828403121561031257600080fd5b813567ffffffffffffffff81111561032957600080fd5b6103358482850161020f565b949350505050565b60005b83811015610358578181015183820152602001610340565b83811115610367576000848401525b50505050565b602081526000825180602084015261038c81604085016020870161033d565b601f01601f19169190910160400192915050565b600082516103b281846020870161033d565b9190910192915050565b600181811c908216806103d057607f821691505b602082108114156103f15763b95aa35560e01b600052602260045260246000fd5b5091905056fea2646970667358221220b611f39d934fd1463cc6bab827e32c249db05f1cf6764f548e3b39eb0f06887264736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"key\",\"type\":\"string\"}],\"name\":\"get\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"selector\":[1765722206,2065403395],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"k\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"v\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"selector\":[3913463062,439950516],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_GET = "get";

    public static final String FUNC_SET = "set";

    protected MapTest(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public String get(String key) throws ContractException {
        final Function function =
                new Function(
                        FUNC_GET,
                        Arrays.<Type>asList(new Utf8String(key)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public TransactionReceipt set(String k, String v) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(new Utf8String(k), new Utf8String(v)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return executeTransaction(function);
    }

    public String set(String k, String v, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(new Utf8String(k), new Utf8String(v)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSet(String k, String v) {
        final Function function =
                new Function(
                        FUNC_SET,
                        Arrays.<Type>asList(new Utf8String(k), new Utf8String(v)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
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

    public static MapTest load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new MapTest(contractAddress, client, credential);
    }

    public static MapTest deploy(Client client, CryptoKeyPair credential) throws ContractException {
        return deploy(
                MapTest.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(),
                null,
                null);
    }
}
