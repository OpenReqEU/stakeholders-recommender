package upc.stakeholdersrecommender.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;

@Entity
@Table(name = "rejected")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RejectedPerson implements Serializable {

    @EmbeddedId
    private RejectedPersonId user;
    @ElementCollection
    private Map<String, HashSet<String>> deleted;
    @Column(length = 300)
    private String organization;

    public RejectedPerson() {
    }

    public RejectedPerson(RejectedPersonId user) {
        this.user = user;
        this.organization = user.getOrganizationId();
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public RejectedPersonId getUser() {
        return user;
    }

    public void setUser(RejectedPersonId user) {
        this.user = user;
    }

    public Map<String, HashSet<String>> getDeleted() {
        return deleted;
    }

    public void setDeleted(Map<String, HashSet<String>> deleted) {
        this.deleted = deleted;
    }

}
