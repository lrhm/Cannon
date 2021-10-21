package core.engine.ai

import core.engine.Engine
import core.engine.Move
import core.engine.Node
import core.engine.util.LruCache

open class Player(val player: Int, val type: Type) {


    open fun getMove(engine: Engine): Move {

        val moves = engine.getPossibleMoves(player)

        return moves.random()

    }

    open fun evaluateState(node: Node): Int {
        return node.evaluateState(player)
    }


    enum class Type {
        Human, AI
    }


}




fun String.toPlayerType(): Player.Type {

    if (this == "AI")
        return Player.Type.AI

    return Player.Type.Human
}



