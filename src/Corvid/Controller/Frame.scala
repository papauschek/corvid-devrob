package Corvid.Controller

import org.encog.ml.data.basic.BasicMLData
import scala.xml.Node
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.Point
import java.awt.Color
;

/**
 * Represents sensomotoric data from one moment of time
 */
class Frame (
  val timeStamp: Long,
  val forwardSpeed: Double,
  val turnSpeed: Double,
  val distances: IndexedSeq[Double]) extends XMLSerializable  {


  /** Converts the frame data to doubles for neural network input,
   * distances [0m;3m] will be normalized to [0;1] */
  def toMLData = {
    new BasicMLData((List(forwardSpeed, turnSpeed) ++
      distances.map(d => Common.limit(d / 3.0, 0.0, 1.0))).toArray);
  }

  override def toString = "f[%+.2f] t[%+.2f] d[%s]".format(forwardSpeed, turnSpeed, distances.map(d => "%.2f".format(d)).mkString(" "));

  def toXML =
    <timeStamp>{timeStamp}</timeStamp>
    <forwardSpeed>{forwardSpeed}</forwardSpeed>
    <turnSpeed>{turnSpeed}</turnSpeed>
    <distances>{Common.serialize(distances)}</distances>;

}

object Frame {
  
  def fromXML(node : Node) = new Frame(
    timeStamp = (node \ "timeStamp").text.toLong,
    forwardSpeed = (node \ "forwardSpeed").text.toDouble,
    turnSpeed = (node \ "turnSpeed").text.toDouble,
    distances = Common.deserializeDoubles(node \ "distances")
  );
  
  def draw(frame : Frame, g : Graphics2D, rect: Rectangle) {
    val center = new Point(rect.width / 2 + rect.x, rect.height / 2 + rect.y);
    val meter = rect.height / 3;
    val corvid = (meter * 0.2).toInt;
    
    // draw robot
    g.setColor(Color.GRAY);
    g.fillRect(center.x - corvid, center.y - corvid,
        corvid * 2, corvid * 2);
    
    // draw sensors
    val distances = frame.distances.map(d => (d * meter).toInt);
    def getColor(distance : Double) =
      if (distance <= 0.3) Color.RED
      else if (distance <= 0.5) Color.ORANGE
      else Color.GREEN;
        
    // front
    g.setColor(getColor(frame.distances(7)));
    g.fillRect(center.x - corvid, center.y - corvid - distances(7),
        corvid, distances(7));    
    g.setColor(getColor(frame.distances(0)));
    g.fillRect(center.x, center.y - corvid - distances(0),
        corvid, distances(0));
    
    // right
    g.setColor(getColor(frame.distances(1)));
    g.fillRect(center.x + corvid, center.y - corvid, distances(1), corvid);
    g.setColor(getColor(frame.distances(2)));
    g.fillRect(center.x + corvid, center.y, distances(2), corvid);

    // back
    g.setColor(getColor(frame.distances(3)));
    g.fillRect(center.x, center.y + corvid, corvid, distances(3));    
    g.setColor(getColor(frame.distances(4)));
    g.fillRect(center.x - corvid, center.y + corvid, corvid, distances(4));    

    // left
    g.setColor(getColor(frame.distances(5)));
    g.fillRect(center.x - corvid - distances(5), center.y - corvid, distances(5), corvid);    
    g.setColor(getColor(frame.distances(6)));
    g.fillRect(center.x - corvid - distances(6), center.y, distances(6), corvid);    
  }

}

class FrameReward (
	val frame : Frame, val goal : String, val reward : Double)
	extends XMLSerializable
{  
	def toXML =
    <frame>{frame.toXML()}</frame>
    <goal>{goal}</goal>
    <reward>{reward}</reward>;  
}

object FrameReward {
 
  def fromXML(node : Node) = new FrameReward(
    		frame = Frame.fromXML((node \ "frame").first),
    		goal = (node \ "goal").text.toString,
    		reward = (node \ "reward").text.toDouble
    );
  
}