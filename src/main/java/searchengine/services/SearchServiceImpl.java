package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.search.DataSearchItem;
import searchengine.dto.search.SearchResponse;
import searchengine.model.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final IndexingService indexingService;
    private final String[] SERVICE_FORMS = {"ПРЕДЛ", "СОЮЗ", "МЕЖД"};
    private List<String> listLemmas;
    private final float COEFF_FREQUENCY_LEMMA_ON_PAGES = 0.8F;
    private final int SIZE_LIMIT_OF_SNIPPET = 180;
    private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#####");
    private List<Page> pageList = new ArrayList<>();
    private final LuceneMorphology luceneMorphology;
    private HashMap<String,Integer> mapOfCountPagesBySite;

    @Override
    public Object getSearchResponse(String query, String site, Integer offset, Integer limit) {
        if (query.isEmpty()) {
            return getGeneratedResponse(true, new ArrayList<>(), 0);
        }
        offset = getValueOffset(offset);
        limit = getValueLimit(limit);

        mapOfCountPagesBySite = indexingService.getCountPagesBySite();
        listLemmas = getFilteredAndSortedListWithLemmas(query, site);
        if (listLemmas.isEmpty()) {
            return getGeneratedResponse(true, new ArrayList<>(), 0);
        }
        fillPageList(site);

        HashMap<Page, Double> mapPageByOtnRelevance = getMapPageByOtnRelevance(pageList);
        HashMap<Page, Double> sortedMapPageByOtnRelevance =
                getSortedMapPageByOtnRelevance(mapPageByOtnRelevance);
        List<DataSearchItem> listData = getListObjectsBySearch(sortedMapPageByOtnRelevance, listLemmas, query);
        List<DataSearchItem> listDataByLimitAndOffset = listData.stream().skip(offset).limit(limit).toList();
        pageList.clear();

        return getGeneratedResponse(true, listDataByLimitAndOffset, listData.size());
    }

    private int getValueLimit(Integer limit) {
        if (limit == null) {
            return 20;
        }
        return limit;
    }

    private int getValueOffset(Integer offset) {
        if (offset == null) {
           return 0;
        }
        return offset;
    }

    private void fillPageList (String site){
        if (site == null) {
            for (Site siteFromRepository : indexingService.getSitesRepository().findAll()) {
                List<String> listLemmasFilter = listLemmas.stream()
                        .filter(l -> indexingService.getLemmaRepository().findByLemma(l).get(0)
                                .getSiteId().getId() == siteFromRepository.getId())
                        .toList();
                if (!listLemmasFilter.isEmpty()) {
                    pageList.addAll(getFilterPageList(siteFromRepository.getUrl(), listLemmasFilter));
                }
            }
        } else {
            pageList.addAll(getFilterPageList(site, listLemmas));
        }
    }

    private SearchResponse getGeneratedResponse(boolean result, List<DataSearchItem> list, int count) {
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setResult(result);
        searchResponse.setData(list);
        searchResponse.setCount(count);

        return searchResponse;
    }

    private HashMap<Page, Double> getMapPageByOtnRelevance(List<Page> pageList) {
        HashMap<Page, Float> mapPageByAbsRelevance = new HashMap<>();
        HashMap<Page, Double> mapPageByOtnRelevance = new HashMap<>();
        List<Lemma> list = listLemmas.stream()
                .map(l -> indexingService.getLemmaRepository().findByLemma(l).get(0))
                .toList();
        for (Page page : pageList) {
            float absRel = 0;
            for (Lemma lemma : list) {
                Optional<Index> optionalIndex = indexingService.getIndexLemmaRepository()
                        .findByLemmaIdAndPageId(lemma, page);
                if (optionalIndex.isPresent()) {
                    absRel += optionalIndex.get().getRank();
                }
            }
            mapPageByAbsRelevance.put(page, absRel);
        }
        float maxAbsRelevance = mapPageByAbsRelevance.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getValue();
        for (Map.Entry<Page, Float> entry : mapPageByAbsRelevance.entrySet()) {
            double otnRel = (double) entry.getValue() / maxAbsRelevance;
            mapPageByOtnRelevance.put(entry.getKey(), otnRel);
        }
        return mapPageByOtnRelevance;
    }

    private HashMap<Page,Double> getSortedMapPageByOtnRelevance (HashMap<Page,Double> map){
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(LinkedHashMap::new, (m, c) -> m.put(c.getKey(), c.getValue()), LinkedHashMap::putAll);
    }

    private List<Page> getFilterPageList(String site, List<String> listLemmas) {
        Site siteFromRepository = indexingService.getSitesRepository().findByUrl(site).get();
        Lemma rareLemmaFromRepository = indexingService.getLemmaRepository().findByLemma(listLemmas.get(0)).get(0);
        List<Index> listOfIndexesFromRepositoryByRareLemma = indexingService.getIndexLemmaRepository()
                                                                            .findAllByLemmaId(rareLemmaFromRepository);
        List<Page> pageList = listOfIndexesFromRepositoryByRareLemma.stream()
                .filter(i -> i.getPageId().getSiteId().getId() == siteFromRepository.getId())
                .map(Index::getPageId)
                .toList();
        if (pageList.isEmpty()) {
            return pageList;
        }
        Iterator<Page> iterator = pageList.iterator();
        for (int i = 1; i < listLemmas.size(); i++) {
            Lemma lemma = indexingService.getLemmaRepository().findByLemma(listLemmas.get(i)).get(0);
            if (lemma.getSiteId().getId() != siteFromRepository.getId()) {
                continue;
            }
            List<Index> listOfIndexesFromRepositoryByLemma = indexingService.getIndexLemmaRepository()
                                                                            .findAllByLemmaId(lemma);
            while (iterator.hasNext()) {
                Page page = iterator.next();
                Optional<Index> optional =
                        listOfIndexesFromRepositoryByLemma.stream()
                                .filter(in -> in.getPageId().getId() == page.getId())
                                .findAny();
                if (!optional.isPresent() && !optional.isEmpty()) {
                    iterator.remove();
                }
            }
        }
        return pageList;
    }


    private List<DataSearchItem> getListObjectsBySearch(Map<Page, Double> map, List<String> lemmas, String query) {
        List<DataSearchItem> list = new ArrayList<>();
        String[] queryElements = query.split(" ");
        for (Map.Entry<Page, Double> entry : map.entrySet()) {
            HashSet<String> setSentencesFromPage = new HashSet<>();
            HashSet<String> setSentencesFromPageWithSecretions = new HashSet<>();
            Page page = entry.getKey();
            Site site = page.getSiteId();
            StringBuilder builder = new StringBuilder();

            fillingSetSentences(queryElements,lemmas,page,setSentencesFromPage);
            fillingSetSentencesWithSecretions(setSentencesFromPage,lemmas,queryElements,setSentencesFromPageWithSecretions);
            fillingBuilderSnippet(setSentencesFromPageWithSecretions,builder);

            if (builder.toString().isBlank()) {
                continue;
            }
            double otnRel = getOntRelValue(entry.getValue());
            String uri = getUri(page.getPath(), site.getUrl());
            String htmlContent = indexingService.getPageRepository().findById(page.getId()).get().getContent();
            Document document = Jsoup.parse(htmlContent);
            list.add(new DataSearchItem(site.getUrl(),
                    site.getName(),
                    uri,
                    document.title(),
                    builder.toString().strip(),
                    otnRel));
        }
        return list;
    }

    private double getOntRelValue(double oldValue) {
        String replaceSign = DECIMAL_FORMAT.format(oldValue).replace(",", ".");
        return Double.parseDouble(replaceSign);
    }

    private void fillingSetSentences(String[] elements,
                                     List<String> lemmas,
                                     Page page,
                                     HashSet<String> set) {
        for (String queryWord : elements) {
            String lemmaFromQuery = luceneMorphology.getNormalForms(queryWord.toLowerCase()).get(0).strip();
            if (!lemmas.contains(lemmaFromQuery)) {
                continue;
            }
            HashMap<String, Set<String>> mapWordsAndSentencesWithThem
                    = indexingService.getMap().get(page.getId());
            Set<String> setSentencesByPageFromBase = mapWordsAndSentencesWithThem.get(queryWord);
            if (setSentencesByPageFromBase == null) {
                continue;
            }
            set.addAll(setSentencesByPageFromBase);
        }
    }

    private void fillingSetSentencesWithSecretions(HashSet<String> setBeforeChange,
                                                   List<String> lemmas,
                                                   String[] elements,
                                                   HashSet<String> setAfterChange) {
        for (String sentence : setBeforeChange) {
            String wordChanged = sentence;
            for (String wordFromQuery : elements) {
                String lemmaFromQuery = luceneMorphology.getNormalForms(wordFromQuery.toLowerCase()).get(0).strip();
                if (!sentence.contains(wordFromQuery)) {
                    continue;
                }
                if (!lemmas.contains(lemmaFromQuery)) {
                    continue;
                }
                wordChanged = wordChanged.replace(wordFromQuery, "<b>" + wordFromQuery + "</b>");
            }
            setAfterChange.add(wordChanged);
        }
    }

    private void fillingBuilderSnippet(HashSet<String> set, StringBuilder builder) {
        for (String sentence : set){
            if (builder.length() <= SIZE_LIMIT_OF_SNIPPET){
                builder.append(sentence).append("...");
            }
        }
    }

    private String getUri(String path, String siteUrl) {
        return path.equals(siteUrl) ? "/" : getSiteUri(path, siteUrl);
    }

    private String getSiteUri(String path, String site) {
        return path.replace(site,"");
    }

    private List<String> getListOfLemmasFromQuery(String query) {
        List<String> listLemmas = new ArrayList<>();
        try {
            String[] elementsOfQuery = query.split(" ");
            for (String word : elementsOfQuery) {
                if (!checkWordByServiceForm(word, luceneMorphology)) {
                    String wordBaseForm = luceneMorphology.getNormalForms(word.toLowerCase()).get(0).strip();
                    boolean hasLemmaInRepository = indexingService.getLemmaRepository()
                            .findByLemma(wordBaseForm)
                            .isEmpty();
                    if (!hasLemmaInRepository) {
                        listLemmas.add(wordBaseForm);
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println(ex.getClass() + "---" + ex.getMessage());
        }
        return listLemmas;
    }

    private boolean checkWordByServiceForm(String word, LuceneMorphology luceneMorphology) {
        for (String form : SERVICE_FORMS) {
            if (luceneMorphology.getMorphInfo(word.toLowerCase()).get(0).contains(form)) {
                return true;
            }
        }
        return false;
    }

    private void excludingLemmasFromListThereTooMany(List<String> lemmas, Site site) {
        int countPageOnList = mapOfCountPagesBySite.get(site.getUrl());
        int eightyPercentOfSite = (int) (countPageOnList * COEFF_FREQUENCY_LEMMA_ON_PAGES);
        Iterator<String> iterator = lemmas.iterator();

        while (iterator.hasNext()) {
            Lemma lemmaEntity;
            Optional<Lemma> lemmaOptional = indexingService.getLemmaRepository().findByLemma(iterator.next())
                    .stream()
                    .filter(l -> l.getSiteId().getId() == site.getId())
                    .findAny();
            if (lemmaOptional.isPresent()) {
                lemmaEntity = lemmaOptional.get();
                int frequencyOfLemma = lemmaEntity.getFrequency();
                if (frequencyOfLemma >= eightyPercentOfSite) {
                    iterator.remove();
                }
            }
        }
    }

    private List<String> sortLemmasByFrequency(List<String> lemmas) {
        HashMap<String, Integer> mapLemmasByFrequency = new HashMap<>();
        for (String lemma : lemmas) {
            Lemma lemmaFromRepository = indexingService.getLemmaRepository().findByLemma(lemma).get(0);
            int frequency = lemmaFromRepository.getFrequency();
            mapLemmasByFrequency.put(lemma, frequency);
        }
        return mapLemmasByFrequency.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<String> getFilteredAndSortedListWithLemmas(String query, String site) {
        List<String> lemmas = getListOfLemmasFromQuery(query);
        if (site == null) {
            List<Site> siteList = indexingService.getSitesRepository().findAll();
            for (Site siteEntity : siteList) {
                if (siteEntity.getStatus() == Type.INDEXED || siteEntity.getStatus() == Type.FAILED) {
                    excludingLemmasFromListThereTooMany(lemmas, siteEntity);
                }
            }
        } else {
            Site siteFromRepository = indexingService.getSitesRepository().findByUrl(site).get();
            Iterator<String> iterator = lemmas.iterator();
            while (iterator.hasNext()) {
                Lemma lemma = indexingService.getLemmaRepository().findByLemma(iterator.next()).get(0);
                if (lemma.getSiteId().getId() != siteFromRepository.getId()) {
                    iterator.remove();
                }
            }
            excludingLemmasFromListThereTooMany(lemmas, siteFromRepository);
        }
        return sortLemmasByFrequency(lemmas);
    }
}
