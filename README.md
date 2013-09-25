Gossip.Simulator
================

Gossip simulator using akka actor

The input provided (as command line to topologyTest.scala) will be of the form:

  topologyTest.scala numNodes topology algorithm
  
Where 
numNodes is the number of actors involved (for 2D based topologies it is round up until it is a square),
topology is one of full, 2D, line, imp2D, 
algorithm is one of gossip, push-sum.

Output: Print the amount of time it took to achieve convergence of the algorithm.

The actual network topology plays a critical role in the dissemination speed of Gossip protocols. This project experiments
with various topologies. The topology determines who is considered a neighbour in the above algorithms.

 Full Network - Every actor is a neighbour of all other actors. That is,every actor can talk directly to any other actors.
 2D Grid - Actors form a 2D grid. The actors can only talk to the grid neigbours.
 Line: Actors are arranged in a line. Each actor has only 2 neighbours (one left and one right, unless you are the first or last actor).
 Imperfect 2D Grid: Grid arrangement but one random other neighbour is selected from the list of all actors (4+1 neighbours).

Gossip Algorithm for information propagation involves the following:
 Starting: A participant(actor) it told/sent a roumor(fact) by the main process
 Step: Each actor selects a random neighbour and tells it the roumor
 Termination: Each actor keeps track of rumors and how many times it has heard the rumor. It stops transmitting once it has heard the rumor N times 
              (N is currently set to 10 (arbitrary), This parameter can be modified in the regularJoe class).
              
Push-Sum algorithm for sum computation
 State: Each actor Ai maintains two quantities: s and w. Initially, s = xi = i (that is actor number i has value i) and w = 1
 Starting: Ask one of the actors to start from the main process.
 Receive: Messages sent and received are pairs of the form (s;w). Upon receive, an actor should add received pair 
           to its own corresponding values. Upon receive, each actor selects a random neighbour and sends it a message.
 Send: When sending a message to another actor, half of s and w is kept by the sending actor and half is placed in the message.
 Sum estimate: At any given moment of time, the sum estimate is s/w where s and w are the current values of an actor.
 Termination: If an actors ratio s/w did not change more than 10^-10 in 3 consecutive rounds the actor terminates.


Bonus section
================

Implemented node failure (a node dies) and communication failure models (a connection dies temporarily or permanently)
