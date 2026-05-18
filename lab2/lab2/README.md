# Lab2 - Apache Lucene Similarity

Java aplikacija za indeksiranje 4 originalna tekstualna fajla iz lab1 kolekcije i poredjenje rangiranja rezultata pomocu:

- `BM25Similarity`
- `ClassicSimilarity`

Program za svaki rezultat prikazuje `ScoreDoc.score` i `IndexSearcher.explain(query, doc)`, a zatim racuna boost vrednost koja izjednacava score upita nad poljem `title` i poljem `content`.

## Sta je cilj laboratorijske vezbe

Cilj je da se pokaze da Lucene ne vraca samo dokumente koji odgovaraju upitu, vec ih i rangira. Rangiranje zavisi od izabrane mere slicnosti, odnosno od klase `Similarity`. U ovom programu se ista kolekcija indeksira dva puta:

1. prvi indeks koristi podrazumevani Lucene model `BM25Similarity`
2. drugi indeks koristi stariji TF-IDF model `ClassicSimilarity`

Nad oba indeksa se izvrsavaju isti upiti. Zatim se porede dobijene `score` vrednosti i objasnjava se zasto isti dokument nema isti score kada se pretrazuje po kratkom polju `title` i po dugom polju `content`.

## Struktura

```text
lab2/
|-- data/original/              # 4 originalna txt fajla
|-- lib/                        # Lucene jar fajlovi
|-- src/main/java/lab1/
|   |-- Indexer.java            # indeksiranje uz zadatu Similarity klasu
|   |-- Lab2Support.java        # pomocne metode za title i boost
|   |-- Main.java               # BM25 i Classic scenario
|   `-- Searcher.java           # pretraga, score i explanation
|-- src/test/java/lab1/
|   `-- Lab2SupportTest.java
`-- pom.xml
```

## Kolekcija dokumenata

U indeks ulaze sva 4 originalna fajla:

```text
data/original/moby_dick.txt
data/original/pride_and_prejudice.txt
data/original/sherlock_holmes.txt
data/original/war_and_peace.txt
```

Program ne koristi podeljenu kolekciju iz lab1. Folder `data/split` i drugi indeks iz lab1 nisu deo lab2 zadatka.

Upit u ovoj implementaciji je izabran tako da zajednicki rezultat bude `moby_dick.txt`. To ne znaci da se indeksira samo taj dokument. Sva 4 dokumenta su u indeksu i uticu na kolekcijske statistike koje Lucene koristi za score, na primer:

- `N`, ukupan broj dokumenata sa datim poljem
- `docFreq`, broj dokumenata koji sadrze termin
- `avgdl`, prosecna duzina polja u BM25 modelu
- `idf`, mera koliko je termin redak u kolekciji

U `Explanation` ispisu se vidi da je `N = 4`, sto potvrdjuje da Lucene score racuna nad celom kolekcijom, a ne samo nad jednim fajlom.

## Polja u indeksu

| Polje | Tip | Opis |
| --- | --- | --- |
| `title` | `TextField` | Naslov izveden iz imena fajla, npr. `moby_dick.txt` -> `moby dick` |
| `content` | `TextField` | Sadrzaj fajla |
| `filename` | `StringField` | Ime fajla |
| `filepath` | `StringField` | Apsolutna putanja |
| `filesize` | `LongPoint` + `StoredField` | Velicina fajla |

Polje `title` se dobija iz imena fajla. Na primer:

```text
moby_dick.txt -> moby dick
war_and_peace.txt -> war and peace
```

Polja `title` i `content` su `TextField`, sto znaci da se analiziraju pomocu `StandardAnalyzer`. To omogucava pretragu po terminima kao sto su `moby`, `dick`, `war` i `peace`.

## Upiti

Koristi se Boolean upit od dva termina koji vraca isti dokument nad oba polja:

```text
title:moby title:dick
content:moby content:dick
```

Zajednicki dokument je `moby_dick.txt`.

U kodu se ovi upiti prave direktno pomocu Lucene klasa:

```java
BooleanQuery.Builder query = new BooleanQuery.Builder();
query.add(new TermQuery(new Term(field, "moby")), BooleanClause.Occur.MUST);
query.add(new TermQuery(new Term(field, "dick")), BooleanClause.Occur.MUST);
```

`BooleanClause.Occur.MUST` znaci da dokument mora da sadrzi oba termina. Zato upit nad `title` vraca `moby_dick.txt`, jer njegov naslov sadrzi i `moby` i `dick`. Isti dokument vraca i upit nad `content`, jer se oba termina pojavljuju i u sadrzaju fajla.

Ostala tri dokumenta su indeksirana, ali ih ovaj konkretan upit ne vraca zato sto ne sadrze oba termina u trazenom polju.

## Pokretanje

Prvo otvoriti terminal u VS Code-u i preci u folder projekta:

```powershell
cd "D:\Претраживање Информација\lab2\lab2"
```

Ako je Maven instaliran:

```bash
mvn clean test
mvn clean package
java -jar target/lab2-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Ako Maven nije instaliran, moze se kompajlirati i pokrenuti direktno iz VS Code PowerShell terminala:

