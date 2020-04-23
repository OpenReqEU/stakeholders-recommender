package upc.stakeholdersrecommender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import upc.stakeholdersrecommender.domain.*;
import upc.stakeholdersrecommender.domain.Preprocess.PreprocessService;
import upc.stakeholdersrecommender.domain.Schemas.*;
import upc.stakeholdersrecommender.domain.keywords.RAKEKeywordExtractor;
import upc.stakeholdersrecommender.domain.keywords.TFIDFKeywordExtractor;
import upc.stakeholdersrecommender.entity.*;
import upc.stakeholdersrecommender.repository.*;

import javax.transaction.Transactional;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static java.lang.Double.max;

@Service
@Transactional
public class StakeholdersRecommenderService {
    Logger logger = Logger.getLogger(StakeholdersRecommenderService.class.getName());
    @Value("${person.hours.default}")
    private Double hoursDefault;
    @Autowired
    private PersonSRRepository PersonSRRepository;
    @Autowired
    private ProjectRepository ProjectRepository;
    @Autowired
    private RequirementSRRepository RequirementSRRepository;
    @Autowired
    private RejectedPersonRepository RejectedPersonRepository;
    @Autowired
    private EffortRepository EffortRepository;
    @Autowired
    private WordEmbedding WordEmbedding;
    @Autowired
    private PreprocessService Preprocess;
    @Autowired
    private TextPreprocessing pre;
    @Autowired
    private RiLoggingService RiLogging;
    @Autowired
    private KeywordExtractionModelRepository KeywordExtractionModelRepository;
    @Autowired
    private SkillExtractor SkillExtractor;

    public static <E> E[] createArray(int length, E... elements) {
        return Arrays.copyOf(elements, length);
    }

    /**
     * FOR EVALUATION PURPOSES
     */


    public void recallRate(String organization, BatchSchema batchSchema) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String,HashMap<String, List<String>>> optResults =
                mapper.readValue(new File("src/main/resources/tuningFiles/optimization-results.json"), HashMap.class);

        int countHigh = 0;
        int countLow = 0;
        for (String req : optResults.keySet()) {
            countHigh += optResults.get(req).get("high").size();
            countLow += optResults.get(req).get("low").size();
        }

        List<List<RecommendReturnSchema>> recommendationsListsOriginal =
                recommendRequirements(organization, batchSchema, optResults);
        /*apprWeight = -2.33680198014;
        availabilityWeight = 1.35657990717;
        compWeight = 8.46635564766;
        List<List<RecommendReturnSchema>> recommendationsListsOptimized=
                recommendRequirements(organization, batchSchema, optResults);

        apprWeight = 3.;
        availabilityWeight = 1.;
        compWeight = 30.;*/

        double[] recallRateOriginal = new double[20];
        double[] recallRateOptimized = new double[20];
        double[] recallRateOriginalLow = new double[20];
        double[] recallRateOptimizedLow = new double[20];
        for (int i = 0; i < 20; ++i) {
            recallRateOriginalLow[i] = 0.;
            recallRateOptimizedLow[i] = 0.;
            recallRateOptimized[i] = 0.;
            recallRateOriginal[i] = 0.;
        }

