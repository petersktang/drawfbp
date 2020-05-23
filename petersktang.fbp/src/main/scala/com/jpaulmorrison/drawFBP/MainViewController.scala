package com.jpaulmorrison.drawFBP

import java.io.File
import java.net.URL

import javafx.application.Platform
import javafx.event.{ActionEvent, Event}
import javafx.fxml.FXML
import javafx.geometry.{BoundingBox, Point2D}
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.TabPane.TabDragPolicy
import javafx.scene.control._
import javafx.scene.input.{MouseEvent, ZoomEvent}
import javafx.scene.layout.{AnchorPane, Pane, StackPane, VBox}
import javafx.scene.shape.{Line, Rectangle}
import javafx.scene.text.Text
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.{FileChooser, Stage}

import scala.collection.mutable
import com.google.common.graph.{Graph, ImmutableGraph, MutableGraph}
import com.jpaulmorrison.drawFBP.layoutAlgo.FruchtermanReingoldLayout
import com.jpaulmorrison.drawFBP.model._
import com.jpaulmorrison.drawFBP.util._
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

// Examples: https://examples.javacodegeeks.com/core-java/javafx-layout-example/?fbclid=IwAR2hTFYafT8ek4xf2dnKRf9uoQ5qLdumcHGrflOzAzvYI5TDNvckYv7iasw#region

class MainViewController(private val primaryStage: Stage, var fileName: String = "Untitled") {

  @FXML protected var location: URL = _
  @FXML protected var menuBar: MenuBar = _
  @FXML protected var quitMenuItem: MenuItem = _

  @FXML protected var anchorPane: AnchorPane = _
  @FXML protected var zoomSlider: Slider = _
  @FXML protected var stackPane: StackPane = _
  @FXML protected var stencilBar: VBox = _
  private var dragText: Text = _

  @FXML protected var pleaseOpenDrawing: Text = _
  @FXML protected var tabPane: TabPane = _
  protected val openedFBPNet = new mutable.LinkedHashMap[Tab, DrawingBoard]()

  //val fbpLayoutWorker: AsyncSubject[(FBPNet, Tab)]  = new AsyncSubject[(FBPNet, Tab)].subscribeOn()

  @FXML private def initialize(): Unit = {
    primaryStage.setTitle(s"Diagram: $fileName")
    menuBar.setUseSystemMenuBar(true)
    quitMenuItem.setOnAction{ _ => Platform.exit() }
    setupStencilControls()
    tabPane.setTabDragPolicy(TabDragPolicy.REORDER)
    configureAsyncWorkListener()
  }

  def setupStencilControls() = {
    val stencils = stencil.list.map{ item  =>
      val button = new Button(item)
      button.setMaxWidth(Double.MaxValue)
      button.setOnMousePressed( handleDragTransfer )
      button.setOnMouseDragged( handleDragTransfer )
      button.setOnMouseReleased{ handleDragTransfer }
      button
    }
    val maxWidth = stencil.list.map{ item => (new Text(item)).getBoundsInLocal.getWidth }.max
    stencilBar.getChildren.addAll(stencils:_*)
    stencilBar.setPrefWidth(maxWidth+20)

    dragText = new Text()
    dragText.setVisible(false)
    anchorPane.getChildren.add(dragText)
    dragText.toFront()

    dragText.setOnMousePressed{ handleDragDetected }
    dragText.setOnMouseDragged{ handleMouseDragged }
    dragText.setOnMouseReleased{ handleMouseReleased }

    AnchorPane.setRightAnchor(stackPane, maxWidth+22)
    AnchorPane.setTopAnchor(stackPane, 10.0)
    AnchorPane.setBottomAnchor(stackPane, 10.0)
    stencilBar.setVisible(false)
  }

