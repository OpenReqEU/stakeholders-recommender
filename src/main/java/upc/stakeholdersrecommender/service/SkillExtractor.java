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
import java.time.Clock;
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

    @Value("${Vogella.skill.dropoff.days}")
    private Double dropoffDaysVogella;
    @Value("${Vogella.skill.dropoff.max}")
    private Double maxDropoffVogella;
    @Value("${Vogella.skill.dropoff.days.unconsider}")
    private Double daysToUnconsiderVogella;


    @Autowired
    private KeywordExtractionModelRepository KeywordExtractionModelRepository;
    @Autowired
    private PreprocessService Preprocess;

    /**
     * Obtains the skills of the given parameters, with the appropiate algorithm to extract these
     * the algorithm can be RAKE, Tf-Idf, or no algorithm
     * @param trueRecs Map compromised of <Stakeholder, Requirements_done_by_stakeholder>
     * @param bugzilla Boolean identifying whether RAKE is to be used
     * @param organization String identifying the organization making this request
     * @param size Amount of unique requirements that exist
     * @param test Only to be used for mock tests, ignored for any other case
     * @param selectivity Value to be used for keyword discrimination in Tf-Idf keyword extraction
     * @param vogella Boolean identifying whether Vogella is making this request or not
     * @return  Map of maps, compromised by <Stakeholder, <Skill,Skill_value> >
     */
    public Map<String, Map<String, Double>> obtainSkills(Map<String, Requirement> trueRecs, Boolean bugzilla, Boolean rake, String organization, Integer size, Integer test, Double selectivity, Boolean vogella,Clock clock) throws IOException {
        Map<String, Map<String, Double>> map;
        if (rake) {
            map = new RAKEKeywordExtractor().computeRake(new ArrayList<>(trueRecs.values()));
        } else if (bugzilla) {
            Collection<Requirement> col = trueRecs.values();
            List<Requirement> toMakeSkill = Preprocess.preprocess(new ArrayList<>(col), test);
            for (Requirement r : toMakeSkill) {
                trueRecs.put(r.getId(), r);
            }
            map = computeAllSkillsNoMethod(trueRecs, vogella, clock);
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

    /**
     * Obtains the skills of the given parameters, with no underlying keyword extraction algorithm
     * @param recs Map compromised of <Stakeholder, Requirements_done_by_stakeholder>
     * @param vogella Boolean identifying whether Vogella is making this request or not
     * @return  Map of maps, compromised by <Stakeholder, <Skill,Skill_value> >
     */

    public Map<String, Map<String, Double>> computeAllSkillsNoMethod(Map<String, Requirement> recs, Boolean vogella,Clock clock) {
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
        return computeTimeFactor(recs, ret, clock, vogella);
    }

    /**
     * Tf-Idf extraction of skills
     * @param recs Map compromised of <Stakeholder, Requirements_done_by_stakeholder>
     * @param vogella Boolean identifying whether Vogella is making this request or not
     * @param selectivity Selectivity factor for Tf-Idf keyword discrimination
     * @param organization Organization making this request
     * @return  Map of maps, compromised by <Stakeholder, <Skill,Skill_value> >
     */
    public Map<String, Map<String, Double>> computeAllSkillsRequirement(Map<String, Requirement> recs, String organization, Double selectivity, Boolean vogella,Clock clock) throws IOException, ExecutionException, InterruptedException {
        TFIDFKeywordExtractor extractor = new TFIDFKeywordExtractor(selectivity);
        //Extract map with (Requirement / KeywordValue)
        Map<String, Map<String, Double>> keywords = extractor.computeTFIDF(new ArrayList<>(recs.values()));
        //Transform the map from (Requirement / KeywordValue) to (Requirement / SkillFactor)

        //Skill factor is a linear function, dropping off lineally up to maxDropoff, based on the days
        //since the requirement was last touched
        HashMap<String, Integer> mod = extractor.getCorpusFrequency();
        KeywordExtractionModel toSave = new KeywordExtractionModel();
        toSave.setModel(mod);
        toSave.setId(organization);
        KeywordExtractionModelRepository.save(toSave);
        KeywordExtractionModelRepository.flush();
        return computeTimeFactor(recs, keywords, clock, vogella);
    }

    /**
     * Computation of skill value degradation in regards to time, if Vogella is making this request, its default settings are overwritten
     * @param recs Map compromised of <Stakeholder, Requirements_done_by_stakeholder>
     * @param vogella Boolean identifying whether Vogella is making this request or not
     * @param allComponents Map compromised of <Stakeholder, <Skill,Skill_value>>
     * @param dat Time of the request
     * @return  Weighted skill map of maps, compromised by <Stakeholder, <Skill,Skill_value> >
     */

    public Map<String, Map<String, Double>> computeTimeFactor(Map<String, Requirement> recs, Map<String, Map<String, Double>> allComponents, Clock dat, Boolean vogella) {
        Double daysUnconsider = daysToUnconsider;
        Double dropoff = dropoffDays;
        Double maxDrop = maxDropoff;
        if (vogella) {
            daysUnconsider = daysToUnconsiderVogella;
            dropoff = dropoffDaysVogella;
            maxDrop = maxDropoffVogella;
        }
        Map<String, Map<String, Double>> scaledKeywords = new HashMap<>();
        for (String s : allComponents.keySet()) {
            Requirement req = recs.get(s);
            long diffInMillies = Math.abs(dat.millis() - req.getModified().getTime());
            long diffDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            Map<String, Double> aux = allComponents.get(s);
            Map<String, Double> helper = new HashMap<>();
            for (String j : aux.keySet()) {
                if (daysUnconsider == -1.0 || daysUnconsider >= diffDays) {
                    if (dropoff != -1.0)
                        helper.put(j, min(1.0, 1.0 - min(maxDrop, diffDays * (maxDrop / dropoff))));
                    else helper.put(j, 1.0);
                }
            }
            scaledKeywords.put(s, helper);
        }
        return scaledKeywords;
    }

    /**
     * Implementation of skill extraction with RAKE algorithm
     * @param recs Map compromised of <Stakeholder, Requirements_done_by_stakeholder>
     * @param vogella Boolean identifying whether Vogella is making this request or not
     * @return  Map of maps, compromised by <Stakeholder, <Skill,Skill_value> >
     */
    public Map<String, Map<String, Double>> computeAllSkillsRequirementRAKE(Map<String, Requirement> recs, Boolean vogella,Clock clock) throws IOException {
        //Extract map with (Requirement / KeywordValue)
        Map<String, Map<String, Double>> keywords = new RAKEKeywordExtractor().computeRake(new ArrayList<>(recs.values()));
        //Transform the map from (Requirement / KeywordValue) to (Requirement / SkillFactor)

        //Skill factor is a linear function, dropping off lineally up to 0.5, based on the days
        //since the requirement was last touched
        return computeTimeFactor(recs, keywords, clock, vogella);
    }

}
