package Corvid.Controller

import javax.swing.ImageIcon
import swing._
import java.awt.{Dimension, Graphics2D, Graphics, Image, Rectangle}
import java.awt.{Color => AWTColor}
import Corvid.Learning._

/**
 * This class shows the graphical user interface
 * which allows basic interaction with the software.
 */
class MainWindow(val controller: Controller) extends swing.Frame
{
  title = "Corvid Controller";

  /*
  val label = new Label() {
    icon = new ImageIcon(controller.robot.cameraImage);
  }; 
  */
  
  val predictorFile = "Data/full.predictor";

  // graphical display
  val screen = new Panel() {
	    
	  background = AWTColor.white
	  preferredSize = new Dimension(400, 400)

      override def paint(g: Graphics2D) {
        g.setColor(AWTColor.white)
        g.fillRect(0, 0, size.width, size.height)
        Frame.draw(controller.getFrame(), g, new Rectangle(0, 0, size.width, size.height));
      }
  };
	  
  val panel = new BorderPanel {
    
    // menu buttons
    add(new BoxPanel(Orientation.Vertical) {
      contents += new Button {
        action = Action("PANIC!") { Common.tryRun { 
          controller.setControl(null); } }
        margin = new Insets(20, 50, 20, 50);
      };
      contents += new Button { action = Action("Predicting control - holddistance") { Common.tryRun {
      	val predictingController = new PredictingControl(predictorFile, controller.parameters,
    	  goal = "holddistance", exploration = 0, preventDamage = true);
      	controller.setControl(predictingController);
	  } } };
      contents += new Button { action = Action("Predicting control - maxdistance") { Common.tryRun {
      	val predictingController = new PredictingControl(predictorFile, controller.parameters,
    	  goal = "maxdistance", exploration = 0, preventDamage = true);
      	controller.setControl(predictingController);
	  } } };
      contents += new Button { action = Action("Predicting control - fwdistance") { Common.tryRun {
      	val predictingController = new PredictingControl(predictorFile, controller.parameters,
    	  goal = "fwdistance", exploration = 0, preventDamage = true);
      	controller.setControl(predictingController);
	  } } };
      contents += new Button { action = Action("Random control") { Common.tryRun {
	    controller.setControl(new RandomControl());
	  } } };
      contents += new Button { action = Action("Train predictor") { Common.tryRun {
	    SensorModel.train(predictorFile, dataCount = 5000);
	  } } };
      contents += new Button { action = Action("Test predictor") { Common.tryRun {
    	val predictingController = new PredictingControl(predictorFile, controller.parameters, "", 0);
    	predictingController.test();
	  } } };
      contents += new Button { action = Action("Evaluation Rewards") { Common.tryRun {
    	Evaluation.testRewards();
	  } } };
      contents += new Button { action = Action("Evaluation Generalization") { Common.tryRun {
    	Evaluation.testGeneralization();
	  } } };
    }, BorderPanel.Position.West);
    
    add(screen, BorderPanel.Position.Center);
    
  };

  contents = panel;
  
  controller.updateHandler = () => {
    //Swing.onEDT(label.icon = new ImageIcon(controller.robot.cameraImage));
    Swing.onEDT(screen.repaint());
  }

  
  centerOnScreen();
  maximize();

}