package lab1;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {

    private final String indexPath;
    private final int maxResults;

    public Searcher(String indexPath, int maxResults) {
        this.indexPath = indexPath;
        this.maxResults = maxResults;
    }

    // Zadatak 7 - BooleanQuery direktno
    public Query buildBooleanQueryDirect() {
        BooleanQuery.Builder orPart = new BooleanQuery.Builder();
        orPart.add(new TermQuery(new Term("content", "people")), BooleanClause.Occur.SHOULD);
        orPart.add(new TermQuery(new Term("content", "world")), BooleanClause.Occur.SHOULD);

        BooleanQuery.Builder mainQuery = new BooleanQuery.Builder();
        mainQuery.add(orPart.build(), BooleanClause.Occur.MUST);
        mainQuery.add(new TermQuery(new Term("content", "man")), BooleanClause.Occur.MUST);
        mainQuery.add(new TermQuery(new Term("content", "study")), BooleanClause.Occur.MUST_NOT);

        return mainQuery.build();
    }

    // Zadatak 7 - BooleanQuery parserom
    public Query buildBooleanQueryParsed() throws ParseException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser("content", analyzer);
        return parser.parse("(people OR world) AND man NOT study");
    }

    // Zadatak 8 - WildcardQuery direktno
    public Query buildWildcardQueryDirect() {
        return new WildcardQuery(new Term("content", "man*"));
    }

    // Zadatak 8 - WildcardQuery parserom
    public Query buildWildcardQueryParsed() throws ParseException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser("content", analyzer);
        parser.setAllowLeadingWildcard(true);
        return parser.parse("man*");
    }

    public int executeQuery(Query query, String queryName) throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)))) {
            IndexSearcher searcher = new IndexSearcher(reader);

            long startTime = System.currentTimeMillis();
            TopDocs results = searcher.search(query, maxResults);
            long elapsed = System.currentTimeMillis() - startTime;

            int totalHits = (int) results.totalHits.value();

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
                System.out.printf("  [%d] score=%.4f | %s | %,d B%n",
                        i + 1,
                        hit.score,
                        doc.get("filename"),
                        Long.parseLong(doc.get("filesize")));
            }
            if (totalHits > shown) {
                System.out.printf("  ... i jos %d rezultata%n", totalHits - shown);
            }

            return totalHits;
        }
    }
}