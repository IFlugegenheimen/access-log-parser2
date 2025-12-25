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
    private int realUserRequests = 0;
    private int errorRequests = 0;
    private final Set<String> realUserIps = new HashSet<>();
    
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
        int code = entry.getResponseCode();
        
        if (code == 200) {
            existingPages.add(path);
        } else if (code == 404) {
            notFoundPages.add(path);
        }
        
        if (code >= 400 && code < 600) {
            errorRequests++;
        }
        
        boolean isBot = entry.getUserAgent().isBot();
        
        if (!isBot) {
            realUserRequests++;
            realUserIps.add(entry.getIp());
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
    
    private long getPeriodInHours() {
        if (minTime == null || maxTime == null) return 1;
        long hours = java.time.temporal.ChronoUnit.HOURS.between(minTime, maxTime);
        return Math.max(1, hours); // не менее 1 часа
    }
    
    public double getAverageVisitsPerHour() {
        long hours = getPeriodInHours();
        return (double) realUserRequests / hours;
    }
    
    public double getAverageErrorsPerHour() {
        long hours = getPeriodInHours();
        return (double) errorRequests / hours;
    }
    
    public double getAverageVisitsPerUser() {
        if (realUserIps.isEmpty()) return 0.0;
        return (double) realUserRequests / realUserIps.size();
    }
    
    public long getTotalTraffic() {
        return totalTraffic;
    }
    
    public double getTrafficRate() {
        long hours = getPeriodInHours();
        return (double) totalTraffic / hours;
    }
    
    public Set<String> getAllExistingPages() {
        return new HashSet<>(existingPages);
    }
    
    public Set<String> getAllNotFoundPages() {
        return new HashSet<>(notFoundPages);
    }
    
    public Set<String> getAllExistingFullUrls() {
        return buildFullUrls(existingPages);
    }
    
    public Set<String> getAllNotFoundFullUrls() {
        return buildFullUrls(notFoundPages);
    }
    
    private Set<String> buildFullUrls(Set<String> paths) {
        String baseUrl = "https://example.com";
        Set<String> urls = new HashSet<>();
        for (String path : paths) {
            if (path == null || path.isEmpty()) continue;
            if (!path.startsWith("/")) path = "/" + path;
            urls.add(baseUrl + path);
        }
        return urls;
    }
    
    public Map<String, Double> getOperatingSystemShare() {
        return calculateShare(osCount);
    }
    
    public Map<String, Double> getBrowserShare() {
        return calculateShare(browserCount);
    }
    
    private Map<String, Double> calculateShare(Map<String, Integer> countMap) {
        if (countMap.isEmpty()) return new HashMap<>();
        int total = countMap.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, Double> share = new HashMap<>();
        for (Map.Entry<String, Integer> e : countMap.entrySet()) {
            share.put(e.getKey(), (double) e.getValue() / total);
        }
        return share;
    }
}