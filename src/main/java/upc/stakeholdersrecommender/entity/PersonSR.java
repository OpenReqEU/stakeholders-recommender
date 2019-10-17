package upc.stakeholdersrecommender.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "personSR",
        indexes = {@Index(name = "name_index", columnList = "name"), @Index(name = "project_index", columnList = "projectIdQuery")
                , @Index(name = "organization_index_person", columnList = "organization")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PersonSR implements Serializable {

    @EmbeddedId
    private PersonSRId id;
    @Column(length = 300)
    private String projectIdQuery;

    private Double availability;

    private Double hours;
    @Column(length = 300)
    private String name;
    @Column(length = 300)
    private String organization;

    @ElementCollection
    @Embedded
    private List<Skill> skills;
    @ElementCollection
    @Embedded
    private List<Skill> components;

    public PersonSR() {

    }


    public PersonSR(PersonSRId id, String projectIdQuer, Double availability, List<Skill> skills, String org) {
        this.id = id;
        this.projectIdQuery = projectIdQuer;
        this.availability = availability;
        this.name = id.getPersonId();
        this.skills = skills;
        this.organization = org;
    }

    public PersonSR(PersonSRId id) {
        this.id = id;
        this.name = id.getPersonId();

    }

    public PersonSR(PersonSR per) {
        this.id = per.getId();
        this.name = per.getName();
        this.skills=per.getSkills();
        this.projectIdQuery=per.getProjectIdQuery();
        this.organization=per.getOrganization();
        this.availability=per.getAvailability();
        this.hours=per.getHours();
        this.components=per.getComponents();


    }

    public PersonSR(String s, List<Skill> skills, Double hoursDefault, String newId, String organization, List<Skill> components, double v, PersonSRId personSRId) {
        this.name = s;
        this.skills = skills;
        this.hours = hoursDefault;
        this.projectIdQuery = newId;
        this.organization = organization;
        this.components = components;
        this.availability = v;
        this.id = personSRId;
    }

    public PersonSRId getId() {
        return id;
    }

    public void setId(PersonSRId id) {
        this.id = id;
    }

    public Double getAvailability() {
        return availability;
    }

    public void setAvailability(Double availability) {
        this.availability = availability;
    }

    public String getProjectIdQuery() {
        return projectIdQuery;
    }

    public void setProjectIdQuery(String projectId) {
        this.projectIdQuery = projectId;
    }

    public Double getHours() {
        return hours;
    }

    public void setHours(Double hours) {
        this.hours = hours;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Skill> getComponents() {
        return components;
    }

    public void setComponents(List<Skill> components) {
        this.components = components;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public PersonSR deepCopy() {
        PersonSR per=new PersonSR(this.name, this.skills, this.hours, this.projectIdQuery, this.organization, this.components, this.availability, this.id);
        return per;
    }
}
