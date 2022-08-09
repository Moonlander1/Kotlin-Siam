package rendering

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import gameLogic.Map
import gameLogic.GameObj
import gameLogic.Piece
import gameLogic.Square
import getResource
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.image.Image
import javafx.scene.image.ImageView

class GUI(private val batch: GraphicsContext,private val mainScene: Scene) {
    private val playingField = Map()
    val inputs: InputHandler = InputHandler(playingField,batch)
    private var benched = 0
    private var totalBenched = 0
    private var firstTime = true
    private val bench:  Image = Image(getResource("/bench.png"))
    private val bench2: Image = Image(getResource("/bench2.png"))

    companion object {
        var widthOffset = 0.0
        var heightOffset = 0.0
        var tileS:  Double = 0.0
        var baseX:  Double = 0.0
        var baseY:  Double = 0.0
        var baseW:  Double = 0.0
        var baseH:  Double = 0.0
        var width:  Double = 0.0
        var height: Double = 0.0
        var gridS:  Double = 0.0
        var sepaX:  Double = 0.0
        var littlePortion: Double = 0.0
        val pastelYellow: Color = Color.rgb(253,253,150,1.0)
        val pastelGreen: Color  = Color.rgb(190,231,176,1.0)
        val pastelRed: Color    = Color.rgb(255,105,97 ,1.0)
        val darkBlue: Color     = Color.rgb( 18,77 ,120,1.0)
    }

    fun update() {
        getWindowRelatedSizes()
        if(firstTime)
            firstInit()
        drawSeparator()
        drawGrid()
        drawObjects()
        drawInputs()
        batch.fill = Color.BLACK
    }
    private fun getWindowRelatedSizes() {
        if(width != mainScene.width || height != mainScene.height) {
            widthOffset = mainScene.width / width
            heightOffset = mainScene.height / height
            width = mainScene.width
            height = mainScene.height
            baseW = mainScene.height * 600 / 650
            baseH = mainScene.height * 600 / 650
            baseX = (width - width * 1 / 3 - baseW) / 2.0
            baseY = mainScene.height * 25 / 650
            gridS = 5.0
            tileS = ((baseW * 2 / 3) - (4 * gridS)) / 5
            sepaX = width * 2 / 3
            littlePortion = ((baseH - (tileS * 5 + gridS * 4)) / 2 - tileS) / 2
            if(!firstTime) {
                updateButtons()
                updateMap()
            }

        }
    }
    private fun updateMap() {
        for(p in playingField.players) for(gO in p.value.pieces) {
            gO.calculateDrawLoc()
        }
        for(r in playingField.rocks)
            r.calculateDrawLoc()
        for(t in playingField.tiles)
            t.reSize()
    }

    private fun updateButtons() {
        for(b in inputs.allButtons)
            b.reSize()
    }
    private fun drawSeparator() {
        batch.fill = Color.BLACK
        batch.fillRect(sepaX,0.0,1.0,GUI.height)
    }

    private fun drawGrid() {
        batch.fill = Color.SKYBLUE
        batch.fillRect(0.0,0.0,sepaX,height)
        batch.drawImage(bench,baseX+baseW/6-40,
        0.0,5 * tileS + 4 * gridS+80,1.5*gridS+tileS+30)
        batch.drawImage(bench2,baseX + baseW / 6 - 40,
            height - tileS - 1.5 * gridS-10,5 * tileS + 4 * gridS+80,1.5*gridS+tileS+10)
        //tiles
        batch.fill = Color.WHITE
        for(row in 0..4) for (col in 0..4) {
            batch.fillRect(
                baseX + (baseW / 6) + (col * (tileS + gridS)),
                baseY + (baseH / 6) + (row * (tileS + gridS)),
                tileS,tileS)
        }
    }

    private fun drawObjects() {
        for(player in playingField.players) {
            benched = player.value.onBench
            totalBenched = benched
            for (obj in player.value.pieces) {
                obj.update()
                if (notOnBench(obj))
                    drawPlayingPieces(obj)
                else
                    drawBench(obj,player.value.starts)
            }
        }
        drawRocks()
    }
        private fun drawRocks() {
            for(obj in playingField.rocks) {
                obj.update()
                if(notOnBench(obj))
                    batch.drawImage(obj.img, obj.drawLocX!!+4, obj.drawLocY!!+4, tileS-8, tileS-8)
            }
        }
        private fun drawPlayingPieces(o: GameObj) {
            batch.drawImage(o.img, o.drawLocX!!, o.drawLocY!!, tileS,tileS,if (o is Piece) o.rotation else 0.0)
        }
        private fun drawBench(o: GameObj, starts:Boolean) {
            if(benched > 0) {
                val yDiff = if(starts) 1.5 * gridS else height - tileS - 1.5 * gridS
                val xDiff: Double = (5 - totalBenched) * (tileS + gridS) / 2
                o.drawLocX = baseX+baseW/6+(benched-1)*(tileS + gridS) + xDiff
                o.drawLocY = yDiff
                batch.drawImage(o.img,o.drawLocX!!, o.drawLocY!!, tileS, tileS,(o as Piece).rotation)
                benched--
            }
        }

