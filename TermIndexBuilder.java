/*  Copyright (C) <2013>  University of Massachusetts Amherst

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 * @author Ismet Zeki Yalniz
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TermIndexBuilder {

    String originalText = null;
    String[] tokens = null;
    Map<String, IndexEntry> index = null;
    TextPreprocessor tp = null;

    public TermIndexBuilder(String filename, TextPreprocessor tp) {
        this.tp = tp;
        originalText = TextPreprocessor.readFile(filename);
        originalText = tp.processText(originalText);
        tokens = originalText.split("\\s+");
    }


    public int getOriginalTextLength() {
        return originalText.length();
    }

    public double intersect_vocabularies(Map<String, IndexEntry> h) {
        long total = 0;
        long mintotal = 0;
        for (IndexEntry ent : index.values()) {
            if ("".equals(ent.getTerm())) {
                continue;
            }
            IndexEntry result = h.get(ent.getTerm());
            if (result != null) {
                mintotal += Math.min(ent.getFrequency(), result.getFrequency());
            }
            total += ent.getFrequency();            
        }

        return ((double) mintotal / total);
    }

    // including startIndex, excluding endIndex
    public Map<String, IndexEntry> indexTerms(int startIndex, int endIndex) {

        index = new HashMap<String, IndexEntry>(endIndex - startIndex);

        for (int i = startIndex; i < endIndex; i++) {
            String s = tokens[i];

            if (!s.equals("")) {
                if (index.containsKey(s)) {
                    IndexEntry e = index.get(s);
                    e.incrementFre();
                } else {
                    index.put(s, new IndexEntry(s, 1, i, 1));
                }
            }
        }
        return index;
    }

    public static Map<String, IndexEntry> findUniqueTerms(Map<String, IndexEntry> terms) {
        Map<String, IndexEntry> uniqueTerms = new HashMap<String, IndexEntry>();

        for (IndexEntry ent : terms.values()) {

            // enforce the term to be unique
            // in order to avoid stop words put a size constraint on the length of words to be selected
            if (ent.getFrequency() == 1 && ent.getTerm().length() > 3) {
                uniqueTerms.put(ent.getTerm(), ent);
            }
        }

        //  Comparator<IndexEntry> comparator = new TermPosComparator();
        //  IndexEntry sorteduniqueterms[] =  uniqueTerms.values().toArray(new IndexEntry[0]);
        //  Arrays.sort(sorteduniqueterms, comparator);
        return uniqueTerms;
    }

    public static List<IndexEntry> countStopWords(Map<String, IndexEntry> terms, String[] stopwords) {
        List<IndexEntry> stopTerms = new ArrayList<IndexEntry>(stopwords.length);

        for (String curWord : stopwords) {
            IndexEntry ent = terms.get(curWord);
            if (ent != null) {
                stopTerms.add(ent);
            } else {
                stopTerms.add(new IndexEntry("", 0, 0, 1));
            }
        }
        return stopTerms;
    }

    public static int[] countTermsBasedOnRank(Map<String, IndexEntry> ind, int MAX_RANK) {
        int result[] = new int[MAX_RANK + 1];

        for (IndexEntry ent : ind.values()) {
            int fre = (int) ent.getFrequency();
            result[fre]++;
        }
        return result;
    }

    public static int[][] countTermsBasedOnRank(Map<String, IndexEntry> ind, Map<String, IndexEntry> ind2,
            int MAX_RANK) {
        int result[][] = new int[MAX_RANK + 1][3];

        for (IndexEntry ent : ind.values()) {
            int fre = (int) ent.getFrequency();
            if (fre > MAX_RANK) {
                continue;
            }

            result[fre][0]++;
            IndexEntry found = ind2.get(ent.getTerm());
            if (found != null) {
                int fre2 = (int) found.getFrequency();
                if (fre == fre2) {
                    result[fre][2]++;
                }
            }
        }
        for (IndexEntry ent : ind2.values()) {
            int fre = (int) ent.getFrequency();
            if (fre > MAX_RANK) {
                continue;
            }
            result[fre][1]++;
        }
        return result;
    }

    public static void outputVocabulary(Map<String, IndexEntry> ind, String filename) {
        if (ind == null) {
            System.out.println("TermIndexBuilder.outputVocabulary(): input hashmap can not be null. Skipping");
            return;
        }

        FileWriter writer = null;
        try {
            Collection<IndexEntry> vocab = ind.values();
            Comparator<IndexEntry> comparator = new IndexTermComparator();
            IndexEntry sortedVocab[] = vocab.toArray(new IndexEntry[0]);
            Arrays.sort(sortedVocab, comparator);
            File file = new File(filename);
            writer = new FileWriter(file, false);
            for (IndexEntry term : sortedVocab) {
                writer.append(term.getTerm() + "\t" + term.getFrequency() + "\n");
            }
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public TextPreprocessor getTextPreprocessor() {
        return tp;
    }

    public String[] getTokens() {
        return tokens;
    }

    public int getNumOfTokens() {
        return tokens.length;
    }

    public Map<String, IndexEntry> getIndex() {
        return index;
    }

    public static void writeRareWordsInFile(TextPreprocessor tp, String inputFile, String outputFile, int max_frequency) {
        FileWriter writer = null;
        try {
            String text = TextPreprocessor.readFile(inputFile);
            text = tp.processText(text).toLowerCase();
            String[] tokens = text.split("\\s+");
            Map<String, IndexEntry> index = new HashMap<String, IndexEntry>();
            // find rare words
            for (int j = 0; j < tokens.length; j++) {
                String s = tokens[j];
                if (index.containsKey(s)) {
                    IndexEntry e = index.get(s);
                    e.incrementFre();
                } else {
                    index.put(s, new IndexEntry(s, 1, j, 1));
                }
            }
            writer = new FileWriter(new File(outputFile));
            for (int i = 0; i < tokens.length; i++) {
                IndexEntry e = index.get(tokens[i]);
                if (e.getFrequency() <= max_frequency) {
                    writer.append(tokens[i] + "\t" + i + "\n");
                }
            }
            writer.close();
        } catch (IOException ex) {
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
            }
        }

    }

    public static List<IndexEntry> getRareWords(TextPreprocessor tp, String inputFile, int max_frequency) {
        long ss = System.nanoTime();
        List<IndexEntry> results = new ArrayList<IndexEntry>();
        String text = TextPreprocessor.readFile(inputFile);
        text = tp.processText(text).toLowerCase();
        String[] tokens = text.split("\\s+");
        Map<String, IndexEntry> index = new HashMap<String, IndexEntry>();
        // find rare words
        for (int j = 0; j < tokens.length; j++) {
            String s = tokens[j];
            if (index.containsKey(s)) {
                IndexEntry e = index.get(s);
                e.incrementFre();
            } else {
                index.put(s, new IndexEntry(s, 1, j, 1));
            }
        }
        for (String token : tokens) {
            IndexEntry e = index.get(token);
            if (e.getFrequency() <= max_frequency) {
                results.add(e);
            }
        }
        System.out.println(System.nanoTime() - ss);
        return results;
    }
}
