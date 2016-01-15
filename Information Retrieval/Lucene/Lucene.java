package lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Lucene {

    private static Analyzer sAnalyzer = new SimpleAnalyzer(Version.LUCENE_47);

    // Method to check if a string is numeric or not
    // used when checking if a the term or token is numeric and if numeric ignore
    private static boolean numeric(String termEnum) {
        return termEnum.matches("[0-9]+");
    }

    // Method for editing the corpus
    // used to remove html and pre tags from the corpus
    // takes the path of the index file/directory provided by the user
    @SuppressWarnings({"null", "ConstantConditions", "ConvertToTryWithResources"})
    private static void editCorpus(String path) throws FileNotFoundException, IOException {
        File file = new File(path);
        // If the path provided is a directory then edit all the files in the 
        // directory
        // else if the given path is a file edit the given file
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            for (File file1 : listFiles) {
                editFiles(file1); // call to the editFile function
            }
        } else if (file.isFile()) {
            editFiles(file); // call to the editFile function
        }
    }

    // Method that actually edits the file
    // removes html and pre tag from corpus files
    @SuppressWarnings("ConvertToTryWithResources")
    private static void editFiles(File file) throws IOException {
        String name = file.getName();
        if (name.endsWith(".htm") || name.endsWith(".html")
                || name.endsWith(".xml") || name.endsWith(".txt")) {
            FileReader fr = new FileReader(file.getAbsolutePath()); // reading the query file
            BufferedReader br = new BufferedReader(fr);
            String str;
            String contents = "";
            while ((str = br.readLine()) != null) {
                contents += str + "\n";
            }

            String contents1 = contents.replace("<pre>", "");
            contents1 = contents1.replace("</pre>", "");
            contents1 = contents1.replace("<html>", "");
            contents1 = contents1.replace("</html>", "");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
            bw.write(contents1);
            bw.close();
        }
    }
    private IndexWriter writer; // used to write the index
    private ArrayList<File> queue = new ArrayList<>();

    @SuppressWarnings({"null", "ConstantConditions", "UseSpecificCatch"})
    public static void main(String[] args) throws IOException {
        System.out
                .println("Enter the FULL path where the index will be created: (e.g. /Usr/index or c:\\temp\\index)");
        String indexLocation = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String s = br.readLine();

        Lucene indexer = null;
        try {
            indexLocation = s;
            indexer = new Lucene(s);
        } catch (Exception ex) {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }

        // ===================================================
        // read input from user until he enters q for quit
        // ===================================================
        while (!s.equalsIgnoreCase("q")) {
            try {
                System.out
                        .println("Enter the FULL path to add into the index (q=quit): (e.g. /home/mydir/docs or c:\\Users\\mydir\\docs)");
                System.out
                        .println("[Acceptable file types: .xml, .html, .html, .txt]");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }
                editCorpus(s);
                // try to add file into the index
                indexer.indexFileOrDirectory(s);
            } catch (Exception e) {
                System.out.println("Error indexing " + s + " : "
                        + e.getMessage());
            }
        }
        // ===================================================
        // after adding, we always have to call the
        // closeIndex, otherwise the index is not created
        // ===================================================
        indexer.closeIndex();
        // =========================================================
        // Now search
        // =========================================================
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
                indexLocation)));
        termFreq(indexLocation); // call to the functions that calculates term-freq for each term

        s = "";
        while (!s.equalsIgnoreCase("q")) {
            try {
                IndexSearcher searcher = new IndexSearcher(reader);
                TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
                System.out.println("Enter the search query (q=quit):");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }
                Query q = new QueryParser(Version.LUCENE_47, "contents",
                        sAnalyzer).parse(s);
                searcher.search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                // 4. display results
                System.out.println(hits.length);
                for (int i = 0; i < hits.length; ++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    System.out.println((i + 1) + ". " + d.get("filename")
                            + " score=" + hits[i].score);
                }
            } catch (Exception e) {
                System.out.println("Error searching " + s + " : "
                        + e.getMessage());
                break;
            }

        }

    }

    /**
     * Constructor
     *
     * @param indexDir the name of the folder in which the index should be
     * created
     * @throws java.io.IOException when exception creating index.
     */
    Lucene(String indexDir) throws IOException {

        FSDirectory dir = FSDirectory.open(new File(indexDir));

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,
                sAnalyzer);

        writer = new IndexWriter(dir, config);

    }

    /**
     * Indexes a file or directory
     *
     * @param fileName the name of a text file or a folder we wish to add to the
     * index
     * @throws java.io.IOException when exception
     */
    static Map sortByValue(Map map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @SuppressWarnings({"null", "ConstantConditions"})
    public void indexFileOrDirectory(String fileName) throws IOException {
        // ===================================================
        // gets the list of files in a folder (if user has submitted
        // the name of a folder) or gets a single file name (is user
        // has submitted only the file name)
        // ===================================================
        addFiles(new File(fileName));

        int originalNumDocs = writer.numDocs();
        for (File f : queue) {
            FileReader fr = null;
            try {
                Document doc = new Document();

                // ===================================================
                // add contents of file
                // ===================================================
                fr = new FileReader(f);

                doc.add(new TextField("contents", fr));
                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                doc.add(new StringField("filename", f.getName(),
                        Field.Store.YES));
                writer.addDocument(doc);
                System.out.println("Added: " + f);

            } catch (Exception e) {
                System.out.println("Could not add: " + f);
            } finally {
                fr.close();
            }
        }

        int newNumDocs = writer.numDocs();
        System.out.println("");
        System.out.println("************************");
        System.out
                .println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");

        queue.clear();
        //System.out.println(writer);
    }

    private void addFiles(File file) {

        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            String filename = file.getName().toLowerCase();
            // ===================================================
            // Only index text files
            // ===================================================
            if (filename.endsWith(".htm") || filename.endsWith(".html")
                    || filename.endsWith(".xml") || filename.endsWith(".txt")) {
                queue.add(file);
            } else {
                System.out.println("Skipped " + filename);
            }
        }
    }

    // Method for calculating the term freq for each term
    // first read the index created by the lucene using IndexReader and get the
    // term and write the frequency to a file termfreq.txt
    // Also write data required to plot the zipfian curve to a file called plot.txt
    @SuppressWarnings("ConvertToTryWithResources")
    public static void termFreq(String indexLocation) throws IOException {
        Map<String, Long> termfreq = new HashMap<>();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
                indexLocation))); // instance of index reader
        TermsEnum termEnum = MultiFields.getTerms(reader, "contents").iterator(null); // getting the terms
        long sum = 0;
        while (termEnum.next() != null) {
            String term = termEnum.term().utf8ToString();
            if (!numeric(term)) {
                termfreq.put(term, termEnum.totalTermFreq());// getting the term frequency 
                sum += termEnum.totalTermFreq();
            }
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter("termfreq.txt")); // file for term-freq
        BufferedWriter bw1 = new BufferedWriter(new FileWriter("plot.txt")); // file for data required to plot
        @SuppressWarnings("UnusedAssignment")
        Map result = new LinkedHashMap();
        result = sortByValue(termfreq);
        int seqrank = 1; // sequential rank
        Set keySet = result.keySet();
        for (Object key : keySet) {
            double prob = (double) Integer.parseInt(result.get(key).toString()) / sum;// probablity of every-term
            bw.write(key + " -> " + result.get(key));
            bw.newLine();
            bw1.write(seqrank + "," + key + "," + result.get(key) + "," + prob + "," + Math.log(seqrank) + "," + Math.log(prob));
            bw1.newLine();
            seqrank++;
        }
        bw.close();
        bw1.close();
    }

    /**
     * Close the index.
     *
     * @throws java.io.IOException when exception closing
     */
    public void closeIndex() throws IOException {
        writer.close();
    }
}
