package tecnico.pt.consensus.hotstuff.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class Ids {
    private Ids() {}

    public static String blockId(String parentId, int view, String command) {
        String s = String.valueOf(parentId) + "|" + view + "|" + String.valueOf(command);
        return sha256Hex(s);
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}