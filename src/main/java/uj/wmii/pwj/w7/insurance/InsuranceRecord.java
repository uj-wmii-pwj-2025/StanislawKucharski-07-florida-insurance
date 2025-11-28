package uj.wmii.pwj.w7.insurance;

public class InsuranceRecord{
    String county;
    double tiv2011;
    double tiv2012;

    public InsuranceRecord(String s, double v, double v1) {
        county = s;
        tiv2011 = v;
        tiv2012 = v1;
    }

    public static InsuranceRecord parseLine(String line) {
        String[] p = line.split(",");

        return new InsuranceRecord(
                p[2],
                Double.parseDouble(p[7]),
                Double.parseDouble(p[8])
        );
    }

}
