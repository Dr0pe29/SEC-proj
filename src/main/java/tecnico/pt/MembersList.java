package tecnico.pt;

import java.util.Map;

public class MembersList {
    public static final Map<String, NetworkAddress> MEMBERS = Map.of(
        "member1", new NetworkAddress("127.0.0.1", 12345),
        "member2", new NetworkAddress("127.0.0.1", 12346),
        "member3", new NetworkAddress("127.0.0.1", 12347)
    );
}
