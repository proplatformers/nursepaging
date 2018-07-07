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



package com.nursepaging.pna ;

/**
 *
 * @author  root
 */
public interface TAP_Constants {
	char pnaSTX	=	'\u0002' ;
	char pnaETX	=	'\u0003' ;
	char pnaEOT	=	'\u0004' ;
	char pnaACK	=	'\u0006' ;
	char pnaLF         =       0x0a ;
        char pnaCR		=	0x0d ;
	char pnaNAK        =	'\u0015' ;
	char pnaESC	=	'\u001b' ;
	char pnaRS         =	'\u001e' ;
}
