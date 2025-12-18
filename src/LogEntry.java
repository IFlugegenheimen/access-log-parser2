import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEntry {
    private final String ip;
    private final LocalDateTime timestamp;
    private final HttpMethod method;
    private final String path;
    private final int responseCode;
    private final int responseSize; // не long!
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
        
        int lastQuote = logLine.lastIndexOf('"');
        int prevQuote = logLine.lastIndexOf('"', lastQuote - 1);
        int prev2Quote = logLine.lastIndexOf('"', prevQuote - 1);
        
        if (prev2Quote == -1) {
            this.referer = "-";
            this.userAgent = new UserAgent(logLine.substring(prevQuote + 1, lastQuote));
        } else {
            this.referer = logLine.substring(prev2Quote + 1, prevQuote);
            this.userAgent = new UserAgent(logLine.substring(prevQuote + 1, lastQuote));
        }
    }
    
    private LocalDateTime parseTimestamp(String ts) {
        int spaceIndex = ts.indexOf(' ');
        if (spaceIndex == -1) {
            throw new IllegalArgumentException("Неверный формат времени: " + ts);
        }
        String dateTimePart = ts.substring(0, spaceIndex);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss")
                .withLocale(java.util.Locale.ENGLISH);
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