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
import com.nursepaging.pna.*;
import java.util.Properties;
import java.util.Random;
import org.opencsta.servicedescription.common.AgentEvent;
import org.opencsta.servicedescription.common.CallEvent;
//import org.apache.log4j.Logger ;

/**
 *
 * @author  root
 */
public class PNA_BP implements TAP_Constants,NursepagingApp{
//    protected static org.apache.log4j.Logger log = Logger.getLogger(PNA_BP.class);
    //FOR TAP/PET logging in
    private static LoginController tap ;
    private final String VERSION = "3.0-rc2" ;
    private final String IMPLEMENTATION = "Basepage2000/Fusion" ;
    private CorePNA coreParent ;
    private Random generator ;
    public PNA_BP(){
        super() ;
    }

    public PNA_BP(Properties props){
        
    }

    public PNA_BP(CorePNA core){
        this.coreParent = core ;
    }

    @SuppressWarnings("static-access")
    public void run(){
//        logRunMessage() ;
//        while(true){
//            try {
//                synchronized(this) {
//                    wait(3000) ;
//                }
//            }catch(InterruptedException e){
//                getLog().warn("Interrupted Exception") ;
//            }
            synchronized(getCoreParent().getPnaSendList()){
                getCoreParent().checkRXList() ;
                if( getCoreParent().getPnaSendList().size() > 0 ){
                    while( getCoreParent().getPnaSendList().size() > 0 ){
                        getCoreParent().getLog().info("pnaSendList for " + getImplementation() + " looping as required") ;
                        PNAMessageBP sm = (PNAMessageBP)getCoreParent().getPnaSendList().remove(0) ;
                        String ext = sm.GetExtensionNumber() ;
                        String display = sm.GetMessage() ;
                        //String msg_id = sm.GetMsgID() ;
                        synchronized(getCoreParent().getCurrentlyDisplaying()){
                            if( getCoreParent().getCurrentlyDisplaying().containsKey(ext) ){
                                //new message received and need to add to list of messages for that ext
                                ExtensionDisplayManagerBP edm = (ExtensionDisplayManagerBP)getCoreParent().getCurrentlyDisplaying().remove(ext) ;
                                if( edm.AddMessage(sm) ){
                                    PNAMessageBP tmp = edm.GetCurrentMessage() ;
//                                    System.out.println("\n\t\tCHANGING TO NEW MESSAGE WITH A '<' IN PLACE") ;
                                    getCoreParent().getCsta().SetDisplay(ext,("<"+display),getCoreParent().isAudible() ) ;//'<' indicates there is a previous message
                                    getCoreParent().getLog().info("Ext: " + ext + " " + display) ;
                                }
                                else{
                                    getCoreParent().getLog().warn("Cannot Add SourceMessage to ExtensionDisplayManager List") ;
                                }
                                getCoreParent().getCurrentlyDisplaying().put(ext, edm) ;
                            }
                            else{ //totally new handset brought into system
                                getCoreParent().getLog().info(getClass().getName() + " - new handset to display") ;
                                getCoreParent().getCsta().SetDisplay(ext,display,getCoreParent().isAudible() ) ; //set the display
                                ExtensionDisplayManagerBP edm = new ExtensionDisplayManagerBP(ext,sm) ;
                                getCoreParent().getCurrentlyDisplaying().put(ext,edm) ;//extension is currentlyDisplaying
                                getCoreParent().getLog().info("Ext: " + ext + " " + display) ;
                            }
                        }
                        try {
                            synchronized(this) {
                                wait(200) ;
                            }
                        }catch(InterruptedException e){
                            getCoreParent().getLog().warn("Interrupted Exception") ;
                        }
                    }
                }
            }
//        }
    }

