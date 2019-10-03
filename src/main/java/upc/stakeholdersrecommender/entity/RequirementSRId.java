package upc.stakeholdersrecommender.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RequirementSRId implements Serializable {

    @Column(name = "projectId", length = 300)
    private String projectId;

    @Column(name = "requirementId", length = 300)
    private String requirementId;
    @Column(name = "organizationId", length = 300)
    private String organizationId;


    public RequirementSRId() {
    }

    public RequirementSRId(String projectId, String personId, String org) {
        this.projectId = projectId;
        this.requirementId = personId;
        this.organizationId = org;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getprojectId() {
        return projectId;
    }

    public String getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(String requirementId) {
        this.requirementId = requirementId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequirementSRId)) return false;
        RequirementSRId that = (RequirementSRId) o;
        return Objects.equals(getprojectId(), that.getprojectId()) &&
                Objects.equals(getRequirementId(), that.getRequirementId()) &&
                Objects.equals(getOrganizationId(), that.getOrganizationId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getprojectId(), getRequirementId(), getOrganizationId());
    }
}

