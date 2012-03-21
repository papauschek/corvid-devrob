package Corvid.Learning
import java.io.File
import scala.collection.mutable.ArrayBuffer
import scala.xml.XML
import Corvid.Controller._
import scala.util.Random
import scala.collection.mutable.ListBuffer
import java.io.FileWriter
import java.util.Date

/**
 * The Evaluation object contains routines for calculating the average reward 
 * from previously recorded experiments, including statistics such as confidence 
 * intervals. It also contains the routine used to evaluate the neural network 
 * prediction performance after training with different amounts of sensomotoric 
 * data.
 */
object Evaluation {
  
  
  def testRewards()
  {
    val folder = new File("EvaluationData/");
    val files = folder.listFiles().filter(f => f.isFile);

    files.foreach(file => {
      val fileList = XML.loadFile(file.getAbsolutePath).child.elements.toSeq;
      val fileFrames = fileList.map(f => FrameReward.fromXML(f));

      printf("%s: loaded total %d data pairs\r\n", file.getName(), fileFrames.length);
      printf("rewards: %s\r\n", Stats(fileFrames.map(f => f.reward)).toConfString);
    });   
  }
  
  def testGeneralization()
  {
    // load data pairs for training
    val timeSteps = 2;
    val dataPairs = SensorModel.loadDataPairs(timeSteps);
    val writer = new FileWriter("generalizationResults.txt", true);
    writer.write("\r\nStarted: %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS\r\n".format(new Date()));
    printf("using %d data pairs\r\n", dataPairs.length);

		// vary training set size
		val result = Seq(0, 50, 500, 2500, 10000).map(trainingSetCount => {
	
		  // vary hidden layer count
		  val entries = Seq(1).map(hiddenLayerCount => {
	
		  	// vary hidden neuron count
		  	Seq(4, 6, 8, 10, 12, 14, 16, 18, 20).map(hiddenNeuronCount => {
	
		  		// Create and test neural network predictor
		  		val predictor = new Predictor(timeSteps, hiddenLayerCount, hiddenNeuronCount);
		  		val error = predictor.train(dataPairs, trainingSetCount);
		  		val details = predictor.testDetail(dataPairs.map(d => d.toMLDataPair));
		  		val entry = "%d;%d;%d;%s".format(trainingSetCount, hiddenLayerCount, hiddenNeuronCount, details);
		  		printf("entry finished: %s\r\n", entry);
		  		writer.write("entry finished: %s\r\n".format(entry));
		  		writer.flush();
		  		(error, entry);	
		  	});
		  	
		  }).flatten.sortBy(s => s._1);
		  
		  // print best result for set size
		  printf("best entry: %s\r\n", entries(0));
		  writer.write("best entry: %s\r\n".format(entries(0)));
		  writer.flush();
		  entries;
		 		  
		});
			
		// print all results
		writer.write("all results:\r\n");
		result.foreach(r => r.foreach(e => {
			printf("%s\r\n", e._2);
			writer.write("%s\r\n".format(e._2));
		}));
		
		writer.close();
	
  }
  


    
}