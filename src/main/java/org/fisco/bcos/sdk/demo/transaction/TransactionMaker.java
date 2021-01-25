package org.fisco.bcos.sdk.demo.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.bouncycastle.util.encoders.Hex;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.ABICodec;
import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.crypto.signature.SignatureResult;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.builder.TransactionBuilderInterface;
import org.fisco.bcos.sdk.transaction.builder.TransactionBuilderService;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderInterface;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.codec.encode.TransactionEncoderInterface;
import org.fisco.bcos.sdk.transaction.codec.encode.TransactionEncoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.transaction.model.exception.TransactionException;
import org.fisco.bcos.sdk.transaction.model.gas.DefaultGasProvider;
import org.fisco.bcos.sdk.transaction.model.po.RawTransaction;
import org.fisco.bcos.sdk.transaction.pusher.TransactionPusherInterface;
import org.fisco.bcos.sdk.transaction.pusher.TransactionPusherService;

public class TransactionMaker {
    // 初始化BcosSDK
    BcosSDK sdk;
    // 为群组1初始化client
    Client client;

    public void setSDK(BcosSDK _sdk) {
        sdk = _sdk;
    }

    public void setClient(Client _client) {
        client = _client;
    }

    /**
     * 创建ABICodec对象
     *
     * @param client client对象
     * @return: 创建的ABICodec对象
     */
    public ABICodec createABICodec(Client client) {
        return new ABICodec(client.getCryptoSuite());
    }

    /**
     * 对合约部署类型的交易进行编码
     *
     * @param abiCodec 用于编码交易内容的ABICodeC对象
     * @param abiContent 合约的abi字符串(需要读取第1节生成的abi文件获取)
     * @param binContent binary字符串(需要读取第1节生成的bin文件获取)
     * @param params 部署合约的初始化参数列表
     * @return 编码后的交易内容
     * @throws ABICodecException
     */
    public String encodeConstructor(
            ABICodec abiCodec, String abiContent, String binContent, List<Object> params)
            throws ABICodecException {
        return abiCodec.encodeConstructor(abiContent, binContent, params);
    }

    /**
     * 对合约调用类型的交易进行编码
     *
     * @param abiCodec 用于编码交易内容的ABICodeC对象
     * @param abiContent 合约的abi字符串(需要读取第1节生成的abi文件获取)
     * @param methodName 需要调用的合约方法名
     * @return 编码后的交易内容
     * @throws ABICodecException
     */
    public String encodeMethod(
            ABICodec abiCodec, String abiContent, String methodName, List<Object> params)
            throws ABICodecException {
        return abiCodec.encodeMethod(abiContent, methodName, params);
    }

    /**
     * 根据交易内容txData构造RawTransaction，并指定交易发送的地址为to(部署合约时，to为全0的合约地址)
     *
     * @param transactionBuilder 交易构造对象
     * @param client Client对象，用于获取BlockLimit
     * @param to 交易发送的目标地址(部署合约时，to为全0的合约地址)
     * @param txData 编码后的交易
     * @return 创建的RawTransaction
     */
    public RawTransaction createRawTransaction(
            TransactionBuilderInterface transactionBuilder,
            Client client,
            String to,
            String txData) {
        // 获取chainId和groupId
        int chainId = 1;
        int groupId = 1;
        return transactionBuilder.createTransaction(
                DefaultGasProvider.GAS_PRICE,
                DefaultGasProvider.GAS_LIMIT,
                to,
                txData,
                BigInteger.ZERO,
                BigInteger.valueOf(chainId),
                BigInteger.valueOf(groupId),
                "");
    }

    /**
     * 对RawTransaction进行RLP编码，返回编码内容的哈希
     *
     * @param transactionEncoder 交易编码器
     * @param client client对象，用于确定使用的哈希算法类型
     * @param rawTransaction 需要编码的交易
     * @return 编码后交易的哈希
     */
    public byte[] encodeRawTransactionAndGetHash(
            TransactionEncoderInterface transactionEncoder,
            Client client,
            RawTransaction rawTransaction) {
        byte[] encodedTransaction = transactionEncoder.encode(rawTransaction, null);
        return client.getCryptoSuite().hash(encodedTransaction);
    }

    /**
     * 根据RawTransaction和签名结果产生带有签名的交易
     *
     * @param transactionEncoder 交易编码器
     * @param transaction 不带有签名的交易
     * @param signatureResult 签名服务器返回的反序列化的签名结果
     * @return 带有签名的交易编码
     */
    public byte[] createSignedTransaction(
            TransactionEncoderInterface transactionEncoder,
            RawTransaction transaction,
            SignatureResult signatureResult) {
        return transactionEncoder.encode(transaction, signatureResult);
    }

