package Corvid.Learning

import Corvid.Controller.{Common, Frame}
import java.io.Serializable
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation
import util.Random
import org.encog.ml.data.basic.{BasicMLDataPair, BasicMLDataSet}

/**
 * Predicts sensor frames using neural networks.
 * 
 * The predictor class represents a neural network that predicts future sensor
 * states from current and past sensomotoric experience. For neural network training, 
 * the well-known Java library Encog is used. Our implementation uses the R-PROP 
 * training algorithm, which needs no parameters except the number of hidden layers
 * and neurons.
 */
@SerialVersionUID(1L)
class Predictor(
    val timeSteps : Int,
    val hiddenLayerCount : Int,
    val hiddenCount : Int)    
  extends Serializable {

  private val visibleCount = 10 * timeSteps + 2;
  private val outputCount = 8;

  var network = new BasicNetwork();
  network.addLayer(new BasicLayer(null, false, visibleCount));
  for (i <- 0 until hiddenLayerCount)
	  network.addLayer(new BasicLayer(hiddenCount));
  network.addLayer(new BasicLayer(outputCount));
  network.getStructure.finalizeStructure();
  network.reset();

  def train(dataPairs : IndexedSeq[DataPair], trainingSetLength : Int) : Double = {

    // prepare training and testing sets
    val mlDataPairs = dataPairs.map(p => p.toMLDataPair);
    val sets = Random.shuffle(mlDataPairs).splitAt(trainingSetLength);
    val trainingSet = sets._1;
    val testingSet = sets._2;
    val trainingData = new BasicMLDataSet(trainingSet.map(p => p.getInputArray).toArray, trainingSet.map(p => p.getIdealArray).toArray);

    printf("Stats: %s\r\n", Stats(trainingSet.map(t => t.getIdealArray()(0))));

    if (trainingSet.length == 0)
      return test(testingSet);
    
    // training
    val trainer = new ResilientPropagation(network, trainingData);
    trainer.iteration();

    var bestNetwork = Common.clone(network);
    var bestError = test(testingSet);
    var stagnationCount = 0;
    printf("initial error training: %.5f, testing: %.5f\r\n", trainer.getError, bestError);

    while(stagnationCount < 20)
    {
      trainer.iteration(50);
      val testingError = test(testingSet);
      printf("error training: %.5f, testing: %.5f\r\n", trainer.getError, testingError);

      if (testingError < bestError)
      {
        bestError = testingError;
        bestNetwork = Common.clone(network);
        stagnationCount = 0;
      }
      else
      {
        stagnationCount += 1;
      }
    }
    trainer.finishTraining();

    network = bestNetwork;
    printf("final testing error: %.5f\r\n", bestError);    
    return bestError;
  }

  private def test(set: IndexedSeq[BasicMLDataPair]) = {

    var errorTotal : Double = set.map(pair => {
      // get prediction
      val actual = pair.getIdealArray;
      val prediction = network.compute(pair.getInput).getData;

      // calculate percentage of correct distance change direction
      (8 - actual.zip(prediction).count(v => (math.signum(v._1) == math.signum(v._2))));
    }).sum;

    errorTotal / (set.length * 8);
  }
  
  def testDetail(set: IndexedSeq[BasicMLDataPair]) = {

    def isFront(index : Int) = (index == 0 || index == 7 || index == 3 || index == 4);  
    def getFront(data: Array[Double]) = data.zipWithIndex.filter(d => isFront(d._2)).map(d => d._1);
    def getSide(data: Array[Double]) = data.zipWithIndex.filter(d => !isFront(d._2)).map(d => d._1);
    
    var predictionPercentage = 0.0;
    var frontPredictionPercentage = 0.0;
    var squaredError = 0.0;
    var naiveSquaredError = 0.0;
    var frontSquaredError = 0.0;
    var frontNaiveSquaredError = 0.0;
    var sideSquaredError = 0.0;
    var sideNaiveSquaredError = 0.0;

    set.foreach(pair => {

      // get prediction
      val actual = pair.getIdealArray;
      val prediction = network.compute(pair.getInput).getData;

      val frontActual = getFront(actual);
      val frontPrediction = getFront(prediction);
      val sideActual = getSide(actual);
      val sidePrediction = getSide(prediction);
      
      // calculate percentage of correct distance change direction
      predictionPercentage += actual.zip(prediction).count(v => math.signum(v._1) == math.signum(v._2)) +
      	actual.count(a => a == 0) * 0.5;
      frontPredictionPercentage += frontActual.zip(frontPrediction).count(v => math.signum(v._1) == math.signum(v._2)) +
      	frontActual.count(a => a == 0) * 0.5;
      
      // calculate MSE in centimeters
      squaredError += actual.zip(prediction).map(v => (v._1 - v._2) * (v._1 - v._2)).sum;
      naiveSquaredError += actual.map(v => v * v).sum;
      frontSquaredError += frontActual.zip(frontPrediction).map(v => (v._1 - v._2) * (v._1 - v._2)).sum;
      frontNaiveSquaredError += frontActual.map(v => v * v).sum;
      sideSquaredError += sideActual.zip(sidePrediction).map(v => (v._1 - v._2) * (v._1 - v._2)).sum;
      sideNaiveSquaredError += sideActual.map(v => v * v).sum;
    });

    predictionPercentage /= set.length * 8;
    frontPredictionPercentage /= set.length * 4;
    val rmse = math.sqrt((squaredError * 100) / (set.length * 8)); // centimeters
    val naiveRmse = math.sqrt((naiveSquaredError * 100) / (set.length * 8)); // centimeters
    val frontRmse = math.sqrt((frontSquaredError * 100) / (set.length * 4)); // centimeters
    val frontNaiveRmse = math.sqrt((frontNaiveSquaredError * 100) / (set.length * 4)); // centimeters
    val sideRmse = math.sqrt((sideSquaredError * 100) / (set.length * 4)); // centimeters
    val sideNaiveRmse = math.sqrt((sideNaiveSquaredError * 100) / (set.length * 4)); // centimeters
    
    "%.3f;%.3f;%.3f;%.3f;%.3f;%.3f;%.3f;%.3f;".format(
        predictionPercentage, frontPredictionPercentage, rmse, naiveRmse,
        frontRmse, frontNaiveRmse, sideRmse, sideNaiveRmse);
  }


  /**
   * Predict the distance sensor difference values from given time series input
   */
  def predict(input: FrameSeries) : IndexedSeq[Double] = {
    network.compute(input.toMLData).getData.map(d => d / 10.0);
  }

}

object Predictor {

  def toDataPairs(frames: IndexedSeq[Frame], timeSteps: Int) = {

    val setCount = frames.length - timeSteps;
    val timeFrames = (0 to timeSteps).map(t => frames.drop(t).dropRight(timeSteps - t));
    val timeSet = (0 until setCount).map(s => timeFrames.map(v => v(s)));
    timeSet.map(v => new DataPair(new FrameSeries(v.take(timeSteps), v.last),
      v(timeSteps - 1).distances.zip(v(timeSteps).distances).map(d =>
        Common.limit((d._2 - d._1) * 10.0, -1.0, 1.0)
        )));
  }

}