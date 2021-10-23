package core.engine.ai

import core.engine.Engine
import core.engine.Move
import core.engine.Node
import core.engine.ai.evaluator.MaterialEvaluator

class MinMaxPlayer(player: Int, val maxDepth: Int = 8) : Player(player, Type.AI) {


//    val evaluator = MaterialEvaluator()

    fun doAlphaBeta(node: Node, depth: Int, alpha: Int, beta: Int): Int {


//        val children = node.getChildNodes()

        if (depth == 0 || node.isTerminalState())
            return evaluator.evaluateState(node, player)


        var score = Int.MIN_VALUE
        var value = Int.MIN_VALUE

        var mAlpha = alpha



        for (move in node.getMoves()) {

            val child = node.getNodeForMove(move)

            value = -1 * doAlphaBeta(child, depth - 1, -beta, -mAlpha)

            if (value > score) {
                score = value
                node.bestMove = child.move!!
            }

            if (value > alpha)
                mAlpha = score
//            if (score >= beta)
//                break
        }
        return score


    }

    override fun getMove(engine: Engine): Move {

        val timeStamp = System.currentTimeMillis()

        val parentNode = Node(
            engine.board, engine, engine.playerTurn, 0, true
        )
        val node = doAlphaBeta(
            parentNode, maxDepth, Int.MIN_VALUE, Int.MAX_VALUE
        )

        println("It took ${System.currentTimeMillis() - timeStamp}")
//        println("node is $parentNode with value $node children ${parentNode.bestMove}")

        if (parentNode.bestMove == null)
            return engine.getPossibleMoves(engine.playerTurn).random()
        return parentNode.bestMove!!

        val moves = engine.getPossibleMoves(player)

        return moves.random()
    }


}
