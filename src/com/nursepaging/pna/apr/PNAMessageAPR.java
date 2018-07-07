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


package com.nursepaging.pna.apr;

import java.util.StringTokenizer ;
import com.nursepaging.pna.PNAMessage;
/**
 * 
 * @author  cocito
 */
public class PNAMessageAPR extends PNAMessage{
    
    private final String TIME = "Time: ";
    private final String DATE = "Date: ";
    private final String ALARMCODE = "Code: ";
    private final String MESSAGE = "Msg: " ;
    private final String DEV = "BED: " ;
    private String time ;
    private String date ;
    private String logStr = "" ;
    
    public PNAMessageAPR(StringBuffer sb, int id) {
        this.sb = sb ;
        this.id = id ;
        Init();
        parse(sb) ;
    }
    
    public PNAMessageAPR(String extensionNumber, String message){
        super() ;
//        this.dev = extensionNumber ;
//        this.message = message ;
//        Init() ;
    }
    public PNAMessageAPR(){
    }
    
    public void parse(StringBuffer sb){
        logStr += "Parsing..." ;
        String theStr = new String(sb);
        String returnStr = Parse_String(theStr,logStr);
        logStr = returnStr;
        log.info(logStr) ;
    }

    public void Init(){

    }
    
    private String Parse_String(String str, String logstr){
        System.out.println(str);
        StringTokenizer st = new StringTokenizer(str,"/");
        station = st.nextToken();
        logstr += "CAPCODES: " + station ;
        alarmCodePriority =  9;//st.nextToken() ;
        logstr += "PRIORITY: " + alarmCodePriority ;
        messageText="";
        while( st.hasMoreTokens() ){
            messageText += st.nextToken() ;
        }
        logstr += "MESSAGE:" + messageText ;
        return logstr;
    }
	
    private void Reset(){
        time = null ;
        date = null ;
        alarmCodePriority = 9 ;
        messageText = null ;
        station = null ;
    }
    
}
