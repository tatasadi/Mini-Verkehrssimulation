package org.lightjason.trafficsimulation;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.lightjason.agentspeak.action.IAction;
import org.lightjason.agentspeak.agent.IAgent;
import org.lightjason.agentspeak.language.score.IAggregation;
import org.lightjason.trafficsimulation.actions.CBroadcast;
import org.lightjason.trafficsimulation.actions.CSend;
import org.lightjason.trafficsimulation.simulation.EObjectFactory;
import org.lightjason.trafficsimulation.simulation.algorithm.routing.CJPSPlus;
import org.lightjason.trafficsimulation.simulation.environment.IEnvironment;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * main desktop application
 *
 * @bug clean-up
 */
public final class CMain
{
    static
    {
        LogManager.getLogManager().reset();
    }


    /**
     * constructor
     */
    private CMain()
    {
    }

    /**
     * main method
     *
     * @param p_args arguments
     * @throws IOException on io errors
     * @throws Exception on URI syntax error
     */
    public static void main( final String[] p_args ) throws Exception
    {
        // --- define CLI options ------------------------------------------------------------------------------------------------------------------------------

        final Options l_clioptions = new Options();
        l_clioptions.addOption( "help", false, "shows this information" );
        l_clioptions.addOption( "generateconfig", false, "generate default configuration" );
        l_clioptions.addOption( "config", true, "path to configuration directory (default: <user home>/.asimov/configuration.yaml)" );

        final CommandLine l_cli;
        try
        {
            l_cli = new DefaultParser().parse( l_clioptions, p_args );
        }
        catch ( final Exception l_exception )
        {
            System.err.println( "command-line arguments parsing error" );
            System.exit( -1 );
            return;
        }



        // --- process CLI arguments and initialize configuration ----------------------------------------------------------------------------------------------

        if ( l_cli.hasOption( "help" ) )
        {
            new HelpFormatter().printHelp( new java.io.File( CMain.class.getProtectionDomain().getCodeSource().getLocation().getPath() ).getName(), l_clioptions );
            return;
        }

        if ( l_cli.hasOption( "generateconfig" ) )
        {
            System.out.println( MessageFormat.format( "default configuration was created under [{0}]", CConfiguration.createdefault() ) );
            return;
        }

        // load configuration and start the http server (if possible)
        CConfiguration.INSTANCE.loadfile( l_cli.getOptionValue( "config", "" ) );

        // initialize server
        CHTTPServer.initialize();

        // --- add test agents -----------------------------------------------------------
        final Map<String, IAgent<?>> l_agents = new ConcurrentHashMap<>();

        final Set<IAction> l_actions = Stream.concat(
            Stream.of(
                new CSend( l_agents ),
                new CBroadcast( l_agents )
            ),
            org.lightjason.agentspeak.common.CCommon.actionsFromPackage()
        ).collect( Collectors.toSet() );


        EObjectFactory.PEDESTRIAN.generate(

            CMain.class.getResourceAsStream( "asl/pedestrian.asl" ),

            l_actions.stream(),

            IAggregation.EMPTY,

            EObjectFactory.ENVIRONMENT.generate(
                CMain.class.getResourceAsStream( "asl/environment.asl" ),
                l_actions.stream(),
                IAggregation.EMPTY
            ).generatesingle( 25, 25, 2.5, new CJPSPlus() ).<IEnvironment>raw()

        ).generatemultiple( 3, new DenseDoubleMatrix1D( 2 ) ).forEach( i ->
        {
        } );
        // -------------------------------------------------------------------------------


        // execute server
        CHTTPServer.execute();
    }


}