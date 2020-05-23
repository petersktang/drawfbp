package com.jpaulmorrison.drawFBP.model

import javafx.geometry.BoundingBox
import javafx.scene.control.Tab

// Each DrawingBoard corresponds to a Tab.Content within the TabPane
class DrawingBoard(val fileName: String, val fbp: FBPNet, val tab: Tab,
                   val graph: com.google.common.graph.Graph[Int],
                  // captures a list of edges each is a separate Javafx.Line or Javafx.Path
                  // captures a list of blocks each represent as a Rectangle or something similar
                   var drawn:Boolean = false)