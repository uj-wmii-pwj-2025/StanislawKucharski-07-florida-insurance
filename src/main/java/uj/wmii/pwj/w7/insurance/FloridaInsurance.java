package uj.wmii.pwj.w7.insurance;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FloridaInsurance {

    public static void main(String[] args) {
        FloridaInsuranceStats stats;

        try {
            stats = readRecordsFromZip("FL_insurance.csv.zip");
        } catch (IOException ex) {
            throw new RuntimeException("Failed to access files: " + ex.getMessage(), ex);
        }



        writeSingleLine("count.txt", String.valueOf(stats.counties.size()));

        writeSingleLine("tiv2012.txt", String.format("%.2f",stats.sumTiv2012));

        Path mostValuablePath = Path.of("most_valuable.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(mostValuablePath)) {
            writer.write("country,value"); //the tests literally expect country even tho its a typo for county 
            writer.newLine();

            List<Map.Entry<String, Double>> entries = new ArrayList<>(stats.growth.entrySet());

            entries.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

            for (int i = 0; i < Math.min(10, entries.size()); i++) {
                Map.Entry<String, Double> e = entries.get(i);
                writer.write(e.getKey() + "," + String.format("%.2f", e.getValue()));
                writer.newLine();
            }

        } catch (IOException ex) {
            System.err.println("Cannot write most_valuable.txt: " + ex.getMessage());
        }
    }

    private static FloridaInsuranceStats readRecordsFromZip(String zipPath) throws IOException {
        try (ZipFile zip = new ZipFile(zipPath)) {
            ZipEntry csvEntry = zip.stream()
                    .filter(e -> !e.isDirectory())
                    .filter(e -> e.getName().endsWith(".csv"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No CSV file in ZIP"));

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(zip.getInputStream(csvEntry), StandardCharsets.UTF_8))) {
                return reader.lines()
                        .skip(1)
                        .map(InsuranceRecord::parseLine)
                        .collect(
                        FloridaInsuranceStats::new,
                        FloridaInsuranceStats::accept,
                        FloridaInsuranceStats::combine
                        );
            }
        }
    }

    private static void writeSingleLine(String filename, String value) {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filename))) {
            writer.write(value);
            writer.newLine();
        } catch (IOException ex) {
            System.err.println("Cannot write " + filename + ": " + ex.getMessage());
        }
    }
}
