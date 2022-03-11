package org.fisco.bcos.sdk.demo.transaction;

import org.fisco.bcos.sdk.jni.common.JniException;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.crypto.signature.SignatureResult;

public class KeyToolSignTransaction implements ISignTransaction {
    public KeyToolSignTransaction(CryptoSuite cryptoSuite) {
        setCryptoSuite(cryptoSuite);
    }

    public CryptoSuite getCryptoSuite() {
        return cryptoSuite;
    }

    public void setCryptoSuite(CryptoSuite cryptoSuite) {
        this.cryptoSuite = cryptoSuite;
    }

    CryptoSuite cryptoSuite;

    // ****纯外部实现，这里比如是个http,rpc接口什么的
    // 这里的实现是调用了java-sdk签名接口，可以一次对接成功
    public String signData(byte[] dataToSign, int crytoType) {

        CryptoKeyPair cryptoKeyPair = cryptoSuite.getCryptoKeyPair();
        // 这里cryptoSuite的实现已经自动适配国密和ECDSA，不需要用crytoType了
        SignatureResult signatureResult = cryptoSuite.sign(dataToSign, cryptoKeyPair);
        System.out.println(
                "crypto type:"
                        + crytoType
                        + ",signData -> signature:"
                        + signatureResult.convertToString());
        return signatureResult.convertToString();
    }

    public String requestForSign(byte[] rawTxHash, int crytoType) {

        String signedDataString = signData(rawTxHash, crytoType);
        return signedDataString;
    }

    /*模拟异步调用，demo代码比较简单，就本地直接同步回调了，可以改成启动一个签名线程*/
    public void requestForSignAsync(
            byte[] dataToSign, int crytotype, ISignedTransactionCallback callback)
            throws JniException {

        String signatureStr = requestForSign(dataToSign, crytotype);
        if (callback != null) {
            callback.handleSignedTransaction(signatureStr);
        }
    }
}
