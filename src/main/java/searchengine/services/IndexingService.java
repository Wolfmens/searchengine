package searchengine.services;

import java.util.Collection;
import java.util.Properties;

public interface IndexingService {

    boolean indexing ();
    void setStatusIndex(boolean statusIndex);
    boolean isStatusIndex();
    boolean stopIndex();
    PageRepository getPageRepository();
    SitesRepository getSitesRepository();
    void addPathToList(String url);
    Collection<String> getPathList();
}
