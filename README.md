# BombMan for J2ME with RL AI agent
![BombMan](https://raw.githubusercontent.com/yaricom/bombman-RL-AI-J2ME/master/docs/ng_site/bm_screen_shoots_568x664.gif)

This classic game was created back in year 2005 with what is called now as Reinforcement Learning AI agent in mind. It was designed to support almost all existing on that period J2ME based handsets groups. As result, taking into account that some of those devices had very strict processing/memory resources available, it was required to implement state-of-the-art AI agent to challenge human player and able to operate under such tough conditions.

So, RL methodologies was applied as natural evolution of my considerations about was is best suited for this task realisation.

The RL AI agent implementation may be found mostly in this class: [AIBombmanSprite.java](https://github.com/yaricom/bombman-RL-AI-J2ME/blob/master/src/common/ng/games/bombman/sprites/AIBombmanSprite.java)

## Building, running
In order to build executables you need to have J2ME platform installed on your system. As for now Java ME SDK may be found here: [JAVA ME SDK](http://www.oracle.com/technetwork/java/embedded/javame/javame-sdk/overview/index.html)

The main build scripts are:
- build_res_ssg.xml - to build all neccessary game resources (it will require PNG processing library developed by me)
- build_ssg_all.xml - to build binaries for all supported handsets (it will require additional tools developed by me )

In order to build it will require Apache ANT build system installed in your environment. All mentioned additionsl tools and libraries provided in [Libraries] folder as JAR files.

I'm not sure if it's possible to build it against latest releaes of Java Micro Edition platform and leave this to anybody interseted enough. Meantime I'll try to port this game to Python in order to use it in my further plays with RL AI.

Watch this repository if iterested - I'll post updates about Python porting.

## Even more fun
Here is some GIF trailer of a game play. The RL AI agent is on the right site - the one in blue outfits :)
![BombMan](https://raw.githubusercontent.com/yaricom/bombman-RL-AI-J2ME/master/docs/ng_site/bm_trailer_176x208.gif)
