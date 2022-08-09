package gameLogic


class Map() {
    val players = mutableMapOf<String,Player>()
    val rocks = mutableListOf<Rock>()
    val tiles = mutableListOf<Square>()

    init {
        players["Elephant"] = Player("Elephant",true)
        players["Rhino"] = Player("Rhino",false)
        for(i in 0 until 3) {
            rocks.add(Rock("Rock",1+i,2))
        }
    }


}