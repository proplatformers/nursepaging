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

package au.com.mrvoip.serialport;

//import org.opencsta.config.PropertiesController ;
import java.util.Properties ;
import org.apache.log4j.*;
/**
 *
 * @author  root
 */
public class SerialConfigurator {
    protected static Logger slog = Logger.getLogger(SerialConfigurator.class) ;
    private String SPORT ;
    private int BAUD_RATE ;
    private String appName ;
    private Properties theProps ;


    /** Creates a new instance of SerialStarter */
    public SerialConfigurator(String appName, Properties _theProps) {
        this.appName = appName.toUpperCase() ;
        System.out.println("SerialConfigurator appName: " + this.appName) ;
        String lowappName = appName.toLowerCase();
        theProps = _theProps ;
        Init() ;
    }

    private void Init(){
        Properties tmp_props = theProps ;
        slog.info("Setting serial port parameters") ;
        setSerialDeviceName( tmp_props.getProperty("SERIALPORT_" + appName) ) ;
        setBaudRate( tmp_props.getProperty("SERIALPORT_" + appName + "_BAUDRATE") ) ;
        slog.info("Setting serial port parameters  ---- OK.DONE") ;
    }

    private void setBaudRate(String baudrate){
        try{
            BAUD_RATE = Integer.valueOf(baudrate).intValue() ;
            slog.info("Baud Rate set to: " + Integer.toString(BAUD_RATE)) ;
        }catch(NumberFormatException e){
            e.printStackTrace() ;
            BAUD_RATE = 9600 ;//default to this value
            slog.info("Baud Rate Defaulting to: " + Integer.toString(BAUD_RATE)) ;
        }
    }

    private void setSerialDeviceName(String name){
        SPORT = name ;
        slog.info("Serial Port set to: " + SPORT) ;
    }

    public int getBaudRate(){
        return BAUD_RATE ;
    }

    public String getDeviceName(){
        return SPORT ;
    }
}
