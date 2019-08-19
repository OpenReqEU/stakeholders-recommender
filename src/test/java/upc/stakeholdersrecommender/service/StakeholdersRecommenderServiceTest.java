

package upc.stakeholdersrecommender.service;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

import upc.stakeholdersrecommender.domain.Schemas.BatchSchema;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import upc.stakeholdersrecommender.domain.Schemas.ProjectKeywordSchema;
import upc.stakeholdersrecommender.domain.Schemas.RecommendReturnSchema;
import upc.stakeholdersrecommender.domain.Schemas.RecommendSchema;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StakeholdersRecommenderServiceTest {
    @Autowired
    StakeholdersRecommenderService instance;

    @Test
    public void testRecommend() throws Exception {
        System.out.println("recommend");
        ObjectMapper mapper = new ObjectMapper();
        RecommendSchema req=new RecommendSchema();
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
        List<RecommendReturnSchema> result = instance.recommend(req, k, true,"UPC");
        String res = mapper.writeValueAsString(result);
        assertEquals(res, "[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"230\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.16666666666666666}]");
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
        List<RecommendReturnSchema> result = instance.recommend(req, k, false,"UPC");
        String res = mapper.writeValueAsString(result);
        assertEquals(res, "[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"230\"},\"availabilityScore\":1.0,\"appropiatenessScore\":0.16666666666666666}]");
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
        List<RecommendReturnSchema> result = instance.recommend(req, k, true,"UPC");
        String res = mapper.writeValueAsString(result);
        assertEquals(res, "[{\"requirement\":{\"id\":\"1\"},\"person\":{\"username\":\"230\"},\"availabilityScore\":0.97,\"appropiatenessScore\":0.16666666666666666}]");
    }


    @Test
    public void testRecommend_reject() throws Exception {
        System.out.println("recommend_reject");
        testAddBatch();
        instance.recommend_reject("230","230" , "1","UPC");
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
        List<RecommendReturnSchema> result = instance.recommend(req, k, true,"UPC");
        String res = mapper.writeValueAsString(result);
        assertEquals(res, "[]");
    }

    @Test
    public void testRecommend_rejectTwice() throws Exception {
        System.out.println("recommend_reject");
        testAddBatch();
        instance.recommend_reject("Dummy","230" , "1","UPC");
        instance.recommend_reject("230","230" , "1","UPC");
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
        List<RecommendReturnSchema> result = instance.recommend(req, k, true,"UPC");
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
        String organization="UPC";
        Integer result = instance.addBatch(bat, false,false,organization,false,false,false);
        Integer expected=5;
        assertEquals(result,expected);
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
        String organization="UPC";
        Integer result = instance.addBatch(bat, true,false,organization,true,false,false);
        Integer expected=5;
        assertEquals(result,expected);
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
        String organization="UPC";
        Integer result = instance.addBatch(bat, true,true,organization,true,false,false);
        Integer expected=5;
        assertEquals(result,expected);
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
        String organization="UPC";
        Integer result = instance.addBatch(bat, true,true,organization,true,false,false);
        Integer expected=5;
        assertEquals(result,expected);
        List<ProjectKeywordSchema> res=instance.extractKeywords("UPC",bat);
        assertTrue(res.get(0).getProjectId().equals("1"));
    }
}
