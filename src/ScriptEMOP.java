/*  Copyright (C) <year>  University of Massachusetts Amherst

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;

public class ScriptEMOP {
    
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException{
        String ignoredChars = ",.'\";:!?()[]{}<>%`-+=/\\$#|&^*_~@\u25AA\u25CF";
       // System.out.println(ignoredChars);
        NumberFormat fm = NumberFormat.getInstance();
        fm.setMaximumFractionDigits(4);
        String gtfile = "";
        String candfile = "";
        String alignfile = "";
        
        String bookPairList = "E:/MellonProjectData2/bookPairList.txt";
      //  String alignmentOutputFolder = "E:/MellonProjectAlignmentOutput/";
        String alignmentOutputFolder = "E:/MellonProjectAlignmentOutputTest/";        
        String dataFolder = "E:/MellonProjectData2";
        String gtDataFolder = "ECCO-TCP-document-text";
        String ocrDataFolder = "ECCO-Gale-document-OCR";
        String OCRaccuracyFileWithPunc = "E:/MellonProjectData2/Gale_OCR_acc_with_punc6.txt";
        String OCRaccuracyFileWithoutPunc = "E:/MellonProjectData2/Gale_OCR_acc_without_punc6.txt";
                
        // read the book list 
        String list = TextPreprocessor.readFile(bookPairList);
        String bookPairs[] = list.split("\n");    
        double OCRWordAcc, OCRCharAcc;
        
        BufferedWriter writerWithP = null;
        if ( OCRaccuracyFileWithPunc != null){
            writerWithP = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(OCRaccuracyFileWithPunc)),"UTF8"));
        }      
        BufferedWriter writerWithoutP = null;
        if ( OCRaccuracyFileWithoutPunc != null){
            writerWithoutP = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(OCRaccuracyFileWithoutPunc)),"UTF8"));
        }
        
        writerWithoutP.append("BOOK_ID\tWORD_ACC\tCHAR_ACC\n");
        writerWithP.append("BOOK_ID\tWORD_ACC\tCHAR_ACC\n");
        
        for ( int i = 0 ; i < bookPairs.length; i++){
            String books[] = bookPairs[i].split("\\s+");
            
            if ( books.length != 3 ){
                System.out.println("Error. there must be at least 3 columns in the book pair list. Found :" + books.length + "skipping");
                continue;
            }
            gtfile = dataFolder + "/" + books[0] + "/" + gtDataFolder + "/" + books[1] + ".txt";
            candfile = dataFolder + "/" + books[0] + "/" + ocrDataFolder + "/" + books[2] + ".txt";        
            
            // no punctuation
            alignfile = alignmentOutputFolder + "/no_punctuation/word_level/" + books[0] + ".txt";  
            OCRWordAcc = RecursiveAlignmentTool.processSingleJob( gtfile, candfile, "word", "lines", ignoredChars, alignfile ).getOCRAccuracy();                                  
            alignfile = alignmentOutputFolder + "/no_punctuation/char_level/" + books[0] + ".txt";    
            OCRCharAcc = RecursiveAlignmentTool.processSingleJob( gtfile, candfile, "char", "lines", ignoredChars, alignfile ).getOCRAccuracy();
            writerWithoutP.append( books[0] + "\t" + fm.format(OCRWordAcc) + "\t" + fm.format(OCRCharAcc) + "\n");                        
            
       //     RecursiveAlignmentTool.
       //     ArrayList<AlignedSequence> out = RecursiveAlignmentTool.processSingleJob_getAlignedSequence(gtfile, candfile, ignoredChars, "w");
       
       //     Stats sts[] = RecursiveAlignmentTool.processSingleJob_getAlignmentStatsOnly(gtfile, candfile, ignoredChars);          
       //   System.out.println(sts[0].getOCRAccuracy() + "\t" + sts[1].getOCRAccuracy()); 
            
            // with punctuation
            alignfile = alignmentOutputFolder + "/with_punctuation/word_level/" + books[0] + ".txt";    
            OCRWordAcc = RecursiveAlignmentTool.processSingleJob( gtfile, candfile, "word", "lines", "\u25AA\u25CF", alignfile ).getOCRAccuracy();
            alignfile = alignmentOutputFolder + "/with_punctuation/char_level/" + books[0] + ".txt";  
            OCRCharAcc = RecursiveAlignmentTool.processSingleJob( gtfile, candfile, "char", "lines", "\u25AA\u25CF", alignfile ).getOCRAccuracy();
            writerWithP.append( books[0] + "\t" + fm.format(OCRWordAcc) + "\t" + fm.format(OCRCharAcc) + "\n");              
           
           // Stats sts2[] = RecursiveAlignmentTool.processSingleJob_getAlignmentStatsOnly(gtfile, candfile, "\u25AA\u25CF");          
           // System.out.println(sts2[0].getOCRAccuracy() + "\t" + sts2[1].getOCRAccuracy()); 
            
            System.out.println((i+1) + "\t" + bookPairs[i].substring(0,bookPairs[i].length()-2) + "\t" + fm.format(OCRWordAcc) + "\t" + fm.format(OCRCharAcc) );
            
        } 
        
        if (    writerWithP != null ) { writerWithP.close();    }       
        if ( writerWithoutP != null ) { writerWithoutP.close(); }
    }
    
}
