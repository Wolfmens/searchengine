package searchengine.services;

import searchengine.dto.search.SearchResponse;

public interface SearchService {
    Object getSearchResponse(String query, String site, Integer offset, Integer limit);
}
