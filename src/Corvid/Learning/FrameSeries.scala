package Corvid.Learning

import org.encog.ml.data.basic.{BasicMLData, BasicMLDataPair}
import Corvid.Controller.{Common, Frame}

/**
 * Collection of consecutive frames (time-series), including the next motor output speeds
 */
class FrameSeries(
  val frames: IndexedSeq[Frame],
  val forwardSpeed: Double,
  val turnSpeed: Double) {

  def this(frames: IndexedSeq[Frame], next: Frame) = this(frames, next.forwardSpeed, next.turnSpeed);

  def toMLData = {
    new BasicMLData((frames.map(f => f.toMLData.getData).flatten ++ List(forwardSpeed, turnSpeed)).toArray);
  }

}

class DataPair(
  val input: FrameSeries,
  val diffs: IndexedSeq[Double])
{
  def toMLDataPair = {
    val normalizedDiffs = diffs.map(d => Common.limit(d, -1.0, 1.0)).toArray;
    new BasicMLDataPair(input.toMLData, new BasicMLData(normalizedDiffs));
  }
}