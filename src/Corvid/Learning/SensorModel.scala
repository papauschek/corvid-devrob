package Corvid.Learning

import collection.mutable.ArrayBuffer
import Corvid.Controller._
import java.io.File
import scala.xml.XML
import scala.util.Random
;

/**
 * After collecting sensomotoric data via the Controller class and a controlling
 * mechanism such as RandomControl, the routines in the SensorModel object can 
 * be used to train a sensomotoric model. The main routine loads sensomotoric 
 * data from disk and filters it. The filter excludes duplicate sensor readings 
 * and sequences of readings which are too short or have time-delays outside 
 * the normal range of 250 to 350 milliseconds. This data is then used to train 
 * a new neural network using the Predictor class. The acquired neural network 
 * represents the sensomotoric model and is then saved to disk, and can be later 
 * used by the PredictingControl class to control the robot.
 */
object SensorModel {


  def train(outputFile: String, dataCount: Int)
  {
    // load data pairs for training
    val timeSteps = 2;
    val allDataPairs = loadDataPairs(timeSteps);
	val dataPairs = Random.shuffle(allDataPairs).take(dataCount);

	printf("using %d of %d data pairs\r\n", dataPairs.length, allDataPairs.length);
    printf("timediffs: %s\r\n", Stats(dataPairs.map(p => (p.input.frames.last.timeStamp - p.input.frames.head.timeStamp) * 0.001)));
    printf("corr0: %s\r\n", Stats.correlate(
      dataPairs.map(p => p.input.frames.last.forwardSpeed),
      dataPairs.map(p => p.diffs(0))));
    printf("corr3: %s\r\n", Stats.correlate(
      dataPairs.map(p => p.input.frames.last.forwardSpeed),
      dataPairs.map(p => p.diffs(3))));

    // Create neural network predictor
    train(dataPairs, timeSteps, outputFile);

    // Test predictor
    val pred = new PredictingControl(outputFile, RobotParameters.RealCorvid, "", 0);
    pred.test();
  }
  
  def loadDataPairs(timeSteps : Int) = {
    val folder = new File("TrainingData/");
    val files = folder.listFiles().filter(f => f.isFile);
    val dataPairs = ArrayBuffer[DataPair]();
    files.foreach(file => {
      val fileList = XML.loadFile(file.getAbsolutePath).child.elements.toSeq;
      val fileFrames = fileList.map(f => Frame.fromXML(f));
      split(fileFrames).foreach(splitFrames => {
        val fileDataPairs = Predictor.toDataPairs(splitFrames, timeSteps);
        printf("loaded %d data pairs\r\n", fileDataPairs.length);
        dataPairs.appendAll(fileDataPairs);
      });
    });
    dataPairs;
  }

  /*** Remove duplicate or invalid sensor readings */
  def split(frames: Seq[Frame]) : ArrayBuffer[ArrayBuffer[Frame]] = {
    val result = ArrayBuffer[ArrayBuffer[Frame]]();
    var current = ArrayBuffer[Frame]();
    val minLength = 10;
    var last : Frame = null;

    frames.foreach(frame => {

      val validFrame = true; //frame.turnSpeed < 0.1;
      if (last == null
        || (math.abs(frame.timeStamp - last.timeStamp - 300) < 150 && validFrame))
      {
        current.append(frame);
      }
      else
      {
        if (current.length >= minLength) result.append(current);
        current = ArrayBuffer(frame);
        printf("filtering frame\r\n");
      }

      last = frame;
    });
    if (current.length >= minLength) result.append(current);
    result;
  }

  def train(dataPairs: IndexedSeq[DataPair], timeSteps : Int, outputFile: String)
  {
    val predictor = new Predictor(timeSteps, 1, 8);
    if (dataPairs.length > 0)
    	predictor.train(dataPairs, dataPairs.length / 2);
    else
      println("Creating random predictor");
    Common.serialize(predictor, outputFile);
  }

  def printStats(frames: ArrayBuffer[Frame])
  {
    printf("count: %d\r\n", frames.length);
//    printf("forwardSpeed: %s\r\n", Stats(frames.map(f => f.forwardSpeed)));
//    printf("turnSpeed: %s\r\n", Stats(frames.map(f => f.turnSpeed)));
//    for (i <- 0 to 7)
//      printf("distance%d: %s\r\n", i, Stats(frames.map(f => f.distances(i))));

    for (i <- 0 to 100)
    {
      printf("forward: %.2f  turn: %.2f\r\n", frames(i).forwardSpeed, frames(i).turnSpeed);
    }

    // print distribution
    val allDistances = frames.map(f => f.distances).flatten;
    for (i <- 0.0 until 6.0 by 0.2)
    {
      val count = allDistances.count(d => d > i && d <= i + 0.2);
      printf("from %.1f to %.1f: %d\r\n", i, i+ 0.2, count);
    }

    // print correlations
    printf("correlation 0 / 7: %.3f\r\n",
      Stats.correlate(frames.map(f => f.distances(0)), frames.map(f => f.distances(7))));

    printf("correlation 0 / 1: %.3f\r\n",
      Stats.correlate(frames.map(f => f.distances(0)), frames.map(f => f.distances(1))));

    val diffSpan = 1;
    val currentFrames = frames.drop(diffSpan);
    val lastFrames = frames.dropRight(diffSpan);
    val distanceDiffs = currentFrames.zip(lastFrames).map(f => f._1.distances.zip(f._2.distances).map(d => d._1 - d._2));
    val timeDiffs = currentFrames.zip(lastFrames).map(f => f._1.timeStamp - f._2.timeStamp);
    val fwDiffs = currentFrames.zip(lastFrames).map(f => f._1.forwardSpeed - f._2.forwardSpeed);
    val turnDiffs = currentFrames.zip(lastFrames).map(f => f._1.turnSpeed - f._2.turnSpeed);

    printf("fw diff stats: %s\r\n", Stats(fwDiffs));
    printf("turn diff stats: %s\r\n", Stats(turnDiffs));

    printf("time diff stats: %s\r\n", Stats(timeDiffs.map(t => t / 1000.0)));

    for (i <- 0 to 7)
      printf("distance diffs: %s\r\n", Stats(distanceDiffs.map(d => d(i))));

    for (i <- 0 to 7)
      printf("correlation to %d: %.3f\r\n", i, Stats.correlate(
          currentFrames.map(f => f.forwardSpeed),
          distanceDiffs.map(d => d(i))));

  }

}