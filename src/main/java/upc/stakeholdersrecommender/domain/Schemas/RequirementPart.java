package upc.stakeholdersrecommender.domain.Schemas;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

public class RequirementPart implements Serializable {
    @ApiModelProperty(notes = "The ID of the requirement part", example = "\"3\"", required = true)
    String id;
    @ApiModelProperty(notes = "The name of the requirement part", example = "\"UI\"", required = true)
    String name;

    public RequirementPart () {

    }

    public RequirementPart(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
