package upc.stakeholdersrecommender.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import upc.stakeholdersrecommender.domain.keywords.TFIDFKeywordExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TfIdfTest {


    @Test
    public void computeTFIDFTest() throws IOException, ExecutionException, InterruptedException {
        TFIDFKeywordExtractor keywordExtractor = new TFIDFKeywordExtractor(-1.0);
        String object = "[{\"id\":\"509913\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"PropertyEditingSupport does not check if IPropertySource is null\",\"modified_at\":\"2017-01-04T09:58:57Z\"}," +
                "{\"id\":\"509920\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"[Platform] Save As with a E4 Part\",\"modified_at\":\"2017-01-04T11:30:00Z\"}," +
                "{\"id\":\"510041\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"No (quick menu) key binding shown for menuContribution/menu/@commandId\",\"modified_at\":\"2017-01-19T03:50:02Z\"}," +
                "{\"id\":\"510057\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"Change hover color for GTK3 (currently it's balck for most themes)\",\"modified_at\":\"2017-02-17T00:21:25Z\"}," +
                "{\"id\":\"510146\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"Adding splash to e4 app causes main menu to not respond\",\"modified_at\":\"2017-06-06T14:47:41Z\"}," +
                "{\"id\":\"510158\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"Toolbar/Menubar enablement/visibility (controlled by source provider and property tester) works across all the open workbench windows\",\"modified_at\":\"2017-01-10T08:10:45Z\"}," +
                "{\"id\":\"510160\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"org.eclipse.ui.internal.progress.ProgressManager does properly clean up Job without a workbench\",\"modified_at\":\"2017-05-26T13:20:29Z\"}]";
        ObjectMapper map = new ObjectMapper();
        Requirement[] recs = map.readValue(object, Requirement[].class);
        List<Requirement> reqList = new ArrayList<>();
        for (Requirement r : recs) {
            reqList.add(r);
        }
        Map<String, Map<String, Double>> res = keywordExtractor.computeTFIDF(reqList);
        System.out.println(map.writeValueAsString(res));
        String result = map.writeValueAsString(res);
        assertEquals("{\"510041\":{},\"510146\":{},\"510057\":{\"gtk\":20.79441541679836},\"509920\":{\"platform\":22.873856958478193},\"510158\":{},\"509913\":{},\"510160\":{}}", result);
    }

    @Test
    public void computeTFIDFSingular() throws IOException, ExecutionException, InterruptedException {
        TFIDFKeywordExtractor keywordExtractor = new TFIDFKeywordExtractor(-1.0);
        String object = "[{\"id\":\"509913\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"PropertyEditingSupport does not check if IPropertySource is null\",\"modified_at\":\"2017-01-04T09:58:57Z\"}," +
                "{\"id\":\"509920\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"[Platform] Save As with a E4 Part\",\"modified_at\":\"2017-01-04T11:30:00Z\"}," +
                "{\"id\":\"510041\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"No (quick menu) key binding shown for menuContribution/menu/@commandId\",\"modified_at\":\"2017-01-19T03:50:02Z\"}," +
                "{\"id\":\"510057\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"Change hover color for GTK3 (currently it's balck for most themes)\",\"modified_at\":\"2017-02-17T00:21:25Z\"}," +
                "{\"id\":\"510146\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"Adding splash to e4 app causes main menu to not respond\",\"modified_at\":\"2017-06-06T14:47:41Z\"}," +
                "{\"id\":\"510158\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"Toolbar/Menubar enablement/visibility (controlled by source provider and property tester) works across all the open workbench windows\",\"modified_at\":\"2017-01-10T08:10:45Z\"}," +
                "{\"id\":\"510160\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"org.eclipse.ui.internal.progress.ProgressManager does properly clean up Job without a workbench\",\"modified_at\":\"2017-05-26T13:20:29Z\"}]";
        ObjectMapper map = new ObjectMapper();
        Requirement[] recs = map.readValue(object, Requirement[].class);
        List<Requirement> reqList = new ArrayList<>();
        for (Requirement r : recs) {
            reqList.add(r);
        }
        keywordExtractor.computeTFIDF(reqList);

        HashMap<String, Integer> model = keywordExtractor.getCorpusFrequency();
        TypeReference<HashMap<String, Integer>> typeRef
                = new TypeReference<HashMap<String, Integer>>() {
        };
        Requirement req = new Requirement();
        req.setName("I am a requirement");
        req.setDescription("[gtk] And this is a big description, platform");
        req.setId("1");
        List<String> res = keywordExtractor.computeTFIDFSingular(req, model, 7);
        assertEquals("[\"gtk\"]", map.writeValueAsString(res));

    }

    @Test
    public void computeTFIDFExtra() throws IOException, ExecutionException, InterruptedException {
        TFIDFKeywordExtractor keywordExtractor = new TFIDFKeywordExtractor(-1.0);
        String object = "[{\"id\":\"509913\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"PropertyEditingSupport does not check if IPropertySource is null\",\"modified_at\":\"2017-01-04T09:58:57Z\"}," +
                "{\"id\":\"509920\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"[Platform] Save As with a E4 Part\",\"modified_at\":\"2017-01-04T11:30:00Z\"}," +
                "{\"id\":\"510041\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"No (quick menu) key binding shown for menuContribution/menu/@commandId\",\"modified_at\":\"2017-01-19T03:50:02Z\"}," +
                "{\"id\":\"510057\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"Change hover color for GTK3 (currently it's balck for most themes)\",\"modified_at\":\"2017-02-17T00:21:25Z\"}," +
                "{\"id\":\"510146\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"Adding splash to e4 app causes main menu to not respond\",\"modified_at\":\"2017-06-06T14:47:41Z\"}," +
                "{\"id\":\"510158\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"Toolbar/Menubar enablement/visibility (controlled by source provider and property tester) works across all the open workbench windows\",\"modified_at\":\"2017-01-10T08:10:45Z\"}," +
                "{\"id\":\"510160\",\"effort\":1.0,\"requirementParts\":[{\"id\":\"1\",\"name\":\"UI\"}],\"name\":null,\"description\":\"org.eclipse.ui.internal.progress.ProgressManager does properly clean up Job without a workbench\",\"modified_at\":\"2017-05-26T13:20:29Z\"}]";
        ObjectMapper map = new ObjectMapper();
        Requirement[] recs = map.readValue(object, Requirement[].class);
        List<Requirement> reqList = new ArrayList<>();
        for (Requirement r : recs) {
            reqList.add(r);
        }
        keywordExtractor.computeTFIDF(reqList);

        HashMap<String, Integer> model = keywordExtractor.getCorpusFrequency();
        Map<String, Requirement> mappy = new HashMap<>();
        Requirement req = new Requirement();
        req.setName("I am a requirement");
        req.setDescription("[gtk] And this is a big description, platform");
        req.setId("1");
        mappy.put("1", req);
        Map<String, Map<String, Double>> res = keywordExtractor.computeTFIDFExtra(model, 7, mappy);
        assertEquals("{\"1\":{\"gtk\":0.0}}", map.writeValueAsString(res));

    }


}
