package Corvid.Controller

import java.io._
import java.util.zip.{InflaterInputStream, DeflaterOutputStream}
import scala.xml.NodeSeq

/**
 * This class contains some common functions for re-use in other projects,
 * such as Serialization and Exception Handling
 */
object Common {

  /**
   * Runs a function in a separate thread
   */
  def runThread(runnable: => Unit) = {
    val thread = new Thread() {
      override def run() {
        runnable;
      }
    }
    thread.start();
    thread;
  }

  /**
   * Runs a function in a separate thread, printing all exceptions
   */
  def tryRun(runnable: => Unit) = {
    try
    {
      runnable;
    }
    catch
    {
      case ex => println(ex.toString()); 
    }
  }
    
  def limit(x : Double, min: Double, max : Double) : Double = {
    if (x < min) return min;
    if (x > max) return max;
    x;
  }

  def serialize(obj: AnyRef, filename: String) {
    using(new FileOutputStream(filename))(f => {
      using(new DeflaterOutputStream(f))(d => serialize(obj, d));
    });
  }

  def serialize(obj: AnyRef, stream: OutputStream) {
    using(new ObjectOutputStream(stream))(o => o.writeObject(obj));
  }

  def deserialize[T](filename: String) : T = {
    using(new FileInputStream(filename))(f => {
      using(new InflaterInputStream(f))(d => deserialize[T](d));
    });
  }

  def deserialize[T](stream: InputStream) : T = {
    using(new ObjectInputStream(stream))(o => o.readObject().asInstanceOf[T]);
  }

  def serialize(obj: AnyRef) : Array[Byte] = {
    val result = new ByteArrayOutputStream();
    serialize(obj, result);
    result.toByteArray;
  }

  def deserialize[T](array: Array[Byte]) : T = {
    deserialize(new ByteArrayInputStream(array));
  }

  def clone[T <: AnyRef](obj: T) : T = {
    deserialize(serialize(obj));
  }

  def using[T <: Closeable, R](c: T)(action: T => R): R = {
    try {
      action(c);
    } finally {
      if (null != c) c.close();
    }
  }

  def serialize(values : IndexedSeq[Double]) = {
    values.map(v => <val>{v}</val>);
  }

  def serialize(values : Seq[XMLSerializable]) = {
    values.map(v => <val>{v.toXML()}</val>);
  }
    
  def deserializeDoubles(node : NodeSeq) = {
    (node \ "val").map(n => n.text.toDouble).toIndexedSeq;
  }
  
}

trait XMLSerializable {
  def toXML() : NodeSeq
}

class Size[T <: AnyVal](val x : T, val y: T)
{
}

class AppException(message: String) extends Exception(message) {

  def this(format: String, args: Any*) = this(format.format(args));

}