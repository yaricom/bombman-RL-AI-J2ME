/**
 * $Id: BombmanGame.java 1015 2006-09-12 12:29:33Z yaric $
 */
package ng.games.bombman;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import ng.games.bombman.media.GameEffects;
import ng.games.bombman.screens.BombmanScreen;
import ng.mobile.game.util.Log;

/**
 * This is general game controller. It is used to maintain game timer and low
 * level events. To synchronize such an events with game execution thread. To
 * show main game menu screen with all subscreens.
 *
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class BombmanGame implements Runnable, CommandListener
{
  /** Parent midlet instance */
  private final Bombman parent;
  /** Game controller instance */
  private BombmanGameController gameController;
  /** Canvas, where game is drawing */
  public final BombmanScreen canvas;
  /** Data access object to acquire/store all game resources */
  public BombmanDAO dao;
  /** Game effects performer */
  public GameEffects effects;
  /** Text manager instance */
  public TextManager textManager;

  /** Time to sleep before next update */
  private int sleepTime;
  /** Flag to indicate that all resources already initialized to avoid double initialization on some phones */
  public boolean initialized;

  /** Repaint definitions. Holds definitions which screen areas should be repainted */
  public int repaintDefs;

  /** Constants to define total repaint flag */
  private static final int REPAINT_TOTAL = 1;
  /** Constants to define repaint generic active area */
  private static final int REPAINT_GENERIC_ACTIVE = 1 << 1;

  /** Current scroll step */
  public byte scrollStep;
  /** Current scroller position for textual info */
  public int scrollPosition;
  /** Array to hold scrolable text strings */
  public String[] scrollableText;
  /** Index of main/settings menu current selection  */
  private int menuIndex;
  /** Menu size */
  private int menuSize;
  /** Array to hold menu strings */
  public String[] menuItems;
  /** Array to hold screen images */
  public Image[] screenImages;

  /**
   * Common images
   * <ul>
   * <li> 0 - flame
   * <li> 1 - game tiles
   * <li> 2 - scroller mark
   * <li> 3 - scroller pointer
   * </ul>
   */
  public Image[] commonImages;

  /** Most recently pressed key */
  private int lastKeyPressed;
  /** Most recently released key */
  private int lastKeyReleased;
  /** Most recently invoked command */
  protected Command lastCommand;
  /** Current game time */
  public long currentTime;
  /** Total time wasted on previous game update cycle */
  private int wasteTime;
  /** Time when last scroll tick was issued */
  private long lastScrollTime;

  /** Semaphore object */
  private Object lock = new Object();

  /** Background image */
  protected Image bgImage;

  /** Maximal number of resources to load */
  private final int maxResources;
  /** Current number of loaded resources */
  public int loadedResources;

  /** Game state flag */
  public byte gameState;
  /** Undefined state */
  public static final byte STATE_UNDEFINED = 0;
  /** Show preloader state */
  public static final byte STATE_PRELOADER = 1;
  /** Load game state */
  public static final byte STATE_LOAD_GAME = 2;
  /** Show splash screen state */
  public static final byte STATE_SHOW_SPLASH = 3;
  /** Show main menu fade out screen state */
  public static final byte STATE_SHOW_MAIN_MENU = 5;
  /** Show help screen state */
  public static final byte STATE_SHOW_HELP = 7;
  /** Show about screen state */
  public static final byte STATE_SHOW_ABOUT = 8;
  /** Show new game screen state */
  public static final byte STATE_NEW_GAME = 9;
  /** Show ordinary game screen state */
  public static final byte STATE_GAME = 10;
  /** Show options screen state */
  public static final byte STATE_OPTIONS = 11;
  /** Show top scores screen state */
  public static final byte STATE_TOP_SCORES = 12;
  /** Quit game state */
  public static final byte STATE_QUIT_GAME = 13;

  /** Game state to be set next after current state processing */
  private byte nextGameState;

  /** Flag to indicate whether all resources for current screen already resolved */
  private boolean valid;

  /** Bitset to activate cheat code */
  private int cheatFlag;

  /** Flag to signal that all commands should be reinitialized */
  private boolean reInitCommands;

  // Negative commands
  /** Pause command */
  public Command pauseCommand;
  /** Accept command */
  public Command okCommand;
  /** No command */
  public Command noCommand;
  /** Instance of Exit command */
  public Command exitCommand;
  /** Instance of Back command */
  public Command backCommand;

  //positive commands
  /** Select item command */
  public Command selectCommand;
  /** Yes location command */
  public Command yesCommand;
  /** Change command */
  public Command changeCommand;
  /** Start command */
  public Command startCommand;
  /** Save command */
  public Command saveCommand;

  //
  // Logging definitions
  //
  public static final boolean logDebug = Log.enabled & false;

  /**
   * Constructs new instance.
   * @param parent the parent midlet instance.
   */
  public BombmanGame( Bombman parent )
  {
    this.parent = parent;
    this.canvas = new BombmanScreen( this );
    this.canvas.setCommandListener( this );
    this.maxResources = 7 + BombmanDAO.resToPreload;
    this.gameState = STATE_UNDEFINED;

    // show canvas
    Bombman.display.setCurrent( this.canvas );
  }

  /**
   * Method to initialize commands.
   */
  private void initCommands()
  {
    // init commands
    this.selectCommand = new Command( this.dao.getResource( StringItems.SELECT_COMMAND,
                         Settings.lang ), Command.OK, 1 );
    this.yesCommand = new Command( this.dao.getResource( StringItems.YES_COMMAND,
                      Settings.lang ), Command.OK, 1 );
    this.changeCommand = new Command( this.dao.getResource( StringItems.CHANGE_COMMAND,
                      Settings.lang ), Command.OK, 1 );
    this.startCommand = new Command( this.dao.getResource( StringItems.START_COMMAND,
                        Settings.lang ), Command.OK, 1 );
    this.saveCommand = new Command( this.dao.getResource( StringItems.SAVE_COMMAND,
                        Settings.lang ), Command.OK, 1 );

    this.pauseCommand = new Command( this.dao.getResource( StringItems.PAUSE_COMMAND,
                      Settings.lang ), Command.BACK, 2 );
    this.okCommand = new Command( this.dao.getResource( StringItems.OK_COMMAND,
                     Settings.lang ), Command.BACK, 1 );
    this.noCommand = new Command( this.dao.getResource( StringItems.NO_COMMAND,
                     Settings.lang ), Command.BACK, 2 );
    this.backCommand = new Command( this.dao.getResource( StringItems.BACK_COMMAND,
                     Settings.lang ), Command.BACK, 2 );
    this.exitCommand = new Command( this.dao.getResource( StringItems.EXIT_COMMAND,
                     Settings.lang ), Command.EXIT, 2 );
  }

  /**
   * Game runner.
   */
  public void run()
  {
    long lastTime = System.currentTimeMillis();
    // perform game initialization
    if( !initialized ){
      // init resource factory
      this.dao = new BombmanDAO();

      this.initialized = true;
      this.valid = true;
      this.gameState = STATE_PRELOADER;
      this.bgImage = this.dao.getImage( "dsp" );
      this.repaintDefs = REPAINT_TOTAL;
      canvas.repaint();
      canvas.serviceRepaints();
      try{
        Thread.sleep( 1000L );
      } catch( Exception exception ){}
      this.repaintDefs = REPAINT_TOTAL | REPAINT_GENERIC_ACTIVE;
      // prepare for splash screen drawing
      this.gameState = STATE_LOAD_GAME;
      try{
        this.bgImage = dao.getImage( "sp" );
      } catch( Exception ex ){
        ex.printStackTrace();
      }
    }

    // normal game cycle
    while( gameState != STATE_QUIT_GAME ){
      currentTime = System.currentTimeMillis();
      wasteTime = ( int ) ( currentTime - lastTime );
      lastTime = currentTime;
      if( canvas.isShown() ){
        // update game
        try{
          updateLogic();
        } catch( Exception ex ){
          ex.printStackTrace();
        }
        canvas.repaint();
        canvas.serviceRepaints();
      }

      synchronized( lock ){
        try{
          sleepTime = GameConfig.speedFactor - wasteTime;
          if( sleepTime > 0 )
            lock.wait( sleepTime );
        } catch( Exception ignore ){}
      }
      // wait for a while to give other threads a chance to fulfill its tasks
      Thread.currentThread().yield();
    }
    // quit game
    this.parent.quitApp();
  }

  /**
   * Method to load game resources.
   * @return <code>true</code> if all resources already loaded.
   */
  private boolean loadGameResources()
  {
    boolean res = false;
    if( !this.dao.preloadResources() ){
      this.loadedResources++;
      return res; // no need to process any further
    }

    int index = ++this.loadedResources - BombmanDAO.resToPreload;
    // init the rest
    switch( index ){
      case 1:
        // init text manager
        this.textManager = new TextManager( this.dao );
        break;

      case 2:
        // load settings
        Settings.loadSettings();
        break;

      case 3:
        // init game effects
        this.effects = new GameEffects( this.dao );
        break;

      case 4:
        // init screen
        this.canvas.init();
        // init commands
        this.initCommands();
        break;

      case 5:
        // init game controller
        this.gameController = new BombmanGameController( this );
        break;

      case 6:
        // prepare 'Press any key' flashing
        textManager.setFont( TextManager.FONT_TEXT | TextManager.FONT_TYPE_WHITE );
        String text = this.dao.getResource( StringItems.S_ITEM_PRESS_ANY_KEY,
                      Settings.lang );
        int w = textManager.stringWidth( text ) + 1;
        int h = textManager.charHeight + 2;
        this.screenImages = new Image[ 2 ];
        this.screenImages[ 0 ] = textManager.textToImage( text,
                               GameConfig.pressAnyKeyFadeOutColor, w, h, 1 );
        textManager.setFont( TextManager.FONT_TEXT | TextManager.FONT_TYPE_BLACK );
        this.screenImages[ 1 ] = textManager.textToImage( text,
                               GameConfig.pressAnyKeyFadeInColor, w, h, 1 );

        break;

      case 7:
        // load common images
        this.commonImages = this.dao.getCommonImages();
        break;

      default:
        res = true;
        this.dao.cleanIntermediate();// release all holded resources
    }
    return res;
  }

  /**
   * Method to update game logic.
   */
  private final void updateLogic()
  {
    // process user actions
    this.processUserActions();

    switch( gameState ){
      case STATE_UNDEFINED:
        this.gameState = this.nextGameState;
        break;

      case STATE_LOAD_GAME:
        // load all general game resources
        if( this.loadGameResources() ){
          this.gameState = STATE_SHOW_SPLASH;
          this.repaintDefs |= REPAINT_TOTAL;
        }
        break;

      case STATE_SHOW_MAIN_MENU:
        if( !valid ){
          // create background image
          this.createMenuBackground( false );
          // preload images
          this.screenImages = new Image[ 1 ];
          this.screenImages[ 0 ] = this.dao.getMenuCaptions( Settings.lang );
          // populate menu options
          int offset = 0;
          if( Settings.hasAnyProgress() )
            this.menuSize = 6;
          else{
            this.menuSize = 5;
            offset = 1;
          }
          this.menuItems = new String[ this.menuSize ];
          for( int i = 0; i < this.menuSize; i++ ){
            this.menuItems[ i ] = this.dao.getResource( i + offset,
                                  Settings.lang );
          }
          // start title music
          this.effects.startTitleMusic();

          // check whether commands should be reinitialized before adding
          if( !GameConfig.fullScreen && reInitCommands )
            this.initCommands();
          // add menu commands
          this.addMenuCommands();
          this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
          this.valid = true;
        }
        break;

      case STATE_SHOW_HELP:
      case STATE_SHOW_ABOUT:
        if( !valid ){
          // create background image
          this.createMenuBackground( true );
          // prepare text
          this.scrollableText = this.textManager.breakIntoLines(
                                this.gameState == STATE_SHOW_HELP ?
                                dao.getHelpMessage( Settings.lang ) :
                                dao.getAboutMessage( Settings.lang ),
                                GameConfig.mainMenuTextAreaWidth,
                                TextManager.FONT_TEXT | TextManager.FONT_TYPE_BLACK );
          // add menu commands
          this.addMenuCommands();
          this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
          this.valid = true;
        }
        break;

      case STATE_TOP_SCORES:
        if( !valid ){
          // create background image
          this.createMenuBackground( true );
          // prepare text
          this.scrollableText = new String[ Settings.names.length ];
          String end = null;
          for( int i = 0; i < this.scrollableText.length; i++ ){
            end = String.valueOf( Settings.results[ i ] );
            this.scrollableText[ i ] = this.textManager.insertChars(
                                  Settings.names[ i ].trim(), end,
                                  GameConfig.mainMenuTextAreaWidth,
                                  TextManager.FONT_TEXT | TextManager.FONT_TYPE_BLACK,
                                  '.' );
          }
          // add menu commands
          this.addMenuCommands();
          this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
          this.valid = true;
        }
        break;

      case STATE_OPTIONS:
        if( !valid ){
          // create background image
          this.createMenuBackground( true );
          this.screenImages = this.dao.getOptionsMenuImages();
          // create menu options
          this.populateOptionsMenu();

          // add menu commands
          this.addMenuCommands();
          this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
          this.valid = true;
        }
        break;

      case STATE_NEW_GAME:
        if( !valid ){
          if( Settings.hasAnyProgress() ){
            // create background image
            this.createMenuBackground( true );
            this.scrollableText = textManager.breakIntoLines(
                                  this.dao.getResource( StringItems.
                                  S_ITEM_NEW_GAME_PROMPT, Settings.lang ),
                                  GameConfig.mainMenuTextAreaWidth,
                                  TextManager.FONT_TEXT | TextManager.FONT_TYPE_BLACK );
            // add menu commands
            this.addMenuCommands();
            this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
            this.valid = true;
          }
          else
            this.requestGameScreen();
        }
        break;

      case STATE_GAME:
        if( !valid ){
          // remove menu background
          this.bgImage = null;
          // stop title music
          this.effects.stopTitleMusic();
          this.gameController.start();
          this.valid = true;
        }
        else
          this.gameController.update( this.wasteTime );
        break;
    }
  }

  /**
   * Populates options menu with captions.
   */
  private final void populateOptionsMenu()
  {
    this.menuSize = GameEffects.hasSound ? 3 : 2;
    this.menuItems = new String[ this.menuSize ];
    for( int i = 0; i < this.menuSize; i++ )
      this.menuItems[ i ] = this.dao.getResource(
                            StringItems.LANGUAGE + i, Settings.lang );
  }

  /**
   * Method to process user actions in the same thread as game running thread.
   */
  private final void processUserActions()
  {
    //
    // Process key presses
    //
    if( this.lastKeyPressed != 0 ){
      int action = canvas.getGameAction( this.lastKeyPressed );
      switch( this.gameState ){
        case STATE_SHOW_SPLASH:
          this.requestMainMenu();
          break;

        case STATE_SHOW_MAIN_MENU:
          // activate cheat mode if appropriate
          this.processCheatCode( this.lastKeyPressed );
        case STATE_OPTIONS:
          int index = this.menuIndex;
          if( action == BombmanScreen.UP ){
            if( index == 0 )
              index = this.menuSize - 1;
            else
              index--;
            this.setMenuIndex( index );
          }
          else if( action == BombmanScreen.DOWN ){
            index = ++index % this.menuSize;
            this.setMenuIndex( index );
          }
          else if( action == BombmanScreen.FIRE )
            this.lastCommand = this.getCurrentPositiveCommand();
          break;

        case STATE_SHOW_HELP:
        case STATE_SHOW_ABOUT:
        case STATE_TOP_SCORES:
          if( action == BombmanScreen.UP )
            this.scrollStep = (byte)-1;
          else if( action == BombmanScreen.DOWN )
            this.scrollStep = 1;
          this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
          break;

        case STATE_GAME:
          this.gameController.actionKeyPressed( action );
          if( action == BombmanScreen.FIRE )
            this.lastCommand = this.gameController.getCurrentPositiveCommand();
          break;
      }
      // reset key
      this.lastKeyPressed = 0;
    }

    //
    // Process key releases
    //
    if( this.lastKeyReleased != 0 ){
      switch( this.gameState ){
        case STATE_GAME:
          this.gameController.actionKeyReleased(
              canvas.getGameAction( lastKeyReleased ) );
          break;

        case STATE_SHOW_HELP:
        case STATE_SHOW_ABOUT:
        case STATE_TOP_SCORES:
          this.scrollStep = 0;
          if( ( this.repaintDefs & REPAINT_GENERIC_ACTIVE ) != 0 )
            this.repaintDefs ^= REPAINT_GENERIC_ACTIVE;
          break;
      }

      // reset key
      this.lastKeyReleased = 0;
    }

    //
    // Process command actions
    //
    if( this.lastCommand != null ){
      if( this.lastCommand == this.exitCommand ){
        this.exitGame();
      }
      else if( this.lastCommand == this.selectCommand ){
        if( this.gameState == STATE_SHOW_MAIN_MENU ){
          String item = this.menuItems[ this.menuIndex ];
          if( item.equals( this.dao.getResource( StringItems.CONTINUE, Settings.lang ) ) )
            this.requestGameScreen();
          else if( item.equals( this.dao.getResource( StringItems.NEW_GAME, Settings.lang ) ) )
            this.requestNewGame();
          else if( item.equals( this.dao.getResource( StringItems.TOP_SCORES, Settings.lang ) ) )
            this.requestTopScoresScreen();
          else if( item.equals( this.dao.getResource( StringItems.OPTIONS, Settings.lang ) ) )
            this.requestOptionsScreen();
          else if( item.equals( this.dao.getResource( StringItems.HELP, Settings.lang ) ) )
            this.requestHelpScreen();
          else if( item.equals( this.dao.getResource( StringItems.ABOUT, Settings.lang ) ) )
            this.requestAboutScreen();
        }
        else if( this.gameState == STATE_GAME )
          this.gameController.positiveCommand();
      }
      else if( this.lastCommand == this.backCommand ){
        if( this.gameState == STATE_GAME )
          this.gameController.negativeCommand();
        else
          this.requestMainMenu();
      }
      else if( this.lastCommand == this.okCommand ){
        // store settings
        Settings.storeSettings();
        this.requestMainMenu();
        this.reInitCommands = true;
      }
      else if( this.lastCommand == this.changeCommand ){
        String item = this.menuItems[ this.menuIndex ];
        if( item.equals( this.dao.getResource( StringItems.LANGUAGE,
            Settings.lang ) ) ){
          Settings.lang = ( Settings.lang + 1 ) % GameConfig.maxSupportedLangCount;
          this.populateOptionsMenu();
        }
        else if( item.equals( this.dao.getResource( StringItems.MUSIC,
            Settings.lang ) ) ){
          Settings.music = !Settings.music;
          if( Settings.music )
            this.effects.startTitleMusic();
          else
            this.effects.stopTitleMusic();
        }
        else if( item.equals( this.dao.getResource( StringItems.SOUND,
            Settings.lang ) ) ){
          Settings.sound = !Settings.sound;
        }

        // mark to repaint
        this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
      }
      else if( this.lastCommand == this.yesCommand ){
        if( this.gameState == STATE_NEW_GAME ){
          Settings.reset();
          Settings.storeSettings();
          this.requestGameScreen();
        }
        else if( this.gameState == STATE_GAME )
          this.gameController.positiveCommand();
      }
      else if( this.lastCommand == this.noCommand ){
        if( this.gameState == STATE_NEW_GAME )
          this.requestMainMenu();
        else if( this.gameState == STATE_GAME )
          this.gameController.negativeCommand();
      }
      else if( this.lastCommand == this.startCommand ||
          this.lastCommand == this.saveCommand ){
        this.gameController.positiveCommand();
      }
      else if( this.lastCommand == this.pauseCommand ){
        this.gameController.negativeCommand();
      }

      // reset, so this command will not be processed twice
      this.lastCommand = null;
      // no need to process key events
      return;
    }
  }

  /**
   * Try to process cheat codes and sets desired level to start if appropriate.
   * @param keyCode the key code of pressed key.
   */
  private final void processCheatCode( int keyCode )
  {
    int mask = Canvas.KEY_STAR * 3;
    if( keyCode == Canvas.KEY_STAR )
      this.cheatFlag += keyCode;
    else if( this.cheatFlag == mask ){
      switch( keyCode ){
        case Canvas.KEY_NUM1:
          Settings.level = 0;
          break;
        case Canvas.KEY_NUM2:
          Settings.level = 1;
          break;
        case Canvas.KEY_NUM3:
          Settings.level = 2;
          break;
        case Canvas.KEY_NUM4:
          Settings.level = 3;
          break;
        case Canvas.KEY_NUM5:
          Settings.level = 4;
          break;
        case Canvas.KEY_NUM6:
          Settings.level = 5;
          break;
        case Canvas.KEY_NUM7:
          Settings.level = 6;
          break;
        case Canvas.KEY_NUM8:
          Settings.level = 7;
          break;
        case Canvas.KEY_NUM9:
          Settings.level = 8;
          break;
        case Canvas.KEY_NUM0:
          Settings.level = 9;
          break;
      }
      this.cheatFlag = 0;
      this.requestMainMenu();
    }
  }

  /**
   * Draws loading caption.
   * @param g The Graphics context.
   */
  public final void drawLoading( Graphics g )
  {
    this.drawClippedBackground( 0, 0, GameConfig.screenWidth,
        GameConfig.screenHeight, g );
    this.textManager.setFont( TextManager.FONT_TEXT | TextManager.FONT_TYPE_BLACK );
    String text = this.dao.getResource( StringItems.S_ITEM_LOADING, Settings.lang );
    int w = this.textManager.stringWidth( text );
    int x = GameConfig.screenWidth - w - 2;
    int y = GameConfig.screenHeight - this.textManager.charHeight - 2;
    // fill background
    g.setColor( GameConfig.loadingBackgroundColor );
    g.fillRect( x - 2, y - 2, w + 3, this.textManager.charHeight + 3 );
    this.textManager.drawText( text, x, y, true, g );
  }

  /**
   * Method to render current game.
   * @param g The Graphics context.
   */
  public void paint( Graphics g )
  {
    if( !this.valid ){
      if( gameState != STATE_UNDEFINED && gameState != STATE_GAME )
        this.drawLoading( g );// to avoid trying to show caption before preloader or level loading
    }
    else{
      // draw appropriate item in accordance with current state
      this.drawPassiveScreenItems( g );
      switch( gameState ){

        case STATE_LOAD_GAME:
          this.drawProgressBar( GameConfig.loadProgressBarX,
              GameConfig.loadProgressBarY, GameConfig.loadProgressBarW,
              GameConfig.loadProgressBarH, this.loadedResources,
              this.maxResources, GameConfig.progressBarColor, g );
          break;

        case STATE_SHOW_SPLASH:
          // flashing caption
          g.drawImage( this.screenImages[ currentTime % 800L < 400L ? 0 : 1 ],
              GameConfig.screenWidth / 2, GameConfig.pressAnyKeyOffset,
              Graphics.BOTTOM | Graphics.HCENTER );
          break;

        case STATE_SHOW_MAIN_MENU:
          this.drawMainMenu( g );
          break;

        case STATE_SHOW_HELP:
        case STATE_SHOW_ABOUT:
        case STATE_NEW_GAME:
        case STATE_TOP_SCORES:
          if( currentTime - lastScrollTime >= GameConfig.scrollingDelay &&
              ( ( this.repaintDefs & REPAINT_GENERIC_ACTIVE ) != 0 ||
              ( this.repaintDefs & REPAINT_TOTAL ) != 0 ) ){
            this.lastScrollTime = this.currentTime;
            this.drawClippedBackground( GameConfig.mainMenuAreaX,
                GameConfig.mainMenuAreaY, GameConfig.mainMenuAreaWidth,
                GameConfig.mainMenuAreaHeight, g );
            int h = GameConfig.mainMenuTextLinesCount * textManager.charHeight;
            this.scrollPosition += this.scrollStep * textManager.charHeight;
            int textHeight = scrollableText.length * textManager.charHeight;
            // draw scrollable text
            this.scrollPosition = this.drawFixedScrollableText( scrollableText,
                                  GameConfig.mainMenuTextAreaX,
                                  GameConfig.mainMenuTextAreaY,
                                  GameConfig.mainMenuTextAreaWidth, h,
                                  GameConfig.scrollTextAligment, g,
                                  this.scrollPosition, textManager.charHeight,
                                  textHeight );
            // draw scroll bar
            this.drawScrollBar( this.scrollPosition,
                textHeight - h, GameConfig.mainMenuScrollMarkX,
                GameConfig.mainMenuScrollBarY,
                GameConfig.mainMenuScrollBarHeight, g );
          }
          break;

        case STATE_OPTIONS:
          this.drawOptionsMenu( g );
          break;

        case STATE_GAME:
          this.gameController.paint( g );
          break;
      }
      // reset total repaint flag
      if( ( this.repaintDefs & REPAINT_TOTAL ) != 0 )
        this.repaintDefs ^= REPAINT_TOTAL;
    }
  }

  /**
   * Method to draw passive game screen items
   * @param g the graphics context to draw on.
   */
  private final void drawPassiveScreenItems( Graphics g )
  {
    if( ( this.repaintDefs & REPAINT_TOTAL ) != 0 ){
      // check state
      if( this.gameState == STATE_PRELOADER ){
        g.setColor( 0xFFFFFF );
        g.fillRect( 0, 0, GameConfig.screenWidth, GameConfig.screenHeight );
        if( this.bgImage != null )
          g.drawImage( this.bgImage,
              ( GameConfig.screenWidth - bgImage.getWidth() ) / 2,
              GameConfig.screenHeight / 2, Graphics.VCENTER | Graphics.LEFT );
      }
      else if( this.gameState != STATE_GAME ){
        // draw screen background
        this.drawClippedBackground( 0, 0, GameConfig.screenWidth,
            GameConfig.screenHeight, g );
      }
    }
  }

  /**
   * Method to draw clipped screen background.
   * @param x the left X coordinate for background area to repaint.
   * @param y the top Y coordinate for background area to repaint.
   * @param w the width of background area to repaint.
   * @param h the height of background area to repaint.
   * @param g the graphics context to draw on.
   */
  public void drawClippedBackground( int x, int y, int w, int h, Graphics g )
  {
    g.setClip( x, y, w, h );
    if( this.bgImage != null )
      g.drawImage( this.bgImage, 0, 0, Graphics.TOP | Graphics.LEFT );
    else{
      g.setColor( GameConfig.gameBackgroundColor );
      g.fillRect( 0, 0, GameConfig.screenWidth, GameConfig.screenHeight );
    }
  }

  /**
   * Method to draw options menu.
   * @param g g the graphics context to draw on.
   */
  private final void drawOptionsMenu( Graphics g )
  {
    if( ( this.repaintDefs & REPAINT_GENERIC_ACTIVE ) != 0 ||
        ( this.repaintDefs & REPAINT_TOTAL ) != 0 ){
      // erase previous
      this.drawClippedBackground( GameConfig.mainMenuAreaX,
                GameConfig.mainMenuAreaY, GameConfig.mainMenuAreaWidth,
                GameConfig.mainMenuAreaHeight, g );

      int y = GameConfig.settMenuOptionY;
      this.textManager.setFont( TextManager.FONT_TEXT | TextManager.FONT_TYPE_BLACK );
      // draw pointer
      if( this.menuIndex > 0 )
        y += GameConfig.settMenuOptionHeight * this.menuIndex +
            GameConfig.settMenuInterval * this.menuIndex;
      g.setColor( GameConfig.settMenuPointerBackground );
      g.fillRect( GameConfig.settMenuOptionX, y,
          GameConfig.settMenuOptionWidth, GameConfig.settMenuOptionHeight );
      g.setColor( GameConfig.settMenuPointerForeground );
      g.drawRect( GameConfig.settMenuOptionX, y,
          GameConfig.settMenuOptionWidth - 1, GameConfig.settMenuOptionHeight - 1 );
      // draw language
      y = GameConfig.settMenuOptionY;
      g.drawImage( this.screenImages[ 3 ], GameConfig.settMenuIconX,
          y + GameConfig.settMenuIconYOffset, Graphics.TOP | Graphics.LEFT );
      this.textManager.drawText( this.dao.getResource( StringItems.LANGUAGE,
          Settings.lang), GameConfig.settMenuTextX,
          y + GameConfig.settMenuTextYOffset, true, g );
      // draw music
      y += GameConfig.settMenuOptionHeight + GameConfig.settMenuInterval;
      g.drawImage( this.screenImages[ 4 ], GameConfig.settMenuIconX,
          y + GameConfig.settMenuIconYOffset, Graphics.TOP | Graphics.LEFT );
      g.drawImage( this.screenImages[ Settings.music ? 2 : 1 ],
          GameConfig.settMenuCheckboxX, y + GameConfig.settMenuCheckboxYOffset,
          Graphics.TOP | Graphics.RIGHT );
      this.textManager.drawText( this.dao.getResource( StringItems.MUSIC,
          Settings.lang), GameConfig.settMenuTextX,
          y + GameConfig.settMenuTextYOffset, true, g );
      // draw sound
      if( GameEffects.hasSound ){
        y += GameConfig.settMenuOptionHeight + GameConfig.settMenuInterval;
        g.drawImage( this.screenImages[ 5 ], GameConfig.settMenuIconX,
            y + GameConfig.settMenuIconYOffset, Graphics.TOP | Graphics.LEFT );
        g.drawImage( this.screenImages[ Settings.sound ? 2 : 1 ],
            GameConfig.settMenuCheckboxX, y + GameConfig.settMenuCheckboxYOffset,
            Graphics.TOP | Graphics.RIGHT );
        this.textManager.drawText( this.dao.getResource( StringItems.SOUND,
            Settings.lang), GameConfig.settMenuTextX,
            y + GameConfig.settMenuTextYOffset, true, g );
      }
      // draw language mark
      y = GameConfig.settMenuOptionY +
          ( GameConfig.settMenuOptionHeight - GameConfig.settMenuFlagIconHeight ) / 2;
      g.setClip( GameConfig.settMenuFlagIconX - GameConfig.settMenuFlagIconWidth, y,
          GameConfig.settMenuFlagIconWidth, GameConfig.settMenuOptionHeight );
      g.drawImage( this.screenImages[ 0 ],
          GameConfig.settMenuFlagIconX -
          ( Settings.lang + 1 ) * GameConfig.settMenuFlagIconWidth,
          y, Graphics.TOP | Graphics.LEFT );

      if( ( this.repaintDefs & REPAINT_GENERIC_ACTIVE ) != 0 )
        this.repaintDefs ^= REPAINT_GENERIC_ACTIVE;
    }
  }

  /**
   * Method to draw main menu.
   * @param g the graphics context to draw on.
   */
  private final void drawMainMenu( Graphics g )
  {
    if( ( this.repaintDefs & REPAINT_GENERIC_ACTIVE ) != 0 ||
        ( this.repaintDefs & REPAINT_TOTAL ) != 0 ){

      if( GameConfig.mainMenuOptionDrawCount == 6 ){
        // draw fixed menu
        // clear only pointer background
        this.drawClippedBackground( GameConfig.mainMenuPointerX,
            GameConfig.mainMenuOptionY, GameConfig.tileWidth,
            GameConfig.mainMenuPointerAreaHeight, g );
      }
      else{
        // draw scrollable menu
        // clear all menu area
        this.drawClippedBackground( 0, GameConfig.mainMenuOptionY,
            GameConfig.screenWidth, GameConfig.mainMenuPointerAreaHeight, g );
        // draw scroll bar if appropriate
        this.drawScrollBar( this.menuIndex, this.menuSize - 1,
            GameConfig.screenWidth - GameConfig.scrollMarkWidth,
            GameConfig.mainMenuOptionY, GameConfig.mainMenuPointerAreaHeight, g );
      }

      // draw menu
      int y = GameConfig.mainMenuOptionY;
      int w = this.screenImages[ 0 ].getWidth();
      int offset = this.menuSize == 6 ? 0 : 1;
      int start = this.menuIndex > GameConfig.mainMenuOptionDrawCount - 1 ?
                  this.menuIndex - GameConfig.mainMenuOptionDrawCount + 1 : 0;
      int finish = start + GameConfig.mainMenuOptionDrawCount;
      if( finish >= this.menuSize )
        finish = this.menuSize;

      for( int i = start; i < finish; i++ ){
        g.setClip( GameConfig.mainMenuOptionX, y, w,
            GameConfig.mainMenuOptionHeight );
        g.drawImage( this.screenImages[ 0 ], GameConfig.mainMenuOptionX,
            y - GameConfig.mainMenuOptionHeight * ( i + offset ),
            Graphics.TOP | Graphics.LEFT );
        // draw pointer
        if( this.menuIndex == i ){
          g.setClip( GameConfig.mainMenuPointerX, y, GameConfig.tileWidth,
              GameConfig.tileHeight );
          g.drawImage( this.commonImages[ Images.TILES ],
              GameConfig.mainMenuPointerX - GameConfig.tileWidth * 7, y,
              Graphics.TOP | Graphics.LEFT );
        }
        y += GameConfig.mainMenuOptionHeight + GameConfig.mainMenuInterval;
      }

      if( ( this.repaintDefs & REPAINT_GENERIC_ACTIVE ) != 0 )
        this.repaintDefs ^= REPAINT_GENERIC_ACTIVE;
    }
  }

  /**
   * Method to draw screen fill.
   * @param fill the fill image.
   * @param g the graphics context to draw on.
   */
  public final void drawScreenFill( Image fill, Graphics g )
  {
    int w = fill.getWidth();
    int h = fill.getHeight();
    int col = GameConfig.screenWidth / w;
    int rows = GameConfig.screenHeight / h;
    int x, y = GameConfig.screenHeight;
    // fill background
    for( int i = rows, j; i >= 0; i-- ){
      x = GameConfig.screenWidth;
      for( j = col; j >= 0; j-- ){
        g.drawImage( fill, x, y, Graphics.BOTTOM | Graphics.RIGHT );
        x -= w;
      }
      y -= h;
    }
  }

  /**
   * Method to create menu background image.
   * @param rect if <code>true</code> than menu rectangle will drawn
   */
  private final void createMenuBackground( boolean rect )
  {
    this.bgImage = Image.createImage( GameConfig.screenWidth, GameConfig.screenHeight );
    Graphics g = this.bgImage.getGraphics();
    Image[]tmp = this.dao.getMenuImages();
    // fill background
    this.drawScreenFill( tmp[ 0 ], g );
    // draw title
    g.drawImage( tmp[ 1 ], GameConfig.menuTitleLeftX, GameConfig.menuTitleTopY,
       Graphics.TOP | Graphics.LEFT );
    tmp = null;

    // draw decor only when appropriate
    if( GameConfig.mainMenuFlameHorizCount != 0 &&
        GameConfig.mainMenuFlameVertCount != 0 ){
      Image flame = this.commonImages[ Images.FLAME ];
      // draw cross
      g.setClip( GameConfig.mainMenuCrossX, GameConfig.mainMenuCrossY,
          GameConfig.tileWidth, GameConfig.tileHeight );
      int x = GameConfig.mainMenuCrossX - GameConfig.tileWidth *
              ( GameConfig.flameCenter + GameConfig.mainMenuCrossIndex );
      g.drawImage( flame, x, GameConfig.mainMenuCrossY,
          Graphics.TOP | Graphics.LEFT );
      // draw up
      int y = GameConfig.mainMenuCrossY - GameConfig.tileHeight;
      g.setClip( GameConfig.mainMenuCrossX, y, GameConfig.tileWidth,
          GameConfig.tileHeight );
      x = GameConfig.mainMenuCrossX - GameConfig.tileWidth *
          ( GameConfig.flameUp + GameConfig.mainMenuCrossIndex );
      g.drawImage( flame, x, y, Graphics.TOP | Graphics.LEFT );
      // draw left
      x = GameConfig.mainMenuCrossX - GameConfig.tileWidth;
      g.setClip( x, GameConfig.mainMenuCrossY, GameConfig.tileWidth,
          GameConfig.tileHeight );
      x -= GameConfig.tileWidth * ( GameConfig.flameLeft +
          GameConfig.mainMenuCrossIndex );
      g.drawImage( flame, x, GameConfig.mainMenuCrossY,
          Graphics.TOP | Graphics.LEFT );
      // draw right
      x = GameConfig.mainMenuCrossX + GameConfig.tileWidth;
      int i, j;
      for( i = 0; i < GameConfig.mainMenuFlameHorizCount; i++ ){
        g.setClip( x, GameConfig.mainMenuCrossY, GameConfig.tileWidth,
            GameConfig.tileHeight );
        j = i == GameConfig.mainMenuFlameHorizCount - 1 ?
            GameConfig.flameRight : GameConfig.flameHorizontal;
        g.drawImage( flame,
            x - GameConfig.tileWidth * ( j + GameConfig.mainMenuCrossIndex ),
            GameConfig.mainMenuCrossY, Graphics.TOP | Graphics.LEFT );
        x += GameConfig.tileWidth;
      }
      // draw bottom
      y = GameConfig.mainMenuCrossY + GameConfig.tileHeight;
      for( i = 0; i < GameConfig.mainMenuFlameVertCount; i++ ){
        g.setClip( GameConfig.mainMenuCrossX, y, GameConfig.tileWidth,
            GameConfig.tileHeight );
        j = i == GameConfig.mainMenuFlameVertCount - 1 ?
            GameConfig.flameDown : GameConfig.flameVertical;
        g.drawImage( flame, GameConfig.mainMenuCrossX -
            GameConfig.tileWidth * ( j + GameConfig.mainMenuCrossIndex ),
            y, Graphics.TOP | Graphics.LEFT );
        y += GameConfig.tileHeight;
      }
    }

    // draw menu rectangle if appropriate
    if( rect ){
      g.setClip( 0, 0, GameConfig.screenWidth, GameConfig.screenHeight );
      g.setColor( GameConfig.mainMenuBackground );
      g.fillRect( GameConfig.mainMenuAreaX, GameConfig.mainMenuAreaY,
          GameConfig.mainMenuAreaWidth, GameConfig.mainMenuAreaHeight );
      g.setColor( GameConfig.mainMenuForeground );
      g.drawRect( GameConfig.mainMenuAreaX, GameConfig.mainMenuAreaY,
          GameConfig.mainMenuAreaWidth - 1, GameConfig.mainMenuAreaHeight - 1 );
    }
  }


  /**
   * Method to draw progress bar.
   * @param x the coordinate of top left corner.
   * @param y the coordinate of top left corner.
   * @param width the width of progress bar.
   * @param height the height of progress bar.
   * @param value current value.
   * @param maxValue maximal value.
   * @param color the progress bar color.
   * @param g the graphics context to draw on.
   */
  public void drawProgressBar( int x, int y, int width, int height, int value,
      int maxValue, int color, Graphics g )
  {
    g.setClip( x, y, width, height );
    g.clipRect( x, y, width, height );
    g.setColor( color );
    g.drawRect( x, y, width, height );
    if( value > 0 )
      g.fillRect( x, y, ( value * width ) / maxValue, height );
  }

  /**
   * Method to draw fixed scrollable text in sub menu area.
   * @param text the text array with scrollable text to draw.
   * @param x the left X coordinate for text area.
   * @param y the top Y coordinate of text area.
   * @param w the width of text area.
   * @param h the height of text area.
   * @param aligment the constant to specify aligment for text item drawing
   * within specified area ( Graphics.LEFT, Graphics.RIGHT, Graphics.HCENTER ).
   * @param g The Graphics context to draw on.
   * @param position the current scroll position.
   * @param step the scroll step
   * @param textHeight the height of text block to draw.
   * @return scroll position.
   */
  public final int drawFixedScrollableText( String[] text, int x, int y, int w,
      int h, int aligment, Graphics g, int position, int step, int textHeight )
  {
    int clipX = g.getClipX();
    int clipY = g.getClipY();
    int clipWidth = g.getClipWidth();
    int clipHeight = g.getClipHeight();

    if( position < 0 || textHeight <= h )
      position = 0;
    else if( textHeight - position < h )
      position -= step;

    int lineX = x;
    int lineY = y - position;
    if( logDebug ){
      Log.log( "SCROLLABLE TEXT AREA", "width: " + w );
      g.setColor( 0xFF0000 );
      g.drawRect( x, y, w - 1, h - 1 );
    }
    // clip text area
    g.setClip( x - 1, y, w + 2, h );
    for( int i = 0; i < text.length; i++ ){
      if( aligment == Graphics.RIGHT )
        lineX = w - textManager.stringWidth( text[ i ] );
      else if( aligment == Graphics.HCENTER )
        lineX = x + ( w - textManager.stringWidth( text[ i ] ) ) / 2;
      if( lineY >= y && lineY <= y + h )
        textManager.drawText( text[ i ], lineX, lineY, true, g );
      lineY += textManager.charHeight;
    }
    // restore clip
    g.setClip( clipX, clipY, clipWidth, clipHeight );
    return position;
  }

  /**
   * Method to draw scroll bar.
   * @param position the current position of text pointer.
   * @param div the total height of text without screen height.
   * @param x the center X coordinate for scroll bar drawing.
   * @param y the top Y coordinate for scroll bar drawing.
   * @param h the height of scroll bar area.
   * @param g the graphics context to draw on.
   */
  public final void drawScrollBar( int position, int div, int x, int y,
      int h, Graphics g )
  {
    int scrollPointerOffset = GameConfig.scrollMarkHeight;
    int bottomLimit = y + h - scrollPointerOffset;

    // draw scroll up mark
    g.drawImage( this.commonImages[ Images.SCROLL_MARK ], x, y,
        Graphics.HCENTER | Graphics.TOP );
    // draw scroll down mark
    g.drawImage( this.commonImages[ Images.SCROLL_MARK ], x, bottomLimit,
        Graphics.HCENTER | Graphics.TOP );
    g.setColor( GameConfig.scrollBarColor );
    g.drawLine( x, y + GameConfig.scrollMarkHeight, x, bottomLimit );

    // draw pointer if appropriate
    if( div > 0 ){
      // calculate height of pointer move area
      h -= scrollPointerOffset + scrollPointerOffset + GameConfig.scrollPointerHeight;
      int pointerY = h * position * 1000;
      pointerY /= div;
      pointerY /= 1000;
      pointerY += y + scrollPointerOffset;
      if( pointerY + GameConfig.scrollPointerHeight >= bottomLimit )
        pointerY = bottomLimit - GameConfig.scrollPointerHeight;

      g.drawImage( this.commonImages[ Images.SCROLL_POINTER ], x, pointerY,
          Graphics.HCENTER | Graphics.TOP );
    }
  }

  /**
   * Method to request new game screen
   */
  private final void requestNewGame()
  {
    this.nextGameState = STATE_NEW_GAME;
    this.invalidate();
  }

  /**
   * Method to request game screen
   */
  private final void requestGameScreen()
  {
    this.nextGameState = STATE_GAME;
    this.invalidate();
  }

  /**
   * Method to request main menu.
   */
  public final void requestMainMenu()
  {
    this.nextGameState = STATE_SHOW_MAIN_MENU;
    this.invalidate();
  }

  /**
   * Method to request top scores screen.
   */
  public final void requestTopScoresScreen()
  {
    this.nextGameState = STATE_TOP_SCORES;
    this.invalidate();
  }

  /**
   * Method to request options screen.
   */
  private final void requestOptionsScreen()
  {
    this.nextGameState = STATE_OPTIONS;
    this.invalidate();
  }

  /**
   * Method to request about screen.
   */
  private final void requestAboutScreen()
  {
    this.nextGameState = STATE_SHOW_ABOUT;
    this.invalidate();
  }

  /**
   * Method to request help screen.
   */
  private final void requestHelpScreen()
  {
    this.nextGameState = STATE_SHOW_HELP;
    this.invalidate();
  }


  /**
   * Method to set new menu index.
   * @param index the new menu index.
   */
  private final void setMenuIndex( int index )
  {
    // reset currently selected
    this.menuIndex = index;
    // mark to repaint
    this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
  }

  /**
   * Method to release menu resources.
   */
  private final void invalidate()
  {
    this.gameState = STATE_UNDEFINED;

    this.scrollableText = null;
    this.scrollPosition = 0;
    this.scrollStep = 0;
    this.menuIndex = 0;
    this.screenImages = null;
    this.menuItems = null;
    this.valid = false;
    this.cheatFlag = 0;

    this.lastScrollTime = 0;

    // set repaint flag
    this.repaintDefs = REPAINT_TOTAL;

    // remove previously added commands
    this.removeAllCommands();
  }

  /**
   * Indicates that a command event has occurred on Displayable d.
   * @param c a Command object identifying the command
   * @param d the Displayable on which this event has occurred
   */
  public final void commandAction( Command c, Displayable d )
  {
    this.lastCommand = c;
    synchronized( lock ){
      lock.notifyAll(); // notify all waiting threads
    }
  }

  /**
   * Called when a key is pressed.
   * @param keyCode The key code of the key that was pressed
   */
  public final void keyPressed( int keyCode )
  {
    this.lastKeyPressed = keyCode;
    synchronized( lock ){
      lock.notifyAll();
    }
  }

  /**
   * Called when a key is released.
   * @param keyCode The key code of the key that was released
   */
  public final void keyReleased( int keyCode )
  {
    lastKeyReleased = keyCode;
    synchronized( lock ){
      lock.notifyAll();
    }
  }

  /**
   * Invoked to pause game from pauseApp midlet method.
   */
  public void pauseApp()
  {
    this.hideNotify();
  }

  /**
   * Invoked shortly after the Canvas has been removed from the display.
   */
  public final void hideNotify()
  {
    // mark to repaint
    this.repaintDefs |= REPAINT_TOTAL;
    if( this.gameState == STATE_GAME )
      this.gameController.hideNotify();
  }

  /**
   * Method to destroy all resources associated with this Game.
   * This method only invoked when parent MIDlet is destroying.
   */
  public final void destroyGame()
  {
    this.effects.destroy();
  }

  /**
   * Invoked to signal that game should terminate its execution.
   */
  public final void exitGame()
  {
    this.invalidate();
    this.gameState = STATE_QUIT_GAME;
  }

  /**
   * Method to add appropriate commands in accordance with current game state.
   */
  private final void addMenuCommands()
  {
    // add positive commands
    Command tmp = this.getCurrentPositiveCommand();
    if( tmp != null )
      this.canvas.addCommand( tmp );

    // add negative commands in accordance with state
    switch( gameState ){
      case STATE_SHOW_MAIN_MENU:
        this.canvas.addCommand( this.exitCommand );
        break;

      case STATE_SHOW_ABOUT:
      case STATE_SHOW_HELP:
      case STATE_TOP_SCORES:
        this.canvas.addCommand( this.backCommand );
        break;

      case STATE_OPTIONS:
        this.canvas.addCommand( this.okCommand );
        break;

      case STATE_NEW_GAME:
        this.canvas.addCommand( this.noCommand );
        break;
    }
  }

  /**
   * Returns current positive command in accordance with menu state.
   * @return current positive command in accordance with menu state.
   */
  private final Command getCurrentPositiveCommand()
  {
    Command tmp = null;
    switch( gameState ){
      case STATE_SHOW_MAIN_MENU:
        tmp = this.selectCommand;
        break;

      case STATE_OPTIONS:
        tmp = this.changeCommand;
        break;

      case STATE_NEW_GAME:
        tmp = this.yesCommand;
        break;
    }
    return tmp;
  }

  /**
   * Method to remove all menu commands from canvas.
   */
  private final void removeAllCommands()
  {
    this.canvas.removeCommand( this.exitCommand );
    this.canvas.removeCommand( this.backCommand );
    this.canvas.removeCommand( this.okCommand );
    this.canvas.removeCommand( this.noCommand );

    this.canvas.removeCommand( this.selectCommand );
    this.canvas.removeCommand( this.changeCommand );
    this.canvas.removeCommand( this.yesCommand );
  }
}
