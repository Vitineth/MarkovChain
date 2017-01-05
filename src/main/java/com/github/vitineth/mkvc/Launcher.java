package com.github.vitineth.mkvc;

import java.io.File;
import java.io.IOException;

/**
 * The basic launcher for the program which takes 4 command line arguments: -file, -regular, -english, -timings.
 */
public class Launcher {

    /**
     * The loation of the input file
     */
    private static String inputFile;
    /**
     * The number of regular strings to generate
     */
    private static int regular = 10;
    /**
     * The number of structured strings to generate
     */
    private static int structured = 10;
    /**
     * Whether the timings should be outputted.
     */
    private static boolean timings = true;

    /**
     * Attempts to launch the rewritten markov chain program by parsing the given command line flags. The possible
     * flags are listed in {@link #printHelp()}.
     *
     * @param args String[] the jvm provided arguments
     * @throws IOException If there is an error reading the input file.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 0 && args.length % 2 != 0) {
            System.err.println("Invalid number of parameters. See help for help.");
            printHelp();
            return;
        }
        if (args.length > 6) {
            System.err.println("Too many arguments. See help for help.");
            printHelp();
            return;
        }

        for (int i = 0; i < args.length - 1; i += 2) {
            if (!processFlag(args[i], args[i + 1])) {
                printHelp();
                return;
            }
        }

        if (inputFile == null) {
            System.err.println("Cannot execute without a valid input file. See help for help");
            printHelp();
            return;
        }

        MarkovChainsRewrite rewrite = new MarkovChainsRewrite();
        rewrite.produceChain(new File(inputFile), structured > 0, regular, structured, timings);
    }

    /**
     * Processes the given key value pair for flags. It will return true or false whether the flag has been parsed
     * successfully. If the flag is unknown it will still return true which is by design.
     *
     * @param key   String the key (should be in the form -[key], for example -file)
     * @param value String the value
     * @return boolean if the given flag was processed successfully.
     */
    @SuppressWarnings("Duplicates")
    private static boolean processFlag(String key, String value) {
        System.out.println("[" + key + ", " + value + "]");
        if (key.equalsIgnoreCase("-file")) inputFile = value;
        if (key.equalsIgnoreCase("-regular")) {
            if (canParseInteger(value)) {
                int reg = Integer.parseInt(value);
                if (reg < 0) {
                    System.err.println("Cannot specify a negative value for sentence productions.");
                    return false;
                }
                regular = reg;
            } else {
                return false;
            }
        }
        if (key.equalsIgnoreCase("-english")) {
            if (canParseInteger(value)) {
                int sent = Integer.parseInt(value);
                if (sent < 0) {
                    System.err.println("Cannot specify a negative value for sentence productions.");
                    return false;
                }
                structured = sent;
            } else {
                return false;
            }
        }
        if (key.equalsIgnoreCase("-timings")) {
            if (canParseBoolean(value)) {
                timings = Boolean.parseBoolean(value);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the given value is either true or false in any case
     *
     * @param s String the string to test
     * @return boolean if the string is true or false
     */
    private static boolean canParseBoolean(String s) {
        return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false");
    }

    /**
     * Returns true if the given string can be parsed as an integer using {@link Integer#parseInt(String)}.
     *
     * @param s String the string to test
     * @return boolean if the string can be cast to an integer
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean canParseInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Prints the help for the program.
     */
    private static void printHelp() {
        System.out.println("HELP -- Markov Chain Generator (using markov.jar as the name of this jar file for demonstration)");
        System.out.println("java -jar markov.jar -file [file] -regular [n] -english [n] -timings [true/false]");
        System.out.println("Arguments: ");
        System.out.println("  -file [file] :: Specifies the input file location. Should be a plaintext file.");
        System.out.println("  -regular [n] :: Specifies the number of regular markov chain strings to produce (no sentence structuring).");
        System.out.println("  -english [n] :: Specifies the number of english markov chain strings to produce (with sentence structuring).");
        System.out.println("  -timings [true/false] :: Specifies whether timings should printed once finished.");
        System.out.println("If english is 0 then it will not do any sentence processing at all.");
    }

}
