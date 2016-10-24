package com.github.vitineth.mkvc.word;

/**
 * Data about each word that follows another containing the number of times that it follows, the total number of times
 * that the word has been followed and the probability of the word following the first.
 * <br>
 * Created by Ryan on 23/10/2016.
 */
public class WordData {

    /**
     * The first word
     */
    private String word;
    /**
     * The word following 'word'
     */
    private String followedBy;
    /**
     * The number of times 'word' is followed by 'followedBy'
     */
    private int count;
    /**
     * The total number of times 'word' is followed by another word.
     */
    private int total;
    /**
     * The probability of 'followedBy' following 'word' calculated though <code>(double)count /(double)total</code>
     */
    private double probability;

    public WordData(String word, String followedBy, int count, int total) {
        this.word = word;
        this.followedBy = followedBy;
        this.count = count;
        this.total = total;
        this.probability = (double) count / (double) total;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getFollowedBy() {
        return followedBy;
    }

    public void setFollowedBy(String followedBy) {
        this.followedBy = followedBy;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
