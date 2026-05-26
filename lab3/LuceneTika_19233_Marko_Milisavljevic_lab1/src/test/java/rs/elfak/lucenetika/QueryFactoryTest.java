package rs.elfak.lucenetika;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.junit.jupiter.api.Test;

class QueryFactoryTest {
    private final QueryFactory factory = new QueryFactory("content", new StandardAnalyzer());

    @Test
    void logicalObjectQueryMatchesParsedLogicalQuery() throws Exception {
        Query objectQuery = factory.createLogicalObjectQuery();
        Query parsedQuery = factory.parseLogicalQuery();

        assertEquals(objectQuery.toString("content"), parsedQuery.toString("content"));
        assertInstanceOf(BooleanQuery.class, objectQuery);
        BooleanQuery booleanQuery = (BooleanQuery) objectQuery;
        assertEquals(3, booleanQuery.clauses().size());
        assertTrue(booleanQuery.clauses().stream().anyMatch(clause -> clause.getOccur() == BooleanClause.Occur.MUST));
        assertTrue(booleanQuery.clauses().stream().anyMatch(clause -> clause.getOccur() == BooleanClause.Occur.SHOULD));
        assertTrue(booleanQuery.clauses().stream().anyMatch(clause -> clause.getOccur() == BooleanClause.Occur.MUST_NOT));
    }

    @Test
    void indexNumber19233SelectsWildcardQueryAndParserEquivalent() throws Exception {
        Query objectQuery = factory.createIndexSpecificObjectQuery("19233");
        Query parsedQuery = factory.parseIndexSpecificQuery("19233");

        assertInstanceOf(WildcardQuery.class, objectQuery);
        assertEquals("alice*", objectQuery.toString("content"));
        assertEquals(new QueryParser("content", new StandardAnalyzer()).parse("alice*").toString("content"),
                parsedQuery.toString("content"));
    }
}
