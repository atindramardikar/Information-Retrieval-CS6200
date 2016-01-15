package webcrawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author atindramardikar
 */
public class WebCrawler {

    static int level = 1; // To check the depth of the crawler
    static int totalUrl = 1000;
    static int noofUrl; // to check number of unique URLs
    static int globalFlag = 0; // Ensures that all the links of a level are visited and no new level file is created before that 
    static Set<String> finallist = new HashSet<>(1000); // Contains the final list of URLs

    public static void main(String[] args) throws IOException {
        File file = new File("level1.txt");
        // remove the keyphrase and provide blank to implement normal crawler
        crawler("http://en.wikipedia.org/wiki/Hugh_of_Saint-Cher", "level1.txt", "concordance"); //passsing first arguments to the crawler function
        finalList();
    }

// This is the actual crawler implementation
    static void crawler(String url, String filename, String keyphrase) throws IOException {
        org.jsoup.nodes.Document doc = null;
        org.jsoup.nodes.Document doc1 = null;
        FileWriter fw = new FileWriter(filename, true);

        try {
            Thread.sleep(100);// delay of 1 sec between request to web servers
            doc = Jsoup.connect(url).get(); // fetching the web page contents
            if (doc != null) // validating the existence of web page and checking its contents for keyphrase
            {
                String str;
                Elements tags = doc.select("a[href]"); // Selecting links from web page content

                for (Element link : tags) {
                    int count = 0;
                    char Check = ':';
                    str = link.attr("abs:href").toString();
                    //checking if colon is not present in rest of URL
                    for (int i = 0; i < str.length(); i++) {
                        if (str.charAt(i) == Check) {
                            count++;
                        }
                    }
                    // checking for english pages and that crawler does visits the wiki main page
                    if (str.contains("en.wikipedia.org") && !str.contains("Main_Page")) {
                        if (count == 1 && noofUrl < totalUrl) {
                            org.jsoup.nodes.Document doc11 = Jsoup.connect(str).get();
                            //Checking for key phrase in the content of the web page
                            if (doc11.body().text().contains(keyphrase)) {
                                //checks if no duplicate entry is added
                                fw.write(str);
                                fw.write("\n");
                                if (finallist.add(str)) {
                                    noofUrl++; //Increment the number of URLs if the non-duplicate entry gets added 
                                    //to keep track of number of URLs so as to stop ater 1000 unique URLS
                                }
                            }
                        }
                    }
                }
                fw.close();
            }
        } catch (Exception ex) {
        }
        if (globalFlag == 0) {
            newLevel(filename, keyphrase);
        }
    }

//Function that creates a new level file after all links from a particular level are visited 
    static void newLevel(String filename, String keyphrase) throws FileNotFoundException, IOException {
        level++;
        // building the file name according to the level
        StringBuilder sb = new StringBuilder();
        sb.append("level");
        sb.append(level);
        sb.append(".txt");
        String newfile = sb.toString();
        File file = new File(newfile);
        parsing(filename, newfile, keyphrase);
    }

//Funtion to parse a file of paarticular level
    static void parsing(String filename, String newfile, String keyphrase) throws FileNotFoundException, IOException {
        String str;
        if (level <= 5 && noofUrl < totalUrl) {
            globalFlag = 1;
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            //Parser
            while ((str = br.readLine()) != null) {
                crawler(str, newfile, keyphrase); // Calls the crawler function for every link in the file of a particular level  
            }
            globalFlag = 0;
            newLevel(newfile, keyphrase);
        }
    }

//Funtion to crate a final.txt file containing 1000 unique URLs or Unique urls after level 5 is reached
    static void finalList() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("final.txt"));
        for (String urls : finallist) {
            bw.write(urls);
            bw.newLine();
        }
        bw.close();
    }
}