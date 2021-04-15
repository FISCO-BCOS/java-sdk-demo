package org.fisco.bcos.sdk.demo.perf.hsm;

import com.webank.wedpr.crypto.hsm.sdf.AlgorithmType;
import com.webank.wedpr.crypto.hsm.sdf.SDF;
import com.webank.wedpr.crypto.hsm.sdf.SDFCryptoResult;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
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

        SDFCryptoResult hashResult = SDF.Hash(null, AlgorithmType.SM3, Hex.toHexString(bHashData));
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
        CryptoKeyPair sdfkp = new SDFSM2KeyPair().generateKeyPair();

        System.out.println("Private key: " + sdfkp.getHexPrivateKey());
        System.out.println("Public key: " + sdfkp.getHexPublicKey());

        System.out.println("*********SDF Sign*********");
        SignatureResult signSDFResult = sdfSigner.sign(hashResult.getHash(), sdfkp);
        System.out.println(signSDFResult.convertToString());

        System.out.println("*********SW Verify SDF Sign*********");
        boolean result =
                SM2Signature.verifyMessage(
                        sdfkp.getHexPublicKey(),
                        hashResult.getHash(),
                        signSDFResult.convertToString());
        if (result) {
            System.out.println("Sw can verify hsm");
        } else {
            System.out.println("Sw can not verify hsm");
        }

        System.out.println("*********SDF Verify SDF Sign*********");
        boolean sdfSDFResult =
                sdfSigner.verify(
                        sdfkp.getHexPublicKey(),
                        hashResult.getHash(),
                        signSDFResult.convertToString());
        if (sdfSDFResult) {
            System.out.println("Hsm can verify hsm");
        } else {
            System.out.println("Hsm can not verify hsm");
        }

        System.out.println("*********SW sign and SDF Verify SW Sign*********");
        CryptoKeyPair kp = new SM2KeyPair().generateKeyPair();
        SignatureResult swSignature = new SM2Signature().sign(hashResult.getHash(), kp);
        System.out.println("sw signature: " + swSignature.convertToString());
        System.out.println("sw public key:" + kp.getHexPublicKey());
        boolean sdfSwResult =
                sdfSigner.verify(
                        kp.getHexPublicKey(), hashResult.getHash(), swSignature.convertToString());
        if (sdfSwResult) {
            System.out.println("Hsm can verify sw");
        } else {
            System.out.println("Hsm can not verify sw");
        }
    }
}
