package com.github.vitineth.mkvc;

import com.github.vitineth.mkvc.util.output.Logger;
import com.github.vitineth.mkvc.util.time.SegmentedTimerTool;
import com.github.vitineth.mkvc.util.time.TimerTool;
import com.github.vitineth.mkvc.word.WordData;
import com.github.vitineth.mkvc.word.WordType;
import javafx.util.Pair;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The main executor for generating Markov Chains. You can use the
 * {@link #produceChain(String, boolean, int, int, boolean)} or {@link #produceChain(File, boolean, int, int, boolean)}
 * which will parse the given input and print out the markov chain that it produces.
 * Created by Ryan on 22/10/2016.
 */
public class MarkovChainsRewrite {

    /**
     * Shorthand method to print a message to the console using the {@link Logger#debug(String, String)} method with
     * {@link Class#getSimpleName()} as the label which should resolve to <code>MarkovChainsRewrite</code>.
     *
     * @param message String the message ot output.
     */
    private void debug(String message) {
        Logger.debug(getClass().getSimpleName(), message);
    }

    /**
     * Splits the given data into sentences using the {@link SentenceDetectorME} returning the value from
     * {@link SentenceDetectorME#sentDetect(String)}
     *
     * @param data String the input data
     * @return String[] the sentences
     * @throws IOException If there was an error reading from the model.
     */
    private String[] processToSentences(String data) throws IOException {
        InputStream sentenceModelInput = getClass().getResourceAsStream("/opennlp/en-sent.bin");
        SentenceModel sentenceModel = new SentenceModel(sentenceModelInput);
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceModel);

        return sentenceDetector.sentDetect(data);
    }

    /**
     * Attempts to parse the given sentences into {@link WordType} and into a list of sentences formed from the Word
     * Types.
     *
     * @param sentences String[] the list of sentences to parse
     * @return Pair[HashMap[String, WordType], List[WordType[]]] A pair of the word type map and the lsit of sentences.
     * @throws IOException if there is an error reading the model.
     */
    private Pair<HashMap<String, WordType>, List<WordType[]>> parseInputSyntax(String[] sentences) throws IOException {
        InputStream parserModelInput = getClass().getResourceAsStream("/opennlp/en-parser-chunking.bin");
        ParserModel parserModel = new ParserModel(parserModelInput);

        Parser parser = ParserFactory.create(parserModel);
        Pattern pattern = Pattern.compile("\\((CC|CD|DT|EX|FW|IN|JJR|JJS|JJ|LS|MD|NNPS|NNP|NNS|NN|PDT|POS|PRP\\$|PRP|RBR|RBS|RB|RP|SYM|TO|UH|VBD|VBG|VBN|VBP|VBZ|VB|WDT|WP\\$|WP|WRB) (.+?)\\)");

        HashMap<String, WordType> wordTypeMap = new HashMap<>();
        List<WordType[]> sentenceList = new ArrayList<>();

        for (String sentence : sentences) {
            Parse parse = ParserTool.parseLine(sentence, parser, 1)[0];
            StringBuffer buffer = new StringBuffer();

            parse.show(buffer);
            String map = buffer.toString();

            Matcher matcher = pattern.matcher(map);
            List<WordType> activeSentence = new ArrayList<>();

            while (matcher.find()) {
                WordType type = WordType.match(matcher.group(1));
                activeSentence.add(type);
                wordTypeMap.put(matcher.group(2), type);
            }

            if (activeSentence.size() > 0)
                sentenceList.add(activeSentence.toArray(new WordType[activeSentence.size()]));
        }

        return new Pair<>(wordTypeMap, sentenceList);
    }

    /**
     * Count the number of times each word follows another word from the given list of words.
     *
     * @param segments String[] the string array of words or data segments.
     * @param parts    List[String] a list of each word followed by the next in the sequence
     * @return int[][] the count data.
     */
    private int[][] countFollows(String[] segments, List<String> parts) {
        //Get a map of each occurance and the count as a long. (This is a magic line of code from stack overflow, I
        //don't really get this yet. Lamdas are strange. http://stackoverflow.com/questions/505928/how-to-count-the-number-of-occurrences-of-an-element-in-a-list)
        Map<String, Long> counts = parts.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        //Store the last printed percentage so that we only print it once instead of spamming it.
        int lastPrinted = -1;
        //Create a blank array to begin with
        int[][] followCount = new int[segments.length][segments.length];
        for (int a = 0; a < segments.length; a++) {
            for (int b = 0; b < segments.length; b++) {
                //Get the count value from the map and cast it to an integer.
                if (counts.containsKey(segments[a] + " " + segments[b]))
                    followCount[a][b] = counts.get(segments[a] + " " + segments[b]).intValue();
                else
                    followCount[a][b] = 0;
            }

            int percentage = (int) (((double) a / (double) segments.length) * 100d);
            if (percentage % 10 == 0 && percentage != lastPrinted) {
                debug(percentage + "% done.");
                lastPrinted = percentage;
            }
        }

        //Return the newly assembled array.
        return followCount;
    }

    /**
     * Produce and print the given number of markov chains to the console.
     *
     * @param inputData       String the data to process to make the markov chains from
     * @param sentenceParsing boolean whether to parse the given string for sentences and word types
     * @param regular         int the number of regular markov chains to produce
     * @param sentence        int the number of structured markov chains to produce
     * @param timings         boolean whether to print the timings used
     */
    public void produceChain(String inputData, boolean sentenceParsing, int regular, int sentence, boolean timings) {
        SegmentedTimerTool timerTool = new SegmentedTimerTool(TimerTool.Precision.MILLISECOND);
        debug("Segmenting data");
        timerTool.start();
        String[] segments = getSegments(inputData);
        timerTool.lap("Segmenting data");

        String[] sentences = null;
        HashMap<String, WordType> wordMap = null;
        List<WordType[]> sentenceOrders = null;
        if (sentenceParsing) {
            debug("Parsing sentences");
            timerTool.start();
            try {
                sentences = processToSentences(inputData);
            } catch (IOException e) {
                e.printStackTrace();
            }
            timerTool.lap("Parsing sentences");

            debug("Parsing word types");
            timerTool.start();
            try {
                Pair<HashMap<String, WordType>, List<WordType[]>> pair = parseInputSyntax(sentences);
                wordMap = pair.getKey();
                sentenceOrders = pair.getValue();
            } catch (IOException e) {
                e.printStackTrace();
            }
            timerTool.lap("Parsing word types");

        }

        debug("Creating index");
        timerTool.start();
        //A matrix containing how many times a word follows another. It uses the mappings in segments for the index. Eg.
        //     |A    |dog  |jumps|over |a    |box  |
        //A    |0    |1    |0    |0    |0    |1    |
        //dog  |0    |0    |1    |0    |0    |0    |
        //jumps|0    |0    |0    |1    |0    |0    |
        //over |0    |0    |0    |0    |1    |0    |
        //a    |0    |0    |0    |0    |0    |1    |
        //box  |0    |0    |0    |0    |0    |0    |
        //
        //followCount[a][b] means segments[a] followed by segments[b]
        //As we are checking how many times a word follows another, we can assemble an arraylist of each word followed
        //by the next word and count the number of times that an entry appears to see how many times a word follows
        //another. First we assemble the arraylist of each word with its subsequent value.
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < segments.length - 1; i++) {
            parts.add(segments[i] + " " + segments[i + 1]);
        }
        timerTool.lap("Creating index");

        debug("Counting follows");
        timerTool.start();
        //Then count the follows.
        int[][] followCount = countFollows(segments, parts);
        timerTool.lap("Counting follows");

        debug("Totalling rows");
        timerTool.start();
        //As each row represents that word followed by each in the array, the total of all values in one row will give
        //the total number of times that the word has another word following after it. This allows us to generate a
        //decimal value of likelihood.
        int[] rowTotals = new int[segments.length];
        for (int a = 0; a < segments.length; a++) {
            int count = 0;
            for (int b = 0; b < segments.length; b++) {
                count += followCount[a][b];
            }
            rowTotals[a] = count;
        }
        timerTool.lap("Totalling rows");

        debug("Reducing data store");
        timerTool.start();
        //To reduce the amount of data that we have to store we can eliminate all records of words that do not follow
        //each other which is simply equal to those with a follow count of 0.
        List<WordData> wordData = new ArrayList<>();
        for (int a = 0; a < segments.length; a++) {
            for (int b = 0; b < segments.length; b++) {
                if (followCount[a][b] != 0) {
                    wordData.add(new WordData(
                            segments[a],
                            segments[b],
                            followCount[a][b],
                            rowTotals[a]
                    ));
                }
            }
        }
        timerTool.lap("Reducing data store");

        debug("Generating chain x" + regular);
        timerTool.start();
        for (int i = 0; i < 10; i++) {
            System.out.println(getMarkovChain(wordData, 100));
        }
        timerTool.lap("Generating chain x" + regular);

        if (sentenceParsing) {
            debug("Generating english chain x" + sentence);
            timerTool.start();
            for (int i = 0; i < 10; i++) {
                System.out.println(getMarkovChain(wordData, 3, wordMap, sentenceOrders));
            }
        }
        timerTool.lap("Generating english chain x" + sentence);

        timerTool.end();
        if (timings) timerTool.printStatistics();
    }

    /**
     * Produce and print the given number of markov chains to the console. Reads the file and passes it along to
     * {@link #produceChain(String, boolean, int, int, boolean)} to make the actual output.
     *
     * @param inputFile       File the file containing the data to process to make the markov chains from
     * @param sentenceParsing boolean whether to parse the given string for sentences and word types
     * @param regular         int the number of regular markov chains to produce
     * @param sentence        int the number of structured markov chains to produce
     * @param timings         boolean whether to print the timings used
     */
    public void produceChain(File inputFile, boolean sentenceParsing, int regular, int sentence, boolean timings) throws IOException {
        produceChain(loadFile(inputFile), sentenceParsing, regular, sentence, timings);
    }

    /**
     * Returns a markov chain generated through {@link #getMarkovChain(List, String, int)} with the seed being generated
     * through {@link #getSeed(List, Random)} meaning that it should not start with a character.
     *
     * @param data    List[WordData] all complete word data
     * @param maximum int the number of words to generate
     * @return String the markov chain
     * @see #getMarkovChain(List, String, int)
     */
    private String getMarkovChain(List<WordData> data, int maximum) {
        Random random = new Random();
        String seed = getSeed(data, random);
        return getMarkovChain(data, seed, maximum);
    }

    /**
     * Generate a markov chain using the given generated WordDatas and the given seed. Maximum defines the maximum
     * number of words that should be generated.
     *
     * @param wordData List[WordData] the generated word datas
     * @param seed     String the seed word
     * @param maximum  int the maximum number of words to generate.
     * @return String the generated string
     */
    private String getMarkovChain(List<WordData> wordData, String seed, int maximum) {
        Random random = new Random();

        StringBuilder chainBuilder = new StringBuilder();
        chainBuilder.append(seed);

        int count = 1;
        List<WordData> options = getFollowOptions(wordData, seed);
        while (options.size() != 0 && count <= maximum) {
            int option = getWeightedResult(random, options);
            if (option == -1) break;

            WordData selected = options.get(option);
            if (selected.getFollowedBy().trim().length() == 1 && !Character.isAlphabetic(selected.getFollowedBy().trim().charAt(0))) {
                chainBuilder.append(selected.getFollowedBy());
            } else {
                chainBuilder.append(" ").append(selected.getFollowedBy());
            }

            count++;

            options = getFollowOptions(wordData, selected.getFollowedBy());
        }

        return chainBuilder.toString();
    }

    /**
     * Returns a valid seed from the given data. It will return a value that is not a single non-alphabetic character.
     *
     * @param data   list[WordData] A list of all possible words
     * @param random Random a random instance to be used to generate the random seed
     * @return String the generated seed
     */
    private String getSeed(List<WordData> data, Random random) {
        String word = data.get(random.nextInt(data.size())).getWord();
        while (word.trim().length() == 1 && !Character.isAlphabetic(word.trim().charAt(0)))
            word = data.get(random.nextInt(data.size())).getWord();

        return word;
    }

    /**
     * Generate a markov chain taking into consideration the sentence structures of the input data. This takes a map
     * of words and their corrosponding word types as well as each sentence structure as a list. It will attempt to make
     * the given number of sentences.
     *
     * @param wordDatas       List[WordData] the list of all word data available
     * @param maximum         int the number of sentences to produce
     * @param wordTypeHashMap HashMap[String, WordType] the map of words with their corrosponding types. it does not
     *                        have to contain all words in the word data map.
     * @param sentences       List[WordType[]] The list of sentence structures
     * @return String the generated markov chain
     */
    private String getMarkovChain(List<WordData> wordDatas, int maximum, HashMap<String, WordType> wordTypeHashMap, List<WordType[]> sentences) {
        Random random = new Random();

        StringBuilder chainBuilder = new StringBuilder();
        for (int i = 0; i < maximum; i++) {
            WordType[] selected = sentences.get(random.nextInt(sentences.size()));
            String seed = getSeed(wordDatas, random);

            List<WordData> options = getFollowOptions(wordDatas, wordTypeHashMap, seed, selected[0], false);
            for (int j = 0; j < selected.length - 1; j++) {
                if (options.size() == 0)
                    options = getFollowOptions(wordDatas, wordTypeHashMap, seed, selected[0], true);
                if (options.size() == 0) {
                    chainBuilder.append(" ").append(wordDatas.get(random.nextInt(wordDatas.size())));
                    continue;
                }

                int option = getWeightedResult(random, options);
                String word = options.get(option).getFollowedBy();

                chainBuilder.append(" ").append(word);

                options = getFollowOptions(wordDatas, wordTypeHashMap, word, selected[j + 1], false);
                seed = word;
            }

            chainBuilder.append(".");
        }

        return chainBuilder.toString();
    }

    /**
     * Returns a pseudo-random selection out of the list of WordDatas with each being valued based on their
     * probabilities. Those with a higher probability will be more likely to be selected by this function.
     *
     * @param random  Random the random object to use to pick the option.
     * @param options List[WordData] the list of options that can follow a word.
     * @return int the index of options that has been selected
     */
    private static int getWeightedResult(Random random, List<WordData> options) {
        //If we only have one option then return 0 for the first option.
        if (options.size() == 1) return 0;

        //Create a list of all options with each option appearing 100 times their probability. This means that on a
        //random selection, those with a greater chance of being selected will appear more in the list.
        List<Integer> optionIndices = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            WordData wd = options.get(i);
            for (int j = 0; j < (int) (wd.getProbability() * 1000d); j++) {
                optionIndices.add(i);
            }
        }

        if (optionIndices.size() == 0) return -1;
        return optionIndices.get(random.nextInt(optionIndices.size()));
    }

    /**
     * Returns all the possible WordDatas that can follow the given word.
     *
     * @param data List[WordData] All available word datas
     * @param word String the word
     * @return List[WordData] the WordDatas that can follow on from the given word.
     */
    private List<WordData> getFollowOptions(List<WordData> data, String word) {
        List<WordData> matches = new ArrayList<>();
        for (WordData wd : data) {
            if (wd.getWord().equals(word)) matches.add(wd);
        }
        return matches;
    }

    /**
     * Returns all the possible WordDatas that can follow the given word with the given type.
     *
     * @param data  List[WordData] All available words and their data.
     * @param types HashMap[String, WordType] A hashmap of all words and their corrosponding types
     * @param word  String the given word
     * @param type  WordType the type that is required
     * @return List[WordData] the available options
     */
    private List<WordData> getFollowOptions(List<WordData> data, HashMap<String, WordType> types, String word, WordType type, boolean permitNonMapped) {
        List<WordData> output = new ArrayList<>();
        List<WordData> options = getFollowOptions(data, word);
        for (WordData wd : options) {
            if (types.containsKey(wd.getFollowedBy())) {
                output.add(wd);
            } else {
                if (permitNonMapped) output.add(wd);
            }
        }
        return output;
    }

    /**
     * Returns the number of times <code>word</code> is followed by <code>followedBy</code> in <code>rawData</code>.
     *
     * @param rawData    String[] the full set of data.
     * @param word       String the first word
     * @param followedBy String the second word
     * @return int the number of times word is followed by followedBy in the original data.
     */
    private int getFollowCount(String[] rawData, String word, String followedBy) {
        int count = 0;
        for (int i = 0; i < rawData.length - 1; i++) {
            if (rawData[i].equals(word) && rawData[i + 1].equals(followedBy)) count++;
        }
        return count;
    }

    /**
     * Splits data according to the regex '\s+|(?=\p{Punct})|(?<=\p{Punct})', trims each segment to remove any leading
     * and trailing whitespace and finally reduces it to lower case before returning it.
     *
     * @param data String the input data
     * @return String[] the split data with removed leading and trailing whitespace in lowercase.
     */
    private String[] getSegments(String data) {
        String[] segments = data.split("\\s+|(?=\\p{Punct})|(?<=\\p{Punct})");
        for (int i = 0; i < segments.length; i++) segments[i] = segments[i].trim().toLowerCase();
        return segments;
    }

    /**
     * Loads and returns the contents of a file using the correct reading method (loading into buffer not available)
     *
     * @param input File the input file object
     * @return String the content of the file
     * @throws IOException If there is an error opening the stream or reading from the file.
     */
    private String loadFile(File input) throws IOException {
        InputStream is = new FileInputStream(input);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        while (is.read(buffer) >= 0) {
            baos.write(buffer);
        }

        return new String(baos.toByteArray());
    }

    /**
     * Convert a given hashmap to string in the form {[KEY, VALUE], [KEY, VALUE], ...}.
     *
     * @param data HashMap[T, K] the given hashmap
     * @param <T>  The key type
     * @param <K>  The value type
     * @return String the hashmap in string form.
     */
    private <T, K> String hashMapToString(HashMap<T, K> data) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (T key : data.keySet()) {
            builder.append("[\"").append(key).append("\", \"").append(data.get(key)).append("\"], ");
        }
        return builder.toString().substring(0, builder.length() - 1) + "}";
    }

}

