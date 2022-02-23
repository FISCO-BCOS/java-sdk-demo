package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessor;

public abstract class CpuHeavyContract extends Contract {
    public CpuHeavyContract(
            String contractBinary,
            String contractAddress,
            Client client,
            CryptoKeyPair credential,
            TransactionProcessor transactionProcessor) {
        super(contractBinary, contractAddress, client, credential, transactionProcessor);
    }

    public CpuHeavyContract(
            String contractBinary,
            String contractAddress,
            Client client,
            CryptoKeyPair credential) {
        super(contractBinary, contractAddress, client, credential);
    }

    public abstract void sort(BigInteger size, BigInteger signature, TransactionCallback callback);
}