    @SuppressWarnings("static-access")
    public boolean checkReceived(StringBuffer curInStr){
        if( !tap.TAPLoggedIn() )
            return tap.Process(curInStr) ;
        else if( getCoreParent().isCurrentlyReceiving() == true ){
            if( getCoreParent().isChecksumToCome() == false && curInStr.charAt( (curInStr.length()-1) ) == BP_ETX){
                getCoreParent().setChecksumToCome(true) ;
                return false ;
            }
            else if( getCoreParent().isCurrentlyReceiving() == true ){
                if( curInStr.charAt( (curInStr.length()-1) ) == BP_CR ){
                    //COMLETE STRING
                    getCoreParent().setChecksumToCome(false) ;
                    getCoreParent().setCurrentlyReceiving(false) ;
                    getCoreParent().WriteToLogger(curInStr, 'R') ;
                    if( tap.CheckSum(curInStr) ){
                        this.addRXListBP(curInStr) ;
//                        try{
//                            synchronized(this){
//                                this.notify() ;
//                            }
//                        }catch(IllegalMonitorStateException e){
//                            e.printStackTrace() ;
//                        }
                        return tap.SendACK(curInStr) ;//returns true once complete
                    }
                    else
                        return tap.SendNAK() ;//returns true once complete
                }
                return false ;
            }
        }
        else if( curInStr.charAt(0) == BP_STX && curInStr.length()==1 ){
            getCoreParent().setCurrentlyReceiving(true) ;
            return false ;
        }
        else if( curInStr.length() == 2 && curInStr.charAt(0) == BP_EOT){
            //time to log off
//            System.out.println("EOT RECEIVED") ;
            getCoreParent().WriteToLogger(curInStr, 'R') ;
            getCoreParent().setCurrentlyReceiving(false) ;
            return tap.LogOff() ;
        }
        else{
            return false ;
        }
        return false ;
    }

