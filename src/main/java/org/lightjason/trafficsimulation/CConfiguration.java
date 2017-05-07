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

package org.lightjason.trafficsimulation;

import org.lightjason.agentspeak.beliefbase.CEmptyBeliefbase;
import org.lightjason.agentspeak.beliefbase.IBeliefbase;
import org.lightjason.agentspeak.beliefbase.view.IView;
import org.lightjason.agentspeak.beliefbase.view.IViewGenerator;
import org.lightjason.agentspeak.common.CPath;
import org.lightjason.agentspeak.common.IPath;
import org.lightjason.agentspeak.language.ILiteral;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.ITrigger;
import org.lightjason.trafficsimulation.simulation.environment.IEnvironment;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * configuration and initialization of all simulation objects
 *
 * @bug refactor
 */
public final class CConfiguration
{
    /**
     * singleton instance
     */
    public static final CConfiguration INSTANCE = new CConfiguration();
    /**
     * map with configuration data
     */
    private final Map<String, Object> m_configuration = new TreeMap<>( String.CASE_INSENSITIVE_ORDER );


    /**
     * ctor
     */

    private CConfiguration()
    {
    }


    /**
     * loads the configuration
     * @param p_path path elements
     * @return self reference
     */
    @SuppressWarnings( "unchecked" )
    public final CConfiguration loadfile( final String p_path )
    {

        try
        (
                final InputStream l_stream = orDefaultPath( p_path );
        )
        {
            return this.set( new Yaml().load( l_stream ) );
        } catch ( final IOException l_exception )
        {
            throw new RuntimeException( l_exception );
        }
    }


    /**
     * loads the configuration from a string
     *
     * @param p_yaml yaml string
     * @return self reference
     */
    @SuppressWarnings( "unchecked" )
    public final CConfiguration loadstring( final String p_yaml )
    {
        return this.set( new Yaml().load( p_yaml ) );
    }


    /**
     * push data to the internal map
     *
     * @param p_data input data
     * @return self reference
     */
    @SuppressWarnings( "unchecked" )
    private CConfiguration set( final Object p_data )
    {
        final Map<String, ?> l_result = (Map<String, Object>) maptolowercase( p_data );
        if ( l_result != null )
        {
            m_configuration.clear();
            m_configuration.putAll( l_result );
        }
        return this;
    }


    /**
     * convert all map keys to lower-case
     *
     * @param p_value any value
     * @return object return
     */
    @SuppressWarnings( "unchecked" )
    private static Object maptolowercase( final Object p_value )
    {
        if ( !( p_value instanceof Map<?, ?> ) )
            return p_value;

        final Map<String, Object> l_map = new TreeMap<>( String.CASE_INSENSITIVE_ORDER );
        l_map.putAll(
                       ( (Map<String, Object>) p_value ).entrySet()
                                                       .parallelStream()
                                                       .collect( Collectors.toMap(
                                                           i -> i.getKey().toLowerCase(),
                                                           i -> maptolowercase( i.getValue() )
                                                       ) )
        );
        return l_map;
    }


    @Override
    public final int hashCode()
    {
        return 1;
    }

    @Override
    public boolean equals( final Object p_object )
    {
        return ( p_object != null ) && ( p_object instanceof CConfiguration ) && ( p_object.hashCode() == this.hashCode() );
    }

    @Override
    public final String toString()
    {
        return m_configuration.toString();
    }


    /**
     * set default path
     *
     * @param p_path path or null / empty
     * @return default path on empty or input path
     */
    private static InputStream orDefaultPath( final String p_path ) throws FileNotFoundException
    {
        return ( p_path == null ) || ( p_path.isEmpty() )
               ? CConfiguration.class.getResourceAsStream( "configuration.yaml" )
               : new FileInputStream( p_path );
    }


    /**
     * creates the default configuration
     *
     * @return full path
     * @throws IOException on any io error
     */
    public static String createdefault() throws IOException
    {
        final String l_path = Stream.of(
            System.getProperty( "user.home" ),
            ".minitrafficsim"
        ).collect( Collectors.joining( File.separator ) );

        new File( l_path ).mkdirs();
        Files.copy(
            CConfiguration.class.getResourceAsStream(  "configuration.yaml"   ),
            FileSystems.getDefault().getPath( l_path + File.separator + "configuration.yaml" ),
            StandardCopyOption.REPLACE_EXISTING
        );

        return l_path;
    }


    /**
     * returns a configuration value
     *
     * @param p_path path of the element
     * @tparam T returning type
     * @return value or null
     */
    public final <T> T get( final String... p_path )
    {
        return recursivedescent( m_configuration, p_path );
    }


    /**
     * returns a configuration value or on not
     * existing the default value
     *
     * @param p_default default value
     * @param p_path path of the element
     * @tparam T returning type
     * @return value / default vaue
     */
    public final <T> T getOrDefault( final T p_default, final String... p_path )
    {
        final T l_result = recursivedescent( m_configuration, p_path );
        return l_result == null
               ? p_default
               : l_result;
    }


