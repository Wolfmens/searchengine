package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class DataSearchItem {

    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private double relevance;

}
