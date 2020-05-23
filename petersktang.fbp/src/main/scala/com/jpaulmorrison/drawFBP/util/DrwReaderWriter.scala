package com.jpaulmorrison.drawFBP.util

import java.io.File

import com.jpaulmorrison.drawFBP.model.{EdgeCoordinate, FBPBend, FBPBlock, FBPConnection, FBPNet, FBPSubnetPort, VertexCoordinate}

import scala.xml.{Elem, XML}

object DrwReader {

  def toFbpNet(file: File): FBPNet = {
    val xml = XML.loadFile(file)
    val desc =  ( xml  \ "net"  \ "desc" ).text
    val complang = ( xml \ "net"  \ "complang" ).text
    val clicktogrid = (xml \ "net"  \  "clicktogrid").text.toBoolean

    val _blocks = (xml \ "net" \ "blocks" \ "block").map{ block =>
      val x = (block \ "x").text.trim.toInt
      val y = (block \ "y").text.trim.toInt
      val width = (block \ "width").text.trim.toInt
      val height = (block \ "height").text.trim.toInt

      val id = (block \ "id").text.trim.toInt
      val `type` = (block \ "type").text
      val description = (block \ "description").text
      val blockClassname = (block \ "blockclassname").text
      val multiplex = (block \ "multiplex").text.toBooleanOption.getOrElse(false)
      val invisible = (block \ "invisible").text.toBooleanOption.getOrElse(true)
      val codeFileName = ( block \ "codefilename").text
      val diagramFileName = ( block \ "diagramfilename").text
      val mpxFactor = ( block \ "mpxfactor").text.toIntOption.getOrElse(0)
      val isSubnet = (block \ "issubnet").text.toBooleanOption.getOrElse(false)
      val substreamSensitive = ( block \ "substreamsensitive").text.toBooleanOption.getOrElse(false)
      val subnetPorts = (block \ "subnetports" \ "subnetport").map{ subnetPort =>
        val name = (subnetPort \ "name").text
        val y = (subnetPort \ "y" ).text.toIntOption.getOrElse(0)
        val side = (subnetPort \ "side").text
        val substreamSensitive = (subnetPort \ "substreamsensitive").text.toBooleanOption.getOrElse(false)
        FBPSubnetPort(name, y, side, substreamSensitive)
      }
      ( VertexCoordinate(id, x, y, width, height),

        FBPBlock(id, `type`, description, multiplex, codeFileName, diagramFileName, blockClassname,
        mpxFactor, isSubnet, substreamSensitive, invisible, subnetPorts
      ))
    }.toList.unzip

    val _connections = (xml \ "net" \ "connections" \ "connection").map{ connection =>
      val fromX = (connection \ "fromx").text.trim.toInt
      val fromY = (connection \ "fromy").text.trim.toInt
      val toX = (connection \ "tox").text.trim.toInt
      val toY = (connection \ "toy").text.trim.toInt

      val id = (connection \ "id").text.trim.toInt
      val fromId = (connection \ "fromid").text.trim.toInt
      val toId = (connection \ "toid").text.trim.toInt
      val endsAtLine = (connection \ "endsatline").text.toBoolean
      val upstreamPort = (connection \ "upstreamport").text
      val downstreamPort = (connection \ "downstreamport").text
      val segno = (connection \ "segno").text.toIntOption.getOrElse(0)
      val dropOldest = (connection \ "dropoldest").text.toBooleanOption.getOrElse(false)
      val bends = (connection \ "bends" \ "bend").map{ bend =>
        val x = (bend \ "x").text.trim.toInt
        val y = (bend \ "y").text.trim.toInt
        FBPBend(x,y)
      }

      ( EdgeCoordinate(fromId, fromX, fromY, toX, toY, bends),
        FBPConnection(fromId, toId, id, upstreamPort, downstreamPort, segno, dropOldest, endsAtLine)
      )
    }.toList.unzip

    val blocks = _blocks._2
    val connections = _connections._2
    val blockCoords = _blocks._1.map{ blockCoord => (blockCoord.id, blockCoord.toBoundingBox) }.toMap
    val connectionCoords = _connections._1

    FBPNet(desc, complang, clicktogrid, blocks, connections, blockCoords, connectionCoords)
  }
}

