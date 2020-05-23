package com.jpaulmorrison.drawFBP

import javafx.geometry.{Bounds, Point2D}
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.shape.{Arc, Box, Circle, Rectangle, Shape}
import org.jgrapht.Graph
import com.jpaulmorrison.drawFBP.model.FBPBlock
import com.jpaulmorrison.drawFBP.shape.Arrow
import org.jgrapht.graph.DefaultEdge

object stencil {
  val list : Seq[String] = fruits

  private lazy val fbpList: Seq[String] = List("Process", "Initial IP", "Enclosure", "Subnet",
    "ExtPorts: In", "... Out", "... Out/In", "Legend", "File", "Person",
    "Report")

  private lazy val fruits: Seq[String] = List("Apple", "Orange", "Pineapple", "Grapes")

  private lazy val cadList: Seq[String] = List("Rect", "Circle", "Eclipse", "Line", "Text")

}
//
//object FBPVertex extends Enumeration {
//  val box = Box
//  val report = Rectangle
//  val source = Rectangle
//  val Sink = Rectangle
//  val arrow = Rectangle
//}
//
//object FBPEdge extends Enumeration {
//  val arc = Arc
//  val arrow = Arrow
//}
object FBPComponents {
  var graph : Graph[FBPComponent, DefaultEdge] = _
  def newCircle(center:Point2D, radius: Double) = new FBPCircle(new Circle(center.getX, center.getY, radius) )
}
class FBPCircle(delegate: Circle) extends  FBPComponent {
  var id:Int = 0
  var block:FBPBlock = _
  def center: Point2D = new Point2D(delegate.getCenterX, delegate.getCenterY)
  def radius: Double = delegate.getRadius
  def bounds: Bounds = delegate.getLayoutBounds

//  override def toBlock(): FBPBlock = {
//    FBPBlock((0, 0, id, "Circle", delegate.getRadius * 2, delegate.getRadius * 2, "Just a circle", false, "", false, false))
//  }

  register()
  // block = new FBPBlock()
  println("Stupid initialization")
}

trait FBPComponent {
  def id:Int
  def bounds:Bounds
  def register(): Unit = { println("You are so silly") }
  //def toBlock():FBPBlock
}