package upc.stakeholdersrecommender.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ParticipantTest {


    @Test
    public void testCreate() {
        System.out.println("create");
        Participant instance = new Participant("1", "2");
        assertTrue(instance.getPerson().equals("1") && instance.getProject().equals("2"));
    }

}
