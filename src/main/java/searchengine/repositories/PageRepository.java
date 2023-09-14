package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    @Transactional
    List<Page> deleteBySiteId(Site site);

    Optional<Page> findByPath(String path);

    List<Page> findAllBySiteId(Site site);


}
