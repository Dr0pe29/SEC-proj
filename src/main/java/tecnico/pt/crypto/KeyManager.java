package tecnico.pt.crypto;

import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

//RUN THIS TO GENERATE KEYS FOR ALL MEMBERS, THEN COPY THE PUBLIC KEYS TO MembersList.java
//DONT NEED TO RUN EVERYTIME
public class KeyManager {
    public static void generateKeys(int memberId){
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        byte[] privKey = pair.getPrivate().getEncoded();
        try (FileOutputStream fos = new FileOutputStream("member" + memberId + ".priv")) {
            fos.write(privKey);
        }
        catch (Exception e) {e.printStackTrace();}

        byte[] pubKey = pair.getPublic().getEncoded();
        String base64PubKey = Base64.getEncoder().encodeToString(pubKey);

        System.out.println("Member " + memberId + " Private Key saved to member" + memberId + ".priv");
        System.out.println("Member " + memberId + " Public Key (Base64 for MembersList):");
        System.out.println(base64PubKey);
    }
}
