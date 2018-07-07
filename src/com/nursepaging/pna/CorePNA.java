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


package com.nursepaging.pna;
import com.nursepaging.pna.apr.APR;
import com.nursepaging.pna.basepage.PNA_BP;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.* ;
import org.apache.log4j.Logger;
import org.opencsta.client.CSTAFunctions ;
import org.opencsta.client.hipath3000.CSTAClient3000;
import org.opencsta.apps.objects.CSTAApplication ;
import org.opencsta.servicedescription.common.CallEvent ;
import org.opencsta.servicedescription.common.AgentEvent ;

public class CorePNA implements TAP_Constants,CSTAApplication,Runnable{
    private static org.apache.log4j.Logger log = Logger.getLogger(CorePNA.class);
    private static Calendar timeStarted ;
    private final String CORE_VERSION = "3.0" ;
    /**
     * This is the list of processed rxList that now exist as a list of
     * SourceMessages ready to send as pnaSendList.
     */
    //PNA APP LOGIC FIELDS
    /**
     * The list of raw messages before they have been formatted into a
     * SourceMessage.
     */    
    private static List rxList ;//raw messages from basepage
    /**
     * Don't know exactly anymore.
     */    
    private static List pnaSendList ;//source messages ready for broadcast
    
    private static Hashtable currentlyDisplaying ;//exts currently displaying
    /**
     * The csta client that does all the csta stuff for this application.
     */    
    private static CSTAFunctions csta ;
    
    //CHECK RECEIVED BOOLEANS
    /**
     * currently receiving boolean
     */    
    private static boolean currentlyReceiving ;
    /**
     * check sum to come...maybe leftover stuff from basepage swapover
     */    
    private static boolean checksumToCome ;
    
    protected static int msg_id = 0 ;
    
    /**
     * The name of the application.  Used for process ID and serial port
     * ownership as well as properties information.
     */    
    protected static final String APPNAME = "PNA" ;
    
    //SCROLLING CONSTANTS
    /**
     * Key which is to be pressed for next message.  3
     */    
    private static final String NEXT = "3" ;
    /**
     * Key which is to be pressed for previous message. 1
     */    
    private static final String PREVIOUS = "1" ;
    /**
     * Key to press to remove message. 9
     */    
    private static final String REMOVE = "9" ;
    
    private static final String TEST = "7" ;
    private static final String CLEAR = "0" ;

    private boolean audible = true;
    private int testCycle ;
    private int testCycleCounter ;
    private boolean runFlag ;
    private int sleepCycle ;
    /**
     * The properties file
     */    
    private Properties theProps ;
    
    protected PNALayer1 layer1 ;
    private boolean noSerial ;
    private NursepagingApp implementation ;
    
    /** Creates a new instance of PNA_Base */
//    public CorePNA() {
//        log.info( this.getClass().getName() + " setting time started" ) ;
//        setTimeStarted( Calendar.getInstance() ) ;
////        theProps = PropertiesController.getInstance(APPNAME) ;//gets /etc/<APPNAME>.conf
//        setNoSerial(Boolean.valueOf(theProps.getProperty("IS_SERIAL"))) ;
//        setSleepCycle(Integer.valueOf(theProps.getProperty("SLEEP_CYCLE")) ) ;
//        setTestCycle(Integer.valueOf(theProps.getProperty("TEST_CYCLE")) ) ;
//        if( getTestCycle() > 0 ){
//            log.info( this.getClass().getName() + " test cycle has been set to " + getTestCycle() ) ;
//            setTestCycleCounter( getTestCycle() ) ;
//        }
//        CoreInit() ;
//        //Start csta link before grabbing ownership of serial port
//        log.info(this.getClass().getName() + " - initialising CSTA Client") ;
//        csta = new CSTAClient3000() ;
//        csta.RegisterParentApplication((CSTAApplication)this) ;
//        log.info(this.getClass().getName() + " - registered PNA as the parent application") ;
//        csta.TDSEnable() ;
//        log.info(this.getClass().getName() + " - enabled telephone data service") ;
//        log.info(this.getClass().getName() + " - creating serial port layer") ;
//        layer1 = new PNALayer1(this);
//        log.info(this.getClass().getName() + " - serial port layer created") ;
//        Init();
//        log.info(this.getClass().getName() + " is fully initialised") ;
//    }

//    public CorePNA(boolean _noSerial){
//        this() ;
//    }

