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
    
    private static final BytePlus NON_BYTE = new BytePlus((byte) 1, true);
    private static final BytePlus END_BYTE = new BytePlus((byte) -1, true);
    private static final String FILENAME = "Judy 1.5 minute.raw";

    public static void main(String[] args) throws IOException {
        final int n = 100;
        byte[] babbledAudio = babbleNMinLengthByte(FILENAME, n, 10000);
        Files.write(Paths.get("out.raw"), babbledAudio);
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
            nextByte = dict.get(lastBytes).get(gen.nextInt(dict.get(lastBytes).size()));
            if (!nextByte.getExtra())
                result.add(nextByte);
            for (int i = 1; i < lastBytes.length; i++){
                lastBytes[i-1] = lastBytes[i];
            }
            lastBytes[-1] = nextByte;
            count++;
        }
        byte[] toReturn = new byte[result.size()];
        for (int i = 0; i < result.size(); i++)
            toReturn[i] = result.get(i).getBase();
        System.out.println("Babbled!");
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
            for (long j = n; j > 0; j--)
                key[(int)(n-j)] = (bytes.get((int) (i-j)));

            appendToKey(toReturn, key, bytes.get((int) i));
            if (i%1000000 == 0 || (i > 5000000 && i%1000 == 0))
                System.out.println("Byte "+i+" added to dictionary!");
        }
        System.out.println("Byte dictionary built!");
        return toReturn;
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
}
