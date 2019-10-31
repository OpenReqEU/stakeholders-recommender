package upc.stakeholdersrecommender.repository;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import upc.stakeholdersrecommender.entity.RejectedPerson;
import upc.stakeholdersrecommender.entity.RejectedPersonId;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface RejectedPersonRepository extends JpaRepository<RejectedPerson, String> {
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    @Fetch(value = FetchMode.SELECT)
    RejectedPerson findByUser(RejectedPersonId id);

    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    @Transactional
    void deleteByOrganization(String organization);

    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    @Fetch(value = FetchMode.SELECT)
    List<RejectedPerson> findByOrganization(String organization);
}
