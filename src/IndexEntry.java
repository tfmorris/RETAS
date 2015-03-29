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
public class IndexEntry {

    private String term;
    private long fre;
    private long pos; // position in the stream (we only need one since we only care about unique terms)
    private int numOfTokens;

    /**
     * @param token word/string for this entry
     * @param frequency number of times the word occurs
     * @param position position of first word
     * @param count ??
     */
    public IndexEntry(String token, long frequency, long position, int count){
        term = token;
        fre = frequency;
        pos = position;
        numOfTokens = count; // TODO: this is always 1 - do we need it?
    }

    public long getFrequency(){
        return fre;
    }
    public long getPos(){
        return pos;
    }
    public int getPosInt(){
        return (int)pos;
    }

    public int getNumOfTokens(){
        return numOfTokens;
    }
    public String getTerm(){
        return term;
    }
    public void incrementFre(){
        fre++;
    }
    
    @Override
    public String toString() {
        return term + ":" + pos + ":" + fre;
    }

}
