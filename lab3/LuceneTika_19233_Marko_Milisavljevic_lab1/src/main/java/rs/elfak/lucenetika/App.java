package rs.elfak.lucenetika;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.Query;

public class App {
    private static final String INDEX_NUMBER = "19233";
    private static final int MAX_RESULTS = 20;
    private static final int SHOWN_RESULTS = 5;

    public static void main(String[] args) throws Exception {
        configureLogging();

        Path projectRoot = Path.of("").toAbsolutePath();
        Path documentsDirectory = args.length > 0 ? Path.of(args[0]) : projectRoot.resolve("documents");
        Path indexDirectory = args.length > 1 ? Path.of(args[1]) : projectRoot.resolve("index");

        DocumentCollection.validate(documentsDirectory);

        try (Analyzer analyzer = new StandardAnalyzer()) {
            System.out.println("Lab1 - Apache Lucene i Tika");

            System.out.println("\n[Zadatak 4] Kreiranje indeksa");
            DocumentIndexer indexer = new DocumentIndexer(analyzer);
            long start = System.currentTimeMillis();
            int indexedCount = indexer.rebuildIndex(documentsDirectory, indexDirectory);
            long elapsedMillis = System.currentTimeMillis() - start;
            long indexSize = folderSize(indexDirectory);
            QueryFactory queryFactory = new QueryFactory("content", analyzer);
            SearchService searchService = new SearchService();

            System.out.printf("  Dokumenti: %s%n", documentsDirectory);
            System.out.printf("  Indeksirano: %d fajlova%n", indexedCount);
            System.out.printf("  Indeks: %d fajlova | velicina: %,d B (%.1f KB) | vreme: %d ms%n",
                    indexedCount, indexSize, indexSize / 1024.0, elapsedMillis);

            System.out.println("\n[Zadatak 5] BooleanQuery: +alice wonderland -zabranjeno");
            runQuery("BooleanQuery direktno", queryFactory.createLogicalObjectQuery(), searchService, indexDirectory);
            runQuery("BooleanQuery parser", queryFactory.parseLogicalQuery(), searchService, indexDirectory);

            System.out.println("\n[Zadatak 6] WildcardQuery za broj indeksa 19233: alice*");
            runQuery("WildcardQuery direktno",
                    queryFactory.createIndexSpecificObjectQuery(INDEX_NUMBER), searchService, indexDirectory);
            runQuery("WildcardQuery parser",
                    queryFactory.parseIndexSpecificQuery(INDEX_NUMBER), searchService, indexDirectory);

            System.out.println("\nKraj.");
        }
    }

    private static void configureLogging() {
        LogManager.getLogManager().reset();
        Logger.getLogger("").setLevel(Level.SEVERE);
    }

    private static void runQuery(String title, Query query, SearchService searchService, Path indexDirectory)
            throws Exception {
        SearchResponse response = searchService.search(indexDirectory, query, MAX_RESULTS);

        System.out.println("\n" + "-".repeat(70));
        System.out.printf(" Indeks  : %s%n", indexDirectory);
        System.out.printf(" Upit    : %s%n", title);
        System.out.printf(" Objektni: %s%n", query.toString("content"));
        System.out.printf(" Rezultat: %d pogodaka, vreme: %d ms%n", response.totalHits(), response.elapsedMillis());
        System.out.println("-".repeat(70));

        int shown = Math.min(response.results().size(), SHOWN_RESULTS);
        for (int i = 0; i < shown; i++) {
            SearchResult result = response.results().get(i);
            System.out.printf("  [%d] score=%.4f | %s | %,d B%n",
                    i + 1, result.score(), result.fileName(), result.fileSize());
        }
        if (response.totalHits() > shown) {
            System.out.printf("  ... i jos %d rezultata%n", response.totalHits() - shown);
        }
    }

    private static long folderSize(Path directory) throws Exception {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths.filter(Files::isRegularFile).mapToLong(path -> {
                try {
                    return Files.size(path);
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }).sum();
        }
    }
}
