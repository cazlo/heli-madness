Helicopter Madness
==================

A basic 2-D side-scroller created using JAVA.
You control an RC helicopter and you must fly the helicopter through the level trying to collect as many rings as possible while avoiding any obstacles along the way.
Reach and land safely past the finish line to advance to the next level. 
Reach the end of all levels and you win.

Program Design
--------------

###Sprites

The program features an abstract class sprite, which allows for quickly making new game objects.
Inheritance was key here, as any common components which all sprites share, such as a position and x speed do not have to be added to each individual game object.
One merely has to expand upon the sprite architecture.
Nearly anything that can be represented with a picture or an animation is a sprite.
A sprite has an x and y representing its position with respect to the game-world , an x speed, and a collision shape.
The image can be handled by the sprite class or by AnimationUtils.

AnimationUtils is basically a way to handle animation based on how many times the program has been updated since the animation started.
It is built in one of two ways.
The first constructor takes the number of frames, a string representing the common path and name of the animation's image frames, an int [] representing the numbers after each frame's common name, a string representing the file extension of the image frames, an int representing the animation delay (in number of updates), and an enumeration representing if this animation plays once or loops.
The use of an int array representing the frame number allows reuse of frames, especially in the helicopter turning animation; the animation for turning left uses frames 1,2,and 3, while the animation for turning right uses frames 3,2, and 1.
This constructor will load files for each instance of this animation, so is best for when there is only one instance of this animation.
However, for sprites with many instances, such as birds, there is another constructor which does not load and store the images for each animation in the AnimationUtils object.
The animation handler is still used to keep track of the index of the frame which should be playing, and when it comes time to draw the animation, only the index of the current frame is returned from the AnimationUtils object.
This is then used to get the correct image from a static image array stored in the Bird object.
This reduces the potential memory footprint of the application, as only one copy of the animation's frames has to be stored in memory.

###The Level Object
In order to maintain readability of the main runner, HeliGameMain, much of the level processing and object handling is put into the level object.  To build a level, it is loaded from a file.  The level file is basically just a plain text file which lists the position of the level's fixed objects, such as the ground, rings, and trees.  The syntax is essentially the command followed by parentheses with the position.  For example, the line "tree(200,370)" creates a tree at x position 200, y position 370.  The ground is defined by a (long) list of x points and y points.  These ground points are read into an array and used to generate a path used for collision detection and also to draw the ground texture along the ground's path.  Birds are then randomly generated off screen.  There is no need for these birds to move through the entire level, so their position is always relative to the canvas, rather than the level world. The fixed objects, such as the ground and trees are drawn onto an image representing the whole level.  Sub-images of this image will be drawn repeatedly as the helicopter moves through the level.  When the helicopter gets to the point GAME_WIDTH/2, the level's currentX value moves at the same speed as the helicopter and the helicopter remains stationary with regards to the canvas.  However, because the level is moving, it appears as if the helicopter is moving through the level.  The level stops moving when it reaches the point “levelLength – GAME_WIDTH/2” and the heli is free to move across the canvas.  Basically when the helicopter is at the very beginning or end of the level, it is free to move around the canvas, but once it reaches the middle of the screen, the level begins to move around it instead.  See illustrations to see graphically how the level draws when the helicopter is not moving the canvas, and when the helicopter is moving the canvas and the helicopter is not moving.

###Running the game
To build and run the game, the level must be loaded from file to create the ground, tree, and ring objects.  Once the level is loaded, other objects in the game, such as the helicopter and the explosions are initialized.  The helicopter is a sprite, and thus had an x and y.  Its x position represents its position in the game-world, which is important for collision detection.  It also features a variable canvasX, which represents where it is drawn on the canvas.  There are also several animations related to the helicopter which must be initialized, including its turning animations, and explosion animations.  

