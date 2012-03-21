package Corvid.Controller

import Corvid.Learning._
import collection.mutable.ArrayBuffer
import scala.util.Random
;

/**
 * This class loads a previously trained Predictor (the sensomotoric model) from disk 
 * and uses it to predict future sensor data in order to choose optimal actions for 
 * the robot. It implements the action generation and action selection part from the
 * hypothesis. It also expresses the three reward formulas as Scala functions.
 * The class takes an exploration parameter, which determines the probability of
 * random exploration versus exploitation of the sensomotoric model.
 */
class PredictingControl(predictorFile: String, parameters : RobotParameters,
    goal : String, exploration : Double, preventDamage : Boolean = false) extends Control {

  private var frameReward : FrameReward = null;
  
  val predictor = Common.deserialize[Predictor](predictorFile);
  val currentFrames = ArrayBuffer.empty[Frame];
  val random = new Random();

  private val deltas = IndexedSeq((0.0, 0.0),
      (-0.3 * parameters.forwardAcceleration, 0.0),
      (0.3 * parameters.forwardAcceleration, 0.0),
      (0.0, -0.3 * parameters.turnAcceleration),
      (0.0, 0.3 * parameters.turnAcceleration));

  def control(robot: Robot) : FrameReward = {

    // Collect frame data over time
    if (currentFrames.length >= predictor.timeSteps) currentFrames.remove(0);
    currentFrames.append(robot.toFrame());

    // We do not have enough information about the past
    if (currentFrames.length < predictor.timeSteps)
    {
      robot.accelerateTo(0, 0);
      return null;
    }

    val start = System.currentTimeMillis();

    val options = getPossibleOutcomes(currentFrames);
  
    // get current rewards
    val currentFrame = currentFrames.last;
	  val currentReward = evaluate(currentFrame);
    
    if (random.nextDouble() >= exploration)
    {
    	// We can predict the future now, yay!
   	  val bestOption = getBestOption(currentFrames, options, 0);
      val bestFrame = bestOption._1;

	    printf("curr: [%.2f] %s\r\n", currentReward, currentFrame);
	    printf("best: [%.2f] %s (future => %.2f)\r\n", evaluate(bestFrame), bestFrame, bestOption._2);
	
	    val needed = System.currentTimeMillis() - start;
	    printf("needed: %d\r\n", needed);
	
	    // act on best option
	    //controlPreventDamage(robot, bestFrame.forwardSpeed, bestFrame.turnSpeed);
	    robot.accelerateTo(bestFrame.forwardSpeed, bestFrame.turnSpeed);
    }
    else
    {
 	    // act randomly
      val randomFrame = options(random.nextInt(options.length));
	    controlPreventDamage(robot, randomFrame.forwardSpeed, randomFrame.turnSpeed);
      //robot.accelerateTo(randomFrame.forwardSpeed, randomFrame.turnSpeed);
    }

    return new FrameReward(currentFrame, goal, currentReward);
  }
  
  // control robot with recommended movements but try to prevent damage by forcing to stop
  def controlPreventDamage(robot : Robot, recForwardSpeed : Double, recTurnSpeed : Double) {
    
    // get sensor values
    val frontMin = robot.minDistance(0);
    val rightMin = robot.minDistance(1) + 0.10;
    val backMin = robot.minDistance(2);
    val leftMin = robot.minDistance(3) + 0.10;
    val sideMin = math.min(rightMin, leftMin);
    val forwardMin = math.min(sideMin, frontMin);
    val backwardMin = math.min(sideMin, backMin);

    val stopDistance    = 0.30;

    val turnSpeed = recTurnSpeed;
    val forwardSpeed = Common.limit(
      recForwardSpeed,
      -backwardMin + stopDistance,
      forwardMin - stopDistance);
        
    robot.accelerateTo(forwardSpeed, turnSpeed);
  }

  def evaluate(frame: Frame) : Double = {
    
    val minDistance = frame.distances.min;
    
    // other test rewards
    //val distance = Common.limit(distances.min, 0.0, 1.0);
    //math.min(math.abs(frame.forwardSpeed), minDistance - 0.5) + minDistance;
    //math.abs(frame.forwardSpeed) - math.abs(frame.turnSpeed);    
      
    if (goal == "holddistance")
    {
    // reward for staying at constant distance
    	//if (reward > -0.3) reward += -math.abs(frame.forwardSpeed) - math.abs(frame.turnSpeed);
    	return -math.abs(minDistance - 1.0);
    }
    else if (goal == "maxdistance")
    {
    	// reward for maximum distance
    	return frame.distances.map(d => math.sqrt(d)).sum;
    }
    else if (goal == "fwdistance")
    {
      // reward for close forward distance
      return -math.abs(frame.distances(0) - 0.80) -math.abs(frame.distances(7) - 0.80);
    }
    else if (goal == "drive")
    {
    	// reward for driving fast without collision
    	return minDistance + math.abs(frame.forwardSpeed);    
    }
    else
      throw new Exception("Unknown goal");
  }

  def getBestOption(frames: IndexedSeq[Frame], options: IndexedSeq[Frame], depth: Int) : (Frame, Double) =
  {
    if (depth >= 2)
    {
      options.map(option => (option, evaluate(option) * (depth + 1))).sortBy(_._2).last;
    }
    else
    {
      options.map(option => {
        val nextFrames = frames.tail :+ option;
        val nextBestOption = getBestOption(nextFrames, getPossibleOutcomes(nextFrames), depth + 1);
        
        // rewards are weighted based on their search depth
        val value = nextBestOption._2 + evaluate(option) * (depth + 1);
        (option, nextBestOption._2 + evaluate(option));
      }).sortBy(_._2).last;
    }
  }

  def getPossibleOutcomes(frames: IndexedSeq[Frame]) = {

    deltas.map(delta => {
      val currentFrame = frames.last;
      val currentDistances = currentFrame.distances;
      
      val frontMin = math.min(currentDistances(0), currentDistances(7));
      val rightMin = math.min(currentDistances(1), currentDistances(2)) + 0.10;
      val backMin = math.min(currentDistances(3), currentDistances(4));
      val leftMin = math.min(currentDistances(5), currentDistances(6)) + 0.10;
      val sideMin = math.min(rightMin, leftMin);
      val forwardMin = math.min(sideMin, frontMin);
      val backwardMin = math.min(sideMin, backMin);
      val stopDistance    = 0.30;
      
      val nextForwardSpeed =
      	if (currentFrame.forwardSpeed != 0 &&
      	    math.signum(currentFrame.forwardSpeed) != math.signum(currentFrame.forwardSpeed + delta._1))
      		0.0
      	else if (preventDamage)
      	  Common.limit(currentFrame.forwardSpeed + delta._1, -backwardMin + stopDistance, forwardMin - stopDistance);
      	else
      	  Common.limit(currentFrame.forwardSpeed + delta._1, -1, 1);
      
      val nextTurnSpeed = if ((currentFrame.turnSpeed != 0 &&
      	    math.signum(currentFrame.turnSpeed) != math.signum(currentFrame.turnSpeed + delta._2))
      	    || math.abs(nextForwardSpeed) > 0)
      		0.0
      	else
      		Common.limit(currentFrame.turnSpeed + delta._2, -1.0, 1.0);
      
      val series = new FrameSeries(frames, nextForwardSpeed, nextTurnSpeed);

      // predict outcome
      val nextDistanceDiffs = predictor.predict(series);
      val nextDistances = nextDistanceDiffs.zip(currentFrame.distances).map(p => Common.limit(p._1 + p._2, 0.1, 6.0));

      new Frame(
        currentFrame.timeStamp + 300,
        nextForwardSpeed,
        nextTurnSpeed,
        nextDistances
      );
    });
  }

  def test() {
    test(-0.5, 0.4, 0.4);
    test(0.0, 0.4, 0.4);
    test(0.5, 0.4, 0.4);
  }

  def test(forwardSpeed : Double, firstDistance: Double, secondDistance: Double) {

    val distance = 1.5;
    //val distanceSide = 

    val frame = new Frame(0,
      0, forwardSpeed,
      Array(firstDistance, distance, distance, distance, distance, distance, distance, firstDistance));

    val frame2 = new Frame(300,
      0, forwardSpeed,
      Array(secondDistance, distance, distance, distance, distance, distance, distance, secondDistance));

    val frameSeries = new FrameSeries(Array(frame, frame2), forwardSpeed, 0.0);
    val prediction = predictor.predict(frameSeries);
    printf("prediction: %s\r\n", (0 to 7).map(i => "%d[%.2f]".format(i, prediction(i))).mkString("  "));

  }

}