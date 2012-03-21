package Corvid.Learning

import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing.Validation
import Corvid.Controller.AppException

/**
 * Calculates basic statistics from a data set of doubles
 */
class Stats(val count: Int,
            val min: Double,
            val max: Double,
            val average: Double,
            val variance: Double) {

  lazy val deviation = math.sqrt(variance);

  lazy val range = max - min;
  
  /* use t distribution with 60 degrees of freedom for 95% confidence interval */
  lazy val confidence = 
    if (count < 60)
      Double.PositiveInfinity;
    else 
    	(2 * deviation) / math.sqrt(count);  

  override def toString() =
    "min[%.2f] max[%.2f] avg[%.2f] var[%.2f]".format(
      min, max, average, variance);
  
  def toConfString() =
    "min[%.2f] max[%.2f] avg[%.2f +- %.2f] var[%.2f]".format(
      min, max, average, confidence, variance);

}

object Stats{

  def apply(data: Iterable[Double]) : Stats = {
    var max = -Double.MaxValue;
    var min = Double.MaxValue;
    var sum = 0.0;
    var count = 0;

    data.foreach(d => {
      if (max < d) max = d;
      if (min > d) min = d;
      sum += d;
      count += 1;
    });

    if (count == 0)
      return new Stats(0, 0, 0, 0, 0);

    val average = sum / count;

    if (count == 1)
      return new Stats(1, min, max, average, 0);

    val diffSum = data.map(d => { val diff = d - average; diff * diff; }).sum;
    val variance = diffSum / (count - 1);
    
    

    new Stats(count, min, max, average, variance);
  }

  def correlate(data1: Iterable[Double], data2: Iterable[Double]) = {
    val data1Stats = Stats(data1);
    val data2Stats = Stats(data2);
    if (data1Stats.count != data2Stats.count)
      throw new AppException("Counts do not match");

    val count = data1Stats.count;
    val multiSum = data1.zip(data2).map(d => d._1 * d._2).sum;
    val covariance = multiSum - count * data1Stats.average * data2Stats.average;
    covariance / ((count - 1) * data1Stats.deviation * data2Stats.deviation);
  }

}