```powershell
$sources = Get-ChildItem -Recurse -Filter *.java -Path src\main\java | ForEach-Object { $_.FullName.Substring((Get-Location).Path.Length + 1) }
javac -cp "lib\*" -d target\classes $sources
java -cp "target\classes;lib\*" lab1.Main
```

Ako se koristi CMD terminal:

```cmd
cd /d "D:\Претраживање Информација\lab2\lab2"
dir /s /b src\main\java\*.java > sources.txt
javac -cp "lib\*" -d target\classes @sources.txt
java -cp "target\classes;lib\*" lab1.Main
```

Za pokretanje testova preko Maven-a:

```bash
mvn test
```

## Primer dobijenih vrednosti

Na ovoj kolekciji za `moby_dick.txt`:

| Similarity | title score | content score | Boost |
| --- | ---: | ---: | ---: |
| BM25Similarity | 1.192052 | 1.070680 | `content^1.113360` |
| ClassicSimilarity | 2.710044 | 0.021164 | `content^128.046860` |

Kod `content` polja score je manji jer je polje mnogo duze. U `Explanation` se to vidi kroz `dl / avgdl` kod BM25, odnosno kroz znatno manji `fieldNorm` kod ClassicSimilarity.

## Kako radi program

### 1. Main.java

Klasa `Main` pokrece ceo eksperiment. U `main` metodi se pozivaju dva scenarija:

```java
runSimilarityScenario("BM25Similarity", BM25_INDEX_PATH, new BM25Similarity());
runSimilarityScenario("ClassicSimilarity", CLASSIC_INDEX_PATH, new ClassicSimilarity());
```

To znaci da se ista kolekcija indeksira i pretrazuje dva puta, ali sa razlicitim modelima rangiranja.

Za svaki scenario se:

1. napravi indeks
2. izvrsi upit nad poljem `title`
3. izvrsi upit nad poljem `content`
4. ispise score i explanation
5. izracuna boost za upit koji ima manji score
6. ponovo izvrsi boostovan upit

### 2. Indexer.java

`Indexer` pravi Lucene indeks. Najvaznije je da prima objekat tipa `Similarity`:

```java
public Indexer(String indexPath, Similarity similarity)
```

Zatim se similarity postavlja u `IndexWriterConfig`:

```java
IndexWriterConfig config = new IndexWriterConfig(analyzer);
config.setSimilarity(similarity);
```

Ovim se ispunjava deo zadatka koji trazi da se mera slicnosti postavi prilikom indeksiranja.

Za svaki tekstualni fajl se pravi jedan Lucene `Document`. Dokument sadrzi naslov, sadrzaj, ime fajla, putanju i velicinu fajla. Najbitnija polja za lab2 su `title` i `content`, jer se nad njima poredi score.

