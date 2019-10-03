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
        String s="[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"22\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.9049269898249501},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"1\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.850930547652002},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"254\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.786627404925508},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"89\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.5716151704339995},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"250\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.40895714390292853},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"2\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.40287690121543335},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"193\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.384119648896422},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"15\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.3635126172626096},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"245\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.35010464351057746},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"83\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.3462136082947244}]\n";
        Double dist=(double) distance(s,res);
        Double percentage =dist/res.length();
        System.out.println(res);
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
        String s="[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"1\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.9436916164438838},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"22\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.582605645400728},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"2\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.5290132472725831},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"15\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.3704632051789171},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"142\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.36509883405276544},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"25\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.30441918025590664},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"38\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.30161140327708996},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"212\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.24308899850588908},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"65\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.2394308817456018},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"214\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.21949165326216402}]\n";
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
        instance.addBatch(bat, true, true, organization, true, true, false,2);
        file = new File("src/main/resources/testingFiles/RecommendTest.txt");
        jsonInString= FileUtils.readFileToString(file, StandardCharsets.US_ASCII);
        req = mapper.readValue(jsonInString, RecommendSchema.class);
        int k = 10;
        List<RecommendReturnSchema> result = instance.recommend(req, k, true, "UPC",2);
        String res = mapper.writeValueAsString(result);
        String s="[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"1\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.9436916164438838},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"22\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.5775272325947214},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"2\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.5285663134250639},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"15\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.3689914357118085},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"142\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.3656419799267084},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"25\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.30493007982501685},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"38\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.30211759061996185},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"212\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.24009682113064823},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"65\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.23983271297782793},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"79\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.21440676407562864}]\n";
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
        List<RecommendReturnSchema> result = instance.recommend(req, k, true, "UPC",2);
        String res = mapper.writeValueAsString(result);
        String s="[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"1\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.9436916164438838},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"22\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.5775272325947214},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"2\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.5285663134250639},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"15\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.3689914357118085},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"142\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.3656419799267084},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"25\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.30493007982501685},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"38\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.30211759061996185},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"212\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.24009682113064823},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"65\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.23983271297782793},{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"79\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.21440676407562864}]\n";
        Double dist=(double) distance(s,res);
        Double percentage =dist/res.length();
        System.out.println(res);
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
