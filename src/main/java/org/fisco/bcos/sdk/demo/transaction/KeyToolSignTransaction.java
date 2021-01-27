package org.fisco.bcos.sdk.demo.transaction;

import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.signature.SignatureResult;

public class KeyToolSignTransaction implements ISignTransaction {
    public KeyToolSignTransaction(CryptoSuite cryptoSuite)
    {
    	setCryptoSuite(cryptoSuite);
    }
	
	public CryptoSuite getCryptoSuite() {
		return cryptoSuite;
	}

	public void setCryptoSuite(CryptoSuite cryptoSuite) {
		this.cryptoSuite = cryptoSuite;

	}



	CryptoSuite cryptoSuite ;
	
	// ****纯外部实现，这里比如是个http,rpc接口什么的
    // 这里的实现是调用了java-sdk签名接口，可以一次对接成功
    public String signData(byte[] msgforSign) {
        CryptoKeyPair cryptoKeyPair = cryptoSuite.getCryptoKeyPair();
        SignatureResult signatureResult = cryptoSuite.sign(msgforSign, cryptoKeyPair);
        System.out.println("signData -> signature:" + signatureResult.convertToString());
        return signatureResult.convertToString();
    }

	public String requestForSign(byte[] rawTxHash) {
		
        String signedDataString = signData(rawTxHash);
        return signedDataString;
	}

	/*模拟异步调用，demo代码比较简单，就本地直接同步回调了，可以改成启动一个签名线程*/
	public void requestForSignAsync(byte[] rawTxHash, ISignedTransactionCallback callback) {
		
		String signatureStr = requestForSign(rawTxHash);
		if (callback != null) {
			callback.handleSignedTransaction(signatureStr);
		}
	}

}
