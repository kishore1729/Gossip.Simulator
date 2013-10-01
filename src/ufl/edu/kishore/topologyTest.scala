package ufl.edu.kishore

import akka.actor.Actor
import akka.actor.Actor._
import akka.actor.Props
import akka.actor.ActorSystem
import scala.util.Random
import scala.concurrent.duration.Duration
import scala.collection.mutable.ArrayBuffer
import akka.dispatch.ExecutionContexts
import scala.concurrent.ExecutionContext
import com.sun.jna.Platform
import scala.compat.Platform
import sun.reflect.MagicAccessorImpl
import akka.actor.Kill
import akka.actor.ActorRef

object topologyTest {
  def main(args: Array[String]){
	  if(args.length != 3){
	    println("Incorrect input parameters")
	  }
	  else {
	    val masContext = ActorSystem("OneNet")
		val boss = masContext.actorOf(Props(new superBoss(args(0).toInt,args(1),args(2))),"bossGuy")
	  }
	}
}

class superBoss(numNodes:Int, topology:String, algo:String) extends Actor {

	val n = numNodes
	val childContext = ActorSystem("OneNet")
	var childDoneState = Array.fill[Boolean](n)(false)
	var startTime:Long = 0
    for (i<- 1 to n){
      var msg:String="init"
      val node = childContext.actorOf(Props(new regularJoe),"Node"+ i.toString)
      node ! msg
    }
	topology.toLowerCase() match {
	  case "line" => 
	    for(i <- 1 to n){
		  var msg:String="l"+i.toString +","
		  val targetChild = childContext.actorSelection("/user/Node"+ i)
		  if(i-1 > 0) 
			  msg = msg + (i-1).toString +","
		  if(i < n)
			  msg = msg + (i+1).toString 
		  targetChild ! msg
	    }
	    
	  case "full" =>
	    for(i <- 1 to n){
	      var msg:String="f"+n.toString
		  val targetChild = childContext.actorSelection("/user/Node"+ i) 
		  msg = msg + ","+i.toString
		  targetChild ! msg
	    }
	    
	  case "2d" =>
	    val magicNumber = math.sqrt(n).floor.toInt
	    val actualN = math.pow(magicNumber, 2).toInt
	    for(i <- 1 to actualN){
	      var msg:String="2"+i.toString
		  val targetChild = childContext.actorSelection("/user/Node"+ i) 
		  if(i-magicNumber > 0) msg = msg + "," + (i-magicNumber).toString  //Upper row check
		  if(i+magicNumber <= actualN) msg = msg + "," + (i+magicNumber).toString  //lower row check
		  if(i % magicNumber == 0) msg = msg + "," + (i-1).toString // right column check
		  else if (i % magicNumber == 1) msg = msg + "," + (i+1).toString // left column check
		  else msg = msg + "," + (i-1).toString + "," + (i+1).toString // middle of the row
		  println(targetChild+" : "+msg)
		  targetChild ! msg
	    }
	    
	    case "imperfect2d" =>
		    val magicNumber = math.sqrt(n).floor.toInt
		    val actualN = math.pow(magicNumber, 2).toInt
		    var rndInt = Int.MaxValue
		    while (rndInt > actualN || rndInt == 0) {
			  rndInt = (Random.nextInt % actualN).abs
		    }
		    for(i <- 1 to actualN){
			  var msg:String="m"+i.toString
			  val targetChild = childContext.actorSelection("/user/Node"+ i) 
			  if(i-magicNumber > 0) msg = msg + "," + (i-magicNumber).toString  //Upper row check
			  if(i+magicNumber <= actualN) msg = msg + "," + (i+magicNumber).toString  //lower row check
			  if(i % magicNumber == 0) msg = msg + "," + (i-1).toString // right column check
			  else if (i % magicNumber == 1) msg = msg + "," + (i+1).toString // left column check
			  else msg = msg + "," + (i-1).toString + "," + (i+1).toString // middle of the row
			  msg = msg + "," + rndInt.toString    //Random node addition
			  println(targetChild+" : "+msg)
			  targetChild ! msg
		    }
		    
		case "torus" =>
			val magicNumber = math.sqrt(n).floor.toInt
			val actualN = math.pow(magicNumber, 2).toInt
			for(i <- 1 to actualN){
				var msg:String="o"+i.toString
				val targetChild = childContext.actorSelection("/user/Node"+ i)
				if(i-magicNumber > 0) msg = msg + "," + (i-magicNumber).toString  //Upper row check
				else msg = msg + "," + (if (i % magicNumber !=0)(actualN - magicNumber + (i % magicNumber)) else actualN).toString
				if(i+magicNumber <= actualN) msg = msg + "," + (i+magicNumber).toString  //lower row check
				else msg = msg + "," + (if (i % magicNumber !=0)i % magicNumber else magicNumber).toString
				if(i % magicNumber == 0) msg = msg + "," + (i-1).toString + "," + (i+1-magicNumber).toString // right column check
				else if (i % magicNumber == 1) msg = msg + "," + (i+1).toString + "," + (i-1+magicNumber).toString// left column check
				else msg = msg + "," + (i-1).toString + "," + (i+1).toString // middle of the row
				
				println(targetChild+" : "+msg)
				targetChild ! msg
			}
	}
	
