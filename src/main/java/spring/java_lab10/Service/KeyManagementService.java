package spring.java_lab10.Service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import spring.java_lab10.Model.User;
import spring.java_lab10.Repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class KeyManagementService {

    @Autowired
    private UserRepository userRepository;

    /*public void generateAndStoreKeysForUser(User user) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", new BouncyCastleProvider());
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        user.setPrivateKey(privateKeyString);
        user.setPublicKey(publicKeyString);

        userRepository.save(user);
    }

    public PrivateKey getPrivateKey() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username);
        if (user == null || user.getPrivateKey() == null) {
            throw new Exception("Private key not found for the authenticated user.");
        }

        byte[] privateKeyBytes = Base64.getDecoder().decode(user.getPrivateKey());
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA", new BouncyCastleProvider());
        return keyFactory.generatePrivate(privateKeySpec);
    }

    /////////////////////////////////yaasdfas
    public PublicKey getPublicKey() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
    ////////треба брати автора а не цього користувача
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username);
        if (user == null || user.getPublicKey() == null) {
            throw new Exception("Public key not found for the authenticated user.");
        }

        byte[] publicKeyBytes = Base64.getDecoder().decode(user.getPublicKey());
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA", new BouncyCastleProvider());
        return keyFactory.generatePublic(publicKeySpec);
    }*/

    public byte[] generateAESKeyFromFile() throws IOException {
        byte[] keyBytes = Files.readAllBytes(Paths.get("key.txt"));
        // Обрізаємо ключ до потрібної довжини (256 біт)
        return truncateKeyToAESLength(keyBytes);
    }

    private byte[] truncateKeyToAESLength(byte[] keyBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(keyBytes);
            byte[] truncatedKey = new byte[32]; // AES-256 використовує 256-бітовий ключ
            System.arraycopy(hashedBytes, 0, truncatedKey, 0, 32);
            return truncatedKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}