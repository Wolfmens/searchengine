package searchengine.services;


public interface SearchService {
    Object getSearchResponse(String query, String site, Integer offset, Integer limit);
}
