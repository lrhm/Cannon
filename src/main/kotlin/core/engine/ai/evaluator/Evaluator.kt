package core.engine.ai.evaluator

import core.engine.Board
import core.engine.Move
import core.engine.Node

interface Evaluator {

    fun evaluateState(state: Node, player: Int, move: Move? = null): Int
}