package tecnico.pt.consensus.hotstuff.net;

import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import tecnico.pt.consensus.hotstuff.msg.HotStuffMessage;
import tecnico.pt.consensus.hotstuff.msg.MsgType;
import tecnico.pt.consensus.hotstuff.msg.ProposeMsg;
import tecnico.pt.consensus.hotstuff.msg.QcForwardMsg;
import tecnico.pt.consensus.hotstuff.msg.VoteMsg;

public final class JsonHotStuffCodec implements HotStuffCodec {

    private final Gson gson = new Gson();

    @Override
    public byte[] encode(int srcId, HotStuffMessage msg) {
        JsonObject root = new JsonObject();
        root.addProperty("srcId", srcId);
        root.addProperty("type", msg.type().name());
        root.add("payload", gson.toJsonTree(msg));
        return gson.toJson(root).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Decoded decode(byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8);
        JsonObject root = gson.fromJson(s, JsonObject.class);

        int srcId = root.get("srcId").getAsInt();
        MsgType type = MsgType.valueOf(root.get("type").getAsString());

        HotStuffMessage msg = switch (type) {
            case PROPOSE -> gson.fromJson(root.get("payload"), ProposeMsg.class);
            case VOTE -> gson.fromJson(root.get("payload"), VoteMsg.class);
            case QC_FORWARD -> gson.fromJson(root.get("payload"), QcForwardMsg.class);
        };

        return new Decoded(srcId, msg);
    }
}