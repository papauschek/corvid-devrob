package Corvid.Controller

import scala.swing.event._
import swing.Swing
import Corvid.Learning.SensorModel

/**
 * This object is the main entry point of the application.
 * It shows the graphical user interface (as specified in the MainWindow class) 
 * which allows basic interaction with the software. Simultaneously, a network connection
 *  to the robot is established by an instance of the \emph{Controller} class. If the robot 
 *  cannot be reached, the robot controller cannot be used and only offline functionality 
 *  is available (such as training a sensomotoric model from previously recorded sensor 
 *  experiences).
 */
object Main
{

  var controller : Controller = null;

  // Main entry point
  def main(args: Array[String])
  {
    Swing.onEDT { startup(args) }
  }

  def startup(args: Array[String])
  {

    try
    {
      // Create controller
      controller = new Controller(RobotParameters.RealCorvid);

      // Show Main Window
      val mainWindow = new MainWindow(controller);

      mainWindow.reactions += {
        case e: WindowClosing => shutdown();
      }
      mainWindow.visible = true;
    }
    catch
    {
      case e: Exception => println(e); System.exit(1);
    }
  }

  def shutdown()
  {
    try
    {
      println("shutting down");
      controller.close();
      System.exit(0);
    }
    catch
    {
      case e: Exception => println(e); System.exit(1);
    }
  }

}