        computeRecallRate(optResults, recommendationsListsOriginal, recallRateOriginal, recallRateOriginalLow, countHigh, countLow);
        //computeRecallRate(optResults, recommendationsListsOptimized, recallRateOptimized, recallRateOptimizedLow, countHigh, countLow);

    }

    private void computeRecallRate(HashMap<String, HashMap<String, List<String>>> optResults, List<List<RecommendReturnSchema>> recommendationsListsOriginal,
                                   double[] recallRate, double[] recallRateLow, int countHigh, int countLow) {

        //RecallRate original
        for (List<RecommendReturnSchema> r : recommendationsListsOriginal) {
            if (r.size() > 0) {
                String req = r.get(0).getRequirement().getId();
                HashMap<String, List<String>> results = optResults.get(req);
                for (int i = 0; i < 20; ++i) {
                    if (r.size() > i) {
                        if (results.get("high").contains(r.get(i).getPerson().getUsername())) {
                            recallRate[i] += 1.;
                        }
                        else if (results.get("low").contains(r.get(i).getPerson().getUsername())) {
                            recallRateLow[i] += 1.;
                        }
                    }
                }
            }
        }

        System.out.println("original,@1,@2,@3,@4,@5,@6,@7,@8,@9,@10,@11,@12,@13,@14,@15,@16,@17,@18,@19,@20");
        List<String> resultsHigh = new ArrayList<>();
        List<String> resultsLow = new ArrayList<>();
        double sum = 0.;
        double sumLow = 0.;
        for (int i = 0; i < recallRate.length; ++i) {
            sum += recallRate[i];
            sumLow += recallRateLow[i];
            resultsHigh.add(sum / (double) countHigh * 100 + "%");
            resultsLow.add(sumLow / (double) countLow * 100 + "%");
            //System.out.print(sum / (double) countHigh * 100 + "%,");
        }
        System.out.print("high,");
        for (String s : resultsHigh) System.out.print(s + ",");
        System.out.print("\n");
        System.out.print("low,");
        for (String s : resultsLow) System.out.print(s + ",");
        System.out.print("\n");
    }

    private List<List<RecommendReturnSchema>> recommendRequirements(String organization, BatchSchema batchSchema, HashMap<String, HashMap<String,
            List<String>>> optResults) throws Exception {

        List<List<RecommendReturnSchema>> recommendationsLists = new ArrayList<>();
        int process = 0;
        for (String reqId : optResults.keySet()) {
            if (process % 5 == 0) System.out.println("Process at " + (double) process / (double) optResults.keySet().size() * 100. + "%");
            ++process;
            RecommendSchema recommendSchema = new RecommendSchema();
            recommendSchema.setProject(new ProjectMinimal(batchSchema.getProjects().get(0).getId()));
            recommendSchema.setUser(batchSchema.getPersons().get(0));
            boolean found = false;
            int k = 0;
            while (!found && k < batchSchema.getRequirements().size()) {
                if (batchSchema.getRequirements().get(k).getId().equals(reqId)) {
                    Requirement req = batchSchema.getRequirements().get(k);
                    recommendSchema.setRequirement(req);
                    found = true;
                } else ++k;
            }

            List<RecommendReturnSchema> recommendations = recommend(recommendSchema, 20, false, organization, 0);
            recommendationsLists.add(recommendations);
        }
        return recommendationsLists;
    }

    public void optimize(String organization) throws Exception {
        String csvFile = "src/main/resources/tuningFiles/triplets.csv";
        String line = "";
        String cvsSplitBy = ",";

        List<String> requirements = new ArrayList<>();
        List<String> goodRecommendations = new ArrayList<>();
        List<String> badRecommendations = new ArrayList<>();

        //We read the triplets information
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(cvsSplitBy);
                requirements.add(fields[0]);
                goodRecommendations.add(fields[1]);
                badRecommendations.add(fields[2]);
            }
        }

        int N = 24;
        double sigma = 1;

        List<Integer> index = new ArrayList<>();
        for (int i = 0; i < requirements.size(); ++i) {
            index.add(i);
        }

        for (int i = 0; i < N; ++i) {
            Collections.shuffle(index);
            System.out.println("New iteration | Starting number " + (i + 1));
            for (int j = 0; j < requirements.size(); ++j) {
                String reqId = requirements.get(index.get(j));
                String goodRec = goodRecommendations.get(index.get(j));
                String badRec = badRecommendations.get(index.get(j));
                List<Double> firstRes = computeStakeholderScore(PersonSRRepository.findById(new PersonSRId("1", badRec, organization)),
                        RequirementSRRepository.findById(new RequirementSRId("1", reqId, organization)), apprWeight, availabilityWeight, compWeight);
                List<Double> secondRes = computeStakeholderScore(PersonSRRepository.findById(new PersonSRId("1", goodRec, organization)),
                        RequirementSRRepository.findById(new RequirementSRId("1", reqId, organization)), apprWeight, availabilityWeight, compWeight);
                //Appropiateness weight
                apprWeight = apprWeight - sigma * (-1) * (secondRes.get(0) - firstRes.get(0)) * (Math.exp(firstRes.get(3))) /
                        (Math.exp(firstRes.get(3)) + Math.exp(secondRes.get(3)));
                //Availability weight
                availabilityWeight = availabilityWeight - sigma * (-1) * (secondRes.get(1) - firstRes.get(1)) * (Math.exp(firstRes.get(3))) /
                        (Math.exp(firstRes.get(3)) + Math.exp(secondRes.get(3)));
                //Component weight
                compWeight = compWeight - sigma * (-1) * (secondRes.get(2) - firstRes.get(2)) * (Math.exp(firstRes.get(3))) /
                        (Math.exp(firstRes.get(3)) + Math.exp(secondRes.get(3)));
                String s = "";
            }
            System.out.println("New values");
            System.out.println("\tApprWeight = " + apprWeight);
            System.out.println("\tAvailabilityWeight = " + availabilityWeight);
            System.out.println("\tComponentWeight = " + compWeight);
        }

    }


    /*
    Returns a double array with the following values:
       0 -> res
       1 -> availability
       2 -> resComp
       3 -> total
     */
    private List<Double> computeStakeholderScore(PersonSR person, RequirementSR req, Double apprWeight, Double availabilityWeight, Double compWeight) throws IOException, ExecutionException, InterruptedException {
        Set<String> reqSkills = req.getSkillsSet();
        List<String> component = req.getComponent();
        Double sum = 0.0;
        Double compSum = 0.0;
        Double resComp = 0.0;
        for (String s : req.getSkillsSet()) {
            Double weightToAdd = 0.0;
            Skill mostSimilarSkill = null;
            for (Skill j : person.getSkills()) {
                if (j.getName().equals(s)) {
                    weightToAdd = 100.0;
                    sum = sum + j.getWeight();
                    break;
                } else {
                    Double val = null;
                    val = WordEmbedding.computeSimilarity(j.getName(), s);
                    if (val > weightToAdd) {
                        weightToAdd = val;
                        mostSimilarSkill = j;
                    }
                }
            }
            if (weightToAdd != 100.0) {
                if (weightToAdd != 0.0)
                    sum = sum + weightToAdd * mostSimilarSkill.getWeight();
            }
        }
        if (component != null && person.getComponents() != null) {
            for (String s : component) {
                for (Skill j : person.getComponents()) {
                    if (s.equals(j.getName())) {
                        compSum += j.getWeight();
                    }
                }
            }
            resComp = component.size() > 0 ? compSum / component.size() : 0;
        }
        double res;
        if (reqSkills.size() == 0) {
            res = 0.0;
        } else {
            res = sum / reqSkills.size();
        }
        double score = res * apprWeight + person.getAvailability() * availabilityWeight + resComp * compWeight;
        return Arrays.asList(res, person.getAvailability(), resComp, score);
    }

    /**
     * END FOR EVALUATION PURPOSES
     */

    /**
     * Recomends a list of stakeholders for a given recommend request
     * @param request Request with requirement, sender and project
     * @param k Maximum number of stakeholders to return
     * @param projectSpecific Whether only stakeholers who are assigned to this project, or any stakeholder is considered
     * @param organization Organization that made the request
     * @param test Only used for mock testing, ignore if not testing
     * @return Returns an ordered list comprised of the top k stakeholders
     */

    public List<RecommendReturnSchema> recommend(RecommendSchema request, int k, Boolean projectSpecific, String organization, Integer test) throws Exception {
        verifyRecommend(request);
        String p = request.getProject().getId();
        List<RecommendReturnSchema> ret;
        List<PersonSR> persList = new ArrayList<>();
        RequirementSR req;
        RequirementSR newReq = new RequirementSR();
        Requirement requirement = request.getRequirement();
        requirement.setDescription(requirement.getDescription() + ". " + requirement.getName());
        newReq.setProjectIdQuery(request.getProject().getId());
        newReq.setId(new RequirementSRId(request.getProject().getId(), request.getRequirement().getId(), organization));
        ProjectSR pro = ProjectRepository.findById(new ProjectSRId(request.getProject().getId(), organization));
        Boolean rake = pro.getRake();
        Boolean bugzilla = pro.getBugzilla();
        if (bugzilla) {
            requirement.setDescription(pre.text_preprocess(requirement.getDescription()));
            requirement.setName(pre.text_preprocess(requirement.getName()));
            newReq.setSkills(Preprocess.preprocessSingular(requirement, test));
        } else {
            if (!rake) {
                Integer size = pro.getRecSize();
                newReq.setSkills(new TFIDFKeywordExtractor(pro.getSelectivity()).computeTFIDFSingular(requirement, KeywordExtractionModelRepository.getOne(organization).getModel(), size));
            } else newReq.setSkills(new RAKEKeywordExtractor().computeRakeSingular(requirement));
        }
        List<String> comps = new ArrayList<>();
        if (request.getRequirement().getRequirementParts() != null) {
            for (RequirementPart l : request.getRequirement().getRequirementParts()) {
                comps.add(l.getId());
            }
        }
        newReq.setComponent(comps);
        RequirementSRRepository.save(newReq);
        req = newReq;
        if (!projectSpecific) {
            Set<String> uniquePersons = new HashSet<>();
            for (PersonSR pers : PersonSRRepository.findByOrganization(organization)) {
                if (!uniquePersons.contains(pers.getName())) {
                    //if (hasTime(pers, organization)) {
                    if (true) {
                        uniquePersons.add(pers.getName());
                        PersonSR per = new PersonSR(pers);
                        per.setAvailability(1.0);
                        persList.add(per);
                    }
                }
            }
        } else {
            persList.addAll(PersonSRRepository.findByProjectIdQueryAndOrganization(p, organization));
        }
        List<PersonSR> clean = removeRejected(persList, request.getUser().getUsername(), organization, request.getRequirement().getId());
        Double hours = 100.0;
        if (projectSpecific) {
            Double effort = request.getRequirement().getEffort();
            Effort e = EffortRepository.findById(new ProjectSRId(request.getProject().getId(), organization));
            if (e != null) {
                Map<Double, Double> effMap = e.getEffortMap();
                if (effMap.containsKey(effort)) {
                    hours = effMap.get(effort);
                } else {
                    hours = effort;
                    Effort eff = e;
                    effMap.put(effort, effort);
                    eff.setEffortMap(effMap);
                    EffortRepository.save(eff);
                }
            }
        }
        Pair<PersonSR, Double>[] bestPeople = computeBestStakeholders(clean, req, hours, k, projectSpecific, apprWeight, availabilityWeight, compWeight);
        ret = prepareFinal(bestPeople, req);
        return ret;
    }

    /**
     * Verifies a recommend request
     * @param request Request with requirement, sender and project
     */
    private void verifyRecommend(RecommendSchema request) throws Exception {
        if (request.getRequirement() == null) throw new Exception("Requirement in request is null");
        if (request.getProject() == null) throw new Exception("Project in request is null");
        if (request.getUser() == null) throw new Exception("User in request is null");
    }
    /**
     * Removes stakeholders who have been rejected in the past from this recommendation
     * @param persList Request with requirement, sender and project
     * @param organization String representing organization that made the request
     * @param requirement Id identifying the requirement
     */
    private List<PersonSR> removeRejected(List<PersonSR> persList, String user, String organization, String requirement) {
        List<PersonSR> newList = new ArrayList<>();
        RejectedPerson rej = RejectedPersonRepository.findByUser(new RejectedPersonId(user, organization));
        if (rej != null) {
            for (PersonSR pers : persList) {
                if (!rej.getDeleted().containsKey(pers.getName())) {
                    newList.add(pers);
                } else if (!rej.getDeleted().get(pers.getName()).contains(requirement)) {
                    newList.add(pers);
                }
            }
        } else newList = persList;
        return newList;
    }

    private List<RecommendReturnSchema> prepareFinal(Pair<PersonSR, Double>[] people, RequirementSR req) throws IOException {
        List<RecommendReturnSchema> ret = new ArrayList<>();
        for (Pair<PersonSR, Double> personPair : people) {
            Double appropiateness = personPair.getSecond();
            PersonSR pers = personPair.getFirst();
            if (appropiateness > 0.0) {
                PersonMinimal min = new PersonMinimal();
                min.setUsername(pers.getName());
                ret.add(new RecommendReturnSchema(new RequirementMinimal(req.getId().getRequirementId()), min, appropiateness, pers.getAvailability()));
            }
        }
        Collections.sort(ret,
                Comparator.comparingDouble(RecommendReturnSchema::getAppropiatenessScore).reversed());
        if (ret.size() > 1) {
            RecommendReturnSchema best = ret.get(0);
            Double percentage = getPercentage(best, people, req);
            Double conversion = percentage / best.getAppropiatenessScore();
            for (RecommendReturnSchema recommend : ret) {
                recommend.setAppropiatenessScore(recommend.getAppropiatenessScore() * conversion);
            }
        }
        return ret;
    }

    private Double getPercentage(RecommendReturnSchema best, Pair<PersonSR, Double>[] people, RequirementSR req) throws IOException {
        Double percentage = 0.0;
        Double intersect = 0.0;
        PersonSR chosen = null;
        for (Pair<PersonSR, Double> pers : people) {
            if (pers.getFirst().getName().equals(best.getPerson().getUsername())) chosen = pers.getFirst();
        }
        for (String sk : req.getSkillsSet()) {
            Double weightToAdd = 0.0;
            for (Skill j : chosen.getSkills()) {
                if (j.getName().equals(sk)) {
                    weightToAdd = 100.0;
                    ++intersect;
                    break;
                } else {
                    Double val = WordEmbedding.computeSimilarity(j.getName(), sk);
                    if (val > weightToAdd) {
                        weightToAdd = val;
                    }
                }
            }
            if (weightToAdd != 100.0) {
                if (weightToAdd != 0.0)
                    intersect = intersect + (weightToAdd * 1);
            }
        }
        percentage = intersect / (double) req.getSkillsSet().size();
        return percentage;
    }

    /*public Double apprWeight = 3.;
    public Double availabilityWeight = 1.;
    public Double compWeight = 30.;
    */public Double apprWeight = -2.33680198014;
    public Double availabilityWeight = 1.35657990717;
    public Double compWeight = 8.46635564766;

    /**
     * Return an array composed of the top K stakeholders and their recommendation value
     * @param persList List of stakeholders to be considered
     * @param hours Hours the requirement requires
     * @param projectSpecific Whether this request is specific to a project, or to all
     * @return An array of pairs, of size k, ordered by their recommendation value
     */
    private Pair<PersonSR, Double>[] computeBestStakeholders(List<PersonSR> persList, RequirementSR req, Double hours, int k, Boolean projectSpecific,
                                                             Double apprWeight, Double availabilityWeight, Double compWeight) throws IOException, ExecutionException, InterruptedException {
        List<Pair<PersonSR, Pair<Double, Double>>> valuesForSR = new ArrayList<>();
        Set<String> reqSkills = req.getSkillsSet();
        List<String> component = req.getComponent();
        Map<String,String> toPrint=new HashMap<>();
        Double counter=0.0;
        Double size=(double)persList.size();
        for (PersonSR person : persList) {
            Double sum = 0.0;
            Double compSum = 0.0;
            Double resComp = 0.0;
            for (String s : req.getSkillsSet()) {
                Double weightToAdd = 0.0;
                Skill mostSimilarSkill = null;
                for (Skill j : person.getSkills()) {
                    if (j.getName().equals(s)) {
                        weightToAdd = 100.0;
                        sum = sum + j.getWeight();
                        break;
                    } else {
                        Double val = null;
                        val = WordEmbedding.computeSimilarity(j.getName(), s);
                        if (val > weightToAdd) {
                            weightToAdd = val;
                            mostSimilarSkill = j;
                        }
                    }
                }
                if (weightToAdd != 100.0) {
                    if (weightToAdd != 0.0 && mostSimilarSkill != null)
                        sum = sum + weightToAdd * mostSimilarSkill.getWeight();
                }
            }
            if (component != null && person.getComponents() != null) {
                for (String s : component) {
                    for (Skill j : person.getComponents()) {
                        if (s.equals(j.getName())) {
                            compSum += j.getWeight();
                        }
                    }
                }
                resComp = component.size() > 0 ? compSum / component.size() : 0;
            }
            Double res;
            if (reqSkills.size() == 0) {
                res = 0.0;
            } else {
                res = reqSkills.size() > 0 ? sum / reqSkills.size() : 0;
            }
            Map<String, Skill> skillTrad = new HashMap<>();
            Double appropiateness = null;
            appropiateness = getAppropiateness(reqSkills, person, skillTrad);
            String toAdd=String.valueOf(appropiateness);
            //res = res * 3 + person.getAvailability() + resComp * 30;
            res = res * apprWeight + person.getAvailability() * availabilityWeight + resComp * compWeight;
            toAdd=toAdd+","+person.getAvailability();
            toAdd=toAdd+","+resComp;
            toPrint.put(person.getName(),toAdd);
            if ((projectSpecific && person.getAvailability() >= (hours / person.getHours())) && appropiateness != 0.0) {
                Pair<Double, Double> auxPair = new Pair<>(-res, appropiateness);
                Pair<PersonSR, Pair<Double, Double>> valuePair = new Pair<>(person, auxPair);
                valuesForSR.add(valuePair);
            } else if (!projectSpecific && appropiateness != 0.0) {
                Pair<Double, Double> auxPair = new Pair<>(-res, appropiateness);
                Pair<PersonSR, Pair<Double, Double>> valuePair = new Pair<>(person, auxPair);
                valuesForSR.add(valuePair);
            }
            counter++;
            if (counter%50==0) System.out.println("Recommendation evaluation is at "+ (counter/size)*100+"%");
        }
        System.out.println("Recommendation evaluation is at 100%");
        Collections.sort(valuesForSR, Comparator.comparing(u -> u.getSecond().getFirst()));
        if (k >= valuesForSR.size()) {
            k = valuesForSR.size();
        }
        Pair<PersonSR, Double>[] out = createArray(k);
        for (int i = 0; i < k && i < valuesForSR.size(); ++i) {
            out[i] = new Pair<>(valuesForSR.get(i).getFirst(), valuesForSR.get(i).getSecond().getSecond());
        }
        System.out.println("Best stakeholders computed");
        return out;
    }

    private Double getAppropiateness(Set<String> reqSkills, PersonSR person, Map<String, Skill> skillTrad) throws IOException {
        Double total = 0.0;
        if (person.getSkills() != null && person.getSkills().size() > 0) {
            for (Skill sk : person.getSkills()) {
                skillTrad.put(sk.getName(), sk);
            }
            for (String done : reqSkills) {
                Double weightToAdd = 0.0;
                String mostSimilarWord = "";
                for (String skill : skillTrad.keySet()) {
                    if (skill.equals(done)) {
                        weightToAdd = 100.0;
                        total = total + skillTrad.get(skill).getWeight();
                        break;
                    } else {
                        Double val = WordEmbedding.computeSimilarity(skill, done);
                        if (val > weightToAdd) {
                            weightToAdd = val;
                            mostSimilarWord = skill;
                        }
                    }
                }
                if (weightToAdd != 100.0) {
                    if (weightToAdd != 0.0)
                        total = total + (weightToAdd * skillTrad.get(mostSimilarWord).getWeight());
                }
            }
        }
        Double amount = (double) reqSkills.size();
        Double appropiateness;
        if (amount == 0.0) {
            appropiateness = 0.0;
        } else appropiateness = total / amount;
        return appropiateness;
    }

    private boolean hasTime(PersonSR pers, String organization) {
        boolean res = false;
        List<PersonSR> work = PersonSRRepository.findByNameAndOrganization(pers.getId().getPersonId(), organization);
        for (PersonSR per : work) {
            if (per.getAvailability() > 0) {
                res = true;
                break;
            }
        }
        return res;
    }

    private void purgeByOrganization(String organization) {
        if (ProjectRepository.findByOrganization(organization) != null)
            ProjectRepository.deleteByOrganization(organization);
        if (PersonSRRepository.findByOrganization(organization) != null)
            PersonSRRepository.deleteByOrganization(organization);
        if (RequirementSRRepository.findByOrganization(organization) != null)
            RequirementSRRepository.deleteByOrganization(organization);
        if (KeywordExtractionModelRepository.existsById(organization))
            KeywordExtractionModelRepository.deleteById(organization);

    }
    /**
     * Add the person identified by rejectedId to the list of blacklisted stakeholders for the specified requeriment by requiremintId
     * when the request is made by userId
     * @param rejectedId Id of person to reject
     * @param userId Id of the stakeholder
     * @param requirementId Id of the requirement
     * @param organization Organization making this request
     */
    public void recommend_reject(String rejectedId, String userId, String requirementId, String organization) {
        if (RejectedPersonRepository.findByUser(new RejectedPersonId(userId, organization)) != null) {
            RejectedPerson rejected = RejectedPersonRepository.findByUser(new RejectedPersonId(userId, organization));
            if (rejected.getDeleted().containsKey(rejectedId)) {
                HashSet<String> aux = rejected.getDeleted().get(rejectedId);
                aux.add(requirementId);
                Map<String, HashSet<String>> auxMap = rejected.getDeleted();
                auxMap.put(rejectedId, aux);
                rejected.setDeleted(auxMap);
            } else {
                HashSet<String> aux = new HashSet<>();
                aux.add(requirementId);
                Map<String, HashSet<String>> auxMap = rejected.getDeleted();
                auxMap.put(rejectedId, aux);
                rejected.setDeleted(auxMap);
            }
            RejectedPersonRepository.saveAndFlush(rejected);
        } else {
            RejectedPerson reject = new RejectedPerson(new RejectedPersonId(userId, organization));
            HashMap<String, HashSet<String>> aux = new HashMap<>();
            HashSet<String> auxset = new HashSet<>();
            auxset.add(requirementId);
            aux.put(rejectedId, auxset);
            reject.setDeleted(aux);
            RejectedPersonRepository.saveAndFlush(reject);
        }
    }

    /**
     * Adds to the database the processed input, to be used for future recommendations
     * @param request List of stakeholders to be considered
     * @param withAvailability Hours the requirement requires
     * @param withComponent Whether this request is specific to a project, or to all
     * @param organization Organization making the request
     * @param autoMapping Whether the mapping of effort points to hours is assumed to be 1 to 1 or not
     * @param bugzillaPreprocessing Whether the external service for keyword extraction is used
     * @param logging Whether the external service for logging is used
     * @param test Used for mock tests, to be ignored if not testing
     * @param selectivity Value to be used for tf-idf keyword discrimination factor
     * @param clock
     * @return The amount of objects processed
     */
    public Integer addBatch(BatchSchema request, Boolean withAvailability, Boolean withComponent, String organization, Boolean autoMapping, Boolean bugzillaPreprocessing, Boolean logging, Integer test, Double selectivity, Clock clock) throws Exception {
        purgeByOrganization(organization);
        verify(request);
        Boolean vogella = false;
        if (organization.equals("Vogella")) vogella = true;
        Map<String, Requirement> recs = new HashMap<>();
        List<Requirement> requeriments;
        if (bugzillaPreprocessing) {
            requeriments = cleanRequirements(request.getRequirements());
            requeriments = Preprocess.preprocess(requeriments, test);
        } else {
            requeriments = request.getRequirements();
        }
        for (Requirement r : requeriments) {
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
            Date dtIn = inFormat.parse(r.getModified_at());
            r.setModified(dtIn);
            if (r.getName() != null) r.setDescription(r.getDescription() + " . " + r.getName());
            recs.put(r.getId(), r);
        }
        Map<String, Set<String>> personRecs = getPersonRecs(request);
        Set<String> persons = getPersons(request);
        Map<String, List<Participant>> participants = getParticipants(request);
        Boolean rake = true;
        Map<String, Map<String, Double>> allSkills;
        if (!bugzillaPreprocessing) {
            if (requeriments.size() > 100) rake = false;
            if (rake) allSkills = SkillExtractor.computeAllSkillsRequirementRAKE(recs, vogella,clock);
            else allSkills = SkillExtractor.computeAllSkillsRequirement(recs, organization, selectivity, vogella,clock);
        } else {
            allSkills = SkillExtractor.computeAllSkillsNoMethod(recs, vogella,clock);
        }
        System.out.println("Skills computed");
        Map<String, Integer> skillfrequency = getSkillFrequency(allSkills);
        Map<String, Map<String, Double>> allComponents = new HashMap<>();
        Map<String, Integer> componentFrequency = new HashMap<>();
        if (withComponent) {
            for (Requirement req : request.getRequirements()) {
                Map<String, Double> component = new HashMap<>();
                if (req.getRequirementParts() != null)
                    for (RequirementPart str : req.getRequirementParts()) {
                        component.put(str.getId(), 0.0);
                        if (componentFrequency.containsKey(str.getId())) {
                            componentFrequency.put(str.getId(), componentFrequency.get(str.getId()) + 1);
                        } else componentFrequency.put(str.getId(), 1);
                    }
                allComponents.put(req.getId(), component);
            }
            allComponents = SkillExtractor.computeTimeFactor(recs,allComponents,clock, vogella);
            System.out.println("Components computed");
        }
        Set<String> projs = new HashSet<>();
        Set<String> seenPersons = new HashSet<>();
        Integer recSize = request.getRequirements().size();

        LoggingInformation pair = null;
        Map<String, Integer> loggingFrequency = null;

        if (logging) {
            pair = RiLogging.getUserLogging(bugzillaPreprocessing, rake, organization, recSize, test, selectivity, vogella,clock);
            loggingFrequency = getSkillFrequency(pair.getFirst());
            System.out.println("Logging computed");
        }


        for (Project proj : request.getProjects()) {
            projs.add(proj.getId());
            if (autoMapping) {
                Effort effortMap = new Effort();
                HashMap<Double, Double> eff = new HashMap<>();
                if (proj.getSpecifiedRequirements() != null) {
                    for (String r : proj.getSpecifiedRequirements()) {
                        Requirement aux = recs.get(r);
                        if (!eff.containsKey(aux.getEffort())) {
                            eff.put(aux.getEffort(), aux.getEffort());
                        }
                    }
                }
                effortMap.setEffortMap(eff);
                effortMap.setId(new ProjectSRId(proj.getId(), organization));
                if (EffortRepository.findById(new ProjectSRId(proj.getId(), organization)) != null)
                    EffortRepository.deleteById(new ProjectSRId(proj.getId(), organization));
                EffortRepository.save(effortMap);
                System.out.println("Auto-effort computed");
            }
            List<Participant> part = new ArrayList<>();
            if (participants.containsKey(proj.getId())) part = participants.get(proj.getId());
            String id = instanciateProject(proj, part, organization, rake, recSize, bugzillaPreprocessing, selectivity);
            System.out.println("Project "+id+" instanciated");
            Map<String, Double> hourMap = new HashMap<>();
            for (Participant par : part) {
                hourMap.put(par.getPerson(), par.getAvailability());
            }
            for (Participant p : part) {
                seenPersons.add(p.getPerson());
            }
            instanciateFeatureBatch(proj.getSpecifiedRequirements(), id, allSkills, recs, withComponent, allComponents, organization);
            System.out.println("Requirements for project "+id+" are at 100%");
            instanciateResourceBatch(hourMap, part, recs, allSkills, personRecs, skillfrequency, proj.getSpecifiedRequirements(), id, withAvailability, withComponent, allComponents, componentFrequency, organization, pair, loggingFrequency);
            System.out.println("Stakeholders for project "+id+" are at 100%");
        }
        persons.removeAll(seenPersons);
        instanciateLeftovers(persons, projs, allSkills, personRecs, skillfrequency, withComponent, allComponents, componentFrequency, organization, pair, loggingFrequency);
        Integer particips = 0;
        System.out.println("Leftovers finished");
        if (request.getParticipants() != null) particips = request.getParticipants().size();
        return request.getPersons().size() + request.getProjects().size() + request.getRequirements().size() + request.getResponsibles().size() + particips;
    }

    private void verify(BatchSchema request) throws Exception {
        Set<String> rec = new HashSet<>();
        for (Requirement r : request.getRequirements()) {
            if (r.getId() == null) throw new Exception("Requirement id of a requirement in request is null");
            if (r.getDescription() == null && r.getName() == null)
                throw new Exception("A requirement on the request has no name nor description");
            if (r.getId().length() > 255) throw new Exception("Requirement id exceeds character size of 255");
            if (rec.contains(r.getId())) throw new Exception("Requirement id " + r.getId() + " is repeated");
            rec.add(r.getId());
        }
        Set<String> proj = new HashSet<>();
        for (Project p : request.getProjects()) {
            if (p.getId() == null) throw new Exception("Project if of a project is null in request");
            if (p.getId().length() > 255) throw new Exception("Project id exceeds character size of 255");
            if (proj.contains(p.getId())) throw new Exception("Project id " + p.getId() + " is repeated");
            for (String a : p.getSpecifiedRequirements())
                if (!rec.contains(a)) throw new Exception("Specified requirement " + a + " doesn't exist");
            proj.add(p.getId());
        }
        Set<String> person = new HashSet<>();
        for (PersonMinimal p : request.getPersons()) {
            if (p.getUsername() == null) throw new Exception("Username of a person in request is null");
            if (p.getUsername().length() > 255) throw new Exception("Requirement id exceeds character size of 255");
            if (person.contains(p.getUsername())) throw new Exception("Person id " + p.getUsername() + " is repeated");
            person.add(p.getUsername());
        }
        for (Responsible r : request.getResponsibles()) {
            if (r.getPerson() == null || r.getRequirement() == null)
                throw new Exception("Person or requirement is null in a responsible in request");
            if (!rec.contains(r.getRequirement()))
                throw new Exception("Person assigned to non-existent requirement " + r.getRequirement());
            if (!person.contains(r.getPerson()))
                throw new Exception("Requirement assigned to non-existent person " + r.getPerson());
        }
        if (request.getParticipants() != null) {
            for (Participant p : request.getParticipants()) {
                if (p.getPerson() == null || p.getProject() == null)
                    throw new Exception("Person or project in a participant is null in request");
                if (!person.contains(p.getPerson()))
                    throw new Exception("Project assigned to non-existent person " + p.getPerson());
                if (!proj.contains(p.getProject()))
                    throw new Exception("Person assigned to non-existent project " + p.getProject());
                if (p.getAvailability() != null && p.getAvailability() < 0)
                    throw new Exception("Availability must be in a range from 1.0 to 0.0");
            }
        }
    }


    private void instanciateLeftovers(Set<String> persons, Set<String> oldIds, Map<String, Map<String, Double>> allSkills, Map<String, Set<String>> personRecs, Map<String, Integer> skillFrequency, Boolean withComponent
            , Map<String, Map<String, Double>> allComponents, Map<String, Integer> componentFrequency, String organization, LoggingInformation pair, Map<String, Integer> loggingFrequency) throws JsonProcessingException {
        String newId = RandomStringUtils.random(15, true, true);
        while (oldIds.contains(newId)) newId = RandomStringUtils.random(15, true, true);
        List<PersonSR> toSave = new ArrayList<>();
        for (String s : persons) {
            List<Skill> skills;
            List<Skill> components;
            if (personRecs.get(s) != null) {
                skills = computeSkillsPerson(personRecs.get(s), allSkills, skillFrequency, pair, s, loggingFrequency);
                if (withComponent)
                    components = computeComponentsPerson(personRecs.get(s), allComponents, componentFrequency, s);
                else components = new ArrayList<>();
            } else {
                skills = new ArrayList<>();
                components = new ArrayList<>();
            }
            PersonSR per = new PersonSR(s, skills, hoursDefault, newId, organization, components, 1.0, new PersonSRId(newId, s, organization));
            toSave.add(per);
        }
        PersonSRRepository.saveAll(toSave);
    }

    private Set<String> getPersons(BatchSchema request) {
        Set<String> s = new HashSet<>();
        for (PersonMinimal p : request.getPersons()) {
            s.add(p.getUsername());
        }
        return s;
    }

    private Map<String, Integer> getSkillFrequency(Map<String, Map<String, Double>> allSkills) {
        Map<String, Integer> skillfrequency = new HashMap<>();
        for (String s : allSkills.keySet()) {
            for (String j : allSkills.get(s).keySet()) {
                if (!skillfrequency.containsKey(j)) {
                    skillfrequency.put(j, 1);
                } else {
                    skillfrequency.put(j, skillfrequency.get(j) + 1);
                }
            }
        }
        return skillfrequency;
    }
    /**
     * Processes stakeholders, and generates the information needed for recommendations, saving it to the database
     * @param part Map comprised of <stakeholder,availability>
     * @param withAvailability Whether availability is considered or not
     * @param withComponent Whether components are considered or not
     * @param organization Organization making the request
     * @param allSkills Map comprised of <Requirement_id,<skill,skill_value>
     * @param componentFrequency Frequency of each requirement_part
     * @param pair Logging information, if null, won't be used
     * @param loggingFrequency Value to be used for tf-idf keyword discrimination factor
     * @param personRecs Requirements that this stakeholder is assigned to within this project
     * @param skillFrequency Frequency of each skill in the corpus of the requirements
     * @param specifiedReq All requirements of the project
     * @param id Id of the project
     * @return The amount of objects processed
     */
    private void instanciateResourceBatch(Map<String, Double> part, List<Participant> persons, Map<String, Requirement> recs, Map<String, Map<String, Double>> allSkills, Map<String, Set<String>> personRecs, Map<String, Integer> skillFrequency, List<String> specifiedReq, String id, Boolean withAvailability, Boolean withComponent
            , Map<String, Map<String, Double>> allComponents, Map<String, Integer> componentFrequency, String organization, LoggingInformation pair, Map<String, Integer> loggingFrequency) throws Exception {
        List<PersonSR> toSave = new ArrayList<>();
        Double counter=0.0;
        Double size=(double) persons.size();
        for (Participant person : persons) {
            List<Skill> skills;
            List<Skill> components;
            if (personRecs.get(person.getPerson()) != null) {
                skills = computeSkillsPerson(personRecs.get(person.getPerson()), allSkills, skillFrequency, pair, person.getPerson(), loggingFrequency);
                if (withComponent)
                    components = computeComponentsPerson(personRecs.get(person.getPerson()), allComponents, componentFrequency, person.getPerson());
                else components = new ArrayList<>();
            } else {
                skills = new ArrayList<>();
                components = new ArrayList<>();
            }
            Double availability;
            if (withAvailability) {
                Double hours = hoursDefault;
                if (part.containsKey(person.getPerson()) && part.get(person.getPerson()) != null)
                    hours = part.get(person.getPerson());
                if (!personRecs.containsKey(person.getPerson())) availability = 1.0;
                else
                    availability = computeAvailability(specifiedReq, personRecs, person, recs, id, hours, organization);
            } else availability = 1.0;
            Double hours = 0.0;
            if (part.containsKey(person.getPerson()) && part.get(person.getPerson()) != null)
                hours = part.get(person.getPerson());
            else hours = hoursDefault;
            PersonSR per = new PersonSR(person.getPerson(), skills, hours, person.getProject(), organization, components, availability, new PersonSRId(id, person.getPerson(), organization));
            toSave.add(per);
            counter++;
            if (counter.intValue()%50==0) System.out.println("Stakeholder instantiation of project "+id+" is at "+(counter/size)*100.0+"%");
        }
        PersonSRRepository.saveAll(toSave);
    }

    private List<Skill> computeComponentsPerson(Set<String> oldRecs, Map<String, Map<String, Double>> allComponents, Map<String, Integer> componentFrequency, String s) throws JsonProcessingException {
        return getSkills(oldRecs, allComponents, componentFrequency, null, s, null);

    }

    /**
     * Returns the list of skills of the stakeholder
     * @param componentFrequency Frequency of each requirement_part
     * @param pair Logging information, if null, won't be used
     * @param loggingFrequency Value to be used for tf-idf keyword discrimination factor
     * @param oldRecs Requeriments to obtain skills from
     * @param person Stakeholder to obtain the skills from
     * @return The amount of objects processed
     */

    private List<Skill> getSkills(Set<String> oldRecs, Map<String, Map<String, Double>> allComponents, Map<String, Integer> componentFrequency,
                                  LoggingInformation pair, String person, Map<String, Integer> loggingFrequency) throws JsonProcessingException {
        List<Skill> toret = new ArrayList<>();
        Map<String, SinglePair<Double>> appearances = getAppearances(oldRecs, allComponents, componentFrequency);
        Pair<Map<String, SinglePair<Double>>, Map<String, Pair<Integer, Integer>>> appearancesLog = null;
        if (loggingFrequency != null) {
            if (pair.getSecond().containsKey(person))
                appearancesLog = getAppearancesWithTime(pair.getSecond().get(person), pair.getFirst(), loggingFrequency);
        }
        if (pair != null && appearancesLog != null) {
            for (String n : appearancesLog.getFirst().keySet()) {
                if (!appearances.containsKey(n)) appearances.put(n, new SinglePair<>(0.0, 0.0));
            }
            for (String key : appearances.keySet()) {
                Double ability = calculateWeight(appearances.get(key).p2, appearances.get(key).p1);
                Double trueAbility = calculateWeightWithLogging(key, appearancesLog);
                ability = ability * 0.6 + trueAbility;
                Skill helper = new Skill(key, ability);
                toret.add(helper);
            }
        } else {
            for (String key : appearances.keySet()) {
                Double ability = calculateWeight(appearances.get(key).p2, appearances.get(key).p1);
                Skill helper = new Skill(key, ability);
                toret.add(helper);
            }

        }
        return toret;
    }

    private Pair<Map<String, SinglePair<Double>>, Map<String, Pair<Integer, Integer>>> getAppearancesWithTime(Map<String, Pair<Integer, Integer>> stringPairMap, Map<String, Map<String, Double>> first, Map<String, Integer> loggingFrequency) throws JsonProcessingException {
        Pair<Map<String, SinglePair<Double>>, Map<String, Pair<Integer, Integer>>> res;
        Map<String, SinglePair<Double>> appearances = new HashMap<>();
        Map<String, Pair<Integer, Integer>> times = new HashMap<>();

        for (String s : stringPairMap.keySet()) {
            Map<String, Double> help = first.get(s);
            for (String sk : help.keySet()) {
                addAppearance(loggingFrequency, appearances, help, sk);
                if (times.containsKey(sk)) {
                    times.put(sk, new Pair<>(times.get(sk).getFirst() + stringPairMap.get(s).getFirst(), times.get(sk).getSecond() + stringPairMap.get(s).getSecond()));
                } else {
                    times.put(sk, new Pair<>(stringPairMap.get(s).getFirst(), stringPairMap.get(s).getSecond()));
                }
            }
        }

        res = new Pair<>(appearances, times);
        return res;
    }

    private Double calculateWeightWithLogging(String key, Pair<Map<String, SinglePair<Double>>, Map<String, Pair<Integer, Integer>>> appearancesAndTimes) {

        Map<String, SinglePair<Double>> appearances = appearancesAndTimes.getFirst();
        Map<String, Pair<Integer, Integer>> times = appearancesAndTimes.getSecond();
        Double editValue = 0.0, viewValue = 0.0;
        if (times.containsKey(key)) editValue = (double) times.get(key).getFirst();
        if (times.containsKey(key)) viewValue = (double) times.get(key).getSecond();
        Double view = -1.0;
        Double edit = -1.0;
        if (editValue != 0.0) {
            edit = appearances.get(key).p1 / appearances.get(key).p2;
            edit = edit * 0.7 + (editValue / 100) * 0.3;
        }
        if (viewValue != 0.0) {
            view = appearances.get(key).p1 / appearances.get(key).p2;
            view = view * 0.7 + (viewValue / 100) * 0.3;
        }
        Double retValue = 0.0;
        if (view != -1.0) {
            retValue = retValue + view * 0.1;
        }
        if (edit != -1.0) {
            retValue = retValue + edit * 0.3;
        }
        return retValue;
    }

    private Map<String, SinglePair<Double>> getAppearances(Set<String> oldRecs, Map<String, Map<String, Double>> allComponents, Map<String, Integer> componentFrequency) {
        Map<String, SinglePair<Double>> appearances = new HashMap<>();
        for (String s : oldRecs) {
            Map<String, Double> help = allComponents.get(s);
            for (String sk : help.keySet()) {
                addAppearance(componentFrequency, appearances, help, sk);
            }
        }
        return appearances;
    }

    private void addAppearance(Map<String, Integer> componentFrequency, Map<String, SinglePair<Double>> appearances, Map<String, Double> help, String sk) {
        if (appearances.containsKey(sk)) {
            SinglePair<Double> aux = appearances.get(sk);
            Double auxi = aux.p1 + help.get(sk);
            appearances.put(sk, new SinglePair<>(auxi, aux.p2));
        } else {
            appearances.put(sk, new SinglePair<>(help.get(sk), (double) componentFrequency.get(sk)));
        }
    }

    private Double computeAvailability(List<String> recs, Map<String, Set<String>> personRecs, Participant person, Map<String, Requirement> requirementMap, String project, Double totalHours, String organization) throws Exception {
        Set<String> requirements = personRecs.get(person.getPerson());
        List<String> intersection = new ArrayList<>(requirements);
        List<String> toRemove = new ArrayList<>(requirements);
        if (recs != null) {
            toRemove.removeAll(recs);
            intersection.removeAll(toRemove);
        } else intersection = null;
        Double hours = 0.0;
        if (intersection != null) {
            for (String s : intersection) {
                hours += extractAvailability(requirementMap.get(s).getEffort(), project, organization);
            }
        }
        return calculateAvailability(hours, totalHours);
    }

    private Double calculateAvailability(Double hours, Double i) {
        return max(0, (1 - (hours / i)));
    }

    private Double extractAvailability(Double s, String project, String organization) throws Exception {
        if (EffortRepository.findById(new ProjectSRId(project, organization)) == null) {
            throw new Exception();
        }
        Effort eff = EffortRepository.findById(new ProjectSRId(project, organization));
        return eff.getEffortMap().get(s);
    }

    private void instanciateFeatureBatch(List<String> requirement, String id, Map<String, Map<String, Double>> keywordsForReq, Map<String, Requirement> recs, Boolean withComponent, Map<String, Map<String, Double>> allComponents, String organization) {
        List<RequirementSR> reqs = new ArrayList<>();
        if (requirement != null) {
            Double size=(double) requirement.size();
            Double counter=0.0;
            for (String rec : requirement) {
                RequirementSR req = new RequirementSR(recs.get(rec), id, organization);
                ArrayList<String> aux = new ArrayList<>(keywordsForReq.get(rec).keySet());
                req.setSkills(aux);
                if (withComponent) req.setComponent(new ArrayList<>(allComponents.get(rec).keySet()));
                reqs.add(req);
                counter++;
                if (counter.intValue()%100==0) System.out.println("Requirement instantiation of project "+id+" is at "+(counter/size)*100.0+"%");
            }
        }
        RequirementSRRepository.saveAll(reqs);
    }

    private String instanciateProject(Project proj, List<Participant> participants, String organization, Boolean rake, Integer size, Boolean bugzilla, Double selectivity) {
        String id = proj.getId();
        List<String> parts = new ArrayList<>();
        for (Participant par : participants) {
            parts.add(par.getPerson());
        }
        ProjectSR projectSRTrad = new ProjectSR(new ProjectSRId(proj.getId(), organization), bugzilla, size, parts, rake);
        projectSRTrad.setSelectivity(selectivity);
        ProjectRepository.save(projectSRTrad);
        return id;
    }

    private List<Skill> computeSkillsPerson(Set<String> oldRecs, Map<String, Map<String, Double>> recs, Map<String, Integer> skillsFrequency, LoggingInformation pair, String s, Map<String, Integer> loggingFrequency) throws JsonProcessingException {
        return getSkills(oldRecs, recs, skillsFrequency, pair, s, loggingFrequency);
    }

    private Double calculateWeight(Double appearances, Double requirement) {
        if (appearances == 0) return 0.0;
        else return requirement / appearances;
    }

    private Map<String, Set<String>> getPersonRecs(BatchSchema request) {
        Map<String, Set<String>> personRecs = new HashMap<>();
        for (Responsible resp : request.getResponsibles()) {
            if (personRecs.containsKey(resp.getPerson())) {
                Set<String> aux = personRecs.get(resp.getPerson());
                aux.add(resp.getRequirement());
                personRecs.put(resp.getPerson(), aux);
            } else {
                Set<String> aux = new HashSet<>();
                aux.add(resp.getRequirement());
                personRecs.put(resp.getPerson(), aux);
            }
        }
        return personRecs;
    }

    private Map<String, List<Participant>> getParticipants(BatchSchema request) {
        Map<String, List<Participant>> participants = new HashMap<>();
        if (request.getParticipants() != null) {
            for (Participant par : request.getParticipants()) {
                if (participants.containsKey(par.getProject())) {
                    participants.get(par.getProject()).add(par);
                } else {
                    List<Participant> aux = new ArrayList<>();
                    aux.add(par);
                    participants.put(par.getProject(), aux);
                }
            }
        }
        return participants;
    }

    public List<ProjectKeywordSchema> extractKeywords(String organization, BatchSchema batch) {
        List<ProjectKeywordSchema> res = new ArrayList<>();
        for (Project j : batch.getProjects()) {
            ProjectKeywordSchema proje = new ProjectKeywordSchema();
            List<KeywordReturnSchema> reqs = new ArrayList<>();
            String id = j.getId();
            proje.setProjectId(id);
            for (RequirementSR req : RequirementSRRepository.findByOrganizationAndProj(organization, id)) {
                KeywordReturnSchema key = new KeywordReturnSchema(req.getId().getRequirementId(), new ArrayList<>(req.getSkillsSet()));
                reqs.add(key);
            }
            proje.setRequirements(reqs);
            res.add(proje);
        }
        return res;
    }

    public void undo_recommend_reject(String rejected, String user, String requirement, String organization) {
        RejectedPerson rej = RejectedPersonRepository.findByUser(new RejectedPersonId(user, organization));
        Map<String, HashSet<String>> deleted = rej.getDeleted();
        HashSet set = deleted.get(rejected);
        set.remove(requirement);
        deleted.put(rejected, set);
        rej.setDeleted(deleted);
        RejectedPersonRepository.save(rej);
    }

    public List<Skill> getPersonSkills(String person, String organization, Integer k) {
        List<PersonSR> per = PersonSRRepository.findByNameAndOrganization(person, organization);
        if (per != null && per.size() > 0) {
            PersonSR truePerson = per.get(0);
            List<Skill> skill = truePerson.getSkills();
            Collections.sort(skill,
                    Comparator.comparingDouble(Skill::getWeight).reversed());
            List<Skill> newList = new ArrayList<>();
            if (k != -1) {
                if (k > skill.size()) k = skill.size();
                for (int i = 0; i < k; ++i) {
                    newList.add(skill.get(i));
                }
            } else newList = skill;
            return newList;
        } else return null;
    }

    public List<Requirement> cleanRequirements(List<Requirement> requirements) throws IOException {
        List<Requirement> toRet = new ArrayList<>();
        for (Requirement r : requirements) {
            if (r.getDescription() != null)
                r.setDescription(pre.text_preprocess(r.getDescription()));
            if (r.getName() != null)
                r.setName(pre.text_preprocess(r.getName()));
            toRet.add(r);
        }
        return toRet;
    }

    private class SinglePair<T> {
        T p1, p2;

        SinglePair(T p1, T p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

    }


}
