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

import com.nursepaging.pna.CorePNA;
import org.apache.log4j.Logger;




/**
 *
 * @author  root
 */
public class ClearDisplay implements Runnable{
    private static final int SECONDSby3 = 60 ;
    CorePNA pna ;
    String ext ;
    Thread selfThread = null ;
    private boolean clear ;
    private boolean update;
    private Logger general_log;
    public ClearDisplay(CorePNA pna, String ext) {
        this.pna = pna ;
        this.ext = ext ;
        this.clear = false ;
        this.update = false ;
        selfThread = new Thread(ClearDisplay.this) ;
        selfThread.start() ;
    }

    public void run(){
        PNAMessageBP sm = new PNAMessageBP(ext, "") ;

        while( clear == false )
            Loop() ;

        pna.AddClearDisplayTxt(sm) ;//possible fault here. like 1:10000000 chance depending on timing or new msg
    }

    public void RestartTimer(){
        general_log.info("TRIGGER: Timer restart triggered for ext: " + ext);
        update = true ;
    }

    private void ResetTimer(){
        general_log.info("RESET: Timer reset for: " + ext);
        update = false ;
    }

    private void Loop(){
        ResetTimer() ;//started looping again, so restart set to false, until triggered.
        for(int i = 0 ; i < SECONDSby3 ; i++){
            synchronized(this){
                try{//THIS SHOULD BE CHANGED TO A PROPERTY
                    Thread.currentThread().sleep(3000) ;
                        if( update == true )//if needs updating, return to while loop in run to restart timer
                            return ;
                        else
                            ;
                }catch(InterruptedException e){
                }
            }
        }
        clear = true ;//time has passed, time to clear display...
    }

}
