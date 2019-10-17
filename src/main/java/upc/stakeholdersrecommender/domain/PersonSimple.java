package upc.stakeholdersrecommender.domain;

import upc.stakeholdersrecommender.entity.PersonSR;
import upc.stakeholdersrecommender.entity.Skill;

import java.util.List;

public class PersonSimple {
    Double availability;
    Double hours;
    String name;
    private List<Skill> skills;
    private List<Skill> components;

    public PersonSimple(PersonSR per) {
        this.skills = per.getSkills();
        this.components = per.getComponents();
        this.availability = per.getAvailability();
        this.hours = per.getHours();
        this.name = per.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAvailability() {
        return availability;
    }

    public void setAvailability(Double availability) {
        this.availability = availability;
    }

    public Double getHours() {
        return hours;
    }

    public void setHours(Double hours) {
        this.hours = hours;
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
}
