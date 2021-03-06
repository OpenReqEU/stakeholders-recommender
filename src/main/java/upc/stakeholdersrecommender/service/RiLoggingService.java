package upc.stakeholdersrecommender.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneandone.compositejks.SslContextUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import upc.stakeholdersrecommender.domain.LoggingInformation;
import upc.stakeholdersrecommender.domain.Requirement;
import upc.stakeholdersrecommender.domain.rilogging.Log;
import upc.stakeholdersrecommender.domain.rilogging.LogArray;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.util.*;

@Service
public class RiLoggingService {
    @Autowired
    SkillExtractor skill;

    /**
     * Calls the logging service, and obtains the logging information
     * @param bugzilla Whether external keyword extraction is used
     * @param rake Whether RAKE algorithm is used for keyword extraction
     * @param organization Organization that made the original request
     * @param size Total amount of unique requirements
     * @param test Whether this is to be used for a mock test, unless testing, ignore
     * @param selectivity Tf-Idf keyword discrimination value
     * @param vogella Whether vogella is making this request
     * @return  The logging information
     */
    public LoggingInformation getUserLogging(Boolean bugzilla, Boolean rake, String organization, Integer size, Integer test, Double selectivity, Boolean vogella,Clock clock) throws GeneralSecurityException, IOException {
        LogArray log = null;
        if (test == 0) {
            SslContextUtils.mergeWithSystem("cert/lets_encrypt.jks");
            RestTemplate temp = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("7kyT5sGL8y5ax6qHJU32L4CJ");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<LogArray> res = temp.exchange("https://api.openreq.eu/ri-logging/frontend/log", HttpMethod.GET, entity, LogArray.class);
            log = res.getBody();
        } else {
            ObjectMapper map = new ObjectMapper();
            File file = new File("src/main/resources/testingFiles/RiLoggingResponse.txt");
            String jsonInString = null;
            jsonInString = FileUtils.readFileToString(file, StandardCharsets.US_ASCII);
            log = map.readValue(jsonInString, LogArray.class);
        }

        return log(log.getLogs(), bugzilla, rake, organization, size, test, selectivity, vogella,clock);
    }

    /**
     * Obtains the stakeholders who viewed or edited requirements, gives them their logs, then obtains the times for these logs that
     * each stakeholder used to edit or view
     * @param logList List of all logs obtained by calling the service
     * @param bugzilla Whether external keyword extraction is used
     * @param rake Whether RAKE algorithm is used for keyword extraction
     * @param organization Organization that made the original request
     * @param size Total amount of unique requirements
     * @param test Whether this is to be used for a mock test, unless testing, ignore
     * @param selectivity Tf-Idf keyword discrimination value
     * @param vogella Whether vogella is making this request
     * @return  The logging information
     */

    public LoggingInformation log(List<Log> logList, Boolean bugzilla, Boolean rake, String organization, Integer size, Integer test, Double selectivity, Boolean vogella,Clock clock) throws IOException {
        Map<String, List<Log>> logged = new HashMap<>();
        if (logList != null)
            for (Log l : logList) {
                if (l.getBody() != null && l.getBody().getUsername() != null && l.getBody().getRequirementId() != null) {
                    if (!logged.containsKey(l.getBody().getUsername())) {
                        ArrayList<Log> list = new ArrayList<>();
                        list.add(l);
                        logged.put(l.getBody().getUsername(), list);
                    } else {
                        List<Log> list = logged.get(l.getBody().getUsername());
                        list.add(l);
                        logged.put(l.getBody().getUsername(), list);
                    }
                }
            }
        Map<String, Map<String, Pair<Integer, Integer>>> timesForReq = new HashMap<>();
        Map<String, List<Log>> reqId = new HashMap<>();
        for (String s : logged.keySet()) {
            List<Log> toOrder = logged.get(s);
            Collections.sort(toOrder,
                    Comparator.comparingInt(Log::getUnixTime));
            for (Log l : toOrder) {
                if (reqId.containsKey(l.getBody().getRequirementId())) {
                    List<Log> auxList = reqId.get(l.getBody().getRequirementId());
                    auxList.add(l);
                    reqId.put(l.getBody().getRequirementId(), auxList);
                } else {
                    List<Log> auxList = new ArrayList<>();
                    auxList.add(l);
                    reqId.put(l.getBody().getRequirementId(), auxList);

                }
            }
            Map<String, Pair<Integer, Integer>> times = extractTimeInRequirement(toOrder);
            logged.put(s, toOrder);
            timesForReq.put(s, times);
        }
        Map<String, Requirement> trueRecs = new HashMap<>();
        for (String s : reqId.keySet()) {
            List<Log> toOrder = reqId.get(s);
            Collections.sort(toOrder,
                    Comparator.comparingInt(Log::getUnixTime));
            Requirement req = new Requirement();
            req.setId(s);
            for (int i = toOrder.size() - 1; i >= 0; --i) {
                Log lo = toOrder.get(i);
                if (req.getName() == null && req.getDescription() == null) {
                    req.setModified(new Date(lo.getUnixTime() * (long) 1000));
                } else if (req.getName() != null && req.getDescription() != null) break;
                if (req.getName() == null && lo.isName()) {
                    req.setName(lo.getDescriptionOrName());
                } else if (req.getDescription() == null && lo.isDescription()) {
                    req.setDescription(lo.getDescriptionOrName());
                }
            }
            if (req.getDescription() == null) req.setDescription("");
            if (req.getName() == null) req.setName("");
            trueRecs.put(s, req);
            reqId.put(s, toOrder);
        }
        Map<String, Map<String, Double>> skills = skill.obtainSkills(trueRecs, bugzilla, rake, organization, size, test, selectivity, vogella,clock);
        skills = skill.computeTimeFactor(trueRecs, skills, Clock.systemDefaultZone(), vogella);
        return new LoggingInformation(skills, timesForReq);
    }

