package rs.elfak.lucenetika;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;

public class SearchService {
    public SearchResponse search(Path indexDirectory, Query query, int limit) throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexDirectory))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            List<SearchResult> results = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            var topDocs = searcher.search(query, limit);
            long elapsedMillis = System.currentTimeMillis() - startTime;

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document document = searcher.storedFields().document(scoreDoc.doc);
                results.add(new SearchResult(
                        document.get("fileName"),
                        document.get("path"),
                        Long.parseLong(document.get("fileSizeStored")),
                        scoreDoc.score));
            }
            return new SearchResponse((int) topDocs.totalHits.value, elapsedMillis, results);
        }
    }
}
