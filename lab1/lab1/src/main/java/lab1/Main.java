package lab1;

import org.apache.lucene.search.Query;

public class Main {

    static final String ORIGINAL_DOCS = "data/original";
    static final String SPLIT_DOCS = "data/split";
    static final String INDEX1_PATH = "index1";
    static final String INDEX2_PATH = "index2";

    public static void main(String[] args) throws Exception {

        System.out.println("Lab1 - Apache Lucene Indeksiranje");

        // Zadatak 5 - deljenje fajlova na 100 delova
        System.out.println("\n[Zadatak 5] Deljenje fajlova na 100 delova...");
        FileSplitter.splitAllFiles(ORIGINAL_DOCS, SPLIT_DOCS, 100);

        // Zadatak 4 - kreiranje prvog indeksa
        System.out.println("\n[Zadatak 4] Kreiranje indeksa 1 - originalni fajlovi...");
        Indexer indexer1 = new Indexer(INDEX1_PATH);
        long start1 = System.currentTimeMillis();
        int count1 = indexer1.indexDocuments(ORIGINAL_DOCS);
        long time1 = System.currentTimeMillis() - start1;
        long size1 = indexer1.getIndexSize();
        System.out.printf("  Indeks 1: %d fajlova | velicina: %,d B (%.1f KB) | vreme: %d ms%n",
                count1, size1, size1 / 1024.0, time1);

        // Zadatak 6 - kreiranje drugog indeksa
        System.out.println("\n[Zadatak 6] Kreiranje indeksa 2 - podeljeni fajlovi...");
        Indexer indexer2 = new Indexer(INDEX2_PATH);
        long start2 = System.currentTimeMillis();
        int count2 = indexer2.indexDocuments(SPLIT_DOCS);
        long time2 = System.currentTimeMillis() - start2;
        long size2 = indexer2.getIndexSize();
        System.out.printf("  Indeks 2: %d fajlova | velicina: %,d B (%.1f KB) | vreme: %d ms%n",
                count2, size2, size2 / 1024.0, time2);

        // Poredjenje indeksa
        System.out.println("\nPoredjenje indeksa:");
        System.out.printf("  %-25s %15s %15s%n", "Metrika", "Indeks 1", "Indeks 2");
        System.out.printf("  %-25s %15d %15d%n", "Broj dokumenata", count1, count2);
        System.out.printf("  %-25s %14.1f K %14.1f K%n", "Velicina indeksa", size1 / 1024.0, size2 / 1024.0);
        System.out.printf("  %-25s %14d ms %14d ms%n", "Vreme kreiranja", time1, time2);

        // Zadatak 7 - BooleanQuery
        System.out.println("\n[Zadatak 7] BooleanQuery: (people OR world) AND man NOT study");
        Searcher searcher1 = new Searcher(INDEX1_PATH, 20);
        Searcher searcher2 = new Searcher(INDEX2_PATH, 20);

        Query boolDirect = searcher1.buildBooleanQueryDirect();
        searcher1.executeQuery(boolDirect, "BooleanQuery direktno - Indeks 1");

        Query boolParsed = searcher1.buildBooleanQueryParsed();
        searcher1.executeQuery(boolParsed, "BooleanQuery parser - Indeks 1");

        Query boolDirect2 = searcher2.buildBooleanQueryDirect();
        searcher2.executeQuery(boolDirect2, "BooleanQuery direktno - Indeks 2");

        Query boolParsed2 = searcher2.buildBooleanQueryParsed();
        searcher2.executeQuery(boolParsed2, "BooleanQuery parser - Indeks 2");

        // Zadatak 8 - WildcardQuery
        System.out.println("\n[Zadatak 8] WildcardQuery: man*");

        Query wildcardDirect = searcher1.buildWildcardQueryDirect();
        searcher1.executeQuery(wildcardDirect, "WildcardQuery direktno - Indeks 1");

        Query wildcardParsed = searcher1.buildWildcardQueryParsed();
        searcher1.executeQuery(wildcardParsed, "WildcardQuery parser - Indeks 1");

        Query wildcardDirect2 = searcher2.buildWildcardQueryDirect();
        searcher2.executeQuery(wildcardDirect2, "WildcardQuery direktno - Indeks 2");

        Query wildcardParsed2 = searcher2.buildWildcardQueryParsed();
        searcher2.executeQuery(wildcardParsed2, "WildcardQuery parser - Indeks 2");

        System.out.println("\nLab1 zavrsen.");
    }
}