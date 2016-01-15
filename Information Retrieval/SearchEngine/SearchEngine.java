package searchengine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author atindramardikar
 */
@SuppressWarnings("unchecked")
public final class SearchEngine {

    Map<Integer, String> query = new HashMap(); // stores query id and the query
    Map<Integer, ArrayList<String>> docs = new HashMap(); //stores the docid and all its tokens excluding the non-numeric tokens
    int tokens = 0;
    // Given constants
    double k1 = 1.2;
    double k2 = 100;
    double b = 0.75;

    public SearchEngine() throws IOException {
        indexer("tccorpus.txt");
        bm25("index.out", "queries.txt", 100);
    }

    // indexer function which takes the tccorpus and produces the inverted index
    public void indexer(String filename) throws IOException {
        Map<String, ArrayList<String>> index = new HashMap();
        File file = new File(filename);
        byte[] data;
        try (FileInputStream fis = new FileInputStream(file)) {
            data = new byte[(int) file.length()];
            fis.read(data);
        }
        String str = new String(data, "UTF-8");
        String split[] = str.split("#");
        int docid = 0;
        for (int i = 0; i < split.length; i++) {
            try (Scanner scanner = new Scanner(split[i])) {
                int lines = 1;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String split1[] = line.trim().split(" ");
                    if (lines == 1) {
                        docid = Integer.parseInt(split1[0]);
                        docs.put(docid, new ArrayList<String>());
                        lines = 0;
                    } else {
                        for (int j = 0; j < split1.length; j++) {
                            if (!isNumber(split1[j])) {
                                docs.get(docid).add(split1[j]);
                                if (index.get(split1[j]) == null) {
                                    index.put(split1[j], new ArrayList<String>());
                                    tokens++;
                                }
                            }
                        }
                    }
                }
            }
        }
        Set<String> keySetI = index.keySet();
        Set<Integer> keySetD = docs.keySet();
        for (String keyi : keySetI) {
            for (Integer keyd : keySetD) {
                int count = 0;
                ArrayList<String> get = docs.get(keyd);
                for (String strr : get) {
                    if (strr.equalsIgnoreCase(keyi)) {
                        count++;
                    }
                }
                if (count != 0) {
                    index.get(keyi).add(keyd + "," + count);
                }
            }
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter("index.out"));
        Set<String> keySetIn1 = index.keySet();
        for (String keyi : keySetIn1) {
            bw.write(keyi + " -> " + index.get(keyi));
            bw.newLine();
            //bw.newLine();
        }
    }

    public static boolean isNumber(String str) {
        return str.matches("[0-9]+");  //match a number with optional '-' and decimal.
    }

    //Bm25 functions which takes the index file, the quryfile and the count of the top docs required
    public void bm25(String indexfilename, String queryfilename, int count) throws IOException {
        Map<String, ArrayList<String>> index1 = new HashMap(); // map to store index from index file
        Map<Integer, Integer> doc1 = new HashMap();
        BufferedWriter bw = new BufferedWriter(new FileWriter("results.eval"));
        FileReader fr1 = new FileReader(indexfilename); // reading the file
        BufferedReader br1 = new BufferedReader(fr1);
        String line;
        // creating index map from the inverted index file
        while ((line = br1.readLine()) != null) {
            String[] lines = line.split(" -> ");
            index1.put(lines[0], new ArrayList<String>());
            String[] docsfreq = lines[1].substring(lines[1].indexOf("[") + 1, (lines[1].length() - 1)).split(", ");
            index1.get(lines[0]).addAll(Arrays.asList(docsfreq));
        }
        // creating doc iinformation from the index file
        Set<String> keySetin = index1.keySet();
        for (String key1 : keySetin) {
            ArrayList<String> getit1 = index1.get(key1);
            for (String tf : getit1) {
                String split2[] = tf.split(",");
                if (split2.length == 2) {
                    if (doc1.containsKey(Integer.parseInt(split2[0]))) {
                        doc1.put(Integer.parseInt(split2[0]), (doc1.get(Integer.parseInt(split2[0])) + Integer.parseInt(split2[1])));
                    } else {
                        doc1.put(Integer.parseInt(split2[0]), Integer.parseInt(split2[1]));
                    }
                }
            }
        }
        double sum=0;
        Set<Integer> keysetdoc=doc1.keySet();
        for(int key:keysetdoc){
            sum+=doc1.get(key);
        }
        double avg=sum/doc1.size(); // dvdl
        FileReader fr = new FileReader(queryfilename); // reading the query file
        BufferedReader br = new BufferedReader(fr);
        String str;
        int id = 1;
        while ((str = br.readLine()) != null) {
            query.put(id, str);
            id++;
        }

        for (int i = 1; i <= query.size(); i++) {
            Map<Integer, Double> BMscore = new HashMap();
            String query1 = query.get(i);
            String[] Q = query1.split(" ");
            Set<Integer> keySetDD = doc1.keySet();
            for (Integer key : keySetDD) {
                double BM = 0;
                int fi;
                for (int k = 0; k < Q.length; k++) {
                    fi = 0;
                    ArrayList<String> getit = index1.get(Q[k]);
                    int n = getit.size();
                    for (String tf : getit) {
                        String split2[] = tf.split(",");
                        if (Integer.parseInt(split2[0]) == key) {
                            fi = Integer.parseInt(split2[1]);
                            break;
                        }
                    }
                    double K = k1 * ((1 - b) + b * (doc1.get(key) / avg));
                    BM += Math.log(((doc1.size() - n) + 0.5) / (n + 0.5)) * ((fi * (k1 + 1)) / (fi + K));
                }
                BMscore.put(key, BM);
            }
            // creating results file
            Map sortByValue = sortByValue(BMscore);
            int max = 1;
            Set<Integer> keySetIn = sortByValue.keySet();
            for (int keyi : keySetIn) {
                {
                    if (max > count) {
                        break;
                    }
                    bw.write(i + " Q0 " + keyi + " " + max + " " + sortByValue.get(keyi) + " atindramardikar");
                    bw.newLine();
                    max++;
                }
            }
            BMscore.clear();
            sortByValue.clear();
        }
    }

    public Map sortByValue(Map unsortMap) {
        List list = new LinkedList(unsortMap.entrySet());

        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        Map sortedMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
    public static void main(String[] args) throws FileNotFoundException, IOException {
        SearchEngine searchEngine = new SearchEngine();
    }
}
