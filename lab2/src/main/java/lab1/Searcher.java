package lab1;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {

    private final String indexPath;
    private final int maxResults;
    private final Similarity similarity;

    public Searcher(String indexPath, int maxResults, Similarity similarity) {
        this.indexPath = indexPath;
        this.maxResults = maxResults;
        this.similarity = similarity;
    }

    public Query buildMobyDickQuery(String field) {
        BooleanQuery.Builder query = new BooleanQuery.Builder();
        query.add(new TermQuery(new Term(field, "moby")), BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term(field, "dick")), BooleanClause.Occur.MUST);
        return query.build();
    }

    public SearchResult executeQuery(Query query, String queryName) throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(similarity);

            long startTime = System.currentTimeMillis();
            TopDocs results = searcher.search(query, maxResults);
            long elapsed = System.currentTimeMillis() - startTime;

            int totalHits = totalHitCount(results.totalHits);

            System.out.println("\n" + "-".repeat(70));
            System.out.printf(" Indeks  : %s%n", indexPath);
            System.out.printf(" Upit    : %s%n", queryName);
            System.out.printf(" Objektni: %s%n", query.toString("content"));
            System.out.printf(" Rezultat: %d pogodaka, vreme: %d ms%n", totalHits, elapsed);
            System.out.println("-".repeat(70));

            int shown = Math.min(results.scoreDocs.length, 5);
            for (int i = 0; i < shown; i++) {
                ScoreDoc hit = results.scoreDocs[i];
                Document doc = searcher.storedFields().document(hit.doc);
                Explanation explanation = searcher.explain(query, hit.doc);
                System.out.printf("  [%d] score=%.6f | %s | title=\"%s\" | %,d B%n",
                        i + 1,
                        hit.score,
                        doc.get("filename"),
                        doc.get("title"),
                        Long.parseLong(doc.get("filesize")));
                System.out.println("  Explanation:");
                System.out.println(indent(explanation.toString(), "    "));
            }
            if (totalHits > shown) {
                System.out.printf("  ... i jos %d rezultata%n", totalHits - shown);
            }

            if (results.scoreDocs.length == 0) {
                return SearchResult.empty(totalHits);
            }

            ScoreDoc firstHit = results.scoreDocs[0];
            Document firstDoc = searcher.storedFields().document(firstHit.doc);
            return new SearchResult(totalHits, firstHit.score, firstDoc.get("filename"));
        }
    }

    private String indent(String text, String indentation) {
        return indentation + text.replace(System.lineSeparator(), System.lineSeparator() + indentation);
    }

    private int totalHitCount(Object totalHits) {
        try {
            return ((Number) totalHits.getClass().getMethod("value").invoke(totalHits)).intValue();
        } catch (ReflectiveOperationException methodMissing) {
            try {
                return ((Number) totalHits.getClass().getField("value").get(totalHits)).intValue();
            } catch (ReflectiveOperationException fieldMissing) {
                throw new IllegalStateException("Cannot read Lucene total hit count.", fieldMissing);
            }
        }
    }

    public static final class SearchResult {
        private final int totalHits;
        private final float topScore;
        private final String topFileName;

        public SearchResult(int totalHits, float topScore, String topFileName) {
            this.totalHits = totalHits;
            this.topScore = topScore;
            this.topFileName = topFileName;
        }

        public static SearchResult empty(int totalHits) {
            return new SearchResult(totalHits, 0.0f, "");
        }

        public int getTotalHits() {
            return totalHits;
        }

        public float getTopScore() {
            return topScore;
        }

        public String getTopFileName() {
            return topFileName;
        }
    }
}
