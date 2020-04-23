package upc.stakeholdersrecommender.tuning;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import upc.stakeholdersrecommender.domain.Schemas.BatchSchema;
import upc.stakeholdersrecommender.domain.Schemas.PersonMinimal;
import upc.stakeholdersrecommender.service.StakeholdersRecommenderService;

import java.io.*;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ParseCsvToOptimizationJson {

    @Autowired
    private static StakeholdersRecommenderService service;

    public static void main(String[] args) {
        String csvFile = "src/main/resources/tuningFiles/train-set.csv";
        String line = "";
        String cvsSplitBy = ",";

        String reqId = null;
        List<String> high = new ArrayList<>();
        List<String> medium = new ArrayList<>();
        List<String> low = new ArrayList<>();

        HashMap<String, HashMap<String, List<String>>> reqsInfo = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            int i = -1;

            while ((line = br.readLine()) != null) {

                if (i == -1) i = 0;


                else {

                    // use comma as separator
                    String[] fields = line.split(cvsSplitBy);

                    if (i == 0) {
                        if (reqId != null) {
                            HashMap<String, List<String>> map = new HashMap<>();
                            map.put("high", high);
                            map.put("medium", medium);
                            map.put("low", low);
                            reqsInfo.put(reqId, map);
                        }
                        reqId = fields[1];
                        high = new ArrayList<>();
                        medium = new ArrayList<>();
                        low = new ArrayList<>();

                    }

                    if (fields.length >= 8) {
                        if (fields[7].equals("High")) {
                            high.add(fields[5]);
                        } else if (fields[7].equals("Medium")) {
                            medium.add(fields[5]);
                        } else if (fields[7].equals("Low")) {
                            low.add(fields[5]);
                        }
                        if (fields.length >= 9 && !fields[8].isEmpty()) {
                            high.add(fields[8].replaceAll("\"", ""));
                        }
                    }
                    i = (i+1) % 5;
                }

            }

            HashMap<String, List<String>> map = new HashMap<>();
            map.put("high", high);
            map.put("medium", medium);
            map.put("low", low);
            reqsInfo.put(reqId, map);

            fixPersonFields(reqsInfo);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //carryOutAnalysis(reqsInfo);

    }

    private static void fixPersonFields(HashMap<String, HashMap<String, List<String>>> reqsInfo) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("Reading JSON file...");
        BatchSchema batchSchema = mapper.readValue(new File("src/main/resources/tuningFiles/batch-process-evaluation-merged.json"), BatchSchema.class);
        System.out.println("JSON file read");

        HashMap<String,String> dictionary = new HashMap<>();

        for (String req : reqsInfo.keySet()) {
            for (String level : reqsInfo.get(req).keySet()) {
                List<String> usernames = new ArrayList<>();
                for (int i = 0; i < reqsInfo.get(req).get(level).size(); ++i) {
                    String user = reqsInfo.get(req).get(level).get(i);
                    boolean found = false;
                    int j = 0;
                    while (j < batchSchema.getPersons().size() && !found) {
                        if (batchSchema.getPersons().get(j).getName() != null &&
                                batchSchema.getPersons().get(j).getName().equals(user)) found = true;
                        else ++j;
                    }

                    if (found) {
                        usernames.add(batchSchema.getPersons().get(j).getUsername());
                    } else if (dictionary.keySet().contains(user)) {
                        usernames.add(dictionary.get(user));
                    }
                    else {
                        while (!found) {
                            System.out.println("WARNING! Person with name " + user + " could not be found. Please input username:");
                            Scanner in = new Scanner(System.in);
                            String s = in. nextLine();
                            int k = 0;
                            while (k < batchSchema.getPersons().size() && !found) {
                                if (batchSchema.getPersons().get(k).getUsername().equals(s)) {
                                    found = true;
                                    usernames.add(s);
                                    dictionary.put(user, s);
                                }
                                else ++k;
                            }
                        }
                    }
                }
                reqsInfo.get(req).put(level, usernames);
            }
        }

        StringBuilder sb = new StringBuilder();
        //We group requirements in pairs of high level recommendations & low level recommendations
        for (String reqId : reqsInfo.keySet()) {
            HashMap<String, List<String>> reqAssignations = reqsInfo.get(reqId);
            //We check if we have at least 1 high assignation and 1 low assignation
            if (reqAssignations.get("high").size() > 0 && reqAssignations.get("low").size() > 0) {
                //If so, we build triplets with reqId,high,low
                for (String high : reqAssignations.get("high")) {
                    for (String low : reqAssignations.get("low")) {
                        sb.append(reqId + "," + high + "," + low + "\n");
                    }
                }
            }
            //If there aren't both high and low, we try to optimize high against medium
            else if (reqAssignations.get("high").size() > 0 && reqAssignations.get("medium").size() > 0) {
                //If so, we build triplets with reqId,high,low
                for (String high : reqAssignations.get("high")) {
                    for (String medium : reqAssignations.get("medium")) {
                        sb.append(reqId + "," + high + "," + medium + "\n");
                    }
                }
            }
            //If there aren't both high and medium, we try to optimize medium against low
            else if (reqAssignations.get("medium").size() > 0 && reqAssignations.get("low").size() > 0) {
                //If so, we build triplets with reqId,high,low
                for (String medium : reqAssignations.get("medium")) {
                    for (String low : reqAssignations.get("low")) {
                        sb.append(reqId + "," + medium + "," + low + "\n");
                    }
                }
            }
        }

        //Store json with all assignations
        mapper.writeValue(new File("src/main/resources/tuningFiles/optimization-results.json"), reqsInfo);
        //Store csv file with triplets
        try (FileOutputStream oS = new FileOutputStream(new File("src/main/resources/tuningFiles/triplets.csv"))) {
            oS.write(sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void carryOutAnalysis(HashMap<String, HashMap<String, List<String>>> reqsInfo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("Reading JSON file...");
            BatchSchema batchSchema = mapper.readValue(new File("src/main/resources/tuningFiles/batch-process-evaluation-merged.json"), BatchSchema.class);
            System.out.println("JSON file read");
            System.out.println("Running batch process...");
            int processedEntities = service.addBatch(batchSchema, true, true, "UPC-optimization", true,
                    false, false, 0, -1., Clock.systemDefaultZone());
            System.out.println("Batch process run. Processed entities = " + processedEntities);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
