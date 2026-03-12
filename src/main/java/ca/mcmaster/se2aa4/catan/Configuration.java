package ca.mcmaster.se2aa4.catan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Configuration {

    private int maxRounds;
    private int humanPlayerId;

    public Configuration() {
        this.maxRounds = 50;
        this.humanPlayerId = 0;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public int getHumanPlayerId() {
        return humanPlayerId;
    }

    public boolean isHumanGame() {
        return humanPlayerId >= 1 && humanPlayerId <= 4;
    }

    public void load(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("turns")) {
                    String[] parts = line.split("[=:]");
                    if (parts.length == 2) {
                        try {
                            int value = Integer.parseInt(parts[1].trim());
                            if (value >= 1 && value <= 8192) {
                                this.maxRounds = value;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid turns value: " + parts[1].trim());
                        }
                    }
                }
                if (line.startsWith("human")) {
                    String[] parts = line.split("[=:]");
                    if (parts.length == 2) {
                        try {
                            int value = Integer.parseInt(parts[1].trim());
                            if (value >= 0 && value <= 4) {
                                this.humanPlayerId = value;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load config: " + e.getMessage());
        }
    }
}
