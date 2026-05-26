package rs.elfak.lucenetika;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DocumentCollectionTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsDownloadedCollectionWithThreeFormatsInRequiredSizeRange() throws Exception {
        writeSizedFile(tempDir.resolve("alice.txt"), 31 * 1024);
        writeSizedFile(tempDir.resolve("alice.html"), 32 * 1024);
        writeSizedFile(tempDir.resolve("alice.epub"), 33 * 1024);

        assertEquals(3, DocumentCollection.validate(tempDir).size());
    }

    @Test
    void rejectsMissingDocumentFolder() {
        assertThrows(IOException.class, () -> DocumentCollection.validate(tempDir.resolve("documents")));
    }

    private static void writeSizedFile(Path path, int size) throws IOException {
        byte[] bytes = new byte[size];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 'a';
        }
        Files.write(path, bytes);
    }
}
