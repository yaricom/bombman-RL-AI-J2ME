/**
 * $Id: GameEffects.java 994 2006-08-30 16:10:16Z yaric $
 */
package ng.games.bombman.media;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;

import ng.games.bombman.BombmanDAO;
import ng.games.bombman.Settings;

/**
 * MMAPI specific for advanced handsets.
 * This class represents game effects, such as sounds, music, vibration, etc.
 *
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class GameEffects
{
  /** <code>true</code> if device has sound support */
  public static final boolean hasSound = true;

  /** Title sound */
  private Player titleSound;

  /**
   * Array to hold game sounds
   * <ul>
   * <li> 0 - Blow sound
   * <li> 1 - Bonus sound
   * <li> 2 - Scream sound
   * <li> 3 - Win sound
   * </ul>
   */
  private Player[] gameSounds;

  /** The index of current active sound */
  private int activeSoundIndex;

  /**
   * Default constructor.
   * @param dao the DAO to acquire resources.
   */
  public GameEffects( BombmanDAO dao )
  {
    InputStream is = null;
    try{
      // load sounds
      Object[] tmp = dao.loadBundle( "sound" );
      gameSounds = new Player[ 4 ];
      for( int i = 3; i >= 0; i-- ){
        gameSounds[ i ] = Manager.createPlayer(
                          new ByteArrayInputStream( ( byte[] )tmp[ i ] ),
                          "audio/x-wav" );
        gameSounds[ i ].realize();
      }

      // load title music
      is = getClass().getResourceAsStream( "/title.mid");
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
        // release resources
        this.deallocateSounds();
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
  public void bombExplosion()
  {
    this.playSound( 0 );
  }

  /**
   * Pickup bonus
   */
  public void pickUpBonus()
  {
    this.playSound( 1 );
  }

  /**
   * Scream
   */
  public final void scream()
  {
    this.playSound( 2 );
  }

  /**
   * Win sound
   */
  public final void winSound()
  {
    this.playSound( 3 );
  }

  /**
   * Method to destroy all resources associated with this class.
   */
  public final void destroy()
  {
    if( this.titleSound != null )
      try{
        this.stopTitleMusic();
        // release resources
        this.deallocateSounds();
      } catch( Exception ex ){
      }
  }

  /**
   * Method to play game sound.
   * @param index the index of sound to play.
   */
  private void playSound( int index )
  {
    if( Settings.sound )
      try{
        if( activeSoundIndex >= 0 && activeSoundIndex != index ){
          this.gameSounds[ activeSoundIndex ].stop();
          this.gameSounds[ activeSoundIndex ].deallocate();
        }

        if( activeSoundIndex != index ){
          this.gameSounds[ index ].prefetch();
          activeSoundIndex = index;
        }
        else// if( this.gameSounds[ index ].getState() == Player.STARTED )
          this.gameSounds[ index ].stop();

        this.gameSounds[ index ].start();
      } catch( Exception ex ){
        ex.printStackTrace();
      }
  }

  /**
   * Method to deallocate game sounds
   */
  private void deallocateSounds()
  {
    int state;
    for( int i = 0; i < this.gameSounds.length; i++ ){
      if( this.gameSounds[ i ] != null ){
        state = this.gameSounds[ i ].getState();
        if( state == Player.PREFETCHED || state == Player.STARTED )
          this.gameSounds[ i ].deallocate();
      }
    }
    activeSoundIndex = -1;
  }
}