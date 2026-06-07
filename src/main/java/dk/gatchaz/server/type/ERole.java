package dk.gatchaz.server.type;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ERole {
    PM("PM", "ROLE_PM"),
    HD_USER("HD_USER", "ROLE_HD_USER"),
    AQD_USER("AQD_USER", "ROLE_AQD_USER"),
    BOTH_USER("BOTH_USER", "ROLE_BOTH_USER"),
    ADMIN("ADMIN", "ROLE_ADMIN"),
    GUEST("GUEST", "ROLE_GUEST"),
    WITHDRAWN("WITHDRAWN", "ROLE_WITHDRAWN");
    private final String name;
    private final String securityName;

    @Override
    public String toString() {
        return this.name;
    }

    public String toSecurityString() {
        return this.securityName;
    }

}
