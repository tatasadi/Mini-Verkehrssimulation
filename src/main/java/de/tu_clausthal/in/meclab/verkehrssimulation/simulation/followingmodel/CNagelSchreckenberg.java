package de.tu_clausthal.in.meclab.verkehrssimulation.simulation.followingmodel;

import de.tu_clausthal.in.meclab.verkehrssimulation.simulation.CConfigs;

import java.util.Random;

/**
 * nagel-schreckenberg class
 *
 * @author Ehsan Tatasadi
 */
public class CNagelSchreckenberg implements IFollowingModel
{
    @Override
    public int applyModelToAVehicle( int p_velocity, int p_blockIndex, int p_nextOccupiedBlockIndex )
    {
        int l_newVelocity = p_velocity;
        //1. rule in Nagel-Schreckenberg-Modell
        if ( l_newVelocity < CConfigs.MAX_VELOCITY_OF_VEHICLES )
            l_newVelocity++;
        //2. rule in Nagel-Schreckenberg-Modell
        if ( p_nextOccupiedBlockIndex != -1 )
        {
            if ( p_blockIndex == -1 && p_nextOccupiedBlockIndex <= CConfigs.MAX_VELOCITY_OF_VEHICLES )
            {
                l_newVelocity = p_nextOccupiedBlockIndex;
            }
            else
            {
                if ( l_newVelocity > p_nextOccupiedBlockIndex - p_blockIndex - 1 )
                {
                    l_newVelocity = p_nextOccupiedBlockIndex - p_blockIndex - 1;
                }
            }
        }
        //3. rule in Nagel-Schreckenberg-Modell
        // get a random number between 1,2,3
        final Random l_randomGenerator = new Random();
        final int l_random = l_randomGenerator.nextInt( 3 );
        if ( l_random == 0 && l_newVelocity >= 1 )
            l_newVelocity--;
        return l_newVelocity;
    }
}