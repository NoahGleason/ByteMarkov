package net.mrpaul.XA110.ps19;

import sun.misc.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Program created by Noah Gleason on 11/20/16.
 */
public class Markov {

    private static final String NON_WORD = "NON_WORD";
    private static final char END_CHAR = (char) 4;
    private static final BytePlus NON_BYTE = new BytePlus((byte) 1, true);
    private static final BytePlus END_BYTE = new BytePlus((byte) -1, true);
    private static final char NON_CHAR = (char) 128721;
    private static final String FILENAME = "Judy 1.5 minute.raw";

    public static void main(String[] args) throws IOException {
        /*System.out.println("-1:");
        System.out.println(babbleNeg1(FILENAME, 100));
        System.out.println("0:");
        System.out.println(babble0(FILENAME, 100));
        System.out.println("1:");
        System.out.println(babble1(FILENAME, 100));*/
        final int n = 100;/*
        System.out.println(n+":");
        System.out.println(babbleN(FILENAME, n, 100));
        System.out.println(n+" with sentence completion:");
        System.out.println(babbleNMinLength(FILENAME, n, 100));
        System.out.println(n+" using characters:");
        String babbled = babbleNMinLengthChar(FILENAME, n, 1000);*/
        /*
        String babbled =
        PrintWriter pw = new PrintWriter("out.txt");
        pw.print(babbled);
        pw.close();
        System.out.println(babbled);
        */
        byte[] babbledAudio = babbleNMinLengthByte(FILENAME, n, 10000);
        Files.write(Paths.get("out.raw"), babbledAudio);
    }

    public static String babbleNeg1(String fileName, int length) throws IOException {
        Set<String> words = readFileToSet(fileName);
        StringBuilder out = new StringBuilder();
        String[] wordsArray = new String[words.size()];
        words.toArray(wordsArray);
        Random gen = new Random();
        for (int i = 0; i < length; i++){
            out.append(wordsArray[gen.nextInt(wordsArray.length)]);
            if (!(i == length-1))
                out.append(" ");
        }
        if(out.charAt(out.length()-1) != '.')
            out.append(".");
        return out.toString();
    }

