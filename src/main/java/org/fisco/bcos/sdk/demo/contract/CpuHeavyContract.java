package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessor;

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
