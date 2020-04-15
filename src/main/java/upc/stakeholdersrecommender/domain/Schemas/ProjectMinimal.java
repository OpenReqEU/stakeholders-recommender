package upc.stakeholdersrecommender.domain.Schemas;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Class representing a project, only with ID.")
public class ProjectMinimal {
    @ApiModelProperty(notes = "Identifier of project.", example = "\"1\"", required = true)
    private String id;

    public ProjectMinimal() {

    }

    public ProjectMinimal(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
