package org.fisco.bcos.sdk.demo.transaction;

import java.math.BigInteger;
import java.util.List;


import org.fisco.bcos.sdk.abi.ABICodec;
import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.signature.SignatureResult;

import org.fisco.bcos.sdk.transaction.builder.TransactionBuilderInterface;
import org.fisco.bcos.sdk.transaction.builder.TransactionBuilderService;
import org.fisco.bcos.sdk.transaction.codec.encode.TransactionEncoderInterface;
import org.fisco.bcos.sdk.transaction.codec.encode.TransactionEncoderService;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.model.gas.DefaultGasProvider;
import org.fisco.bcos.sdk.transaction.model.po.RawTransaction;

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



	BigInteger gasPrice = DefaultGasProvider.GAS_PRICE;
	BigInteger gasLimit = DefaultGasProvider.GAS_LIMIT;
	BigInteger value = BigInteger.ZERO;
	String extraData = "";
		
	CryptoSuite cryptoSuite;
	ABICodec abiCodec;
	TransactionEncoderService transactionEncoder;
	

	public BigInteger getGasPrice() {
		return gasPrice;
	}

	public void setGasPrice(BigInteger gasPrice) {
		this.gasPrice = gasPrice;
	}

	public BigInteger getGasLimit() {
		return gasLimit;
	}

	public BasicAbiTransaction setGasLimit(BigInteger gasLimit) {
		this.gasLimit = gasLimit;
		return this;
	}

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




	public BasicAbiTransaction()
	{
		
	}
	
	public BasicAbiTransaction(String ContractName_,String abiContent_, String methodName_, String to_, List<Object> params_) {

		contractName = ContractName_; 
		abiContent = abiContent_;
		methodName = methodName_;
		to = to_;
		params = params_;
	}
	
	public BasicAbiTransaction setTools(CryptoSuite cryptoSuite_,ABICodec abiCodec_,TransactionEncoderService transactionEncoder_) {
		this.cryptoSuite = cryptoSuite_;
		this.abiCodec = abiCodec_;
		if (this.abiCodec == null)
		{
			this.abiCodec = new ABICodec(cryptoSuite);
		}
		this.transactionEncoder = transactionEncoder_;
		if (this.transactionEncoder == null)
		{
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

	/**
	 * 对合约部署类型的交易进行编码
	 *
	 * @param abiCodec   用于编码交易内容的ABICodeC对象
	 * @param abiContent 合约的abi字符串(需要读取第1节生成的abi文件获取)
	 * @param binContent binary字符串(需要读取第1节生成的bin文件获取)
	 * @param params     部署合约的初始化参数列表
	 * @return 编码后的交易内容
	 * @throws ABICodecException
	 */
	public String encodeConstructor()
			throws ABICodecException {
		return abiCodec.encodeConstructor(abiContent, binContent, params);
	}

	/**
	 * 对合约调用类型的交易进行编码
	 *
	 * @param abiCodec   用于编码交易内容的ABICodeC对象
	 * @param abiContent 合约的abi字符串(需要读取第1节生成的abi文件获取)
	 * @param methodName 需要调用的合约方法名
	 * @return 编码后的交易内容
	 * @throws ABICodecException
	 */
	public String encodeMethodInput(ABICodec abiCodec) throws ABICodecException {
		return abiCodec.encodeMethod(this.getAbiContent(), this.getMethodName(), this.getParams());
	}



	/**
	 * 对RawTransaction进行RLP编码，返回编码内容的哈希
	 * @param rawTransaction     需要编码的交易
	 * @return 编码后交易的哈希
	 */
	public byte[] calcRawTransactionHash( RawTransaction rawTransaction) {
		byte[] encodedTransaction = transactionEncoder.encode(rawTransaction, null);
		return cryptoSuite.hash(encodedTransaction);
	}

	/**
	 * 根据RawTransaction和签名结果产生带有签名的交易
	 * @param transaction        不带有签名的交易
	 * @param signatureResult    签名服务器返回的反序列化的签名结果
	 * @return 带有签名的交易编码
	 */
	public byte[] encodeRawTransactionWithSignature(
			RawTransaction transaction, SignatureResult signatureResult

	) {
		return transactionEncoder.encode(transaction, signatureResult);
	}

	public RawTransaction makeRawTransaction(Client client,int chainId,int groupId) throws ABICodecException
	{
		if(this.isDeployTransaction)
			return makeDeployRawTransaction(client, chainId, groupId);
		return makeMethodRawTransaction(client, chainId, groupId);
	}
	
	
	public RawTransaction makeRawTransactionByInput(Client client,int chainId,int groupId,String input)
	{
		// 创建TransactionBuilder，构造RawTransaction
		TransactionBuilderInterface transactionBuilder = new TransactionBuilderService(client);
		RawTransaction rawtx =  transactionBuilder.createTransaction(gasPrice, gasLimit, to,
				input, value, BigInteger.valueOf(chainId), BigInteger.valueOf(groupId), extraData);
		return rawtx;

	}
	public RawTransaction makeDeployRawTransaction(Client client,int chainId,int groupId) throws ABICodecException
	{
		String deployInput = encodeConstructor();
		System.out.println("deploy contract bin:"+deployInput);
		System.out.println("bin size "+deployInput.length());
		// 创建TransactionBuilder，构造RawTransaction
		return makeRawTransactionByInput(client, chainId, groupId, deployInput);

	}

	// 构造未签名交易
	public RawTransaction makeMethodRawTransaction(Client client,int chainId,int groupId) throws ABICodecException {

		// 编码交易内容
		String txInput = encodeMethodInput(abiCodec);

		// 创建TransactionBuilder，构造RawTransaction
		return makeRawTransactionByInput(client, chainId, groupId, txInput);

	}
}
