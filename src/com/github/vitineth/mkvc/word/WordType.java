package com.github.vitineth.mkvc.word;

/**
 * The various types of words that are considered valid by the sentence parser.
 * <p>
 * Created by Ryan on 23/10/2016.
 */
public enum WordType {
    COORDINATING_CONJUNCTION("CC"), CARDINAL_NUMBER("CD"), DETERMINER("DT"), EXISTENIAL_THERE("EX"), FOREIGN_WORD("FW"), PREPOSITION("IN"), ADJECTIVE("JJR|JJS|JJ"), LIST_ITEM_MARKER("LS"), MODAL("MD"), NOUN("NNS|NN"), PROPER_NOUN("NNPS|NNP"), PREDETERMINER("PDT"), POSSESSIVE_ENDING("POS"), PERSONAL_PRONOUN("PRP"), POSSESSIVE_PRONOUN("PRP\\$"), ADVERB("RBR|RBS|RB"), PARTICLE("RP"), SYMBOL("SYM"), TO("TO"), INTERJECTION("UH"), VERB("VBZ|VBP|VBN|VBG|VBD|VB"), WH("WRB|WP\\$|WP|WDT"), ANY(null);

    private String matcher;

    WordType(String matcher) {
        this.matcher = matcher;
    }

    public String getMatcher() {
        return matcher;
    }

    /**
     * Matches the given type to one of the word types and if none can be found then it returns {@link #ANY}
     *
     * @param type String the name of the word
     * @return WordType the corresponding word type
     */
    public static WordType match(String type) {
        for (WordType wordType : WordType.values()) {
            if (wordType == ANY) continue;
            if (type.matches(wordType.getMatcher())) return wordType;
        }
        return ANY;
    }
}