    private fun drawInputs() {
        inputs.updateInputs()
    }
    private fun firstInit() {
        tileInit()
        buttonInit()
        firstTime = false
    }
        private fun tileInit() {
            for(row in 0 until 5) for(col in 0 until 5) {
                playingField.tiles.add(
                    Square(
                        baseX + (baseW / 6) + (col * (tileS + gridS)),
                        baseY + (baseH / 6) + (row * (tileS + gridS)),row,col)
                )
            }
            for(i in 11..13)
                playingField.tiles[i].busy = true
        }
        private fun buttonInit() {
            inputs.cancelButton = MyButton(sepaX + tileS / 2,
                baseY + littlePortion + tileS + 2 * gridS,
                tileS * 3 + 4 * gridS,tileS,"Cancel",null)

            inputs.rotationButtons = listOf(
                MyButton(
                    sepaX + tileS / 2,
                    baseY + 3 * tileS + 2 * (littlePortion + gridS),
                    tileS,tileS,"Left",inputs.arrows),
                MyButton(sepaX + tileS / 2 + tileS + 2 * gridS,
                    baseY + 2 * tileS + 2 * littlePortion,
                    tileS, tileS,"Up",inputs.arrows),
                MyButton(sepaX + tileS / 2 + tileS + 2 * gridS,
                    baseY + 4 * tileS + 2 * (littlePortion + 2 * gridS),
                    tileS,tileS,"Down",inputs.arrows),
                MyButton(sepaX + tileS / 2 + tileS + 4 * gridS + tileS,
                    baseY + 3 * tileS + 2 * (littlePortion + gridS),
                    tileS,tileS,"Right",inputs.arrows)
            )

            inputs.taskButtons = listOf(
                MyButton(sepaX + (width - sepaX - 3 * tileS - 4 * gridS) / 2,
                    baseY + littlePortion,tileS,tileS,"Move", null),
                MyButton(sepaX + (width - sepaX - 3 * tileS - 4 * gridS) / 2 + tileS + 2 * gridS,
                    baseY + littlePortion,tileS,tileS,"Push", null),
                MyButton(sepaX + (width - sepaX - 3 * tileS - 4 * gridS) / 2 + 2 * (tileS + 2 * gridS),
                    baseY + littlePortion,tileS,tileS,"Remove", null),
            )
            for(b in inputs.taskButtons) {
                b.fillColor = pastelGreen
                b.hoverColor = Color.LIGHTGREEN
                b.textColor = darkBlue
            }

            for(b in inputs.rotationButtons) {
                b.fillColor    = pastelYellow
                b.hoverColor   = Color.YELLOW
                b.clickedColor = Color.GREEN
                b.textColor    = Color.TRANSPARENT
            }
            inputs.cancelButton.fillColor  = pastelRed
            inputs.cancelButton.hoverColor = Color.RED
            inputs.cancelButton.textColor  = darkBlue

            inputs.allButtons = mutableListOf<MyButton>()
            inputs.allButtons.add(inputs.taskButtons)
            inputs.allButtons.add(inputs.rotationButtons)
            inputs.allButtons.add(inputs.cancelButton)

        }


    /* CONDITIONS */
    private fun notOnBench(o: GameObj): Boolean = o.gridLoc?.x != -1 && o.gridLoc?.y != -1
}
private fun <E> MutableList<E>.add(element: List<E>) {
    for(e in element)
        this.add(e)
}
fun GraphicsContext.drawImage(img: Image,x: Double,y: Double,w: Double,h: Double,r: Double) {
    val iv= ImageView(img)
    iv.rotate = r
    val params = SnapshotParameters()
    params.fill = Color.TRANSPARENT
    val rotatedImg: Image = iv.snapshot(params,null)
    this.drawImage(rotatedImg,x,y,w,h)
}
