package upc.stakeholdersrecommender.tuning;

import com.fasterxml.jackson.databind.ObjectMapper;
import upc.stakeholdersrecommender.domain.Participant;
import upc.stakeholdersrecommender.domain.Person;
import upc.stakeholdersrecommender.domain.Requirement;
import upc.stakeholdersrecommender.domain.Responsible;
import upc.stakeholdersrecommender.domain.Schemas.BatchSchema;
import upc.stakeholdersrecommender.domain.Schemas.PersonMinimal;

import java.io.File;

public class MergeBatchProcesses {

    public static void main(String[] args) {

        ObjectMapper mapper = new ObjectMapper();

        //Object to JSON in file
        try {
            BatchSchema batchSchema1 = mapper.readValue(new File("src/main/resources/tuningFiles/batch-process-evaluation.json"), BatchSchema.class);
            BatchSchema batchSchema2 = mapper.readValue(new File("src/main/resources/tuningFiles/batch-process-evaluation-PDE.json"), BatchSchema.class);

            for (PersonMinimal p : batchSchema2.getPersons()) {
                if (!batchSchema1.getPersons().contains(p)) {
                    batchSchema1.getPersons().add(p);
                }
            }

            for (Responsible r : batchSchema2.getResponsibles()) {
                if (!batchSchema1.getResponsibles().contains(r)) {
                    batchSchema1.getResponsibles().add(r);
                }
            }

            for (Requirement r : batchSchema2.getRequirements()) {
                if (!batchSchema1.getRequirements().contains(r)) {
                    batchSchema1.getRequirements().add(r);
                    batchSchema1.getProjects().get(0).getSpecifiedRequirements().add(r.getId());
                }
            }

            for (Participant p : batchSchema2.getParticipants()) {
                if (!batchSchema1.getParticipants().contains(p)) {
                    batchSchema1.getParticipants().add(p);
                }
            }

            mapper.writeValue(new File("src/main/resources/tuningFiles/batch-process-evaluation-merged.json"), batchSchema1);
            //TODO merge
            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
