package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    List<Lemma> findByLemma(String word);

    @Transactional
    List<Lemma> deleteBySiteId(Site s);

    List<Lemma> findAllBySiteId(Site site);
}
