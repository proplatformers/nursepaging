/*
This file is part of Nursepaging.

    Nursepaging is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Nursepaging is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Nursepaging.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.nursepaging.pna.basepage;

import com.nursepaging.pna.PNAMessage;
import java.util.StringTokenizer ;
/**
 *
 * @author  root
 */
public class PNAMessageBP extends PNAMessage implements TAP_Constants{

    private StringBuffer sb ;
    private String msg_id ;
    private String extensionNumber ;
    private String message ;

    /** Creates a new instance of SourceMessageBASEPage */
    public PNAMessageBP(StringBuffer sb){
        this.sb = sb ;
        ParseData(sb) ;
    }
    
    public PNAMessageBP(StringBuffer sb, int id){
        this(sb) ;
    }

    public PNAMessageBP(String extensionNumber, String message){
        this.extensionNumber = extensionNumber ;
        this.message = message ;
    }

    public String GetExtensionNumber(){
        return extensionNumber ;
    }

    public String GetMessage(){
        return message ;
    }

    public String GetMsgID(){
        return msg_id ;
    }

        //FOR STRINGS: MID<CR>EXT#<CR>string [where XXXX is ext number to page]
        private void ParseData(StringBuffer sb){

            //System.out.println("\n\n\tParseData with StringTokenizer") ;
            char[] carRet = {0x0d} ;
            String carriageReturn = new String (carRet) ;
            StringTokenizer strtok = new StringTokenizer( sb.toString(), carriageReturn ) ;
            int column = 1 ;
            while(strtok.hasMoreTokens() ){
                //if(column == 1)
                //    msg_id = strtok.nextToken() ;
                if(column == 1)
                    extensionNumber = strtok.nextToken() ;
                else if(column == 2)
                    message = strtok.nextToken() ;
                else
                    System.out.println("Too many tokens in Parsing the data") ;
                column++ ;
                //System.out.println( tmp ) ;
            }

            return ;
        }

}
