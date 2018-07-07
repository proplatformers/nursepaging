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

//import com.nursepaging.pna.CorePNA ;
//import au.com.mrvoip.config.PropertiesController ;
//import org.opencsta.config.PropertiesController ;
import java.util.Properties ;
import org.apache.log4j.Logger ;
//import java.io.IOException ;
/**
 *
 * @author  root
 */
public class LoginController implements TAP_Constants{

    private Properties theProps ;
    private PNA_BP pnabp ;
    private boolean loggedIn, firstPart ;

    //LOGGING STUFF
    private static Logger log = Logger.getLogger(com.nursepaging.pna.basepage.PNA_BP.class) ;


//    /** Creates a new instance of TAP_LoggingOn */
//    public LoginController(PNA_BP pna) {
//        this.pnabp = pna ;
//        loggedIn = false ;
//        firstPart = true ;
//        theProps = PropertiesController.getInstance("/etc/pna.conf") ;
//    }

    public LoginController(PNA_BP pna, Properties aprop) {
        this.pnabp = pna ;
        loggedIn = false ;
        firstPart = true ;
        theProps = aprop ;
    }

    public boolean TAPLoggedIn(){
        return loggedIn ;
    }

    public void Logs(){
        Properties tmp_props = theProps ;
        log = Logger.getLogger(tmp_props.getProperty("LOGGER_PNA_RXTX")) ;
    }

    public boolean Process(StringBuffer sb){

        //System.out.println("LoginController.Process()\n\tNot Logged In\n\tIn Login Procedure\n") ;

        if(sb.length() == 1 && sb.charAt(0) == BP_CR && firstPart == true ){
            //System.out.println("\t\tReceived: <CR>......first part\n\t\tSending: ID=<CR><LF>") ;
            firstPart = false ;
            WriteToLogger(sb, 'R') ;
            return SendID() ;
        }
        else if( (sb.length() == 5 && sb.charAt(0) == BP_ESC)){
            if( firstPart == true ){
                //this has been received out of order, don't send anything...but clear buffer
                return true ;
            }
            else{
                ChangeLoggedInTo(true) ;
                //System.out.println("\t\tReceived: <ESC>PG1<CR>\n\t\tSending: 110 TAPIn Ready...\n\tLogged In") ;
                WriteToLogger(sb, 'R') ;
                return SendTAPInReady() ;
            }
        }
        else{
            //System.out.println("LoginController.Process()\n\tReceived something else - Returning FALSE: not a full string") ;
            return false ;
        }
        //return true ;
    }

    public boolean SendID(){
        String id = "ID=" ;
        StringBuffer sb = new StringBuffer(id) ;
        sb = sb.append(BP_CR).append(BP_LF) ;
        firstPart = false ;
        pnabp.getCoreParent().WriteToSerialPort(sb) ;
        return true ;
    }

    public boolean SendTAPInReady(){
        char[] tmp110 = {0x31,0x31,0x30,0x20,0x31,0x2e,0x37,
                        0x54,0x41,0x50,0x49,0x6e,0x20,0x52,0x65,0x61,
                        0x64,0x79,0x0d,0x06,0x0d,0x1b,0x5b,0x70,0x0d} ;

        String tmp = new String(tmp110) ;
        StringBuffer sb = new StringBuffer(tmp) ;
        pnabp.getCoreParent().WriteToSerialPort(sb) ;
        return true ;
    }

    public boolean LogOff(){
        char[] tmpgoodbye = {BP_CR,BP_ESC,BP_EOT,BP_CR,BP_CR} ;
        String tmp = new String(tmpgoodbye) ;
        StringBuffer sb = new StringBuffer(tmp) ;
        ChangeLoggedInTo(false) ;
        firstPart = true ;//restart for next log in
        pnabp.getCoreParent().WriteToSerialPort(sb) ;
        return true ;
    }

