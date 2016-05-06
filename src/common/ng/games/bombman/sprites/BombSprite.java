/**
 * $Id: BombSprite.java 197 2005-07-08 16:00:57Z yaric $
 */
package ng.games.bombman.sprites;

import javax.microedition.lcdui.Graphics;

import ng.games.bombman.GameConfig;
import ng.games.bombman.GameEngine;
import ng.games.bombman.Images;
import ng.mobile.game.util.Log;

/**
 * Holds definition of bomb sprite.
 *
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class BombSprite
{
  /** Maximal number of steps until explosion */
  private static final byte STEPS_UNTIL_EXPLOSION = 14;

  /** The left X coordinate of sprite relative to the board */
  private int x;
  /** The top Y coordinate of sprite relative to the board */
  private int y;

  /** Current state of bomb */
  private byte state;
  /** Undefined state */
  public static final byte STATE_UNDEFINED = 0;
  /** Charging state */
  public static final byte STATE_CHARGING = 1;
  /** Charged state */
  public static final byte STATE_CHARGED = 2;
  /** Explosion state */
  public static final byte STATE_EXPLOSION = 3;


  /** Game engine instance */
  private final GameEngine engine;
  /** Current frame index to use */
  private byte frameIndex;
  /** Last time */
  private int lastTime;
  /** Number of ticks before bomb explosion */
  protected byte ticksToExplode;
  /** Current explosion range */
  private int range;
  /** Column where bomb is charged */
  public int col;
  /** Row where bomb is charged */
  public int row;
  /** Holds definition of destruction range */
  private byte[] fireZone;

  /** Bombman charged this bomb */
  private BombmanSprite bombman;

  //
  // Logging definition
  //
  private static final boolean logAffectCell = Log.enabled & false;

  /**
   * Constructs new instance.
   * @param engine the game engine instance.
   */
  public BombSprite( GameEngine engine )
  {
    this.engine = engine;
    this.fireZone = new byte[ GameConfig.extendedEplosionRange * 4 ];
    this.reset();
  }

  /**
   * Method to reset sprite status.
   */
  public final void reset()
  {
    this.state = STATE_UNDEFINED;
    this.frameIndex = 1;
    this.ticksToExplode = STEPS_UNTIL_EXPLOSION;
    this.range = 0;
    this.col = 0;
    this.row = 0;
    this.bombman = null;
    for( int i = 0; i < this.fireZone.length; i++ )
      this.fireZone[ i ] = -1;
  }

  /**
   * Invoked to signal that bomb should become charged.
   * @param bombman the bombman charging this bomb.
   * @param col the column where bomb is charged.
   * @param row the row where bomb is charged.
   */
  public final void charge( BombmanSprite bombman, int col, int row )
  {
    this.bombman = bombman;
    this.state = STATE_CHARGING;
    this.range = bombman.getExplosionRange();
    this.col = col;
    this.row = row;
    this.x = col * GameConfig.tileWidth;
    this.y = row * GameConfig.tileHeight;
    // signal that this cell is occupied by bomb
    this.engine.setCell( GameEngine.BOMB_PLACE, col, row );
    // find and mark destruction range of this bomb
    this.markDesctructionRange();
  }

  /**
   * Returns bombman charged this bomb.
   * @return bombman charged this bomb.
   */
  public final BombmanSprite getBombman()
  {
    return bombman;
  }

  /**
   * Returns <code>true</code> if bomb is already charged.
   * @return <code>true</code> if bomb is already charged.
   */
  public final boolean charged()
  {
    return this.state != STATE_UNDEFINED;
  }

  /**
   * Check whether specified cell will be affected by bomb explosion and if so
   * than return distance between specified cell and bomb. If cell will not be
   * affected than -1 returned.
   *
   * @param cCol the cell column.
   * @param cRow the cell row.
   * @return distance between specified cell and bomb or -1 if specified cell
   * will not be affected.
   */
  protected final int affectCell( int cCol, int cRow )
  {
    boolean affected = false;
    int affectRange = -1;
    if( this.charged() ){
      // process only when bomb is charged
      int dr = cRow - this.row;
      int adr = Math.abs( dr );
      int dc = cCol - this.col;
      int adc = Math.abs( dc );
      int index = -1;
      if( ( cCol == this.col && adr <= this.range ) ){
        affectRange = adr;
        // check in vertical direction
        if( dr == 0 )
          affected = true;
        else
          index = dr < 0 ? affectRange + 5 : affectRange + 17;// above : below
      }
      else if( ( cRow == this.row && adc <= this.range ) ){
        affectRange = adc;
        // check in horizontal direction
        if( dc == 0 )
          affected = true;
        else
          index = dc < 0 ? affectRange - 1 : affectRange + 11;// left : right
      }
      // check if affected
      if( index != -1 )
        affected = this.fireZone[ index ] != -1;
    }
    // the cell where bomb is placed shouldn't be considered in range calculations,
    // because you can't go to the bomb place, so decrease range by one
    affectRange--;
    if( logAffectCell ){
      Log.log( " AFFECT CELL", "col: " + cCol + " row: " + cRow +
          " affectRange: " + affectRange + " bomb [ col: " + this.col +
          " row: " + this.row + " ]" );
    }

    if( affected )
      return affectRange;
    else
      return -1;
  }


  /**
   * Updates state of the bomb if appropriate.
   * @param time the current game time.
   */
  public final void update( int time )
  {
    int dt = time - lastTime;
    switch( this.state ){
      case STATE_CHARGING:
        this.lastTime = time;
        this.state = STATE_CHARGED;
        break;

      case STATE_CHARGED:
        // signal that this cell is occupied by bomb
        this.engine.setCell( GameEngine.BOMB_PLACE, col, row );
        if( dt >= GameConfig.bombStepChangeInterval ){
          this.lastTime = time;
          if( this.ticksToExplode-- > 0 ){
            this.frameIndex = (byte)( ( this.frameIndex + 1 ) % 3 );
          }
          else{
            engine.parent.game.effects.bombExplosion();
            this.state = STATE_EXPLOSION;
            this.frameIndex = 0;
          }
        }
        break;

      case STATE_EXPLOSION:
        // signal that this cell is occupied by bomb
        this.engine.setCell( GameEngine.BOMB_PLACE, col, row );
        if( dt >= GameConfig.bombStepChangeInterval ){
          this.lastTime = time;
          // check whether anything on the board can be blown up
          this.blowUpDestructionRange( true );
          // clear bomb cell
          this.engine.eraseCell( this.col, this.row );
          // check whether explosion is complete
          if( ++this.frameIndex >= GameConfig.flameSegments ){
            this.blowUpDestructionRange( false );// decrease hazzard status of affected cells
            this.engine.bombHaveBeenExploded( this );
            this.reset();
          }
        }
        break;
    }
  }

  /**
   * Draws this sprite.
   * @param g the graphics context to draw on.
   */
  public final void paint( Graphics g )
  {
    int xx = this.x + engine.boardX;
    int yy = this.y + engine.boardY;
    switch( this.state ){
      case STATE_CHARGED:
        int h = GameConfig.tileHeight;
        int w = GameConfig.tileWidth;
        if( yy + GameConfig.tileHeight > GameConfig.gameScreenActiveHeight )
          h = GameConfig.gameScreenActiveHeight - yy;
        if( xx + GameConfig.tileWidth > GameConfig.screenWidth )
          w = GameConfig.screenWidth - xx;
        if( h > 0 && w > 0 ){
          // draw only visible part
          g.setClip( xx, yy, w, h );
          g.drawImage( this.engine.parent.game.commonImages[ Images.TILES ],
              xx - ( 7 + this.frameIndex ) * GameConfig.tileWidth, yy,
              Graphics.TOP | Graphics.LEFT );
        }
        break;

      case STATE_EXPLOSION:
        // draw center
        this.drawFlameSegment( xx, yy, (byte)GameConfig.flameCenter, g );
        // draw branches starting from center
        for( int i = this.range, offset, index; i > 0; i-- ){
          offset = i * GameConfig.tileWidth;
          // draw left
          this.drawFlameSegment( xx - offset, yy, this.fireZone[ i - 1 ], g );
          // draw top
          this.drawFlameSegment( xx, yy - offset, this.fireZone[ i + 5 ], g );
          // draw right
          this.drawFlameSegment( xx + offset, yy, this.fireZone[ i + 11 ], g );
          // draw bottom
          this.drawFlameSegment( xx, yy + offset, this.fireZone[ i + 17 ], g );
        }
        break;
    }
  }

  /**
   * Founds anything within destruction range on the board and blows it up or
   * decrease hazzard range.
   * @param blowup if <code>true</code> than cell will be blown up, otherwise
   * its hazard status will be simply decreased without blowing anything on it.
   */
  protected final void blowUpDestructionRange( boolean blowup )
  {
    // check center
    if( blowup )
      this.engine.blowUpBombman( col, row );
    else // clear mark in cell where bomb is placed
      this.engine.changeCellHazardStatus( this.col, this.row, false );

    // check branches
    for( int i = this.range, c, r, index; i > 0; i-- ){
      // check left branch
      c = this.col - i;
      r = this.row;
      index = i - 1;
      if( this.fireZone[ index ] != -1 ){
        if( blowup )
          this.engine.blowUpCell( bombman, c, r );
        else
          this.engine.changeCellHazardStatus( c, r, false );
      }

      // check top branch
      c = this.col;
      r = this.row - i;
      index = i + 5;
      if( this.fireZone[ index ] != -1 ){
        if( blowup )
          this.engine.blowUpCell( bombman, c, r );
        else
          this.engine.changeCellHazardStatus( c, r, false );
      }

      // check right branch
      c = this.col + i;
      r = this.row;
      index = i + 11;
      if( this.fireZone[ index ] != -1 ){
        if( blowup )
          this.engine.blowUpCell( bombman, c, r );
        else
          this.engine.changeCellHazardStatus( c, r, false );
      }

      // check down branch
      c = this.col;
      r = this.row + i;
      index = i + 17;
      if( this.fireZone[ index ] != -1 ){
        if( blowup )
          this.engine.blowUpCell( bombman, c, r );
        else
          this.engine.changeCellHazardStatus( c, r, false );
      }
    }
  }

  /**
   * Draws specified flame segment.
   * @param xx the left X coordinate for segment drawing.
   * @param yy the top Y coordinate for segment drawing.
   * @param segment the segment definition.
   * @param g the graphics context to draw on.
   */
  private final void drawFlameSegment( int xx, int yy, byte segment, Graphics g )
  {
    if( segment != -1 ){
      int h = GameConfig.tileHeight;
      int w = GameConfig.tileWidth;
      if( yy + GameConfig.tileHeight > GameConfig.gameScreenActiveHeight )
        h = GameConfig.gameScreenActiveHeight - yy;
      if( xx + GameConfig.tileWidth > GameConfig.screenWidth )
          w = GameConfig.screenWidth - xx;
      if( h > 0 && w > 0 ){
        g.setClip( xx, yy, w, h );
        xx = xx - GameConfig.tileWidth * ( segment + this.frameIndex );
        g.drawImage( this.engine.parent.game.commonImages[ Images.FLAME ],
            xx, yy, Graphics.TOP | Graphics.LEFT );
      }
    }
  }

  /**
   * Calculates current destruciton range for this bomb.
   */
  protected final void markDesctructionRange()
  {
    boolean left = false, top = false, right = false, down = false;
    for( int i = 1, c, r, index; i <= this.range; i++ ){
      // check left
      c = this.col - i;
      r = this.row;
      index = i - 1;
      this.fireZone[ index ] = -1;// erase previous settings if any
      if( !left ){
        // path still open for a flame
        if( this.explosible( c, r ) ){
          // empty cell found
          if( i != this.range )
            this.fireZone[ index ] = ( byte )GameConfig.flameHorizontal;
          else
            this.fireZone[ index ] = ( byte )GameConfig.flameLeft;
          // increase hazzard status for cell
          this.engine.changeCellHazardStatus( c, r, true );
        }
        else if( this.engine.isSoftWallAt( c, r ) ){
          // first soft wall encountered
          this.fireZone[ index ] = ( byte )GameConfig.flameLeft;
          // increase hazzard status for cell
          this.engine.changeCellHazardStatus( c, r, true );
          left = true;
        }
        else if( c >= 0 ){
          // hard wall or bomb found
          left = true;
          if( index > 0 )
            this.fireZone[ index - 1 ] = ( byte )GameConfig.flameLeft;
        }
      }

      // check top
      c = this.col;
      r = this.row - i;
      index = i + 5;
      this.fireZone[ index ] = -1;// erase previous settings if any
      if( !top ){
        // still path open for a flame
        if( this.explosible( c, r ) ){
          // empty cell found
          if( i != this.range )
            this.fireZone[ index ] = ( byte )GameConfig.flameVertical;
          else
            this.fireZone[ index ] = ( byte )GameConfig.flameUp;
          // increase hazzard status for cell
          this.engine.changeCellHazardStatus( c, r, true );
        }
        else if( this.engine.isSoftWallAt( c, r ) ){
          // first soft wall encountered
          this.fireZone[ index ] = ( byte )GameConfig.flameUp;
          // increase hazzard status for cell
          this.engine.changeCellHazardStatus( c, r, true );
          top = true;
        }
        else if( r >= 0 ){
          // hard wall found
          top = true;
          if( index > 6 )
            this.fireZone[ index - 1 ] = ( byte )GameConfig.flameUp;
        }
      }

      // check right
      c = this.col + i;
      r = this.row;
      index = i + 11;
      this.fireZone[ index ] = -1;// erase previous settings if any
      if( !right ){
        // still path open for a flame
        if( this.explosible( c, r ) ){
          // empty cell found
          if( i != this.range )
            this.fireZone[ index ] = ( byte )GameConfig.flameHorizontal;
          else
            this.fireZone[ index ] = ( byte )GameConfig.flameRight;
          // increase hazzard status for cell
          this.engine.changeCellHazardStatus( c, r, true );
        }
        else if( this.engine.isSoftWallAt( c, r ) ){
          // first soft wall encountered
          this.fireZone[ index ] = ( byte )GameConfig.flameRight;
          // increase hazzard status for cell
          this.engine.changeCellHazardStatus( c, r, true );
          right = true;
        }
        else if( c < this.engine.colNumber ){
          // hard wall found
          right = true;
          if( index > 12 )
            this.fireZone[ index - 1 ] = ( byte )GameConfig.flameRight;
        }
      }

      // check down
      c = this.col;
      r = this.row + i;
      index = i + 17;
      this.fireZone[ index ] = -1;// erase previous settings if any
      if( !down ){
        // still path open for a flame
        if( this.explosible( c, r ) ){
          // empty cell found
          if( i != this.range )
            this.fireZone[ index ] = ( byte )GameConfig.flameVertical;
          else
            this.fireZone[ index ] = ( byte )GameConfig.flameDown;
          // increase hazzard status for cell
          this.engine.changeCellHazardStatus( c, r, true );
        }
        else if( this.engine.isSoftWallAt( c, r ) ){
          // first soft wall encountered
          this.fireZone[ index ] = ( byte )GameConfig.flameDown;
          // increase hazzard status for cell
          this.engine.changeCellHazardStatus( c, r, true );
          down = true;
        }
        else if( r < this.engine.rowsNumber ){
          // hard wall found
          down = true;
          if( index > 18 )
            this.fireZone[ index - 1 ] = ( byte )GameConfig.flameDown;
        }
      }
    }

    // mark cell where bomb is placed
    this.engine.changeCellHazardStatus( this.col, this.row, true );
  }

  /**
   * Returns <code>true</code> if specified cell can be destructed.
   * @param col the column of cell.
   * @param row the row of cell.
   * @return <code>true</code> if this cell can be blowed up by bomb explosion.
   */
  private final boolean explosible( int col, int row )
  {
    if( col <= 0 || col >= this.engine.colNumber ||
        row <= 0 || row >= this.engine.rowsNumber )
      return false;

    byte content = this.engine.getCell( col, row );
    if( content != GameEngine.SOFT_WALL_PLACE &&
        content != GameEngine.HARD_WALL_PLACE )
      return true;
    else
      return false;
  }
}