package rendering

import gameLogic.*
import gameLogic.Map
import javafx.scene.input.MouseEvent
import getResource
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.paint.Color
import java.awt.Point

class InputHandler(private val playingField: Map, private val batch: GraphicsContext) {

    private var selObj: Piece? = null
    private var player: Player = playingField.players["Elephant"]!!
    val arrows: Image = Image(getResource("/arrows.png"))
    lateinit var rotationButtons: List<MyButton>
    lateinit var taskButtons: List<MyButton>
    lateinit var cancelButton: MyButton
    lateinit var allButtons: MutableList<MyButton>
    private var selectingFacing = false
    private var moving = false

    /* MOUSE EVENT */
    fun mousePressed(event: MouseEvent) {
        if(selObj == null) {
            selObj = locateObject(event)
            if(selObj != null) {
                showTaskButtons()
                cancelButton.visible = true
            }
        }
        if(selObj != null && moving)
            moveObject(event)
        buttonActions(event)
    }
    fun mouseMoved(event: MouseEvent) {
        for(b in allButtons)
            if(b.visible)
                b.hovered = b.pointInsideButton(event.sceneX,event.sceneY)
    }

    fun updateInputs() {
        if(moving) {
            highlightPlaceable()
        }
        highlightTile()
        for(b in allButtons) {
            b.draw(batch)
        }
    }

    private fun highlightTile() {
        if(selObj != null)
            highlightPiece()
    }

    private fun movingObject() {
        moving = true
        showButtons(false,taskButtons)
    }
    private fun moveObject(e: MouseEvent) {
        var validTile = false
        val x = e.sceneX ; val y = e.sceneY
        for(t in playingField.tiles) {
            if(t.insideSquare(x,y) && ((t.selected && tileNotBusy(t)) || sameTile(t))) {
                if (selObj!!.benched) {
                    selObj?.benched = false
                    player.onBench -= 1
                } else
                    playingField.tiles[selObj!!.gridLoc?.x!! + selObj!!.gridLoc?.y!! * 5].busy = false
                selObj?.gridLoc = Point(t.col, t.row)
                playingField.tiles[t.row * 5 + t.col].busy = true
                validTile = true
                break
            }
        }
        if(validTile) {
            moving = false
            facingButtons()
            nullSelected()
        }
    }



    private fun locateObject(e: MouseEvent): Piece? {
        val x = e.sceneX
        val y = e.sceneY
        for(o in player.pieces) {
            if(x > o.drawLocX!! && x < o.drawLocX!! + GUI.tileS &&
                y > o.drawLocY!! &&  y < o.drawLocY!! + GUI.tileS) {
                return o
            }

        }
        return null
    }
    private fun doAction(b: MyButton) {
        when (b.actionCommand) {
            "Up"     -> rotateObject(180.0)
            "Down"   -> rotateObject(0.0)
            "Left"   -> rotateObject(90.0)
            "Right"  -> rotateObject(270.0)
            "Cancel" -> cancel()
            "Move"   -> movingObject()
            "Push"   -> push()
            "Remove" -> removeObject()
        }
    }

    /* HELPER FUNCTIONS */
    private fun rotateObject(rotation: Double) {
        selObj!!.rotation = rotation
        switchPlayer()
        cancel()
    }
    private fun removeObject() {
        if(!(selObj?.benched!!)) {
            playingField.tiles[selObj?.gridLoc!!.y * 5 + selObj?.gridLoc!!.x].busy = false
            player.benchPiece(selObj!!)
            switchPlayer()
            cancel()
        }
    }
    private fun push() {
        var over: Rock? = null
        if(!(selObj?.benched!!)) {
            val line = mutableListOf<GameObj>(selObj!!)
            val rot = selObj!!.rotation
            if(getForce(line,rot) >= 0) {
                playingField.tiles[selObj!!.gridLoc!!.y * 5 + selObj!!.gridLoc!!.x].busy = false
                for (o in line) {
                    oneStep(o.gridLoc!!,rot)
                    if(offMap(o)) {
                        if(o is Piece)
                            playingField.players[o.type]!!.benchPiece(o)
                        if(o is Rock) {
                            over = o
                        }
                    }
                    else
                        playingField.tiles[o.gridLoc!!.y * 5 + o.gridLoc!!.x].busy = true
                }
                if(over != null) {
                    checkWinner(over)
                }
                switchPlayer()
                cancel()
            }
        }
    }
    private fun getForce(line: MutableList<GameObj>,rot: Double): Int {
        var force = 1
        val gridPos = Point(selObj?.gridLoc!!)
        var nextObj: GameObj? = if(tileBusy(gridPos,rot)) getObj(gridPos) else null
        while(nextObj != null) {
            line.add(0, nextObj)
            force += nextObj.push(selObj!!)
            nextObj = if (tileBusy(gridPos, rot)) getObj(gridPos) else null
        }
        return force
    }
    private fun getObj(point: Point): GameObj? {
        for(p in playingField.players) for(o in p.value.pieces) {
            if(o.gridLoc == point)
                return o
        }
        for(rock in playingField.rocks) {
            if(rock.gridLoc == point)
                return rock
        }
        return null
    }
    private fun oneStep(o: Point,rot: Double) {
        when(rot) {
            0.0   -> o.y += 1
            90.0  -> o.x -= 1
            180.0 -> o.y -= 1
            270.0 -> o.x += 1
        }
    }
    private fun checkWinner(r: Rock) {
        val ogPoint = r.gridLoc!!
        val rot = if(selObj!!.rotation + 180 >= 360) selObj!!.rotation - 180 else selObj!!.rotation + 180
        var winner: Player? = null
        while(winner == null) {
            oneStep(ogPoint,rot)
            for(p in playingField.players) for(piece in p.value.pieces) {
                if(piece.gridLoc == ogPoint && piece.rotation == selObj!!.rotation)
                    winner = p.value
            }
        }
        println("Winner is: $winner")
        r.gridLoc = Point(-1, -1)
    }