    /**
     * Obtains the editing and viewing times of each requirement
     * @param toOrder Logs obtained by logging pertaining to a certain stakeholder
     * @return  Map compromised of <RequirementId,<TimeEdited,TimeViewed>>
     */

    private Map<String, Pair<Integer, Integer>> extractTimeInRequirement(List<Log> toOrder)  {
        String lastElement = "";
        String lastType = "";
        String lastValue = "";
        String lastInnertext = "";
        Integer lastTime = 0;
        Map<String, Pair<Integer, Integer>> toRet = new HashMap<>();
        for (Log l : toOrder) {
            String newType = l.getEvent_type();
            if ((lastElement.equals(l.getBody().getSrcElementclassName()) || (lastElement.equals("note-editable") && l.getBody().getSrcElementclassName().equals("note-editable or-description-active"))
                    || (lastElement.equals("note-editable or-description-active") && l.getBody().getSrcElementclassName().equals("note-editable"))) && lastType.equals("focus") && newType.equals("blur")) {
                Integer time = l.getUnixTime() - lastTime;
                if (toRet.containsKey(l.getBody().getRequirementId())) {
                    if (edited(lastValue, lastInnertext, l)) {
                        toRet.put(l.getBody().getRequirementId(), new Pair<>(time + toRet.get(l.getBody().getRequirementId()).getFirst(), toRet.get(l.getBody().getRequirementId()).getSecond()));
                    } else {
                        toRet.put(l.getBody().getRequirementId(), new Pair<>(toRet.get(l.getBody().getRequirementId()).getFirst(), toRet.get(l.getBody().getRequirementId()).getSecond() + time));
                    }
                } else {
                    if (edited(lastValue, lastInnertext, l)) {
                        toRet.put(l.getBody().getRequirementId(), new Pair<>(time, 0));
                    } else {
                        toRet.put(l.getBody().getRequirementId(), new Pair<>(0, time));
                    }
                }
            }
            lastTime = l.getUnixTime();
            lastType = l.getEvent_type();
            lastElement = l.getBody().getSrcElementclassName();
            lastValue = l.getBody().getValue();
            lastInnertext = l.getBody().getInnerText();
        }
        return toRet;
    }

    /**
     * Whether this requirement was edited or simply viewed
     * @param lastValue Last value taken by this requirement
     * @param lastInnerText Last innerText taken by this requirement
     * @param l Current log being examined
     * @return  Whether this requirement was edited or viewed
     */
    private boolean edited(String lastValue, String lastInnerText, Log l) {
        if (l.getBody().getSrcElementclassName().equals("select-dropdown")) {
            return true;
        } else if (l.getBody().getSrcElementclassName().equals("or-requirement-title form-control")) {
            return !lastValue.equals(l.getBody().getValue());
        } else if (l.getBody().getSrcElementclassName().equals("note-editable") || l.getBody().getSrcElementclassName().equals("note-editable or-description-active")) {
            return !lastInnerText.equals(l.getBody().getInnerText());
        } else return false;
    }

}