    public static String babble0(String fileName, int length) throws IOException {
        List<String> words = readToList(fileName);
        Random gen = new Random();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < length; i++){
            out.append(words.get(gen.nextInt(words.size())));
            if (!(i == length-1))
                out.append(" ");
        }
        if(out.charAt(out.length()-1) != '.')
            out.append(".");
        return out.toString();
    }

    public static String babble1(String filename, int length) throws IOException {
        Map<String, List<String>> bigramDict = buildBigramSuffixDictionary(filename);
        StringBuilder out = new StringBuilder();
        Random gen = new Random();
        String lastWord = NON_WORD;
        for (int i = 0; i < length; i++){
            String newWord = bigramDict.get(lastWord).get(gen.nextInt(bigramDict.get(lastWord).size()));
            out.append(newWord);
            if (!(i == length-1))
                out.append(" ");
            lastWord = newWord;
        }
        return out.toString();
    }

    public static String babbleN(String filename, int n, int length) throws IOException {
        Map<String, List<String>> dict = buildNgramSuffixDictionary(filename, n);
        StringBuilder out = new StringBuilder();
        Random gen = new Random();
        List<String> lastWords = new ArrayList<>();
        for (int i = 0; i < n; i++){
            lastWords.add(NON_WORD);
        }
        for (int i = 0; i < length; i++){
            String newWord = dict.get(stitchList(lastWords)).get(gen.nextInt(dict.get(stitchList(lastWords)).size()));
            out.append(newWord);
            if (!(i == length-1))
                out.append(" ");
            lastWords.remove(0);
            lastWords.add(newWord);
        }
        return out.toString();
    }

    public static String babbleNMinLength(String filename, int n, int minLength) throws IOException {
        Map<String, List<String>> dict = buildNgramSuffixDictionary(filename, n);
        StringBuilder out = new StringBuilder();
        Random gen = new Random();
        List<String> lastWords = new ArrayList<>();
        for (int i = 0; i < n; i++){
            lastWords.add(NON_WORD);
        }
        int count = 0;
        while (count < minLength || out.charAt(out.length()-2) != '.'){
            String newWord = dict.get(stitchList(lastWords)).get(gen.nextInt(dict.get(stitchList(lastWords)).size()));
            out.append(newWord);
            out.append(" ");
            lastWords.remove(0);
            lastWords.add(newWord);
            count ++;
        }
        return out.toString();
    }

    public static byte[] babbleNMinLengthByte(String filename, int n, int minLength) throws IOException {
        Map<BytePlus[], List<BytePlus>> dict = buildNgramByteSuffixDictionary(filename, n);
        Random gen = new Random();
        int count = 0;
        BytePlus nextByte;
        List<BytePlus> result = new ArrayList<>();
        BytePlus[] lastBytes = new BytePlus[n];
        for (int i = 0; i < n; i++){
            lastBytes[i] = NON_BYTE;
        }
        while (count < minLength || lastBytes[-1] != END_BYTE){
            try {
                nextByte = dict.get(lastBytes).get(gen.nextInt(dict.get(lastBytes).size()));

            if (!nextByte.getExtra())
                result.add(nextByte);
            for (int i = 1; i < lastBytes.length; i++){
                lastBytes[i-1] = lastBytes[i];
            }
            lastBytes[-1] = nextByte;
            count++;
            } catch (NullPointerException e){
                System.out.print("[");
                for (BytePlus b : lastBytes)
                    System.out.print(b+", ");
                System.out.println("] not found.");
                System.out.println("Size: "+lastBytes.length);
            }
        }
        byte[] toReturn = new byte[result.size()];
        for (int i = 0; i < result.size(); i++)
            toReturn[i] = result.get(i).getBase();
        System.out.println("Babbled!");
        return toReturn;
    }

    public static String babbleNMinLengthChar(String filename, int n, int minLength) throws IOException {
        Map<String, List<String>> dict = buildNgramCharSuffixDictionary(filename, n);
        System.out.println("Suffix dictionary built!");
        StringBuilder out = new StringBuilder();
        Random gen = new Random();
        String lastWords = "";
        for (int i = 0; i < n; i++){
            lastWords += NON_CHAR;
        }
        int count = 0;
        String newLetter = dict.get(lastWords).get(gen.nextInt(dict.get(lastWords).size()));
        out.append(newLetter);
        while (count < minLength && (out.charAt(out.length()-1) != END_CHAR)){
            newLetter = dict.get(lastWords).get(gen.nextInt(dict.get(lastWords).size()));
            out.append(newLetter);
            lastWords = lastWords.substring(1);
            lastWords += newLetter;
            count ++;
            if ((double) (count)/100.0 == count/100)
                System.out.println(count+" characters printed!");
        }
        return out.toString();
    }

    public static List<String> readToList(String filename) throws IOException{
        List<String> toReturn = new ArrayList<>();
        readToList(filename, toReturn);
        return toReturn;
    }

    public static Set<String> readFileToSet(String filename) throws IOException {
        Set<String> toReturn = new HashSet<>();
        readToList(filename, toReturn);
        return toReturn;
    }

    public static Map<String, List<String>> buildBigramSuffixDictionary(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        Map<String, List<String>> toReturn = new HashMap<>();
        String lastWord = NON_WORD;
        for (String line = br.readLine(); line != null; line = br.readLine()){
            for (String word: line.split(" ")){
                if (word.equals(""))
                    word = "\n";
                appendToKey(toReturn, lastWord, word);
                lastWord = word;
            }
        }
        appendToKey(toReturn, lastWord, NON_WORD);
        return toReturn;
    }

    public static Map<String, List<String>> buildNgramSuffixDictionary(String filename, int n) throws IOException {
        List<String> words = readToList(filename);
        Map<String, List<String>> toReturn = new HashMap<>();
        for (int i = 0; i < n; i++){
            words.add(0, NON_WORD);
            words.add(NON_WORD);
        }

        for (int i = n; i < words.size(); i++){
            String key = "";
            for (int j = n; j > 0; j--){
                key += words.get(i-j)+" ";
            }
            key = key.substring(0, key.length()-1);
            appendToKey(toReturn, key, words.get(i));
        }

        return toReturn;
    }

    public static Map<String, List<String>> buildNgramCharSuffixDictionary(String filename, int n) throws IOException{
        List<Character> chars = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        for (String line = br.readLine(); line != null; line = br.readLine()){
            for (int i = 0; i < line.length(); i++){
                chars.add(line.charAt(i));
            }
            chars.add('\n');
        }
        chars.add(END_CHAR);
        System.out.println(chars.size()+" characters found in document.");
        Map<String, List<String>> toReturn = new HashMap<>();
        for (int i = 0; i < n; i++){
            chars.add(0, NON_CHAR);
            chars.add(NON_CHAR);
        }

        for (long i = n; i < chars.size(); i++){
            String key = "";
            for (long j = n; j > 0; j--){
                key += chars.get((int) (i-j));
            }
            appendToKey(toReturn, key, Character.toString(chars.get((int) i)));
        }
        return toReturn;
    }

    public static Map<BytePlus[], List<BytePlus>> buildNgramByteSuffixDictionary(String filename, int n) throws IOException{
        byte[] data = Files.readAllBytes(Paths.get(filename));
        List<BytePlus> bytes = new ArrayList<>();
        for (byte b: data)
            bytes.add(new BytePlus(b));
        bytes.add(END_BYTE);
        System.out.println(bytes.size()+" bytes found in document.");
        for (int i = 0; i < n; i++){
            bytes.add(0, NON_BYTE);
            bytes.add(NON_BYTE);
        }
        Map<BytePlus[], List<BytePlus>> toReturn = new HashMap<>();
        for (long i = n; i < bytes.size(); i++){
            BytePlus[] key = new BytePlus[n];
            for (long j = n; j > 0; j--){
                key[(int)(n-j)] = (bytes.get((int) (i-j)));
                //System.out.println("Byte "+(n-j)+" of "+n+" added to the dictionary for byte "+i+".");
            }
            appendToKey(toReturn, key, bytes.get((int) i));
            if (i%1000000 == 0 || (i > 5000000 && i%1000 == 0))
                System.out.println("Byte "+i+" added to dictionary!");
            if ((((double) (toReturn.size())/(double)(bytes.size()))*100.0)%1 == 0)
                System.out.println(((double) (toReturn.size())/(double)(bytes.size()))*100+"% complete at byte "+i+".");
        }
        System.out.println("Byte dictionary built!");
        return toReturn;
    }

    public static void readToList(String filename, Collection<String> toReturn) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        for (String line = br.readLine(); line != null; line = br.readLine()){
            for (String word: line.split(" ")){
                if (word.equals(""))
                    toReturn.add("\n");
                else
                    toReturn.add(word);
            }
        }
    }

    public static <K, V>  void appendToKey(Map<K, List<V>> m, K key, V value){
        List<V> modifiedValue;
        if (m.containsKey(key)) {
            modifiedValue = m.get(key);
            modifiedValue.add(value);
            m.replace(key, modifiedValue);
        } else{
            modifiedValue = new ArrayList<>();
            modifiedValue.add(value);
            m.put(key, modifiedValue);
        }
    }


    public static String stitchList(List<String> l){
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < l.size(); i++){
            out.append(l.get(i));
            if(!(i == l.size()-1))
                out.append(" ");
        }
        return out.toString();
    }
}
