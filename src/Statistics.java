import java.time.LocalDateTime;
import java.util.*;

public class Statistics {
    private long totalTraffic = 0;
    private LocalDateTime minTime = null;
    private LocalDateTime maxTime = null;
    
    private final Set<String> existingPages = new HashSet<>();
    private final Map<String, Integer> osCount = new HashMap<>();
    
    public Statistics() {}
    
    public void addEntry(LogEntry entry) {
        if (entry == null) return;
        
        totalTraffic += entry.getDataSize();
        
        LocalDateTime ts = entry.getTimestamp();
        if (minTime == null || ts.isBefore(minTime)) {
            minTime = ts;
        }
        if (maxTime == null || ts.isAfter(maxTime)) {
            maxTime = ts;
        }
        
        if (entry.getResponseCode() == 200) {
            existingPages.add(entry.getPath());
        }
        
        String os = entry.getUserAgent().getOs();
        if (os != null && !os.trim().isEmpty()) {
            osCount.put(os, osCount.getOrDefault(os, 0) + 1);
        }
    }
    
    public Set<String> getAllExistingFullUrls() {
        String baseUrl = "https://example.com";
        Set<String> fullUrls = new HashSet<>();
        for (String path : existingPages) {
            if (path == null || path.isEmpty()) continue;
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            fullUrls.add(baseUrl + path);
        }
        return fullUrls;
    }
    
    public long getTotalTraffic() {
        return totalTraffic;
    }
    
    public double getTrafficRate() {
        if (minTime == null || maxTime == null) {
            return 0.0;
        }
        long hours = java.time.temporal.ChronoUnit.HOURS.between(minTime, maxTime);
        if (hours == 0) hours = 1;
        return (double) totalTraffic / hours;
    }
    
    public Set<String> getAllExistingPages() {
        return new HashSet<>(existingPages);
    }
    
    public Map<String, Double> getOperatingSystemShare() {
        if (osCount.isEmpty()) {
            return new HashMap<>();
        }
        
        int totalCount = osCount.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, Double> shareMap = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : osCount.entrySet()) {
            double share = (double) entry.getValue() / totalCount;
            shareMap.put(entry.getKey(), share);
        }
        
        return shareMap;
    }
}