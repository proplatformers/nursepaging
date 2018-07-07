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

public interface TAP_Constants {
        char BP_STX     =       '\u0002' ;
        char BP_ETX     =       '\u0003' ;
        char BP_EOT     =       '\u0004' ;
        char BP_ACK     =       '\u0006' ;
        char BP_LF         =       0x0a ;
        char BP_CR              =       0x0d ;
        char BP_NAK        =    '\u0015' ;
        char BP_ESC     =       '\u001b' ;
        char BP_RS         =    '\u001e' ;
}
