package spring.java_lab10.Service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";

    @Autowired
    private KeyManagementService keyManagementService;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public byte[] encryptDocument(byte[] documentBytes) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        byte[] secretKeyBytes = keyManagementService.generateAESKeyFromFile();
        SecretKeySpec keySpec = new SecretKeySpec(secretKeyBytes, ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(documentBytes);
    }

    public byte[] decryptDocument(byte[] encryptedDocumentBytes) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        byte[] secretKeyBytes = keyManagementService.generateAESKeyFromFile();
        SecretKeySpec keySpec = new SecretKeySpec(secretKeyBytes, ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(encryptedDocumentBytes);
    }
}

