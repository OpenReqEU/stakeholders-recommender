package upc.stakeholdersrecommender.domain.Schemas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import upc.stakeholdersrecommender.domain.Participant;
import upc.stakeholdersrecommender.domain.Project;
import upc.stakeholdersrecommender.domain.Requirement;
import upc.stakeholdersrecommender.domain.Responsible;

import java.io.Serializable;
import java.util.List;

@ApiModel(description = "Class representing the information needed for the recommendation of stakeholders.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchSchema implements Serializable {
    @ApiModelProperty(notes = "List of projects.", required = true)
    private List<Project> projects;
    @ApiModelProperty(notes = "List of stakeholders.", required = true)
    private List<PersonMinimal> persons;
    @ApiModelProperty(notes = "List of responsibles.", required = true)
    private List<Responsible> responsibles;
    @ApiModelProperty(notes = "List of requirements.", required = true)
    private List<Requirement> requirements;
    @ApiModelProperty(notes = "List of participants.", required = true)
    private List<Participant> participants;

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<PersonMinimal> getPersons() {
        return persons;
    }

    public void setPersons(List<PersonMinimal> persons) {
        this.persons = persons;
    }

    public List<Responsible> getResponsibles() {
        return responsibles;
    }

    public void setResponsibles(List<Responsible> responsibles) {
        this.responsibles = responsibles;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }
}
