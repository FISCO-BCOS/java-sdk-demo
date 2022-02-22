package org.fisco.bcos.sdk.demo.transaction;

import org.fisco.bcos.sdk.jni.common.JniException;

public interface ISignedTransactionCallback {
    /*当采用异步签名方式时，如调用网络上的加密机，KMS，异步不堵塞，然后签名完成后回调接口*/
    public int handleSignedTransaction(String signatureStr) throws JniException;
}
