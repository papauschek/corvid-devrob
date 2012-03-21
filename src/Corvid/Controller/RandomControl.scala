package Corvid.Controller
import scala.util.Random

class RandomControl extends Control {
  
  private var directionMode = 1;
  private var turnMode = 0;

  /**
   * Controls the robot randomly, but avoids obstacles
   */
  def control(robot: Robot) : FrameReward = {

    // get sensor values
    val frontMin = robot.minDistance(0);
    val rightMin = robot.minDistance(1) + 0.10;
    val backMin = robot.minDistance(2);
    val leftMin = robot.minDistance(3) + 0.10;
    val sideMin = math.min(rightMin, leftMin);
    val forwardMin = math.min(sideMin, frontMin);
    val backwardMin = math.min(sideMin, backMin);

    // randomize movement mode
    //if (Random.nextDouble() < 0.01) turnMode = 1 - turnMode;
    if (Random.nextDouble() < 0.01) directionMode *= -1;

    // randomize forward speed
    val turnDistance    = 0.45;
    val stopDistance    = 0.30;
    val reverseDistance = 0.25;
    val turnMaxGain     = 0.03;

    //val speedDelta = (Random.nextDouble() - 0.4) * 0.3 * directionMode;
    val speedDelta = directionMode;
    var forwardSpeed = Common.limit(
      robot.lastForwardSpeed + speedDelta,
      -backwardMin + stopDistance,
      forwardMin - stopDistance);

    var turnSpeed = 0.0;
    if (turnMode == 1)
      turnSpeed = robot.lastTurnSpeed + (Random.nextDouble() - 0.5) * 0.5;

    val leftGain = (robot.distance(6) - robot.distance(5)) * directionMode;
    val rightGain = (robot.distance(1) - robot.distance(2)) * directionMode;

    //printf("forward speed: [%.2f] => [%.2f]\r\n", robot.realForwardSpeed, robot.lastForwardSpeed);
    //printf("front[%.2f] left[%.2f] right[%.2f]\r\n", frontMin, leftMin, rightMin);
    //printf("turning[%d] direction[%d]\r\n", turnMode, directionMode);

    printf("%s\r\n", (0 to 7).map(i => "%d[%.2f]".format(i, robot.distance(i))).mkString("  "));

    // turn if obstacle
    if (frontMin < reverseDistance && backwardMin > turnDistance)
    {
      turnSpeed = 0;
      forwardSpeed = -1.0;
      println("front collision: reversing");
    }
    else if (backMin < reverseDistance && forwardMin > turnDistance)
    {
      turnSpeed = 0;
      forwardSpeed = 1.0;
      println("back collision: reversing");
    }
    else if (frontMin < turnDistance && directionMode == 1)
    {
      if (rightMin > leftMin)
      {
        println("front collision: turning right");
        turnSpeed = 1;
      }
      else
      {
        println("front collision: turning left");
        turnSpeed = -1;
      }
    }
    else if (backMin < turnDistance && directionMode == -1)
    {
      if (rightMin < leftMin)
      {
        println("back collision: turning right");
        turnSpeed = 1;
      }
      else
      {
        println("back collision: turning left");
        turnSpeed = -1;
      }
    }
    else if (leftMin < turnDistance && leftGain < turnMaxGain)
    {
      turnSpeed = directionMode;
      println("left collision: turning");
    }
    else if (rightMin < turnDistance && rightGain < turnMaxGain)
    {
      turnSpeed = -directionMode;
      println("right collision: turning");
    }

    // send motor command
    //robot.stop();
    robot.accelerateTo(forwardSpeed, turnSpeed);
    //robot.accelerateTo(0, 0);
    null;
  }

}