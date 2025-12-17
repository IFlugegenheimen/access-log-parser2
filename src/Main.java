import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int validFileCount = 0;
        
        while (true) {
            System.out.println("Введите путь к файлу и нажмите <Enter>: ");
            String path = scanner.nextLine();
            File file = new File(path);
            
            if (!file.exists()) {
                System.out.println("Файл не существует. Попробуйте снова.");
            } else if (file.isDirectory()) {
                System.out.println("Указан путь к папке. Попробуйте снова.");
            } else {
                validFileCount++;
                System.out.println("Путь указан верно. Это файл номер " + validFileCount);
                
                try {
                    FileReader fileReader = new FileReader(path);
                    BufferedReader reader = new BufferedReader(fileReader);
                    
                    int totalLines = 0;
                    int googlebotCount = 0;
                    int yandexbotCount = 0;
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        int length = line.length();
                        
                        if (length > 1024) {
                            throw new LineTooLongException(
                                    "Обнаружена строка длиной " + length +
                                            " символов, что превышает допустимый лимит в 1024 символа."
                            );
                        }
                        
                        totalLines++;
                        
                        int lastQuote = line.lastIndexOf('"');
                        int prevQuote = line.lastIndexOf('"', lastQuote - 1);
                        if (prevQuote == -1) {
                            continue;
                        }
                        String userAgent = line.substring(prevQuote + 1, lastQuote);
                        
                        int openBracket = userAgent.indexOf('(');
                        int closeBracket = userAgent.indexOf(')', openBracket);
                        if (openBracket == -1 || closeBracket == -1) {
                            continue;
                        }
                        
                        String firstBrackets = userAgent.substring(openBracket + 1, closeBracket);
                        String[] parts = firstBrackets.split(";");
                        
                        if (parts.length < 2) {
                            continue;
                        }
                        
                        String secondFragment = parts[1].trim();
                        int slashIndex = secondFragment.indexOf('/');
                        String program = (slashIndex != -1)
                                ? secondFragment.substring(0, slashIndex)
                                : secondFragment;
                        
                        if ("Googlebot".equals(program)) {
                            googlebotCount++;
                        } else if ("YandexBot".equals(program)) {
                            yandexbotCount++;
                        }
                    }
                    
                    reader.close();
                    
                    if (totalLines > 0) {
                        double googlebotRatio = (double) googlebotCount / totalLines;
                        double yandexbotRatio = (double) yandexbotCount / totalLines;
                        System.out.printf("Доля запросов от Googlebot: %.6f\n", googlebotRatio);
                        System.out.printf("Доля запросов от YandexBot: %.6f\n", yandexbotRatio);
                    } else {
                        System.out.println("Файл пуст.");
                    }
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}