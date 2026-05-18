package lab1;

import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;

public class Main {

    static final String ORIGINAL_DOCS = "data/original";
    static final String BM25_INDEX_PATH = "index-bm25";
    static final String CLASSIC_INDEX_PATH = "index-classic";

    public static void main(String[] args) throws Exception {

        System.out.println("Lab2 - Apache Lucene similarity, score i explanation");
        runSimilarityScenario("BM25Similarity", BM25_INDEX_PATH, new BM25Similarity());
        runSimilarityScenario("ClassicSimilarity", CLASSIC_INDEX_PATH, new ClassicSimilarity());
        System.out.println("\nLab2 zavrsen.");
    }

    private static void runSimilarityScenario(String label, String indexPath, Similarity similarity) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.printf("[%s] Kreiranje indeksa nad originalna 4 fajla%n", label);
        System.out.println("=".repeat(80));

        Indexer indexer = new Indexer(indexPath, similarity);
        long start = System.currentTimeMillis();
        int count = indexer.indexDocuments(ORIGINAL_DOCS);
        long elapsed = System.currentTimeMillis() - start;
        long size = indexer.getIndexSize();
        System.out.printf("  Indeks: %d fajlova | velicina: %,d B (%.1f KB) | vreme: %d ms%n",
                count, size, size / 1024.0, elapsed);

        Searcher searcher = new Searcher(indexPath, 10, similarity);
        Query titleQuery = searcher.buildMobyDickQuery("title");
        Query contentQuery = searcher.buildMobyDickQuery("content");

        Searcher.SearchResult titleResult = searcher.executeQuery(titleQuery, label + " - title:moby title:dick");
        Searcher.SearchResult contentResult = searcher.executeQuery(contentQuery, label + " - content:moby content:dick");

        printBoostedComparison(label, searcher, titleQuery, contentQuery, titleResult, contentResult);
    }

    private static void printBoostedComparison(
            String label,
            Searcher searcher,
            Query titleQuery,
            Query contentQuery,
            Searcher.SearchResult titleResult,
            Searcher.SearchResult contentResult) throws Exception {

        String sharedDocument = titleResult.getTopFileName().equals(contentResult.getTopFileName())
                ? titleResult.getTopFileName()
                : "proveriti top rezultate";

        System.out.println("\n" + "-".repeat(80));
        System.out.printf("[%s] Poredjenje score vrednosti za zajednicki dokument: %s%n", label, sharedDocument);
        System.out.printf("  title score  : %.6f%n", titleResult.getTopScore());
        System.out.printf("  content score: %.6f%n", contentResult.getTopScore());

        if (titleResult.getTopScore() == 0.0f || contentResult.getTopScore() == 0.0f) {
            System.out.println("  Boost nije izracunat jer jedan upit nema rezultat.");
            return;
        }

        if (titleResult.getTopScore() < contentResult.getTopScore()) {
            float boost = Lab2Support.boostToMatchScore(contentResult.getTopScore(), titleResult.getTopScore());
            Query boostedTitleQuery = new BoostQuery(titleQuery, boost);
            System.out.printf("  Manji score ima title upit. Boost za title: %.6f%n", boost);
            searcher.executeQuery(boostedTitleQuery, label + " - boosted title query");
        } else {
            float boost = Lab2Support.boostToMatchScore(titleResult.getTopScore(), contentResult.getTopScore());
            Query boostedContentQuery = new BoostQuery(contentQuery, boost);
            System.out.printf("  Manji score ima content upit. Boost za content: %.6f%n", boost);
            searcher.executeQuery(boostedContentQuery, label + " - boosted content query");
        }
    }
}
