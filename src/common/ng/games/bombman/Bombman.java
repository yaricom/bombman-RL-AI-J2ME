/**
 * $Id: Bombman.java 1015 2006-09-12 12:29:33Z yaric $
 */
package ng.games.bombman;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

/**
 * This is main game runner.
 *
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */

public class Bombman extends MIDlet
{
  /** Game instance */
  private static BombmanGame game;
  /** Display used to show screens */
  public static Display display;

  /**
   * Signals the MIDlet that it has entered the Active state. First checks whether
   * game is already loaded and if not than create new game and starts it.
   */
  protected void startApp()
  {
    try{
      if( display == null )
        display = Display.getDisplay( this );
      if( game == null ){
        game = new BombmanGame( this );
        new Thread( game ).start();
      }
    }catch( Throwable ex ){
      this.quitApp();
    }
  }

  /**
   * Signals the MIDlet to stop and enter the Paused state.
   */
  protected void pauseApp()
  {
    if( game != null )
      game.pauseApp();
  }

  /**
   * Signals the MIDlet to terminate and enter the Destroyed state.
   * @param unconditional If <code>true</code> when this method is called,
   * the MIDlet must cleanup and release all resources.
   */
  protected void destroyApp(boolean unconditional)
  {
    if( this.game != null )
      game.destroyGame();
  }

  /**
   * Method to interupt game execution and quit MIDlet.
   */
  public void quitApp()
  {
    this.destroyApp( true );
    this.notifyDestroyed();
    this.game = null;
  }
}
