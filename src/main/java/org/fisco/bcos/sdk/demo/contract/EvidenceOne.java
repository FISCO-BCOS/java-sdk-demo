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
public class EvidenceOne extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50610386806100206000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c806387e5fb071461003b578063f7da3f5714610050575b600080fd5b61004e6100493660046101ec565b610079565b005b61006361005e3660046102a7565b61009b565b60405161007091906102c0565b60405180910390f35b60008281526020818152604090912082516100969284019061013d565b505050565b60008181526020819052604090208054606091906100b890610315565b80601f01602080910402602001604051908101604052809291908181526020018280546100e490610315565b80156101315780601f1061010657610100808354040283529160200191610131565b820191906000526020600020905b81548152906001019060200180831161011457829003601f168201915b50505050509050919050565b82805461014990610315565b90600052602060002090601f01602090048101928261016b57600085556101b1565b82601f1061018457805160ff19168380011785556101b1565b828001600101855582156101b1579182015b828111156101b1578251825591602001919060010190610196565b506101bd9291506101c1565b5090565b5b808211156101bd57600081556001016101c2565b634e487b7160e01b600052604160045260246000fd5b600080604083850312156101ff57600080fd5b82359150602083013567ffffffffffffffff8082111561021e57600080fd5b818501915085601f83011261023257600080fd5b813581811115610244576102446101d6565b604051601f8201601f19908116603f0116810190838211818310171561026c5761026c6101d6565b8160405282815288602084870101111561028557600080fd5b8260208601602083013760006020848301015280955050505050509250929050565b6000602082840312156102b957600080fd5b5035919050565b600060208083528351808285015260005b818110156102ed578581018301518582016040015282016102d1565b818111156102ff576000604083870101525b50601f01601f1916929092016040019392505050565b600181811c9082168061032957607f821691505b6020821081141561034a57634e487b7160e01b600052602260045260246000fd5b5091905056fea26469706673582212207d08ed0e37fc4b57e74df2a429c10a407fe6902ee3010c94f0182d423c7a312364736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50610386806100206000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c8063336fee401461003b578063997f188a14610050575b600080fd5b61004e6100493660046101ec565b610079565b005b61006361005e3660046102a7565b61009b565b60405161007091906102c0565b60405180910390f35b60008281526020818152604090912082516100969284019061013d565b505050565b60008181526020819052604090208054606091906100b890610315565b80601f01602080910402602001604051908101604052809291908181526020018280546100e490610315565b80156101315780601f1061010657610100808354040283529160200191610131565b820191906000526020600020905b81548152906001019060200180831161011457829003601f168201915b50505050509050919050565b82805461014990610315565b90600052602060002090601f01602090048101928261016b57600085556101b1565b82601f1061018457805160ff19168380011785556101b1565b828001600101855582156101b1579182015b828111156101b1578251825591602001919060010190610196565b506101bd9291506101c1565b5090565b5b808211156101bd57600081556001016101c2565b63b95aa35560e01b600052604160045260246000fd5b600080604083850312156101ff57600080fd5b82359150602083013567ffffffffffffffff8082111561021e57600080fd5b818501915085601f83011261023257600080fd5b813581811115610244576102446101d6565b604051601f8201601f19908116603f0116810190838211818310171561026c5761026c6101d6565b8160405282815288602084870101111561028557600080fd5b8260208601602083013760006020848301015280955050505050509250929050565b6000602082840312156102b957600080fd5b5035919050565b600060208083528351808285015260005b818110156102ed578581018301518582016040015282016102d1565b818111156102ff576000604083870101525b50601f01601f1916929092016040019392505050565b600181811c9082168061032957607f821691505b6020821081141561034a5763b95aa35560e01b600052602260045260246000fd5b5091905056fea2646970667358221220cfe9810c8678ddce65f215a29b56de18e6a2726bbf28499ff92adbde1efe84b664736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]}],\"inputs\":[{\"internalType\":\"int256\",\"name\":\"id\",\"type\":\"int256\"}],\"name\":\"getEvidence\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"selector\":[4158275415,2575243402],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]}],\"inputs\":[{\"internalType\":\"int256\",\"name\":\"id\",\"type\":\"int256\"},{\"internalType\":\"string\",\"name\":\"evi\",\"type\":\"string\"}],\"name\":\"setEvidence\",\"outputs\":[],\"selector\":[2279996167,862973504],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_GETEVIDENCE = "getEvidence";

    public static final String FUNC_SETEVIDENCE = "setEvidence";

    protected EvidenceOne(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public String getEvidence(BigInteger id) throws ContractException {
        final Function function =
                new Function(
                        FUNC_GETEVIDENCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(id)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public TransactionReceipt setEvidence(BigInteger id, String evi) {
        final Function function =
                new Function(
                        FUNC_SETEVIDENCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(id),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(evi)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return executeTransaction(function);
    }

    public String getSignedTransactionForSetEvidence(BigInteger id, String evi) {
        final Function function =
                new Function(
                        FUNC_SETEVIDENCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(id),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(evi)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return createSignedTransaction(function);
    }

    public String setEvidence(BigInteger id, String evi, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SETEVIDENCE,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(id),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(evi)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple2<BigInteger, String> getSetEvidenceInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_SETEVIDENCE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Int256>() {},
                                new TypeReference<Utf8String>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, String>(
                (BigInteger) results.get(0).getValue(), (String) results.get(1).getValue());
    }

    public static EvidenceOne load(
            String contractAddress, Client client, CryptoKeyPair credential) {
        return new EvidenceOne(contractAddress, client, credential);
    }

    public static EvidenceOne deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                EvidenceOne.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(),
                null,
                null);
    }
}
