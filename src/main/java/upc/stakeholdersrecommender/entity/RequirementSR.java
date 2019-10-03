package upc.stakeholdersrecommender.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import upc.stakeholdersrecommender.domain.Requirement;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "requirementSR",
        indexes = {@Index(name = "organization_Index_requirement", columnList = "organization")}
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RequirementSR implements Serializable {

    @ElementCollection
    List<String> component;
    @ElementCollection
    List<String> skills;
    @EmbeddedId
    private RequirementSRId id;
    @Column(length = 300)
    private String projectIdQuery;
    @Column(length = 300)
    private String modified_at;
    @Column(length = 300)
    private String organization;
    @Column(length = 300)
    private String proj;

    public RequirementSR() {

    }

    public RequirementSR(RequirementSRId id) {
        this.id = id;
    }

    public RequirementSR(Requirement req, String id, String org) {
        this.id = new RequirementSRId(id, req.getId(), org);
        this.projectIdQuery = req.getId();
        this.organization = org;
        this.proj = id;
    }

    public RequirementSRId getId() {
        return id;
    }

    public void setId(RequirementSRId id) {
        this.id = id;
    }

    public String getProjectIdQuery() {
        return projectIdQuery;
    }

    public void setProjectIdQuery(String projectIdQuery) {
        this.projectIdQuery = projectIdQuery;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(ArrayList<String> skills) {
        this.skills = skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public Set<String> getSkillsSet() {
        Set ret = new HashSet<>(skills);
        return ret;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getModified_at() {
        return modified_at;
    }

    public void setModified_at(String modified_at) {
        this.modified_at = modified_at;
    }

    public List<String> getComponent() {
        return component;
    }

    public void setComponent(List<String> component) {
        this.component = component;
    }

    public String getProj() {
        return proj;
    }

    public void setProj(String proj) {
        this.proj = proj;
    }
}
