/*  Name: Joshua Chen
 */

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Manages the details of EvilHangman. This class keeps
 * tracks of the possible words from a dictionary.txt during
 * rounds of hangman, based on guesses so far.
 */
public class HangmanManager {
    // Constants
    public static final String UNKNOWN_LETTER = "-";

    // Instance variables
    private Set<String> words;
    private List<String> wordsLeft;
    private boolean debugOn;
    private int guesses;
    private StringBuilder pattern;
    private List<String> letters;
    private HangmanDifficulty diff;
    private int diffCount;


    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * pre: words != null, words.size() > 0
     *
     * @param words   A set with the words for this instance of Hangman.
     * @param debugOn true if we should print out debugging to System.out.
     */
    public HangmanManager(Set<String> words, boolean debugOn) {
        if (words == null || words.isEmpty()) {
            throw new IllegalArgumentException("words is empty");
        }
        this.words = new HashSet<>(words);
        this.debugOn = debugOn;
    }


    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * Debugging is off.
     * pre: words != null, words.size() > 0
     *
     * @param words A set with the words for this instance of Hangman.
     */
    public HangmanManager(Set<String> words) {
        if (words == null || words.isEmpty()) {
            throw new IllegalArgumentException("words is empty");
        }
        this.words = new HashSet<>(words);
        this.debugOn = false;
    }


    /**
     * Get the number of words in this HangmanManager of the given length.
     * pre: none
     *
     * @param length The given length to check.
     * @return the number of words in the original Dictionary
     * with the given length
     */
    public int numWords(int length) {
        int count = 0;
        for (String word : words) {
            if (word.length() == length)
                count++;
        }
        return count;
    }


    /**
     * Get for a new round of Hangman. Think of a round as a
     * complete game of Hangman.
     *
     * @param wordLen    the length of the word to pick this time.
     *                   numWords(wordLen) > 0
     * @param numGuesses the number of wrong guesses before the
     *                   player loses the round. numGuesses >= 1
     * @param diff       The difficulty for this round.
     */
    public void prepForRound(int wordLen, int numGuesses, HangmanDifficulty diff) {
        if (wordLen <= 0 || numGuesses < 1) {
            throw new IllegalArgumentException("wordLen <= 0 || numGuesses < 1");
        }
        pattern = makeDash(wordLen);
        guesses = numGuesses;
        this.diff = diff;
        diffCount = 0;
        letters = new ArrayList<>();
        wordsLeft = new ArrayList<>();
        for (String addWord : words) {
            if (addWord.length() == wordLen)
                wordsLeft.add(addWord);
        }
    }


    /**
     * The number of words still possible (live) based on the guesses so far.
     * Guesses will eliminate possible words.
     *
     * @return the number of words that are still possibilities based on the
     * original dictionary.txt and the guesses so far.
     */
    public int numWordsCurrent() {
        return wordsLeft.size();
    }


    /**
     * Get the number of wrong guesses the user has left in
     * this round (game) of Hangman.
     *
     * @return the number of wrong guesses the user has left
     * in this round (game) of Hangman.
     */
    public int getGuessesLeft() {
        return guesses;
    }


    /**
     * Return a String that contains the letters the user has guessed
     * so far during this round.
     * The characters in the String are in alphabetical order.
     * The String is in the form [let1, let2, let3, ... letN].
     * For example [a, c, e, s, t, z]
     *
     * @return a String that contains the letters the user
     * has guessed so far during this round.
     */
    public String getGuessesMade() {
        return letters.toString();
    }


    /**
     * Check the status of a character.
     *
     * @param guess The character to check.
     * @return true if guess has been used or guessed this round of Hangman,
     * false otherwise.
     */
    public boolean alreadyGuessed(char guess) {
        for (String let : letters) {
            if (let.equals(Character.toString(guess)))
                return true;
        }
        return false;
    }


    /**
     * Get the current pattern. The pattern contains '-''s for
     * unrevealed (or guessed)
     * characters and the actual character for "correctly guessed" characters.
     *
     * @return the current pattern.
     */
    public String getPattern() {
        return pattern.toString();
    }


    /**
     * Update the game status (pattern, wrong guesses, word list),
     * based on the given guess.
     *
     * @param guess pre: !alreadyGuessed(ch), the current guessed character
     * @return return a tree map with the resulting patterns and the number of
     * words in each of the new patterns.
     * The return value is for testing and debugging purposes.
     */
    public TreeMap<String, Integer> makeGuess(char guess) {
        if (alreadyGuessed(guess)) {
            throw new IllegalArgumentException("already guessed");
        }
        String guessStr = Character.toString(guess);
        letters.add(guessStr);
        // A HashMap with different patterns as keys mapped to an ArrayList
        // with words that follow the pattern
        Map<String, List<String>> wordPats = generatePatterns(guessStr);
        // Pattern with largest or second largest number of words
        String possibleNewPat = findNewPat(wordPats);
        // Decrease guesses if the new pattern is the same, else change pattern
        if (pattern.toString().equals(possibleNewPat)) {
            guesses--;
        } else {
            pattern.delete(0, pattern.length());
            pattern.append(possibleNewPat);
        }
        wordsLeft = wordPats.get(pattern.toString());
        // Return TreeMap for debugging
        TreeMap<String, Integer> numWords = new TreeMap<>();
        for (String key : wordPats.keySet()) {
            numWords.put(key, wordPats.get(key).size());
        }
        return numWords;
    }


