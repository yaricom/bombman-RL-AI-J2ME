/**
 * $Id: ClockSprite.java 2 2005-06-30 12:47:28Z yaric $
 */
package ng.games.bombman.sprites;

import javax.microedition.lcdui.Graphics;

import ng.games.bombman.GameEngine;
import ng.games.bombman.GameConfig;

/**
 * Represents clock sprite used to display time to blow. This sprite is driven
 * by registered bomb sprite.
 *
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class ClockSprite
{
  /** The left X coordinate of sprite relative to the board */
  private int x;
  /** The top Y coordinate of sprite relative to the board */
  private int y;

  /** Current state of clock */
  private byte state;
  /** Undefined state */
  public static final byte STATE_UNDEFINED = 0;
  /** Counting state */
  public static final byte STATE_COUNT_DOWN = 1;
  /** Beeping state */
  public static final byte STATE_BEEPING = 2;


  /** Game engine instance */
  private final GameEngine engine;
  /** Current frame index for bottom part */
  private byte frameIndexBottom;
  /** Current frame index for top part */
  private byte frameIndexTop;


  /** Beep tick */
  private int beepTick;
  /** Last time */
  private int lastTime;

  /**
   * Constructs new instance.
   * @param engine the game engine.
   */
  public ClockSprite( GameEngine engine )
  {
    this.engine = engine;
    this.stop();
  }

  /**
   * Method to stop clock and reset sprite status.
   */
  public final void stop()
  {
    this.state = STATE_UNDEFINED;
  }

  /**
   * Invoked to signal that count down should be started.
   */
  public final void startCountDown()
  {
    this.frameIndexBottom = 0;
    this.frameIndexTop = 1;
    this.beepTick = 0;
    this.x = GameConfig.clockAreaX;
    this.y = GameConfig.clockAreaY;
    this.lastTime = 0;

    this.state = STATE_COUNT_DOWN;
  }

  /**
   * Invoked to signal that beeping should be started
   */
  public final void startBeeping()
  {
    this.frameIndexTop = 0;
    this.state = STATE_BEEPING;
  }

  /**
   * Updates state of clock in accordance with current game time.
   * @param time the current game time.
   */
  public final void update( int time )
  {
    int dt = time - this.lastTime;
    switch( this.state ){
      case STATE_COUNT_DOWN:
        if( dt >= GameConfig.clockCountDownInterval ){
          this.frameIndexBottom = ( byte ) ( ( this.frameIndexBottom + 1 ) % 8 );
          this.lastTime = time;
        }
        break;

      case STATE_BEEPING:
        if( dt >= GameConfig.clockCountDownInterval / 2 ){
          this.lastTime = time;
          if( this.x != GameConfig.clockAreaX - 1 )
            this.x = GameConfig.clockAreaX - 1;
          else if( this.x != GameConfig.clockAreaX + 1 )
            this.x = GameConfig.clockAreaX + 1;
          // check whether clock should stop beeping
          if( this.beepTick++ >= GameConfig.clockBeepSteps )
            this.stop();
        }
        break;
    }
  }

  /**
   * Draws clock on the screen.
   * @param g the graphics context to draw on.
   */
  public final void paint( Graphics g )
  {
    if( this.state != STATE_UNDEFINED ){
      g.setClip( x, y, GameConfig.clockAreaWidth, GameConfig.clockAreaHeight );
      // draw top
      g.drawImage( engine.clockImages[ 1 ],
          x - this.frameIndexTop * GameConfig.clockAreaWidth, y,
          Graphics.TOP | Graphics.LEFT );
      // draw bottom
      g.drawImage( engine.clockImages[ 0 ],
          x - this.frameIndexBottom * GameConfig.clockAreaWidth,
          y + GameConfig.clockAreaHeight, Graphics.BOTTOM | Graphics.LEFT );
    }
  }
}