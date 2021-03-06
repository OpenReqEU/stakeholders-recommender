package upc.stakeholdersrecommender.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProjectSRId implements Serializable {

    @Column(name = "projectId", length = 300)
    private String projectId;

    @Column(name = "organizationId", length = 300)
    private String organizationId;


    public ProjectSRId() {
    }

    public ProjectSRId(String projectId, String organizationId) {
        this.projectId = projectId;
        this.organizationId = organizationId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectSRId)) return false;
        ProjectSRId that = (ProjectSRId) o;
        return Objects.equals(getProjectId(), that.getProjectId()) &&
                Objects.equals(getOrganizationId(), that.getOrganizationId());
    }


    @Override
    public int hashCode() {
        return Objects.hash(getProjectId(), getOrganizationId());
    }


}
