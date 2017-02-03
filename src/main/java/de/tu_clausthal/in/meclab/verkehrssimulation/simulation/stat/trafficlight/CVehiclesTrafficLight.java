package de.tu_clausthal.in.meclab.verkehrssimulation.simulation.stat.trafficlight;

import java.util.List;

/**
 * vehicles traffic light class
 */
public class CVehiclesTrafficLight extends IBaseTrafficLight<EVehiclesTrafficLight>
{
    /**
     * traffic light constructor
     *
     * @param p_leftupper left-upper position
     * @param p_rightbottom right-bottom position
     * @param p_startColor start color of the traffic light
     * @param p_startColorDuration duration of the start color
     * @param p_duration duration of the traffic light colors
     */
    public CVehiclesTrafficLight( final List<Integer> p_leftupper, final List<Integer> p_rightbottom, final EVehiclesTrafficLight p_startColor, final int p_startColorDuration, final int... p_duration )
    {
        super( p_leftupper, p_rightbottom, p_startColor, p_startColorDuration, p_duration );
    }
}
