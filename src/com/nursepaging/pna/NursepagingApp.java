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

/**
 *
 * @author cm
 */
public interface NursepagingApp extends Runnable{
    public String getVersion() ;
    public String getImplementation() ;
    public String getCoreVersion() ;
    public Properties getCoreProperties() ;
    public void createPNAMessage(StringBuffer recd_string) ;
    public boolean checkReceived(StringBuffer sb) ;
    public void init() ;
    public void addNewTestMessage() ;
    public void TDSDataReceived(String dev, String code, String data) ;
}
