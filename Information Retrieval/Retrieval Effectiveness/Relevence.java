package relevence;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author atindramardikar
 */
public final class Relevence {

    Map<Integer, ArrayList<String>> relevance = new HashMap(); // Map for storing relevant docs for all the queries using the relevance file
    Map<String, Double> query1 = new LinkedHashMap(); // storing query 1 top 100 docs with their Score obtained from Lucene
    Map<String, Double> query2 = new LinkedHashMap(); // storing query 2 top 100 docs with their Score obtained from Lucene
    Map<String, Double> query3 = new LinkedHashMap(); // storing query 3 top 100 docs with their Score obtained from Lucene
    Double[] DCG = new Double[100]; // to store DCG values for every query
    Double[] iDCG = new Double[100]; // to store iDCG values for every query
    double Map = 0; // to store the MAP value

    // constructor which calls all the functions in order
    public Relevence() throws FileNotFoundException, IOException {

        insert_relevence_values(); // storing relevant docs details
        insert_query_values(query1, "query1_top_100.txt"); // inserting top 100 docs with their Score obtained from Lucene for query1
        insert_query_values(query2, "query2_top_100.txt"); // inserting top 100 docs with their Score obtained from Lucene for query2
        insert_query_values(query3, "query3_top_100.txt"); // inserting top 100 docs with their Score obtained from Lucene for query3
        relevance_factor(query1, 12); // Calculating the relevance scores for query1
        relevance_factor(query2, 13); // Calculating the relevance scores for query2
        relevance_factor(query3, 19); // Calculating the relevance scores for query3
        System.out.println("MAP= " + Map / 3); // MAP value printing

    }

    // main function
    public static void main(String[] args) throws FileNotFoundException, IOException {
        Relevence relevence = new Relevence();
    }

    // Functions to read from the relevance file and store it in a map where key is
    // the query ID and value is the list of documents that are relevant to that query
    @SuppressWarnings("ConvertToTryWithResources")
    public void insert_relevence_values() throws FileNotFoundException, IOException {
        FileReader fr = new FileReader("relevance.txt"); // reading the file
        BufferedReader br = new BufferedReader(fr);
        String str;
        String docid;
        String queryid = "";
        while ((str = br.readLine()) != null) {
            String[] split = str.split(" ");
            String[] split1 = split[2].split("-");
            if (split1[1].length() == 3) {
                docid = split1[0].concat("-0").concat(split1[1]);
            } else {
                docid = split[2];
            }
            if (queryid.equals("") || !queryid.equals(split[0])) {
                relevance.put(Integer.parseInt(split[0]), new ArrayList<String>());
                relevance.get(Integer.parseInt(split[0])).add(docid);
            } else {
                relevance.get(Integer.parseInt(split[0])).add(docid);
            }

            queryid = split[0];
        }
        br.close();
    }

    // Function to insert top 100 docs with their Score obtained from Lucene for
    // every query
    // input to the function is the map name and the filename of the file having top100 results for a query
    // are passed to the function.
    // the output is the stored values of top 100 docId and its corresponding score 
    // in the map.
    @SuppressWarnings("ConvertToTryWithResources")
    private void insert_query_values(Map<String, Double> query, String filename) throws FileNotFoundException, IOException {
        FileReader fr = new FileReader(filename); // reading the file
        BufferedReader br = new BufferedReader(fr);
        String str;
        while ((str = br.readLine()) != null) {
            String[] split = str.split(" ");
            String[] split1 = split[2].split("=");
            query.put(split[1].substring(0, 9), Double.parseDouble(split1[1]));
        }
        br.close();
    }

    // Function to calculate the relevance score for a query
    // input to the function is the map name and the query ID
    // output is the relevance score table and P@20 and average precision values
    private void relevance_factor(Map<String, Double> query, int par) {
        ndcg_calculation(query, par);
        double retreived = 0, relevant = 0, pat20 = 0;
        @SuppressWarnings("UnusedAssignment")
        double precision = 0, recall = 0, totalpre = 0;
        int rank = 1;
        ArrayList<String> get = relevance.get(par);
        Set<String> keySetm = query.keySet();
        System.out.println("Table for Query = " + par);
        System.out.println();
        for (String key1 : keySetm) {
            if (get.contains(key1)) {
                retreived++;
                relevant++;
                recall = (double) (relevant / get.size());
                precision = (double) (relevant / retreived);
                totalpre += precision;
                if (rank == 20) {
                    pat20 = precision;
                }
                double ndcg = (double) DCG[rank - 1] / iDCG[rank - 1];
                System.out.println(rank++ + " " + key1 + " " + query.get(key1) + " " + 1 + " " + precision + " " + recall + " " + ndcg);
            } else {
                retreived++;
                recall = (double) (relevant / get.size());
                precision = (double) (relevant / retreived);
                if (rank == 20) {
                    pat20 = precision;
                }
                double ndcg = (double) DCG[rank - 1] / iDCG[rank - 1];
                System.out.println(rank++ + " " + key1 + " " + query.get(key1) + " " + 0 + " " + precision + " " + recall + " " + ndcg);
            }
        }
        System.out.println();
        System.out.println("Average Precision= " + (totalpre / get.size()));
        Map += totalpre / get.size();
        System.out.println("P@20 value for query " + par + "=" + pat20);
        System.out.println();
    }

    // Function to Calculate DCG and iDCG values
    // input to the function is the map name and the query ID
    // output is the array wiith stored values of the DCG and iDCG
    public void ndcg_calculation(Map<String, Double> query, int par) {
        int rank = 1;
        double sum = 0, sumd = 0;
        int index = 0;
        ArrayList<String> get = relevance.get(par);
        Set<String> keySetm = query.keySet();
        for (String key1 : keySetm) {
            if (get.contains(key1)) {
                if (rank == 1) {
                    sum = 1;
                    sumd = 1;
                } else {
                    double temp = 1 / (Math.log(rank) / Math.log(2));
                    sum += temp;
                    sumd += 1 / (Math.log(index + 1) / Math.log(2));
                }
                DCG[rank - 1] = sum;
                iDCG[index++] = sumd;
                rank++;
            } else {
                if (rank == 1) {
                    sum = 0;
                }
                DCG[rank - 1] = sum;
                rank++;
            }
            for (int i = index; i < 100; i++) {
                iDCG[i] = iDCG[index - 1];
            }
        }
    }
}