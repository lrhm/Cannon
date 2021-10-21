package core.engine.ai

import core.engine.Node
import core.engine.Engine
import core.engine.Move
import core.engine.otherPlayer
import java.lang.Integer.max
import java.lang.Integer.min


class AlphaBetaPlayer(player: Int, val maxDepth: Int = 8) : Player(player, Type.AI) {


//    val transpositionTable = LRUCache<String, Node?>(1000)

    fun doAlphaBeta(node: Node, depth: Int, alpha: Int, beta: Int, isMax: Boolean): Int {


//        node.player = node.player.otherPlayer()
        if (depth == 0 || node.isTerminalState()) {
            return node.evaluateState(player)
        }


        var value = Int.MIN_VALUE
        var mAlpha = alpha
        var mBeta = beta

        var moves = node.getMoves()

        if (isMax) {
            value = Int.MIN_VALUE

            for (move in moves) {
                val child = node.getNodeForMove(move)
                val score = doAlphaBeta(child, depth - 1, mAlpha, mBeta, false)
                if (score > value) {
                    node.bestMove = move
                }
                value = max(
                    value,
                    score
                )

                if (score  + 5 >= mBeta)
                    break
                mAlpha = max(mAlpha, value)

            }

            return value

        } else {
            value = Int.MAX_VALUE

            moves = moves.asReversed()

            for (move in moves) {



                val child = node.getNodeForMove(move)
                val score = doAlphaBeta(child, depth - 1, mAlpha, mBeta, true)

//                if (value < score) {
//                    node.bestMove = move
//                }
                value = min(
                    value,
                    score
                )
                if (value <= mAlpha + 5)
                    break

                mBeta = min(mAlpha, value)

            }

            return value

        }


    }

    override fun getMove(engine: Engine): Move {

        val timeStamp = System.currentTimeMillis()

        val parentNode = Node(
            engine.board, engine, engine.playerTurn, maxDepth, true, Int.MIN_VALUE, Int.MIN_VALUE
        )
         doAlphaBeta(
            parentNode, maxDepth, Int.MIN_VALUE, Int.MAX_VALUE, true
        )

        println("It took ${System.currentTimeMillis() - timeStamp} ${parentNode.bestMove?.type}")
//        println("node is $parentNode with value $node children ${parentNode.bestMove}")

        if (parentNode.bestMove == null)
            return engine.getPossibleMoves(engine.playerTurn).random()
        return parentNode.bestMove!!

        val moves = engine.getPossibleMoves(player)

        return moves.random()
    }


}