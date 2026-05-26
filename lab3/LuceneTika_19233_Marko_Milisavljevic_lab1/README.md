# 19233_Marko_Milisavljevic_lab1

Java aplikacija za laboratorijsku vezbu iz predmeta Pretrazivanje informacija.
Aplikacija koristi Apache Lucene za indeksiranje i pretragu, a Apache Tika za
izdvajanje teksta iz fajlova razlicitih formata.

## Pokretanje u VS Code-u

1. Otvoriti folder `LuceneTika_19233_Marko_Milisavljevic_lab1` u VS Code-u.
2. Otvoriti terminal u root folderu projekta.
3. Pokrenuti aplikaciju:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

Pokretanje testova:

```powershell
powershell -ExecutionPolicy Bypass -File .\test.ps1
```

Skripte koriste lokalni Maven iz `%USERPROFILE%\.cache\lucene-tika-lab-tools`.
Ako Maven nije instaliran, skripta ga automatski preuzima pri prvom pokretanju.

## Dokumenti

Dokumenti za indeksiranje nalaze se u folderu `documents`.

Koriste se tri fajla preuzeta sa Project Gutenberg-a:

- `alice-wonderland.txt`
- `alice-wonderland.html`
- `alice-wonderland.epub`

Fajlovi su u tri razlicita formata i svaki je veci od 30 KB, a manji od 1 MB.
Ovim je ispunjen uslov da kolekcija sadrzi vise dokumenata razlicitog formata.

Ako dokumente treba ponovo preuzeti, koristi se:

```powershell
powershell -ExecutionPolicy Bypass -File .\download-documents.ps1
```

## Tok rada aplikacije

Pri pokretanju aplikacija prvo proverava da li postoji folder `documents` i da
li se u njemu nalaze najmanje tri fajla razlicitih formata. Proverava se i da su
fajlovi u opsegu od 30 KB do 1 MB.

Zatim se kreira Lucene indeks u folderu `index`. Ako indeks vec postoji, ponovo
se pravi preko postojeceg indeksa. Svaki fajl iz foldera `documents` postaje
jedan dokument u Lucene indeksu.

Za svaki dokument se cuvaju sledeca polja:

- `content` - tekstualni sadrzaj fajla koji izvlaci Apache Tika
- `fileName` - naziv fajla
- `path` - puna putanja do fajla
- `fileSize` - velicina fajla za range upite
- `fileSizeStored` - velicina fajla sacuvana za prikaz rezultata

Nakon indeksiranja aplikacija ispisuje broj indeksiranih fajlova, velicinu
indeksa i vreme potrebno za kreiranje indeksa.

## Upiti

Aplikacija izvrsava dva tipa upita.

Prvi upit je logicki upit od tri termina:

```text
+alice wonderland -zabranjeno
```

Ovaj upit znaci da dokument mora da sadrzi termin `alice`, moze da sadrzi
termin `wonderland`, a ne sme da sadrzi termin `zabranjeno`.

Isti upit se pravi na dva nacina:

- direktno, preko Lucene objektnih klasa (`BooleanQuery`, `TermQuery`)
- parsiranjem tekstualnog upita preko `QueryParser`

Drugi upit zavisi od poslednje cifre broja indeksa. Broj indeksa je `19233`, a
poslednja cifra je `3`, pa se koristi `WildcardQuery`.

Wildcard upit je:

```text
alice*
```

On pronalazi termine koji pocinju sa `alice`. I ovaj upit se pravi na dva nacina:

- direktno, kao `WildcardQuery`
- parsiranjem tekstualnog upita preko `QueryParser`

## Ispis rezultata

Za svaki upit aplikacija prikazuje:

- putanju do indeksa
- naziv upita
- Lucene oblik upita
- broj pogodaka
- vreme pretrage
- listu pronadjenih dokumenata sa score vrednoscu i velicinom fajla

Primer ispisa jednog rezultata:

```text
[1] score=0.2374 | alice-wonderland.epub | 137,516 B
```

## Struktura projekta

```text
LuceneTika_19233_Marko_Milisavljevic_lab1/
|-- documents/
|   |-- alice-wonderland.txt
|   |-- alice-wonderland.html
|   `-- alice-wonderland.epub
|-- index/
|-- src/
|   |-- main/java/rs/elfak/lucenetika/
|   |   |-- App.java
|   |   |-- DocumentCollection.java
|   |   |-- DocumentIndexer.java
|   |   |-- QueryFactory.java
|   |   |-- SearchResponse.java
|   |   |-- SearchResult.java
|   |   `-- SearchService.java
|   `-- test/java/rs/elfak/lucenetika/
|       |-- DocumentCollectionTest.java
|       `-- QueryFactoryTest.java
|-- download-documents.ps1
|-- run.ps1
|-- test.ps1
`-- pom.xml
```

## Opis klasa

`App.java` je glavna klasa. Ona povezuje proveru dokumenata, indeksiranje i
izvrsavanje upita.

`DocumentCollection.java` proverava da li kolekcija dokumenata ispunjava uslove
zadatka.

`DocumentIndexer.java` koristi Apache Tika za citanje sadrzaja fajlova i Apache
Lucene za kreiranje indeksa.

`QueryFactory.java` sadrzi metode za pravljenje upita. U njoj se nalaze objektni
model upita i parsiranje tekstualnih upita.

`SearchService.java` izvrsava pretragu nad kreiranim indeksom.

`SearchResult.java` i `SearchResponse.java` sluze za prenos podataka o
rezultatima pretrage.

## Direktno pokretanje preko Maven-a

Ako je Maven vec instaliran na racunaru, aplikacija moze da se pokrene i bez
PowerShell skripti:

```powershell
mvn test
mvn compile exec:java
```
