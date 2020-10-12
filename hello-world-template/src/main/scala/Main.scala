import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Color

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props


trait Helper {

  // sequential filter
  def runFilterV1 (file: String) {

    val startTimeMillis = System.currentTimeMillis()

    val img = ImageIO.read(new File(file))

    val w = img.getWidth
    val h = img.getHeight

    applyMedianFilter(img, w, h, 0, 0)

    ImageIO.write(img,"png",new File("output.png"))

    val endTimeMillis = System.currentTimeMillis()
    val durationSeconds = (endTimeMillis - startTimeMillis) // / 1000
 
    //println(startTimeMillis)
    //println(endTimeMillis)

    println("Actor 1 completed in " + durationSeconds + " seconds")
  }

  // Concurrent filter
  def runFilterV2 (file: String) {

    val startTimeMillis = System.currentTimeMillis()

    val img = ImageIO.read(new File(file))

    val w = img.getWidth
    val h = img.getHeight
    
    val fut1 = Future{
    
      // first half
      applyMedianFilter(img, w/2, h, 0, 0)

      // second half
      //applyMedianFilter(img, w, h, w/2, 0)

    }

    val fut2 = Future{
    
      // first half
      //applyMedianFilter(img, w/2, h, 0, 0)

      // second half
      applyMedianFilter(img, w, h, w/2, 0)

    }


    val aggFut = for{
      f1Result <- fut1
      f2Result <- fut2
    } yield (f1Result, f2Result)

    aggFut onComplete {
      case Success(idx) => {
        
        ImageIO.write(img,"png",new File("output2.png"))
        val endTimeMillis = System.currentTimeMillis()
        val durationSeconds = (endTimeMillis - startTimeMillis) // / 1000
        
        //println(startTimeMillis)
        //println(endTimeMillis)

        println("Actor 2 completed in " + durationSeconds + " seconds")
        }
      case Failure(t) => println("Could not process file: " + t.getMessage)
    }

  }


  def applyMedianFilter(img: BufferedImage, w: Int, h: Int, sw: Int, sh: Int) = {
    //for (x <- 0 until w) {
    // for (y <- 0 until h) {
    
    for (x <- sw until w) {
      for (y <- sh until h) {

        val pixel = img.getRGB(x,y)

        // Todo, add to check within range instead of try/catch
        try {
          // Left side
          val topL = img.getRGB(x-1,y+1)
          val midL = img.getRGB(x-1,y)
          val lowL = img.getRGB(x-1,y-1)

          // Middle side, excluding middle pixel
          val topM = img.getRGB(x,y+1)
          val lowM = img.getRGB(x,y-1)

          // Right side
          val topR = img.getRGB(x+1,y+1)
          val midR = img.getRGB(x+1,y)
          val lowR = img.getRGB(x+1,y-1)

          // Place values in an array
          var myArr = Array(topL, midL, lowL, topM, pixel, lowM, topR, midR, lowR)

          // Get median
          val median = getMedian(myArr)

          // Set pixel to median
          img.setRGB(x,y, median)
          //println("Changed pixel")
        } catch {
          case e:Exception=>
            //println("Skipped pixel!!")
        }

      }
    }
  }



  def getMedian(arr: Array[Int]): Int = {  
    
    // We only expect to view a 3x3 matrix resulting an array of length 9
    if(arr.length != 9) {
      throw new ArithmeticException("Array must be of length 9")
    }

    // Clone given array
    var cloneArr = arr.clone

    // Sort clone array
    scala.util.Sorting.quickSort(cloneArr)

    // Return median value of clone array
    cloneArr((arr.length/2))  
  } 
}

// (1) changed the constructor here
class HelloActor(file: String) extends Actor with Helper {

  def receive = {
    // (2) changed these println statements
    case "filter"   => runFilterV1(file)
    case _          => println("'huh?', said %s".format(file))
  }

}

// (1) changed the constructor here
class HelloActor2(file: String) extends Actor with Helper {

  def receive = {
    // (2) changed these println statements
    case "filter"   => runFilterV2(file)
    case _          => println("'huh?', said %s".format(file))
  }

}

object Main extends App {

  val myFile = "grainy.png"

  val system = ActorSystem("HelloSystem")

  // (3) changed this line of code
  val helloActor = system.actorOf(Props(new HelloActor(myFile)), name = "helloactor")

  val helloActor2 = system.actorOf(Props(new HelloActor2(myFile)), name = "helloactor2")

  helloActor ! "filter"
  helloActor2 ! "filter"
}





//def getMedian(arr: Array[Int]): Int = {} 