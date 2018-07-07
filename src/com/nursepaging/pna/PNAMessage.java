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
import java.util.Properties;
import org.apache.log4j.Logger;

import com.nursepaging.pna.utils.Alarm;


/**
 *
 * @author  mylo
 */
public class PNAMessage implements Alarm{
	protected static org.apache.log4j.Logger log = Logger.getLogger(CorePNA.class);
    protected StringBuffer sb ;
    protected int id ;
    protected String messageText ="";
    protected int alarmCodePriority = 0;
    protected String station ;
    protected List displayingExtensions ;
    protected Properties theProps ;
    private int age = 300 ;
    
    /** Creates a new instance of SourceMessage_Base */
    public PNAMessage(StringBuffer sb, int id) {
        this.id = id ;
        this.sb = sb ;
        displayingExtensions = Collections.synchronizedList( new LinkedList() ) ;
        parse(sb) ;
    }
    
    public PNAMessage(){
    }
    
    public void parse(StringBuffer sb){
        
    }
    
    public String getMessageText(){
    	return messageText ;
    }
    
    public String getMessageID(){
    	return Integer.toString(id) ;
    }
    
    public String getStation(){
        return station ;
    }
    
    public int getMessageAlarmCodePriority(){
        return alarmCodePriority ;
    }
    
    public List getDisplayingExtensions(){
    	return displayingExtensions ;
    }

    /**
     * @return the age
     */
    public int getAge() {
        return age;
    }

    /**
     * @param age the age to set
     */
    public void setAge(int age) {
        this.age = age;
    }
}
