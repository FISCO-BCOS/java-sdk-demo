package org.fisco.bcos.sdk.demo.transaction;

import org.fisco.bcos.sdk.jni.common.JniException;

public interface ISignTransaction {
    /*同步的签名接口，不需要传入回调，直接返回签名结果
     * cryptoType: ECDSA=0,SM=1等,也可以约定其他，可修改类型以扩展更多参数
     * */
    public String requestForSign(byte[] rawTxHash, int cryptoType);
    /*异步的签名接口，传入回调，当远程调用签名服务时，可以采用异步回调风格，避免堵塞
     *  cryptoType: ECDSA=0,SM=1等,也可以约定其他，可修改类型以扩展更多参数
     * */
    public void requestForSignAsync(
            byte[] dateToSign, int cryptoType, ISignedTransactionCallback callback)
            throws JniException;
}
