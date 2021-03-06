# BombMan for J2ME with Reinforcement Learning AI agent
![BombMan](https://raw.githubusercontent.com/yaricom/bombman-RL-AI-J2ME/master/docs/ng_site/bm_screen_shoots_568x664.gif)

This classic game was created back in year 2005 with what is called now as Reinforcement Learning (RL) Artifical Inteligence (AI) agent in mind. It was designed to support almost all existing on that period Java 2 Micro Edition (J2ME) based groups of handsets. As result, taking into account that some of those devices had very strict processing/memory resources consumption requirements, it was required to implement state-of-the-art AI agent to challenge human player and be able to operate under such tough conditions.

So, RL methodologies was applied as result of natural evolution of my considerations about was is the best suited for this task realisation. The AI algorithm was developed by me from scratch without any references.

The RL AI agent implementation may be found in this class: [AIBombmanSprite.java](https://github.com/yaricom/bombman-RL-AI-J2ME/blob/master/src/common/ng/games/bombman/sprites/AIBombmanSprite.java)

## Building, running
In order to build executables you need to have J2ME platform installed on your system. As for now Java ME SDK may be found here: [JAVA ME SDK](http://www.oracle.com/technetwork/java/embedded/javame/javame-sdk/overview/index.html)

The main build scripts are:
- build_res_ssg.xml - to build all neccessary game resources (it will require PNG processing library developed by me)
- build_ssg_all.xml - to build binaries for all supported handsets (it will require additional tools developed by me )

In order to build it will require Apache ANT build system installed in your environment. All mentioned additional tools and libraries provided under [Libraries](https://github.com/yaricom/bombman-RL-AI-J2ME/tree/master/Libraries) folder as JAR files.

I'm not sure if it's still possible to build it against latest releases of Java Micro Edition platform and leave this to find out by anybody interseted enough. Meantime I'll try to port this game to Python in order to use it in my further plays with RL AI.

Watch this repository if iterested - I'll post updates about Python porting.

## Bonus
If you happen to still have one of those Nokia Series 40 or Series 60 handsets operable, than you are the lucky one! Because as additional bonus for your thriftiness you may found working binaries of the game. Just look under [release](https://github.com/yaricom/bombman-RL-AI-J2ME/tree/master/dist/ssg/all/release) directory - I've small gift for you if you have Nokia device similar to [Nokia 6131](http://www.gsmarena.com/nokia_6131-1434.php) or [Nokia N73](http://www.gsmarena.com/nokia_n73-1550.php)

## Even more fun
Here is some GIF trailer of a game play. The RL AI agent is on the right site - the one in blue helmet :)

![BombMan](https://raw.githubusercontent.com/yaricom/bombman-RL-AI-J2ME/master/docs/ng_site/bm_trailer_176x208.gif)

## Author

Iaroslav Omelianenko

## License

BombMan is available under the MIT license. See the LICENSE file for more info.