    private fun nullSelected() {
        for(row in 0..4) for(col in 0..4) {
            playingField.tiles[row+col].selected = false
        }
    }
    private fun cancel() {
        showButtons(false, rotationButtons)
        showButtons(false, taskButtons)
        cancelButton.visible = false
        cancelButton.hovered = false
        selObj = null
        moving = false
        selectingFacing = false
    }
    private fun switchPlayer() {
        player =
            if(player == playingField.players["Elephant"])
                playingField.players["Rhino"]!!
            else
                playingField.players["Elephant"]!!
    }

    /* Buttons */
    private fun showButtons(show: Boolean, buttons: List<MyButton>) {
        for(b in buttons) {
            b.visible = show
            if(!show)
                b.hovered = show
        }
    }
    private fun facingButtons() {
        selectingFacing = true
        showButtons(true,rotationButtons)
        cancelButton.visible = false
    }
    private fun showTaskButtons() {
        showButtons(true,taskButtons)
    }
    private fun buttonActions(e: MouseEvent) {
        for(b in allButtons)
            if(b.pointInsideButton(e.sceneX,e.sceneY) && b.visible)
                doAction(b)
    }

    /* PURE DRAWING */
    private fun highlightPlaceable() {
        batch.fill = Color.YELLOW
        if(selObj!!.benched)
            highlightFromBench()
        else
            highlightFromInGame()
    }
        private fun highlightFromBench() {
            for (row in 0..4) for (col in 0..4) {
                if (!(col in 1..3 && row in 1..3) && !playingField.tiles[row * 5 + col].busy) {
                    drawHighlight(row, col)
                    playingField.tiles[row * 5 + col].selected = true
                }
            }
        }
        private fun highlightFromInGame() {
            for(row in 0..4) for(col in 0..4) {
                if(nextToSelected(row,col) && !playingField.tiles[row * 5 + col].busy) {
                    drawHighlight(row, col)
                    playingField.tiles[row * 5 + col].selected = true
                }
            }
        }
            private fun drawHighlight(row: Int, col: Int) {
        batch.fillRect(
            GUI.baseX + (GUI.baseW / 6) + (col * (GUI.tileS + GUI.gridS)),
            GUI.baseY + (GUI.baseH / 6) + (row * (GUI.tileS + GUI.gridS)),
            GUI.tileS, GUI.tileS
        )
    }
    private fun highlightPiece() {
            batch.drawImage(selObj!!.selectedImg,selObj?.drawLocX!!,selObj?.drawLocY!!,
                GUI.tileS,GUI.tileS, selObj!!.rotation)
        }


    /* CONDITIONS */
    private fun nextToSelected(row: Int, col: Int): Boolean {
        return (selObj!!.gridLoc?.x == col && (
                selObj!!.gridLoc?.y == row + 1 || selObj!!.gridLoc?.y == row - 1) ||
                selObj!!.gridLoc?.y == row && (
                selObj!!.gridLoc?.x == col + 1 || selObj!!.gridLoc?.x == col - 1))
    }
    private fun tileNotBusy(t: Square): Boolean = !playingField.tiles[t.row * 5 + t.col].busy
    private fun tileBusy(p: Point,r: Double): Boolean {
        when(r) {
            0.0   -> p.y += 1
            90.0  -> p.x -= 1
            180.0 -> p.y -= 1
            270.0 -> p.x += 1
        }
        if(p.x == -1 || p.y == -1 || p.x == 5 || p.y == 5)
            return false
        return playingField.tiles[p.y * 5 + p.x].busy
    }
    private fun offMap(o: GameObj): Boolean {
        return (o.gridLoc?.x == -1 || o.gridLoc?.y == -1 ||
                o.gridLoc?.x == 5  || o.gridLoc?.y == 5)
    }
    private fun sameTile(s: Square): Boolean {
        return (s.row == selObj?.gridLoc!!.y && s.col == selObj?.gridLoc!!.x)
    }

}

fun Point.write():String {
    return "[${this.x},${this.y}]"
}