    /**
     * 发送带有签名的交易
     *
     * @param txPusher 交易发送器
     * @param signedTransaction 带有签名的交易
     * @return 交易回执
     */
    TransactionReceipt sendTransaction(byte[] signedTransaction) {
        // 发送签名交易
        TransactionPusherInterface txPusher = new TransactionPusherService(client);
        return txPusher.push(Hex.toHexString(signedTransaction));
    }

    // ****纯外部实现，这里比如是个http,rpc接口什么的
    // 这里的实现是调用了java-sdk签名接口，可以一次对接成功
    public static String signData(byte[] msgforSign, int cryptoType) {
        String eccPrivateKeySample =
                "28018238ac7eec853401dfc3f31133330e78ac27a2f53481270083abb1a126f9";
        CryptoSuite cryptoSuite = new CryptoSuite(cryptoType);
        CryptoKeyPair cryptoKeyPair = cryptoSuite.createKeyPair(eccPrivateKeySample);
        SignatureResult signatureResult = cryptoSuite.sign(msgforSign, cryptoKeyPair);
        System.out.println("ecc signature:" + signatureResult.convertToString());
        return signatureResult.convertToString();
    }

    // ***对接外部实现,对接外部签名硬件，KMS，签名服务等，接口拟应说明曲线名称等

    public String requestForSign(byte[] rawTxHash) {
        // 在这里实现对交易数据签名,多种方案，考虑建立个通用接口类
        String signedDataString = signData(rawTxHash, CryptoType.ECDSA_TYPE);
        return signedDataString;
    }

    public SignatureResult decodeSign(String txSignature) {
        // 返回的txSignature可能是自定义的数据结构
        SignatureResult sigRes = new ECDSASignatureResult(txSignature);
        return sigRes;
    }

    // 请求签名服务
    public SignatureResult requestForTransactionSignature(
            TransactionEncoderInterface transactionEncoder,
            RawTransaction rawTransaction,
            Client client) {
        // 获取RawTransaction的哈希
        byte[] rawTxHash =
                encodeRawTransactionAndGetHash(transactionEncoder, client, rawTransaction);
        // 请求签名服务，获取交易签名
        String signature = requestForSign(rawTxHash);
        // 对签名结果进行反序列化
        return decodeSign(signature);
    }

    // 构造交易
    public RawTransaction makeTransaction(
            Client client, String abiContent, String methodName, String to, List<Object> params)
            throws ABICodecException {
        // 1.创建ABICodeC对象
        ABICodec abiCodec = createABICodec(client);

        // 2.编码交易内容
        String txData = encodeMethod(abiCodec, abiContent, methodName, params);

        // 3. 创建TransactionBuilder，构造RawTransaction
        TransactionBuilderInterface transactionBuilder = new TransactionBuilderService(client);
        return createRawTransaction(transactionBuilder, client, to, txData);
    }

    // 根据合约abi、合约方法、合约地址发送交易，其中交易签名通过签名服务产生
    public TransactionReceipt makeAndSendSignedTransaction(
            String abiContent, String methodName, String toaddress, List<Object> params)
            throws ABICodecException, JsonProcessingException, TransactionException, IOException {

        // 创建transactionEncoder
        TransactionEncoderInterface transactionEncoder =
                new TransactionEncoderService(client.getCryptoSuite());

        // 创建RawTransaction
        RawTransaction rawTransaction =
                makeTransaction(client, abiContent, methodName, toaddress, params);
        System.out.println("1:getRawTransaction: " + rawTransaction.getData());
        // 请求签名服务签名
        SignatureResult signature =
                requestForTransactionSignature(transactionEncoder, rawTransaction, client);
        System.out.println("2:SignatureResult: " + signature.convertToString());
        // 产生带有签名的交易
        byte[] signedTransaction =
                createSignedTransaction(transactionEncoder, rawTransaction, signature);
        System.out.println("3:signedTransaction: " + new String(Hex.encode(signedTransaction)));

        TransactionReceipt receipt = sendTransaction(signedTransaction);
        System.out.println(
                "4:receipt on block : "
                        + receipt.getBlockNumber()
                        + ", output:"
                        + receipt.getOutput());

        TransactionDecoderInterface transactionDecoder =
                new TransactionDecoderService(client.getCryptoSuite());
        TransactionResponse response =
                transactionDecoder.decodeReceiptWithValues(abiContent, methodName, receipt);
        System.out.println("5:response decode: " + response.getEvents());
        return receipt;
    }
}
