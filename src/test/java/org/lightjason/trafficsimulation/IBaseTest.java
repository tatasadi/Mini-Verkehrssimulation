package org.lightjason.trafficsimulation;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.Ignore;
import org.junit.Test;
import org.lightjason.agentspeak.generator.IAgentGenerator;
import org.lightjason.trafficsimulation.actions.CBroadcast;
import org.lightjason.trafficsimulation.actions.CSend;
import org.lightjason.trafficsimulation.simulation.EObjectFactory;
import org.lightjason.trafficsimulation.simulation.IObject;
import org.lightjason.trafficsimulation.simulation.algorithm.routing.ERoutingFactory;
import org.lightjason.trafficsimulation.simulation.environment.EDirection;
import org.lightjason.trafficsimulation.simulation.environment.IEnvironment;
import org.junit.Assume;
import org.junit.Before;
import org.lightjason.agentspeak.common.CCommon;
import org.lightjason.agentspeak.action.IAction;
import org.lightjason.agentspeak.agent.IAgent;
import org.lightjason.agentspeak.language.score.IAggregation;
import org.lightjason.trafficsimulation.simulation.virtual.EArea;

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;


/**
 * base test
 *
 * @todo fix documentation
 */
public abstract class IBaseTest
{
    protected IEnvironment m_environment;

    private final Map<String, IAgent<?>> m_agents = new ConcurrentHashMap<>();

    private final Set<IAction> m_actions = Stream.concat(
                                                Stream.of(
                                                    new CSend( m_agents ),
                                                    new CBroadcast( m_agents )
                                                ),
                                                CCommon.actionsFromPackage()
                                           ).collect( Collectors.toSet() );



    static
    {
        LogManager.getLogManager().reset();
    }

    /**
     * initialize environment
     *
     * @throws Exception on any error
     */
    @Before
    public final void initializeenvironment() throws Exception
    {
        try
        (
            final FileInputStream l_stream = new FileInputStream( "src/test/resources/environment.asl" );
        )
        {
            m_environment = EObjectFactory.ENVIRONMENT
                .generate( l_stream, m_actions.stream(), IAggregation.EMPTY )
                .generatesingle( 64, 64, 25, ERoutingFactory.JPSPLUS.get() )
                .raw();
        }
        catch ( final Exception l_exception )
        {
            l_exception.printStackTrace();
            assertTrue( false );
        }
    }

    /**
     * load configuration from yaml file
     */
    @Before
    public final void loadconfiguration()
    {
        CConfiguration.INSTANCE.loadfile( "src/main/resources/" + org.lightjason.trafficsimulation.CCommon.PACKAGEPATH + "configuration.yaml" );
    }

    /**
     * runs the object generation proecess
     *
     * @param p_file filename
     * @param p_factory generator
     * @param p_arguments generating arguments
     * @param <T> IObject
     * @return IObject
     */
    protected final <T extends IObject<?>> T generate( final String p_file, final EObjectFactory p_factory, final Object... p_arguments )
    {
        Assume.assumeNotNull( m_actions );
        Assume.assumeNotNull( m_environment );

        try
        (
            final FileInputStream l_stream = new FileInputStream( p_file );
        )
        {

            return p_factory.generate( l_stream, m_actions.stream(), IAggregation.EMPTY, m_environment, p_arguments ).generatesingle( p_arguments ).raw();

        }
        catch ( final Exception l_exception )
        {
            l_exception.printStackTrace();
            assertTrue( false );
            return null;
        }
    }


    /**
     * runs the object generation proecess
     *
     * @param p_file filename
     * @param p_factory generator
     * @param p_number number of agents
     * @param p_arguments generating arguments
     * @param <T> IObject
     * @return List of IObjects
     */
    protected final <T extends IObject<?>> List<T> generatemultiple( final String p_file, final EObjectFactory p_factory, final int p_number, final Object... p_arguments )
    {
        Assume.assumeNotNull( m_actions );
        Assume.assumeNotNull( m_environment );

        try
        (
            final FileInputStream l_stream = new FileInputStream( p_file );
        )
        {

            return p_factory.generate( l_stream, m_actions.stream(), IAggregation.EMPTY, m_environment )
                .generatemultiple( p_number, p_arguments )
                .map( IAgent::<T>raw )
                .collect( Collectors.toList() );

        }
        catch ( final Exception l_exception )
        {
            l_exception.printStackTrace();
            assertTrue( false );
            return null;
        }
    }

    /**
     * invoke all test manually
     */
    protected final void invoketest()
    {
        final Set<Method> l_before = this.before();

        Arrays.stream( this.getClass().getMethods() )
              .filter( i -> i.getAnnotation( Test.class ) != null )
              .filter( i -> i.getAnnotation( Ignore.class ) == null )
              .forEach( i -> this.invoke( i, l_before ) );
    }

    /**
     * invoke method and read if possible the data-provider
     *
     * @param p_method method
     * @param p_before before method
     */
    private void invoke( final Method p_method, final Set<Method> p_before )
    {
        // method uses a data-provider
        if ( p_method.getAnnotation( UseDataProvider.class ) == null )
            this.execute( p_method, p_before );
        else
        {
            final Object[] l_arguments;

            try
            {
                l_arguments = (Object[]) this.getClass().getDeclaredMethod( p_method.getAnnotation( UseDataProvider.class ).value() ).invoke( null );

            }
            catch ( final InvocationTargetException l_exception )
            {
                Assert.assertTrue( l_exception.getTargetException().toString(), false );
                return;
            }
            catch ( final IllegalAccessException | NoSuchMethodException l_exception )
            {
                Assert.assertTrue( l_exception.toString(), false );
                return;
            }

            Arrays.stream( l_arguments ).forEach( i -> this.execute( p_method, p_before, i ) );
        }
    }

    /**
     * invokes the method within the current object context
     *
     * @param p_method method
     * @param p_before before method
     * @param p_arguments optional arguments
     */
    private void execute( final Method p_method, final Set<Method> p_before, final Object... p_arguments )
    {
        try
        {
            if ( !p_before.isEmpty() )
                p_before.forEach( i ->
                {
                    try
                    {
                        i.invoke( this );
                    }
                    catch ( final IllegalAccessException | InvocationTargetException l_exception )
                    {
                        l_exception.printStackTrace();
                        Assert.assertTrue( false );
                    }
                } );

            p_method.invoke( this, p_arguments );
        }
        catch ( final AssumptionViolatedException l_exception )
        {
        }
        catch ( final InvocationTargetException l_exception )
        {
            if ( l_exception.getTargetException() instanceof AssumptionViolatedException )
                return;

            if ( !p_method.getAnnotation( Test.class ).expected().isInstance( l_exception.getTargetException() ) )
            {
                l_exception.getTargetException().printStackTrace();
                Assert.assertTrue( false );
            }
        }
        catch ( final IllegalAccessException l_exception )
        {
            Assert.assertTrue( l_exception.toString(), false );
        }
    }

    /**
     * reads the before annotated methods
     *
     * @return optional before method
     */
    private Set<Method> before()
    {
        return Arrays.stream( this.getClass().getMethods() )
                     .filter( i -> i.getAnnotation( Before.class ) != null )
                     .filter( i -> i.getAnnotation( Ignore.class ) == null )
                     .collect( Collectors.toSet() );
    }

}