### 3. Searcher.java

`Searcher` otvara indeks i izvrsava upit. I ovde se similarity postavlja, ali sada na `IndexSearcher`:

```java
IndexSearcher searcher = new IndexSearcher(reader);
searcher.setSimilarity(similarity);
```

Ovim se ispunjava deo zadatka koji trazi da se ista mera slicnosti koristi i prilikom pretrage.

Rezultati se dobijaju preko:

```java
TopDocs results = searcher.search(query, maxResults);
```

Score svakog rezultata se cita iz:

```java
ScoreDoc.score
```

Objasnjenje score-a se dobija metodom:

```java
Explanation explanation = searcher.explain(query, hit.doc);
```

`Explanation` je najvazniji deo za odbranu, jer pokazuje od cega se score sastoji.

### 4. Lab2Support.java

`Lab2Support` sadrzi pomocne metode:

- `titleFromFileName` pretvara ime fajla u naslov
- `boostToMatchScore` racuna boost faktor

Boost se racuna formulom:

```text
boost = veci_score / manji_score
```

Ako je `content` score manji od `title` score-a, program pravi:

```java
new BoostQuery(contentQuery, boost)
```

Tako se score boostovanog upita izjednacava sa score-om drugog upita.

## Kako tumaciti Explanation

Kod `BM25Similarity`, u objasnjenju se vide komponente:

- `idf`, koliko je termin redak u kolekciji
- `tf`, doprinos ucestanosti termina
- `dl`, duzina polja u dokumentu
- `avgdl`, prosecna duzina tog polja u kolekciji
- `k1` i `b`, BM25 parametri

Za `title` polje duzina je mala, npr. `dl = 2`, jer naslov `moby dick` ima dva termina. Za `content` polje duzina je mnogo veca, npr. preko 30000 termina. Zato duzina polja utice na score.

Kod `ClassicSimilarity`, u objasnjenju je posebno vazan `fieldNorm`. Za kratko polje `title` `fieldNorm` je veliki, a za dugacko polje `content` je veoma mali. Zbog toga je razlika izmedju `title` i `content` score-a mnogo veca kod `ClassicSimilarity` nego kod BM25.

## Kako braniti

Kratko objasnjenje za odbranu:

```text
U lab2 sam koristio originalnu kolekciju od 4 fajla iz lab1. Napravio sam dva odvojena indeksa nad istim fajlovima: jedan sa BM25Similarity i drugi sa ClassicSimilarity. Similarity postavljam i u IndexWriterConfig pri indeksiranju i u IndexSearcher pri pretrazi.

Dodao sam polje title koje se izvodi iz imena fajla, dok content sadrzi ceo tekst fajla. Zatim sam napravio Boolean upit od dva termina, moby i dick. Upit se izvrsava jednom nad title poljem i jednom nad content poljem. U oba slucaja zajednicki rezultat je moby_dick.txt.

Za svaki rezultat ispisujem ScoreDoc.score i Explanation. Explanation pokazuje kako Lucene racuna score, ukljucujuci idf, tf i normalizaciju duzine polja. Kod content polja score je manji zato sto je content mnogo duzi od title polja. Kod ClassicSimilarity je ta razlika mnogo veca zbog fieldNorm vrednosti.

Na kraju racunam boost kao odnos veceg i manjeg score-a i koristim BoostQuery da izjednacim score upita nad title i content poljem.
```

Ako profesor pita zasto se rezultat prikazuje samo za `moby_dick.txt`, odgovor je:

```text
Indeksirana su sva 4 dokumenta. Upit je namerno izabran tako da postoji jedan isti dokument koji se vraca i za title i za content pretragu. Ostali dokumenti ne zadovoljavaju ovaj upit jer ne sadrze oba termina, ali i dalje uticu na score kroz kolekcijske statistike kao sto su N, docFreq, avgdl i idf.
```
