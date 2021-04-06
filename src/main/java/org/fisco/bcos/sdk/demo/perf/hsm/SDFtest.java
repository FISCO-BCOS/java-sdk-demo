package org.fisco.bcos.sdk.demo.perf.hsm;

import com.webank.wedpr.crypto.hsm.sdf.AlgorithmType;
import com.webank.wedpr.crypto.hsm.sdf.SDFCrypto;
import com.webank.wedpr.crypto.hsm.sdf.SDFCryptoResult;
import org.fisco.bcos.sdk.crypto.keypair.SDFSM2KeyPair;
import org.fisco.bcos.sdk.crypto.keypair.SM2KeyPair;
import org.fisco.bcos.sdk.crypto.signature.SDFSM2Signature;
import org.fisco.bcos.sdk.crypto.signature.SM2Signature;
import org.fisco.bcos.sdk.crypto.signature.SignatureResult;
import org.fisco.bcos.sdk.utils.Hex;

public class SDFtest {
    public static void main(String[] args) {
        byte[] bHashData = {
            0x61, 0x62, 0x63, 0x64, 0x61, 0x62, 0x63, 0x64, 0x61, 0x62, 0x63, 0x64, 0x61, 0x62,
            0x63, 0x64, 0x61, 0x62, 0x63, 0x64, 0x61, 0x62, 0x63, 0x64, 0x61, 0x62, 0x63, 0x64,
            0x61, 0x62, 0x63, 0x64, 0x61, 0x62, 0x63, 0x64, 0x61, 0x62, 0x63, 0x64, 0x61, 0x62,
            0x63, 0x64, 0x61, 0x62, 0x63, 0x64, 0x61, 0x62, 0x63, 0x64, 0x61, 0x62, 0x63, 0x64,
            0x61, 0x62, 0x63, 0x64, 0x61, 0x62, 0x63, 0x64
        };

        byte[] bHashStdResult = {
            (byte) 0xde,
            (byte) 0xbe,
            (byte) 0x9f,
            (byte) 0xf9,
            0x22,
            0x75,
            (byte) 0xb8,
            (byte) 0xa1,
            0x38,
            0x60,
            0x48,
            (byte) 0x89,
            (byte) 0xc1,
            (byte) 0x8e,
            0x5a,
            0x4d,
            0x6f,
            (byte) 0xdb,
            0x70,
            (byte) 0xe5,
            0x38,
            0x7e,
            0x57,
            0x65,
            0x29,
            0x3d,
            (byte) 0xcb,
            (byte) 0xa3,
            (byte) 0x9c,
            0x0c,
            0x57,
            0x32
        };


        SDFCrypto crypto = new SDFCrypto();
        SDFCryptoResult hashResult =
                crypto.Hash(null, AlgorithmType.SM3, Hex.toHexString(bHashData), 64);
        String stdResult = Hex.toHexString(bHashStdResult);
        System.out.println("*********Hash*********");
        if (hashResult.getSdfErrorMessage() != null) {
            System.out.println(hashResult.getSdfErrorMessage());
        } else {
            if (stdResult.equals(hashResult.getHash())) {
                System.out.println("Match with standard result");
            } else {
                System.out.println("Not match with the standard result");
            }
        }

        System.out.println("*********KeyGen*********");
        SDFSM2Signature sdfSigner = new SDFSM2Signature();
        SDFSM2KeyPair sdfkp = new SDFSM2KeyPair();
        System.out.println(sdfkp.getHexPrivateKey());
        System.out.println(sdfkp.getHexPublicKey());


        System.out.println("*********Sign*********");
        SignatureResult signResult = sdfSigner.sign(hashResult.getHash(),sdfkp);
        System.out.println(signResult.convertToString());
        boolean result =
                SM2Signature.verifyMessage(
                        sdfkp.getHexPrivateKey(),
                        hashResult.getHash(),
                        signResult.convertToString());
        if (result) {
            System.out.println("Sw can verify hsm");
        } else {
            System.out.println("Sw can not verify hsm");
        }

        System.out.println("*********Verify*********");
        SM2KeyPair kp = new SM2KeyPair();
        SM2Signature signer = new SM2Signature();
        SignatureResult swSignResult = signer.sign(hashResult.getHash(), kp);

        boolean sdfSwResult = sdfSigner.verify(sdfkp.getHexPublicKey(),hashResult.getHash(),signResult.convertToString());
        if (sdfSwResult) {
            System.out.println("Hsm can verify sw");
        } else {
            System.out.println("Hsm can not verify sw");
        }


        SignatureResult sdfSignResult = sdfSigner.sign(hashResult.getHash(), kp);
        boolean sdfSdfResult = sdfSigner.verify(sdfkp.getHexPublicKey(),hashResult.getHash(),sdfSignResult.convertToString());
        if (sdfSwResult) {
            System.out.println("Hsm can verify hsm");
        } else {
            System.out.println("Hsm can not verify hsm");
        }
    }
}
