package uj.wmii.pwj.w7.insurance;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FloridaInsurance {

    public static void main(String[] args) {
        List<InsuranceRecord> records;

        try {
            records = readRecordsFromZip("FL_insurance.csv.zip");
        } catch (IOException ex) {
            throw new RuntimeException("Failed to access files: " + ex.getMessage(), ex);
        }

        FloridaInsuranceStats stats = records.stream().collect(
                FloridaInsuranceStats::new,
                FloridaInsuranceStats::accept,
                FloridaInsuranceStats::combine
        );

        writeSingleLine("count.txt", String.valueOf(stats.counties.size()));

        writeSingleLine("tiv2012.txt", String.format("%.2f",stats.sumTiv2012));

        Path mostValuablePath = Path.of("most_valuable.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(mostValuablePath)) {
            writer.write("country,value"); // <- TYPO in header (in tests and README !) <- (county not country)
            writer.newLine();

            stats.growth.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                    .limit(10)
                    .forEach(e -> {
                        try {
                            writer.write(e.getKey() + "," + String.format("%.2f", e.getValue()));
                            writer.newLine();
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    });
        } catch (IOException ex) {
            System.err.println("Cannot write most_valuable.txt: " + ex.getMessage());
        }
    }

    private static List<InsuranceRecord> readRecordsFromZip(String zipPath) throws IOException {
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
                        .toList();
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
