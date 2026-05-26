package rs.elfak.lucenetika;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.document.LongPoint;

public class QueryFactory {
    private final String fieldName;
    private final Analyzer analyzer;

    public QueryFactory(String fieldName, Analyzer analyzer) {
        this.fieldName = fieldName;
        this.analyzer = analyzer;
    }

    public Query createLogicalObjectQuery() {
        return new BooleanQuery.Builder()
                .add(new TermQuery(new Term(fieldName, "alice")), BooleanClause.Occur.MUST)
                .add(new TermQuery(new Term(fieldName, "wonderland")), BooleanClause.Occur.SHOULD)
                .add(new TermQuery(new Term(fieldName, "zabranjeno")), BooleanClause.Occur.MUST_NOT)
                .build();
    }

    public Query parseLogicalQuery() throws ParseException {
        return parser().parse("+alice wonderland -zabranjeno");
    }

    public Query createIndexSpecificObjectQuery(String indexNumber) {
        int lastDigit = Character.digit(indexNumber.charAt(indexNumber.length() - 1), 10);
        if (lastDigit == 0 || lastDigit == 5) {
            return TermRangeQuery.newStringRange(fieldName, "lucene", "tika", true, true);
        }
        if (lastDigit == 1 || lastDigit == 6) {
            return LongPoint.newRangeQuery("fileSize", 30L * 1024L, 1024L * 1024L);
        }
        if (lastDigit == 2 || lastDigit == 7) {
            return new PrefixQuery(new Term(fieldName, "pretra"));
        }
        if (lastDigit == 3 || lastDigit == 8) {
            return new WildcardQuery(new Term(fieldName, "alice*"));
        }
        return new PhraseQuery.Builder()
                .add(new Term(fieldName, "apache"))
                .add(new Term(fieldName, "lucene"))
                .build();
    }

    public Query parseIndexSpecificQuery(String indexNumber) throws ParseException {
        int lastDigit = Character.digit(indexNumber.charAt(indexNumber.length() - 1), 10);
        if (lastDigit == 0 || lastDigit == 5) {
            return parser().parse("[lucene TO tika]");
        }
        if (lastDigit == 1 || lastDigit == 6) {
            return LongPoint.newRangeQuery("fileSize", 30L * 1024L, 1024L * 1024L);
        }
        if (lastDigit == 2 || lastDigit == 7) {
            return parser().parse("pretra*");
        }
        if (lastDigit == 3 || lastDigit == 8) {
            return parser().parse("alice*");
        }
        return parser().parse("\"apache lucene\"");
    }

    private QueryParser parser() {
        return new QueryParser(fieldName, analyzer);
    }
}
