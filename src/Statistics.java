import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class Statistics {
    private long totalTraffic = 0;
    private LocalDateTime minTime = null;
    private LocalDateTime maxTime = null;
    private final Map<String, Set<String>> existingPageToDomains = new HashMap<>();
    private final Map<String, Set<String>> notFoundPageToDomains = new HashMap<>();
    private final Map<String, Integer> osCount = new HashMap<>();
    private final Map<String, Integer> browserCount = new HashMap<>();
    private final Map<Long, Integer> visitsPerSecond = new HashMap<>();
    private final Set<String> referringDomains = new HashSet<>();
    private final Map<String, Integer> visitsPerIp = new HashMap<>();
    private int realUserRequests = 0;
    private int errorRequests = 0;
    private final Set<String> realUserIps = new HashSet<>();
    private static final String DEFAULT_DOMAIN = "example.com";
    
    public Statistics() {}
    
    public void addEntry(LogEntry entry) {
        if (entry == null) return;
        
        String referer = entry.getReferer();
        String refererDomain = extractDomain(referer);
        
        if (refererDomain != null && !refererDomain.isEmpty()) {
            referringDomains.add(refererDomain);
        }
        
        totalTraffic += entry.getDataSize();
        
        LocalDateTime ts = entry.getTimestamp();
        if (minTime == null || ts.isBefore(minTime)) minTime = ts;
        if (maxTime == null || ts.isAfter(maxTime)) maxTime = ts;
        
        int code = entry.getResponseCode();
        String path = entry.getPath();
        
        UserAgent ua = entry.getUserAgent();
        boolean isBot = (ua != null) && ua.isBot();
        
        if (code >= 400 && code < 600) {
            errorRequests++;
        }
        
        if (!isBot) {
            realUserRequests++;
            realUserIps.add(entry.getIp());
            
            long epochSecond = ts.toEpochSecond(ZoneOffset.UTC);
            visitsPerSecond.merge(epochSecond, 1, Integer::sum);
            
            String ip = entry.getIp();
            visitsPerIp.merge(ip, 1, Integer::sum);
            
            String domainToUse = (refererDomain != null && !refererDomain.isEmpty())
                    ? refererDomain
                    : DEFAULT_DOMAIN;
            
            if (code == 200) {
                existingPageToDomains
                        .computeIfAbsent(path, k -> new HashSet<>())
                        .add(domainToUse);
            } else if (code == 404) {
                notFoundPageToDomains
                        .computeIfAbsent(path, k -> new HashSet<>())
                        .add(domainToUse);
            }
            
            if (ua != null) {
                String os = ua.getOs();
                if (os != null && !os.trim().isEmpty()) {
                    osCount.put(os, osCount.getOrDefault(os, 0) + 1);
                }
                
                String browser = ua.getBrowser();
                if (browser != null && !browser.trim().isEmpty()) {
                    browserCount.put(browser, browserCount.getOrDefault(browser, 0) + 1);
                }
            }
        }
    }
    
    private String extractDomain(String url) {
        if (url == null || "-".equals(url.trim())) {
            return null;
        }
        
        String clean = url.trim();
        if (clean.startsWith("\"")) clean = clean.substring(1);
        if (clean.endsWith("\"")) clean = clean.substring(0, clean.length() - 1);
        clean = clean.trim();
        
        if (clean.isEmpty() || "-".equals(clean)) {
            return null;
        }
        
        if (clean.startsWith("https://")) {
            clean = clean.substring(8);
        } else if (clean.startsWith("http://")) {
            clean = clean.substring(7);
        }
        
        int slashIndex = clean.indexOf('/');
        if (slashIndex != -1) {
            clean = clean.substring(0, slashIndex);
        }
        
        int colonIndex = clean.indexOf(':');
        if (colonIndex != -1) {
            clean = clean.substring(0, colonIndex);
        }
        
        if (clean.startsWith("www.")) {
            clean = clean.substring(4);
        }
        
        if (clean.isEmpty() || !clean.contains(".")) {
            return null;
        }
        
        return clean.toLowerCase();
    }
    
    public int getMaxVisitsPerSecond() {
        return visitsPerSecond.isEmpty() ? 0 : Collections.max(visitsPerSecond.values());
    }
    
    public Set<String> getReferringDomains() {
        return new HashSet<>(referringDomains);
    }
    
    public int getMaxVisitsBySingleUser() {
        return visitsPerIp.isEmpty() ? 0 : Collections.max(visitsPerIp.values());
    }
    
    public long getTotalTraffic() { return totalTraffic; }
    
    public double getTrafficRate() {
        if (minTime == null || maxTime == null) return 0.0;
        long hours = java.time.temporal.ChronoUnit.HOURS.between(minTime, maxTime);
        return hours == 0 ? totalTraffic : (double) totalTraffic / hours;
    }
    
    public double getAverageVisitsPerHour() {
        long hours = (minTime == null || maxTime == null) ? 1 :
                Math.max(1, java.time.temporal.ChronoUnit.HOURS.between(minTime, maxTime));
        return (double) realUserRequests / hours;
    }
    
    public double getAverageErrorsPerHour() {
        long hours = (minTime == null || maxTime == null) ? 1 :
                Math.max(1, java.time.temporal.ChronoUnit.HOURS.between(minTime, maxTime));
        return (double) errorRequests / hours;
    }
    
    public double getAverageVisitsPerUser() {
        return realUserIps.isEmpty() ? 0.0 : (double) realUserRequests / realUserIps.size();
    }
    
    public Set<String> getAllExistingFullUrls() {
        return buildFullUrlsFromMap(existingPageToDomains);
    }
    
    public Set<String> getAllNotFoundFullUrls() {
        return buildFullUrlsFromMap(notFoundPageToDomains);
    }
    
    private Set<String> buildFullUrlsFromMap(Map<String, Set<String>> pageToDomains) {
        Set<String> urls = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : pageToDomains.entrySet()) {
            String path = entry.getKey();
            Set<String> domains = entry.getValue();
            String cleanPath = path.startsWith("/") ? path : "/" + path;
            for (String domain : domains) {
                urls.add("https://" + domain + cleanPath);
            }
        }
        return urls;
    }
    
    public Set<String> getAllExistingPages() {
        return new HashSet<>(existingPageToDomains.keySet());
    }
    
    public Set<String> getAllNotFoundPages() {
        return new HashSet<>(notFoundPageToDomains.keySet());
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