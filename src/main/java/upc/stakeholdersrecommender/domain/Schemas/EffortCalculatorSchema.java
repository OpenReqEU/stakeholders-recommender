package upc.stakeholdersrecommender.domain.Schemas;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import upc.stakeholdersrecommender.domain.Requirement;

import java.io.Serializable;
import java.util.List;

// Use requirement, using effort and hours
// effort_points   // hours
@ApiModel(description = "Class representing the requirements with their effort points and hours.")
public class EffortCalculatorSchema implements Serializable {
    @ApiModelProperty(notes = "List of requirements with their effort points and hours.", required = true)
    private List<RequirementBasic> requirements;

    public List<RequirementBasic> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<RequirementBasic> requirements) {
        this.requirements = requirements;
    }
}