    /**
     * recursive descent
     *
     * @param p_map configuration map
     * @param p_path path
     * @tparam T returning type parameter
     * @return value
     */
    @SuppressWarnings( "unchecked" )
    private static <T> T recursivedescent( final Map<String, ?> p_map, final String... p_path )
    {
        if ( ( p_path == null ) || ( p_path.length == 0 ) )
            throw new RuntimeException( "path need not to be empty" );

        final Object l_data = p_map.get( p_path[0].toLowerCase( Locale.ROOT ) );
        return ( p_path.length == 1 ) || ( l_data == null )
               ? (T) l_data
               : (T) recursivedescent( (Map<String, ?>) l_data, Arrays.copyOfRange( p_path, 1, p_path.length ) );
    }



    /**
     * view of the configuration
     */
    private final class CView implements IView<IEnvironment>
    {
        /**
         * name of the view
         */
        private static final String NAME = "config";
        /**
         * parent name
         */
        private final IView<IEnvironment> m_parent;

        /**
         * ctor
         *
         * @param p_parent parent view
         */
        CView( final IView<IEnvironment> p_parent )
        {
            m_parent = p_parent;
        }

        @Override
        public final int hashCode()
        {
            return NAME.hashCode();
        }

        @Override
        public final boolean equals( final Object p_object )
        {
            return ( p_object != null ) && ( p_object instanceof IView<?> ) && ( p_object.hashCode() == this.hashCode() );
        }

        @Override
        @SafeVarargs
        public final Stream<IView<IEnvironment>> walk( final IPath p_path, final IViewGenerator<IEnvironment>... p_generator )
        {
            return Stream.of( this );
        }

        @Override
        public final IView<IEnvironment> generate( final IViewGenerator<IEnvironment> p_generator, final IPath... p_paths )
        {
            return this;
        }

        @Override
        public final Stream<IView<IEnvironment>> root()
        {
            return Stream.concat(
                Stream.of( this ),
                Stream.of( this.parent() ).filter( Objects::nonNull )
            );
        }

        @Override
        public final IBeliefbase<IEnvironment> beliefbase()
        {
            return CEmptyBeliefbase.instance();
        }

        @Override
        public final IPath path()
        {
            final IPath l_path = new CPath();
            this.root().forEach( i -> l_path.pushfront( i.name() ) );
            return l_path;
        }

        @Override
        public final String name()
        {
            return NAME;
        }

        @Override
        public final IView<IEnvironment> parent()
        {
            return m_parent;
        }

        @Override
        public final boolean hasParent()
        {
            return m_parent != null;
        }

        @Override
        public final Stream<ITrigger> trigger()
        {
            return Stream.of();
        }

        @Override
        public final Stream<ILiteral> stream( final IPath... p_path )
        {

            return Stream.of();
        }

        @Override
        public final Stream<ILiteral> stream( final boolean p_negated, final IPath... p_path )
        {
            return Stream.of();
        }

        @Override
        public final IView<IEnvironment> clear( final IPath... p_path )
        {
            return this;
        }

        @Override
        public final IView<IEnvironment> add( final Stream<ILiteral> p_literal )
        {
            return this;
        }

        @Override
        public final IView<IEnvironment> add( final ILiteral... p_literal )
        {
            return this;
        }

        @Override
        @SafeVarargs
        public final IView<IEnvironment> add( final IView<IEnvironment>... p_view )
        {
            return this;
        }

        @Override
        @SafeVarargs
        public final IView<IEnvironment> add( final IPath p_path, final IView<IEnvironment>... p_view )
        {
            return this;
        }

        @Override
        public final IView<IEnvironment> remove( final Stream<ILiteral> p_literal )
        {
            return this;
        }

        @Override
        public final IView<IEnvironment> remove( final ILiteral... p_literal )
        {
            return this;
        }

        @Override
        public final IView<IEnvironment> remove( final IView<IEnvironment> p_view
        )
        {
            return this;
        }

        @Override
        public final boolean containsLiteral( final IPath p_path )
        {
            final Object l_value = recursivedescent( m_configuration, p_path.stream().toArray( String[]::new ) );
            return ( l_value != null ) && ( !( l_value instanceof Map<?, ?> ) );
        }

        @Override
        public final boolean containsView( final IPath p_path )
        {
            final Object l_value = recursivedescent( m_configuration, p_path.stream().toArray( String[]::new ) );
            return ( l_value != null ) && ( l_value instanceof Map<?, ?> );
        }

        @Override
        public final boolean empty()
        {
            return m_configuration.isEmpty();
        }

        @Override
        public final int size()
        {
            return 0;
        }



        @Override
        public final IEnvironment update( final IEnvironment p_agent )
        {
            return p_agent;
        }

    }

}
