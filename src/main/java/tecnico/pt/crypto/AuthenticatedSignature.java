package tecnico.pt.crypto;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthenticatedSignature {
    private PrivateKey privateKey;
    private Map<Integer, PublicKey> publicKeys = new ConcurrentHashMap<>();


    // Initialize with your private key and others' public keys
    public byte[] sign(byte[] data) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(data);
        return sig.sign();
    }

    public boolean verify(int senderId, byte[] data, byte[] signature) throws Exception {
        PublicKey pubKey = publicKeys.get(senderId);
        if (pubKey == null) return false;
        
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(pubKey);
        sig.update(data);
        return sig.verify(signature);
    }

    public void loadPrivateKey(int memberId){
        byte[] keyBytes = null;
        try {
            keyBytes = Files.readAllBytes(Paths.get("member" + memberId + ".priv"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = null;
        try {
            kf = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            this.privateKey = kf.generatePrivate(spec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public void addPublicKey(int memberId, byte[] encodedKey){
        X509EncodedKeySpec spec = new X509EncodedKeySpec(encodedKey);
        KeyFactory kf = null;
        try {
            kf = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (kf != null) {
            try {
                this.publicKeys.put(memberId, kf.generatePublic(spec));
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
    }
}
