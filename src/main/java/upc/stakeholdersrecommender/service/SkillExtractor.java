package upc.stakeholdersrecommender.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import upc.stakeholdersrecommender.domain.Preprocess.PreprocessService;
import upc.stakeholdersrecommender.domain.Requirement;
import upc.stakeholdersrecommender.domain.keywords.RAKEKeywordExtractor;
import upc.stakeholdersrecommender.domain.keywords.TFIDFKeywordExtractor;
import upc.stakeholdersrecommender.entity.KeywordExtractionModel;
import upc.stakeholdersrecommender.repository.KeywordExtractionModelRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;

@Service
public class SkillExtractor {

    @Value("${skill.dropoff.days}")
    private Double dropoffDays;
    @Value("${skill.dropoff.max}")
    private Double maxDropoff;
    @Value("${skill.dropoff.days.unconsider}")
    private Double daysToUnconsider;


    @Autowired
    private KeywordExtractionModelRepository KeywordExtractionModelRepository;
    @Autowired
    private PreprocessService Preprocess;

    public Map<String, Map<String, Double>> obtainSkills(Map<String, Requirement> trueRecs, Boolean bugzilla, Boolean rake, String organization, Integer size, Integer test,Double selectivity) throws IOException {
        Map<String, Map<String, Double>> map;
        if (rake) {
            map = new RAKEKeywordExtractor().computeRake(new ArrayList<>(trueRecs.values()));
        } else if (bugzilla) {
            Collection<Requirement> col = trueRecs.values();
            List<Requirement> toMakeSkill = Preprocess.preprocess(new ArrayList<>(col), test);
            for (Requirement r : toMakeSkill) {
                trueRecs.put(r.getId(), r);
            }
            map = computeAllSkillsNoMethod(trueRecs);
        } else {
            Map<String, Integer> model = KeywordExtractionModelRepository.getOne(organization).getModel();
            map = new TFIDFKeywordExtractor(selectivity).computeTFIDFExtra(model, size, trueRecs);
            KeywordExtractionModel mod = new KeywordExtractionModel();
            mod.setId(organization);
            mod.setModel(model);
            KeywordExtractionModelRepository.save(mod);
        }
        return map;
    }

    public Map<String, Map<String, Double>> computeAllSkillsNoMethod(Map<String, Requirement> recs) {
        Map<String, Map<String, Double>> ret = new HashMap<>();
        for (String s : recs.keySet()) {
            Requirement r = recs.get(s);
            Set<String> helper = new HashSet<>();
            for (String h : r.getDescription().split(" ")) {
                h = h.replace(".", "");
                if (!h.equals("") && h.length() > 1)
                    helper.add(h);
            }
            Map<String, Double> aux = new HashMap<>();
            for (String j : helper) {
                aux.put(j, 0.0);
            }
            ret.put(s, aux);
        }
        return computeTimeFactor(recs, ret, new Date());
    }

    public Map<String, Map<String, Double>> computeTime(Map<String, Map<String, Double>> skills, Map<String, Requirement> trueRecs) {
        skills = computeTimeFactor(trueRecs, skills, new Date());
        return skills;
    }

    public Map<String, Map<String, Double>> computeAllSkillsRequirement(Map<String, Requirement> recs, String organization,Double selectivity) throws IOException, ExecutionException, InterruptedException {
        TFIDFKeywordExtractor extractor = new TFIDFKeywordExtractor(selectivity);
        //Extract map with (Requirement / KeywordValue)
        Map<String, Map<String, Double>> keywords = extractor.computeTFIDF(new ArrayList<>(recs.values()));
        Date dat = new Date();
        //Transform the map from (Requirement / KeywordValue) to (Requirement / SkillFactor)

        //Skill factor is a linear function, dropping off lineally up to maxDropoff, based on the days
        //since the requirement was last touched
        HashMap<String, Integer> mod = extractor.getCorpusFrequency();
        KeywordExtractionModel toSave = new KeywordExtractionModel();
        toSave.setModel(mod);
        toSave.setId(organization);
        KeywordExtractionModelRepository.save(toSave);
        KeywordExtractionModelRepository.flush();
        return computeTimeFactor(recs, keywords, dat);
    }

    public Map<String, Map<String, Double>> computeTimeFactor(Map<String, Requirement> recs, Map<String, Map<String, Double>> allComponents, Date dat) {
        Map<String, Map<String, Double>> scaledKeywords = new HashMap<>();
        for (String s : allComponents.keySet()) {
            Requirement req = recs.get(s);
            long diffInMillies = Math.abs(dat.getTime() - req.getModified().getTime());
            long diffDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            Map<String, Double> aux = allComponents.get(s);
            Map<String, Double> helper = new HashMap<>();
            for (String j : aux.keySet()) {
                if (daysToUnconsider == -1.0 || daysToUnconsider >= diffDays) {
                    if (dropoffDays != -1.0)
                        helper.put(j, min(1.0, 1.0 - min(maxDropoff, diffDays * (maxDropoff / dropoffDays))));
                    else helper.put(j, 1.0);
                }
            }
            scaledKeywords.put(s, helper);
        }
        return scaledKeywords;
    }

    public Map<String, Map<String, Double>> computeAllSkillsRequirementRAKE(Map<String, Requirement> recs, String organization) throws IOException {
        //Extract map with (Requirement / KeywordValue)
        Map<String, Map<String, Double>> keywords = new RAKEKeywordExtractor().computeRake(new ArrayList<>(recs.values()));
        Date dat = new Date();

        //Transform the map from (Requirement / KeywordValue) to (Requirement / SkillFactor)

        //Skill factor is a linear function, dropping off lineally up to 0.5, based on the days
        //since the requirement was last touched
        return computeTimeFactor(recs, keywords, dat);
    }

}
