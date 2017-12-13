# Joseph Gonzales
# COSC4342 Networks
___________________________________________________________________________________________
This is a client and server network implementation of a tetris game

To compile the program, use an editor such as NetBeans to compile the packages. 
To compile and run the programs by hand use javac and java commands in a terminal:

Server:
javac Log.java STetrominoes.java TServer.java
java TServer {address} &

Client:
javac ActionKey.java Board.java Shape.java ShapeStack.java TetrisC.java Tetrominoes.java TStart.java
java TStart (address}

To fully initialize the program make sure two computers are able to run the client and a computer to run the server. 
Run the server first either specifying an IP address in the arguments with an & sign to set as daemon, or run it in the
NetBeans IDE where it will prompt for a IP address.
The client can accept an IP address argument or a prompt will ask for one. enter it correctly to start. Once two
clients connect, the server will initiallize the game.

Use the keyboard to control your pieces:
left/right to move the piece left/right
up to drop the piece all the way to the bottom
down to move the piece down by one line sooner than it's falling rate
z to rotate left
x to rotate right

The game is over when one player reaches the top of the board first and is declared the loser
The player left standing is the winner
The game will exit afterwards
