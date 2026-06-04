package your.newproject.rename.type;

public class EProviderFactory {
    public static EProvider of(String provider) {
        return switch (provider) {
            case "GOOGLE" -> EProvider.GOOGLE;
            case "APPLE" -> EProvider.APPLE;
            default -> throw new IllegalArgumentException("Invalid Provider Type.");
        };
    }
}
