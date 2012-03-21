package Corvid.Controller

import javaclient3._
import javaclient3.structures._
import util.control.Breaks._
import java.awt.image._
import java.awt.Transparency
import java.awt.color.ColorSpace
import javax.imageio.ImageIO
import java.io.File
;

/**
 * The robot class encapsulates all capabilities of the robot and provides a standard
 * interface for them. For example, all units are converted to meters, and the full 
 * forward speed is always 1.0, while full reverse speed is always -1.0
 *  This class makes it possible to use the real robot in the same way as the virtual
 *   robot in simulation. Calibration data is provided via the object RobotParameters,
 *   which contains default settings for both simulation and the real device.
 */
class Robot(val parameters: RobotParameters)
{
  var realForwardSpeed = 0.0;
  var realTurnSpeed = 0.0;
  var lastForwardSpeed = 0.0;
  var lastTurnSpeed = 0.0;
  var cameraImage : BufferedImage = null;

  // Connect to player server
  private val playerClient = new PlayerClient(parameters.hostname, parameters.port);
  playerClient.setNotThreaded();
  //playerClient.requestDataDeliveryMode(PlayerConstants.PLAYER_DATAMODE_PUSH);

  // Set up simulation interface
  private val simulationProxy : SimulationInterface =
    if (!parameters.isSimulation) null
    else playerClient.requestInterfaceSimulation(0, PlayerConstants.PLAYER_OPEN_MODE);

  // Initialize distance sensors
  private val rangers = Ranger(parameters, playerClient);

  // Initialize motors and stop them
  println("getting position proxy");
  private val positionProxy = playerClient.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
  if (positionProxy == null) throw new AppException("Position proxy is null");
  //positionProxy.setMotorPower(1);
  //positionProxy.setSpeed(0, 0);

  //playerClient.readAll();
  //playerClient.readAll();

  //playerClient.runThreaded(0, 0);

  // Initialize camera
  private val cameraProxy : CameraInterface = null; //playerClient.requestInterfaceCamera(0, PlayerConstants.PLAYER_OPEN_MODE);
  private val cameraColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
  private val cameraBandOffsets = if (parameters.isSimulation) Array[Int](2, 1, 0) else Array[Int](0, 1, 2);
  private var cameraSampleModel : PixelInterleavedSampleModel = null;
  //if (cameraProxy == null) throw new AppException("Camera proxy is null");

  //playerClient.requestDataDeliveryMode(PlayerConstants.PLAYER_DATAMODE_PULL);
  //playerClient.requestAddReplaceRule(-1, -1, PlayerConstants.PLAYER_MSGTYPE_DATA, -1, 1);

  // Wait for camera image
//  breakable { for (i <- 0 until 10) {
//    playerClient.readAll();
//    val cameraData = cameraProxy.getData;
//    if (cameraData != null && cameraData.getWidth > 0) break;
//  } }

  // Fill local sensory data
  private var lastRead = System.currentTimeMillis();
  private var lastSpeedSet = lastRead;
  read(0);


  /** Reads new sensor data from robot
   * and returns seconds since last read */
  def read(delay : Long) : Double = {

    // Retrieve new sensory information
    //Thread.sleep(delay);
    //val readStart = System.currentTimeMillis();
    playerClient.readAll();

    // Process new readings
    rangers.foreach(r => r.read());

    val positionData = positionProxy.getData;
    if (positionData == null)
      println("Position data is null");
    else
    {
      val velocity = positionProxy.getData.getVel;
      realForwardSpeed = velocity.getPx / parameters.maxForwardSpeed;
      realTurnSpeed = velocity.getPa / parameters.maxTurnSpeed;
    }

    readCamera();

    // Time since last readings
    val now = System.currentTimeMillis();
    val duration = now - lastRead;
    lastRead = now;
    (duration / 1000.0);
  }

  /** Closes the connection to the robot */
  def close() {
    playerClient.close();
  }


  /** Process new camera image */
  def readCamera() {

    return; // skip camera

    val cameraData = cameraProxy.getData;

    if (cameraData == null)
      throw new AppException("Camera data is null");

    var imageData = cameraData.getImage;
    if (imageData == null)
      throw new AppException("Image data is null");

    //printf("Image size: %d\r\n", imageData.length);
    val width = cameraData.getWidth;
    val height = cameraData.getHeight;
    val pixelCount = width * height;
    val dataCount = pixelCount * 3;
    if (imageData.length != dataCount)
      throw new AppException("Image data has size %d, while %d was expected", imageData.length, dataCount);

    // Create buffered image out of byte data
    if (cameraSampleModel == null)
      cameraSampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width, height, 3, width * 3, cameraBandOffsets);

    val dataBuffer = new DataBufferByte(imageData, width * height * 3);
    val raster = Raster.createWritableRaster(cameraSampleModel, dataBuffer, null);
    cameraImage = new BufferedImage(cameraColorModel, raster, false, null);
    //ImageIO.write(cameraImage, "png", new File("d:\\test.png"));
  }

  /** Set unitless motor speeds (1.0 for maximum speed, 0 for stop) */
  private def setSpeed(forwardSpeed: Double, turnSpeed: Double) {
    lastForwardSpeed = Common.limit(forwardSpeed, -1.0, 1.0);
    lastTurnSpeed = Common.limit(turnSpeed, -1.0, 1.0);
    lastSpeedSet = System.currentTimeMillis();
    val setTurnSpeed = if (!parameters.isSimulation) -lastTurnSpeed else lastTurnSpeed;
    positionProxy.setSpeed(
      lastForwardSpeed * parameters.maxForwardSpeed,
      setTurnSpeed * parameters.maxTurnSpeed);
  }

  /** Accelerate to unitless motor speeds (from -1.0 to 1.0) */
  def accelerateTo(newForwardSpeed : Double, newTurnSpeed : Double) = {
    // seconds since last speed set
    val duration = System.currentTimeMillis() - lastSpeedSet;
    val seconds = Common.limit(duration / 1000.0, 0.001, 0.300);

    // limit speed
    val forwardDelta = seconds * parameters.forwardAcceleration;
    val turnDelta = seconds * parameters.turnAcceleration;
    val forwardSpeed = Common.limit(newForwardSpeed,
      lastForwardSpeed - forwardDelta, lastForwardSpeed + forwardDelta);
    val turnSpeed = Common.limit(newTurnSpeed,
      lastTurnSpeed - turnDelta, lastTurnSpeed + turnDelta);
    setSpeed(forwardSpeed, turnSpeed);

    (forwardSpeed == newForwardSpeed && turnSpeed == newTurnSpeed);
  }

  def distance(index: Int) = rangers(index).distance;

  def distances = rangers.map(r => r.distance);

  def ranger(side: Int, offset: Int) = rangers((side * 2 + offset + 7) % 8);

  def minDistance(side: Int) = math.min(ranger(side, 0).distance, ranger(side, 1).distance);

  def toFrame() = new Frame(lastRead, lastForwardSpeed, lastTurnSpeed, distances);

  def stop()
  {
    while(!accelerateTo(0, 0)
      || math.abs(realForwardSpeed) > 0.001
      || math.abs(realTurnSpeed) > 0.001)
      read(100);
    setSpeed(0, 0);
  }

}

