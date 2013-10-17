Gossip.Simulator
================

Gossip simulator using akka actor

The <b>input</b> provided (as command line to topologyTest.scala) will be of the form:

  topologyTest.scala numNodes topology algorithm
  
numNodes - the number of actors involved (for 2D based topologies it is round up until it is a square),
topology - one of full, 2D, line, imp2D, torus
algorithm - one of gossip, push-sum.

<b>Output:</b> Print the amount of time it took to achieve convergence of the algorithm.


The actual <b>network topology</b> plays a critical role in the dissemination speed of Gossip protocols. This project experiments with various topologies. The topology determines who is considered a neighbour in the above algorithms.

  <i>Full Network</i>- Every actor is a neighbour of all other actors. That is,every actor can talk directly to any other actors.

  <i>2D Grid</i> - Actors form a 2D square grid of maximum possible size which is lesser than number of actors. The actors can only talk to the grid neigbours.

  <i>Line</i> - Actors are arranged in a line. Each actor has only 2 neighbours (one left and one right, unless you are the first or last actor).

  <i>Imperfect 2D Grid</i> - Grid arrangement but one random other neighbour is selected from the list of all actors (4+1 neighbours).

  <i>Torus</i> - Creates a torus out of the 2D grid.


<b>Gossip Algorithm</b> for information propagation involves the following:

<i>Starting</i> - A participant(actor) it told/sent a roumor(fact) by the main process

<i>Step</i> - Each actor selects a random neighbour and tells it the roumor

<i>Termination</i> - Each actor keeps track of rumors and how many times it has heard the rumor. It stops transmitting once it has heard the rumor N times (N is currently set to 10 (arbitrary), This parameter can be modified in the regularJoe class).
              

<b>Push-Sum algorithm</b> for sum computation

<i>State</i> - Each actor Ai maintains two quantities: s and w. Initially, s = xi = i (that is actor number i has value i) and w = 1

<i>Starting</i> - Ask one of the actors to start from the main process.

<i>Receive</i> - Messages sent and received are pairs of the form (s;w). Upon receive, an actor should add received pair to its own corresponding values. Upon receive, each actor selects a random neighbour and sends it a message.

<i>Send</i> - When sending a message to another actor, half of s and w is kept by the sending actor and half is placed in the message.

<i>Sum estimate</i> - At any given moment of time, the sum estimate is s/w where s and w are the current values of an actor.

<i>Termination</i> - If an actors ratio s/w did not change more than 10^-10 in 3 consecutive rounds the actor terminates.


Bonus section
================

Implemented node failure (a node dies), BossGuy, randomly kills a user specified number of children. I am assuming a clean exit, and what I mean by that is, when a node dies, it communicates to the bossGuy that it is shutting down, and the bossGuy can communicate this message to all other nodes, to avoid accumalation of dead letters(messages that are delivered to a dead node).
This code for this is present with the tags "---->NEW CODE<----" and end with the corresponding same tag. The file Project2Bonus.scala contains the code for this.

To Do: Implement - communication failure models (a connection dies temporarily or permanently). Steps to  do this would be, bossGuy, tell a random node to stop using a random channel. We should handle the dead letter that are generated out of this communication channel loss.
