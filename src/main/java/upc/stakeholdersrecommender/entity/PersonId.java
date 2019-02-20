package upc.stakeholdersrecommender.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PersonId implements Serializable {

        @Column(name = "projectId")
        private Integer projectId;

        @Column(name = "personId")
        private String personId;

        public PersonId() {
        }

        public PersonId(Integer projectId, String personId) {
            this.projectId = projectId;
            this.personId = personId;
        }

        public Integer getprojectId() {
            return projectId;
        }

        public String getPersonId() {
            return personId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PersonId)) return false;
            PersonId that = (PersonId) o;
            return Objects.equals(getprojectId(), that.getprojectId()) &&
                    Objects.equals(getPersonId(), that.getPersonId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getprojectId(), getPersonId());
        }
    }

