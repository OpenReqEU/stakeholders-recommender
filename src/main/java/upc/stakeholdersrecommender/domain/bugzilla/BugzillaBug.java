package upc.stakeholdersrecommender.domain.bugzilla;

import java.io.Serializable;
import java.util.List;

public class BugzillaBug implements Serializable {

    String assigned_to;
    String id;
    String status;
    String summary;

    public String getAssigned_to() {
        return assigned_to;
    }

    public void setAssigned_to(String assigned_to) {
        this.assigned_to = assigned_to;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}