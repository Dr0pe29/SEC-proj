package tecnico.pt;

import java.util.Map;

public class MembersList {
    public final Map<Integer, NetworkAddress> MEMBERS = Map.of(
        1, new NetworkAddress("127.0.0.1", 12345),
        2, new NetworkAddress("127.0.0.1", 12346),
        3, new NetworkAddress("127.0.0.1", 12347),
        4, new NetworkAddress("127.0.0.1", 12348)
    );

    public NetworkAddress getMemberAddress(Integer memberName) {
        return MEMBERS.get(memberName);
    }
}
