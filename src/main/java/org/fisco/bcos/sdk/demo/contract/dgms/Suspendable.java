package org.fisco.bcos.sdk.demo.contract.dgms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.abi.FunctionEncoder;
import org.fisco.bcos.sdk.v3.codec.datatypes.Address;
import org.fisco.bcos.sdk.v3.codec.datatypes.Bool;
import org.fisco.bcos.sdk.v3.codec.datatypes.Event;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class Suspendable extends Contract {
    public static final String[] BINARY_ARRAY = {};

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {};

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"constant\":false,\"inputs\":[{\"name\":\"account\",\"type\":\"address\"}],\"name\":\"addIssuer\",\"outputs\":[],\"payable\":false,\"selector\":[543772080,596820072],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"account\",\"type\":\"address\"}],\"name\":\"removeIssuer\",\"outputs\":[],\"payable\":false,\"selector\":[1203531923,2051880263],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"suspended\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"selector\":[1882127859,2039485372],\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"account\",\"type\":\"address\"}],\"name\":\"isIssuer\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"selector\":[2273024615,45068934],\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"unSuspend\",\"outputs\":[],\"payable\":false,\"selector\":[3623580105,1304026934],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"suspend\",\"outputs\":[],\"payable\":false,\"selector\":[3862956990,1796534436],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"admin\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"account\",\"type\":\"address\"}],\"name\":\"Suspended\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"account\",\"type\":\"address\"}],\"name\":\"UnSuspended\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"account\",\"type\":\"address\"}],\"name\":\"IssuerAdded\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"account\",\"type\":\"address\"}],\"name\":\"IssuerRemoved\",\"type\":\"event\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_ADDISSUER = "addIssuer";

    public static final String FUNC_REMOVEISSUER = "removeIssuer";

    public static final String FUNC_SUSPENDED = "suspended";

    public static final String FUNC_ISISSUER = "isIssuer";

    public static final String FUNC_UNSUSPEND = "unSuspend";

    public static final String FUNC_SUSPEND = "suspend";

    public static final Event SUSPENDED_EVENT =
            new Event(
                    "Suspended", Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));;

    public static final Event UNSUSPENDED_EVENT =
            new Event(
                    "UnSuspended",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));;

    public static final Event ISSUERADDED_EVENT =
            new Event(
                    "IssuerAdded",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));;

    public static final Event ISSUERREMOVED_EVENT =
            new Event(
                    "IssuerRemoved",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));;

    protected Suspendable(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public TransactionReceipt addIssuer(String account) {
        final Function function =
                new Function(
                        FUNC_ADDISSUER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(account)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String addIssuer(String account, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_ADDISSUER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(account)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForAddIssuer(String account) {
        final Function function =
                new Function(
                        FUNC_ADDISSUER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(account)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple1<String> getAddIssuerInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_ADDISSUER,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>((String) results.get(0).getValue());
    }

    public TransactionReceipt removeIssuer(String account) {
        final Function function =
                new Function(
                        FUNC_REMOVEISSUER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(account)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String removeIssuer(String account, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_REMOVEISSUER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(account)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForRemoveIssuer(String account) {
        final Function function =
                new Function(
                        FUNC_REMOVEISSUER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(account)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple1<String> getRemoveIssuerInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_REMOVEISSUER,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>((String) results.get(0).getValue());
    }

    public Boolean suspended() throws ContractException {
        final Function function =
                new Function(
                        FUNC_SUSPENDED,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeCallWithSingleValueReturn(function, Boolean.class);
    }

    public Boolean isIssuer(String account) throws ContractException {
        final Function function =
                new Function(
                        FUNC_ISISSUER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(account)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeCallWithSingleValueReturn(function, Boolean.class);
    }

    public TransactionReceipt unSuspend() {
        final Function function =
                new Function(
                        FUNC_UNSUSPEND,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String unSuspend(TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_UNSUSPEND,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForUnSuspend() {
        final Function function =
                new Function(
                        FUNC_UNSUSPEND,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public TransactionReceipt suspend() {
        final Function function =
                new Function(
                        FUNC_SUSPEND,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String suspend(TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SUSPEND,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSuspend() {
        final Function function =
                new Function(
                        FUNC_SUSPEND,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public List<SuspendedEventResponse> getSuspendedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList =
                extractEventParametersWithLog(SUSPENDED_EVENT, transactionReceipt);
        ArrayList<SuspendedEventResponse> responses =
                new ArrayList<SuspendedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            SuspendedEventResponse typedResponse = new SuspendedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.account = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public List<UnSuspendedEventResponse> getUnSuspendedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList =
                extractEventParametersWithLog(UNSUSPENDED_EVENT, transactionReceipt);
        ArrayList<UnSuspendedEventResponse> responses =
                new ArrayList<UnSuspendedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            UnSuspendedEventResponse typedResponse = new UnSuspendedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.account = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public List<IssuerAddedEventResponse> getIssuerAddedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList =
                extractEventParametersWithLog(ISSUERADDED_EVENT, transactionReceipt);
        ArrayList<IssuerAddedEventResponse> responses =
                new ArrayList<IssuerAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            IssuerAddedEventResponse typedResponse = new IssuerAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.account = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public List<IssuerRemovedEventResponse> getIssuerRemovedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList =
                extractEventParametersWithLog(ISSUERREMOVED_EVENT, transactionReceipt);
        ArrayList<IssuerRemovedEventResponse> responses =
                new ArrayList<IssuerRemovedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            IssuerRemovedEventResponse typedResponse = new IssuerRemovedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.account = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static Suspendable load(
            String contractAddress, Client client, CryptoKeyPair credential) {
        return new Suspendable(contractAddress, client, credential);
    }

    public static Suspendable deploy(Client client, CryptoKeyPair credential, String admin)
            throws ContractException {
        byte[] encodedConstructor =
                FunctionEncoder.encodeConstructor(
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(admin)));
        return deploy(
                Suspendable.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(),
                encodedConstructor,
                null);
    }

    public static class SuspendedEventResponse {
        public TransactionReceipt.Logs log;

        public String account;
    }

    public static class UnSuspendedEventResponse {
        public TransactionReceipt.Logs log;

        public String account;
    }

    public static class IssuerAddedEventResponse {
        public TransactionReceipt.Logs log;

        public String account;
    }

    public static class IssuerRemovedEventResponse {
        public TransactionReceipt.Logs log;

        public String account;
    }
}
