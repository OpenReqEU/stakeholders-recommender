package upc.stakeholdersrecommender.domain.keywords;

import com.linguistic.rake.Rake;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import upc.stakeholdersrecommender.domain.Requirement;
import upc.stakeholdersrecommender.domain.TextPreprocessing;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class RAKEKeywordExtractor {

    private Double cutoff = 3.0;
    private TextPreprocessing preprocess = new TextPreprocessing();

    public static List<String> getAnalyzedStrings(String text, Analyzer analyzer, List<String> result) throws IOException {
        TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(text));
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            result.add(attr.toString());
        }
        return result;
    }

    public List<Map<String, Double>> extractKeywords(List<String> corpus) throws IOException {
        List<Map<String, Double>> res = new ArrayList<>();
        Rake rake = new Rake();
        for (String s : corpus) {
            String text = "";
            for (String k : RAKEanalyzeNoStopword(s)) {
                text = text + " " + k;
            }
            Map<String, Double> aux = rake.getKeywordsFromText(text);
            String sum = "";
            for (String j : aux.keySet()) {
                Double val = aux.get(j);
                if (val >= cutoff) sum = sum + " " + j;
            }
            List<String> result = RAKEanalyze(sum);
            Map<String, Double> helper = new HashMap<>();
            for (String i : result) {
                helper.put(i, aux.get(i));
            }
            res.add(helper);
        }
        return res;
    }

    public List<String> computeTFIDFSingular(Requirement req) throws IOException {
        Rake rake = new Rake();
        String text = "";
        for (String k : RAKEanalyzeNoStopword(req.getDescription())) {
            text = text + " " + k;
        }
        Map<String, Double> aux = rake.getKeywordsFromText(text);
        text = "";
        for (String l : aux.keySet()) {
            Double val = aux.get(l);
            if (val >= cutoff) ;
            text = text + " " + l;
        }
        return RAKEanalyze(text);
    }

    public Map<String, Map<String, Double>> computeRake(List<Requirement> corpus) throws IOException {
        List<String> docs = new ArrayList<>();
        for (Requirement r : corpus) {
            docs.add(r.getDescription());
        }
        List<Map<String, Double>> res = extractKeywords(docs);
        int counter = 0;
        return TFIDFKeywordExtractor.getStringMapMap(corpus, res, counter);
    }

    List<String> RAKEanalyze(String text) throws IOException {
        text = preprocess.text_preprocess(text);
        Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer("standard")
                .addTokenFilter("lowercase")
                .addTokenFilter("stop")
                .addTokenFilter("kstem")
                .build();
        return analyze(text, analyzer);
    }

    List<String> RAKEanalyzeNoStopword(String text) throws IOException {
        Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer("standard")
                .addTokenFilter("lowercase")
                .addTokenFilter("kstem")
                .build();
        return analyze(text, analyzer);
    }

    List<String> analyze(String text, Analyzer analyzer) throws IOException {
        List<String> result = new ArrayList<>();
        return getAnalyzedStrings(text, analyzer, result);
    }

}