After all of the gameworld objects are initialized, it is time to start building the UI.  The main runner for the program HeliGameMain is actually a JFrame, so this is built.  Then various panels are created and added to this JFrame.  There is an instrument panel, designed as an inner class of HeliGameMain, InstrumentPanel, which presents speed, score, and throttle information graphically to the user.  There is also an options panel with buttons to pause, play, stop, or bring up a help menu.  This is also designed as an inner class of HeliGameMain, OptionsPanel.  The help menu is also an inner class of HeliGameMain, HelpMenu.  It contains information on the controls and objective of the game, as well as a button to play the game. There is also a Canvas, gameCanvas, designed as an inner class of HeliGameMain so that it can easily access the game objects.  This is where the game is drawn.  This canvas utilizes active rendering, using double buffering to prevent flickering caused by drawing directly onto the screen.  The gameCanvas contains an off screen image, offImage which it draws to to render the game.  It also implements KeyListener, so that it can receive input from the keyboard.  Once all of this is constructed the game is set to PAUSED, and the game's loop thread is started by start().

Start() invokes the loop() method, the game's loop.  The loop will run until the gameState is set to GAMEOVER or WIN, and the process is relatively simple.  First it takes a measurement of the time using System.nanotime().  It calls updateGame(), which updates the position of all of the level's objects and then performs collision detection.  Then the game is drawn based on these updated positions.  Then it takes another measurement of the time, which is used to calculate how long to sleep to keep at the set number of updates per second.  This sleep time allows for other threads to use the CPU, such as the EDT, which prevents input lag.  This process continues until the game is won or lost, at which time a message will be drawn on the game to relay that the user has won or lost.

###Game states
There are several enumerations which deal with states.  The use of enumerations for states of the game and the game's objects is a lot less clunky then using boolean flags, and therefore allows for more readable and maintainable code.

There is the gameState(), found in HeliGameMain, which represents what is happening in the game.  This state is either INITALIZED, PLAYING, PAUSED, GAMEOVER, NEXTLEVEL, EXPLODING, or WIN.  These are used draw the correct things, and either keep the loop going or to break out of it.  Most of these are self-explanatory, but let me talk about EXPLODING.  The animation works by keeping track of how many game updates have happened since the last update to the animation.  So when the helicopter is exploding, the game must continue to update in order for the explosion animation to play correctly.  Therefore, EXPLODING is used to show that the explosion animation is still playing, so the game must still continue to be updated.

Then there is CanvasStates, which represents the state of the helicopter in regard to the canvas.  STATIONARY refers to the state in which the helicopter is not moving, but rather the level is moving.  MOVING_IN_LEFT refers to the state in which the helicopter is moving in the extreme beginning part of the level, and MOVING_IN_RIGHT refers to the state in which the helicopter is moving at the end of the level.  For both of these states, the canvas is stationary.  These states are used to correctly update the canvas, and to make sure the helicopter is drawn at the correct part of the canvas.

The helicopter itself has 2 states: a ThrottleState and a MovementState.  The throttle state represents the state of the throttle and consequently the blades.  This is used to move the helicopter up or down and to ignore bird collision when the engine is idle.  The movement state is used to keep track of which way the helicopter is facing, whether it is animated or not, and whether it is turning or not.  These are used to draw the correct image for the helicopter at any given time.

###Double Buffering
Initially the game was setup to use passive rendering.
The game canvas was set up as a Jpanel.
The method paintComponent(Graphics g) was overridden and drawGame(Graphics g) was within this method.
In the main loop, repaint() was called when the game needed to be repainted, which invoked the paintComponent() method of all objects on main game Jframe.
The downside to this is that repaint() calls do not always get heard, and in fact many of these calls are ignored completely.
This leads to a bit of lagginess and jaginess in the game.
To combat this, the rendering technique was switched to active rendering.
Now the game canvas is set up as a Canvas, and uses double buffering to render the game image a bit more smoothly (and possibly faster, through acceleration by the local graphics environment).  First the image to be drawn onto the canvas is rendered in an off screen image.  The drawGame() method is called to draw the game world and objects onto this off screen image.
The image is then blitted to the canvas's buffer and this buffer is shown.
This rendering technique gives a more consistent experience, as frames are not dropped at the discretion of the java virtual machine, but rather are painted every time they need to be.

	
