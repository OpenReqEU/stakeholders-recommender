package upc.stakeholdersrecommender.domain.Schemas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Class representing a stakeholder, only with ID.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonMinimal {
    @ApiModelProperty(notes = "Username of stakeholder.", example = "John Doe", required = true)
    private String username;
    private String name;

    public PersonMinimal() {

    }

    public PersonMinimal(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

        if (v instanceof PersonMinimal){
            PersonMinimal ptr = (PersonMinimal) v;
            retVal = ptr.getUsername().equals(this.getUsername());
        }

        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.getUsername() != null ? this.getUsername().hashCode() : 0);
        return hash;
    }
}
