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
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class CpuHeavy extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50610469806100206000396000f3fe608060405234801561001057600080fd5b506004361061002b5760003560e01c80637b395ec214610030575b600080fd5b61004361003e3660046102aa565b610045565b005b60008267ffffffffffffffff811115610060576100606102cc565b604051908082528060200260200182016040528015610089578160200160208202803683370190505b50905060005b81518110156100d1576100a281856102f8565b8282815181106100b4576100b461030f565b6020908102919091010152806100c981610325565b91505061008f565b506100eb816000600184516100e691906102f8565b610129565b60408051848152602081018490527fd596fdad182d29130ce218f4c1590c4b5ede105bee36690727baa6592bd2bfc8910160405180910390a1505050565b81818082141561013a575050505050565b60008560026101498787610340565b610153919061037f565b61015d90876103bb565b8151811061016d5761016d61030f565b602002602001015190505b81831361027c575b808684815181106101935761019361030f565b602002602001015110156101b357826101ab816103fc565b935050610180565b8582815181106101c5576101c561030f565b60200260200101518110156101e657816101de81610415565b9250506101b3565b818313610277578582815181106101ff576101ff61030f565b60200260200101518684815181106102195761021961030f565b60200260200101518785815181106102335761023361030f565b6020026020010188858151811061024c5761024c61030f565b60209081029190910101919091525282610265816103fc565b935050818061027390610415565b9250505b610178565b8185121561028f5761028f868684610129565b838312156102a2576102a2868486610129565b505050505050565b600080604083850312156102bd57600080fd5b50508035926020909101359150565b634e487b7160e01b600052604160045260246000fd5b634e487b7160e01b600052601160045260246000fd5b60008282101561030a5761030a6102e2565b500390565b634e487b7160e01b600052603260045260246000fd5b6000600019821415610339576103396102e2565b5060010190565b60008083128015600160ff1b85018412161561035e5761035e6102e2565b6001600160ff1b0384018313811615610379576103796102e2565b50500390565b60008261039c57634e487b7160e01b600052601260045260246000fd5b600160ff1b8214600019841416156103b6576103b66102e2565b500590565b600080821280156001600160ff1b03849003851316156103dd576103dd6102e2565b600160ff1b83900384128116156103f6576103f66102e2565b50500190565b60006001600160ff1b03821415610339576103396102e2565b6000600160ff1b82141561042b5761042b6102e2565b50600019019056fea26469706673582212201b3c5fd9ad7c06506361958dae1e24642f52ab5071376dfab0a29606d3eea0ba64736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b50610469806100206000396000f3fe608060405234801561001057600080fd5b506004361061002b5760003560e01c806374f1c79514610030575b600080fd5b61004361003e3660046102aa565b610045565b005b60008267ffffffffffffffff811115610060576100606102cc565b604051908082528060200260200182016040528015610089578160200160208202803683370190505b50905060005b81518110156100d1576100a281856102f8565b8282815181106100b4576100b461030f565b6020908102919091010152806100c981610325565b91505061008f565b506100eb816000600184516100e691906102f8565b610129565b60408051848152602081018490527f24ca5594ba8a48a066178b3c63c028d9743b8f480eeaf4dc2d6295f65a1fff4a910160405180910390a1505050565b81818082141561013a575050505050565b60008560026101498787610340565b610153919061037f565b61015d90876103bb565b8151811061016d5761016d61030f565b602002602001015190505b81831361027c575b808684815181106101935761019361030f565b602002602001015110156101b357826101ab816103fc565b935050610180565b8582815181106101c5576101c561030f565b60200260200101518110156101e657816101de81610415565b9250506101b3565b818313610277578582815181106101ff576101ff61030f565b60200260200101518684815181106102195761021961030f565b60200260200101518785815181106102335761023361030f565b6020026020010188858151811061024c5761024c61030f565b60209081029190910101919091525282610265816103fc565b935050818061027390610415565b9250505b610178565b8185121561028f5761028f868684610129565b838312156102a2576102a2868486610129565b505050505050565b600080604083850312156102bd57600080fd5b50508035926020909101359150565b63b95aa35560e01b600052604160045260246000fd5b63b95aa35560e01b600052601160045260246000fd5b60008282101561030a5761030a6102e2565b500390565b63b95aa35560e01b600052603260045260246000fd5b6000600019821415610339576103396102e2565b5060010190565b60008083128015600160ff1b85018412161561035e5761035e6102e2565b6001600160ff1b0384018313811615610379576103796102e2565b50500390565b60008261039c5763b95aa35560e01b600052601260045260246000fd5b600160ff1b8214600019841416156103b6576103b66102e2565b500590565b600080821280156001600160ff1b03849003851316156103dd576103dd6102e2565b600160ff1b83900384128116156103f6576103f66102e2565b50500190565b60006001600160ff1b03821415610339576103396102e2565b6000600160ff1b82141561042b5761042b6102e2565b50600019019056fea2646970667358221220964239a101ccb2a93a21e95a9e31b28c8e5f13c5421a689fe5f5ffcee6595b9264736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"size\",\"type\":\"uint256\"},{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"signature\",\"type\":\"uint256\"}],\"name\":\"finish\",\"type\":\"event\"},{\"conflictFields\":[{\"kind\":5}],\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"size\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"signature\",\"type\":\"uint256\"}],\"name\":\"sort\",\"outputs\":[],\"selector\":[2067357378,1962002325],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_SORT = "sort";

    public static final Event FINISH_EVENT =
            new Event(
                    "finish",
                    Arrays.<TypeReference<?>>asList(
                            new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));;

    protected CpuHeavy(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public List<FinishEventResponse> getFinishEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList =
                extractEventParametersWithLog(FINISH_EVENT, transactionReceipt);
        ArrayList<FinishEventResponse> responses =
                new ArrayList<FinishEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            FinishEventResponse typedResponse = new FinishEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.size = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.signature =
                    (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public TransactionReceipt sort(BigInteger size, BigInteger signature) {
        final Function function =
                new Function(
                        FUNC_SORT,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(size),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(
                                        signature)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return executeTransaction(function);
    }

    public String sort(BigInteger size, BigInteger signature, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SORT,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(size),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(
                                        signature)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSort(BigInteger size, BigInteger signature) {
        final Function function =
                new Function(
                        FUNC_SORT,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(size),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(
                                        signature)),
                        Collections.<TypeReference<?>>emptyList(),
                        4);
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

    public static CpuHeavy load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new CpuHeavy(contractAddress, client, credential);
    }

    public static CpuHeavy deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                CpuHeavy.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(),
                null,
                null);
    }

    public static class FinishEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger size;

        public BigInteger signature;
    }
}
