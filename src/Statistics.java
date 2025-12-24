import java.time.LocalDateTime;

public class Statistics {
    private long totalTraffic = 0;
    private LocalDateTime minTime = null;
    private LocalDateTime maxTime = null;
    
    public Statistics() {
    }
    
    public void addEntry(LogEntry entry) {
        totalTraffic += entry.getDataSize();
        
        LocalDateTime ts = entry.getTimestamp();
        if (minTime == null || ts.isBefore(minTime)) {
            minTime = ts;
        }
        if (maxTime == null || ts.isAfter(maxTime)) {
            maxTime = ts;
        }
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
    
}