  def handleDragTransfer(event: MouseEvent) = {
    val button = event.getSource.asInstanceOf[Button]
    if (event.getEventType == MouseEvent.MOUSE_PRESSED) {
      if (button.getFont != dragText.getFont) {
        dragText.setFont(button.getFont)
      }
      dragText.setText(button.getText)
      dragText.setVisible(true)
      val bCenter = button.localToScene(new Point2D(button.getLayoutBounds.getCenterX, button.getLayoutBounds.getCenterY))
      val bCenterToLocal = dragText.getParent.sceneToLocal(bCenter.getX - dragText.getLayoutBounds.getWidth / 2, bCenter.getY - dragText.getLayoutBounds.getHeight / 2)
      dragText.relocate(bCenterToLocal.getX, bCenterToLocal.getY)
      //IMPORTANT: below triggers dragText.handleDragDetected(event)
      dragText.fireEvent(event.copyFor(dragText, event.getTarget))

    } else if (event.getEventType == MouseEvent.MOUSE_DRAGGED) {
      //IMPORTANT: below triggers dragText.handleDMouseDragged(event)
      dragText.fireEvent(event.copyFor(dragText, event.getTarget))
    } else if (event.getEventType == MouseEvent.MOUSE_RELEASED) {
      //IMPORTANT: below triggers dragText.handleMouseReleased(event)
      dragText.fireEvent((event.copyFor(dragText, event.getTarget)))
    }
    event.consume()
  }

  var lastXY: Point2D = null
  def handleDragDetected(event: MouseEvent) = {
    lastXY = new Point2D(event.getSceneX, event.getSceneY)
    event.consume()
  }

  def handleMouseDragged(event: MouseEvent) = {
    event.setDragDetect(false)
    val source = event.getSource.asInstanceOf[Node]
    if (source.isVisible) {
      val dx = event.getSceneX - lastXY.getX
      val dy = event.getSceneY - lastXY.getY
      source.setLayoutX(source.getLayoutX + dx)
      source.setLayoutY(source.getLayoutY + dy)
      lastXY = new Point2D(event.getSceneX, event.getSceneY)
    }
    event.consume()
  }

  def handleMouseReleased(event: MouseEvent) = {
    val source = event.getSource.asInstanceOf[Node]
    if (source ==  dragText) {
      source.setVisible(false)
    }
    lastXY = null
    event.consume()
  }

  def handleFileOpen(event: ActionEvent) = {
    val fc = new FileChooser()
    fc.setTitle("Open FBP Drawing File")
    fc.getExtensionFilters.addAll( new ExtensionFilter("Drawing Files", "*.drw") )
    var tab: Tab = null
    var alternate:Boolean = true
    fc.showOpenMultipleDialog(primaryStage).forEach( (file: File) =>
      if (file != null && file.isFile && !openedFBPNet.values.exists( _.fileName == file.getCanonicalPath)) {
        fileName = file.getCanonicalPath
        primaryStage.setTitle(s"Diagram: $fileName")
        val fbp = DrwReader.toFbpNet(file)
        val graph = GraphImporter.fromFBPtoGuava(fbp)  //val graph2 = GraphImporter.fromFBPtoJGraphT(fbp)
        val canvas = new Canvas(tabPane.getWidth, tabPane.getHeight - tabPane.getTabMaxHeight - 7)
        val pane = new Pane(canvas)
        tab = new Tab(file.getName, pane)
        tabPane.getTabs.add(tab)

        tab.setOnClosed( e => handleTabOnClose(e) )
        tab.setOnSelectionChanged( e => handleSelectionChanged(e) )

        alternate = !alternate

        val board = new DrawingBoard(fileName, fbp, tab, graph)
        openedFBPNet.addOne(tab, board)
        stencilBar.setVisible(true)
        pleaseOpenDrawing.setVisible(false)
        computeLayoutCombined.onNext(ForceComputedLayout(GraphImporter.fromFBPtoGuava(fbp), board.fbp.vertexCoordinates, board.fbp.edgeCoordinates, tab, pane, canvas, true))
      }
    )
    event.consume()
  }

  //FIXME: Pane and Canvas should not be included in ForceComputedLayout, and should just pick up before drawing.
  case class ForceComputedLayout(graph:MutableGraph[Int],  vertexCoordinates: Map[Int, BoundingBox], edgeCoordinates: Seq[EdgeCoordinate], tab: Tab, pane: Pane, canvas: Canvas, invalidate: Boolean = false)
  val computeLayoutCombined : PublishSubject[ForceComputedLayout] = PublishSubject.create()

  def configureAsyncWorkListener():Unit = {
    computeLayoutCombined.observeOn(Schedulers.computation).map { event:ForceComputedLayout =>
      computeForceAlgorithmLayout(event)
    }.observeOn( JavaFxScheduler.platform() ).subscribe({ e:ForceComputedLayout =>
      // should break down into individual javafx.Node relocation rather than a complete redraw
      // it will then be faster, changes more incremental and UI changes be smoother
      if (e.invalidate) {
        this.redrawLayout(e.vertexCoordinates, e.edgeCoordinates, e.tab, e.pane, e.canvas, e.invalidate)
      } else {

      }
    })
  }

