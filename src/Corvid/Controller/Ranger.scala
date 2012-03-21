package Corvid.Controller

import javaclient3._
import javaclient3.structures._

/** Represents a distance sensor */
class Ranger private (
  private val parameters: RobotParameters,
  private val index : Int)
{
  private var laserProxy : LaserInterface = null;
  private var sonarProxy : SonarInterface = null;
  private var lastReading : Double = 0;

  private def this(parameters: RobotParameters, laserProxy : LaserInterface, index: Int) {
    this(parameters, index);
    this.laserProxy = laserProxy;
  }

  private def this(parameters: RobotParameters, sonarProxy : SonarInterface, index: Int) {
    this(parameters, index);
    this.sonarProxy = sonarProxy;
  }

  def distance = lastReading;

  def read()
  {
    if (parameters.isSimulation)
    {
      val reading = laserProxy.getData.getRanges.toList.min * parameters.distanceMultiplier;
      if (reading < 0.20)
        lastReading = 0.20;
      else if (reading > 6.00)
        lastReading = 6.00;
      else
        lastReading = reading;
    }
    else
    {
      val sonarData = sonarProxy.getData;
      if (sonarData == null) return;

      val ranges = sonarData.getRanges;
      if (ranges == null) return;

      val rawDistance = ranges(index);
      //if (rawDistance > 0.0001)
        lastReading = rawDistance * parameters.distanceMultiplier;
    }
  }

}

object Ranger {

  /** Construct the rangers with laser sensors or ultrasound sensors depending
   * on whether we are simulating or not, in clockwise ordering */
  def apply(parameters: RobotParameters, client: PlayerClient) =
  {
    if (parameters.isSimulation)
    {
      (0 to 7).map(i => {
        val laserProxy = client.requestInterfaceLaser(7 - i, PlayerConstants.PLAYER_OPEN_MODE);
        new Ranger(parameters, laserProxy, 7 - i);
      });
    }
    else
    {
      println("getting sonar proxy");
      val sonarProxy = client.requestInterfaceSonar(0, PlayerConstants.PLAYER_OPEN_MODE);
      if (sonarProxy == null) throw new AppException("Sonar proxy is null");
      (0 to 7).map(i => new Ranger(parameters, sonarProxy, 7 - i));
    }
  }

}
