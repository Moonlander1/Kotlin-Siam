package gameLogic
import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.stage.Stage
import rendering.GUI

class Game : Application() {


    private lateinit var mainScene: Scene
    private lateinit var batch: GraphicsContext
    private lateinit var gui: GUI
    private lateinit var canvas: Canvas




    // use a set so duplicates are not possible

    override fun start(mainStage: Stage) {

        val root = Group()
        mainScene = Scene(root)
        mainStage.scene = mainScene

        canvas = Canvas(975.0,650.0)
        root.children.add(canvas)

        prepareActionHandlers()

        batch = canvas.graphicsContext2D
        gui = GUI(batch,mainScene)

        // Main loop
        object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                tickAndRender()
            }
        }.start()

        mainStage.show()
    }

    private fun prepareActionHandlers() {
        mainScene.onMousePressed = EventHandler { event ->
            gui.inputs.mousePressed(event)
        }
        mainScene.onMouseMoved = EventHandler { event ->
            gui.inputs.mouseMoved(event)
        }
    }


    private fun tickAndRender() {
        // the time elapsed since the last frame, in nanoseconds
        // can be used for physics calculation, etc

        // clear canvas
        batch.clearRect(0.0, 0.0, mainScene.width, mainScene.height)

        // draw background
        updateCanvasSize()
        batch.fill = Color.SANDYBROWN
        batch.fillRect(0.0,0.0, mainScene.width, mainScene.height)
        // draw things
        gui.update()
    }

    private fun updateCanvasSize() {
        canvas.width = mainScene.width
        canvas.height = mainScene.height
    }
}
