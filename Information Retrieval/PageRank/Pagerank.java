package pagerank;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
/**
 *
 * @author atindramardikar
 */
public final class Pagerank {

    String[] p = new String[190000]; // stores the list pages
    Integer[] inlinks = new Integer[190000];// used to store inlinks
    Map<String, Double> newPR = new HashMap(); // Map which has key as the document and value as new page rank calculated
    Map<String, Double> oldPR = new HashMap(); // Map which has key as the document and value as old page rank
    Map<String, Integer> s = new HashMap(); // Map to store sinks; the value for a corresponding page is 1 if its a sink node
    Double[] perplex = new Double[4]; // used to evaluate last 4 iterations of perplexity
    Map<String, ArrayList<String>> L = new HashMap(); //out-links (without duplicates) from page q(key)
    Map<String, ArrayList<String>> M = new HashMap(); //set (without duplicates) of pages that link to page p(key)
    int index;
    double d = 0.85;
    double sinkPR = 0.00;
    int N = 0;
    double PRsum;
    int temp;

    public Pagerank() throws FileNotFoundException, IOException {

        PRsum = 0;
        temp = 0;
        int summ = 0;
        int sources = 0;
        FileReader fr = new FileReader("wt2g_inlinks.txt"); // reading the file
        BufferedReader br = new BufferedReader(fr);
        String str;
        int k;
        while ((str = br.readLine()) != null) {
            String[] split1 = str.split(" ");
            if (split1.length == 1) {
                sources++;
            }
            p[N] = split1[0]; //creating the page list
            //initialising the M and L for every page
            M.put(split1[0], new ArrayList<String>());
            L.put(split1[0], new ArrayList<String>());
            //load the value for M for every page
            for (int i = 1; i < split1.length; i++) {
                if (M.get(split1[0]) == null) {
                    M.put(split1[0], new ArrayList<String>());
                }
                if (!M.get(split1[0]).contains(split1[i])) {
                    M.get(split1[0]).add(split1[i]);
                }
            }
            inlinks[N++] = index;
        }
        
        //load the value for L for every page
        Set<String> keySetm = M.keySet();
        for (String key1 : keySetm) {
            ArrayList<String> get1 = M.get(key1);
            for (String strng : get1) {
                if (L.get(strng) == null) {
                    L.put(strng, new ArrayList<String>());
                }
                if (!L.get(strng).contains(key1)) {
                    L.get(strng).add(key1);
                }
            }
        }

         //load the value for s for every page
        Set<String> keySet = L.keySet();
        for (String key : keySet) {
            if (L.get(key).isEmpty()) {
                s.put(key, 1);
            } else {
                s.put(key, 0);
            }
        }
        
        //initialising oldPR and newPR
        for (int i = 0; i < N; i++) {
            oldPR.put(p[i], (double) 1 / (N));
            newPR.put(p[i], 0.0);
        }
        // iterative PageRank algorithm
        while (!converge()) {
            sinkPR = 0;
            Set<String> keySetS = s.keySet();
            for (String key : keySetS) {
                if (s.get(key) == 1) {
                    sinkPR += oldPR.get(key);
                }
            }
            for (int l = 0; l < N; l++) {
                newPR.put(p[l], ((1 - d) / N));
                double temp = newPR.get(p[l]);
                newPR.put(p[l], (temp + (d * sinkPR) / N));

                if (M.containsKey(p[l])) {
                    ArrayList<String> get = M.get(p[l]);
                    for (String strr : get) {
                        temp = newPR.get(p[l]);
                        newPR.put(p[l], (temp + d * (oldPR.get(strr) / L.get(strr).size())));
                    }
                }
            }
            // copy the newPR calculated to oldPR
            Set<String> keySet123 = newPR.keySet();
            for (String key : keySet123) {
                oldPR.put(key, newPR.get(key));
            }
            //call the perplexity function to calculate perplexity
            perplexity(oldPR);

        }
        //Displaying pagerank values
        System.out.println("PAGE RANK VALUES:");
        Set<String> keySet11 = oldPR.keySet();
        for (String key : keySet11) {
            DecimalFormat df = new DecimalFormat("#.########");
            System.out.println(key + "  " + df.format(newPR.get(key)));
        }
    }

    //Perplexity function: calculates the perplexity after every iteration
    void perplexity(Map<String, Double> newPR) {
        double sum = 0, perplexity;
        Set<String> keySet123 = newPR.keySet();
        for (String key : keySet123) {
            sum += newPR.get(key) * (Math.log(newPR.get(key)) / Math.log(2));
        }
        sum = 0 - sum;
        perplexity = Math.pow(2, sum);
        addperplex(perplexity);
        System.out.println("perplexity: " + perplexity);
    }
    
    //converge function checks if the change in perplexity is less than 1 for consecutive
   // 4 iterations. returns true if pagerank has converged else returns false
    boolean converge() {
        int flag;
        Integer check[] = new Integer[3];
        if (temp <= 3) {
            return false;
        } else {
            for (int i = 0; i < 3; i++) {
                if (Math.abs(perplex[i + 1] - perplex[i]) < 1) {
                    check[i] = 1;
                } else {
                    check[i] = 0;
                }
            }
            if (check[0] == 1 && check[1] == 1 && check[2] == 1) {
                flag = 1;
            } else {
                flag = 0;
            }
            if (flag == 1) {
                return true;
            } else {
                return false;
            }
        }
    }

    // add perplexity: adds the perplexity to a vector containing perplexity
    //for last four iteration which is used to check convergence
    private void addperplex(double perplexity) {
        if (temp <= 3) {
            perplex[temp++] = perplexity;
        } else {
            for (int i = 0; i < 3; i++) {
                perplex[i] = perplex[i + 1];
            }
            perplex[3] = perplexity;
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        new Pagerank();
    }
}
