package com.jpaulmorrison.drawFBP.util

import com.jpaulmorrison.drawFBP.model.{FBPNet, VertexCoordinate}
import com.google.common.graph.{GraphBuilder, MutableGraph}
import org.apache.tinkerpop.gremlin.structure.util.star.StarGraph
import org.jgrapht.Graph
import org.jgrapht.graph.{DefaultDirectedGraph, DefaultEdge, SimpleGraph}

object GraphImporter {

  def fromFBPtoGuava(fbp: FBPNet): com.google.common.graph.MutableGraph[Int] = {
    val mGraphGuava: MutableGraph[Int] = GraphBuilder.directed.build()
    fbp.connections.foreach{ connection =>
      mGraphGuava.putEdge(connection.fromId, connection.toId)
    }
    mGraphGuava
  }
  def fromFBPtoJGraphT(fbp: FBPNet): org.jgrapht.Graph[Int, DefaultEdge]  = {
    val dgraphJGraphT: Graph[Int, DefaultEdge] = new DefaultDirectedGraph[Int, DefaultEdge](classOf[DefaultEdge])
    fbp.blocks.forall{ block =>
      dgraphJGraphT.addVertex(block.id)
    }
    fbp.connections.foreach{ connection =>
      dgraphJGraphT.addEdge(connection.fromId, connection.toId)
    }
    dgraphJGraphT
  }
}