    /**
     * Creates a HashMap with different patterns as keys mapped to an ArrayList
     * of words that follow the pattern.
     *
     * @param guess the letter being guessed in String format
     * @return HashMap of patterns being mapped to ArrayLists containing words
     */
    private Map<String, List<String>> generatePatterns(String guess) {
        Map<String, List<String>> wordPats = new HashMap<>();
        // Iterates through the remaining words
        for (String word : wordsLeft) {
            StringBuilder newPat = new StringBuilder();
            newPat.append(pattern);
            if (word.contains(guess)) {
                // Finds occurrences of the letter in word and then changes
                // the new pattern to match
                int prevIndex = 0;
                int currIndex = word.indexOf(guess, prevIndex);
                while (currIndex != -1) {
                    newPat.deleteCharAt(currIndex);
                    newPat.insert(currIndex, guess);
                    prevIndex = currIndex + 1;
                    currIndex = word.indexOf(guess, prevIndex);
                }
            }
            // Checks if pattern already exists in wordPats and adds word to
            // the ArrayList, else add a new family if it doesn't exist
            if (wordPats.containsKey(newPat.toString())) {
                wordPats.get(newPat.toString()).add(word);
            } else {
                List<String> family = new ArrayList<>();
                family.add(word);
                wordPats.put(newPat.toString(), family);
            }
        }
        return wordPats;
    }


    /**
     * Finds the pattern with the most amount of words and then check for ties.
     * Find second hardest list if difficulty is medium or easy.
     *
     * @param wordPats the HashMap of patterns mapped to lists of words
     * @return the pattern with most number of elements in Arraylist or second
     * depending on difficulty
     */
    private String findNewPat(Map<String, List<String>> wordPats) {
        final int MEDIUM_CHECK = 4;
        final int EASY_CHECK = 2;
        int maxSize = 0;
        diffCount++;
        // Finds pattern with the most words
        String largestPat = "";
        for (String key : wordPats.keySet()) {
            int keySize = wordPats.get(key).size();
            if (keySize > maxSize) {
                largestPat = key;
                maxSize = keySize;
            } else if (keySize == maxSize) {
                largestPat = breakTie(key, largestPat);
            }
        }
        // Medium difficulty chooses second largest list every 4 guesses
        // Easy difficulty chooses second largest list every 2 guesses
        if ((diff.equals(HangmanDifficulty.MEDIUM) && diffCount % MEDIUM_CHECK == 0) ||
                (diff.equals(HangmanDifficulty.EASY) && diffCount % EASY_CHECK == 0)) {
            String secondHardest = findSecondLargest(wordPats, largestPat, maxSize);
            if (!secondHardest.isEmpty()) {
                return secondHardest;
            }
        }
        return largestPat;
    }


    /**
     * Finds the pattern with the second to most amount of words and then
     * check for ties.
     *
     * @param wordPats    the HashMap of patterns mapped to lists of words
     * @param possiblePat the largest pattern in wordPats
     * @param size        the size of the ArrayList mapped to possibleNewPat
     * @return the second most number of elements in Arraylist
     */
    private String findSecondLargest(Map<String, List<String>> wordPats,
                                     String possiblePat, int size) {
        String secondHardest = "";
        int sizeCheck = 0;
        for (String key : wordPats.keySet()) {
            int newSize = wordPats.get(key).size();
            if (newSize < size && newSize > sizeCheck) {
                secondHardest = key;
                sizeCheck = newSize;
            } else if (newSize == size && !key.equals(possiblePat)) {
                secondHardest = breakTie(key, secondHardest);
            }
        }
        return secondHardest;
    }


    /**
     * If total words is a tie pick the one that reveals the fewest characters.
     * Then, break another tie by picking the pattern that is "smallest"
     * based based on the lexicographical ordering of Strings.
     *
     * @param key the pattern being compared to in wordPats
     * @param pat the current pattern
     * @return either key or pat, whichever breaks the tie
     */
    private String breakTie(String key, String pat) {
        int countKey = countMissing(key);
        int countPat = countMissing(pat);
        if (countKey > countPat) {
            return key;
        } else if (countKey == countPat && key.compareTo(pat) < 0) {
            return key;
        }
        return pat;
    }


    /**
     * Counts the number of missing letters.
     *
     * @param str the pattern to count the missing letters
     * @return the number of missing letters
     */
    private int countMissing(String str) {
        int count = 0;
        int index = 0;
        while (str.indexOf(UNKNOWN_LETTER, index) != -1) {
            index = str.indexOf(UNKNOWN_LETTER, index) + 1;
            count++;
        }
        return count;
    }


    /**
     * Return the secret word this HangmanManager finally ended up
     * picking for this round.
     * If there are multiple possible words left one is selected at random.
     * <br> pre: numWordsCurrent() > 0
     *
     * @return return the secret word the manager picked.
     */
    public String getSecretWord() {
        if (numWordsCurrent() <= 0) {
            throw new IllegalArgumentException("numWordsCurrent() <= 0");
        }
        return wordsLeft.get((int) (Math.random() * numWordsCurrent()));
    }


    /**
     * Creates a StringBuilder that is only made of dashes
     *
     * @param length the length of the StringBuilder returned
     * @return a StringBuilder of dashes
     */
    private StringBuilder makeDash(int length) {
        StringBuilder dashes = new StringBuilder();
        for (int i = 0; i < length; i++) {
            dashes.append(UNKNOWN_LETTER);
        }
        return dashes;
    }
}