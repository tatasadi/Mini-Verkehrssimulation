package de.tu_clausthal.in.meclab.verkehrssimulation.simulation.environment;

import de.tu_clausthal.in.meclab.verkehrssimulation.CConfiguration;
import de.tu_clausthal.in.meclab.verkehrssimulation.simulation.algorithm.routing.ERoutingFactory;
import de.tu_clausthal.in.meclab.verkehrssimulation.simulation.movable.vehicle.CVehicle;
import de.tu_clausthal.in.meclab.verkehrssimulation.simulation.movable.vehicle.CVehicleGenerator;
import de.tu_clausthal.in.meclab.verkehrssimulation.simulation.virtual.CVehiclesWay;
import de.tu_clausthal.in.meclab.verkehrssimulation.simulation.virtual.ILane;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.lightjason.agentspeak.action.IAction;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.CTrigger;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.ITrigger;
import org.lightjason.agentspeak.language.score.IAggregation;

import java.io.FileInputStream;
import java.util.*;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 *
 */
public final class TestCAgent
{
    /**
     * environment reference
     */
    private IEnvironment m_environment;
    /**
     * action references
     */
    private Set<IAction> m_actions;
    /**
     * vehicle agent
     */
    private CVehicle m_vehicle;

    static
    {
        LogManager.getLogManager().reset();
    }

    @Before
    public void initialize()
    {
        m_environment = new CEnvironment( 100, 100, 25, ERoutingFactory.JPSPLUS.get(), Collections.<ILane>emptyList() );

        m_actions = Collections.unmodifiableSet( Stream.concat(
                org.lightjason.agentspeak.common.CCommon.actionsFromPackage(),
                org.lightjason.agentspeak.common.CCommon.actionsFromAgentClass( CVehicle.class )
        ).collect( Collectors.toSet() ) );

        Assume.assumeNotNull( m_environment );
        Assume.assumeNotNull( m_actions );
    }

    @Test
    public void testVehicleGenerator()
    {
        List<Map<String, Object>> l_vehiclerandomgeneratepositions = new LinkedList<>();
        Map<String, Object> l_map = new HashMap<String, Object>() {{
            put("position", Stream.of(1,2).collect(Collectors.toList()));
            put("rotation", 0);
        }};
        l_vehiclerandomgeneratepositions.add(l_map);
        try
            (
                final FileInputStream l_stream = new FileInputStream( "src/test/resources/agent.asl" );
            )
        {
            m_vehicle = (CVehicle) new CVehicleGenerator(
                m_environment,
                l_stream,
                m_actions,
                IAggregation.EMPTY )
                .generatesingle( l_vehiclerandomgeneratepositions, "car", 32, 16);
            m_vehicle.call();
        } catch (final Exception l_exception) {
            l_exception.printStackTrace();
            assertTrue( l_exception.getMessage() , false );
        }
    }

    @Test
    public void testVehicleLiteral()
    {
        try {
            m_vehicle.trigger(
                    CTrigger.from(
                            ITrigger.EType.ADDGOAL,
                            m_vehicle.literal().findFirst().get() )
            );

            m_vehicle.call();
        }
        catch ( final Exception l_exception )
        {
            assertTrue( l_exception.getMessage() , false );
        }

    }

    public static void main( final String[] p_args )
    {
        final TestCAgent l_test = new TestCAgent();

        l_test.initialize();
        l_test.testVehicleGenerator();

        l_test.initialize();
        l_test.testVehicleLiteral();
    }

    /*
    static final class MyAgent extends IBaseAgent<MyAgent>
    {

        MyAgent( final IAgentConfiguration<MyAgent> p_configuration )
        {
            super( p_configuration );
        }
    }

    static final class MyAgentGenerator extends IBaseAgentGenerator<MyAgent>
    {

        MyAgentGenerator( final InputStream p_stream ) throws Exception
        {
            super(

                    p_stream,

                    CCommon.actionsFromPackage().collect( Collectors.toSet() ),


                    IAggregation.EMPTY
            );
        }

        @Override
        public final MyAgent generatesingle( final Object... p_data )
        {
            return new MyAgent( m_configuration );
        }
    }*/
}
