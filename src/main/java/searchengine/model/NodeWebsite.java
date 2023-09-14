package searchengine.model;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class NodeWebsite {

    private String webSite;
    private HashSet<String> links;
    private LinkedHashSet<NodeWebsite> linkChildsSet;
    private Site site;

    public NodeWebsite(String website, Site site) {
        this.webSite = website;
        links = new HashSet<>();
        linkChildsSet = new LinkedHashSet<>();
        this.site = site;
    }

    public String getWebsite() {
        return webSite;
    }

    public HashSet<String> getLinks() {
        return links;
    }

    public void addLink(String linkWeb, NodeWebsite nodeWebsite) {
        linkChildsSet.add(nodeWebsite);
        links.add(linkWeb);
    }

    public Site getSite() {
        return site;
    }
}