	algo.toLowerCase() match {
	  case "gossip"=>
	    val msg:String="g"+"pluto is not a planet anymore :("
	    var rndInt = Int.MaxValue
	    while (rndInt > n || rndInt == 0) {
		  rndInt = (Random.nextInt % n).abs
	    }
	    val targetChild = childContext.actorSelection("/user/Node"+ rndInt)
	    targetChild ! msg
	  case "push-sum"=>
	    val msg:String="p"+0.0.toString + "," + 0.0.toString
	    var rndInt = Int.MaxValue
	    while (rndInt > n || rndInt == 0) {
		  rndInt = (Random.nextInt % n).abs
	    }
	    val targetChild = childContext.actorSelection("/user/Node"+ rndInt)
	    targetChild ! msg
	  case whatever =>
	    println("Uncomprehensable algorithm: "+whatever+". Please ask my creators to teach me this ^_^")
	}
	
	startTime = System.currentTimeMillis()
	
	def receive = {
		case l:String =>
		  l.toLowerCase().head match {
		    case 'd' => //done
		        //println("Child "+l)
		        childDoneState.update((l.tail.toInt-1), true)
		        var childCount =0;
		        for (x <- childDoneState)
		          if(x == true) childCount += 1
		        //println(childDoneState.deep.mkString(","))
		        println("Time taken: " + (System.currentTimeMillis() - startTime).toString)
		        if(childCount == n) {
		          println("Percentage complete: 100")
		          context.children.foreach(context.stop(_))
		          context.stop(self)
		          exit
		        }
		        else {
		          println("Percentage complete: "+(childCount*100/n).toString)
		        }
		    case 'e' => //error
		        println("Error in Joe! I am stopping everything!")
		        context.children.foreach(context.stop(_))
		        println("stopped children, now I quit!")
		        context.stop(context.self)
		    case whatever =>
		        println("Master got this "+whatever)
		  }
    }
}

class regularJoe extends Actor {
  import context._
  var parentNode:ActorRef = null
  var myCount =0
  var gossipMode = true
  var rumor =""
  var staticCount = 0
  var myActiveLines = ArrayBuffer[String]()
  def transmitMsg = {
	  val msg = if(gossipMode == true)"rt"+rumor else "rf"
	  val len = myActiveLines.length
	  var rndInt = Int.MaxValue
	  while (rndInt > len || rndInt == 0) {
		  rndInt = (Random.nextInt % len).abs
	  }
	  val targetBro = context.actorSelection("/user/Node"+myActiveLines(rndInt))
	  targetBro ! msg
  }
  def receive = {
    case l:String =>
      l.head match{
        case 'i' => 
          parentNode = sender
          //println(context.self + ": "+"Joe Initalized.")
        case 'l' =>
          myActiveLines ++= l.tail.split(",")
        case 'r' =>
          l.tail.head match {
            case 't'=> gossipMode = true
            case 'f'=> gossipMode = false
          }
          if (gossipMode == true && myCount< 10) {
	          myCount +=1
	          rumor = l.tail.tail
	          val sendmsg = "d"+myActiveLines(0);
        	  	parentNode ! sendmsg;
	          //println(context.self + ": "+myCount+" "+l.tail)
	          transmitMsg
          }
          else if (myCount == 10) {
        	  myCount+=1;
        	  val sendmsg = "d"+myActiveLines(0);
        	  parentNode ! sendmsg;
        	  context.actorSelection("/user/bossGuy") ! sendmsg;
        	  println(context.self +" is done.")
          }
          else if (gossipMode == false) println("LOL")// check for difference and transmit
        case 'f' =>
          val input = l.tail.split(",")
          val numNodes = input(0).toInt
          val myNumber = input(1)
          for (i <- 1 to numNodes)
            myActiveLines += i.toString
          myActiveLines.update(0, myNumber)
          myActiveLines.update((myNumber.toInt - 1), 1.toString)
        case '2' =>
          myActiveLines ++= l.tail.split(",")
        case 'm' =>
          myActiveLines ++= l.tail.split(",")
        case 'o' =>
          myActiveLines ++= l.tail.split(",")
        case 'z'=>
          transmitMsg
        case 'g'=>
          gossipMode = true
          rumor = l.tail
          //need re-transmission
		  if(staticCount < 5 && myCount <9) {
		    val dur = Duration.create(500, scala.concurrent.duration.MILLISECONDS);
		    val me = context.self
		    context.system.scheduler.scheduleOnce(dur, me, "z")
		  }
          val sendmsg = "d"+myActiveLines(0);
        	  parentNode ! sendmsg;
          transmitMsg
        case 'p'=>
          gossipMode = false
          //push sum part
        case whatever => {println("error in Joe, got this: "+whatever); sender ! "error"}
      }
  }
}