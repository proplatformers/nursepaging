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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.nursepaging.pna.utils.Extension;

/**
 *
 * @author  root
 */
public class ExtensionDisplayManager {
	protected static org.apache.log4j.Logger log = Logger.getLogger(CorePNA.class);
    protected PNAMessage currentMessage ;//the SourceMessage displayed
    protected List messages ;//all SourceMessages for this extension
    protected ListIterator li ;
    protected Extension extension ;
    protected int index ;
    protected static final String PREVIOUS = "1" ;
    protected static final String NEXT = "3" ;
    protected static final String REMOVED = "REMOVED" ;
    protected static final String ADDED = "ADDED" ;
    protected Properties theProps ;
    protected String logStr = "" ;
    
    /** Creates a new instance of ExtensionDisplayManager */
    public ExtensionDisplayManager(String ext, PNAMessage newMsg) {
        Init() ;
        this.extension = new Extension() ;
        this.extension.setExtensionNumber(ext) ;
        this.currentMessage = newMsg ;
        if( addDisplayMessage(currentMessage) ){
            ;
        }
        log.info("New EDM for " + this.extension.getExtensionNumber() + " created.  PNAMessage added.") ;
    }
    
    public void Init(){
        messages = Collections.synchronizedList( new LinkedList() ) ;
        li = messages.listIterator() ;
        index = -1 ;
    }
    
    public boolean removeDisplayMessage(String station){
        if( currentMessage.getStation().equals(station) ){
            return removeDisplayMessage() ;
        }
        synchronized(messages){
            for(int i = 0 ; i < messages.size() ; i++ ){
                PNAMessage tmp = (PNAMessage)messages.get(i) ;
                if( station.equals( tmp.getStation()) ){
                    messages.remove(i) ;
                    if( i < index ){
                        index-- ;
                    }
                }
                return true ;
            }
        }
        return true ;
    }
    
    
    public boolean removeDisplayMessage(){
        
        //GO THROUGH LIST OF SOURCEMESSAGES AND FIND THE CORRECT msg_id
        //FIRST, CHECK currentMessage
        //        if( msg_id.equals( currentMessage.GetMsgID()) ){
        //            System.out.println("\n\n\tREMOVING MESSAGE") ;
        //            currentMessage = null ;
        //           return true ;
        //        }
        
        //IF IT WAS currentMessage, THEN IT MUST BE REMOVED FROM THE LIST, AND
        //IF IT WASN'T CURRENT MESSAGE IT HAS TO BE REMOVED AS WELL
        //GO THRU LIST HERE
        //System.out.println("\n\tThis mes_id to remove is: " + msg_id) ;
        TestTheList() ;
        logStr += "EDM: + " + this.extension.getExtensionNumber() + " - REMOVING A MESSAGE....." ;
        synchronized(messages){
            //System.out.println("Prior to removal, allMessages.size() is: " + Integer.toString(allMessages.size()) ) ;
            for( int i = 0 ; i < messages.size() ; i++ ){
                PNAMessage sm = (PNAMessage)messages.get(i) ;
                if( currentMessage == sm ){
                    logStr += "with ID: " + sm.getMessageID() ;
                    messages.remove(i) ;
                    log.info(logStr) ;
                    logStr = "" ;
                    setCurrentDisplayMessage(REMOVED) ;
                    if(index >= 0)
                        TestTheList() ;
                    return true ;
                }
            }
            //                System.out.println("Trying to match up this msg_id is: " + sm.GetMsgID() ) ;
            //                if( sm.GetMsgID().equals(msg_id) ){
            //                   System.out.println("\n\t\tREMOVING A MESSAGE\n\tSIZE NOW: " + Integer.toString(allMessages.size()) ) ;
            //                    allMessages.remove(i) ;
            //                    return true ;
            //                }
            //                else{
            //                    continue ;
            //                }
        }
        TestTheList() ;
        return false ;
    }

    public void removeAgedMessage(String id_of_msg){
        log.info(this.getClass().getName() + " --- " + "removeAgedMessage with MSGID: " + id_of_msg);
        log.info(this.getClass().getName() + " --- " + " size of messages is: " + messages.size()) ;
        synchronized(messages){
            for( int i = 0 ; i < messages.size() ; i++ ){
                PNAMessage sm = (PNAMessage)messages.get(i) ;
                if( id_of_msg.equalsIgnoreCase( sm.getMessageID() )){
                    logStr += "with ID: " + sm.getMessageID() ;
                    messages.remove(i) ;
                    log.info(logStr) ;
                    logStr = "" ;
                    setCurrentDisplayMessage(REMOVED) ;
                    break ;
                }
            }
        }
    }
    
    public int currentDisplayMessagePosition(){
        int value = 0 ;
        synchronized(messages){
            //            if( l_itr.hasPrevious() )
            //                value += 1 ;//having next adds 1
            //            if( l_itr.hasNext() )
            //                value += 2 ;//having previous adds 2
            int size = messages.size() ;
            if( index > 0 )
                value += 1 ;//has previous
            if( index < (size-1) )//has next
                value += 2 ;
        }
        return value ;
    }
    
    public PNAMessage getCurrentDisplayMessage(){
        return currentMessage ;
    }
    
