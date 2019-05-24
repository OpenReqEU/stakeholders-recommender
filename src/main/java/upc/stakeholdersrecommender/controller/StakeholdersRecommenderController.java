package upc.stakeholdersrecommender.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.stakeholdersrecommender.domain.Requirement;
import upc.stakeholdersrecommender.domain.Responsible;
import upc.stakeholdersrecommender.domain.Schemas.*;
import upc.stakeholdersrecommender.service.BugzillaService;
import upc.stakeholdersrecommender.service.EffortCalculator;
import upc.stakeholdersrecommender.service.StakeholdersRecommenderService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/*
Llega horas por
 */


@SuppressWarnings("ALL")
@RestController
@RequestMapping("/upc/stakeholders-recommender")
@Api(value = "Stakeholders Recommender API", produces = MediaType.APPLICATION_JSON_VALUE)
public class StakeholdersRecommenderController {

    private static final Logger logger = LoggerFactory.getLogger(StakeholdersRecommenderController.class);
    @Autowired
    StakeholdersRecommenderService stakeholdersRecommenderService;
    @Autowired
    BugzillaService bugzillaService;
    @Autowired
    EffortCalculator effortCalc;


    @RequestMapping(value = "batch_process", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Batch process request to upload required data for stakeholder recommendation." +
            " \n The parameter withAvailability specifies whether a availability is calculated based on the stakeholder's past history" +
            " or not.", notes = "", response = BatchReturnSchema.class)
    public ResponseEntity addBatch(@RequestBody BatchSchema batch, @RequestParam Boolean withAvailability) throws Exception {
        int res = 0;
        try {
            res = stakeholdersRecommenderService.addBatch(batch, withAvailability);
        } catch (IOException e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(new BatchReturnSchema(res), HttpStatus.CREATED);
    }

    @RequestMapping(value = "purge", method = RequestMethod.DELETE)
    @ApiOperation(value = "Removes all data from the service database.")
    public ResponseEntity purge() {
        stakeholdersRecommenderService.purge();
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "reject_recommendation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Recommendation rejection method: used to state that the user identified by REJECTED must not be assigned to REQUIREMENT. The" +
            " rejection is performed by USER.", notes = "")
    public ResponseEntity recommend_reject(@RequestParam("rejected") String rejected, @RequestParam("user") String user, @RequestParam("requirement") String requirement) {
        stakeholdersRecommenderService.recommend_reject(rejected, user, requirement);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "recommend", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Given a REQUIREMENT in a PROJECT, asked by a USER, the Stakeholder Recommender service performs a " +
            "recommendation and returns a list of the best K stakeholders based on the historic data given in the batch_process." +
            "\n The parameter projectSpecific specifies if the recommendation takes into account all stakeholders given in the batch_process, or only those" +
            " specified in \"PARTICIPANTS\", in the batch_process", notes = "", response = RecommendReturnSchema[].class)
    public ResponseEntity<List<Responsible>> recommend(@RequestBody RecommendSchema request,
                                                       @RequestParam Integer k, @RequestParam Boolean projectSpecific) throws Exception {
        List<RecommendReturnSchema> ret = stakeholdersRecommenderService.recommend(request, k, projectSpecific);
        return new ResponseEntity(ret, HttpStatus.CREATED);
    }

    @RequestMapping(value = "deleteProject", method = RequestMethod.DELETE)
    @ApiOperation(value = "Deletes a project from the database", notes = "")
    public ResponseEntity extract(@RequestParam String id) throws IOException {
        stakeholdersRecommenderService.deleteProject(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "extractHistoric", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Extract all historic information of the bugzilla API", notes = "")
    public ResponseEntity extractBugzilla() throws Exception {
        bugzillaService.extractInfo();
        BatchSchema batch = new BatchSchema();
        batch.setPersons(bugzillaService.getPersons());
        batch.setResponsibles(bugzillaService.getResponsibles());
        batch.setRequirements(bugzillaService.getRequirements());
        batch.setParticipants(bugzillaService.getParticipants());
        batch.setProjects(bugzillaService.getProject());
        List<Requirement> corpus = new ArrayList<Requirement>();
        for (Requirement req : batch.getRequirements()) {
            corpus.add(req);
        }
        stakeholdersRecommenderService.extract2(corpus);
        KeywordExtractSchema extr = new KeywordExtractSchema();
        for (Requirement req : batch.getRequirements()) {
            RequirementDocument doc = new RequirementDocument();
            doc.setDescription(req.getDescription());
            doc.setId(req.getId());
            extr.addRequirement(doc);
        }
        return new ResponseEntity(extr, HttpStatus.OK);
    }

    @RequestMapping(value = "extractKeywords", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Extracts all  keywords of the given corpus, and outputs these giving for each requirement, its id, its description and keywords", notes = "")
    public ResponseEntity extractKeywords(@RequestBody KeywordExtractSchema extr) throws Exception {
        OutputKeywordExtraction out = stakeholdersRecommenderService.extract(extr.getRequirements());
        return new ResponseEntity(out, HttpStatus.OK);
    }

    // Añadir funciones para calcular effort historico y poner effort directamente
    @RequestMapping(value = "setEffort", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Set the mapping of effort points into hours, the effort points go in a scale from 1 to 5, the effort is specific to a project", notes = "")
    public ResponseEntity setEffort(@RequestBody SetEffortSchema eff, @RequestParam String project) throws IOException {
        effortCalc.setEffort(eff, project);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "computeEffort", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Generate a mapping of effort points into hours specific to the project specified, based in the historic information given", notes = "")
    public ResponseEntity calculateEffort(@RequestBody EffortCalculatorSchema eff, @RequestParam String project) throws IOException {
        effortCalc.effortCalc(eff, project);
        return new ResponseEntity(HttpStatus.OK);
    }


}
