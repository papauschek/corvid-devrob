package Corvid.Controller

/**
 * This object provides default calibration parameters for both the
 * simulated and the real robot
 */
object RobotParameters
{
  val Simulation =
  {
    new RobotParameters(

      hostname = "gazebo-desktop",
      port = 6665,
      isSimulation = true,

      maxForwardSpeed = 0.1,
      maxTurnSpeed = 0.4,
      distanceMultiplier = 0.1, // multiplier for converting sensor distance to meters

      //minFrontDistance = 0.30, // 30 cm minimum distance to obstacles
      //minSideDistance = 0.20, // 20 cm minimum distance to obstacles
      imageSize = new Size(160, 120),

      forwardAcceleration = 1, // fraction of maximum speed per second
      turnAcceleration = 0.5 // fraction of maximum speed per second

      //turnRate = 0.72, // radians per player unit per second
      //forwardRate = 1.4336 // meters per player unit per second
    );
  }

  val RealCorvid =
  {
    new RobotParameters(

      hostname = "192.168.1.103",
      port = 6665,
      isSimulation = false,

      maxForwardSpeed = 0.4, // MaxSpeed corvid 0.3
      maxTurnSpeed = 1.0, // MaxSpeed corvid 0.5
      distanceMultiplier = 1.0, // multiplier for converting sensor distance to meters

      //minFrontDistance = 0.45, // 30 cm minimum distance to obstacles
      //minSideDistance = 0.35, // 20 cm minimum distance to obstacles
      imageSize = new Size(160, 120),

      forwardAcceleration = 1.0, // fraction of maximum speed per second
      turnAcceleration = 2.0 // fraction of maximum speed per second

      //turnRate = 0.72, // radians per player unit per second
      //forwardRate = 1.4336 // meters per player unit per second
    );
  }
}

class RobotParameters(

  val hostname : String,                // Player server hostname
  val port: Int,                        // Player server port
  val isSimulation : Boolean,           // Connect to simulation interface?
  val maxForwardSpeed : Double,         // The maximum forward speed in Player units
  val maxTurnSpeed : Double,            // The maximum turn speed in Player units
  val forwardAcceleration: Double,      // The maximum forward acceleration in fraction of maximum speed per second
  val turnAcceleration: Double,         // The maximum turn acceleration in fraction of maximum speed per second

  //val forwardRate : Double,             // The forward speed of the robot in relation to the Player units,
                                        // in meters per Player unit per second

  //val turnRate : Double,                // The turn speed of the robot in relation to the Player units,
                                        // in radians per Player unit per second

  val distanceMultiplier : Double,      // The multiplier for converting sensor distance to meters.

  val imageSize : Size[Int]             // The size of the desired robot camera image. Camera images will be
                                        // resized to this solution by the Robot::Corvid class.
)
{

}
