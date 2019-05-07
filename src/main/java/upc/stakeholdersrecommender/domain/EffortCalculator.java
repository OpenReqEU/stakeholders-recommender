package upc.stakeholdersrecommender.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upc.stakeholdersrecommender.domain.Schemas.EffortSchema;
import upc.stakeholdersrecommender.entity.Effort;
import upc.stakeholdersrecommender.repository.EffortRepository;

import java.util.List;

@Service
public class EffortCalculator {

    @Autowired
    EffortRepository effortRepository;

    public void effortCalc(EffortSchema eff,String id) {
        Effort aux=effortRepository.getOne(id);
        if (aux!=null) {
            effortRepository.delete(aux);
        }
        Integer total;
        Double[] effort=new Double[5];
        total = getTotal(eff.getOne());
        effort[0]=total.doubleValue()/eff.getOne().size();

        total = getTotal(eff.getTwo());
        effort[1]=total.doubleValue()/eff.getTwo().size();

        total = getTotal(eff.getThree());
        effort[2]=total.doubleValue()/eff.getThree().size();

        total = getTotal(eff.getFour());
        effort[3]=total.doubleValue()/eff.getFour().size();

        total = getTotal(eff.getFive());
        effort[4]=total.doubleValue()/eff.getFive().size();

        Effort effortMap=new Effort();
        effortMap.setEffort(effort);
        effortMap.setId(id);
        effortRepository.save(effortMap);
    }

    private Integer getTotal(List<Integer> list) {
        Integer total;
        total = 0;
        if (list==null || list.size()==0) total=1;
        else {
            for (Integer i : list) {
                total += i;
            }
        }
        return total;
    }

}
