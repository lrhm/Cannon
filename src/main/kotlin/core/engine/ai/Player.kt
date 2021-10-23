package core.engine.ai

import core.engine.Engine
import core.engine.Move
import core.engine.Node
import core.engine.ai.evaluator.MaterialEvaluator
import core.engine.util.LruCache

open class Player(var player: Int, val type: Type) {


    open var evaluator: MaterialEvaluator = MaterialEvaluator()
    open fun getMove(engine: Engine): Move {

        val moves = engine.getPossibleMoves(player)

        return moves.random()

    }

    open fun evaluateState(node: Node): Int {
        return 0
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



