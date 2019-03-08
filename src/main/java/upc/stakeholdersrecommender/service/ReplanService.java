package upc.stakeholdersrecommender.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import upc.stakeholdersrecommender.domain.Person;
import upc.stakeholdersrecommender.domain.Project;
import upc.stakeholdersrecommender.domain.Requirement;
import upc.stakeholdersrecommender.domain.Skill;
import upc.stakeholdersrecommender.domain.replan.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReplanService {

    @Value("${replan.client.serviceUrl}")
    private String replanUrl;

    private RestTemplate restTemplate = new RestTemplate();

    public Project getProject(String id) {
        ResponseEntity<Project> response = restTemplate.exchange(
                replanUrl + "/projects/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Project>() {
                });
        Project project = response.getBody();
        return project;
    }

    public ProjectReplan createProject(Project p) {
        ProjectReplan projectReplan = new ProjectReplan(p);

        ProjectReplan createdPlan = restTemplate.postForObject(
                replanUrl + "/projects",
                projectReplan,
                ProjectReplan.class);

        return createdPlan;
    }

    public ResourceReplan createResource(Person p, String projectId) {
        ResourceReplan resourceReplan = new ResourceReplan(p);

        ResourceReplan createdResource = restTemplate.postForObject(
                replanUrl + "/projects/" + projectId + "/resources",
                resourceReplan,
                ResourceReplan.class);

        return createdResource;
    }

    public FeatureReplan createRequirement(String requirement, String projectId) {
        FeatureReplan featureReplan = new FeatureReplan(requirement);

        FeatureReplan createdFeature = restTemplate.postForObject(
                replanUrl + "/projects/" + projectId + "/features/create_one",
                featureReplan,
                FeatureReplan.class);

        return createdFeature;
    }

    public void deleteProject(String id) {
        restTemplate.delete(replanUrl + "/projects/" + id);
    }

    public SkillReplan createSkill(Skill s, String projectId) {
        SkillReplan skillReplan = new SkillReplan(s);

        SkillReplan createdReplan = restTemplate.postForObject(
                replanUrl + "/projects/" + projectId + "/skills",
                skillReplan,
                SkillReplan.class);

        return createdReplan;
    }

    public void addSkillsToPerson(String projectReplanId, Integer personId, List<SkillListReplan> skillListReplans) {
        restTemplate.postForObject(
                replanUrl + "/projects/" + projectReplanId + "/resources/" + personId + "/skills",
                skillListReplans,
                Object.class);

    }

    public void addSkillsToRequirement(String projectReplanId, Integer reqId, List<SkillListReplan> skillListReplans) {
        restTemplate.postForObject(
                replanUrl + "/projects/" + projectReplanId + "/features/" + reqId + "/skills",
                skillListReplans,
                Object.class);

    }

    public ReleaseReplan createRelease(String id) {
        ReleaseReplan releaseReplan = new ReleaseReplan("Release");
        ReleaseReplan createdRelease = restTemplate.postForObject(
                replanUrl + "/projects/" + id + "/releases",
                releaseReplan,
                ReleaseReplan.class);
        return createdRelease;
    }

    public void addResourcesToRelease(String projectReplanId, Integer releaseReplanId, List<ResourceListReplan> resourceReplanList) {
        restTemplate.postForObject(
                replanUrl + "/projects/" + projectReplanId + "/releases/" + releaseReplanId + "/resources",
                resourceReplanList,
                Object.class);
    }

    public void addFeaturesToRelease(String projectReplanId, Integer releaseReplanId, FeatureListReplan featureReplanList) {
        List<FeatureListReplan> skills = new ArrayList<FeatureListReplan>();
        skills.add(featureReplanList);
        restTemplate.postForObject(
                replanUrl + "/projects/" + projectReplanId + "/releases/" + releaseReplanId + "/features",
                skills,
                Object.class);
    }

    public Plan[] plan(String projectReplanId, Integer releaseReplanId) {
        return restTemplate.postForObject(
                replanUrl + "/projects/" + projectReplanId + "/releases/" + releaseReplanId + "/plan?multiple_solutions=true",
                null, Plan[].class);
    }

    public void deleteRelease(String projectReplanId, Integer releaseReplanId) {
        restTemplate.delete(replanUrl + "/projects/" + projectReplanId + "/releases/" + releaseReplanId);
    }

    public void modifyResource(Person person, Integer id_replan, Integer id) {
    }

    public void modifyRequirement(Requirement requirement, Integer id_replan, Integer id) {
    }

    public void modifyProject(Project p, Integer id_replan) {
    }
}
