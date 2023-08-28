package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Address;
import org.fisco.bcos.sdk.v3.codec.datatypes.Event;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class SimpleEvidenceFactory extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50610602806100206000396000f3fe608060405234801561001057600080fd5b506004361061002b5760003560e01c80635bd8b3a214610030575b600080fd5b61004361003e3660046100e4565b610045565b005b60008282604051610055906100c1565b610060929190610199565b604051809103906000f08015801561007c573d6000803e3d6000fd5b506040516001600160a01b03821681529091507f8b94c7f6b3fadc764673ea85b4bfef3e17ce928d13e51b818ddfa891ad0f1fcc9060200160405180910390a1505050565b6103d6806101f783390190565b634e487b7160e01b600052604160045260246000fd5b600080604083850312156100f757600080fd5b823567ffffffffffffffff8082111561010f57600080fd5b818501915085601f83011261012357600080fd5b813581811115610135576101356100ce565b604051601f8201601f19908116603f0116810190838211818310171561015d5761015d6100ce565b8160405282815288602084870101111561017657600080fd5b826020860160208301376000602093820184015298969091013596505050505050565b604081526000835180604084015260005b818110156101c757602081870181015160608684010152016101aa565b818111156101d9576000606083860101525b50602083019390935250601f91909101601f19160160600191905056fe608060405234801561001057600080fd5b506040516103d63803806103d683398101604081905261002f916100fb565b815161004290600090602085019061004c565b506001555061020e565b828054610058906101d3565b90600052602060002090601f01602090048101928261007a57600085556100c0565b82601f1061009357805160ff19168380011785556100c0565b828001600101855582156100c0579182015b828111156100c05782518255916020019190600101906100a5565b506100cc9291506100d0565b5090565b5b808211156100cc57600081556001016100d1565b634e487b7160e01b600052604160045260246000fd5b6000806040838503121561010e57600080fd5b82516001600160401b038082111561012557600080fd5b818501915085601f83011261013957600080fd5b81518181111561014b5761014b6100e5565b604051601f8201601f19908116603f01168101908382118183101715610173576101736100e5565b8160405282815260209350888484870101111561018f57600080fd5b600091505b828210156101b15784820184015181830185015290830190610194565b828211156101c25760008484830101525b969092015195979596505050505050565b600181811c908216806101e757607f821691505b6020821081141561020857634e487b7160e01b600052602260045260246000fd5b50919050565b6101b98061021d6000396000f3fe608060405234801561001057600080fd5b506004361061002b5760003560e01c8063596f21f814610030575b600080fd5b61003861004f565b6040516100469291906100eb565b60405180910390f35b606060008060015481805461006390610148565b80601f016020809104026020016040519081016040528092919081815260200182805461008f90610148565b80156100dc5780601f106100b1576101008083540402835291602001916100dc565b820191906000526020600020905b8154815290600101906020018083116100bf57829003601f168201915b50505050509150915091509091565b604081526000835180604084015260005b8181101561011957602081870181015160608684010152016100fc565b8181111561012b576000606083860101525b50602083019390935250601f91909101601f191601606001919050565b600181811c9082168061015c57607f821691505b6020821081141561017d57634e487b7160e01b600052602260045260246000fd5b5091905056fea2646970667358221220940d75243a70f881e10b5aa0645863ed642fd429cd7b15bffb944c5788175b7b64736f6c634300080b0033a2646970667358221220092d4a55b0166b3d68186328108e15dfeb0918abb5e763215144c935d18b264764736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50610602806100206000396000f3fe608060405234801561001057600080fd5b506004361061002b5760003560e01c806347ef631614610030575b600080fd5b61004361003e3660046100e4565b610045565b005b60008282604051610055906100c1565b610060929190610199565b604051809103906000f08015801561007c573d6000803e3d6000fd5b506040516001600160a01b03821681529091507ffce723060091dd1452a91ae12d05541e3141b37fa27968bc557add04601f74d09060200160405180910390a1505050565b6103d6806101f783390190565b63b95aa35560e01b600052604160045260246000fd5b600080604083850312156100f757600080fd5b823567ffffffffffffffff8082111561010f57600080fd5b818501915085601f83011261012357600080fd5b813581811115610135576101356100ce565b604051601f8201601f19908116603f0116810190838211818310171561015d5761015d6100ce565b8160405282815288602084870101111561017657600080fd5b826020860160208301376000602093820184015298969091013596505050505050565b604081526000835180604084015260005b818110156101c757602081870181015160608684010152016101aa565b818111156101d9576000606083860101525b50602083019390935250601f91909101601f19160160600191905056fe608060405234801561001057600080fd5b506040516103d63803806103d683398101604081905261002f916100fb565b815161004290600090602085019061004c565b506001555061020e565b828054610058906101d3565b90600052602060002090601f01602090048101928261007a57600085556100c0565b82601f1061009357805160ff19168380011785556100c0565b828001600101855582156100c0579182015b828111156100c05782518255916020019190600101906100a5565b506100cc9291506100d0565b5090565b5b808211156100cc57600081556001016100d1565b63b95aa35560e01b600052604160045260246000fd5b6000806040838503121561010e57600080fd5b82516001600160401b038082111561012557600080fd5b818501915085601f83011261013957600080fd5b81518181111561014b5761014b6100e5565b604051601f8201601f19908116603f01168101908382118183101715610173576101736100e5565b8160405282815260209350888484870101111561018f57600080fd5b600091505b828210156101b15784820184015181830185015290830190610194565b828211156101c25760008484830101525b969092015195979596505050505050565b600181811c908216806101e757607f821691505b602082108114156102085763b95aa35560e01b600052602260045260246000fd5b50919050565b6101b98061021d6000396000f3fe608060405234801561001057600080fd5b506004361061002b5760003560e01c80634ae70cef14610030575b600080fd5b61003861004f565b6040516100469291906100eb565b60405180910390f35b606060008060015481805461006390610148565b80601f016020809104026020016040519081016040528092919081815260200182805461008f90610148565b80156100dc5780601f106100b1576101008083540402835291602001916100dc565b820191906000526020600020905b8154815290600101906020018083116100bf57829003601f168201915b50505050509150915091509091565b604081526000835180604084015260005b8181101561011957602081870181015160608684010152016100fc565b8181111561012b576000606083860101525b50602083019390935250601f91909101601f191601606001919050565b600181811c9082168061015c57607f821691505b6020821081141561017d5763b95aa35560e01b600052602260045260246000fd5b5091905056fea2646970667358221220efc061a98e40cc06a1de894dc8169b2538b84b632ac867f7ea55242c1244705764736f6c634300080b0033a2646970667358221220b4fd7c33feb3c173257d70fc38317f8b125e5a436800be57a09fb2d3120d867164736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"address\",\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"newEvidenceEvent\",\"type\":\"event\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"evidence\",\"type\":\"string\"},{\"internalType\":\"int256\",\"name\":\"id\",\"type\":\"int256\"}],\"name\":\"newEvidence\",\"outputs\":[],\"selector\":[1540928418,1206870806],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_NEWEVIDENCE = "newEvidence";

    public static final Event NEWEVIDENCEEVENT_EVENT =
            new Event(
                    "newEvidenceEvent",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));;

    protected SimpleEvidenceFactory(
            String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public List<NewEvidenceEventEventResponse> getNewEvidenceEventEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList =
                extractEventParametersWithLog(NEWEVIDENCEEVENT_EVENT, transactionReceipt);
        ArrayList<NewEvidenceEventEventResponse> responses =
                new ArrayList<NewEvidenceEventEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            NewEvidenceEventEventResponse typedResponse = new NewEvidenceEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.addr = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public TransactionReceipt newEvidence(String evidence, BigInteger id) {
        final Function function =
                new Function(
                        FUNC_NEWEVIDENCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(evidence),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(id)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String getSignedTransactionForNewEvidence(String evidence, BigInteger id) {
        final Function function =
                new Function(
                        FUNC_NEWEVIDENCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(evidence),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(id)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public String newEvidence(String evidence, BigInteger id, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_NEWEVIDENCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(evidence),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(id)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple2<String, BigInteger> getNewEvidenceInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_NEWEVIDENCE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Int256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(
                (String) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    public static SimpleEvidenceFactory load(
            String contractAddress, Client client, CryptoKeyPair credential) {
        return new SimpleEvidenceFactory(contractAddress, client, credential);
    }

    public static SimpleEvidenceFactory deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                SimpleEvidenceFactory.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(),
                null,
                null);
    }

    public static class NewEvidenceEventEventResponse {
        public TransactionReceipt.Logs log;

        public String addr;
    }
}
