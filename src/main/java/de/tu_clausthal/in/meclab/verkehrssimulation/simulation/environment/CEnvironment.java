package de.tu_clausthal.in.meclab.verkehrssimulation.simulation.environment;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import de.tu_clausthal.in.meclab.verkehrssimulation.CCommon;
import de.tu_clausthal.in.meclab.verkehrssimulation.simulation.IObject;

import java.util.stream.IntStream;

/**
 * environment class
 */
public class CEnvironment implements IEnvironment
{

    /**
     * row number
     */
    private final int m_row;
    /**
     * column number
     */
    private final int m_column;
    /**
     * cell size
     */
    private final int m_cellsize;
    /**
     * matrix with object positions
     */
    private final ObjectMatrix2D m_positions;


    /**
     * create environment
     *
     * @param p_cellrows number of row cells
     * @param p_cellcolumns number of column cells
     * @param p_cellsize cell size
     */
    public CEnvironment( final int p_cellrows, final int p_cellcolumns, final int p_cellsize )
    {
        if ( ( p_cellcolumns < 1 ) || ( p_cellrows < 1 ) || ( p_cellsize < 1 ) )
            throw new IllegalArgumentException( "environment size must be greater or equal than one" );

        m_row = p_cellrows;
        m_column = p_cellcolumns;
        m_cellsize = p_cellsize;
        m_positions = new SparseObjectMatrix2D( m_row, m_column );

    }

    @Override
    public final IEnvironment call()
    {
        return this;
    }

    @Override
    public final int row()
    {
        return m_row;
    }

    @Override
    public final int column()
    {
        return m_column;
    }

    @Override
    public final int cellsize()
    {
        return m_cellsize;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public final synchronized IObject move( final IObject p_object, final DoubleMatrix1D p_position )
    {
        final DoubleMatrix1D l_position = this.clip( new DenseDoubleMatrix1D( p_position.toArray() ) );

        // check of the target position is free, if not return object, which blocks the cell
        final IObject l_object = (IObject) m_positions.getQuick( (int) l_position.getQuick( 0 ), (int) l_position.getQuick( 1 ) );
        if ( l_object != null )
            return l_object;

        // cell is free, move the position and return updated object
        m_positions.set( (int) p_object.position().get( 0 ), (int) p_object.position().get( 1 ), null );
        m_positions.set( (int) l_position.getQuick( 0 ), (int) l_position.getQuick( 1 ), p_object );
        p_object.position().setQuick( 0, l_position.getQuick( 0 ) );
        p_object.position().setQuick( 1, l_position.getQuick( 1 ) );

        return p_object;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public final synchronized IObject get( final DoubleMatrix1D p_position )
    {
        return (IObject) m_positions.getQuick( (int) CEnvironment.clip( p_position.get( 0 ), m_row ), (int) CEnvironment.clip( p_position.get( 1 ), m_column ) );
    }

    @Override
    public final synchronized IObject remove( final IObject p_object )
    {
        final DoubleMatrix1D l_position = this.clip( new DenseDoubleMatrix1D( p_object.position().toArray() ) );
        m_positions.set( (int) l_position.get( 0 ), (int) l_position.get( 1 ), null );

        return p_object;
    }

    @Override
    public final synchronized boolean empty( final DoubleMatrix1D p_position )
    {
        final DoubleMatrix1D l_position = this.clip( new DenseDoubleMatrix1D( p_position.toArray() ) );
        return m_positions.getQuick( (int) l_position.getQuick( 0 ), (int) l_position.getQuick( 1 ) ) == null;
    }

    @Override
    public final boolean isinside( final DoubleMatrix1D p_position )
    {
        return ( p_position.getQuick( 0 ) >= 0 )
            && ( p_position.getQuick( 1 ) >= 0 )
            && ( p_position.getQuick( 0 ) < m_row )
            && ( p_position.getQuick( 1 ) < m_column );
    }

    @Override
    public final DoubleMatrix1D clip( final DoubleMatrix1D p_position )
    {
        // clip position values if needed
        p_position.setQuick( 0, CEnvironment.clip( p_position.getQuick( 0 ), m_row ) );
        p_position.setQuick( 1, CEnvironment.clip( p_position.getQuick( 1 ), m_column ) );

        return p_position;
    }

    /**
     * value clipping
     *
     * @param p_value value
     * @param p_max maximum
     * @return modifed value
     */
    private static double clip( final double p_value, final double p_max )
    {
        return Math.max( Math.min( p_value, p_max - 1 ), 0 );
    }


    // --- visualization ---------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public final TiledMap map()
    {
        return new TmxMapLoader().load( CCommon.PACKAGEPATH + "background.tmx" );
    }
    
}