package upc.stakeholdersrecommender.domain.keywords;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import upc.stakeholdersrecommender.domain.Requirement;
import upc.stakeholdersrecommender.domain.TextPreprocessing;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;


public class TFIDFKeywordExtractor {

    private Double cutoffParameter; //This can be set to different values for different selectivity (more or less keywords)
    private ConcurrentHashMap<String, Integer> corpusFrequency = new ConcurrentHashMap<>();
    private TextPreprocessing text_preprocess = new TextPreprocessing();
    private int batchSize=1000;

    public TFIDFKeywordExtractor(Double cutoff) {
        if (cutoff == -1.0) cutoffParameter = 4.0;
        else cutoffParameter = cutoff;
    }

    static Map<String, Map<String, Double>> getStringMapMap(List<Requirement> corpus, List<Map<String, Double>> res, int counter) {
        Map<String, Map<String, Double>> ret = new HashMap<>();
        for (Requirement r : corpus) {
            ret.put(r.getId(), res.get(counter));
            counter++;
        }
        return ret;
    }

    private Map<String, Integer> tf(List<String> doc) {
        Map<String, Integer> frequency = new HashMap<>();
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
        return Math.log(size.doubleValue() / frequency.doubleValue() + 1.0);
    }

    private List<String> analyze(String text, Analyzer analyzer) throws IOException {
        List<String> result = new ArrayList<>();
        text = clean_text(text);
        return RAKEKeywordExtractor.getAnalyzedStrings(text, analyzer, result);
    }

    private List<String> englishAnalyze(String text) throws IOException {
        Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer("standard")
                .addTokenFilter("lowercase")
                .addTokenFilter("stop")
                .addTokenFilter("kstem")
                .build();
        return analyze(text, analyzer);
    }

