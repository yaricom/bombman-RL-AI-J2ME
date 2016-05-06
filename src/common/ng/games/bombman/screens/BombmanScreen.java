/**
 * $Id: BombmanScreen.java 2 2005-06-30 12:47:28Z yaric $
 */
package ng.games.bombman.screens;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

import ng.games.bombman.BombmanGame;

/**
 * This class represents drawing and events recieving facilities. It recieves
 * such low level events from underlying system and forwards it to the game controller.
 * Used to encapsulate all device specific details from the game.
 * This is standard MIDP 1.0 variant of such object.
 *
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class BombmanScreen extends Canvas
{
  /** Canvas listener to delegate game events to */
  public final BombmanGame game;

  /**
   * Constructs new instance.
   * @param parent the parent game.
   */
  public BombmanScreen( BombmanGame parent )
  {
    this.game = parent;
  }

  /**
   * Method to perform initialization steps if required.
   */
  public void init()
  {
  // just stub
  }

  /**
   * Called when a key is pressed.
   * @param keyCode The key code of the key that was repeated
   */
  protected void keyPressed( int keyCode )
  {
    this.game.keyPressed( keyCode );
  }

  /**
   * Called when a key is released.
   * @param keyCode The key code of the key that was released
   */
  protected void keyReleased( int keyCode )
  {
    this.game.keyReleased( keyCode );
  }

  /**
   * Hides commands in full screen mode.
   * @param hide if <code>true</code> than commands will not be drawn in full
   * screen mode, otherwise its will be drawn.
   */
  public void hideCommands( boolean hide )
  {
    // just stub
  }

  /**
   * Method to render current game.
   * @param g The Graphics context.
   */
  protected void paint( Graphics g )
  {
    this.game.paint( g );
  }

  /**
   * Gets the game action associated with the given key code of the device.
   * @param keyCode the key code.
   * @return the game action corresponding to this key, or 0 if none.
   */
  public int getGameAction( int keyCode )
  {
    int action = super.getGameAction( keyCode );
    if( action == Canvas.UP || keyCode == ActionKeysMappings.GAME_KEY_UP )
      action = Canvas.UP;
    else if( action == Canvas.DOWN || keyCode == ActionKeysMappings.GAME_KEY_DOWN )
      action = Canvas.DOWN;
    else if( action == Canvas.LEFT || keyCode == ActionKeysMappings.GAME_KEY_LEFT )
      action = Canvas.LEFT;
    else if( action == Canvas.RIGHT || keyCode == ActionKeysMappings.GAME_KEY_RIGHT )
      action = Canvas.RIGHT;
    else if( action == Canvas.FIRE || keyCode == ActionKeysMappings.GAME_KEY_FIRE )
      action = Canvas.FIRE;

    return action;
  }

  /**
   * Invoked shortly after the Canvas has been removed from the display.
   * Used to pause game execution for Nokia phones.
   */
  protected void hideNotify()
  {
    this.game.hideNotify();
  }
}