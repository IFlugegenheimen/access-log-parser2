import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LogEntry {
    private final String ip;
    private final LocalDateTime timestamp;
    private final HttpMethod method;
    private final String path;
    private final int responseCode;
    private final int responseSize;
    private final String referer;
    private final UserAgent userAgent;
    
    public LogEntry(String logLine) {
        String[] parts = logLine.split(" ", 2);
        if (parts.length < 2) throw new IllegalArgumentException("Invalid log format");
        this.ip = parts[0];
        
        int tsStart = logLine.indexOf('[');
        int tsEnd = logLine.indexOf(']');
        if (tsStart == -1 || tsEnd == -1) throw new IllegalArgumentException("Missing timestamp");
        String tsStr = logLine.substring(tsStart + 1, tsEnd);
        this.timestamp = parseTimestamp(tsStr);
        
        int reqStart = logLine.indexOf('"');
        int reqEnd = logLine.indexOf('"', reqStart + 1);
        if (reqStart == -1 || reqEnd == -1) throw new IllegalArgumentException("Missing request");
        String request = logLine.substring(reqStart + 1, reqEnd);
        String[] reqParts = request.split(" ", 3);
        if (reqParts.length < 3) throw new IllegalArgumentException("Invalid request format");
        this.method = HttpMethod.fromString(reqParts[0]);
        this.path = reqParts[1];
        
        String afterRequest = logLine.substring(reqEnd + 1).trim();
        String[] statusSizeRef = afterRequest.split(" ", 3);
        if (statusSizeRef.length < 2) throw new IllegalArgumentException("Missing status or size");
        
        this.responseCode = Integer.parseInt(statusSizeRef[0]);
        String sizeStr = statusSizeRef[1];
        this.responseSize = "-".equals(sizeStr) ? 0 : Integer.parseInt(sizeStr);
        
        int quote1 = logLine.indexOf('"');
        int quote2 = logLine.indexOf('"', quote1 + 1);
        int quote3 = logLine.indexOf('"', quote2 + 1);
        int quote4 = logLine.indexOf('"', quote3 + 1);
        int quote5 = logLine.indexOf('"', quote4 + 1);
        int quote6 = logLine.indexOf('"', quote5 + 1);
        
        if (quote5 == -1) {
            this.referer = "-";
            this.userAgent = new UserAgent(logLine.substring(quote3 + 1, quote4));
        } else if (quote6 == -1) {
            this.referer = logLine.substring(quote3 + 1, quote4).trim();
            this.userAgent = new UserAgent(logLine.substring(quote4 + 1).trim());
        } else {
            this.referer = logLine.substring(quote3 + 1, quote4).trim();
            this.userAgent = new UserAgent(logLine.substring(quote5 + 1, quote6).trim());
        }
    }
    
    private LocalDateTime parseTimestamp(String ts) {
        int spaceIndex = ts.indexOf(' ');
        if (spaceIndex == -1) {
            throw new IllegalArgumentException("Invalid timestamp format: " + ts);
        }
        String dateTimePart = ts.substring(0, spaceIndex);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss")
                .withLocale(Locale.ENGLISH);
        return LocalDateTime.parse(dateTimePart, formatter);
    }
    
    public String getIp() { return ip; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public HttpMethod getMethod() { return method; }
    public String getPath() { return path; }
    public int getResponseCode() { return responseCode; }
    public long getDataSize() { return responseSize; }
    public String getReferer() { return referer; }
    public UserAgent getUserAgent() { return userAgent; }
}