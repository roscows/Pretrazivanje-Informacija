package rs.elfak.lucenetika;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.Field;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

public class DocumentIndexer {
    private final Analyzer analyzer;
    private final Tika tika = new Tika();

    public DocumentIndexer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public int rebuildIndex(Path documentsDirectory, Path indexDirectory) throws IOException {
        Files.createDirectories(indexDirectory);

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(OpenMode.CREATE);
        try (IndexWriter writer = new IndexWriter(FSDirectory.open(indexDirectory), config);
                Stream<Path> paths = Files.walk(documentsDirectory)) {
            var files = paths.filter(Files::isRegularFile).toList();
            for (Path file : files) {
                writer.addDocument(toLuceneDocument(file));
            }
            writer.commit();
            return files.size();
        }
    }

    private Document toLuceneDocument(Path file) throws IOException {
        String content;
        try (InputStream inputStream = Files.newInputStream(file)) {
            content = tika.parseToString(inputStream);
        } catch (TikaException ex) {
            throw new IOException("Tika ne moze da procita fajl: " + file, ex);
        }

        Document document = new Document();
        document.add(new TextField("content", content, Field.Store.YES));
        document.add(new TextField("fileName", file.getFileName().toString(), Field.Store.YES));
        document.add(new StringField("path", file.toAbsolutePath().toString(), Field.Store.YES));
        document.add(new LongPoint("fileSize", Files.size(file)));
        document.add(new StoredField("fileSizeStored", Files.size(file)));
        return document;
    }
}
