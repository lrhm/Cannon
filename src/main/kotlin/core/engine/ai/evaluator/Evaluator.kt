package core.engine.ai.evaluator

import core.engine.Board
import core.engine.Node

interface Evaluator {

    fun evaluateState(state: Board, player: Int): Int
}