package upc.stakeholdersrecommender.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import upc.stakeholdersrecommender.domain.Schemas.PersonMinimal;

import java.io.Serializable;

@ApiModel(description = "Class representing the relation of a person working for a project, and the time this person has with the project.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Participant implements Serializable {

    @ApiModelProperty(notes = "Identifier of the project.", example = "\"1\"", required = true)
    private String project;
    @ApiModelProperty(notes = "Identifier of the person.", example = "John Doe", required = true)
    private String person;
    @ApiModelProperty(notes = "Hours the person has for this project, necessary if parameter withAvailability is true.", example = "40", required = true)
    private Double availability;

    public Participant() {
    }

    public Participant(String person, String project) {
        this.person = person;
        this.project = project;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public Double getAvailability() {
        return availability;
    }

    public void setAvailability(Double availability) {
        this.availability = availability;
    }

    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof Participant){
            Participant ptr = (Participant) v;
            retVal = ptr.getPerson().equals(this.getPerson()) && ptr.getProject().equals(this.getProject());
        }

        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.getPerson() != null ? this.getPerson().hashCode() : 0) +
                (this.getProject() != null ? this.getProject().hashCode() : 0);
        return hash;
    }
}
