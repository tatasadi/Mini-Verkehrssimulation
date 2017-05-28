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

package org.lightjason.trafficsimulation.simulation.bounding;

import cern.colt.matrix.DoubleMatrix1D;

/**
 * circle bounding box
 */
public final class CCircleBoundingBox implements IBoundingBox
{
    /**
     * position of the center of the bounding box
     */
    private DoubleMatrix1D m_centerposition;
    /**
     * radius of the bounding box
     */
    private double m_radius;

    /**
     * ctor
     * @param p_centerposition center
     * @param p_radius radius
     */
    public CCircleBoundingBox( final DoubleMatrix1D p_centerposition, final double p_radius )
    {
        this.m_centerposition = p_centerposition;
        this.m_radius = p_radius;
    }

    @Override
    public boolean intersects( final IBoundingBox p_boundingbox )
    {
        return false;
    }
}
