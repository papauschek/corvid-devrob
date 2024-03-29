/*
 *  Player Java Client 3 - PlayerLocalizeSetPose.java
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

package javaclient3.structures.localize;

import javaclient3.structures.*;

/**
 * Request/reply: Set the robot pose estimate.
 * Set the current robot pose hypothesis by sending a
 * PLAYER_LOCALIZE_REQ_SET_POSE request.  Null response.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v3.0 - Player 3.0 supported
 * </ul>
 */
public class PlayerLocalizeSetPose implements PlayerConstants {

    // The mean value of the pose estimate (m, m, rad).
    private PlayerPose mean;
    // The diagonal elements of the covariance matrix pose estimate
    //       (m$^2$, rad$^2$).
    private double[] cov = new double[6];


    /**
     * @return  The mean value of the pose estimate (m, m, rad).
     **/
    public synchronized PlayerPose getMean () {
        return this.mean;
    }

    /**
     * @param newMean  The mean value of the pose estimate (m, m, rad).
     */
    public synchronized void setMean (PlayerPose newMean) {
        this.mean = newMean;
    }
    /**
     * @return  The diagonal elements of the covariance matrix pose estimate
     *       (m$^2$, rad$^2$).
     **/
    public synchronized double[] getCov () {
        return this.cov;
    }

    /**
     * @param newCov  The diagonal elements of the covariance matrix pose estimate
     *       (m$^2$, rad$^2$).
     */
    public synchronized void setCov (double[] newCov) {
        this.cov = newCov;
    }

}