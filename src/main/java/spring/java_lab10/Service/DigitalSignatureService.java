package spring.java_lab10.Service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spring.java_lab10.Model.User;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;

@Service
public class DigitalSignatureService {

    @Autowired
    private KeyManagementService keyManagementService;

    @Autowired
    private BouncyCastleProvider provider;

    public byte[] sign(byte[] documentBytes) throws Exception {
        PrivateKey privateKey = keyManagementService.getPrivateKey();

        Signature signature = Signature.getInstance("SHA256withRSA", provider.getName());
        signature.initSign(privateKey);
        signature.update(documentBytes);
        byte[] signedBytes = signature.sign();

        return signedBytes;
    }

    public boolean verify(byte[] documentBytes, byte[] signatureBytes, User user) throws Exception {
        PublicKey publicKey = keyManagementService.getPublicKey(user);

        Signature signature = Signature.getInstance("SHA256withRSA", provider.getName());
        signature.initVerify(publicKey);
        signature.update(documentBytes);

        return signature.verify(signatureBytes);
    }
}