package tecnico.pt;

import java.util.Base64;
import java.util.Map;

public class MembersList {
    private static final String KEY1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu85VDMnFq1tFaRn5t4zCLEbTaJZ5CuWdLuSYvyz2OzIEUu1E7EwiIEVFg2Ik+2XfxiLkpnuu5XMtya91XzIWjtHytTl3/nF+JL2xWT2btrev8zFOaGtdZO5aWFsH6PqWCeS68JAjXuuRXNPww9k/RkqFQUihnS4e+2PoNk3vUdS6vCwNw5ARqypIoTfmbX6nmXvvVHnOPUeuax13mNmRn9audYRutQ8X68G4yffwJuFClI9NPZGPV/gsEkXj77rf9PE42CYkLzRGhgmJN/zNr3zSosPeVrKE/HfJDEMa0jaefHbFmpbwx0/Jxiij1pXJ8dhh0B2IZsTny2vs7ZlDRwIDAQAB"; 
    private static final String KEY2 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnZhKmQSwon/6KqGidjkS3c/voztNXDk3XoVuCzlwIhGUwL20GU3D74qRjTwaVRILZQTLUrLV31ElWtrvjogUtQZni/zR9826YmGLwM/tlBicZuDMpl2O3G3SrPpgkkCyhA2O95f5aiTbjHwVoS7Zz05MpekXgNeeMK2A1IlwU4fW0kBlazN5Ko6Bw9ECHW+M8Rc7QR0MJDZRdrb/izDwLIEbniu1qttGsMl6fX1uZmYDz08K7AuH7nxnpR/+fIKM4Fnzu1igUE+bV4WvKfX0WzMLAMYUWynpb+1DErj2LoV6Dd/sGjwsOAf+OUVre0RdzwNr/Ir4kQ2JfEl9vyR9+wIDAQAB";
    private static final String KEY3 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAt/NTtCLHy0ztSvW0jYhX7t7Avw+6PLibmig6qRkUXXX0GrqSxYAu6sB4Gwo2muHGytQ4AQIEUbW358FozjKkl7tKewmv3imupuP17BPWz8kQhpuEwjxRL+VF335AMXEnBtrM2mPzpoDPPsxhfTv37R9qIGLxUe91+TmFXR2D7bhys1xQD5eXAUp1dQggXBuUioPvsLFztYqRdoG4mv4FmD5Ck6R9ftgzFwg0Up8kfqzEOmXeLKxWxLCYyKvUw+u5d0ebDtiJvKxWSMlFO1R9e26vOQzzCF8AJAOqS/j7py+6Y4XLh/pgGH/02O469irZ83OGLNA41cQfCRmWQx8IpQIDAQAB";
    private static final String KEY4 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5g1DXFz7JRCYwpXkSGMzSlEOhanRJNOHYlbrQpZCv9oqqQcnrriw0GYeFvTxn+h4Vqm6lZ74VCa2OQ7jGpUqbJz+5buIZe66Rb1Vbn4ePJIG+Js2m/YxbzPM/IuoKTea7i82njRQwZXVOssuzqdvcScjG3r/+y8GxMas/XJm26Fr8wlGkmtSEPhVSLsIjKKjlEV4WO9I/mT6bkA4S7+3xvxkUFXdWmUKKjE4b71bU5qhZI9sLQjwKue6siX26E2EeA/68MKGZt5UwbuuOHXt0I5bcGGCZZIPjUIh6kG0ZvpRmiYqo+WcEoMiNFYtoCCPaETL7SEf9hGNB8XOdJsqVQIDAQAB";

    public final Map<Integer, MemberInfo> MEMBERS = Map.of(
        1, new MemberInfo("127.0.0.1", 12345, Base64.getDecoder().decode(KEY1)),
        2, new MemberInfo("127.0.0.1", 12346, Base64.getDecoder().decode(KEY2)),
        3, new MemberInfo("127.0.0.1", 12347, Base64.getDecoder().decode(KEY3)),
        4, new MemberInfo("127.0.0.1", 12348, Base64.getDecoder().decode(KEY4))
    );

    public MemberInfo getMemberInfo(Integer memberName) {
        return MEMBERS.get(memberName);
    }

    public Map<Integer, MemberInfo> getAllMembers() {
        return MEMBERS;
    }
}
