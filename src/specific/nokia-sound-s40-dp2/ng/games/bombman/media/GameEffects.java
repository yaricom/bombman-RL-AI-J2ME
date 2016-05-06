/**
 * $Id: GameEffects.java 1015 2006-09-12 12:29:33Z yaric $
 */
package ng.games.bombman.media;

import java.io.InputStream;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

import com.nokia.mid.sound.Sound;

import ng.games.bombman.BombmanDAO;
import ng.games.bombman.Settings;

/**
 * Nokia series 40 DP 2 specific.
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
  public static final boolean hasSound = true;

  /** Title sound */
  private Player titleSound;

  /** Bomb explosion sound */
  private static final Sound bombExplosionSound =
      new Sound( parseOTASoundString( "034a443a78014801a401b8019c01a401b8019c008001d001bc01b80194008000c404001528961b8a88c4a31272259800" ),
      Sound.FORMAT_TONE );

  /** Win sound */
  private static final Sound winSound =
      new Sound( parseOTASoundString( "034a443a78014801a401b8019c01a401b8019c008001d001bc01b80194008000c404001328961861c61c61c628aa0000" ),
      Sound.FORMAT_TONE );

  /** Bonus sound */
  private static final Sound bonusSound =
      new Sound( parseOTASoundString( "034a443a78014801a401b8019c01a401b8019c008001d001bc01b80194008000cc04000f289619628930a30000" ),
      Sound.FORMAT_TONE );

  /** Scream sound */
  private static final Sound screamSound =
      new Sound( parseOTASoundString( "034a443a78014801a401b8019c01a401b8019c008001d001bc01b80194008000c404000f28961789c89989360000" ),
      Sound.FORMAT_TONE );



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
      titleSound.realize();
      // release resoucres
      is.close();
    }
    catch( Exception ex ){
      ex.printStackTrace();
    }
  }

  /**
   * Starts playing title music.
   */
  public void startTitleMusic()
  {
    if( this.titleSound != null && Settings.music ){
      try{
        // start title music
        titleSound.prefetch();
        titleSound.setLoopCount( -1 );
        this.titleSound.start();
      } catch( Exception ex ){}
    }
  }

  /**
   * Stops playing title music.
   */
  public void stopTitleMusic()
  {
    if( this.titleSound != null ){
      try{
        this.titleSound.stop();
        this.titleSound.deallocate();
      }
      catch( Exception ex ){}
    }
  }

  /**
   * Bomb explosion
   */
  public void bombExplosion()
  {
    this.playSound( bombExplosionSound );
  }

  /**
   * Pickup bonus
   */
  public void pickUpBonus()
  {
    this.playSound( bonusSound );
  }

  /**
   * Scream
   */
  public final void scream()
  {
    this.playSound( screamSound );
  }

  /**
   * Win sound
   */
  public final void winSound()
  {
    this.playSound( winSound );
  }

  /**
   * Method to destroy all resources associated with this class.
   */
  public final void destroy(){}

  /**
   * Play specific sound
   * @param sound the sound to play.
   */
  private void playSound( Sound sound )
  {
    if( Settings.sound ){
      try{
        if( sound.getState() == Sound.SOUND_PLAYING )
          sound.stop();
        sound.play( 1 );
      } catch( Exception e ){
        e.printStackTrace();
      }
    }
  }

  /**
   * Method to parse sound defined using OTA specification.
   * @param soundString the sound encoded in OTA string.
   * @return array of bytes, which represent sound.
   */
  private static byte[] parseOTASoundString( String soundString )
  {
    soundString = soundString.toUpperCase();
    byte sound[] = new byte[ soundString.length() / 2 ];
    for( int i = 0; i < soundString.length(); i += 2 ){
      char litera = soundString.charAt( i + 1 );
      int soundByte;
      if( litera >= '0' && litera <= '9' ){
        soundByte = litera - 48;
      }
      else{
        soundByte = ( litera - 65 ) + 10;
      }
      litera = soundString.charAt( i );
      if( litera >= '0' && litera <= '9' ){
        soundByte += ( litera - 48 ) * 16;
      }
      else{
        soundByte += ( ( litera - 65 ) + 10 ) * 16;
      }
      sound[ i / 2 ] = ( byte )soundByte;
    }
    return sound;
  }
}