    public void setCurrentDisplayMessage(String val){//NEXT = "3", PREVIOUS = "1", "ANY" is for non-scrolled change
        //GETS NEXT MESSAGE TO DISPLAY AND THIS MESSAGE IS SET TO currentMessage
        logStr += "EDM for extension *** " + this.extension.getExtensionNumber() + " ***";
        synchronized(messages){
            //if( currentMessage == null )
            //    System.out.println("\n\t\tcurrentMessage == null, GETTING MOST-RECENT MESSAGE") ;
            if( val.equals(REMOVED) ){
                int size = messages.size() ;
                logStr += "\n\tSize of message list: " + size + "\tOld index: " + index ;
                //BECAUSE INDEX STARTS AT 0, IT CAN NEVER EQUAL SIZE.  THEREFORE
                //THE REMOVED MESSAGE WAS THE END MESSAGE.  INDEX CONSEQUENTLY
                //GOES TO THE NEW LAST MESSAGE WHICH IS SIZE-1.
                if( index == size )
                    index-- ;
                else if( (index+1) <= size )
                    ;//index stays the same because the newer msg is now in the old messages spot.
                
                logStr += "\tNew index: " + index + "\n\t(same as old if mid.msg deletion.  -1 if end msg deleted)";
            } else if( val.equals(PREVIOUS) && hasPrevious() ){
                index-- ;
            } else if( val.equals(NEXT) && hasNext() ){
                index++ ;
            } else if( val.equals(ADDED) ){
                index = messages.size() - 1 ;
                logStr += "\nMessage added. index = " + index ;
            }
            if( index >= 0 )
                currentMessage = (PNAMessage)messages.get(index) ;
            else
                currentMessage = null ;
        }
        log.info(logStr) ;
        logStr = "" ;
        TestTheList() ;
    }
    
    private synchronized boolean hasNext(){
        int size = messages.size() ;
        if( index < (size-1) )//THEN THERE IS A NEXT_ hehe
            return true ;
        else
            return false ;
    }
    
    private synchronized boolean hasPrevious(){
        if( index > 0 )//THEN THERE IS A PREVIOUS
            return true ;
        else
            return false ;
    }
    
    public boolean addDisplayMessage(PNAMessage sm){
        boolean val ;
        synchronized(messages){
            val = messages.add(sm) ;
            setCurrentDisplayMessage(ADDED) ;
        }
        
        return val ;
    }
    
    public boolean hasMoreMessages(){
        //RETURN TRUE IF THERE ARE MORE SOURCEMESSAGES IN THE LIST OR FALSE IF EMPTY
        synchronized(messages){
            if( messages.size() > 0 ){
                System.out.println("\n\n\tHAS " + Integer.toString(messages.size()) + " MESSAGES LEFT") ;
                return true ;
            } else
                return false ;
        }
    }
        
//    public PNAMessage removeAllListings(String machine){
//        synchronized(messages){
//            //            for(int i = 0 ; i < allMessages.size() ; i++){
//            //                SourceMessage_Base tmp = (SourceMessage_Base)allMessages.get(i) ;
//            //                if( tmp.GetMachine().equals(machine) )
//            //                    allMessages.remove(i) ;
//            //            }
//            
//            while( li.hasPrevious() ){
//                PNAMessage temp = (SourceMessage_Base)li.previous() ;
//                if( temp.GetMachine().equals(machine) )
//                    li.remove() ;
//            }
//            while( li.hasNext() ){
//                SourceMessage_Base temp = (SourceMessage_Base)li.next() ;
//                if( temp.GetMachine().equals(machine) )
//                    li.remove() ;
//            }
//            
//            
//            
//        }
//        if( li.hasNext() )
//            return (SourceMessage_Base)li.next() ;
//        else if( li.hasPrevious() )
//            return (SourceMessage_Base)li.previous() ;
//        else
//            return ( new SourceMessage_Base(device,"",0) ) ;
//        //        if( allMessages.size() != 0 ){
//        //            SetCurrentMessage(ADDED) ;
//        //            return GetCurrentMessage() ;
//        //        }else
//        //            return ( new SourceMessage_Base(device,"",0)) ;
//    }

    public void ageMessages(){
        synchronized(messages){
            int number = messages.size() ;
            log.info(this.getClass().getName() + " --- " + " aging the messages for extension " + this.extension + ", there are currently this many messages: " + number) ;
            for( int i = number ; i > 0 ; i-- ){
                PNAMessage pm = (PNAMessage)messages.get(i-1) ;
                int cur_age = pm.getAge();
                log.info(this.getClass().getName() + " ---PRE--- " + "MSGID: " + pm.getMessageID() + ", age for message is " + cur_age) ;
                if( cur_age <= 0 ){
                    removeAgedMessage(pm.getMessageID());
                }
                else{
                    cur_age-- ;
                    log.info(this.getClass().getName() + " ---POST--- " + "MSGID: " + pm.getMessageID() + ", age for message is " + cur_age) ;
                    pm.setAge(cur_age) ;
                }
            }
        }

    }
    public void TestTheList(){
        //NEED TO SHOW:
        //a) HOW MANY MESSAGES, THE CONTENTS, AND WHERE THE INDEX IS
        synchronized(messages){
            
            //THIS IS HOW MANY MESSAGES IN THE LIST
            int number = messages.size() ;
            
            //THE CURRENT INDEX
            int currentIndex = index ;
            
            //PUT LIST INTO AN ARRAY
            PNAMessage[] qqmessages = new PNAMessage[number] ;
            for( int i = 0 ; i < number ; i++)
                qqmessages[i] = (PNAMessage)messages.get(i) ;
            
            logStr += "EDM for extension *** " + this.extension.getExtensionNumber() + " ***" ;
            //NOW  DISPLAY CONTENTS
            String output = "\n\tList Contains" + Integer.toString(number) + " items: " ;
            for(int i = 0 ; i < number ; i++){
                if( currentIndex == i ){
                    output += "\n\t\t->" ;
                    output += qqmessages[i].getMessageID() + " - " + qqmessages[i].getMessageText() ;
                } else
                    output += "\n\t\t  " + qqmessages[i].getMessageID() + " - " + qqmessages[i].getMessageText() ;
            }
            
            logStr += output ;
            log.info(logStr) ;
            logStr = "" ;
        }
        
    }
}
