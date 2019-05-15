package upc.stakeholdersrecommender.domain.keywords;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import upc.stakeholdersrecommender.domain.Requirement;
import upc.stakeholdersrecommender.domain.Schemas.RequirementDocument;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static java.lang.StrictMath.sqrt;

public class TFIDFKeywordExtractor {

    Double cutoffParameter = 4.0; //This can be set to different values for different selectivity values
    Map<String, Map<String, Double>> model;
    private Map<String, Integer> corpusFrequency = new HashMap<String, Integer>();

    public Map<String, Integer> getCorpusFrequency() {
        return corpusFrequency;
    }

    public void setCorpusFrequency(Map<String, Integer> corpusFrequency) {
        this.corpusFrequency = corpusFrequency;
    }

    public Double getCutoffParameter() {
        return cutoffParameter;
    }

    public void setCutoffParameter(Double cutoffParameter) {
        this.cutoffParameter = cutoffParameter;
    }

    public Map<String, Map<String, Double>> getModel() {
        return model;
    }

    public void setModel(Map<String, Map<String, Double>> model) {
        this.model = model;
    }

    private Map<String, Integer> tf(List<String> doc) {
        Map<String, Integer> frequency = new HashMap<String, Integer>();
        for (String s : doc) {
            if (frequency.containsKey(s)) frequency.put(s, frequency.get(s) + 1);
            else {
                frequency.put(s, 1);
                if (corpusFrequency.containsKey(s)) corpusFrequency.put(s, corpusFrequency.get(s) + 1);
                else corpusFrequency.put(s, 1);
            }

        }
        return frequency;
    }


    private double idf(Integer size, Integer frequency) {
        return Math.log(size / frequency + 1);
    }


    private List<String> analyze(String text, Analyzer analyzer) throws IOException {
        List<String> result = new ArrayList<String>();
        text=clean_text(text);
        TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(text));
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            result.add(attr.toString());
        }
        return result;
    }

    private List<String> englishAnalyze(String text) throws IOException {
        Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer("standard")
                .addTokenFilter("lowercase")
                .addTokenFilter("porterstem")
                .addTokenFilter("stop")
                .build();
        return analyze(text, analyzer);
    }

    public Map<String,Map<String, Double>> computeTFIDF(Collection<Requirement> corpus) throws IOException {
        List<List<String>> docs = new ArrayList<List<String>>();
        for (Requirement r : corpus) {
            docs.add(englishAnalyze(r.getDescription()));
        }
        List<Map<String, Double>> res = tfIdf(docs);
        int counter=0;
        Map<String,Map<String, Double>> ret=new HashMap<String,Map<String, Double>>();
        for (Requirement r : corpus) {
            ret.put(r.getId(),res.get(counter));
            counter++;
        }
        return ret;

    }

    private List<Map<String, Double>> tfIdf(List<List<String>> docs) {
        List<Map<String, Double>> tfidfComputed = new ArrayList<Map<String, Double>>();
        List<Map<String, Integer>> wordBag = new ArrayList<Map<String, Integer>>();
        for (List<String> doc : docs) {
            wordBag.add(tf(doc));
        }
        Integer i = 0;
        for (List<String> doc : docs) {
            HashMap<String, Double> aux = new HashMap<String, Double>();
            for (String s : doc) {
                Double idf = idf(docs.size(), corpusFrequency.get(s));
                Integer tf = wordBag.get(i).get(s);
                Double tfidf = idf * tf;
                if (tfidf >= cutoffParameter && s.length()>1) aux.put(s, tfidf);
            }
            tfidfComputed.add(aux);
            ++i;
        }
        return tfidfComputed;

    }

    public double cosineSimilarity(Map<String,Double> wordsA,Map<String,Double> wordsB) {
        Double cosine=0.0;
        Set<String> intersection= new HashSet<String>(wordsA.keySet());
        intersection.retainAll(wordsB.keySet());
        for (String s: intersection) {
            Double forA=wordsA.get(s);
            Double forB=wordsB.get(s);
            cosine+=forA*forB;
        }
        Double normA=norm(wordsA);
        Double normB=norm(wordsB);

        cosine=cosine/(normA*normB);
        return cosine;
    }



    private Double norm(Map<String, Double> wordsB) {
        Double norm=0.0;
        for (String s:wordsB.keySet()) {
            norm+=wordsB.get(s)*wordsB.get(s);
        }
        Double result=sqrt(norm);
        return result;
    }

    private String clean_text(String text) {
            text = text.replaceAll("(\\{.*?})"," code ");
            text=text.replaceAll("[.$,;\\\"/:|!?=%,()><_0-9\\-\\[\\]{}']", " ");
            String[] aux2=text.split(" ");
            String result="";
            for (String a:aux2) {
                if (a.length()>1) {
                    result=result.concat(" "+a);
                }
            }
        return result;
     }

    public Map<String,Map<String, Double>> computeTFIDF(List<RequirementDocument> corpus) throws IOException {
        List<List<String>> docs = new ArrayList<List<String>>();
        for (RequirementDocument r : corpus) {
            docs.add(englishAnalyze(r.getDescription()));
        }
        List<Map<String, Double>> res = tfIdf(docs);
        int counter=0;
        Map<String,Map<String, Double>> ret=new HashMap<String,Map<String, Double>>();
        for (RequirementDocument r : corpus) {
            ret.put(r.getId(),res.get(counter));
            counter++;
        }
        return ret;

    }
}
