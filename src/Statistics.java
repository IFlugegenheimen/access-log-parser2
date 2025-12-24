import java.time.LocalDateTime;
import java.util.*;

public class Statistics {
    private long totalTraffic = 0;
    private LocalDateTime minTime = null;
    private LocalDateTime maxTime = null;
    
    private final Set<String> existingPages = new HashSet<>();
    private final Set<String> notFoundPages = new HashSet<>();
    private final Map<String, Integer> osCount = new HashMap<>();
    private final Map<String, Integer> browserCount = new HashMap<>();
    
    
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
        
        String path = entry.getPath();
        
        if (entry.getResponseCode() == 200) {
            existingPages.add(entry.getPath());
        }
        
        else if (entry.getResponseCode() == 404) {
            notFoundPages.add(path);
        }
        
        String os = entry.getUserAgent().getOs();
        if (os != null && !os.trim().isEmpty()) {
            osCount.put(os, osCount.getOrDefault(os, 0) + 1);
        }
        
        String browser = entry.getUserAgent().getBrowser();
        if (browser != null && !browser.trim().isEmpty()) {
            browserCount.put(browser, browserCount.getOrDefault(browser, 0) + 1);
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
    
    public Set<String> getAllNotFoundFullUrls() {
        String baseUrl = "https://example.com";
        Set<String> fullUrls = new HashSet<>();
        for (String path : notFoundPages) {
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
    
    public Set<String> getAllNotFoundPages() {
        return new HashSet<>(notFoundPages);
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
    
    public Map<String, Double> getBrowserShare() {
        return calculateShare(browserCount);
    }
    
    private Map<String, Double> calculateShare(Map<String, Integer> countMap) {
        if (countMap.isEmpty()) {
            return new HashMap<>();
        }
        int total = countMap.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, Double> shareMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            shareMap.put(entry.getKey(), (double) entry.getValue() / total);
        }
        return shareMap;
    }
    
}