object DrwWriter {
  private def subnetPortToElem(subnetPort: FBPSubnetPort): Elem = {
    <subnetport>
      <name>{subnetPort.name}</name>
      <y>{subnetPort.y}</y>
      <side>{subnetPort.side}</side>
      <substreamsensitive>{subnetPort.substreamSensitive}</substreamsensitive>
    </subnetport>
  }
  private def blockToElem(block:FBPBlock, coord: VertexCoordinate): Elem = {
    val xml = <block>
      <x>{coord.x}</x><y>{coord.y}</y><width>{coord.width}</width><height>{coord.height}</height>
      <id>{block.id}</id>
      <type>{block.`type`}</type>
      <description>{block.description}</description>
      <multiplex>{block.multiplex}</multiplex>
      <codefilename>{block.codeFileName}</codefilename>
      <diagramfilename>{block.diagramFileName}</diagramfilename>
      <blockclassname>{block.blockClassname}</blockclassname>
      <mpxfactor>{block.mpxFactor}</mpxfactor>
      <invisible>{block.invisible}</invisible>
      <issubnet>{block.isSubnet}</issubnet>
      <substreamsensitive>{block.substreamSensitive}</substreamsensitive>
      <subnetports>{block.subnetPorts.map{p => subnetPortToElem(p)}}</subnetports>
    </block>
    xml
  }

  private def bendToElem(bend:FBPBend): Elem = {
    <bend><x>{bend.x}</x><y>{bend.y}</y></bend>
  }

  private def connectionToElem(connection: FBPConnection, coord: EdgeCoordinate): Elem = {
    val xml = <connection>
      <fromx>{coord.fromX}</fromx>
      <fromy>{coord.fromY}</fromy>
      <tox>{coord.toX}</tox>
      <toy>{coord.toY}</toy>
      <fromid>{connection.fromId}</fromid>
      <toid>{connection.toId}</toid>
      <id>{connection.id}</id>
      <seqno>{connection.segno}</seqno>
      <upstreamport>{connection.upstreamPort}</upstreamport>
      <downstreamport>{connection.downstreamPort}</downstreamport>
      <dropoldest>{connection.dropOldest}</dropoldest>
      <endsAtLine>{connection.endsAtLine}</endsAtLine>
      <bends>{coord.bends.map{ b => bendToElem(b)}}</bends>
    </connection>
    xml
  }

  def fbpNetToElem(fbpNet: FBPNet) : Elem = {
    val _blocks = fbpNet.blocks.sortBy(_.id)
    val _bcoords = fbpNet.vertexCoordinates.toList.map{ vc => VertexCoordinate(vc._1,vc._2.getMinX.toInt,vc._2.getMinY.toInt,(vc._2.getMaxX-vc._2.getMinX).toInt,(vc._2.getMaxY-vc._2.getMinY).toInt)}.sortBy(_.id)
    val zBlocks: Seq[(VertexCoordinate, FBPBlock)] = _bcoords.zip(_blocks)
    val _connections = fbpNet.connections.sortBy(_.id)
    val _ecoords = fbpNet.edgeCoordinates.sortBy(_.id)
    val zConnections: Seq[(EdgeCoordinate,FBPConnection)] = _ecoords.zip(_connections)
    if (_blocks.map{ _.id } != _bcoords.map{ _.id } || _connections.map{_.id} != _ecoords.map{_.id}) {
      println("Huge problem, model inconsistent")
    }
    val xml = <drawfbp_file xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:noNamespaceSchemaLocation="https://github.com/jpaulm/drawfbp/blob/master/lib/drawfbp_file.xsd">
      <net>
        <desc>{fbpNet.desc}</desc>
        <complang>{fbpNet.complang}</complang>
        <clicktogrid>{fbpNet.clicktogrid}</clicktogrid>
        <blocks>{ zBlocks.map{ pair => val (c, b) = pair; blockToElem(b,c) } }</blocks>
        <connections>{ zConnections.map{ pair => val (coord,connection) = pair; connectionToElem(connection, coord) } }</connections>
      </net>
    </drawfbp_file>
    xml
  }

}