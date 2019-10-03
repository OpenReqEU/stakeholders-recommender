package upc.stakeholdersrecommender.domain;


import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.sqrt;

@Service
public class WordEmbedding {

    Map<String, Double[]> model = null;

    public Double computeSimilarity(String a, String b) throws IOException {
        if (model == null) {
            Path p = Paths.get("GloVe_model/glove.6B.50d.txt");
            String h = new String(Files.readAllBytes(p));
            loadModel(h);
        }
        Double[] help1 = null, help2 = null;
        if (model.containsKey(a)) help1 = model.get(a);
        if (model.containsKey(b)) help2 = model.get(b);
        if (help1 != null && help2 != null) {
            return cosineSimilarity(help1, help2);
        } else return -1.0;
    }

    private Double cosineSimilarity(Double[] help1, Double[] help2) {
        Double sum = 0.0;
        for (int i = 0; i < help1.length; ++i) {
            sum += help1[i] * help2[i];
        }
        return sum / (norm(help1) * norm(help2));
    }

    private Double norm(Double[] array) {
        Double tot = 0.0;
        for (Double d : array) {
            tot += d * d;
        }
        return sqrt(tot);
    }

    private void loadModel(String h) {
        Map<String, Double[]> map = new HashMap<>();
        String[] help = h.split("\n");
        for (String a : help) {
            String[] l = a.split(" ");
            Double[] aux = new Double[l.length - 1];
            for (int i = 1; i < l.length; ++i) {
                aux[i - 1] = Double.parseDouble(l[i]);
            }
            map.put(l[0], aux);
        }
        model = map;
    }

}
