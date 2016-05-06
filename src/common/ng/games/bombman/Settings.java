/**
 * $Id: Settings.java 2 2005-06-30 12:47:28Z yaric $
 */
package ng.games.bombman;

import java.io.*;

import javax.microedition.rms.RecordStore;

/**
 * This class used to store, load and hold current game settings, such as
 * sound, vibration, music, user name, difficulty level and last track.
 * These settings stored as one record in the underlying recordstore.
 *
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class Settings
{
  /** Flag to indicate whether sound is enabled */
  public static boolean sound;
  /** Flag to indicate whether vibration is enabled */
  public static boolean vibration;
  /** Flag to indicate whether music is enabled */
  public static boolean music;

  /** User name */
  public static String userName;
  /** Current language */
  public static int lang;
  /** Current level */
  public static int level = 0;
  /** Current score */
  public static int score;

  /** Array with top5 names  */
  public static String[] names = new String[ GameConfig.topScoresCount ];
  /** Array with top5 results */
  public static int[] results = new int[ GameConfig.topScoresCount ];

  /**
   * Resets current progress
   */
  public static final void reset()
  {
    level = 0;
    score = 0;
  }

  /**
   * Returns <code>false</code> if no progress was saved yet.
   * @return <code>false</code> if no progress was saved yet.
   */
  public static boolean hasAnyProgress()
  {
    return level > 0;
  }

  /**
   * Method to current store settings in the recordstore.
   */
  public static final void storeSettings()
  {
    RecordStore recordstore = null;
    try{
      recordstore = RecordStore.openRecordStore( "bombman", true );
      byte record[] = getSettingsArray();
      recordstore.setRecord( 1, record, 0, record.length );
    } catch( Exception ex ){
    // nothing to do
    } finally{
      try{
        recordstore.closeRecordStore();
      } catch( Exception ex1 ){}
    }
  }

  /**
   * Checks whether specified result fits into top scores table.
   * @param score the result to check.
   * @return <code>true</code> if specified result fits into top scores table.
   */
  public static final boolean isHighScore( int score )
  {
    return score >= results[ GameConfig.topScoresCount - 1 ] && score > 0;
  }

  /**
   * Add score to the score results.
   * @param name the name of user.
   * @param score the user score.
   */
  public static final void addScore( String name, int score )
  {
    userName = name;
    if( isHighScore( score ) ){
      boolean added = false;
      for( int i = results.length - 1; i > 0; i-- ){
        if( score < results[ i - 1 ] && score >= results[ i ] ){
          results[ i ] = score;
          names[ i ] = name;
          added = true;
          break;
        }
        else{
          results[ i ] = results[ i - 1 ];
          names[ i ] = names[ i - 1 ];
        }
      }
      if( !added && score >= results[ 0 ] ){
        results[ 0 ] = score;
        names[ 0 ] = name;
      }
    }
  }

  /**
   * Method to load stored setting from recordstore.
   */
  public static final void loadSettings()
  {
    RecordStore store = null;
    DataInputStream dis = null;
    try{
      store = RecordStore.openRecordStore( "bombman", true );
      if( store.getNumRecords() == 0 ){
        // no records was stored, thus create new one
        music = true;
        sound = true;
        vibration = true;
        userName = "";

        // initialize top scores
        for( int i = 0; i < GameConfig.topScoresCount; i++ ){
          results[ i ] = 0;
          names[ i ] = "aaa";
        }
        byte[] tmp = getSettingsArray();
        store.addRecord( tmp, 0, tmp.length );
      }
      else{
        // load already stored records
        dis = new DataInputStream( new ByteArrayInputStream( store.getRecord( 1 ) ) );

        // read settings
        byte effects = dis.readByte();
        music = ( effects & 0x1 ) == 1;
        sound = ( effects & 0x2 ) == 2;
        vibration = ( effects & 0x4 ) == 4;
        userName = dis.readUTF();
        lang = dis.readInt();
        level = dis.readInt();
        score = dis.readInt();

        // read top scores
        for( int i = 0; i < GameConfig.topScoresCount; i++ ){
          names[ i ] = dis.readUTF();
          results[ i ] = dis.readInt();
        }

      }
    } catch( Exception ex ){
    // nothing to do
    } finally{
      try{
        dis.close();
      } catch( Exception ignore ){}
      try{
        store.closeRecordStore();
      } catch( Exception ignore ){}
    }
  }

  /**
   * Method to represent all game settings as array of bytes.
   * @return array of bytes with all game settings.
   * @throws Exception if any exception occurs.
   */
  private static final byte[] getSettingsArray() throws Exception
  {
    byte[] tmp = null;
    DataOutputStream daos = null;
    try{
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      daos = new DataOutputStream( baos );
      byte effects = 0;
      if( music ){
        effects |= 0x1;
      }
      if( sound ){
        effects |= 0x2;
      }
      if( vibration ){
        effects |= 0x4;
      }

      // write settings
      daos.writeByte( effects );
      daos.writeUTF( userName );
      daos.writeInt( lang );
      daos.writeInt( level );
      daos.writeInt( score );

      // write top scores
      for( int i = 0; i < GameConfig.topScoresCount; i++ ){
        daos.writeUTF( names[ i ] );
        daos.writeInt( results[ i ] );
      }
      tmp = baos.toByteArray();
    } catch( Exception ex ){
      throw ex;
    } finally{
      try{
        daos.close();
      } catch( Exception ex ){}
    }
    return tmp;
  }
}