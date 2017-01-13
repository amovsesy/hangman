import api.Api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Aleks on 1/12/17.
 */
public class Solution2 {
    public static Map<Integer, int[]> characterTotalsByLength = new HashMap<>();
    public static Map<Integer, List<String>> countToWords = new HashMap<>();
    public static String[] phraseParts;
    public static Set<Character> guessedCharacters = new HashSet<>();

    public static void playHangman() {
        // Show an example usage of the API. This creates a new game and makes
        // three guesses, showing the game state response after each call.

        int numWon = 0;

        readFileAndSetupDictionary();

        for (int i = 0; i < 1000; i++) {
            guessedCharacters.clear();
            Api.GameResponse response = Api.sendNewGameRequest("test@test.com");

            Character[] frequencyOfLetters = getOrderedFrequencyOfLetters(response);

            for (int j = 25; response.state.equals("alive") && j >= 0;) {
                char nextCharacterToGuess = getNextCharacterToGuess(response, frequencyOfLetters, j);
//        System.out.println(nextCharacterToGuess);
                response = Api.sendGuessRequest(response.game_key, nextCharacterToGuess);
                System.out.println(response);

                String[] currentParts = response.phrase.split(" ");
                for (; i < currentParts.length; i++) {
                    if (currentParts[i].split("[^_]").length > 1) {
                        break;
                    }
                }

                if (i == currentParts.length) {
                    j--;
                }

                numWon += (response.state.equals("won") ? 1 : 0);
            }
        }

        System.out.println("Winning percent = " + (numWon / 1000.0));
    }

    private static char getNextCharacterToGuess(Api.GameResponse response,
                                                Character[] frequencyOfLetters, int currentIndex) {
        String[] currentParts = response.phrase.split(" ");

        int i = 0;
        for (; i < currentParts.length; i++) {
            if (currentParts[i].split("[^_]").length > 1) {
                System.out.println(currentParts[i]);
                break;
            }
        }

        if (i == currentParts.length) {
            guessedCharacters.add(frequencyOfLetters[currentIndex]);
            return frequencyOfLetters[currentIndex];
        }

        // go through parts and see if not full word guessed, but part guessed
        for (i = 0; i < currentParts.length; i++) {
            phraseParts[i] = currentParts[i];
        }

        for (i=0; i < currentParts.length; i++) {
            if (currentParts[i].contains("_")) {
                break;
            }
        }


        // take length of that and get all words for that length
        // narrow down to possible words
        // get frequency counts per position, guess first missing position based on
        // frequency and not guessed
        String regex = currentParts[i].replace("_", ".");
        List<String> possibleWords = new ArrayList<>();
        List<String> allWords = countToWords.get(regex.length());

        for(String s : allWords) {
            if (s.matches(regex)) {
                possibleWords.add(s);
            }
        }

        int[] nextNeededPositionFreq = new int[26];
        for(i=0; i < regex.length(); i++) {
            if (regex.charAt(i) == '.') {
                break;
            }
        }

        for (String s : possibleWords) {
            int index = s.charAt(i) - 'a';
            nextNeededPositionFreq[index] += 1;
        }

        SortedMap<Integer, Character> sortedMap = new TreeMap<>();
        for (int j = 0; j < 26; j++) {
            int key = nextNeededPositionFreq[j];
            while (sortedMap.containsKey(key)) {
                key -= 1;
            }

            sortedMap.put(key, ((char) ('a' + j)));

        }

        Character[] guessesToMake = new Character[26];
        sortedMap.values().toArray(guessesToMake);

        for(i=0; i < 26; i++) {
            if (!guessedCharacters.contains(guessesToMake[i])) {
                guessedCharacters.add(guessesToMake[i]);
                return guessesToMake[i];
            }
        }

        return 'a';
    }

    private static Character[] getOrderedFrequencyOfLetters(Api.GameResponse response) {
        phraseParts = response.phrase.split(" ");
        int[] finalNum = new int[26];

        for (int j = 0; j < phraseParts.length; j++) {
            for (int k = 0; k < 26; k++) {
                finalNum[k] += characterTotalsByLength.get(phraseParts[j].length())[k];
            }
        }

        SortedMap<Integer, Character> sortedMap = new TreeMap<>();
        for (int j = 0; j < 26; j++) {
            int key = finalNum[j];
            while (sortedMap.containsKey(key)) {
                key -= 1;
            }

            sortedMap.put(key, ((char) ('a' + j)));

        }

        Character[] guessesToMake = new Character[26];
        sortedMap.values().toArray(guessesToMake);
        return guessesToMake;
    }

    private static void readFileAndSetupDictionary() {
        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader("/Users/Aleks/workspace/Hangman/src/words.txt"));
            while ((line = br.readLine()) != null) {
                int count = line.length();
                int[] countsByChar = (characterTotalsByLength.containsKey(count)
                        ? characterTotalsByLength.get(count) : new int[26]);

                for (int i = 0; i < line.length(); i++) {
                    int index = line.charAt(i) - 'a';
                    countsByChar[index] += 1;
                }

                characterTotalsByLength.put(count, countsByChar);

                if (!countToWords.containsKey(count)) {
                    countToWords.put(count, new ArrayList<String>());
                }

                countToWords.get(count).add(line);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Unable to find the file: fileName");
        } catch (IOException e) {
            System.err.println("Unable to read the file: fileName");
        }
    }
}
