package upc.stakeholdersrecommender.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(description = "Class representing the relation between a requirement and a person.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Responsible implements Serializable {
    @ApiModelProperty(notes = "Identifier of the requirement.", example = "\"1\"", required = true)
    private String requirement;
    @ApiModelProperty(notes = "Identifier of the person.", example = "John Doe", required = true)
    private String person;

    public Responsible() {

    }

    public Responsible(String person, String requirement) {
        this.person = person;
        this.requirement = requirement;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirements) {
        this.requirement = requirements;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof Responsible){
            Responsible ptr = (Responsible) v;
            retVal = ptr.getPerson().equals(this.getPerson()) && ptr.getRequirement().equals(this.getRequirement());
        }

        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.getPerson() != null ? this.getPerson().hashCode() : 0) +
                (this.getRequirement() != null ? this.getRequirement().hashCode() : 0);
        return hash;
    }

}
