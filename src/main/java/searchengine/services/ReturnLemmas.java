package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;


import java.util.*;

@RequiredArgsConstructor
public class ReturnLemmas {

    private final String[] SERVICE_FORMS = {"ПРЕДЛ", "СОЮЗ", "МЕЖД"};
    private final IndexingService indexingService;
    private final Site site;
    private final Page page;
    private final HashMap<String, Integer> lemmasMap = new HashMap<>();


    public void getLemmas(String text) throws Exception {
        String textNew = getStringsOfURL(text);
        String regex = "[^а-яА-Я\\s]";
        String newtext = textNew.toLowerCase().replaceAll(regex, " ").strip();
        String[] textForGetLemmas = newtext.split("\\s+");
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
        List<String> wordBaseForms = new ArrayList<>();
        for (String word : textForGetLemmas) {
            if (!checkWordByServiceForm(word, luceneMorphology)) {
                String wordBaseForm = luceneMorphology.getNormalForms(word.toLowerCase()).get(0).strip();
                if (!wordBaseForms.contains(wordBaseForm)) {
                    addLemmaToRepository(wordBaseForm);
                }
                wordBaseForms.add(wordBaseForm);
                addLemmaToMap(wordBaseForm);
            }
        }
        addIndexToRepository();
    }

    private void addIndexToRepository() {
        for (Map.Entry<String, Integer> entry : lemmasMap.entrySet()) {
            Lemma lemma = indexingService.getLemmaRepository().findByLemma(entry.getKey()).get(0);
            createIndexAndAddHimInRepository(lemma,entry.getValue());
        }
    }

    private void addLemmaToMap(String lemma) {
        if (lemmasMap.containsKey(lemma)) {
            lemmasMap.put(lemma, lemmasMap.get(lemma) + 1);
        } else {
            lemmasMap.put(lemma, 1);
        }
    }

    private boolean checkWordByServiceForm(String word, LuceneMorphology luceneMorphology) {
        for (String form : SERVICE_FORMS) {
            if (luceneMorphology.getMorphInfo(word).get(0).contains(form)) {
                return true;
            }
        }
        return false;
    }

    private String getStringsOfURL(String uri) throws Exception {
        return uri.replaceAll("[^А-Яа-я]+", " ");
    }

    private boolean hasLemmaInRepository(String lemma) {
        List<Lemma> hasLemma = indexingService.getLemmaRepository().findByLemma(lemma);
        return hasLemma.isEmpty();
    }

    private void addLemmaToRepository(String lemma) {
        Lemma lemmaEntity;
        if (!hasLemmaInRepository(lemma)) {
            lemmaEntity = indexingService.getLemmaRepository().findByLemma(lemma).get(0);
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
        String textNew = getStringsOfURL(content);
        String regex = "[^а-яА-Я\\s]";
        String newtext = textNew.toLowerCase().replaceAll(regex, " ").strip();
        String[] textForGetLemmas = newtext.split("\\s+");
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
        for (String word : textForGetLemmas) {
            if (!checkWordByServiceForm(word, luceneMorphology)) {
                String wordBaseForm = luceneMorphology.getNormalForms(word.toLowerCase()).get(0).strip();
                addLemmaToMap(wordBaseForm);
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

    private void createIndexAndAddHimInRepository (Lemma lemma, float rank){
        Index newIndex = new Index();
        newIndex.setPageId(page);
        newIndex.setLemmaId(lemma);
        newIndex.setRank(rank);
        indexingService.getIndexLemmaRepository().save(newIndex);
    }
}
