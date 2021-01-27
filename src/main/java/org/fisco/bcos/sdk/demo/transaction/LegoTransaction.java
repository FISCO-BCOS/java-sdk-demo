package org.fisco.bcos.sdk.demo.transaction;

import java.io.IOException;

import org.bouncycastle.util.encoders.Hex;
import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.crypto.signature.SM2SignatureResult;
import org.fisco.bcos.sdk.crypto.signature.SignatureResult;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderInterface;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.transaction.model.exception.TransactionException;
import org.fisco.bcos.sdk.transaction.model.po.RawTransaction;
import org.fisco.bcos.sdk.transaction.pusher.TransactionPusherInterface;
import org.fisco.bcos.sdk.transaction.pusher.TransactionPusherService;

import com.fasterxml.jackson.core.JsonProcessingException;

public class LegoTransaction {
	BcosClientWrapper bcosClientWrapper;
	TransactionPusherInterface txPusher;
	
	public void init() throws Exception {
		bcosClientWrapper = new BcosClientWrapper();
		bcosClientWrapper.init(1);
		this.txPusher = new TransactionPusherService(bcosClientWrapper.getClient());
	}

	public class SignedTxCallback implements ISignedTransactionCallback {

		Client client;
		RawTransaction rawTransaction;
		TransactionResponse response;
		BasicAbiTransaction abiTx;
		TransactionCallback txCallback;
		/**
		  * 签名结果回调的实现
		 *
		 * @param client   BcosSDK里的client
		 * @param RawTransaction 缓存交易数据，用于签名后发送交易
		 * @param TransactionCallback 当异步发送交易时，链上返回后给调用者的回调
		 * @return **/
		public SignedTxCallback(Client client_, BasicAbiTransaction abiTx_, RawTransaction rawTransaction_,TransactionCallback callback_) {
			client = client_;
			abiTx = abiTx_;
			rawTransaction = rawTransaction_;
			txCallback  = callback_;

		}

		/**
		  * 签名结果回调的实现
		 *
		 * @param signatureStr   签名服务回调返回的签名结果串
		 * @return **/
		@Override
		public int handleSignedTransaction(String signatureStr) {
			// 完成了交易签名后，将其发送出去
			// 对签名结果进行反序列化
			SignatureResult signature = decodeSignatureString(signatureStr);
			System.out.println("2:SignatureResult: " + signature.convertToString());
			// 产生带有签名的交易
			byte[] signedTransaction = abiTx.encodeRawTransactionWithSignature(rawTransaction, signature);
			System.out.println("3:signedTransaction: " + new String(Hex.encode(signedTransaction)));
			// 发送交易，传入调用者的TransactionCallback
			txPusher.pushAsync(Hex.toHexString(signedTransaction),txCallback);
			return 0;
		}

	}

	
	/*用ABI解析回执*/
	public TransactionResponse decodeReceipt(TransactionReceipt  receipt,BasicAbiTransaction abiTx ) throws JsonProcessingException, TransactionException, IOException, ABICodecException
	{

		TransactionDecoderInterface transactionDecoder = new TransactionDecoderService(bcosClientWrapper.getClient().getCryptoSuite());
		TransactionResponse response;
		if (abiTx.isDeployTransaction)
			return transactionDecoder.decodeReceiptWithoutValues(abiTx.abiContent,  receipt);
		return transactionDecoder.decodeReceiptWithValues(abiTx.abiContent, abiTx.methodName, receipt);
		
	}
	
	/*根据密码学算法配置解析签名串到对象*/
	public SignatureResult decodeSignatureString(String  signatureStr)
	{
		SignatureResult signature;
		if (bcosClientWrapper.getClient().getCryptoType() == CryptoType.ECDSA_TYPE) {
			signature = new ECDSASignatureResult(signatureStr);
		}else {
			signature = new SM2SignatureResult(bcosClientWrapper.getTxCryptoSuite().getCryptoKeyPair().getHexPublicKey(), signatureStr);
		}
		return signature;
	}
	
	/**
	  * 同步发送交易
	 * @param chainId 链id 
	 * @param BasicAbiTransaction   交易数据封装对象
	 * @param ISignTransaction signTxImpl 外部签名服务的实现
	 * @return **/
	public TransactionResponse sendTransactionAndGetResponse(int chainId,BasicAbiTransaction abiTx, ISignTransaction signTxImpl) throws ABICodecException, JsonProcessingException, TransactionException, IOException
	{
		RawTransaction rawTransaction = abiTx.makeRawTransaction(bcosClientWrapper.getClient(), chainId,
				bcosClientWrapper.getClient().getGroupId());
		// 请求签名服务，获取交易HASH的签名，这里用同步方式
		byte[] rawTxHash = abiTx.calcRawTransactionHash(rawTransaction);
		String signatureStr = signTxImpl.requestForSign(rawTxHash);
		SignatureResult signature = decodeSignatureString(signatureStr);
	    // 产生带有签名的交易
		byte[] signedTransaction = abiTx.encodeRawTransactionWithSignature(rawTransaction, signature);
		System.out.println("3:signedTransaction: " + new String(Hex.encode(signedTransaction)));
		// 发送交易
		TransactionReceipt  receipt  = txPusher.push(Hex.toHexString(signedTransaction));
		TransactionResponse response = decodeReceipt(receipt, abiTx);
		return response;
		
	}

	/**
	  * 异步发送Transaction，
	 * @param chainId 链id 
	 * @param BasicAbiTransaction   交易数据封装对象
	 * @param ISignTransaction signTxImpl 外部签名服务的实现
	 * @param TransactionCallback 最终异步上链完成后的交易结果
	 * @return **/
	public void sendTransactionAsync(int chainId,
			BasicAbiTransaction abiTx, ISignTransaction signTxImpl,TransactionCallback txCallback)
			throws ABICodecException, JsonProcessingException, TransactionException, IOException {
		// 创建RawTransaction
		
		RawTransaction rawTransaction = abiTx.makeMethodRawTransaction(bcosClientWrapper.getClient(), chainId,
				bcosClientWrapper.getClient().getGroupId());
		System.out.println("1:getRawTransaction: " + rawTransaction.getData());
		SignedTxCallback afterSignedTxCallback = new SignedTxCallback(bcosClientWrapper.getClient(), abiTx,
				rawTransaction,txCallback);
		// 请求签名服务，获取交易HASH的签名
		byte[] rawTxHash = abiTx.calcRawTransactionHash(rawTransaction);
		signTxImpl.requestForSignAsync(rawTxHash, afterSignedTxCallback);
	}
	


}
