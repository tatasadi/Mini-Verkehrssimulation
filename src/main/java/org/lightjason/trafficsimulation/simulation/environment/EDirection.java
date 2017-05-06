/*
 * @cond LICENSE
 * ######################################################################################
 * # LGPL License                                                                       #
 * #                                                                                    #
 * # This file is part of the LightJason TrafficSimulation                              #
 * # Copyright (c) 2016-17, LightJason (info@lightjason.org)                            #
 * # This program is free software: you can redistribute it and/or modify               #
 * # it under the terms of the GNU Lesser General Public License as                     #
 * # published by the Free Software Foundation, either version 3 of the                 #
 * # License, or (at your option) any later version.                                    #
 * #                                                                                    #
 * # This program is distributed in the hope that it will be useful,                    #
 * # but WITHOUT ANY WARRANTY; without even the implied warranty of                     #
 * # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                      #
 * # GNU Lesser General Public License for more details.                                #
 * #                                                                                    #
 * # You should have received a copy of the GNU Lesser General Public License           #
 * # along with this program. If not, see http://www.gnu.org/licenses/                  #
 * ######################################################################################
 * @endcond
 */

package org.lightjason.trafficsimulation.simulation.environment;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;
import org.lightjason.trafficsimulation.simulation.CMath;

import java.util.Locale;


/**
 * direction enum (counter clockwise)
 */
public enum EDirection
{
    EAST( 0 ),
    NORTHEAST( 45 ),
    NORTH( 90 ),
    NORTHWEST( 135 ),
    WEST( 180 ),
    SOUTHWEST( 225 ),
    SOUTH( 270 ),
    SOUTHEAST( 315 );


    /**
     * rotation-matrix for the direction vector
     */
    private final DoubleMatrix2D m_rotation;

    /**
     * ctor
     *
     * @param p_alpha rotation of the normal-viewpoint-vector
     */
    EDirection( final double p_alpha )
    {
        m_rotation = CMath.rotationmatrix( Math.toRadians( p_alpha ) );
    }

    /**
     * calculates the new position
     *
     * @param p_position current position
     * @param p_goalposition goal position
     * @param p_speed number of cells / step size
     * @return new position
     */
    public DoubleMatrix1D position( final DoubleMatrix1D p_position, final DoubleMatrix1D p_goalposition, final int p_speed )
    {
        // calculate the stright line by: current position + l * (goal position - current position)
        // normalize direction and rotate the normalized vector based on the direction
        // calculate the target position based by: current position + speed * rotate( normalize( goal position - current position ) )
        final DoubleMatrix1D l_view = new DenseDoubleMatrix1D( p_goalposition.toArray() );
        return CMath.ALGEBRA.mult(
            m_rotation,
            l_view
                .assign( p_position, Functions.minus )
                .assign( Functions.div( Math.sqrt( Algebra.DEFAULT.norm2( l_view ) ) ) )
        )
                            .assign( Functions.mult( p_speed ) )
                            .assign( p_position, Functions.plus )
                            .assign( Math::round );
    }


    /**
     * returns the direction by an angle (in degree)
     *
     * @param p_angle angle in degree
     * @return direction
     */
    public static EDirection byAngle( final double p_angle )
    {
        return EDirection.values()[
            (int) (
                p_angle < 0
                ? 360 + p_angle
                : p_angle
            ) / 45
            ];
    }

    /**
     * get enum from string
     *
     * @param p_name string name
     * @return area
     */
    public static EDirection from( final String p_name )
    {
        return EDirection.valueOf( p_name.toUpperCase( Locale.ROOT ) );
    }
}
