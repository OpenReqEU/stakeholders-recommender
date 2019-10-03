package upc.stakeholdersrecommender.domain.Schemas;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Class representing a requirement with its keywords.")
public class KeywordReturnSchema {
    @ApiModelProperty(notes = "ID of the requirement.", example = "1", required = true)
    private String requirement;
    @ApiModelProperty(notes = "Set of skills of the requirement.", example = "[ubuntu,UI,linux]", required = true)
    private List<String> skills;

    public KeywordReturnSchema(String requirementId, ArrayList<String> strings) {
        this.requirement=requirementId;
        this.skills=strings;
    }

    public KeywordReturnSchema() {
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

}
