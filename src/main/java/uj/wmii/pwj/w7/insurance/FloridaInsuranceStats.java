package uj.wmii.pwj.w7.insurance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FloridaInsuranceStats {
    Set<String> counties = new HashSet<>();
    double sumTiv2012 = 0;
    Map<String, Double> growth = new HashMap<>();

    void accept(InsuranceRecord r) {
        counties.add(r.county);
        sumTiv2012 += r.tiv2012;
        growth.merge(r.county, r.tiv2012 - r.tiv2011, Double::sum);
    }

    void combine(FloridaInsuranceStats other){
        counties.addAll(other.counties);
        sumTiv2012 += other.sumTiv2012;
        other.growth.forEach( (k, v) -> growth.merge(k, v, Double::sum));
    }
}