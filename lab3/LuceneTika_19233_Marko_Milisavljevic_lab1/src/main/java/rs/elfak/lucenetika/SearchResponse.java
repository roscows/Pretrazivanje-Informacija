package rs.elfak.lucenetika;

import java.util.List;

public record SearchResponse(int totalHits, long elapsedMillis, List<SearchResult> results) {
}
