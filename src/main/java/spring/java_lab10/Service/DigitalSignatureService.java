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

    @Autowired
    private BouncyCastleProvider provider;

    public String sign(byte[] documentBytes) throws Exception {
        PrivateKey privateKey = keyManagementService.getPrivateKey();

        Signature signature = Signature.getInstance("SHA256withRSA", provider.getName());
        signature.initSign(privateKey);
        signature.update(documentBytes);
        byte[] signedBytes = signature.sign();

        return Base64.getEncoder().encodeToString(signedBytes);
    }

    public boolean verify(byte[] documentBytes, String signatureString, User user) throws Exception {
        PublicKey publicKey = keyManagementService.getPublicKey(user);

        Signature signature = Signature.getInstance("SHA256withRSA", provider.getName());
        signature.initVerify(publicKey);
        signature.update(documentBytes);

        byte[] signatureBytes = Base64.getDecoder().decode(signatureString);
        return signature.verify(signatureBytes);
    }
}