    public Map<String, Map<String, Double>> computeTFIDF(List<Requirement> corpus) throws IOException, ExecutionException, InterruptedException {
        ConcurrentHashMap<Integer, List<String>> concurrentMap = new ConcurrentHashMap<>();
        int batches = (corpus.size() / batchSize) + 1;
        ForkJoinPool commonPool=new ForkJoinPool(8);
        commonPool.commonPool().submit(()->
        IntStream.range(0, batches)
                .parallel().forEach(i -> {
            int n = i * batchSize;
            int max = batchSize;
            for (int l = 0; l < max; ++l) {
                int current = n + l;
                if (current >= corpus.size()) break;
                Requirement r = corpus.get(current);
                List<String> s = new ArrayList<>();
                try {
                    s = englishAnalyze(r.getDescription());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                concurrentMap.put(current, s);
            }
        })).get();
        List<List<String>> trueDocs = new ArrayList<>();
        for (int i = 0; i < concurrentMap.keySet().size(); ++i) {
            trueDocs.add(concurrentMap.get(i));
        }
        List<Map<String, Double>> res = tfIdf(trueDocs);
        int counter = 0;
        return getStringMapMap(corpus, res, counter);

    }

    public List<String> computeTFIDFSingular(Requirement req, Map<String, Integer> model, Integer corpusSize) throws IOException {
        List<String> doc = englishAnalyze(clean_text(req.getDescription()));
        Map<String, Integer> wordBag = tf(doc);
        List<String> keywords = new ArrayList<>();
        for (String s : wordBag.keySet()) {
            if (model.containsKey(s)) {
                model.put(s, model.get(s) + 1);
                if (wordBag.get(s) * idf(corpusSize, model.get(s)) >= cutoffParameter) keywords.add(s);
            } else {
                model.put(s, 1);
                if (wordBag.get(s) * idf(corpusSize, model.get(s)) >= cutoffParameter) keywords.add(s);
            }
        }
        return keywords;
    }

    public Map<String, Map<String, Double>> computeTFIDFExtra(Map<String, Integer> model, Integer size, Map<String, Requirement> trueRecs) throws IOException {
        Map<String, Map<String, Double>> result = new HashMap<>();
        for (String l : trueRecs.keySet()) {
            Requirement req = trueRecs.get(l);
            List<String> doc = englishAnalyze(clean_text(req.getDescription()));
            Map<String, Integer> wordBag = tf(doc);
            Map<String, Double> keywords = new HashMap<>();
            for (String s : wordBag.keySet()) {
                if (model.containsKey(s)) {
                    model.put(s, model.get(s) + 1);
                    if (wordBag.get(s) * idf(size, model.get(s)) >= cutoffParameter) keywords.put(s, 0.0);
                } else {
                    model.put(s, 1);
                    if (wordBag.get(s) * idf(size, model.get(s)) >= cutoffParameter) keywords.put(s, 0.0);
                }
            }
            result.put(l, keywords);
        }
        return result;

    }


    private List<Map<String, Double>> tfIdf(List<List<String>> docs) throws ExecutionException, InterruptedException {
        List<Map<String, Double>> tfidfComputed = new ArrayList<>();
        List<Map<String, Integer>> wordBag = new ArrayList<>();
        ConcurrentHashMap<Integer, Map<String, Double>> concurrentMap = new ConcurrentHashMap<>();
        int batches = (docs.size() / batchSize) + 1;
        ConcurrentHashMap<Integer, Map<String, Integer>> auxConcurrentMap = new ConcurrentHashMap<>();
        ForkJoinPool commonPool=new ForkJoinPool(8);
        commonPool.commonPool().submit(()->
                IntStream.range(0, batches)
                .parallel().forEach(i -> {
            int n = i * batchSize;
            int max = batchSize;
            for (int l = 0; l < max; ++l) {
                int current = n + l;
                if (current >= docs.size()) break;
                List<String> doc = docs.get(current);
                auxConcurrentMap.put(current, tf(doc));
            }
        })).get();
        for (int i = 0; i < auxConcurrentMap.keySet().size(); ++i) {
            wordBag.add(auxConcurrentMap.get(i));
        }
        commonPool.commonPool().submit(()->
                IntStream.range(0, batches)
                .parallel().forEach(i -> {
            int n = i * batchSize;
            int max = batchSize;
            for (int l = 0; l < max; ++l) {
                int current = n + l;
                if (current >= docs.size()) break;
                HashMap<String, Double> aux = new HashMap<>();
                List<String> doc = docs.get(current);
                for (String s : new TreeSet<>(doc)) {
                    Double idf = idf(docs.size(), corpusFrequency.get(s));
                    Integer tf = wordBag.get(current).get(s);
                    Double tfidf = idf * tf;
                    if (tfidf >= cutoffParameter && s.length() > 1) {
                        aux.put(s, tfidf);
                    }
                }
                concurrentMap.put(current, aux);
                ++i;
            }
        })).get();
        for (int i = 0; i < concurrentMap.keySet().size(); ++i) {
            tfidfComputed.add(concurrentMap.get(i));
        }
        return tfidfComputed;

    }

    private String clean_text(String text) throws IOException {
        text = text_preprocess.text_preprocess(text);
        String result = "";
        if (text.contains("[")) {
            String[] p = text.split("]\\[");
            for (String f : p) {
                if (f != null && f.length() > 0) {
                    if (f.charAt(0) != '[') f = "[" + f;
                    if (f.charAt(f.length() - 1) != ']') f = f.concat("]");
                    String[] thing = f.split("\\[");
                    if (thing.length > 1) {
                        String[] help = thing[1].split("]");
                        if (help.length > 0) {
                            String[] badIdea = help[0].split(" ");
                            String nice = "";
                            for (String s : badIdea) {
                                nice = nice.concat(s);
                            }
                            for (int i = 0; i < 10; ++i) {
                                result = result.concat(" " + nice);
                            }
                        }
                    }
                }
            }
        }
        String[] aux4 = text.split("]");
        String[] aux2 = aux4[aux4.length - 1].split(" ");
        for (String a : aux2) {
            String helper = "";
            if (a.toUpperCase().equals(a)) {
                for (int i = 0; i < 10; ++i) {
                    helper = helper.concat(" " + a);
                }
                a = helper;
            }
            result = result.concat(" " + a);
        }
        return result;
    }


    public ConcurrentHashMap<String, Integer> getCorpusFrequency() {
        return corpusFrequency;
    }

    public void setCorpusFrequency(ConcurrentHashMap<String, Integer> corpusFrequency) {
        this.corpusFrequency = corpusFrequency;
    }

    public Double getCutoffParameter() {
        return cutoffParameter;
    }

    public void setCutoffParameter(Double cutoffParameter) {
        this.cutoffParameter = cutoffParameter;
    }


}
