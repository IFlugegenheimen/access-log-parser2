import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("Введите путь к файлу access.log и нажмите <Enter>: ");
            String path = scanner.nextLine().trim();
            
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("Файл не существует. Попробуйте снова.");
                continue;
            }
            if (file.isDirectory()) {
                System.out.println("Это папка, а не файл. Попробуйте снова.");
                continue;
            }
            
            try {
                List<String> lines = Files.readAllLines(Paths.get(path));
                Statistics stats = new Statistics();
                int processed = 0;
                int errors = 0;
                
                System.out.println("\n🔍 Информация по User-Agent из обработанных строк:");
                System.out.println("----------------------------------------------------");
                
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    try {
                        LogEntry entry = new LogEntry(line);
                        stats.addEntry(entry);
                        processed++;
                        
                        UserAgent ua = entry.getUserAgent();
                        System.out.printf("IP: %-15s | ОС: %-10s | Браузер: %-10s%n",
                                entry.getIp(), ua.getOs(), ua.getBrowser());
                        
                    } catch (Exception e) {
                        errors++;
                    }
                }
                
                System.out.println("\n🔗 Полные URL существующих страниц (код ответа 200):");
                Set<String> fullUrls = stats.getAllExistingFullUrls();
                if (fullUrls.isEmpty()) {
                    System.out.println("  Нет данных.");
                } else {
                    fullUrls.stream().sorted().forEach(url -> System.out.println("  - " + url));
                }
                
                System.out.println("\n📊 Доли операционных систем:");
                Map<String, Double> osStats = stats.getOperatingSystemShare();
                if (osStats.isEmpty()) {
                    System.out.println("  Нет данных об операционных системах.");
                } else {
                    osStats.entrySet().stream()
                            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                            .forEach(entry ->
                                    System.out.printf("  %s: %.1f%% (%.3f)%n",
                                            entry.getKey(),
                                            entry.getValue() * 100,
                                            entry.getValue())
                            );
                }
                
                System.out.println("\n✅ Обработка завершена.");
                System.out.println("Обработано строк: " + processed);
                if (errors > 0) {
                    System.out.println("Ошибок парсинга: " + errors);
                }
                System.out.printf("Общий трафик: %d байт%n", stats.getTotalTraffic());
                System.out.printf("Средний трафик в час: %.2f байт/час%n", stats.getTrafficRate());
                
            } catch (Exception e) {
                System.err.println("❌ Ошибка: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("\n---\n");
        }
    }
}