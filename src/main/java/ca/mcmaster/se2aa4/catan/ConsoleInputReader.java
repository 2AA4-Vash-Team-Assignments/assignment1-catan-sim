package ca.mcmaster.se2aa4.catan;

import java.util.Scanner;

public class ConsoleInputReader implements HumanInputReader {

    private final Scanner scanner;

    public ConsoleInputReader() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public String readLine() {
        return scanner.hasNextLine() ? scanner.nextLine() : "";
    }

    @Override
    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }
}
