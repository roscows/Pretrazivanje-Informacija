package lab1;

import java.io.*;
import java.nio.file.*;

public class FileSplitter {

    public static void splitFile(File inputFile, String outputDir, int parts) throws IOException {
        Files.createDirectories(Paths.get(outputDir));

        byte[] content = Files.readAllBytes(inputFile.toPath());
        int partSize = (int) Math.ceil((double) content.length / parts);
        String baseName = inputFile.getName().replace(".txt", "");

        for (int i = 0; i < parts; i++) {
            int start = i * partSize;
            int end = Math.min(start + partSize, content.length);
            if (start >= content.length)
                break;

            String partFileName = String.format("%s_part_%03d.txt", baseName, i + 1);
            Path outputPath = Paths.get(outputDir, partFileName);
            Files.write(outputPath, java.util.Arrays.copyOfRange(content, start, end));
        }

        System.out.printf("  Podeljen: %s -> %d delova u %s%n", inputFile.getName(), parts, outputDir);
    }

    public static void splitAllFiles(String inputDir, String outputDir, int parts) throws IOException {
        File dir = new File(inputDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            System.out.println("Nema .txt fajlova u: " + inputDir);
            return;
        }

        System.out.printf("Deljenje %d fajlova na po %d delova...%n", files.length, parts);
        int totalParts = 0;
        for (File file : files) {
            splitFile(file, outputDir, parts);
            totalParts += parts;
        }
        System.out.printf("Ukupno kreirano: %d fajlova u %s%n", totalParts, outputDir);
    }

    public static void main(String[] args) throws IOException {
        String inputDir = args.length > 0 ? args[0] : "data/original";
        String outputDir = args.length > 1 ? args[1] : "data/split";
        int parts = args.length > 2 ? Integer.parseInt(args[2]) : 100;
        splitAllFiles(inputDir, outputDir, parts);
    }
}