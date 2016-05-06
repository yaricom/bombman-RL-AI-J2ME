/**
 * $Id: GameEffects.java 997 2006-08-30 16:14:09Z yaric $
 */
package ng.games.bombman.media;

import java.io.InputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;

import ng.games.bombman.BombmanDAO;
import ng.games.bombman.Settings;

/**
 * MMAPI specific with only midi title music playing.
 *
 * Class to perform game effects.
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class GameEffects
{
  /** <code>true</code> if device has sound support */
  public static final boolean hasSound = false;

  /** Title sound */
  private Player titleSound;

  /**
   * Default constructor.
   * @param dao the DAO to acquire resources.
   */
  public GameEffects( BombmanDAO dao )
  {
    InputStream is = null;
    try{
      // load title music
      is = getClass().getResourceAsStream( "/title.mid" );
      titleSound = Manager.createPlayer( is, "audio/midi" );
      titleSound.prefetch();
      titleSound.setLoopCount( -1 );
      is.close();
    } catch( Exception ex ){
      ex.printStackTrace();
    }
  }

  /**
   * Play title music.
   */
  public void startTitleMusic()
  {
    if( this.titleSound != null && Settings.music )
      try{
        // start title music
        if( this.titleSound.getState() != Player.PREFETCHED )
          this.titleSound.prefetch();
        this.titleSound.start();
      } catch( Exception ex ){}
  }

  /**
   * Stop title music.
   */
  public void stopTitleMusic()
  {
    try{
      this.titleSound.stop();
      this.titleSound.deallocate();
    } catch( Exception ex ){}
  }

  /**
   * Bomb explosion
   */
  public void bombExplosion(){}

  /**
   * Pickup bonus
   */
  public void pickUpBonus(){}

  /**
   * Scream
   */
  public final void scream()
  {
  }

  /**
   * Win sound
   */
  public final void winSound()
  {
  }

  /**
   * Method to destroy all resources associated with this class.
   */
  public final void destroy()
  {
    if( this.titleSound != null )
      try{
        this.stopTitleMusic();
      } catch( Exception ex ){
      }
  }
}