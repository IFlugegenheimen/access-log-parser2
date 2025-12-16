import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

class LineTooLongException extends RuntimeException {
    public LineTooLongException(String message) {
        super(message);
    }
}

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
                    int maxLength = Integer.MIN_VALUE;
                    int minLength = Integer.MAX_VALUE;
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        int length = line.length();
                        
                        if (length > 1024) {
                            throw new LineTooLongException("Обнаружена строка длиной " + length +
                                    " символов, что превышает допустимый лимит в 1024 символа.");
                        }
                        
                        totalLines++;
                        if (length > maxLength) {
                            maxLength = length;
                        }
                        if (length < minLength) {
                            minLength = length;
                        }
                    }
                    
                    reader.close();
                    System.out.println("Общее количество строк в файле: " + totalLines);
                    System.out.println("Длина самой длинной строки: " + maxLength);
                    System.out.println("Длина самой короткой строки: " + minLength);
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}