  def computeForceAlgorithmLayout(layout:ForceComputedLayout): ForceComputedLayout = {
    // put in Force-based graph layout algorithm here. e.g. F
    val r = FruchtermanReingoldLayout.goAlgo(layout.graph, layout.vertexCoordinates)
    val l = layout.vertexCoordinates.toList.map( v => (v._1, v._2.getCenterX, v._2.getCenterY) )
    println("layout: ", l)
    println("result: ", r)
    layout
  }

  def redrawLayout(vertexCoordinates: Map[Int, BoundingBox], edgeCoordinates: Seq[EdgeCoordinate], tab: Tab, pane: Pane, canvas: Canvas, invalidate: Boolean) = {
    // println("redrawLayout: ", vertexCoordinates, pane, canvas)
    val width = pane.getWidth; val height = pane.getHeight
    openedFBPNet.get(tab) match {
      case Some(board) =>
        if (!board.drawn || invalidate) {
          board.drawn = true
          val lines = board.fbp.edgeCoordinates.map({ c =>  new Line(c.fromX, c.fromY, c.toX, c.toY) })
          val blocks = board.fbp.vertexCoordinates.toList.map({ b =>
            (new Rectangle(b._2.getWidth, b._2.getHeight), new Point2D(b._2.getMinX, b._2.getMinY))
          })
          pane.getChildren.removeAll()
          val canvas = new Canvas(tabPane.getWidth, tabPane.getHeight - tabPane.getTabMaxHeight - 7)
          pane.getChildren.add(canvas)
          pane.getChildren.addAll(lines: _*)
          pane.getChildren.addAll( blocks.unzip._1 : _*)
          blocks.foreach({ b =>
            b._1.relocate(b._2.getX, b._2.getY )
            b._1.setOnMousePressed({ handleDragDetected })
            b._1.setOnMouseDragged({ handleMouseDragged })
            b._1.setOnMouseReleased({ handleMouseReleased })
          })
        }
      case _ =>
    }
  }


  def handleTabOnClose(event: Event): Unit = {
    val tab = event.getSource.asInstanceOf[Tab]
    // separate out model changes from UI changes
    openedFBPNet.remove(tab)
    if (openedFBPNet.values.toList.length <= 0) {
      pleaseOpenDrawing.setVisible(true)
      stencilBar.setVisible(false)
    }
    event.consume()
  }

  def handleSelectionChanged(event: Event): Unit = {
    val tab = event.getSource.asInstanceOf[Tab]
    openedFBPNet.get(tab) match {
      case Some(board) =>
        val pane = tab.getContent.asInstanceOf[Pane]
        val canvas = pane.getChildren.get(0).asInstanceOf[Canvas]
        val graph = GraphImporter.fromFBPtoGuava(board.fbp)
        computeLayoutCombined.onNext(ForceComputedLayout(graph, board.fbp.vertexCoordinates, board.fbp.edgeCoordinates, tab, pane, canvas, false))
      case _ =>
    }
  }

  def handleFileClose(event: ActionEvent) ={
  }

  def zoomAction(event: ZoomEvent) = {
    event.consume()
  }

  def pad(s: String, width: Int, ch: String) = {
    val left = (width - s.length) / 2
    s.prependedAll(ch*left)
  }

  @FXML def handleAbout(event: ActionEvent): Unit = {
    import com.jpaulmorrison.graphics.VersionAndTimestamp
    val alert = new Alert(AlertType.INFORMATION)
    alert.setTitle("About")
    alert.setHeaderText(VersionAndTimestamp.NAME)
    alert.setContentText(s"""
        |${VersionAndTimestamp.NAME} v${VersionAndTimestamp.VERSION}
        |
        |Authors: J.Paul Rodker Morrison,
        |Bob Corrick
        |
        |Copyright 2009, ..., 2019
        |
        |FBP web site: www.jpaulmorrison.com/fbp
        |
        |(${VersionAndTimestamp.DATE})
        |""".stripMargin.split('\n')
      .map{ line => pad(line, 60, " ") }.mkString("\n")
    )
    //val stage = alert.getDialogPane.getScene.getWindow
    alert.initOwner(primaryStage)
    alert.showAndWait
  }

  private[MainViewController] def showErrorDialog(title: String, msg: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setTitle("Error")
    alert.setHeaderText(title)
    alert.setContentText(msg)
    alert.initOwner(primaryStage)
    alert.showAndWait
  }

}
