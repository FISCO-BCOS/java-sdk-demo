package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Address;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class DelegateCallTest extends Contract {
    public static final String[] BINARY_ARRAY = {"60806040526000805534801561001457600080fd5b50600280546001600160a01b0319163017905560405161003390610075565b604051809103906000f08015801561004f573d6000803e3d6000fd5b50600580546001600160a01b0319166001600160a01b0392909216919091179055610082565b610223806104d083390190565b61043f806100916000396000f3fe608060405234801561001057600080fd5b50600436106100935760003560e01c80634f2be91f116100665780634f2be91f1461010757806367e404ce1461010f578063713d3e3e14610122578063986d9f841461012a578063aec32fb01461013c57600080fd5b80630d216bba146100985780630ddbcc82146100c857806326b85ee1146100dd5780633fa4f245146100f0575b600080fd5b6005546100ab906001600160a01b031681565b6040516001600160a01b0390911681526020015b60405180910390f35b6100d061014e565b6040516100bf9190610333565b6002546100ab906001600160a01b031681565b6100f960005481565b6040519081526020016100bf565b6100d0610184565b6001546100ab906001600160a01b031681565b6100d06101ce565b6100f9610138366004610366565b3b90565b6100f961014a366004610366565b3f90565b60606000611001905061017e8160405180604001604052806005815260200164616464282960d81b815250610208565b91505090565b600180546001600160a01b0319163317815560008054606092919081906101ac9084906103ac565b90915550506040805180820190915260018152603160f81b6020820152919050565b600580546040805180820190915291825264616464282960d81b6020830152606091610203916001600160a01b031690610208565b905090565b6040805160048152602481019182905260609160009183916001600160a01b03871691906102379087906103ed565b60408051918290039091206020830180516001600160e01b03166001600160e01b03199092169190911790525161026e91906103ed565b600060405180830381855af49150503d80600081146102a9576040519150601f19603f3d011682016040523d82523d6000602084013e6102ae565b606091505b509092509050816102fb5760405162461bcd60e51b815260206004820152601360248201527219195b1959d85d1958d85b1b0819985a5b1959606a1b604482015260640160405180910390fd5b949350505050565b60005b8381101561031e578181015183820152602001610306565b8381111561032d576000848401525b50505050565b6020815260008251806020840152610352816040850160208701610303565b601f01601f19169190910160400192915050565b60006020828403121561037857600080fd5b81356001600160a01b038116811461038f57600080fd5b9392505050565b634e487b7160e01b600052601160045260246000fd5b600080821280156001600160ff1b03849003851316156103ce576103ce610396565b600160ff1b83900384128116156103e7576103e7610396565b50500190565b600082516103ff818460208701610303565b919091019291505056fea26469706673582212203f46fd2a3982d7dd2e0460456814094814216a871a8ccdba5a109274bbb13bcf64736f6c634300080b003360806040526000805534801561001457600080fd5b50600280546001600160a01b031916301790556101ed806100366000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c806326b85ee1146100515780633fa4f245146100815780634f2be91f1461009857806367e404ce146100ad575b600080fd5b600254610064906001600160a01b031681565b6040516001600160a01b0390911681526020015b60405180910390f35b61008a60005481565b604051908152602001610078565b6100a06100c0565b604051610078919061010b565b600154610064906001600160a01b031681565b600180546001600160a01b031916331790556000805460609160029181906100e9908490610176565b90915550506040805180820190915260018152601960f91b6020820152919050565b600060208083528351808285015260005b818110156101385785810183015185820160400152820161011c565b8181111561014a576000604083870101525b50601f01601f1916929092016040019392505050565b634e487b7160e01b600052601160045260246000fd5b600080821280156001600160ff1b038490038513161561019857610198610160565b600160ff1b83900384128116156101b1576101b1610160565b5050019056fea2646970667358221220fdb654baf28b14f03f6490feb414a9eff7d850acc49b9024187549f8b06805de64736f6c634300080b0033"};

    public static final String BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"60806040526000805534801561001457600080fd5b50600280546001600160a01b0319163017905560405161003390610075565b604051809103906000f08015801561004f573d6000803e3d6000fd5b50600580546001600160a01b0319166001600160a01b0392909216919091179055610082565b610223806104d183390190565b610440806100916000396000f3fe608060405234801561001057600080fd5b50600436106100935760003560e01c806386f928011161006657806386f92801146100ed5780639aeb848914610118578063bd24dbdc14610120578063c7c6ae1014610133578063d89b65811461013b57600080fd5b80632d92c59d1461009857806369861710146100b457806381b2b19f146100c6578063820df6c8146100d8575b600080fd5b6100a160005481565b6040519081526020015b60405180910390f35b6100a16100c2366004610304565b3b90565b6100a16100d4366004610304565b3f90565b6100e061014e565b6040516100ab9190610364565b600554610100906001600160a01b031681565b6040516001600160a01b0390911681526020016100ab565b6100e0610198565b600254610100906001600160a01b031681565b6100e06101d2565b600154610100906001600160a01b031681565b600180546001600160a01b0319163317815560008054606092919081906101769084906103ad565b90915550506040805180820190915260018152603160f81b6020820152919050565b600580546040805180820190915291825264616464282960d81b60208301526060916101cd916001600160a01b031690610208565b905090565b6060600061100190506102028160405180604001604052806005815260200164616464282960d81b815250610208565b91505090565b6040805160048152602481019182905260609160009183916001600160a01b03871691906102379087906103ee565b60408051918290039091206020830180516001600160e01b03166001600160e01b03199092169190911790525161026e91906103ee565b600060405180830381855af49150503d80600081146102a9576040519150601f19603f3d011682016040523d82523d6000602084013e6102ae565b606091505b509092509050816102fc57604051636381e58960e11b815260206004820152601360248201527219195b1959d85d1958d85b1b0819985a5b1959606a1b604482015260640160405180910390fd5b949350505050565b60006020828403121561031657600080fd5b81356001600160a01b038116811461032d57600080fd5b9392505050565b60005b8381101561034f578181015183820152602001610337565b8381111561035e576000848401525b50505050565b6020815260008251806020840152610383816040850160208701610334565b601f01601f19169190910160400192915050565b63b95aa35560e01b600052601160045260246000fd5b600080821280156001600160ff1b03849003851316156103cf576103cf610397565b600160ff1b83900384128116156103e8576103e8610397565b50500190565b60008251610400818460208701610334565b919091019291505056fea264697066735822122043ec97eed83998d1d6096da69f9e534fc3f7d19f00decfef6ee990c0b2d94c2d64736f6c634300080b003360806040526000805534801561001457600080fd5b50600280546001600160a01b031916301790556101ed806100366000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c80632d92c59d14610051578063820df6c81461006d578063bd24dbdc14610082578063d89b6581146100ad575b600080fd5b61005a60005481565b6040519081526020015b60405180910390f35b6100756100c0565b604051610064919061010b565b600254610095906001600160a01b031681565b6040516001600160a01b039091168152602001610064565b600154610095906001600160a01b031681565b600180546001600160a01b031916331790556000805460609160029181906100e9908490610176565b90915550506040805180820190915260018152601960f91b6020820152919050565b600060208083528351808285015260005b818110156101385785810183015185820160400152820161011c565b8181111561014a576000604083870101525b50601f01601f1916929092016040019392505050565b63b95aa35560e01b600052601160045260246000fd5b600080821280156001600160ff1b038490038513161561019857610198610160565b600160ff1b83900384128116156101b1576101b1610160565b5050019056fea2646970667358221220c02b5c177174c85f7b6efa061822aea0d88dfd98cef86b3c2694c796098a70f364736f6c634300080b0033"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"inputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"conflictFields\":[{\"kind\":4,\"value\":[0]},{\"kind\":4,\"value\":[1]},{\"kind\":4,\"value\":[5]}],\"inputs\":[],\"name\":\"add\",\"outputs\":[{\"internalType\":\"bytes\",\"name\":\"\",\"type\":\"bytes\"}],\"selector\":[1328277791,2181953224],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":5}],\"inputs\":[{\"internalType\":\"address\",\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"codehashAt\",\"outputs\":[{\"internalType\":\"bytes32\",\"name\":\"\",\"type\":\"bytes32\"}],\"selector\":[2932027312,2175971743],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":5}],\"inputs\":[{\"internalType\":\"address\",\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"codesizeAt\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"selector\":[2557321092,1770395408],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":4,\"value\":[5]}],\"inputs\":[],\"name\":\"delegateDest\",\"outputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"selector\":[220294074,2264475649],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":4,\"value\":[2]}],\"inputs\":[],\"name\":\"myAddress\",\"outputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"selector\":[649617121,3173309404],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":4,\"value\":[1]}],\"inputs\":[],\"name\":\"sender\",\"outputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"selector\":[1742996686,3634062721],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[],\"name\":\"testFailed\",\"outputs\":[{\"internalType\":\"bytes\",\"name\":\"\",\"type\":\"bytes\"}],\"selector\":[232508546,3351686672],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[],\"name\":\"testSuccess\",\"outputs\":[{\"internalType\":\"bytes\",\"name\":\"\",\"type\":\"bytes\"}],\"selector\":[1899839038,2599126153],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":4,\"value\":[0]}],\"inputs\":[],\"name\":\"value\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"selector\":[1067774533,764593565],\"stateMutability\":\"view\",\"type\":\"function\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_ADD = "add";

    public static final String FUNC_CODEHASHAT = "codehashAt";

    public static final String FUNC_CODESIZEAT = "codesizeAt";

    public static final String FUNC_DELEGATEDEST = "delegateDest";

    public static final String FUNC_MYADDRESS = "myAddress";

    public static final String FUNC_SENDER = "sender";

    public static final String FUNC_TESTFAILED = "testFailed";

    public static final String FUNC_TESTSUCCESS = "testSuccess";

    public static final String FUNC_VALUE = "value";

    protected DelegateCallTest(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public TransactionReceipt add() {
        final Function function = new Function(
                FUNC_ADD, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String add(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_ADD, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForAdd() {
        final Function function = new Function(
                FUNC_ADD, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple1<byte[]> getAddOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_ADD, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public TransactionReceipt codehashAt(String addr) {
        final Function function = new Function(
                FUNC_CODEHASHAT, 
                Arrays.<Type>asList(new Address(addr)),
                Collections.<TypeReference<?>>emptyList(), 4);
        return executeTransaction(function);
    }

    public String codehashAt(String addr, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_CODEHASHAT, 
                Arrays.<Type>asList(new Address(addr)),
                Collections.<TypeReference<?>>emptyList(), 4);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForCodehashAt(String addr) {
        final Function function = new Function(
                FUNC_CODEHASHAT, 
                Arrays.<Type>asList(new Address(addr)),
                Collections.<TypeReference<?>>emptyList(), 4);
        return createSignedTransaction(function);
    }

    public Tuple1<String> getCodehashAtInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_CODEHASHAT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public Tuple1<byte[]> getCodehashAtOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_CODEHASHAT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public TransactionReceipt codesizeAt(String addr) {
        final Function function = new Function(
                FUNC_CODESIZEAT, 
                Arrays.<Type>asList(new Address(addr)),
                Collections.<TypeReference<?>>emptyList(), 4);
        return executeTransaction(function);
    }

    public String codesizeAt(String addr, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_CODESIZEAT, 
                Arrays.<Type>asList(new Address(addr)),
                Collections.<TypeReference<?>>emptyList(), 4);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForCodesizeAt(String addr) {
        final Function function = new Function(
                FUNC_CODESIZEAT, 
                Arrays.<Type>asList(new Address(addr)),
                Collections.<TypeReference<?>>emptyList(), 4);
        return createSignedTransaction(function);
    }

    public Tuple1<String> getCodesizeAtInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_CODESIZEAT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public Tuple1<BigInteger> getCodesizeAtOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_CODESIZEAT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
                );
    }

    public String delegateDest() throws ContractException {
        final Function function = new Function(FUNC_DELEGATEDEST, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public String myAddress() throws ContractException {
        final Function function = new Function(FUNC_MYADDRESS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public String sender() throws ContractException {
        final Function function = new Function(FUNC_SENDER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public TransactionReceipt testFailed() {
        final Function function = new Function(
                FUNC_TESTFAILED, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String testFailed(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_TESTFAILED, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForTestFailed() {
        final Function function = new Function(
                FUNC_TESTFAILED, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple1<byte[]> getTestFailedOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_TESTFAILED, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public TransactionReceipt testSuccess() {
        final Function function = new Function(
                FUNC_TESTSUCCESS, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String testSuccess(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_TESTSUCCESS, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForTestSuccess() {
        final Function function = new Function(
                FUNC_TESTSUCCESS, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple1<byte[]> getTestSuccessOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_TESTSUCCESS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<byte[]>(

                (byte[]) results.get(0).getValue()
                );
    }

    public BigInteger value() throws ContractException {
        final Function function = new Function(FUNC_VALUE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public static DelegateCallTest load(String contractAddress, Client client,
            CryptoKeyPair credential) {
        return new DelegateCallTest(contractAddress, client, credential);
    }

    public static DelegateCallTest deploy(Client client, CryptoKeyPair credential) throws
            ContractException {
        return deploy(DelegateCallTest.class, client, credential, getBinary(client.getCryptoSuite()), getABI(), null, null);
    }
}
