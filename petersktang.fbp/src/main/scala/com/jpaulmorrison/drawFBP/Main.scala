package com.jpaulmorrison.drawFBP

import javafx.animation.FadeTransition
import javafx.application.{Application, Platform}
import javafx.fxml.FXMLLoader
import javafx.stage.Stage
import javafx.scene.{Parent, Scene}
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.util.Duration
// import org.springframework.context.ConfigurableApplicationContext

object Main extends App {

  Application.launch(classOf[DrawFBPApp], args: _*)
}

class DrawFBPApp extends Application {

  private val javaVersion = System.getProperty("java.version")
  private val javafxVersion = System.getProperty("javafx.version")

  @throws[Exception]
  def start(primaryStage: Stage): Unit = {
    //startDrawFBP(primaryStage)
    startHelloWorld(primaryStage)
  }

  def startDrawFBP(primaryStage: Stage): Unit = {
    //val controller = new MainViewController(primaryStage)
    //val fxml = new FXMLLoader(getClass().getResource("/drawFBP/MainView.fxml"))
    //fxml.setController(controller)
    //primaryStage.setScene(new Scene(fxml.load))
    //primaryStage.setOnCloseRequest( _ => Platform.exit ) // seems better than System.exit(0)
    //primaryStage.show()
  }

  def startHelloWorld(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Hello World on javafx " + javafxVersion + " on java " + javaVersion)

    val hello = new Label("Hello!")
    val stackPane = new StackPane( hello )
    primaryStage.setScene( new Scene(stackPane, 800, 600) )
    primaryStage.show()
  }
}