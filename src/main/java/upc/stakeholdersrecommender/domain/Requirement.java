package upc.stakeholdersrecommender.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import upc.stakeholdersrecommender.domain.Schemas.PersonMinimal;
import upc.stakeholdersrecommender.domain.Schemas.RequirementPart;
import upc.stakeholdersrecommender.entity.Skill;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(description = "Class representing a requirement.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Requirement implements Serializable {
    @ApiModelProperty(notes = "Identifier of the requirement.", example = "\"1\"", required = true)
    private String id;
    @ApiModelProperty(notes = "How much effort the requirement will take. It is not required if using the parameter withAvailability as false, or using autoMapping", example = "\"3.0\"", required = false)
    private Double effort;
    @ApiModelProperty(notes = "The requirement parts of the requirement", required = false)
    private List<RequirementPart> requirementParts;
    @ApiModelProperty(notes = "The title of the requirement", example = "This is a title", required = true)
    private String name;

    @JsonIgnore
    private List<Skill> skills = new ArrayList<>();

    @JsonIgnore
    private Date modified;
    @ApiModelProperty(notes = "The requirement's description.", example = "This is not really a requirement, but an example", required = true)
    private String description;
    @ApiModelProperty(notes = "When was the requirement last modified.", example = "2014-01-13T15:14:17Z", required = false)
    private String modified_at;

    public Requirement() {

    }

    public Requirement(String id, Double effort, List<RequirementPart> requirementParts, String name, List<Skill> skills, Date modified, String description, String modified_at) {
        this.id = id;
        this.effort = effort;
        this.requirementParts = requirementParts;
        this.name = name;
        this.skills = skills;
        this.modified = modified;
        this.description = description;
        this.modified_at = modified_at;
    }

    public Requirement(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addSkill(Skill auxiliar) {
        this.skills.add(auxiliar);
    }

    public Double getEffort() {
        return effort;
    }

    public void setEffort(Double effort) {
        this.effort = effort;
    }

    public String getModified_at() {
        return modified_at;
    }

    public void setModified_at(String modified_at) {
        this.modified_at = modified_at;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public List<RequirementPart> getRequirementParts() {
        return requirementParts;
    }

    public void setRequirementParts(List<RequirementPart> requirementParts) {
        this.requirementParts = requirementParts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof Requirement){
            Requirement ptr = (Requirement) v;
            retVal = ptr.getId().equals(this.getId());
        }

        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.getId() != null ? this.getId().hashCode() : 0);
        return hash;
    }
}
