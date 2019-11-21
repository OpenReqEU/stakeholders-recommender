package upc.stakeholdersrecommender.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upc.stakeholdersrecommender.domain.Schemas.EffortCalculatorSchema;
import upc.stakeholdersrecommender.domain.Schemas.EffortHour;
import upc.stakeholdersrecommender.domain.Schemas.RequirementBasic;
import upc.stakeholdersrecommender.domain.Schemas.SetEffortSchema;
import upc.stakeholdersrecommender.entity.Effort;
import upc.stakeholdersrecommender.entity.ProjectSRId;
import upc.stakeholdersrecommender.repository.EffortRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EffortCalculator {
    @Autowired
    EffortRepository effortRepository;

    /**
     * Computer the effort to hour ratio for the specified project from the specified organization
     * @param id Project id
     * @param organization Organization that made the original request
     * @param eff Effort to hour conversion value
     */

    public void effortCalc(EffortCalculatorSchema eff, String id, String organization) {
        if (effortRepository.findById(new ProjectSRId(id, organization)) != null) {
            effortRepository.deleteById(new ProjectSRId(id, organization));
        }
        Map<Double, List<Double>> auxiliar = new HashMap<>();
        for (RequirementBasic req : eff.getRequirements()) {
            if (auxiliar.containsKey(req.getEffort())) {
                List<Double> intList = auxiliar.get(req.getEffort());
                intList.add(req.getHours());
                auxiliar.put(req.getEffort(), intList);
            } else {
                List<Double> intList = new ArrayList<>();
                intList.add(req.getHours());
                auxiliar.put(req.getEffort(), intList);
            }
        }
        HashMap<Double, Double> effortMap = new HashMap<>();
        for (Double s : auxiliar.keySet()) {
            List<Double> d = auxiliar.get(s);
            Double count = 0.0;
            for (Double dub : d) {
                count += dub;
            }
            Double average = count / d.size();
            effortMap.put(s, average);
        }
        Effort effort = new Effort();
        effort.setEffortMap(effortMap);
        effort.setId(new ProjectSRId(id, organization));
        effortRepository.save(effort);
    }


    /**
     * Sets the effort to hour ratio for the specified project from the specified organization
     * @param id Project id
     * @param organization Organization that made the original request
     * @param set Effort to hour conversion value
     */

    public void setEffort(SetEffortSchema set, String id, String organization) {
        if (effortRepository.findById(new ProjectSRId(id, organization)) != null) {
            effortRepository.deleteById(new ProjectSRId(id, organization));
        }
        HashMap<Double, Double> effortMap = new HashMap<>();
        for (EffortHour r : set.getEffortToHour()) {
            effortMap.put(r.getEffort(), r.getHours());
        }
        Effort effort = new Effort();
        effort.setEffortMap(effortMap);
        effort.setId(new ProjectSRId(id, organization));
        effortRepository.save(effort);
    }


}
