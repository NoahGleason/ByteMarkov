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
    //Special character used to let the program handle going past the end of the file.
    private static final BytePlus NON_BYTE = new BytePlus((byte) 1, true);
    //Special character used to mark the end of the file.
    private static final BytePlus END_BYTE = new BytePlus((byte) -1, true);
    //Filename of the file to be read from.
    private static final String FILENAME = "Judy 1.5 minute.raw";

    public static void main(String[] args) throws IOException {
        final int n = 50;
        byte[] babbledAudio = babbleNMinLengthByte(FILENAME, n, 100000);
        Files.write(Paths.get("out.raw"), babbledAudio);
    }

    public static byte[] babbleNMinLengthByte(String filename, int n, int minLength) throws IOException {
        //Build a suffix dictionary
        Map<List<BytePlus>, List<BytePlus>> dict = buildNgramByteSuffixDictionary(filename, n);
        
        //Instantiate variables
        Random gen = new Random();
        int count = 0;
        BytePlus nextByte;
        List<BytePlus> result = new ArrayList<>();
        List<BytePlus> lastBytes = new ArrayList<>(n);
        
        //Fill up lastBytes with the proper number of non-bytes
        for (int i = 0; i < n; i++){
            lastBytes.add(NON_BYTE);
        }
        
        //Loop until both the minimum number of bits have been written and the end of file character was just written
        while (count < minLength || lastBytes.get(lastBytes.size()-1) != END_BYTE){
            //Fetch the possible next bytes from the dictionary.
            List<BytePlus> possibleNext = dict.get(lastBytes);
            
            //Choose an item at random from the possible next items as the next byte.
            nextByte = possibleNext.get(gen.nextInt(possibleNext.size()));
            
            //If the chosen byte is not a special character (i.e. NON-BYTE or END_BYTE), add it to the file
            if (!nextByte.getExtra()){
                //Add the chosen byte to the output and increment the count of how many bytes have been chosen
                result.add(nextByte);
                count++;
            }
            
            //Shift the lastBytes list, removing the oldest element and adding in next.
            lastBytes.remove(0);
            lastBytes.add(nextByte);
        }
        
        //Convert toReturn to an array (needed to write it to file)
        byte[] toReturn = new byte[result.size()];
        for (int i = 0; i < result.size(); i++)
            toReturn[i] = result.get(i).getBase();
        
        //Return the output and notify the user that the babbling succeeded.
        System.out.println("Babbled!");
        return toReturn;
    }

    public static Map<List<BytePlus>, List<BytePlus>> buildNgramByteSuffixDictionary(String filename, int n) throws IOException{
        //Read the bytes of the named file.
        byte[] data = Files.readAllBytes(Paths.get(filename));
        
        //Convert the array of bytes to a list of bytePlus
        List<BytePlus> bytes = new ArrayList<>();
        for (byte b: data)
            bytes.add(new BytePlus(b));
        
        //Add the EOF character to the end.
        bytes.add(END_BYTE);
        
        //Notify the user of the file's size.
        System.out.println(bytes.size()+" bytes found in document.");
        
        //Pad the list with n NON_BYTEs on each end.
        for (int i = 0; i < n; i++){
            bytes.add(0, NON_BYTE);
            bytes.add(NON_BYTE);
        }
        
        //Construct the ditionary where each key is a list of bytes, in order, and each value is a list of
        //possible bytes that could come after it, with certain bytes being entered multiple times to 
        //reflect that they more often come after in the file.
        Map<List<BytePlus>, List<BytePlus>> toReturn = new HashMap<>();
        
        //Loop through the array of bytes, starting at the first non-NON_BYTE byte.
        for (long i = n; i < bytes.size(); i++){
            List<BytePlus> key = new ArrayList<>(n);
            for (long j = n; j > 0; j--)
                key.add(bytes.get((int) (i-j)));

            appendToKey(toReturn, key, bytes.get((int) i));
            if (i%1000000 == 0)
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
