package lab1;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;

public class AllQueryTypes {

    private final StandardAnalyzer analyzer = new StandardAnalyzer();

    // Cifra 0 ili 5 - TermRangeQuery
    public Query termRangeQueryDirect() {
        return new TermRangeQuery(
                "content",
                new BytesRef("man"),
                new BytesRef("men"),
                true,
                true);
    }

    public Query termRangeQueryParsed() throws ParseException {
        QueryParser parser = new QueryParser("content", analyzer);
        return parser.parse("content:[man TO men]");
    }

    // Cifra 1 ili 6 - PointRangeQuery
    public Query pointRangeQueryDirect() {
        return LongPoint.newRangeQuery("filesize", 50_000L, 200_000L);
    }

    public Query pointRangeQueryParsed() {
        return LongPoint.newRangeQuery("filesize", 50_000L, 200_000L);
    }

    // Cifra 2 ili 7 - PrefixQuery
    public Query prefixQueryDirect() {
        return new PrefixQuery(new Term("content", "man"));
    }

    public Query prefixQueryParsed() throws ParseException {
        QueryParser parser = new QueryParser("content", analyzer);
        return parser.parse("man*");
    }

    // Cifra 3 ili 8 - WildcardQuery
    public Query wildcardQueryDirect() {
        return new WildcardQuery(new Term("content", "man*"));
    }

    public Query wildcardQueryParsed() throws ParseException {
        QueryParser parser = new QueryParser("content", analyzer);
        parser.setAllowLeadingWildcard(true);
        return parser.parse("man*");
    }

    // Cifra 4 ili 9 - PhraseQuery
    public Query phraseQueryDirect() {
        PhraseQuery.Builder builder = new PhraseQuery.Builder();
        builder.add(new Term("content", "man"), 0);
        builder.add(new Term("content", "time"), 1);
        builder.setSlop(0);
        return builder.build();
    }

    public Query phraseQueryWithSlop() {
        PhraseQuery.Builder builder = new PhraseQuery.Builder();
        builder.add(new Term("content", "man"), 0);
        builder.add(new Term("content", "time"), 1);
        builder.setSlop(2);
        return builder.build();
    }

    public Query phraseQueryParsed() throws ParseException {
        QueryParser parser = new QueryParser("content", analyzer);
        return parser.parse("\"man time\"~2");
    }
}