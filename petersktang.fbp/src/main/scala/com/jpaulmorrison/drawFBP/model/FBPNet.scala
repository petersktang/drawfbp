package com.jpaulmorrison.drawFBP.model

import javafx.geometry.BoundingBox

// refactored to separate pure FBPNet from coordinate concerns, i.e. VertexCoordinate and EdgeCoordinate

case class FBPNet(desc:String, complang: String, clicktogrid:Boolean, blocks:Seq[FBPBlock], connections:Seq[FBPConnection], vertexCoordinates: Map[Int, BoundingBox], edgeCoordinates: Seq[EdgeCoordinate] )

case class FBPBlock(id: Int,
                    `type`: String, description: String, multiplex:Boolean,
                    codeFileName: String, diagramFileName: String, blockClassname: String,
                    mpxFactor:Int, isSubnet:Boolean, substreamSensitive: Boolean,
                    invisible: Boolean,
                    subnetPorts:Seq[FBPSubnetPort]
                   )

case class VertexCoordinate(id: Int, x: Int, y: Int, width: Int, height: Int) {
  def toBoundingBox: BoundingBox = {
    new BoundingBox(x-width/2,y-height/2,width,height)
  }
}

case class FBPSubnetPort(name:String, y: Int, side: String, substreamSensitive:Boolean)

case class FBPConnection(fromId:Int, toId:Int, id:Int, upstreamPort:String, downstreamPort:String, segno:Int, dropOldest:Boolean, endsAtLine:Boolean)

case class EdgeCoordinate(id:Int, fromX:Int, fromY:Int, toX:Int, toY:Int, bends: Seq[FBPBend])

case class FBPBend(x:Int, y:Int)