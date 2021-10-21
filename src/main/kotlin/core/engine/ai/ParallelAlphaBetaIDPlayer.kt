package core.engine.ai

import core.engine.Node
import core.engine.Engine
import core.engine.Move
import core.engine.ai.evaluator.MaterialEvaluator
import core.engine.util.LruCache
import kotlinx.coroutines.*
import java.lang.Integer.max
import java.lang.Integer.min
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicInteger


class ParallelAlphaBetaIDPlayer(player: Int, val maxDepth: Int = 8) : Player(player, Type.AI) {

    data class GameState(
        val value: Int,
        val flag: Flag,
        val depth: Int,
        val bestMaxMove: Move?,
        val bestMinMove: Move?
    ) {
        enum class Flag { Exact, LowerBound, UpperBound }
    }

    data class KillerData(
        var maxMove: Move?,
        var minMove: Move?
    )

    data class WorkerData(var status: Status, var value: Int? = null, var assignedThread: Thread? = null) {
        enum class Status {
            Working,
            NotStarted
        }
    }

    val killerTable = LruCache<String, KillerData>(3000)

    var evaluator = MaterialEvaluator()

    override fun evaluateState(node: Node): Int {

        return evaluator.evaluateState(node, player)
    }

    val syncAlpha = AtomicInteger(Int.MIN_VALUE)

    fun startASubThread(node: Node) {

    }

    fun startAlphaBeta(node: Node, depth: Int, alpha: Int, beta: Int) {


        val numberOfThreads = 8
        var numberOfIdleThreads = 4

        syncAlpha.set(Int.MIN_VALUE)
        var mAlpha = alpha
        var mBeta = beta

        var value = Int.MIN_VALUE
        var moves = node.getMoves()

        var killerMove = killerTable[node.state.toStr()]
        if (killerMove == null)
            killerMove = KillerData(null, null)


        value = Int.MIN_VALUE

        if (killerMove.maxMove != null) {

            if (moves.remove(killerMove.maxMove))
                moves.add(0, killerMove.maxMove!!)
        }



        for (move in moves) {


            if (numberOfIdleThreads > 0) {
                numberOfIdleThreads--

                val worker = Thread() {

                    val child = node.getNodeForMove(move)
                    val score = doAlphaBeta(child, depth - 1, syncAlpha.get(), mBeta, false)

                    if (score > syncAlpha.get()) {
                        node.bestMove = move
                        syncAlpha.set(score)
                    }

                    numberOfIdleThreads++
                }

                worker.start()

            } else{

                val child = node.getNodeForMove(move)
                val score = doAlphaBeta(child, depth - 1, syncAlpha.get(), mBeta, false)

                if (score > syncAlpha.get()) {
                    node.bestMove = move
                    syncAlpha.set(score)
                }


            }
        }


    }

    fun doAlphaBeta(node: Node, depth: Int, alpha: Int, beta: Int, isMax: Boolean): Int {

        var mAlpha = alpha
        var mBeta = beta

        if (depth == 0 || node.isTerminalState()) {
            return evaluateState(node)
        }

        var value = Int.MIN_VALUE
        var moves = node.getMoves()
        var killerMove = killerTable[node.state.toStr()]
        if (killerMove == null)
            killerMove = KillerData(null, null)



        if (isMax) {
            value = Int.MIN_VALUE


            if (killerMove.maxMove != null) {

                if (moves.remove(killerMove.maxMove))
                    moves.add(0, killerMove.maxMove!!)
            }


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

                if (score >= mBeta) {
                    killerMove.maxMove = move
                    killerTable.put(node.state.toStr(), killerMove)
                    break
                }
                mAlpha = max(mAlpha, value)

            }



            return value

        } else {
            value = Int.MAX_VALUE


            moves = moves.asReversed()

            var idx = 0



            if (killerMove.minMove != null) {
                if (moves.remove(killerMove.minMove))
                    moves.add(0, killerMove.minMove!!)
            }

            for (move in moves) {
                val child = node.getNodeForMove(move)
                val score = doAlphaBeta(child, depth - 1, mAlpha, mBeta, true)

                value = min(
                    value,
                    score
                )
                if (value <= mAlpha) {

                    killerMove.minMove = move
                    killerTable.put(node.state.toStr(), killerMove)
                    break
                }
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


        var depth = 0
        var bestMove: Move? = null
        val thread = Thread(Runnable {
            try {


                while (System.currentTimeMillis() - timeStamp < 2000) {
                    depth++
                    println("Parallel Iteration $depth starts")

                    startAlphaBeta(
                        parentNode, depth, Int.MIN_VALUE, Int.MAX_VALUE
                    )

                    println("Parallel Iteration $depth ends")
                    if (parentNode.bestMove != null)
                        bestMove = parentNode.bestMove


                }
            } catch (e: Exception) {

            }

        })
        thread.start()

        sleep(1500)

        thread.stop()

        println("It took ${System.currentTimeMillis() - timeStamp} ${bestMove?.type}")
//        println("node is $parentNode with value $node children ${parentNode.bestMove}")

        if (bestMove == null)
            return engine.getPossibleMoves(engine.playerTurn).random()
        return bestMove!!

        val moves = engine.getPossibleMoves(player)

        return moves.random()
    }


}