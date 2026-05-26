package rs.elfak.lucenetika;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DocumentCollection {
    public static final long MIN_FILE_SIZE = 30L * 1024L;
    public static final long MAX_FILE_SIZE = 1024L * 1024L;

    private DocumentCollection() {
    }

    public static List<Path> validate(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IOException("Folder sa dokumentima ne postoji: " + directory);
        }

        try (Stream<Path> paths = Files.list(directory)) {
            List<Path> files = paths.filter(Files::isRegularFile).toList();
            Set<String> extensions = files.stream()
                    .map(DocumentCollection::extensionOf)
                    .collect(Collectors.toSet());

            if (files.size() < 3 || extensions.size() < 3) {
                throw new IOException("Folder mora da sadrzi najmanje 3 fajla u 3 razlicita formata: " + directory);
            }

            for (Path file : files) {
                long size = Files.size(file);
                if (size < MIN_FILE_SIZE || size > MAX_FILE_SIZE) {
                    throw new IOException("Fajl nije u opsegu 30KB-1MB: " + file + " (" + size + " B)");
                }
            }
            return files;
        }
    }

    private static String extensionOf(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(dotIndex + 1).toLowerCase() : "";
    }
}
