package ca.mcmaster.se2aa4.catan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Configuration {

    private int maxRounds;

    public Configuration() {
        this.maxRounds = 50;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public void load(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("turns")) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        int value = Integer.parseInt(parts[1].trim());
                        if (value >= 1 && value <= 8192) {
                            this.maxRounds = value;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load config: " + e.getMessage());
        }
    }
}
