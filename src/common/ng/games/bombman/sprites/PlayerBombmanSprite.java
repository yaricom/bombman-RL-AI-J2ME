/**
 * $Id: PlayerBombmanSprite.java 197 2005-07-08 16:00:57Z yaric $
 */
package ng.games.bombman.sprites;

import javax.microedition.lcdui.Graphics;

import ng.games.bombman.GameConfig;
import ng.games.bombman.GameEngine;
import ng.games.bombman.screens.BombmanScreen;

/**
 * Represents game sprite managed by player.
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class PlayerBombmanSprite extends BombmanSprite
{
  /** Flag to indicate that bomb should be droped when bombman comes to the next cell */
  private boolean dropBomb;

  /**
   * Constructs new PlayerSprite
   * @param engine the game engine instance.
   */
  public PlayerBombmanSprite( GameEngine engine )
  {
    super( engine );
  }
  /**
   * Returns type of this sprite.
   * @return type of this sprite.
   */
  public int getType()
  {
    return TYPE_PLAYER;
  }

  /**
   * Called when a key is pressed.
   * @param gameCode The game code associated with pressed key.
   */
  public void keyPressed( int gameCode )
  {
    if( gameCode != BombmanScreen.FIRE )
      this.nextMovingDirection = gameCode;
    else
      this.dropBomb = true;
  }

  /**
   * Called when a key is released.
   * @param gameCode The game code associated with pressed key.
   */
  public void keyReleased( int gameCode )
  {
    if( this.nextMovingDirection == gameCode )
      nextMovingDirection = GameEngine.STOP;
  }

  /**
   * Method to reset player status.
   */
  public void reset()
  {
    super.reset();
    this.dropBomb = false;
  }

  /**
   * Method to update player state in active state.
   * @param time the current game time.
   */
  public final void update( int time )
  {
    super.update( time );

    if( this.state == STATE_ACTIVE ){
      // try to drop bomb in current cell
      if( this.dropBomb && this.bombsInPocket > 0 )
        this.tryToDropBomb();
      this.dropBomb = false;// reset to avoid bomb drops repeating
      // calculate next cell
      if( movingDirection != GameEngine.STOP )
        this.finishMoving( time );
      else
        this.initMoving();
      // check speed bonus expiration when player not moving
      if( movingDirection == GameEngine.STOP &&
          nextMovingDirection == GameEngine.STOP && this.checkSpeed( time ) )
        this.engine.startClockBeeping();
    }
  }

  /**
   * Should be implemented by subclasses in order to provide logic of death.
   */
  protected void processDeath()
  {
    this.engine.playerDead();
  }

  /**
   * Should be implemented by subclasses in order to provide logic of win.
   */
  protected void processWin()
  {
    this.engine.playerWin();
  }

  /**
   * Draws this sprite on the provided graphics context.
   * @param g the graphics context.
   */
  public void paint( Graphics g )
  {
    int xx = x + engine.boardX;
    int yy = y + engine.boardY;
    switch( this.state ){
      case STATE_ACTIVE:
      case STATE_FREEZE:
        g.setClip( xx, yy, GameConfig.tileWidth, GameConfig.tileHeight );
        g.drawImage( this.engine.bombermanImages[ 0 ],
            xx - frameIndex * GameConfig.tileWidth,
            yy, Graphics.TOP | Graphics.LEFT );
        break;

      case STATE_DEATH_ANIMATION:
        g.setClip( xx, yy, GameConfig.tileWidth, GameConfig.tileHeight );
        g.drawImage( this.engine.bombermanImages[ 1 ],
            xx - frameIndex * GameConfig.tileWidth,
            yy, Graphics.TOP | Graphics.LEFT );
        break;

      case STATE_WIN_ANIMATION:
        yy += GameConfig.tileHeight - GameConfig.bombmanWinAnimationHeight;
        g.setClip( xx, yy, GameConfig.bombmanWinAnimationWidth,
            GameConfig.bombmanWinAnimationHeight );
        g.drawImage( this.engine.bombermanImages[ 2 ],
            xx - frameIndex * GameConfig.bombmanWinAnimationWidth,
            yy, Graphics.TOP | Graphics.LEFT );
        break;
    }
  }

  /**
   * Method to initialize moving of player in particullar direction.
   */
  private final void initMoving()
  {
    switch( nextMovingDirection ){
      case BombmanScreen.UP: // UP
        row--;
        if( row >= 0 && canMoveTo( col, row ) ){
          if( engine.getCell( col, row ) == GameEngine.EMPTY_PLACE ||
              engine.getCell( col, row ) == GameEngine.SHADOWED_EMPTY_PLACE ){
            engine.setCell( GameEngine.PLAYER_PLACE, col, row );
          }
          engine.movePlayerUP( this.movingSpeed );
          frameIndex = 1;
          movingDirection = BombmanScreen.UP;
          this.move( 0, -this.movingSpeed );
        }
        else if( movingDirection != BombmanScreen.UP ){
          frameIndex = 0;
        }
        break;
      case BombmanScreen.DOWN: // DOWN
        row++;
        if( row < engine.rowsNumber && canMoveTo( col, row ) ){
          if( engine.getCell( col, row ) == GameEngine.EMPTY_PLACE ||
              engine.getCell( col, row ) == GameEngine.SHADOWED_EMPTY_PLACE ){
            engine.setCell( GameEngine.PLAYER_PLACE, col, row );
          }
          engine.movePlayerDown( this.movingSpeed  );
          frameIndex = 4;
          movingDirection = BombmanScreen.DOWN;
          this.move( 0, this.movingSpeed );
        }
        else if( movingDirection != BombmanScreen.DOWN ){
          frameIndex = 3;
        }
        break;
      case BombmanScreen.LEFT: // LEFT
        col--;
        if( col >= 0 && canMoveTo( col, row ) ){
          if( engine.getCell( col, row ) == GameEngine.EMPTY_PLACE ||
              engine.getCell( col, row ) == GameEngine.SHADOWED_EMPTY_PLACE ){
            engine.setCell( GameEngine.PLAYER_PLACE, col, row );
          }
          engine.movePlayerLeft( this.movingSpeed);
          frameIndex = 7;
          movingDirection = BombmanScreen.LEFT;
          this.move( -this.movingSpeed, 0 );
        }
        else if( movingDirection != BombmanScreen.LEFT ){
          frameIndex = 6;
        }
        break;
      case BombmanScreen.RIGHT: // RIGHT
        col++;
        if( col < engine.colNumber && canMoveTo( col, row ) ){
          if( engine.getCell( col, row ) == GameEngine.EMPTY_PLACE ||
              engine.getCell( col, row ) == GameEngine.SHADOWED_EMPTY_PLACE ){
            engine.setCell( GameEngine.PLAYER_PLACE, col, row );
          }
          engine.movePlayerRight( this.movingSpeed );
          frameIndex = 10;
          movingDirection = BombmanScreen.RIGHT;
          this.move( this.movingSpeed, 0 );
        }
        else if( movingDirection != BombmanScreen.RIGHT ){
          frameIndex = 9;
        }
        break;
    }
  }

  /**
   * Method to finish moving of player to the destination cell.
   * @param time the current game time.
   */
  private final void finishMoving( int time )
  {
    int mod = 0;
    switch( this.movingDirection ){
      case BombmanScreen.UP: // UP
        mod = y % GameConfig.tileHeight;
        if( mod != 0 ){
          // target cell doesn't reached yet
          this.move( 0, -this.movingSpeed );
          this.engine.movePlayerUP( this.movingSpeed );
          if( mod == GameConfig.tileHeight / 2 ){
            frameIndex = 2;
          }
        }
        else{
          // player in target cell
          this.engine.processBombmanMovement( this, col, row + 1 );
          movingDirection = GameEngine.STOP;
          if( nextMovingDirection == GameEngine.STOP ){
            frameIndex = 0;
          }
          if( this.checkSpeed( time ) )
            this.engine.startClockBeeping();
        }
        break;
      case BombmanScreen.DOWN: // DOWN
        mod = y % GameConfig.tileHeight;
        if( mod != 0 ){
          // target cell doesn't reached yet
          this.move( 0, this.movingSpeed );
          this.engine.movePlayerDown( this.movingSpeed );
          if( mod == GameConfig.tileHeight / 2 ){
            frameIndex = 5;
          }
        }
        else{
          // player in target cell
          this.engine.processBombmanMovement( this, col, row - 1 );
          movingDirection = GameEngine.STOP;
          if( nextMovingDirection == GameEngine.STOP ){
            frameIndex = 3;
          }
          if( this.checkSpeed( time ) )
            this.engine.startClockBeeping();
        }
        break;
      case BombmanScreen.LEFT: // LEFT
        mod = x % GameConfig.tileWidth;
        if( mod != 0 ){
          // target cell doesn't reached yet
          this.move( -this.movingSpeed, 0 );
          this.engine.movePlayerLeft( this.movingSpeed );
          if( mod == GameConfig.tileWidth / 2 ){
            frameIndex = 8;
          }
        }
        else{
          // player in target cell
          this.engine.processBombmanMovement( this, col + 1, row );
          movingDirection = GameEngine.STOP;
          if( nextMovingDirection == GameEngine.STOP ){
            frameIndex = 6;
          }
          if( this.checkSpeed( time ) )
            this.engine.startClockBeeping();
        }
        break;
      case BombmanScreen.RIGHT: // RIGHT
        mod = x % GameConfig.tileWidth;
        if( mod != 0 ){
          // target cell doesn't reached yet
          this.move( this.movingSpeed, 0 );
          this.engine.movePlayerRight( this.movingSpeed );
          if( mod == GameConfig.tileWidth / 2 ){
            frameIndex = 11;
          }
        }
        else{
          // player in target cell
          this.engine.processBombmanMovement( this, col - 1, row );
          movingDirection = GameEngine.STOP;
          if( nextMovingDirection == GameEngine.STOP ){
            frameIndex = 9;
          }
          if( this.checkSpeed( time ) )
            this.engine.startClockBeeping();
        }
        break;
    }
  }

  /**
   * Returns <code>true</code> if player's sprite can move to the specified position.
   * @param col the column to move.
   * @param row the row to move.
   * @return <code>true</code> if player's sprite can move to the specified position.
   */
  protected final boolean canMoveTo( int col, int row )
  {
    boolean possible = super.canMoveTo( col, row );
    if( possible ){
      // check collision with opponent
      int yy = row * GameConfig.tileHeight;
      int xx = col * GameConfig.tileWidth;
      possible = !( yy < engine.aiBombman.y + GameConfig.tileHeight &&
        yy + GameConfig.tileHeight > engine.aiBombman.y &&
        xx < engine.aiBombman.x + GameConfig.tileWidth &&
        xx + GameConfig.tileWidth > engine.aiBombman.x );
    }
    return possible;
  }
}