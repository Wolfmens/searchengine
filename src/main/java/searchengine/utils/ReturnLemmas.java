package searchengine.utils;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.services.IndexingService;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ReturnLemmas {

    private final IndexingService indexingService;
    private final Site site;
    private final Page page;
    private final HashMap<String, Integer> lemmasMap = new HashMap<>();


    public void getLemmas(String text) throws Exception {
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
        String[] textForGetLemmas = getMassiveElementsFromContent(text);
        List<String> wordBaseForms = new ArrayList<>();
        for (String word : textForGetLemmas) {
            if (!ApplicationConstantsAndChecks.checkWordByServiceForm(word, luceneMorphology)) {
                String wordBaseForm = luceneMorphology.getNormalForms(word.toLowerCase()).get(0).strip();
                if (!wordBaseForms.contains(wordBaseForm)) {
                    addLemmaToRepository(wordBaseForm);
                    getFindWordsInPage(text,word);
                }
                wordBaseForms.add(wordBaseForm);
                addLemmaToMap(wordBaseForm);
            }
        }
        addIndexToRepository();
    }

    private void getFindWordsInPage (String text, String word){
        Pattern pattern = Pattern.compile("[А-Яа-я\\s,]*\\s*,*" + word + ",*\\s*[А-Яа-я\\s,]*");
        Matcher matcher = pattern.matcher(text);
        Set<String> results = matcher.results()
                .map(r -> r.group().strip())
                .collect(Collectors.toSet());
        HashMap<String,Set<String>> mapWordAndFindInText = new HashMap<>(){{put(word,results);}};
        if (indexingService.getMapOfPositionWordsByPage().containsKey(page.getId())){
            mapWordAndFindInText.putAll(indexingService.getMapOfPositionWordsByPage().get(page.getId()));
            indexingService.fillingMap(page.getId(), mapWordAndFindInText);
        } else {
            indexingService.fillingMap(page.getId(), mapWordAndFindInText);
        }
    }

    private void addIndexToRepository() {
        for (Map.Entry<String, Integer> entry : lemmasMap.entrySet()) {
            Optional<Lemma> optionalLemma = indexingService.getLemmaRepository()
                    .findByLemma(entry.getKey())
                    .stream()
                    .filter(l -> l.getSiteId().getId() == site.getId())
                    .findFirst();
            if (optionalLemma.isPresent()) {
                Lemma lemma = optionalLemma.get();
                createIndexAndAddHimInRepository(lemma, entry.getValue());
            }
        }
    }

    private void addLemmaToMap(String lemma) {
        if (lemmasMap.containsKey(lemma)) {
            lemmasMap.put(lemma, lemmasMap.get(lemma) + 1);
        } else {
            lemmasMap.put(lemma, 1);
        }
    }

    private String getStringsOfURL(String uri) throws Exception {
        return uri.replaceAll("[^А-Яа-я]+", " ");
    }

    private void addLemmaToRepository(String lemma) {
        List<Lemma> hasLemma = indexingService.getLemmaRepository().findByLemma(lemma);
        Lemma lemmaEntity;
        Optional<Lemma> lemma1 = hasLemma.stream().filter(s -> s.getSiteId().getId() == site.getId()).findFirst();
        if (!hasLemma.isEmpty() && lemma1.isPresent()) {
            lemmaEntity = lemma1.get();
            int frequency = lemmaEntity.getFrequency();
            lemmaEntity.setFrequency(frequency + 1);
        } else {
            lemmaEntity = new Lemma();
            lemmaEntity.setFrequency(1);
            lemmaEntity.setSiteId(site);
            lemmaEntity.setLemma(lemma);
        }
        indexingService.getLemmaRepository().save(lemmaEntity);
    }

    public void updateLemmas(String content) throws Exception {
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
        String[] textForGetLemmas = getMassiveElementsFromContent(content);
        for (String word : textForGetLemmas) {
            if (!ApplicationConstantsAndChecks.checkWordByServiceForm(word, luceneMorphology)) {
                String wordBaseForm = luceneMorphology.getNormalForms(word.toLowerCase()).get(0).strip();
                addLemmaToMap(wordBaseForm);
                getFindWordsInPage(content,word);
            }
        }
        List<Index> indexesByPage = indexingService.getIndexLemmaRepository().findAllByPageId(page);
        for (Map.Entry<String, Integer> entry : lemmasMap.entrySet()) {
            List<Lemma> lemma = indexingService.getLemmaRepository().findByLemma(entry.getKey());
            if (!lemma.isEmpty()) {
                Optional<Index> index = indexesByPage.stream()
                        .filter(i -> i.getLemmaId().getId() == lemma.get(0).getId())
                        .findFirst();
                if (index.isPresent()) {
                    if (index.get().getRank() != entry.getValue()) {
                        index.get().setRank(entry.getValue());
                        indexingService.getIndexLemmaRepository().save(index.get());
                    }
                } else {
                    createIndexAndAddHimInRepository(lemma.get(0),entry.getValue());
                }
            } else {
                addLemmaToRepository(entry.getKey());
                Lemma lemmaFromRepository = indexingService.getLemmaRepository().findByLemma(entry.getKey()).get(0);
                createIndexAndAddHimInRepository(lemmaFromRepository,entry.getValue());
            }
        }
    }

    private String[] getMassiveElementsFromContent(String content) throws Exception {
        String textNew = getStringsOfURL(content);
        String regex = "[^а-яА-Я\\s]";
        String newtext = textNew.toLowerCase().replaceAll(regex, " ").strip();
        return newtext.split("\\s+");
    }

    private void createIndexAndAddHimInRepository (Lemma lemma, float rank){
        Index newIndex = new Index();
        newIndex.setPageId(page);
        newIndex.setLemmaId(lemma);
        newIndex.setRank(rank);
        indexingService.getIndexLemmaRepository().save(newIndex);
    }
}