    public CorePNA(Properties props,boolean _noSerial){
        log.info( this.getClass().getName() + " setting time started" ) ;
        setTimeStarted( Calendar.getInstance() ) ;
        theProps = props ;
        setNoSerial(Boolean.valueOf(theProps.getProperty("IS_SERIAL"))) ;
        setSleepCycle(Integer.valueOf(theProps.getProperty("SLEEP_CYCLE")) ) ;
        setTestCycle(Integer.valueOf(theProps.getProperty("TEST_CYCLE")) ) ;
        if( getTestCycle() > 0 ){
            log.info( this.getClass().getName() + " test cycle has been set to " + getTestCycle() ) ;
            setTestCycleCounter( getTestCycle() ) ;
        }
        CoreInit() ;
        //Start csta link before grabbing ownership of serial port
        log.info(this.getClass().getName() + " - initialising CSTA Client") ;
        csta = new CSTAClient3000(getTheProps()) ;
        while (true) {
            if( csta.getSocket() != null ) {
                log.info(this.getClass().getName() + " -> " + " connection to CSTA Server established (" + csta.getSocket() + ")");
                break;
            } else {
                try {
                    csta.release() ;
                    log.info(this.getClass().getName() + " -> " + " connection to CSTA Server failed, trying again in 5 seconds");
                    Thread.currentThread().sleep(5000);
                    csta = null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (NullPointerException e2) {
                    e2.printStackTrace();
                }
                csta = new CSTAClient3000( getTheProps() );
            }
        }


        
        csta.RegisterParentApplication((CSTAApplication)this) ;
        log.info(this.getClass().getName() + " - registered PNA as the parent application") ;
        csta.TDSEnable() ;
        log.info(this.getClass().getName() + " - enabled telephone data service") ;


        log.info(this.getClass().getName() + " - creating serial port layer") ;
        layer1 = new PNALayer1(this,theProps);
        log.info(this.getClass().getName() + " - serial port layer created") ;
        Init();
        log.info(this.getClass().getName() + " is fully initialised") ;
    }

    
    public void Init(){
        //which implementation will get started
        log.info(this.getClass().getName() + " -> Getting property: IMPLEMENTATION") ;
        setImplementation( getTheProps().getProperty("IMPLEMENTATION") ) ;
        log.info(this.getClass().getName() + " -> Initialising implementation: " + this.getImplementation() ) ;
        implementation.init() ;
        log.info(this.getClass().getName() + " -> Getting property: AUDIBLE (true/false)") ;
        String aud = getTheProps().getProperty("AUDIBLE") ;
        if( aud.equalsIgnoreCase("TRUE") ){
            setAudible(true) ;
            log.info(this.getClass().getName() + " - is audible") ;
        }
        else{
            setAudible(false) ;
            log.info(this.getClass().getName() + " - is running in silent mode") ;
        }
    }

    private void setImplementation(String imp){
        if( imp.equalsIgnoreCase("APR") ){
            implementation = new APR(this) ;
            log.info(this.getClass().getName() + " -> implementation set to APR") ;
        }
        else if( imp.equalsIgnoreCase("BP") ){
            implementation = new PNA_BP(this) ;
            log.info(this.getClass().getName() + " -> implementation set to basepage") ;
        }
    }
    
    protected void CoreInit(){
        log.info(this.getClass().getName() + " *** Core Components Initialising ***") ;
        setRunFlag(true) ;
        log.info(this.getClass().getName() + " - run flag is set to true") ;
        setRxList(Collections.synchronizedList(new LinkedList()));
        log.info(this.getClass().getName() + " - created received messages list") ;
        setPnaSendList(Collections.synchronizedList(new LinkedList()));
        log.info(this.getClass().getName() + " - created pna messages send list") ;
        setCurrentlyDisplaying(new Hashtable()) ;
        log.info(this.getClass().getName() + " - set currently displaying table") ;
        setCurrentlyReceiving(false) ;
        log.info(this.getClass().getName() + " - set currently receiving to false") ;
        setChecksumToCome(false) ;
        log.info(this.getClass().getName() + " - set checksum to come to false") ;
    }

        /**
     * @return the log
     */
    public static org.apache.log4j.Logger getLog() {
        return log;
    }

    /**
     * @param aLog the log to set
     */
    public static void setLog(org.apache.log4j.Logger aLog) {
        log = aLog;
    }

    public void KillServer(){
        log.info(this.getClass().getName() + " - killing PNA, run flag set to false") ;
        setRunFlag(false) ;
    }
    
    protected void BeautifyLogString(StringBuffer strBuf, char s_or_r){
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
        getLog().info(str) ;
    }

        /**
     * @return the pnaSendList
     */
    public static List getPnaSendList() {
        return pnaSendList;
    }

    /**
     * @param aPnaSendList the pnaSendList to set
     */
    public static void setPnaSendList(List aPnaSendList) {
        pnaSendList = aPnaSendList;
    }
    
    /**
     * Gets the control char given as the parameter and returns a <readable>
     * value of it.
     * @param controlChar The control character to convert from computer gibberish to readable, mainly for rxtx logs.
     * @return The readable representation of the control character.
     */    
    protected String GetControlChar(char controlChar){
        //System.out.println("Received CONTROL CHARACTER") ;
        String tmpStr = "TMP" ;
        switch( (int)controlChar ){
            case pnaSTX:   tmpStr = "<STX>" ;
                        break ;
            case pnaETX:   tmpStr = "<ETX>" ;
                        break ;
            case pnaCR:    tmpStr = "<CR>" ;
                        break ;
            case pnaEOT:   tmpStr = "<EOT>" ;
                        break ;
            case pnaACK:   tmpStr = "<ACK>" ;
                        break ;
            case pnaLF:    tmpStr = "<LF>" ;
                        break ;                        
            case pnaNAK:   tmpStr = "<NAK>" ;
                        break ;
            case pnaRS:   tmpStr = "<RS>" ;
                        break ;
            case pnaESC:    tmpStr = "<ESC>" ;
                        break ;                        
            default:    getLog().warn("Error in control char switch statement with " + Integer.toHexString((int)controlChar) ) ;
                        break ;
            
            
        }
        if( tmpStr.equals("TMP" ) )
            return Integer.toHexString( (int)controlChar ) ;
        else
            return tmpStr ;
    }

    //THIS METHOD MUST BE OVERRIDDEN!
    /**
     * The serial port checks with this method whether to clear the buffer
     * for the next transmission because this one is over.  Thus, this method
     * is over-written in each implementation of a protocol.
     * @param curInStr the currently received string
     * @return whether it is complete and to clear the buffer, or not.
     */    
    public boolean CheckReceived(StringBuffer curInStr) {
        return implementation.checkReceived(curInStr);
    }
    
    /**
     * Adds the received string to the rxList list.
     * @param recd_string the string to add to the rxList list
     * @return true when added to the list
     */    
    public synchronized boolean addRXList(StringBuffer recd_string){
        BeautifyLogString(recd_string, 'R') ;
        getLog().info("Adding new received nursecall message") ;
        return (getRxList().add(recd_string)) ;
    }
    
    public int NewMsgID(){
        int tmp = msg_id ;
        log.info(this.getClass().getName() + " message id created: " + Integer.toString(tmp)) ;
        msg_id++ ;
        return tmp ;
    }
    
    /**
     * Makes a new SourceMessage object based on the received string then calls
     * the method to add the newly created SourceMessage object to the pnaSendList list
     * @param recd_string the string to make the SourceMessage from
     */    
    public void createPNAMessage(StringBuffer recd_string){
//        int id = NewMsgID() ;
//        PNAMessage sm = new PNAMessage(recd_string, id) ; 
//        addPNASendList(sm) ;
        implementation.createPNAMessage(recd_string);

    }
    
    
    /**
     * Adds the newly created SourceMessage to the pnaSendList list
     * @param sm the SourceMessage to add to the pnaSendList list
     */    
    public synchronized void addPNASendList(PNAMessage sm){
        getLog().info(this.getClass().getName() + " - converted raw nursecall message to PNA Message - added to queue") ;
        synchronized(getPnaSendList()){
            if( getPnaSendList().add(sm) )
                ;
            else
                getLog().warn("SourceMessage_Base not added to list") ;
        }
    }
    
    /**
     * gets the next available message from the rxList list for processing
     * @return the next string(buffer) on the rxList list
     */    
    protected synchronized StringBuffer getRXMsg(){
        StringBuffer sb ;
        //GET THE FIRST INDEX
        try{
            sb = (StringBuffer)getRxList().remove(0) ;
            return sb ;
        }catch(IndexOutOfBoundsException e){
            e.printStackTrace() ;
        }
        return null ;
    }

    public void kill(){
        setRunFlag(false);
    }
    
    public void run(){
        logRunMessage() ;
        while( isRunFlag() ){
            if( getTestCycle() > 0 ){
                log.info(this.getClass().getName() + " - entering a test cycle") ;
                if( getTestCycleCounter() == 0 ){
                    log.info(this.getClass().getName() + " - test cycle will execute now") ;
                    //implementation add test case
                    implementation.addNewTestMessage() ;
                    //reset counter
                    setTestCycleCounter( getTestCycle() ) ;
                }
                else{
                    setTestCycleCounter( (getTestCycleCounter()-1) ) ;
                }
            }
            try {
                synchronized(this) {
                    wait( getSleepCycle() ) ;
                    implementation.run();
                }
            }catch(InterruptedException e){
                getLog().warn("Interrupted Exception") ;
            }
        }
        log.info(this.getClass().getName() + " !! " + " exiting run loop") ;
    }

    public boolean isRunFlag() {
        return runFlag;
    }

    public void setRunFlag(boolean runFlag) {
        this.runFlag = runFlag;
    }

    public void WriteToSerialPort(StringBuffer sb){
        WriteToLogger(sb, 's') ;
        layer1.SendString(sb);
    }

    public void WriteToLogger(StringBuffer strBuf, char s_or_r){
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
        getLog().info(str) ;
    }

    public int GetRegisteredCordlessCount(){
        return 55 ;
    }

    public void AddClearDisplayTxt(PNAMessage sm){
        addPNASendList(sm) ;
    }

    /**
     * @return the timeStarted
     */
    public static Calendar getTimeStarted() {
        return timeStarted;
    }

    /**
     * @param aTimeStarted the timeStarted to set
     */
    public static void setTimeStarted(Calendar aTimeStarted) {
        timeStarted = aTimeStarted;
    }

    /**
     * @return the noSerial
     */
    public boolean isNoSerial() {
        return noSerial;
    }

    /**
     * @param noSerial the noSerial to set
     */
    public void setNoSerial(boolean noSerial) {
        this.noSerial = noSerial;
    }
    
    public String getVersion(){
        return "3.0" ;
    }
    
    public String getImplementation(){
        return "CorePNA" ;
    }
    
    public String getCoreVersion(){
        return CORE_VERSION ;
    }
    
    public void LogTDSAction(String dev, String code, String data){
        String m = "Clearing message with TDS - " ;
        m += "Device: " + dev + ", Code: " + code + ", Data: " + data ;
        getLog().info(m) ;
    }
    
    public void logRunMessage(){
        getLog().info("Running " + getImplementation() + " version " + getVersion() + " using Core " + getCoreVersion() ) ;
    }
    
    public void checkRXList(){
        
        if( getRxList().size() > 0 ){//a new string received
            getLog().info(this.getClass().getName() + " RX list has some messages");
            for(int i = 0 ; i < getRxList().size() ; i++){
                //some parsing etc...
                StringBuffer sb = getRXMsg() ;
                if(sb == null)
                    continue ;
                else
                    createPNAMessage(sb) ;
            }
        }
    }
    
    public void clearCordlessDisplay(String dev){
      synchronized(getCurrentlyDisplaying()){
          if( getCurrentlyDisplaying().containsKey(dev) ){//we have an item to remove
              Object edmdel = getCurrentlyDisplaying().remove(dev) ;
                getCsta().SetDisplay(dev,"",false) ;
              edmdel = null ;
          }
          else{//MISTAKEN TDS TRANSMISSION
              //DO NOTHING
                getLog().warn("Mistaken TDS transmission - nothing to do") ;
          }
      }
    }

    public boolean isAudible() {
        return audible;
    }

    public void setAudible(boolean aud) {
        this.audible = aud;
    }

    /**
     * @return the theProps
     */
    public Properties getTheProps() {
        return theProps;
    }

    /**
     * @param theProps the theProps to set
     */
    public void setTheProps(Properties theProps) {
        this.theProps = theProps;
    }

    public Properties getCoreProperties(){
        return getTheProps() ;
    }

        /**
     * @return the currentlyReceiving
     */
    public static boolean isCurrentlyReceiving() {
        return currentlyReceiving;
    }

    /**
     * @param aCurrentlyReceiving the currentlyReceiving to set
     */
    public static void setCurrentlyReceiving(boolean aCurrentlyReceiving) {
        currentlyReceiving = aCurrentlyReceiving;
    }

    /**
     * @return the currentlyDisplaying
     */
    public static Hashtable getCurrentlyDisplaying() {
        return currentlyDisplaying;
    }

    /**
     * @param aCurrentlyDisplaying the currentlyDisplaying to set
     */
    public static void setCurrentlyDisplaying(Hashtable aCurrentlyDisplaying) {
        currentlyDisplaying = aCurrentlyDisplaying;
    }

    /**
     * @return the csta
     */
    public static CSTAFunctions getCsta() {
        return csta;
    }

    /**
     * @param aCsta the csta to set
     */
    public static void setCsta(CSTAFunctions aCsta) {
        csta = aCsta;
    }

    /**
     * @return the NEXT
     */
    public static String getNEXT() {
        return NEXT;
    }

    /**
     * @return the PREVIOUS
     */
    public static String getPREVIOUS() {
        return PREVIOUS;
    }

    /**
     * @return the REMOVE
     */
    public static String getREMOVE() {
        return REMOVE;
    }

    /**
     * @return the TEST
     */
    public static String getTEST() {
        return TEST;
    }

    /**
     * @return the CLEAR
     */
    public static String getCLEAR() {
        return CLEAR;
    }

    /**
     * @return the rxList
     */
    public static List getRxList() {
        return rxList;
    }

    /**
     * @param aRxList the rxList to set
     */
    public static void setRxList(List aRxList) {
        rxList = aRxList;
    }

    /**
     * @return the checksumToCome
     */
    public static boolean isChecksumToCome() {
        return checksumToCome;
    }

    /**
     * @param aChecksumToCome the checksumToCome to set
     */
    public static void setChecksumToCome(boolean aChecksumToCome) {
        checksumToCome = aChecksumToCome;
    }

    public static void main(String[] argv){
        setFileEncoding() ;
        Properties someProps = loadPropertiesFromFile() ;
        CorePNA app = new CorePNA(someProps, false) ;
        app.run();
    }

    private static Properties loadPropertiesFromFile(){
        return loadPropertiesFromFile("corepna.conf") ;
    }

    private static Properties loadPropertiesFromFile(String filename){
        FileInputStream is ;
        try {
            System.out.println("Trying to load properties from:  " + System.getProperty("user.dir") + "/" + filename) ;
            is = new FileInputStream( (System.getProperty("user.dir") + "/"+filename) );
            Properties props = new Properties() ;
            props.load(is) ;
            return props ;
        }catch (FileNotFoundException ex) {
            ex.printStackTrace() ;
        } catch (IOException ex) {
            ex.printStackTrace() ;
        }
        return null ;
    }

    private Properties loadProperties(InputStream propstream){
        Properties props ;
        try {
            props = new Properties();
            props.load(propstream);
            return props ;
        } catch (IOException ex) {
            ex.printStackTrace();
            props = null ;
        }
        return null ;
    }

    public void CSTACallEventReceived(CallEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void CSTAAgentEventReceived(AgentEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void TDSDataReceived(String dev, String code, String data) {
        implementation.TDSDataReceived(dev, code, data);
    }

    private static void setFileEncoding(){
        System.out.println(System.getProperty("file.encoding")) ;
        System.setProperty("file.encoding", "ISO-8859-1") ;
        System.out.println(System.getProperty("file.encoding")) ;
    }

    /**
     * @return the testCycle
     */
    public int getTestCycle() {
        return testCycle;
    }

    /**
     * @param testCycle the testCycle to set
     */
    public void setTestCycle(int testCycle) {
        this.testCycle = testCycle;
    }

    /**
     * @return the testCycleCounter
     */
    public int getTestCycleCounter() {
        return testCycleCounter;
    }

    /**
     * @param testCycleCounter the testCycleCounter to set
     */
    public void setTestCycleCounter(int testCycleCounter) {
        this.testCycleCounter = testCycleCounter;
    }

    /**
     * @return the sleepCycle
     */
    public int getSleepCycle() {
        return sleepCycle;
    }

    /**
     * @param sleepCycle the sleepCycle to set
     */
    public void setSleepCycle(int sleepCycle) {
        this.sleepCycle = sleepCycle;
    }

    public void cstaFail() {
        log.warn(this.getClass().getName() + " !! " + "csta Fail") ;
        try{
            layer1.closeComms();
        }catch(NullPointerException e){
            ;
        }
        this.setRunFlag(false);
    }
}
