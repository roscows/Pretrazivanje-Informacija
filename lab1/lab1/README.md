# Lab1 – Apache Lucene: Indeksiranje i pretraživanje

## Opis projekta

Java aplikacija koja koristi Apache Lucene 9.8.0 za indeksiranje kolekcije
tekstualnih fajlova i pretraživanje nad kreiranim indeksima.

---

## Struktura projekta

```
lab1/
├── data/
│   ├── original/               ← 4 originalna .txt fajla (30–500 KB)
│   │   ├── pride_and_prejudice.txt
│   │   ├── sherlock_holmes.txt
│   │   ├── moby_dick.txt
│   │   └── war_and_peace.txt
│   └── split/                  ← 400 podeljenih fajlova (auto-generiše se)
├── index1/                     ← Lucene indeks originalne kolekcije
├── index2/                     ← Lucene indeks podeljene kolekcije
├── src/main/java/lab1/
│   ├── Main.java               ← Ulazna tačka aplikacije
│   ├── Indexer.java            ← Kreiranje i upravljanje indeksom
│   ├── Searcher.java           ← Izvršavanje upita (Zadatak 7 i 8)
│   └── FileSplitter.java       ← Deljenje fajlova na N delova (Zadatak 5)
├── lib/                        ← Lucene JAR fajlovi (dodati ručno)
├── .classpath                  ← Eclipse konfiguracija
├── .project                    ← Eclipse projekat
└── pom.xml                     ← Maven konfiguracija
```

---

## Preduslovi

- Java 11 ili noviji
- Apache Lucene 9.8.0 JARs (preuzeti sa https://lucene.apache.org/core/)
  - `lucene-core-9.8.0.jar`
  - `lucene-queryparser-9.8.0.jar`
  - `lucene-analysis-common-9.8.0.jar`

---

## Podešavanje u Eclipse

1. **Preuzeti Lucene JAR fajlove** sa [lucene.apache.org](https://lucene.apache.org/core/)
   ili sa Maven Central i kopirati ih u folder `lib/`

2. **Importovati projekat u Eclipse:**
   - File → Import → General → Existing Projects into Workspace
   - Odabrati root folder `lab1/`

3. **Dodati JAR-ove na build path** (ako `.classpath` nije automatski prepoznat):
   - Desni klik na projekat → Build Path → Configure Build Path
   - Libraries → Add JARs → odabrati sva tri JAR-a iz `lib/`

4. **Pokrenuti** klasu `lab1.Main`

---

## Pokretanje sa Maven-om

```bash
# Buildovanje i kreiranje executable JAR
mvn clean package

# Pokretanje
java -jar target/lab1-1.0-SNAPSHOT-jar-with-dependencies.jar
```

---

## Implementirani zadaci

### Zadatak 4 – Indeksiranje originalnih fajlova
Klasa `Indexer` indeksira sve `.txt` fajlove iz `data/original/`.

Polja svakog dokumenta:
| Polje      | Tip          | Opis                              |
|------------|--------------|-----------------------------------|
| `content`  | TextField    | Sadržaj fajla (indeksiran, čuvan) |
| `filename` | StringField  | Naziv fajla                       |
| `filepath` | StringField  | Kompletna putanja na fajl sistemu |
| `filesize` | LongPoint    | Veličina u bajtovima (za range)   |
| `filesize` | StoredField  | Veličina (čuvana za prikaz)       |

### Zadatak 5 – Deljenje fajlova
Klasa `FileSplitter` deli svaki od 4 originalna fajla na 100 delova.
- Deljenje po broju bajtova (ne po rečenicama)
- Ukupno: 400 fajlova u `data/split/`

### Zadatak 6 – Poređenje indeksa
Aplikacija meri i ispisuje:
- Broj dokumenata u svakom indeksu
- Veličinu indeksa na disku
- Vreme kreiranja indeksa

### Zadatak 7 – BooleanQuery
Upit: `(people OR world) AND man NOT study`

Implementirano na dva načina:
1. **Direktno** – objektni model `BooleanQuery.Builder` sa `TermQuery`
2. **Parser** – `QueryParser.parse("(people OR world) AND man NOT study")`

Izvršava se nad oba indeksa.

### Zadatak 8 – WildcardQuery (cifra indeksa 3 ili 8)
Upit: `man*` (reči koje počinju sa "man")

Implementirano na dva načina:
1. **Direktno** – `new WildcardQuery(new Term("content", "man*"))`
2. **Parser** – `QueryParser.parse("man*")`

Izvršava se nad oba indeksa.

---

## Napomena o tipu upita za Zadatak 8

Ako vaša poslednja cifra broja indeksa **nije** 3 ili 8, izmenite klasu
`Searcher.java` prema sledećoj tabeli:

| Cifra | Tip upita       | Primer                                           |
|-------|-----------------|--------------------------------------------------|
| 0, 5  | TermRangeQuery  | `TermRangeQuery.newStringRange("content","a","m",true,true)` |
| 1, 6  | PointRangeQuery | `LongPoint.newRangeQuery("filesize", 50000L, 200000L)` |
| 2, 7  | PrefixQuery     | `new PrefixQuery(new Term("content", "man"))`    |
| 3, 8  | WildcardQuery   | `new WildcardQuery(new Term("content", "man*"))` |
| 4, 9  | PhraseQuery     | `new PhraseQuery("content","man","time")`        |
