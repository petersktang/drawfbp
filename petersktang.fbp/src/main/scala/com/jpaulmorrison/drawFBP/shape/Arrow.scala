package com.jpaulmorrison.drawFBP.shape

import javafx.scene.paint.Color
import javafx.scene.shape.{LineTo, MoveTo, Path}

object Arrow {
  private val defaultArrowHeadSize = 5.0
}

class Arrow(val startX: Double, val startY: Double, val endX: Double, val endY: Double, val arrowHeadSize: Double) extends Path {
  strokeProperty.bind(fillProperty)
  setFill(Color.BLACK)
  //Line
  getElements.add(new MoveTo(startX, startY))
  getElements.add(new LineTo(endX, endY))
  //ArrowHead
  val angle: Double = Math.atan2(endY - startY, endX - startX) - Math.PI / 2.0
  val sin: Double = Math.sin(angle)
  val cos: Double = Math.cos(angle)
  //point1
  val x1: Double = (-1.0 / 2.0 * cos + Math.sqrt(3) / 2 * sin) * arrowHeadSize + endX
  val y1: Double = (-1.0 / 2.0 * sin - Math.sqrt(3) / 2 * cos) * arrowHeadSize + endY
  //point2
  val x2: Double = (1.0 / 2.0 * cos + Math.sqrt(3) / 2 * sin) * arrowHeadSize + endX
  val y2: Double = (1.0 / 2.0 * sin - Math.sqrt(3) / 2 * cos) * arrowHeadSize + endY
  getElements.add(new LineTo(x1, y1))
  getElements.add(new LineTo(x2, y2))
  getElements.add(new LineTo(endX, endY))

  def this(startX: Double, startY: Double, endX: Double, endY: Double) = {
    this(startX, startY, endX, endY, Arrow.defaultArrowHeadSize)
  }
}