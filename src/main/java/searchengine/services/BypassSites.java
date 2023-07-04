package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.NodeWebsite;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Type;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.concurrent.RecursiveTask;


public class BypassSites extends RecursiveTask<Boolean> {

    private NodeWebsite nodeWebsite;
    private String url;
    private IndexingService indexingService;
    private Page page;

    public BypassSites(NodeWebsite nodeWebsite, IndexingService indexingService) {
        page = new Page();
        this.indexingService = indexingService;
        this.nodeWebsite = nodeWebsite;
        url = nodeWebsite.getWebsite();
    }

    @Override
    protected Boolean compute() {
        try {
            Thread.sleep(500);
            LinkedHashSet<BypassSites> subTask = new LinkedHashSet<>();
            Document document = Jsoup.connect(url).timeout(200000).get();
            Elements elements = document.select("a");
            setFieldsOfPage(document,url, page);
            Optional<Page> optional = indexingService.getPageRepository().findByPath(url);
            if (!optional.isPresent()) {
                saveToRepository(page);
            }
            indexingService.addPathToList(url);
            for (Element element : elements) {
                String urlOfElement = element.attr("abs:href");
                boolean hasHeadURL = urlOfElement.matches(nodeWebsite.getSite().getUrl() + "/.+");
                boolean hasNotURLByElementsWebsite = urlOfElement.matches(nodeWebsite.getSite().getUrl() + ".*#.*");

                if (urlOfElement.matches("http.*") && hasHeadURL) {
                    if (nodeWebsite.getLinks().contains(urlOfElement)
                            || indexingService.getPathList().contains(urlOfElement)
                            || hasNotURLByElementsWebsite) {
                        continue;
                    }
                    if (!indexingService.isStatusIndex()) {
                        addPageToRepositoryIfBypassStop(urlOfElement);
                        System.err.println(urlOfElement + " - " + " завершено");
                    } else {
                        forkBypass(urlOfElement,subTask);
                    }
                }
            }
            for (BypassSites bypass : subTask) {
               bypass.join();
            }
        } catch (Error er){
            Site site = nodeWebsite.getSite();
            site.setStatus(Type.FAILED);
            site.setLastError(er.getMessage());
            indexingService.getSitesRepository().save(site);
        } catch (Exception ex){
            System.err.println(ex.getClass() + " --- " + ex.getMessage());
        }
        return indexingService.isStatusIndex();
    }

    private void setFieldsOfPage(Document doc, String url, Page page) {
        int statusCode = doc.connection().response().statusCode();
        page.setContent(doc.toString());
        page.setPath(url);
        page.setSiteId(nodeWebsite.getSite());
        page.setCode(statusCode);
    }

    private void addPageToRepositoryIfBypassStop(String url) throws Exception {
        Document document = Jsoup.connect(url).timeout(200000).get();
        Page page = new Page();
        setFieldsOfPage(document, url, page);
        Optional<Page> optional = indexingService.getPageRepository().findByPath(url);
        if (!optional.isPresent()) {
           saveToRepository(page);
        }
        indexingService.addPathToList(url);
    }

    private void forkBypass (String url,  LinkedHashSet<BypassSites> subTask){
        NodeWebsite child = new NodeWebsite(url, nodeWebsite.getSite());
        BypassSites bypassTask = new BypassSites(child, indexingService);
        bypassTask.fork();
        subTask.add(bypassTask);
        nodeWebsite.addLink(url, child);
    }

    private void saveToRepository(Page page){
        indexingService.getPageRepository().save(page);
        nodeWebsite.getSite().setStatusTime(LocalDateTime.now());
        indexingService.getSitesRepository().save(nodeWebsite.getSite());
    }

}
