package tecnico.pt;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;

public class Serializer {
    private static final Gson gson = new Gson();

    /**
     * Converts the PacketPayload to a JSON byte array.
     */
    public static byte[] serialise(PacketPayload payload) {
        String json = gson.toJson(payload);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Converts JSON bytes back into a PacketPayload object.
     */
    public static PacketPayload deserialise(byte[] data) {
        String json = new String(data, StandardCharsets.UTF_8);
        return gson.fromJson(json, PacketPayload.class);
    }
}
