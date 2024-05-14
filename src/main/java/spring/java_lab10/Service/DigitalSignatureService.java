package spring.java_lab10.Service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spring.java_lab10.Model.User;

import java.security.*;
import java.util.Base64;

@Service
public class DigitalSignatureService {

    @Autowired
    private KeyManagementService keyManagementService;

    public byte[] sign(byte[] documentBytes) throws Exception {
        PrivateKey privateKey = keyManagementService.getPrivateKey();

        Signature signature = Signature.getInstance("SHA256withRSA", new BouncyCastleProvider());
        signature.initSign(privateKey);
        signature.update(documentBytes);
        byte[] signedBytes = signature.sign();

        return signedBytes;
    }

    public boolean verify(byte[] documentBytes, byte[] signatureBytes, User user) throws Exception {
        PublicKey publicKey = keyManagementService.getPublicKey(user);

        Signature signature = Signature.getInstance("SHA256withRSA", new BouncyCastleProvider());
        signature.initVerify(publicKey);
        signature.update(documentBytes);

        return signature.verify(signatureBytes);
    }
}