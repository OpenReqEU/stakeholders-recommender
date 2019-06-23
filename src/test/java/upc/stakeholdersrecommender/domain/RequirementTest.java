/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package upc.stakeholdersrecommender.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import upc.stakeholdersrecommender.entity.Skill;
@RunWith(SpringRunner.class)
@SpringBootTest
public class RequirementTest {


    @Test
    public void testGetId() {
        System.out.println("getId");
        Requirement instance = new Requirement();
        instance.setId("Id");
        String expResult = "Id";
        String result = instance.getId();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetId() {
        System.out.println("setId");
        String id = "id";
        Requirement instance = new Requirement();
        instance.setId(id);
        assertEquals("id", instance.getId());
    }

    @Test
    public void testGetSkills() {
        System.out.println("getSkills");
        Requirement instance = new Requirement();
        List<Skill> expResult = new ArrayList<>();
        List<Skill> result = instance.getSkills();
        assertEquals(expResult, result);
    }


    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        Requirement instance = new Requirement();
        instance.setDescription("desc");
        String expResult = "desc";
        String result = instance.getDescription();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        Requirement instance = new Requirement();
        instance.setDescription(description);
    }

    @Test
    public void testAddSkill() {
        System.out.println("addSkill");
        Skill auxiliar = new Skill("sk");
        Requirement instance = new Requirement();
        instance.addSkill(auxiliar);
        String s="";
        for (Skill sk:instance.getSkills()) {
            s=s+sk.getName();
        }
        assertEquals("sk", s);
    }

    @Test
    public void testGetEffort() {
        System.out.println("getEffort");
        Requirement instance = new Requirement();
        Double expResult = 1.0;
        instance.setEffort(1.0);
        Double result = instance.getEffort();
        assertEquals(expResult, result);
    }


    @Test
    public void testSetEffort() {
        System.out.println("setEffort");
        Double effort = 1.0;
        Requirement instance = new Requirement();
        instance.setEffort(effort);
        assertTrue(1.0==instance.getEffort());
    }


    @Test
    public void testGetModified_at() {
        System.out.println("getModified_at");
        Requirement instance = new Requirement();
        instance.setModified_at("122");
        String expResult = "122";
        String result = instance.getModified_at();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetModified_at() {
        System.out.println("setModified_at");
        String modified_at = "one";
        Requirement instance = new Requirement();
        instance.setModified_at(modified_at);
        assertEquals("one", instance.getModified_at());
    }


    @Test
    public void testGetModified() {
        System.out.println("getModified");
        Requirement instance = new Requirement();
        Date expResult = null;
        Date result = instance.getModified();
        assertEquals(expResult, result);
    }


    @Test
    public void testSetModified() {
        System.out.println("setModified");
        Date modified = null;
        Requirement instance = new Requirement();
        instance.setModified(modified);
        assertEquals(null, instance.getModified());

    }
    
}