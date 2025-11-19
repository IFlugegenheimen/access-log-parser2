import java.io.File;
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
            }
        }
    }
}

