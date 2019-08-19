package upc.stakeholdersrecommender.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import upc.stakeholdersrecommender.entity.PersonSR;
import upc.stakeholdersrecommender.entity.PersonSRId;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface PersonSRRepository extends JpaRepository<PersonSR, String> {
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    List<PersonSR> findByProjectIdQueryAndOrganization(String projectId, String organization);

    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    PersonSR findById(PersonSRId id);

    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    @Transactional
    void deleteByOrganization(String organization);

    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    List<PersonSR> findByNameAndOrganization(String name, String organization);

    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    List<PersonSR> findByOrganization(String organization);

}
