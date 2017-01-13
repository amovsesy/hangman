import api.Api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This hangman solution will go through each word in the and get a frequency of letters
 *   based on the number of characters. Then it will take the phrases and see how many characters
 *   in each, add the frequencies of those number of characters, and guess in order or frequency.
 *
 * @author Aleks
 *
 */
public class Solution1 {
    public static Map<Integer, int[]> characterTotalsByLength = new HashMap<>();
    public static void playHangman() {
        // Show an example usage of the API. This creates a new game and makes
        // three guesses, showing the game state response after each call.

        int numWon = 0;

        fillFrequencyCounts();

        for (int i = 0; i < 10; i++) {
            Api.GameResponse response = Api.sendNewGameRequest("test@test.com");

            Character[] guessesToMake = getOrderedCharacterCountsBasedOnPhrase(response);

            for (int j = 25; response.state.equals("alive") && j >= 0; j--) {
                response = Api.sendGuessRequest(response.game_key, guessesToMake[j]);
                System.out.println(response);
                numWon += (response.state.equals("won") ? 1 : 0);
            }
        }

        System.out.println("Winning percent = " + (numWon / 1000.0));
    }

    private static Character[] getOrderedCharacterCountsBasedOnPhrase(Api.GameResponse response) {
        String[] phraseParts = response.phrase.split(" ");
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

    private static void fillFrequencyCounts() {
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
            }
        } catch (FileNotFoundException e) {
            System.err.println("Unable to find the file: fileName");
        } catch (IOException e) {
            System.err.println("Unable to read the file: fileName");
        }
    }
}
