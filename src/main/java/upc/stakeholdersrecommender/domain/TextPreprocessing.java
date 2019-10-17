package upc.stakeholdersrecommender.domain;

import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.MaxSizeConcurrentHashSet;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TextPreprocessing {

    ConcurrentHashMap<String, Integer> exclusions = null;

    public String text_preprocess(String text) throws IOException {
        String trueRes = "";
        if (text != null) {
            text = text.replaceAll("(\\{.*?})", " code ");
            text = text.replaceAll("[$,;\\\"/:|!?=()><_{}'+%[0-9]]", " ");
            text = text.replaceAll("] \\[", "][");

            if (exclusions == null) {
                BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/ExcludedWords.txt"));
                String word = null;
                exclusions = new ConcurrentHashMap<>();

                while ((word = reader.readLine()) != null) {
                    exclusions.put(word,0);
                }
                reader.close();
            }
            for (String l : text.split(" ")) {
                if (!(l.toLowerCase().equals("null") && !l.equals("null") && !l.equals("Null")) && !l.toUpperCase().equals(l)) l = l.toLowerCase();
                if (l != null && !exclusions.keySet().contains(l) && l.length() > 1) {
                    trueRes = trueRes.concat(l + " ");
                }
            }
        }
        return trueRes;

    }


}