    public boolean SendACK(StringBuffer curInStr){
        char[] tmpack = {BP_CR,BP_ACK,BP_CR} ;
        String tmp = new String(tmpack) ;
        StringBuffer sb = new StringBuffer(tmp) ;
        pnabp.getCoreParent().WriteToSerialPort(sb) ;
        return true ;
    }

    public boolean SendNAK(){
            char[] tmpnak = {BP_CR,BP_NAK,BP_CR} ;
            String tmp = new String(tmpnak) ;
            StringBuffer sb = new StringBuffer(tmp) ;
            pnabp.getCoreParent().WriteToSerialPort(sb) ;
            return true ;
    }

    public boolean CheckSum(StringBuffer thisStr){
        int givenCSvalue = 0 ;
        int phase = 48 ;
        thisStr = thisStr.deleteCharAt( thisStr.length() - 1 ) ;//remove last <CR> to compute LRC easier
        for(int i = 0 ; i < 3 ; i++){
            if( i == 0 )
                givenCSvalue += thisStr.charAt( thisStr.length() - 1 ) - phase;
            else if(i == 1)
                givenCSvalue += (thisStr.charAt( thisStr.length() - 1 ) - phase) * 16 ;
            else if(i == 2)
                givenCSvalue += (thisStr.charAt( thisStr.length() - 1 ) - phase) * 256 ;
            thisStr = thisStr.deleteCharAt( thisStr.length() - 1 ) ;
        }
        int workedCSvalue = 0 ;
        for( int i = 0 ; i < thisStr.length() ; i++ ){//NOTE: THERE IS AN EQUAL SIGN ON THE WAY DOWN IN THE FOR LOOP!!!!!!!!!!!!!!
            workedCSvalue += thisStr.charAt(i) ;
            //System.out.println("value = " + Integer.toHexString(workedCSvalue) ) ;
        }
        if( workedCSvalue != givenCSvalue ){
            System.out.println("Checksum disagree - theirs | ours is " + Integer.toHexString(givenCSvalue) + " | " + Integer.toHexString(workedCSvalue) ) ;
        }
        return true ;
    }

    private void ChangeLoggedInTo(boolean loggedIn){
            this.loggedIn = loggedIn ;
        }

        private String GetControlChar(char controlChar){
        //System.out.println("Received CONTROL CHARACTER") ;
        String tmpStr = "TMP" ;
        switch( (int)controlChar ){
            case BP_STX:   tmpStr = "<STX>" ;
                        break ;
            case BP_ETX:   tmpStr = "<ETX>" ;
                        break ;
            case BP_CR:    tmpStr = "<CR>" ;
                        break ;
            case BP_EOT:   tmpStr = "<EOT>" ;
                        break ;
            case BP_ACK:   tmpStr = "<ACK>" ;
                        break ;
            case BP_LF:    tmpStr = "<LF>" ;
                        break ;
            case BP_NAK:   tmpStr = "<NAK>" ;
                        break ;
            case BP_RS:   tmpStr = "<RS>" ;
                        break ;
            case BP_ESC:    tmpStr = "<ESC>" ;
                        break ;
            default:    System.out.println("Error in control char switch statement") ;
                        break ;


        }
        if( tmpStr.equals("TMP" ) )
            return Integer.toHexString( (int)controlChar ) ;
        else
            return tmpStr ;

    }

        private void WriteToLogger(StringBuffer strBuf, char s_or_r){
            String str ;
            str = Character.toString(s_or_r) + ": " ;
            for(int i = 0 ; i < strBuf.length() ; i++){
                char tmpChar = strBuf.charAt(i) ;
                if( Character.isISOControl(tmpChar) )
                    str += GetControlChar(tmpChar) ;
                else
                    //str += Integer.toHexString( strBuf.charAt(i) ) + " ";
                    str += Character.toString(strBuf.charAt(i) ) ;
            }
            log.info(str) ;
        }


}
