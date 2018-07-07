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

import java.util.Properties;
import org.apache.log4j.Logger;
import au.com.mrvoip.serialport.SerialConfigurator;
import au.com.mrvoip.serialport.SerialPortOwner;
import au.com.mrvoip.serialport.TTYSx;
import org.opencsta.communications.CommunicationsStream;

public class PNALayer1 implements SerialPortOwner,CommunicationsStream{
	protected static org.apache.log4j.Logger log = Logger.getLogger(CorePNA.class);
	private CorePNA parent ;
    private Properties theProps ;
    private static CommunicationsStream layer1 ;
    private SerialConfigurator sc ;
    
    public PNALayer1(CorePNA _parent, Properties _theProps) {
        this.parent = _parent ;
        this.theProps = _theProps ;
        openComms() ;
    }

    public boolean SendString(StringBuffer sb){
        if( parent.isNoSerial() )
            return true ;
        else{
            return layer1.SendString(sb);
        }
    }
    public boolean CheckReceived(StringBuffer sb){ return parent.CheckReceived(sb) ; }

    public void closeComms(){
        layer1.closeComms();
        log.info(this.getClass().getName() + " -> " + " closing comms") ;
    }
    
    public void openComms(){
            sc = new SerialConfigurator("pna",theProps) ;
            StartSerialPort( sc.getDeviceName(), sc.getBaudRate() ) ;
    }
    
    public void StartSerialPort(String serial_device, int baudrate){
        layer1 = new TTYSx(serial_device, baudrate, this, "PNA") ;
        log.info("******** STARTED SERIAL PORT: " + serial_device + "/" + baudrate);
    }

}
