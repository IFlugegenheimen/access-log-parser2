public class UserAgent {
    private final String os;
    private final String browser;
    
    public UserAgent(String userAgentString) {
        this.os = detectOS(userAgentString);
        this.browser = detectBrowser(userAgentString);
    }
    
    private String detectOS(String ua) {
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac OS") || ua.contains("Macintosh")) return "macOS";
        if (ua.contains("Linux")) return "Linux";
        return "other";
    }
    
    private String detectBrowser(String ua) {
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("OPR") || ua.contains("Opera")) return "Opera";
        if (ua.contains("Edg") || ua.contains("Edge")) return "Edge";
        if (ua.contains("Chrome")) return "Chrome";
        // Safari, etc.
        return "other";
    }
    
    public String getOs() { return os; }
    public String getBrowser() { return browser; }
}