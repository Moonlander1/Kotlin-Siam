package rendering
import com.sun.javafx.tk.Toolkit
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text.Font


class MyButton(
    private var x: Double,private var y: Double,
    private var w: Double,private var h: Double,
    private val text: String = "", private val img: Image? = null) {
    /* VÁLTOZÓK */
    lateinit var fillColor: Color
    lateinit var hoverColor: Color
    lateinit var clickedColor: Color
    lateinit var textColor: Color
    val actionCommand = text
    var hovered = false
    var visible = false

    /* FÜGGVÉNYEK */
    fun pointInsideButton(a: Double, b: Double): Boolean = (x <= a && a < x + w && y <= b && b < y + h)
    fun draw(batch: GraphicsContext) {
        if(visible) {
            batch.fill = Color.BLACK
            batch.fillRect(x - 2, y - 2, w + 4, h + 4)

            batch.fill = if (hovered) hoverColor else fillColor
            batch.fillRect(x, y, w, h)

            val metrics = Toolkit.getToolkit().fontLoader.getFontMetrics(batch.font)
            var stringWidth = 0.0f
            for (i in text) stringWidth += metrics.getCharWidth(i)

            drawImage(batch)

            batch.font = Font(20.0)
            batch.fill = textColor
            batch.fillText(text, x + (w - stringWidth) / 2, y + 23.0 + (h - 23.0) / 2)
        }
    }
    private fun drawImage(batch: GraphicsContext) {
        val diff = when(actionCommand) {
            "Up" -> 200.0
            "Down" -> 600.0
            "Right" -> 400.0
            else -> 0.0
        }
            batch.drawImage(img,0 + diff,0.0,200.0,200.0,x,y,w,h)
    }

    fun reSize() {
        x *= GUI.widthOffset
        y *= GUI.heightOffset
        w = if(actionCommand == "Cancel") 3 * GUI.tileS + 4 * GUI.gridS * GUI.widthOffset
        else GUI.tileS
        h = GUI.tileS
    }

}