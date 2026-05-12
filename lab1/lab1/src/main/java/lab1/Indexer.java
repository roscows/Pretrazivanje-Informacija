package lab1;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Indexer {

    private final String indexPath;

    public Indexer(String indexPath) {
        this.indexPath = indexPath;
    }

    public int indexDocuments(String docsPath) throws IOException {
        Path idxPath = Paths.get(indexPath);
        if (Files.exists(idxPath)) {
            deleteDirectory(idxPath.toFile());
        }
        Files.createDirectories(idxPath);

        Directory directory = FSDirectory.open(idxPath);
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        int count = 0;
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            File docsDir = new File(docsPath);
            if (!docsDir.exists() || !docsDir.isDirectory()) {
                throw new IOException("Direktorijum ne postoji: " + docsPath);
            }

            File[] files = docsDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files == null || files.length == 0) {
                System.out.println("Nema .txt fajlova u: " + docsPath);
                return 0;
            }

            for (File file : files) {
                count += indexFile(writer, file);
            }

            writer.commit();
        }

        System.out.printf("  Indeksirano %d fajlova -> %s%n", count, indexPath);
        return count;
    }

    private int indexFile(IndexWriter writer, File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()));
        long fileSize = file.length();
        String fileName = file.getName();
        String filePath = file.getAbsolutePath();

        Document doc = new Document();
        doc.add(new TextField("content", content, Field.Store.YES));
        doc.add(new StringField("filename", fileName, Field.Store.YES));
        doc.add(new StringField("filepath", filePath, Field.Store.YES));
        doc.add(new LongPoint("filesize", fileSize));
        doc.add(new StoredField("filesize", fileSize));

        writer.addDocument(doc);
        System.out.printf("  [+] %s (%,d B)%n", fileName, fileSize);
        return 1;
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        dir.delete();
    }

    public long getIndexSize() {
        return getFolderSize(new File(indexPath));
    }

    private long getFolderSize(File folder) {
        long size = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                size += f.isDirectory() ? getFolderSize(f) : f.length();
            }
        }
        return size;
    }
}