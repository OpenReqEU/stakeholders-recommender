package upc.stakeholdersrecommender.domain;

import edu.stanford.nlp.util.ArraySet;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

@Service
public class TextPreprocessing {

    Set<String> exclusions=null;

    public String text_preprocess(String text) throws IOException {
        String trueRes="";
        if (text!=null) {
            text = text.replaceAll("(\\{.*?})", " code ");
            text = text.replaceAll("[$,;\\\"/:|!?=()><_{}'+%[0-9]]", " ");
            text = text.replaceAll("] \\[", "][");

            if (exclusions == null) {
                BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/ExcludedWords.txt"));
                String word = null;
                exclusions = new ArraySet<>();

                while ((word = reader.readLine()) != null) {
                    exclusions.add(word);
                }
                reader.close();
            }
            for (String l : text.split(" ")) {
                if (!(l.toLowerCase().equals("null") && !l.equals("null") && !l.equals("Null")) && !l.toUpperCase().equals(l))  l = l.toLowerCase();
                if (l != null && !exclusions.contains(l) && l.length() > 1) {
                    trueRes = trueRes.concat(l + " ");
                }
            }
        }
        return trueRes;

    }


}