    @SuppressWarnings({"static-access", "empty-statement"})
    public void ClearMessageViaTDS(String dev,String code,String data){
//                System.out.println("\n\tPNA_BASEPage\n\tDevice: " + dev + "\n\tCode: " + code + "\n\tData: " + data) ;
//                String tmp2 = dev + code + data ;
//                for(int i = 0 ; i < tmp2.length() ; i++)
//                    System.out.println( Integer.toHexString( (int)tmp2.charAt(i) ) ) ;
        getCoreParent().LogTDSAction(dev,code,data) ;
        if( code.equals(getCoreParent().getREMOVE()) ){//REMOVE DISPLAYED MESSAGE
            synchronized(getCoreParent().getCurrentlyDisplaying()){
                if( getCoreParent().getCurrentlyDisplaying().containsKey(dev) ){//we have an item to remove
                    ExtensionDisplayManagerBP edm = (ExtensionDisplayManagerBP)getCoreParent().getCurrentlyDisplaying().remove(dev) ;
                    if( edm.RemoveSM(data) ){//remove SM with msg_id == data
                        if( edm.HasMoreMessages() ){
                            //GET NEXT MSG AND DISPLAY
//                            System.out.println("MORE MESSAGES TO DIPLAY") ;
                            PNAMessageBP sm = edm.GetCurrentMessage() ;
                            String display = sm.GetMessage() ;
                            String msg_id = sm.GetMsgID() ;
                            if( edm.CurrentMessagePosition() == 0 ){//0 indicates only one message, no <> needed
                                getCoreParent().getCsta().SetDisplay(dev,display, getCoreParent().isAudible() ) ;
                                getCoreParent().getLog().info("Ext: " + dev + " " + display) ;
                            }
                            else if( edm.CurrentMessagePosition() == 1 ){//indicates has previous only, needs <
                                getCoreParent().getCsta().SetDisplay(dev,("<"+display),getCoreParent().isAudible() ) ;
                                getCoreParent().getLog().info("Ext: " + dev + " " + display) ;
                            }
                            else if( edm.CurrentMessagePosition() == 2 ){//indicates has next only, needs >
                                getCoreParent().getCsta().SetDisplay(dev,(">"+display),getCoreParent().isAudible() ) ;
                                getCoreParent().getLog().info("Ext: " + dev + " " + display) ;
                            }
                            else if( edm.CurrentMessagePosition() == 3){//indicates both next and previous, <> needed
                                getCoreParent().getCsta().SetDisplay(dev,"<>"+display,getCoreParent().isAudible() ) ;
                                getCoreParent().getLog().info("Ext: " + dev + " " + display) ;
                            }
                            //PUT BACK INTO currentlyDisplaying
                            getCoreParent().getCurrentlyDisplaying().put(dev,edm) ;
                        }
                        else{//NO MORE MESSAGES FOR THIS EXTENSION, CLEAR DISPLAY
//                            System.out.println("\nNO MORE ELEMENTS TO DISPLAY, CLEARING DISPLAY") ;
                            getCoreParent().getCsta().SetDisplay(dev,"",false) ;
                            getCoreParent().getLog().info("Ext: " + dev + " " + "B L A N K") ;
                        }
                    }
                    else{//WRONG MSG_ID GIVEN DURING NURSE INPUT FOR TDS
                        //MUST PUT BACK INTO currentlyDisplaying COS STILL IS
//                        System.out.println("\n\n\t\t\tWRONG MSG_ID, COULDN'T REMOVE") ;
                        getCoreParent().getCurrentlyDisplaying().put(dev,edm) ;
                    }
                }
                else{//MISTAKEN TDS TRANSMISSION
                    //DO NOTHING
                    getCoreParent().getLog().warn(getClass().getName() + " - MISTAKEN TRANSMISSION") ;
                }
            }
        //ConcludeAction(dev) ;
        }
        else if( code.equals(getCoreParent().getPREVIOUS()) ){//SCROLL PREVIOUS TO DISPLAYED MESSAGE
            synchronized(getCoreParent().getCurrentlyDisplaying()){
                if( getCoreParent().getCurrentlyDisplaying().containsKey(dev) ){
                    ExtensionDisplayManagerBP edm = (ExtensionDisplayManagerBP)getCoreParent().getCurrentlyDisplaying().remove(dev) ;
                    if( edm.CurrentMessagePosition() == 0 ){//0 indicates only one message, nothing to do
                                                            //but put back into hashtable currentlyDisplaying
                        getCoreParent().getCurrentlyDisplaying().put(dev,edm) ;
                        //ConcludeAction(dev) ;
                        return ;
                    }
                    edm.SetCurrentMessage(getCoreParent().getPREVIOUS()) ;
                    PNAMessageBP tmp = edm.GetCurrentMessage() ;
                    String display = tmp.GetMessage() ;
                    if( edm.CurrentMessagePosition() == 2 ){//next only
                        getCoreParent().getCsta().SetDisplay(dev,(">"+display),getCoreParent().isAudible()) ;
                        getCoreParent().getLog().info("Ext: " + dev + " " + display) ;
                    }
                    else if( edm.CurrentMessagePosition() == 3 ){//both next and previous
                        getCoreParent().getCsta().SetDisplay(dev,("<>"+display),getCoreParent().isAudible()) ;
                        getCoreParent().getLog().info("Ext: " + dev + " " + display) ;
                    }
                    getCoreParent().getCurrentlyDisplaying().put(dev,edm) ;
                }
                else
                    ;
                }
            //ConcludeAction(dev) ;
        }
        else if( code.equals(getCoreParent().getNEXT()) ){//SCROLL NEXT TO DISPLAYED MESSAGE
            synchronized(getCoreParent().getCurrentlyDisplaying()){
                if( getCoreParent().getCurrentlyDisplaying().containsKey(dev) ){
                    ExtensionDisplayManagerBP edm = (ExtensionDisplayManagerBP)getCoreParent().getCurrentlyDisplaying().remove(dev) ;
                    if( edm.CurrentMessagePosition() == 0 ){//0 indicates only one message, no scrolling
                                                            //except put back into hastable currentlyDisplaying
                        getCoreParent().getCurrentlyDisplaying().put(dev,edm) ;
                        //ConcludeAction(dev) ;
                        return ;
                    }
                    edm.SetCurrentMessage(getCoreParent().getNEXT()) ;
                    PNAMessageBP tmp = edm.GetCurrentMessage() ;
                    String display = tmp.GetMessage() ;
                    if( edm.CurrentMessagePosition() == 1 ){//previous only
                        getCoreParent().getCsta().SetDisplay(dev,("<"+display),getCoreParent().isAudible()) ;
                        getCoreParent().getLog().info("Ext: " + dev + " " + display) ;
                    }
                    else if( edm.CurrentMessagePosition() == 3 ){//both next and previous
                        getCoreParent().getCsta().SetDisplay(dev,("<>"+display),getCoreParent().isAudible()) ;
                        getCoreParent().getLog().info("Ext: " + dev + " " + display) ;
                    }
                    //PUT BACK INTO currentlyDisplaying
                    getCoreParent().getCurrentlyDisplaying().put(dev,edm) ;
                }
                else{
                    ;
                }
            }
        }
        else if( code.equals(getCoreParent().getCLEAR()) ){//REMOVE DISPLAYED MESSAGE
            getCoreParent().clearCordlessDisplay(dev);
//                  synchronized(currentlyDisplaying){
//                      if( currentlyDisplaying.containsKey(dev) ){//we have an item to remove
//                          ExtensionDisplayManagerBP edm = (ExtensionDisplayManagerBP)currentlyDisplaying.remove(dev) ;
//                          csta.SetDisplay(dev,"",false) ;
//                      }
//                      else{//MISTAKEN TDS TRANSMISSION
//                          //DO NOTHING
//                          log.warn("Mistaken TDS transmission - nothing to do") ;
//                      }
//                  }
        }
        //ConcludeAction(dev) ;
    }

