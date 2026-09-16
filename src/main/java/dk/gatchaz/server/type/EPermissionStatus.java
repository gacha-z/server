package dk.gatchaz.server.type;

/**
 * 디바이스 권한(위치/카메라/알림) 상태. 클라이언트(OS)가 보내는 값은 GRANTED/DENIED 둘뿐이다.
 */
public enum EPermissionStatus {
    GRANTED,
    DENIED
}
