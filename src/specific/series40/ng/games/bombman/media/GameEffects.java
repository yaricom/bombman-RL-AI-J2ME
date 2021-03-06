/**
 * $Id: GameEffects.java 206 2005-07-12 11:54:29Z yaric $
 */
package ng.games.bombman.media;

import com.nokia.mid.sound.Sound;

import ng.games.bombman.BombmanDAO;
import ng.games.bombman.Settings;

/**
 * Nokia series 40 specific.
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
  private static final Sound titleSound =
      new Sound( parseOTASoundString( "024A3A400401FF244584604284D84604584284604284D84604284584604284D84604584284624D882D130C21431134C21431030C31134C31130D31134C30C30C214371350210214491810A13613895613893610A13810A15613893610A13895610A13893613895810610820C30850C44D30C44C30850C40D30850C44C30850C44D30C44C20C212458408410418458400458410418458410418428A22D21022C20C22C20C22C21041B8A48B08C49B08C08B08508C08509B08C08508B08C08509B08C08B08508C49B105A261842862069842862061862269862061A6226986224286186E26A0420923021426C2712AC27126C2142702AC27126C2712AC21427026C2712B020C214214418610A1889A618898610A1881A610A18818610A1889A618898410610A24E51445A52245846042845846245842845A420460458460458428460428458460458520428458460936C21431134C3112CC2912CC21021428C511891624DC54458428935030C2D021428C23041B624000" ),
      Sound.FORMAT_TONE );

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
  public GameEffects( BombmanDAO dao ){}

  /**
   * Starts playing title music.
   */
  public void startTitleMusic()
  {
    if( Settings.music ){
      try{
        this.titleSound.play( 0 );
      } catch( Exception e ){
        e.printStackTrace();
      }
    }
  }

  /**
   * Stops playing title music.
   */
  public void stopTitleMusic()
  {
    if( this.titleSound != null )
      this.titleSound.stop();
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
