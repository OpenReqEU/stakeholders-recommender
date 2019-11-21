package upc.stakeholdersrecommender.domain;


import org.apache.commons.math3.util.Pair;

import java.util.Map;

public class LoggingInformation {

    Map<String, Map<String, Double>> first;
    Map<String, Map<String, Pair<Integer, Integer>>> second;

    public LoggingInformation(Map<String, Map<String, Double>> skills, Map<String, Map<String, Pair<Integer, Integer>>> timesForReq) {
        this.first = skills;
        this.second = timesForReq;
    }

    public Map<String, Map<String, Double>> getFirst() {
        return first;
    }

    public void setFirst(Map<String, Map<String, Double>> first) {
        this.first = first;
    }

    public Map<String, Map<String, Pair<Integer, Integer>>> getSecond() {
        return second;
    }

    public void setSecond(Map<String, Map<String, Pair<Integer, Integer>>> second) {
        this.second = second;
    }
}
