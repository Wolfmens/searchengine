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
import java.util.concurrent.RecursiveAction;


public class BypassSites extends RecursiveAction {

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
    protected void compute() {
        try {
            Thread.sleep(500);
            LinkedHashSet<BypassSites> subTask = new LinkedHashSet<>();
            Document document = Jsoup.connect(url).timeout(200000).get();
            Elements elements = document.select("a");
            setFieldsOfPage(document);
            Optional<Page> optional = indexingService.getPageRepository().findByPath(url);
            if (!optional.isPresent()) {
                indexingService.getPageRepository().save(page);
                nodeWebsite.getSite().setStatusTime(LocalDateTime.now());
                indexingService.getSitesRepository().save(nodeWebsite.getSite());
            }
            indexingService.addPathToList(url);
            for (Element element : elements) {
                String urlOfElement = element.attr("abs:href");
                boolean hasHeadURL = urlOfElement.matches(nodeWebsite.getSite().getUrl() + "/.+");
                boolean HasNotURLByElementsWebsite = urlOfElement.matches(nodeWebsite.getSite().getUrl() + ".*#.*");

                if (urlOfElement.matches("http.*") && hasHeadURL) {
                    if (nodeWebsite.getLinks().contains(urlOfElement)
                            || indexingService.getPathList().contains(urlOfElement)
                            || HasNotURLByElementsWebsite) {
                        continue;
                    }
                    NodeWebsite child = new NodeWebsite(urlOfElement, nodeWebsite.getSite());
                    BypassSites bypassTask = new BypassSites(child, indexingService);
                    bypassTask.fork();
                    subTask.add(bypassTask);
                    nodeWebsite.addLink(urlOfElement, child);
                }
            }
            for (BypassSites bypass : subTask) {
                bypass.join();
            }
        } catch (Throwable e) {
            System.err.println(e.getClass() + " --- " + e.getMessage());
            if (e instanceof Error) {
                Site site = nodeWebsite.getSite();
                site.setStatus(Type.FAILED);
                site.setLastError(e.getMessage());
                indexingService.getSitesRepository().save(site);
            }
        }
    }

    private void setFieldsOfPage(Document doc) {
        int statusCode = doc.connection().response().statusCode();
        page.setContent(doc.toString());
        page.setPath(url);
        page.setSiteId(nodeWebsite.getSite());
        page.setCode(statusCode);
    }


}