    @SuppressWarnings("static-access")
    public void TurnLoggingOff(){
        getCoreParent().getLog().info("WANT TO SET LOGGING OFF - WHY?") ;
    }

    public static void main(String[] args){
        PNA_BP pna = new PNA_BP();
        pna.run();
    }

    @SuppressWarnings("static-access")
    public void init(){
//        String aud = getCoreParent().getTheProps().getProperty("AUDIBLE") ;
//        if( aud.equalsIgnoreCase("TRUE") ){
//            getCoreParent().setAudible(true) ;
//        }
//        else{
//            getCoreParent().setAudible(false) ;
//        }
        generator = new Random() ;
        getCoreParent().getLog().info("Starting TAP") ;
        tap = new LoginController(this,getCoreProperties()) ;
        NotifyTAP4Logs() ;
        coreParent.getLog().info(this.getClass().getName() + "BasePAGE Implementation Initialising");
    }


    @SuppressWarnings("static-access")
    private synchronized boolean addRXListBP(StringBuffer recd_string){
        recd_string = Strip(recd_string) ;//remove <STX>...<ETX>checksum<CR> leaving only '...'
        //recd_string = recd_string.append(" *** Added new msg to received BASEPage messages for processing") ;
        getCoreParent().WriteToLogger(recd_string, 'R') ;
        return (getCoreParent().getRxList().add(recd_string)) ;
    }

    private StringBuffer Strip(StringBuffer recd_string){
        for(int i = 0 ; i < 2 ; i++)
            recd_string = recd_string.deleteCharAt( recd_string.length()-1 ) ;
        recd_string = recd_string.deleteCharAt(0) ;
        return recd_string ;
    }

    private void NotifyTAP4Logs(){
        tap.Logs() ;
    }

    @SuppressWarnings("static-access")
    private synchronized void AddTxt(PNAMessageBP sm){
        synchronized(getCoreParent().getPnaSendList()){
            if( getCoreParent().getPnaSendList().add(sm) )
                ;
            else
                getCoreParent().getLog().warn("SourceMessageBASEPage not added to list") ;
        }
    }
    
    public void createPNAMessage(StringBuffer recd_string){
        int id = getCoreParent().NewMsgID() ;
        PNAMessageBP sm = new PNAMessageBP(recd_string,id) ;
        getCoreParent().addPNASendList(sm) ;
    }
    
        
    public String getVersion(){
        return VERSION ;
    }
    
    public String getImplementation(){
        return IMPLEMENTATION ;
    }

    public void CSTACallEventReceived(CallEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void CSTAAgentEventReceived(AgentEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void TDSDataReceived(String dev, String code, String data) {
        ClearMessageViaTDS(dev,code,data) ;
    }

    /**
     * @return the coreParent
     */
    public CorePNA getCoreParent() {
        return coreParent;
    }

    /**
     * @param coreParent the coreParent to set
     */
    public void setCoreParent(CorePNA coreParent) {
        this.coreParent = coreParent;
    }

    public String getCoreVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Properties getCoreProperties(){
        return coreParent.getTheProps() ;
    }

    public void addNewTestMessage(){
        String testmsg = "99/D/ALARM CALL: Test" + Integer.toString(generator.nextInt(1000)) ;
        coreParent.addRXList( new StringBuffer(testmsg) ) ;
    }
}

