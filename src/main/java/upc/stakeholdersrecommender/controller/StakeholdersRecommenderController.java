package upc.stakeholdersrecommender.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.stakeholdersrecommender.domain.Schemas.*;
import upc.stakeholdersrecommender.entity.Skill;
import upc.stakeholdersrecommender.service.EffortCalculator;
import upc.stakeholdersrecommender.service.StakeholdersRecommenderService;

import java.io.IOException;
import java.util.List;


@SuppressWarnings("ALL")
@RestController
@RequestMapping("/upc/stakeholders-recommender")
@Api(value = "Stakeholders Recommender API", produces = MediaType.APPLICATION_JSON_VALUE)
public class StakeholdersRecommenderController {

    @Autowired
    StakeholdersRecommenderService stakeholdersRecommenderService;
    @Autowired
    EffortCalculator effortCalc;

    @RequestMapping(value = "batch_process", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This endpoint is used to upload the required data for making stakeholder recommendations. /n The parameter withAvailability species whether availability is calculated based on the stakeholder's past history or not. All information in the database is purged every time this method is called. A person's relation to the project is defined with PARTICIPANT (availability is expressed in hours), while the person is defined in PERSONS, the requirements in REQUIREMENTS, the project in PROJECTS, and a person's relation to a requirement in RESPONSIBLES (i.e., the person is the one in charge of the requirement)." +
", response = BatchReturnSchema.class")
    public ResponseEntity addBatch(@RequestBody BatchSchema batch, @ApiParam(value = "If set to true, the recommendations for the organization making the request will take into account the stakeholder’s availability. If set to false, the field “availability” in participant is optional.", example = "false", required=true)
    @RequestParam Boolean withAvailability,@ApiParam(value = "If set to true, the recommendations for the organization making the request will take into account the requirement’s component (which is expressed in the requirementParts field of a requirement). If set to false, it is not necessary to state the component.", example = "false", required=true)
    @RequestParam Boolean withComponent,@ApiParam(value = "The organization that is making the request.", example = "UPC", required=true)
    @RequestParam String organization ,@ApiParam(value = "If auto-mapping is used (i.e., set to true), it is not necessary to set or compute effort (i.e., to establish the mappint from effort points to hours). The mapping used in auto-mapping is a 1 to 1 mapping of effort to hours.", example = "true", required=true)
    @RequestParam Boolean autoMapping,@ApiParam(value = "If set to true, the endpoint returns each requirement with its set of keywords." ,example = "true", required=true)
    @RequestParam Boolean keywords  ) throws Exception {
        int res = 0;
        try {
            res = stakeholdersRecommenderService.addBatch(batch, withAvailability,withComponent,organization,autoMapping);
        } catch (IOException e) {
            return new ResponseEntity(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (!keywords)
        return new ResponseEntity<>(new BatchReturnSchema(res), HttpStatus.CREATED);
        else {
            List<ProjectKeywordSchema> keys=stakeholdersRecommenderService.extractKeywords(organization,batch);
            return new ResponseEntity<>(keys,HttpStatus.CREATED);
        }
    }


    @RequestMapping(value = "reject_recommendation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This endpoint is used to state that the user identied by REJECTED must not be recommended for REQUIREMENT if USER performs the recommendation for REQUIREMENT.", notes = "")
    public ResponseEntity recommendReject(@ApiParam(value = "Id of the person who is rejected.", example = "Not JohnDoe", required=true)@RequestParam("rejected") String rejected,@ApiParam(value = "Id of the person who makes the rejection.", example = "JohnDoe", required=true) @RequestParam("user") String user,@ApiParam(value = "Id of the requirement from which the person REJECTED is rejected.", example = "1", required=true) @RequestParam("requirement") String requirement
            ,@ApiParam(value = "The organization that is making the request.", example = "UPC", required=true)@RequestParam String organization ) {
        stakeholdersRecommenderService.recommend_reject(rejected, user, requirement,organization);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "recommend", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Given a REQUIREMENT in a PROJECT, asked by a USER, the stakeholder recommender service performs a recommendation and returns a list of the best K stakeholders with an appropiateness between 0 and 1(being 1 the best appropriateness) based on the historic data given in the batch_process request.", notes = "", response = RecommendReturnSchema[].class)
    public ResponseEntity<List<RecommendReturnSchema>> recommend(@RequestBody RecommendSchema request,
                                                                 @ApiParam(value = "Maximum number of stakeholders to be returned by the recommender.", example = "10",required=true)@RequestParam Integer k,@ApiParam(value = "If set to true, the recommendation only takes into account as possible set of stakeholders the ones in the project to which the requirement pertains. If set to false, this set of stakeholders will be all the stakeholders received in the batch_process of the organization that is making the request. If set to false, it takes all stakeholders with enough availability in any project and considers them. The availabilityScore of the partcipants of other projects will be always one. ", required=true,example = "false") @RequestParam Boolean projectSpecific
                                                                  ,@ApiParam(value = "The organization that is making the request.", example = "UPC", required=true)@RequestParam String organization ) throws Exception {
        List<RecommendReturnSchema> ret = stakeholdersRecommenderService.recommend(request, k, projectSpecific,organization);
        return new ResponseEntity<>(ret, HttpStatus.CREATED);
    }

    @RequestMapping(value = "setEffort", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Set the mapping of effort points to hours for an specific project. The effort points go in a scale from 1 to 5.", notes = "")
    public ResponseEntity setEffort(@RequestBody SetEffortSchema eff,@ApiParam(value = "The project in which the effort mapping should be used.", example = "1",required=true) @RequestParam String project
            ,@ApiParam(value = "The organization that is making the request.", example = "UPC", required=true)@RequestParam String organization ) throws IOException {
        effortCalc.setEffort(eff, project,organization);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping(value = "computeEffort", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This endpoint generates a mapping of effort points into hours specific to the project specified, based in the historic information given. Each requirement sohuld contain the effort stated in a scale from 1 to 5, and the hours that have been needed to complete this requirement. Taking this into account, the service computes the average of hours needed per effort point.", notes = "")
    public ResponseEntity calculateEffort(@RequestBody EffortCalculatorSchema eff,@ApiParam(value = "The project in which the effort mapping will be used in future recommendations.", example = "1",required=true) @RequestParam String project
            ,@ApiParam(value = "The organization that is making the request.", example = "UPC", required=true)@RequestParam String organization ) throws IOException {
        effortCalc.effortCalc(eff, project,organization);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping(value = "undoRejection", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This endpoint is used to state that the user identified by REJECTED will again be considered as valid to the REQUIREMENT when the person USER ask for a recommendation over this requirement.", notes = "")
    public ResponseEntity undoRejection(@ApiParam(value = "Id of the person who was rejected.", example = "Not JohnDoe", required=true)@RequestParam("rejected") String rejected,@ApiParam(value = "Id of the person who made the initial rejection.", example = "JohnDoe", required=true) @RequestParam("user") String user,@ApiParam(value = "Id of the requirement from which the person REJECTED was rejected by the person USER.", example = "1", required=true) @RequestParam("requirement") String requirement
            ,@ApiParam(value = "The organization that is making the request.", example = "UPC", required=true)@RequestParam String organization ) {
        stakeholdersRecommenderService.undo_recommend_reject(rejected, user, requirement,organization);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "getPersonSkills", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the set of skills of a person", notes = "")
    public ResponseEntity getPersonSkills(@ApiParam(value = "Id of the person.", example = "Not JohnDoe", required=true)@RequestParam("person") String person,
                                          @ApiParam(value = "The organization that is making the request.", example = "UPC", required=true)@RequestParam("organization") String organization,
                                          @ApiParam(value = "Maximum number of skills to be returned", example = "10",required=false)@RequestParam(value="k",defaultValue = "-1",required=false) Integer k) {
        List<Skill> skills=stakeholdersRecommenderService.getPersonSkills(person,organization,k);
        return new ResponseEntity<>(skills,HttpStatus.OK);
    }
}
