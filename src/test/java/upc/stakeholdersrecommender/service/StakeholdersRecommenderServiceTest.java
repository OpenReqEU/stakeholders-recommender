package upc.stakeholdersrecommender.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import upc.stakeholdersrecommender.domain.Schemas.BatchSchema;
import upc.stakeholdersrecommender.domain.Schemas.ProjectKeywordSchema;
import upc.stakeholdersrecommender.domain.Schemas.RecommendReturnSchema;
import upc.stakeholdersrecommender.domain.Schemas.RecommendSchema;
import upc.stakeholdersrecommender.entity.Skill;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.apache.xmlbeans.impl.common.Levenshtein.distance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StakeholdersRecommenderServiceTest {
    @Autowired
    StakeholdersRecommenderService instance;

    @Test
    public void testRecommend() throws Exception {
        System.out.println("recommend");
        ObjectMapper mapper = new ObjectMapper();
        RecommendSchema req = new RecommendSchema();
        testAddBatch();
        String jsonInString = "{\n" +
                "  \"project\": {\n" +
                "    \"id\": \"1\"\n" +
                "  },\n" +
                "  \"requirement\": {\n" +
                "    \"description\": \"This is not really a requirement, but an example\",\n" +
                "    \"effort\": \"3.0\",\n" +
                "    \"id\": \"1\",\n" +
                "    \"modified_at\": \"2014-01-13T15:14:17Z\",\n" +
                "    \"name\": \"This is a title\",\n" +
                "    \"requirementParts\": [\n" +
                "      {\n" +
                "        \"id\": \"3\",\n" +
                "        \"name\": \"UI\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"user\": {\n" +
                "    \"username\": \"John Doe\"\n" +
                "  }\n" +
                "}";
        req = mapper.readValue(jsonInString, RecommendSchema.class);

        int k = 10;
        List<RecommendReturnSchema> result = instance.recommend(req, k, true, "UPC",1);
        String res = mapper.writeValueAsString(result);
        assertEquals("[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"230\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.42774495717678834}]",res);
    }

    @Test
    public void testNotProjectSpecific() throws Exception {
        System.out.println("recommend");
        ObjectMapper mapper = new ObjectMapper();
        RecommendSchema req;
        testAddBatch();
        String jsonInString = "{\n" +
                "  \"project\": {\n" +
                "    \"id\": \"1\"\n" +
                "  },\n" +
                "  \"requirement\": {\n" +
                "    \"description\": \"This is not really a requirement, but an example\",\n" +
                "    \"effort\": \"3.0\",\n" +
                "    \"id\": \"1\",\n" +
                "    \"modified_at\": \"2014-01-13T15:14:17Z\",\n" +
                "    \"name\": \"This is a title\",\n" +
                "    \"requirementParts\": [\n" +
                "      {\n" +
                "        \"id\": \"3\",\n" +
                "        \"name\": \"UI\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"user\": {\n" +
                "    \"username\": \"John Doe\"\n" +
                "  }\n" +
                "}";
        req = mapper.readValue(jsonInString, RecommendSchema.class);

        int k = 10;
        List<RecommendReturnSchema> result = instance.recommend(req, k, false, "UPC",1);
        String res = mapper.writeValueAsString(result);
        assertEquals("[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"230\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.42774495717678834}]",res);
    }

    @Test
    public void testRecommendAvailability() throws Exception {
        System.out.println("recommend");
        ObjectMapper mapper = new ObjectMapper();
        RecommendSchema req;
        testAddBatchAvailabilityAutoMappingComponent();
        String jsonInString = "{\n" +
                "  \"project\": {\n" +
                "    \"id\": \"1\"\n" +
                "  },\n" +
                "  \"requirement\": {\n" +
                "    \"description\": \"This is not really a requirement, but an example\",\n" +
                "    \"effort\": \"3.0\",\n" +
                "    \"id\": \"1\",\n" +
                "    \"modified_at\": \"2014-01-13T15:14:17Z\",\n" +
                "    \"name\": \"This is a title\",\n" +
                "    \"requirementParts\": [\n" +
                "      {\n" +
                "        \"id\": \"3\",\n" +
                "        \"name\": \"UI\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"user\": {\n" +
                "    \"username\": \"John Doe\"\n" +
                "  }\n" +
                "}";
        req = mapper.readValue(jsonInString, RecommendSchema.class);

        int k = 10;
        List<RecommendReturnSchema> result = instance.recommend(req, k, true, "UPC",1);
        String res = mapper.writeValueAsString(result);
        assertEquals("[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"230\"},\"availabilityScore\":0.97,\"appropiatenessScore\":0.42774495717678834}]",res);
    }


    @Test
    public void testRecommend_reject() throws Exception {
        System.out.println("recommend_reject");
        testAddBatch();
        instance.recommend_reject("230", "230", "1", "UPC");
        ObjectMapper mapper = new ObjectMapper();
        RecommendSchema req;
        String jsonInString = "{\n" +
                "  \"project\": {\n" +
                "    \"id\": \"1\"\n" +
                "  },\n" +
                "  \"requirement\": {\n" +
                "    \"description\": \"This is not really a requirement, but an example\",\n" +
                "    \"effort\": \"3.0\",\n" +
                "    \"id\": \"1\",\n" +
                "    \"modified_at\": \"2014-01-13T15:14:17Z\",\n" +
                "    \"name\": \"This is a title\",\n" +
                "    \"requirementParts\": [\n" +
                "      {\n" +
                "        \"id\": \"3\",\n" +
                "        \"name\": \"UI\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"user\": {\n" +
                "    \"username\": \"230\"\n" +
                "  }\n" +
                "}";
        req = mapper.readValue(jsonInString, RecommendSchema.class);

        int k = 10;
        List<RecommendReturnSchema> result = instance.recommend(req, k, true, "UPC",1);
        String res = mapper.writeValueAsString(result);
        assertEquals(res, "[]");
    }
    @Test
    public void testUndo_Recommend_reject() throws Exception {
        System.out.println("recommend_reject");
        testAddBatch();
        testRecommend_reject();
        instance.undo_recommend_reject("230", "230", "1", "UPC");
        ObjectMapper mapper = new ObjectMapper();
        RecommendSchema req;
        String jsonInString = "{\n" +
                "  \"project\": {\n" +
                "    \"id\": \"1\"\n" +
                "  },\n" +
                "  \"requirement\": {\n" +
                "    \"description\": \"This is not really a requirement, but an example\",\n" +
                "    \"effort\": \"3.0\",\n" +
                "    \"id\": \"1\",\n" +
                "    \"modified_at\": \"2014-01-13T15:14:17Z\",\n" +
                "    \"name\": \"This is a title\",\n" +
                "    \"requirementParts\": [\n" +
                "      {\n" +
                "        \"id\": \"3\",\n" +
                "        \"name\": \"UI\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"user\": {\n" +
                "    \"username\": \"230\"\n" +
                "  }\n" +
                "}";
        req = mapper.readValue(jsonInString, RecommendSchema.class);

        int k = 10;
        List<RecommendReturnSchema> result = instance.recommend(req, k, true, "UPC",1);
        String res = mapper.writeValueAsString(result);
        assertEquals("[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"230\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.42774495717678834}]",res);
    }

    @Test
    public void testGetSkills() throws Exception {
        System.out.println("recommend_reject");
        testAddBatch();
        ObjectMapper mapper=new ObjectMapper();
        int k = 10;
        List<Skill> result = instance.getPersonSkills("230","UPC",k);
        String res = mapper.writeValueAsString(result);
        assertEquals("[{\"name\":\"requirement\",\"weight\":0.5},{\"name\":\"title\",\"weight\":0.5}]",res);
    }



    @Test
    public void testRecommend_rejectTwice() throws Exception {
        System.out.println("recommend_reject");
        testAddBatch();
        instance.recommend_reject("Dummy", "230", "1", "UPC");
        instance.recommend_reject("230", "230", "1", "UPC");
        ObjectMapper mapper = new ObjectMapper();
        RecommendSchema req;
        String jsonInString = "{\n" +
                "  \"project\": {\n" +
                "    \"id\": \"1\"\n" +
                "  },\n" +
                "  \"requirement\": {\n" +
                "    \"description\": \"This is not really a requirement, but an example\",\n" +
                "    \"effort\": \"3.0\",\n" +
                "    \"id\": \"1\",\n" +
                "    \"modified_at\": \"2014-01-13T15:14:17Z\",\n" +
                "    \"name\": \"This is a title\",\n" +
                "    \"requirementParts\": [\n" +
                "      {\n" +
                "        \"id\": \"3\",\n" +
                "        \"name\": \"UI\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"user\": {\n" +
                "    \"username\": \"230\"\n" +
                "  }\n" +
                "}";
        req = mapper.readValue(jsonInString, RecommendSchema.class);

        int k = 10;
        List<RecommendReturnSchema> result = instance.recommend(req, k, true, "UPC",1);
        String res = mapper.writeValueAsString(result);
        assertEquals(res, "[]");
    }


    @Test
    public void testAddBatch() throws Exception {
        System.out.println("addBatch");
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "{\n" +
                "    \"projects\": [\n" +
                "        {\n" +
                "            \"id\": \"1\",\n" +
                "            \"specifiedRequirements\": [\"1\"]\n" +
                "        }\n" +
                "    ],\n" +
                "    \"persons\": [\n" +
                "        {\n" +
                "            \"username\": \"230\"\n" +
                "        }\n" +
                "        ],\n" +
                "    \"responsibles\": [\n" +
                "        {\n" +
                "            \"requirement\": \"1\",\n" +
                "            \"person\": \"230\"\n" +
                "        }\n" +
                "        ],\n" +
                "    \"participants\": [\n" +
                "    \t        {\n" +
                "            \"project\": \"1\",\n" +
                "            \"person\": \"230\",\n" +
                "            \"availability\": \"100\"\n" +
                "        }\n" +
                "    \t],\n" +
                "  \"requirements\": [{\n" +
                "    \"description\": \"This is not really a requirement, but an example\",\n" +
                "    \"effort\": \"3.0\",\n" +
                "    \"id\": \"1\",\n" +
                "    \"modified_at\": \"2014-01-13T15:14:17Z\",\n" +
                "    \"name\": \"This is a title\",\n" +
                "    \"requirementParts\": [\n" +
                "      {\n" +
                "        \"id\": \"3\",\n" +
                "        \"name\": \"UI\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "  ]\n" +
                "    \t\n" +
                "}\n" +
                "    \t";
        BatchSchema bat = mapper.readValue(jsonInString, BatchSchema.class);
        String organization = "UPC";
        Integer result = instance.addBatch(bat, false, false, organization, false, false, false,1);
        Integer expected = 5;
        assertEquals(result, expected);
    }

    @Test
    public void testAddBatchAvailabilityAutoMapping() throws Exception {
        System.out.println("addBatch");
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "{\n" +
                "    \"projects\": [\n" +
                "        {\n" +
                "            \"id\": \"1\",\n" +
                "            \"specifiedRequirements\": [\"1\"]\n" +
                "        }\n" +
                "    ],\n" +
                "    \"persons\": [\n" +
                "        {\n" +
                "            \"username\": \"230\"\n" +
                "        }\n" +
                "        ],\n" +
                "    \"responsibles\": [\n" +
                "        {\n" +
                "            \"requirement\": \"1\",\n" +
                "            \"person\": \"230\"\n" +
                "        }\n" +
                "        ],\n" +
                "    \"participants\": [\n" +
                "    \t        {\n" +
                "            \"project\": \"1\",\n" +
                "            \"person\": \"230\",\n" +
                "            \"availability\": \"100\"\n" +
                "        }\n" +
                "    \t],\n" +
                "  \"requirements\": [{\n" +
                "    \"description\": \"This is not really a requirement, but an example\",\n" +
                "    \"effort\": \"3.0\",\n" +
                "    \"id\": \"1\",\n" +
                "    \"modified_at\": \"2014-01-13T15:14:17Z\",\n" +
                "    \"name\": \"This is a title\",\n" +
                "    \"requirementParts\": [\n" +
                "      {\n" +
                "        \"id\": \"3\",\n" +
                "        \"name\": \"UI\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "  ]\n" +
                "    \t\n" +
                "}\n" +
                "    \t";
        BatchSchema bat = mapper.readValue(jsonInString, BatchSchema.class);
        String organization = "UPC";
        Integer result = instance.addBatch(bat, true, false, organization, true, false, false,1);
        Integer expected = 5;
        assertEquals(result, expected);
    }

    @Test
    public void testAddBatchAvailabilityAutoMappingComponent() throws Exception {
        System.out.println("addBatch");
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "{\n" +
                "    \"projects\": [\n" +
                "        {\n" +
                "            \"id\": \"1\",\n" +
                "            \"specifiedRequirements\": [\"1\"]\n" +
                "        }\n" +
                "    ],\n" +
                "    \"persons\": [\n" +
                "        {\n" +
                "            \"username\": \"230\"\n" +
                "        }\n" +
                "        ],\n" +
                "    \"responsibles\": [\n" +
                "        {\n" +
                "            \"requirement\": \"1\",\n" +
                "            \"person\": \"230\"\n" +
                "        }\n" +
                "        ],\n" +
                "    \"participants\": [\n" +
                "    \t        {\n" +
                "            \"project\": \"1\",\n" +
                "            \"person\": \"230\",\n" +
                "            \"availability\": \"100\"\n" +
                "        }\n" +
                "    \t],\n" +
                "  \"requirements\": [{\n" +
                "    \"description\": \"This is not really a requirement, but an example\",\n" +
                "    \"effort\": \"3.0\",\n" +
                "    \"id\": \"1\",\n" +
                "    \"modified_at\": \"2014-01-13T15:14:17Z\",\n" +
                "    \"name\": \"This is a title\",\n" +
                "    \"requirementParts\": [\n" +
                "      {\n" +
                "        \"id\": \"3\",\n" +
                "        \"name\": \"UI\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "  ]\n" +
                "    \t\n" +
                "}\n" +
                "    \t";
        BatchSchema bat = mapper.readValue(jsonInString, BatchSchema.class);
        String organization = "UPC";
        Integer result = instance.addBatch(bat, true, true, organization, true, false, false,1);
        Integer expected = 5;
        assertEquals(result, expected);
    }

    @Test
    public void testAddBatchAvailabilityPreprocessing() throws Exception {
        System.out.println("addBatch");
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/main/resources/testingFiles/BatchTest.txt");
        String jsonInString= FileUtils.readFileToString(file, StandardCharsets.US_ASCII);
        BatchSchema bat = mapper.readValue(jsonInString, BatchSchema.class);
        String organization = "UPC";
        Integer result = instance.addBatch(bat, true, true, organization, true, true, true,2);
        Integer expected = 22213;
        assertEquals(result, expected);
    }

    @Test
    public void testAddBatchAvailabilityTfIdfLogging() throws Exception {
        System.out.println("addBatch");
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/main/resources/testingFiles/BatchTest.txt");
        String jsonInString= FileUtils.readFileToString(file, StandardCharsets.US_ASCII);
        BatchSchema bat = mapper.readValue(jsonInString, BatchSchema.class);
        String organization = "UPC";
        Integer result = instance.addBatch(bat, true, true, organization, true, false, true,1);
        Integer expected = 22213;
        assertEquals(result, expected);
    }

    @Test
    public void testRecommendTfIdf() throws Exception {
        System.out.println("recommend");
        ObjectMapper mapper = new ObjectMapper();
        RecommendSchema req;
        testAddBatchAvailabilityTfIdfLogging();
        File file = new File("src/main/resources/testingFiles/RecommendTest.txt");
        String jsonInString= FileUtils.readFileToString(file, StandardCharsets.US_ASCII);
        req = mapper.readValue(jsonInString, RecommendSchema.class);

        int k = 10;
        List<RecommendReturnSchema> result = instance.recommend(req, k, true, "UPC",1);
        String res = mapper.writeValueAsString(result);
        String s="[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"254\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.13429365752426303},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"22\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.13204162824085602},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"1\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.12882151600388306},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"260\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.08603980783086922},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"2\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.06751206120688939},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"15\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.060057074318516424},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"245\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.05705376748365999},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"257\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.05340446815471452},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"47\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.04820298580437775},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"223\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.04596896140205564}]";
        Double dist=(double) distance(s,res);
        Double percentage =dist/res.length();
        assertTrue(percentage<0.1);
    }

    @Test
    public void testRecommendPreprocessingLogging() throws Exception {
        System.out.println("recommend");
        ObjectMapper mapper = new ObjectMapper();
        RecommendSchema req;
        testAddBatchAvailabilityPreprocessing();
        File file = new File("src/main/resources/testingFiles/RecommendTest.txt");
        String jsonInString= FileUtils.readFileToString(file, StandardCharsets.US_ASCII);
        req = mapper.readValue(jsonInString, RecommendSchema.class);

        int k = 10;
        List<RecommendReturnSchema> result = instance.recommend(req, k, true, "UPC",2);
        String res = mapper.writeValueAsString(result);
        String s="[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"1\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.21995032496806483},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"22\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.13649867479870212},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"2\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.1233462287241922},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"15\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.08653215914689405},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"142\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.08501582589473772},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"25\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.07087741754784019},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"38\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.07022368744732053},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"212\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.057475521818266775},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"65\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.05574629878795652},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"214\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.05201326946766942}]\n";
        Double dist=(double) distance(s,res);
        Double percentage =dist/res.length();
        System.out.println(res);
        assertTrue(percentage<0.1);
    }

    @Test
    public void testRecommendLeftovers() throws Exception {
        System.out.println("recommend");
        ObjectMapper mapper = new ObjectMapper();
        RecommendSchema req;
        File file = new File("src/main/resources/testingFiles/BatchTestLeftover.txt");
        String jsonInString= FileUtils.readFileToString(file, StandardCharsets.US_ASCII);
        BatchSchema bat = mapper.readValue(jsonInString, BatchSchema.class);
        String organization = "UPC";
        instance.addBatch(bat, true, true, organization, true, true, false,0);
        file = new File("src/main/resources/testingFiles/RecommendTest.txt");
        jsonInString= FileUtils.readFileToString(file, StandardCharsets.US_ASCII);
        req = mapper.readValue(jsonInString, RecommendSchema.class);
        int k = 10;
        List<RecommendReturnSchema> result = instance.recommend(req, k, true, "UPC",2);
        String res = mapper.writeValueAsString(result);
        String s="[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"1\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.21995032496806483},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"22\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.13649867479870212},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"2\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.1233462287241922},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"15\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.08653215914689405},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"142\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.08501582589473772},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"25\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.07087741754784019},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"38\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.07022321870041932},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"212\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.05745384225327756},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"65\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.05574629878795652},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"214\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.05201326946766942}]\n";
        Double dist=(double) distance(s,res);
        Double percentage =dist/res.length();
        System.out.println(res);
        assertTrue(percentage<0.1);
    }


    @Test
    public void testRecommendPreprocessing() throws Exception {
        System.out.println("recommend");
        ObjectMapper mapper = new ObjectMapper();
        RecommendSchema req;
        testAddBatchAvailabilityPreprocessing();
        File file = new File("src/main/resources/testingFiles/RecommendTest.txt");
        String jsonInString= FileUtils.readFileToString(file, StandardCharsets.US_ASCII);
        req = mapper.readValue(jsonInString, RecommendSchema.class);

        int k = 10;
        List<RecommendReturnSchema> result = instance.recommend(req, k, true, "UPC",1);
        String res = mapper.writeValueAsString(result);
        String s="[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"22\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.13971077173798735},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"1\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.13679338569143035},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"15\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.07360075154597227},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"2\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.06999829304655297},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"212\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.045740563813790984},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"117\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.04464753998839412},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"47\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.04425330148288506},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"38\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.04362902765076417},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"33\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.04347882557014783},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"104\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.04334282886870377}]";
        Double dist=(double) distance(s,res);
        Double percentage =dist/res.length();
        assertTrue(percentage<0.1);
    }

    @Test
    public void testKeyword() throws Exception {
        System.out.println("addBatch");
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "{\n" +
                "    \"projects\": [\n" +
                "        {\n" +
                "            \"id\": \"1\",\n" +
                "            \"specifiedRequirements\": [\"1\"]\n" +
                "        }\n" +
                "    ],\n" +
                "    \"persons\": [\n" +
                "        {\n" +
                "            \"username\": \"230\"\n" +
                "        }\n" +
                "        ],\n" +
                "    \"responsibles\": [\n" +
                "        {\n" +
                "            \"requirement\": \"1\",\n" +
                "            \"person\": \"230\"\n" +
                "        }\n" +
                "        ],\n" +
                "    \"participants\": [\n" +
                "    \t        {\n" +
                "            \"project\": \"1\",\n" +
                "            \"person\": \"230\",\n" +
                "            \"availability\": \"100\"\n" +
                "        }\n" +
                "    \t],\n" +
                "  \"requirements\": [{\n" +
                "    \"description\": \"This is not really a requirement, but an example\",\n" +
                "    \"effort\": \"3.0\",\n" +
                "    \"id\": \"1\",\n" +
                "    \"modified_at\": \"2014-01-13T15:14:17Z\",\n" +
                "    \"name\": \"This is a title\",\n" +
                "    \"requirementParts\": [\n" +
                "      {\n" +
                "        \"id\": \"3\",\n" +
                "        \"name\": \"UI\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "  ]\n" +
                "    \t\n" +
                "}\n" +
                "    \t";
        BatchSchema bat = mapper.readValue(jsonInString, BatchSchema.class);
        String organization = "UPC";
        Integer result = instance.addBatch(bat, true, true, organization, true, false, false,0);
        Integer expected = 5;
        assertEquals(result, expected);
        List<ProjectKeywordSchema> res = instance.extractKeywords("UPC", bat);
        assertTrue(res.get(0).getProjectId().equals("1"));
    }


}
