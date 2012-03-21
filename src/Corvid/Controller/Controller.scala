package Corvid.Controller

import util.Random
import collection.mutable.ArrayBuffer
import java.util.Date
import scala.xml.XML

/**
 * This class is responsible for establishing a network connection to the robot and controlling it.
 * Different controlling routines can be used. For the experiments here, we used two different
 * implementations which can be found in the PredictingControl and RandomControl classes.
 * The controller makes sure that new sensor readings are taken about every 300 milliseconds from 
 * the robot, and stores the collected sensor, motor and reward data on disk. This data can later 
 * be used for building a sensomotoric model from the robots experience, or evaluating the average
 * reward.
 */
class Controller(val parameters : RobotParameters)
{
  
  private var control : Control = null;

  val frames = new ArrayBuffer[Frame]();
  val frameRewards = new ArrayBuffer[FrameReward]();
  var currentFrame = new Frame(0, 0.2, 0.2, Array(0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5));
  var updateHandler = () => ();

  // Controller thread
  private var isRunning = true;
  private val thread = Common.runThread( {

    try
    {
	    // Create robot proxy
	    val robot = new Robot(parameters);
	    
	    robot.stop();
	    val predicting = false;
	    while(isRunning)
	    {
	
	      val lastDistances = robot.distances;
	      val start = System.currentTimeMillis();
	      var end = start;
	      do {
	        robot.read(0);
	        end = System.currentTimeMillis();
	      }
	      while (
	        (robot.parameters.isSimulation || !lastDistances.zip(robot.distances).exists(d => d._1 != d._2))
	        && end - start < 200); // 200 on real corvid
	
	      val stepDuration = end - start;
	      if (stepDuration < 200 || stepDuration > 400)
	    	printf("Step duration exception: %d\r\n", stepDuration);
	
	      currentFrame = robot.toFrame();
	      frames.append(currentFrame);
	
	      if (control != null)
	      {
	        val frameReward = control.control(robot);
	        if (frameReward != null) frameRewards.append(frameReward);
	      }
	      else
	        robot.accelerateTo(0, 0);
	
	      updateHandler();
	    }
	
	    XML.save("%1$tY-%1$tm-%1$td_%1$tH-%1$tM-%1$tS.frames".format(new Date()),
	        <frames>{Common.serialize(frames)}</frames>);

	    if (frameRewards.length > 0)
	    	XML.save("%1$tY-%1$tm-%1$td_%1$tH-%1$tM-%1$tS.rewards".format(new Date()),
	    		<frameRewards>{Common.serialize(frameRewards)}</frameRewards>);

	    robot.stop();
	    robot.close();
    }
    catch
    {
      case e : Exception => println("Controller stopped: " + e.toString());
    }

  });

  def setControl (value : Control) {
    control = value;
  }
  
  def getFrame() = currentFrame;

  def close() {
    isRunning = false;
    thread.join();
  }
}

trait Control {
  def control(robot : Robot) : FrameReward;
}
