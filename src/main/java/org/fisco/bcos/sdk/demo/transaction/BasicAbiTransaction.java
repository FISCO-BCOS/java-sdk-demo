package org.fisco.bcos.sdk.demo.transaction;

import java.math.BigInteger;
import java.util.List;
import org.fisco.bcos.sdk.jni.common.JniException;
import org.fisco.bcos.sdk.jni.utilities.tx.TransactionBuilderJniObj;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.model.TransactionAttribute;
import org.fisco.bcos.sdk.v3.codec.ContractCodec;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.signature.SignatureResult;
import org.fisco.bcos.sdk.v3.transaction.codec.encode.TransactionEncoderService;
import org.fisco.bcos.sdk.v3.utils.Hex;

public class BasicAbiTransaction {
    String contractName;
    String abiContent;
    String binContent;
    boolean isDeployTransaction = false;

    public boolean isDeployTransaction() {
        return isDeployTransaction;
    }

    public BasicAbiTransaction setDeployTransaction(boolean isDeployTransactoin) {
        this.isDeployTransaction = isDeployTransactoin;
        return this;
    }

    public String getBinContent() {
        return binContent;
    }

    public BasicAbiTransaction setBinContent(String binContent) {
        this.binContent = binContent;
        return this;
    }

    String methodName;
    String to;
    List<Object> params;

    public String getContractName() {
        return contractName;
    }

    public BasicAbiTransaction setContractName(String contractName) {
        this.contractName = contractName;
        return this;
    }

    BigInteger value = BigInteger.ZERO;
    String extraData = "";

    CryptoSuite cryptoSuite;
    ContractCodec ContractCodec;
    TransactionEncoderService transactionEncoder;

    public BigInteger getValue() {
        return value;
    }

    public BasicAbiTransaction setValue(BigInteger value) {
        this.value = value;
        return this;
    }

    public String getExtraData() {
        return extraData;
    }

    public BasicAbiTransaction setExtraData(String extraData) {
        this.extraData = extraData;
        return this;
    }

    public BasicAbiTransaction() {}

    public BasicAbiTransaction(
            String ContractName_,
            String abiContent_,
            String methodName_,
            String to_,
            List<Object> params_) {

        contractName = ContractName_;
        abiContent = abiContent_;
        methodName = methodName_;
        to = to_;
        params = params_;
    }

    public BasicAbiTransaction setTools(
            CryptoSuite cryptoSuite_,
            ContractCodec ContractCodec_,
            TransactionEncoderService transactionEncoder_) {
        this.cryptoSuite = cryptoSuite_;
        this.ContractCodec = ContractCodec_;
        if (this.ContractCodec == null) {
            this.ContractCodec = new ContractCodec(cryptoSuite, false);
        }
        this.transactionEncoder = transactionEncoder_;
        if (this.transactionEncoder == null) {
            this.transactionEncoder = new TransactionEncoderService(cryptoSuite);
        }
        return this;
    }

    public String getAbiContent() {
        return abiContent;
    }

    public BasicAbiTransaction setAbiContent(String abiContent) {
        this.abiContent = abiContent;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public BasicAbiTransaction setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public String getTo() {
        return to;
    }

    public BasicAbiTransaction setTo(String to) {
        this.to = to;
        return this;
    }

    public List<Object> getParams() {
        return params;
    }

    public BasicAbiTransaction setParams(List<Object> params) {
        this.params = params;
        return this;
    }

    /** 对合约部署类型的交易进行编码 */
    public byte[] encodeConstructor() throws ContractCodecException {
        return ContractCodec.encodeConstructor(abiContent, binContent, params);
    }

    /**
     * 对合约调用类型的交易进行编码
     *
     * @param ContractCodec 用于编码交易内容的ContractCodec对象
     * @return 编码后的交易内容
     * @throws ContractCodecException
     */
    public byte[] encodeMethodInput(ContractCodec ContractCodec) throws ContractCodecException {
        return ContractCodec.encodeMethod(
                this.getAbiContent(), this.getMethodName(), this.getParams());
    }

    /**
     * 对RawTransaction进行RLP编码，返回编码内容的哈希
     *
     * @param rawTransaction 需要编码的交易
     * @return 编码后交易的哈希
     */
    public byte[] calcRawTransactionHash(long rawTransaction) throws JniException {
        byte[] hash = transactionEncoder.encodeAndHashBytes(rawTransaction);
        return hash;
    }

    /**
     * 根据RawTransaction和签名结果产生带有签名的交易
     *
     * @param transaction 不带有签名的交易
     * @param signatureResult 签名服务器返回的反序列化的签名结果
     * @return 带有签名的交易编码
     */
    public byte[] encodeRawTransactionWithSignature(
            long transaction, SignatureResult signatureResult, boolean isWASM) throws JniException {
        byte[] hash = transactionEncoder.encodeAndHashBytes(transaction);
        int txAttribute = 0;
        if (isWASM) {
            txAttribute |= TransactionAttribute.LIQUID_SCALE_CODEC;
            if (this.isDeployTransaction) {
                txAttribute |= TransactionAttribute.LIQUID_CREATE;
            }
        }
        return transactionEncoder.encodeToTransactionBytes(
                transaction, hash, signatureResult, txAttribute);
    }

    public long makeRawTransaction(Client client, String chainId, String groupId)
            throws ContractCodecException, JniException {
        if (this.isDeployTransaction) return makeDeployRawTransaction(client, chainId, groupId);
        return makeMethodRawTransaction(client, chainId, groupId);
    }

    public long makeRawTransactionByInput(
            Client client, String chainId, String groupId, byte[] input) throws JniException {
        // 构造RawTransaction
        return TransactionBuilderJniObj.createTransactionData(
                groupId,
                chainId,
                to,
                Hex.toHexString(input),
                "",
                client.getBlockLimit().longValue());
    }

    public long makeDeployRawTransaction(Client client, String chainId, String groupId)
            throws ContractCodecException, JniException {
        byte[] deployInput = encodeConstructor();
        System.out.println("deploy contract bin:" + deployInput);
        System.out.println("bin size " + deployInput.length);
        // 创建TransactionBuilder，构造RawTransaction
        return makeRawTransactionByInput(client, chainId, groupId, deployInput);
    }

    // 构造未签名交易
    public long makeMethodRawTransaction(Client client, String chainId, String groupId)
            throws ContractCodecException, JniException {

        // 编码交易内容
        byte[] txInput = encodeMethodInput(ContractCodec);

        // 创建TransactionBuilder，构造RawTransaction
        return makeRawTransactionByInput(client, chainId, groupId, txInput);
    }
}
