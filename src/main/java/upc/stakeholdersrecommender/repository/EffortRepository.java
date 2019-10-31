package upc.stakeholdersrecommender.repository;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import upc.stakeholdersrecommender.entity.Effort;
import upc.stakeholdersrecommender.entity.ProjectSRId;

import javax.persistence.QueryHint;

@Repository
public interface EffortRepository extends JpaRepository<Effort, String> {
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    @Fetch(value = FetchMode.SELECT)
    Effort findById(ProjectSRId id);

    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    @Transactional
    void deleteById(ProjectSRId id);


}
