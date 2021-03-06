package upc.stakeholdersrecommender.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProjectSRTest {

    @Test
    public void testGetId() {
        System.out.println("getId");
        ProjectSR instance = new ProjectSR();
        instance.setId(new ProjectSRId("one", "two"));
        ProjectSRId expResult = new ProjectSRId("one", "two");
        ProjectSRId result = instance.getId();
        assertTrue(expResult.getOrganizationId().equals(result.getOrganizationId()) &&
                expResult.getProjectId().equals(result.getProjectId()));
    }

    @Test
    public void testSetId() {
        System.out.println("setId");
        ProjectSR instance = new ProjectSR();
        instance.setId(new ProjectSRId("one", "two"));
        ProjectSRId expResult = new ProjectSRId("one", "two");
        ProjectSRId result = instance.getId();
        assertTrue(expResult.getOrganizationId().equals(result.getOrganizationId()) &&
                expResult.getProjectId().equals(result.getProjectId()));
    }


    @Test
    public void testGetParticipants() {
        System.out.println("getParticipants");
        ProjectSR instance = new ProjectSR();
        List<String> expResult = null;
        List<String> result = instance.getParticipants();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetParticipants() {
        System.out.println("setParticipants");
        List<String> participants = null;
        ProjectSR instance = new ProjectSR();
        instance.setParticipants(participants);
        assertEquals(null, instance.getParticipants());
    }

}
