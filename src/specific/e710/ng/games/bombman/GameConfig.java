/**
 * $Id: GameConfig.java 223 2005-07-14 15:42:03Z yaric $
 */
package ng.games.bombman;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Samsung E710 specific.
 * Game constants holder.
 *
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public interface GameConfig
{
  /** Screen width */
  public static final int screenWidth = 128;
  /** Screen height */
  public static final int screenHeight = 144;

  /** Game speed factor */
  public static final int speedFactor = 50;
  /** Text scrolling speed */
  public static final int scrollingDelay = speedFactor * 2;
  /** Levels count */
  public static final int levels = 10;

  /** Game background color */
  public static final int gameBackgroundColor = 0x8834B2;
  /** Screen bounds color */
  public static final int screenBoundsColor = 0;

  /** The X coordinate of top left corner for progress bar drawing */
  public static final int loadProgressBarX = 14;
  /** The Y coordinate of top left corner for progress bar drawing */
  public static final int loadProgressBarY = 136;
  /** Load progress bar width */
  public static final int loadProgressBarW = 100;
  /** Load progress bar height */
  public static final int loadProgressBarH = 5;
  /** Progress bar color */
  public static final int progressBarColor = 0xFFFFFF;

  /** Vertical offset for 'press any key' caption */
  public static final int pressAnyKeyOffset = 140;
  /** Fade in background color */
  public static final int pressAnyKeyFadeInColor = 0xFFD800;
  /** Fade out background color */
  public static final int pressAnyKeyFadeOutColor = 0xFF9000;

  /** Loading background color */
  public static final int loadingBackgroundColor = gameBackgroundColor;

  /** Tile height */
  public static final int tileHeight = 12;
  /** Tile width */
  public static final int tileWidth = 12;
  /** Flame segments count */
  public static final int flameSegments = 4;
  /** Flame vertical segments start index */
  public static final int flameVertical = 0;
  /** Flame horizontal segments start index */
  public static final int flameHorizontal = 1 * flameSegments;
  /** Flame up segments start index */
  public static final int flameUp = 2 * flameSegments;
  /** Flame left segments start index */
  public static final int flameLeft = 3 * flameSegments;
  /** Flame down segments start index */
  public static final int flameDown = 4 * flameSegments;
  /** Flame right segments start index */
  public static final int flameRight = 5 * flameSegments;
  /** Flame center segments start index */
  public static final int flameCenter = 6 * flameSegments;

  /** Scroll mark width */
  public static final int scrollMarkWidth = 5;
  /** Scroll mark height */
  public static final int scrollMarkHeight = 5;
  /** Scroll bar color */
  public static final int scrollBarColor = 0xB00000;
  /** Scroll pointer height including offset */
  public static final int scrollPointerHeight = 5;

  /** Number of supported languages */
  public static final int maxSupportedLangCount = 6;
  /** Scroll texts aligment */
  public static final int scrollTextAligment = Graphics.HCENTER;
  /** Top scores count */
  public static final int topScoresCount = 10;

  //
  // Main menu configurations
  //
  /** Menu title top offset */
  public static final int menuTitleTopY = 4;
  /** Menu title left offset */
  public static final int menuTitleLeftX = 19;

  /** Main menu first option left X coordinate */
  public static final int mainMenuOptionX = 30;
  /** Main menu first option top Y coordinate */
  public static final int mainMenuOptionY = 35;
  /** Main menu highlighted option height */
  public static final int mainMenuOptionHeight = 14;
  /** Main menu interoption distance */
  public static final int mainMenuInterval = 4;

  /** Number of menu options to draw simultaneously */
  public static final int mainMenuOptionDrawCount = 6;

  /** Main menu decor cross left X */
  public static final int mainMenuCrossX = 6;
  /** Main menu decor cross top Y */
  public static final int mainMenuCrossY = 21;
  /** Main menu decor cross index */
  public static final int mainMenuCrossIndex = 1;
  /** Horizontal segments count */
  public static final int mainMenuFlameHorizCount = 9;
  /** Vertical segments count */
  public static final int mainMenuFlameVertCount = 9;

  /** Main menu pointer left X coordinate */
  public static final int mainMenuPointerX = 17;
  /** ain menu pointer area height */
  public static final int mainMenuPointerAreaHeight = mainMenuOptionHeight * 6 +
      mainMenuInterval * 5;

  /** Main menu area width */
  public static final int mainMenuAreaWidth = 104;
  /** Main menu area height */
  public static final int mainMenuAreaHeight = 106;
  /** Main menu area left X coordinate */
  public static final int mainMenuAreaX = 19;
  /** Main menu area top Y coordinate */
  public static final int mainMenuAreaY = 34;
  /** Main menu background color */
  public static final int mainMenuBackground = 0xFF9000;
  /** Main menu foreground color */
  public static final int mainMenuForeground = 0xB00000;
  /** Text padding */
  public static final int mainMenuTextPadding = 2;
  /** Main menu text area (area available for text drawing) left X coordinate */
  public static final int mainMenuTextAreaX = mainMenuAreaX + mainMenuTextPadding;
  /** Main menu text area (area available for text drawing) top Y coordinate */
  public static final int mainMenuTextAreaY = mainMenuAreaY + 2;
  /** Main menu active text area (area available for text drawing) width */
  public static final int mainMenuTextAreaWidth =
      mainMenuAreaWidth - mainMenuTextPadding - mainMenuTextPadding -
      scrollMarkWidth - 2;
  /** Number of text lines to display */
  public static final int mainMenuTextLinesCount = 6;

  /** Scroll bar height in main menu area */
  public static final int mainMenuScrollBarHeight = mainMenuAreaHeight - 8;
  /** Top Y coordinate for menu scroll bar drawing */
  public static final int mainMenuScrollBarY = mainMenuAreaY + 4;
  /** Center X coordinate for scroll marks drawing in main menu area */
  public static final int mainMenuScrollMarkX = mainMenuAreaX + mainMenuAreaWidth - 7;


  /** Settings menu option left X coordinate */
  public static final int settMenuOptionX = mainMenuAreaX + 2;
  /** Settings menu option start Y coordinate */
  public static final int settMenuOptionY = mainMenuAreaY + 2;
  /** Settings menu option width */
  public static final int settMenuOptionWidth = mainMenuAreaWidth - 4;
  /** Settings menu option height */
  public static final int settMenuOptionHeight = 18;
  /** Settings menu interoption distance */
  public static final int settMenuInterval = 4;
  /** Setting menu icon left coordinate */
  public static final int settMenuIconX = settMenuOptionX + 2;
  /** Setting menu icon top offset */
  public static final int settMenuIconYOffset = 2;
  /** Settings menu checkbox right X coordinate */
  public static final int settMenuCheckboxX = settMenuOptionX +
      settMenuOptionWidth - 6;
  /** Settings menu checkbox top offset */
  public static final int settMenuCheckboxYOffset = settMenuIconYOffset + 1;
  /** Settings menu text left coordinate */
  public static final int settMenuTextX = settMenuOptionX + 16;
  /** Settings menu text top offset */
  public static final int settMenuTextYOffset = 1;
  /** Settings menu pointer background color */
  public static final int settMenuPointerBackground = 0xFFD800;
  /** Settings menu pointer foreground color */
  public static final int settMenuPointerForeground = mainMenuForeground;
  /** Flag icon right X coordinate */
  public static final int settMenuFlagIconX= settMenuOptionX +
                                     settMenuOptionWidth - 2;
  /** Flag icon width */
  public static final int settMenuFlagIconWidth = 15;
  /** Flag icon height */
  public static final int settMenuFlagIconHeight = 12;


  //
  // In-Game menu configuration
  //
  /** Title left X coordinate */
  public static final int gameMenuTitleX = 12;
  /** Title top Y coordinate */
  public static final int gameMenuTitleY = 3;
  /** Game menu area left X coordinate */
  public static final int gameMenuAreaX = 3;
  /** Game menu area top Y coordinate */
  public static final int gameMenuAreaY = 22;
  /** Game menu area width */
  public static final int gameMenuAreaWidth = 122;
  /** Game menu area height */
  public static final int gameMenuAreaHeight = 116;
  /** Text padding */
  public static final int gameMenuTextPadding = 3;
  /** Game menu text area left X coordinate */
  public static final int gameMenuTextAreaX = gameMenuAreaX + gameMenuTextPadding;
  /** Game menu text area top Y coordinate */
  public static final int gameMenuTextAreaY = gameMenuAreaY + gameMenuTextPadding;
  /** Game menu text area width */
  public static final int gameMenuTextAreaWidth = gameMenuAreaWidth -
      gameMenuTextPadding - gameMenuTextPadding - scrollMarkWidth - 2;
  /** Number of text lines on game menu screens */
  public static final int gameMenuTextLines = mainMenuTextLinesCount + 1;
  /** Load bar left X coordinate */
  public static final int gameMenuLoadBarX = gameMenuAreaX + 10;
  /** Load bar width */
  public static final int gameMenuLoadBarWidth = gameMenuAreaWidth - 20;
  /** Load bar width */
  public static final int gameMenuLoadBarHeight = 5;
  /** Load bar vertical offset */
  public static final int gameMenuLoadBarYOffset = 2;
  /** Load bar color */
  public static final int gameMenuLoadBarColor = 0;

  /** Scroll bar height in game menu area */
  public static final int gameMenuScrollBarHeight = gameMenuAreaHeight - 8;
  /** Top Y coordinate for game menu scroll bar drawing */
  public static final int gameMenuScrollBarY = gameMenuAreaY + 4;
  /** Center X coordinate for scroll marks drawing in game menu area */
  public static final int gameMenuScrollMarkX = gameMenuAreaX + gameMenuAreaWidth - 7;


  /** The height of level box */
  public static final int gameMenuLevelBoxHeight = 18;
  /** Top Y coordinate for level text box drawing */
  public static final int gameMenuLevelBoxY = gameMenuAreaY +
                                                ( gameMenuAreaHeight -
                                                gameMenuLevelBoxHeight ) / 2;
  /** Text vertical offset for level caption */
  public static final int gameMenuLevelBoxTextYOffset = 1;
  /** Text horizontal offset for level caption */
  public static final int gameMenuLevelBoxTextXOffset = 4;


  //
  // Game screen configuration
  //
  /** Bottom area height */
  public static final int gameScreenIndicatorsAreaHeight = 14;
  /** Background color for indicators area */
  public static final int gameScreenIndicatorsBackground = 0xFF9000;
  /** Foreground color for indicators area */
  public static final int gameScreenIndicatorsForeground = 0xB00000;

  /** Active area for game board drawing */
  public static final int gameScreenActiveHeight = screenHeight -
      gameScreenIndicatorsAreaHeight;

  /** Width of number for score displaying */
  public static final int gameScreenScoreNumWidth = 5;
  /** Height of number for score displaying */
  public static final int gameScreenScoreNumHeight = 10;
  /** Total width of numbers image */
  public static final int gameScreenScoreNumWidthTotal =
      gameScreenScoreNumWidth * 11;
  /** Interval between score numbers */
  public static final int gameScreenScoreInterwal = 2;
  /** Left X coordinate for score drawing */
  public static final int gameScreenScoreX =
      ( screenWidth - gameScreenScoreNumWidth * 5 ) / 2;
  /** Left X coordinate for score divider */
  public static final int gameScreenScoreDivider = 10000;
  /** The width of lives mark */
  public static final int gameScreenLivesMarkWidth = 12;
  /** The width of bombs mark */
  public static final int gameScreenBombsMarkWidth = 10;
  /** The horizontal offset of indicators */
  public static final int gameScreenIndicatorHorizontalOffset = 4;
  /** The top Y coordinate for indicators drawing */
  public static final int gameScreenIndicatorY = gameScreenActiveHeight + 2;

  /** Enter name interwal between letters*/
  public static final int gameScreenEnterNameInterwal = 10;
  /** Enter name carret blink interval */
  public static final int gameScreenEnterNameBlinkDelay = 200;
  /** Enter name bottom Y coordinate */
  public static final int gameScreenEnterNameY = gameMenuAreaY +
                                                 gameMenuAreaHeight - 5;
  /** Enter name carret color */
  public static final int gameScreenEnterNameCarretColor = 0xB00100;


  //
  // Game screen pause menu configuration
  //
  /** The width of pause menu area */
  public static final int gameScreenPauseMenuWidth = 126;
  /** The height of pause menu area */
  public static final int gameScreenPauseMenuHeight = 94;
  /** The left X coordinate of pause menu area */
  public static final int gameScreenPauseMenuX =
      ( screenWidth - gameScreenPauseMenuWidth ) / 2;
  /** The top Y coordinate of pause menu area */
  public static final int gameScreenPauseMenuY =
      ( gameScreenActiveHeight - gameScreenPauseMenuHeight ) / 2;
  /** The pause menu option Y start */
  public static final int gameScreenPauseMenuOptionY = gameScreenPauseMenuY + 8;
  /** The pause menu interoption distance */
  public static final int gameScreenPauseMenuInterval = 3;
  /** The pointer horizontal offset regarding option */
  public static final int gameScreenPauseMenuPointerXOffset = 3;
  /** The pointer vertical offset regarding option top */
  public static final int gameScreenPauseMenuPointerYOffset = 2;

  //
  // Game engine configuration
  //
  /** Normal bombman move offset */
  public static final int bombmanMoveOffset = tileWidth / 4;
  /** Accelerated bombman move offset */
  public static final int bombmanAcceleratedMoveOffset = tileWidth / 2;
  /** Bombman scceleration time in ms */
  public static final int bombmanAccelerationTime = 3000;//3 sec
  /** Bombman animation interval in ms */
  public static final int bombmanAnimationInterval = 100;
  /** Number of animation frames for death */
  public static final int bombmanDeathAnimationFrames = 7;
  /** Number of animation frames for win animation */
  public static final int bombmanWinAnimationFrames = 11;
  /** Number of animation cycles for win animation */
  public static final int bombmanWinAnimationCycles = 3;
  /** Bombman win animation sprite width */
  public static final int bombmanWinAnimationWidth = tileWidth;
  /** Bombman win animation sprite height */
  public static final int bombmanWinAnimationHeight = 20;
  /** Maximal number of bombs in the bombman pocket */
  public static final int maxBombsInPocket = 3;
  /** Normal explosion range in cells */
  public static final int normalEplosionRange = 3;
  /** Extended explosion range in cells */
  public static final int extendedEplosionRange = normalEplosionRange * 2;
  /** Interval between bomb step changes in ms */
  public static final int bombStepChangeInterval = speedFactor * 2;

  /** Clock count down interval in ms */
  public static final int clockCountDownInterval = 100;
  /** Number of beep steps */
  public static final int clockBeepSteps = 10;
  /** Clock area left X coordinate */
  public static final int clockAreaX = 1;
  /** Clock area top Y coordinate */
  public static final int clockAreaY = 1;
  /** Clock area height */
  public static final int clockAreaHeight = 18;
  /** Clock area width */
  public static final int clockAreaWidth = 13;

  /** Board panning speed */
  public static final int boardPanningSpeed = 2;

  /** Level fading animation segments count */
  public static final int levelFadingSegments = 8;
  /** Level fading animation segment height */
  public static final int levelFadingSegmentHeight = screenHeight / levelFadingSegments;
  /** Level fading animation step */
  public static final int levelFadingStep = 1;

  /** Final animation step */
  public static final int finalAnimationStep = 2;
  /** Final caption display delay in ms */
  public static final int finalCaptionDelay = 1500;

  /** AI look range */
  public static final int aiLookRange = normalEplosionRange;

  //
  // Fonts definitions
  //
  public static final int FONT_TEXT_STYLE = Font.STYLE_BOLD;
  public static final int FONT_TEXT_SIZE = Font.SIZE_MEDIUM;
  public static final int FONT_TEXT_HEIGHT = 16;

  public static final int FONT_MENU_STYLE = Font.STYLE_PLAIN;
  public static final int FONT_MENU_SIZE = Font.SIZE_MEDIUM;
  public static final int FONT_MENU_HEIGHT = 16;

  public static final boolean fullScreen = false;
}