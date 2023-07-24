package searchengine.services;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

public interface LemmaRepository extends JpaRepository<Lemma,Integer> {

    Optional<Lemma> findByLemma(String lemma);

    @Transactional
    List<Lemma> deleteBySiteId(Site s);
}
