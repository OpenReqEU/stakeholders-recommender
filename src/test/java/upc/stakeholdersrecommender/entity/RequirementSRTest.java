package upc.stakeholdersrecommender.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RequirementSRTest {

    @Test
    public void testGetId() {
        System.out.println("getID");
        RequirementSR instance = new RequirementSR();
        RequirementSRId expResult = null;
        RequirementSRId result = instance.getId();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetId() {
        System.out.println("setID");
        RequirementSRId id = new RequirementSRId("one", "two", "three");
        RequirementSR instance = new RequirementSR();
        instance.setId(id);
        assertEquals(new RequirementSRId("one", "two", "three"), instance.getId());
    }

    @Test
    public void testGetProjectIdQuery() {
        System.out.println("getProjectIdQuery");
        RequirementSR instance = new RequirementSR();
        instance.setProjectIdQuery("12");
        String expResult = "12";
        String result = instance.getProjectIdQuery();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetProjectIdQuery() {
        System.out.println("setProjectIdQuery");
        String projectIdQuery = "12";
        RequirementSR instance = new RequirementSR();
        instance.setProjectIdQuery(projectIdQuery);
        assertEquals("12", instance.getProjectIdQuery());
    }

    @Test
    public void testGetSkills() {
        System.out.println("getSkills");
        RequirementSR instance = new RequirementSR();
        List<String> expResult = null;
        List<String> result = instance.getSkills();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetSkills_ArrayList() {
        System.out.println("setSkills");
        ArrayList<String> skills = null;
        RequirementSR instance = new RequirementSR();
        instance.setSkills(skills);
        assertEquals(null, instance.getSkills());

    }

    @Test
    public void testSetSkills_List() {
        System.out.println("setSkills");
        List<String> skills = null;
        RequirementSR instance = new RequirementSR();
        instance.setSkills(skills);
        assertEquals(null, instance.getSkills());
    }

    @Test
    public void testGetModified_at() {
        System.out.println("getModified_at");
        RequirementSR instance = new RequirementSR();
        instance.setModified_at("11");
        String expResult = "11";
        String result = instance.getModified_at();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetModified_at() {
        System.out.println("setModified_at");
        String modified_at = "122";
        RequirementSR instance = new RequirementSR();
        instance.setModified_at(modified_at);
        assertEquals("122", instance.getModified_at());
    }

    @Test
    public void testSetProj() {
        System.out.println("setProj");
        String proj = "122";
        RequirementSR instance = new RequirementSR();
        instance.setProj(proj);
        assertEquals("122", instance.getProj());
    }

    @Test
    public void testSetOrganization() {
        System.out.println("setOrganization");
        String proj = "122";
        RequirementSR instance = new RequirementSR();
        instance.setOrganization(proj);
        assertEquals("122", instance.getOrganization());
    }

    @Test
    public void testCreateRequirementSRId() {
        System.out.println("createRequirementSRId");
        RequirementSRId id = new RequirementSRId();
        RequirementSR instance = new RequirementSR(id);
        assertEquals(id, instance.getId());
    }


}
