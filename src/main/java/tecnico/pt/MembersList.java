package tecnico.pt;

import java.util.Map;

public class MembersList {
    public static final Map<String, NetworkAddress> MEMBERS = Map.of(
        "member1", new NetworkAddress("localhost", 12345),
        "member2", new NetworkAddress("127.0.0.2", 12346),
        "member3", new NetworkAddress("localhost", 12347)
    );
}
