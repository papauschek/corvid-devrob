/*
 *  Player Java Client 3 - PlayerSpeechRecognitionData.java
 *  Copyright (C) 2006 Radu Bogdan Rusu
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id$
 *
 */

package javaclient3.structures.speechrecognition;

import javaclient3.structures.*;

/**
 * Data: recognized string
 * The speech recognition data packet.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v3.0 - Player 3.0 supported
 * </ul>
 */
public class PlayerSpeechRecognitionData implements PlayerConstants {

    // Recognized text
    private String text = null;


    /**
     * @return  Length of text
     **/
    public synchronized int getText_count () {
        return this.text.length();
    }

    /**
     * @return  Recognized text
     **/
    public synchronized String getText () {
        return this.text;
    }

    /**
     * @param newText  Recognized text
     */
    public synchronized void setText (String newText) {
        this.text = newText;
    }
}