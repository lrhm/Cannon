package core.engine

import kotlin.math.roundToInt
import kotlin.random.Random

val random: Random = Random(System.currentTimeMillis())


class Node(
    val state: GameState, val engine: Engine, var player: Int,
    val depth: Int,
    val isMax: Boolean,
    var move: Move? = null
) {

    constructor(
        node: Node,
        move: Move,
        state: GameState, engine: Engine, player: Int, depth: Int,
        isMax: Boolean,
    ) : this(state, engine, player, depth, isMax, move) {
        parent = node

    }


    override fun equals(other: Any?): Boolean {
        if (other is Node) {
            if (other.state.toStr() == state.toStr() && isMax == other.isMax)
                return true
        }
        return super.equals(other)
    }


    lateinit var parent: Node


    fun isTerminalState(): Boolean {


        if (state.isTownDead(player) && engine.turnsPassed > 2)
            return true
        if (state.isTownDead(player.otherPlayer()) && engine.turnsPassed > 2)
            return true


        calcMoves()
        if (myMoves.isEmpty() || enemyMoves.isEmpty())
            return true


        return false

    }

    lateinit var myMoves: List<Move>

    lateinit var enemyMoves: List<Move>


    fun calcMoves() {
        if (::myMoves.isInitialized.not())
            myMoves = engine.getPossibleMoves(player, state)


//        myMoves = engine.getPossibleMoves(player, state)
        if (::enemyMoves.isInitialized.not())
            enemyMoves = engine.getPossibleMoves(player.otherPlayer(), state)
    }


    fun calcDistance(position: Position, toPosition: Position): Double {

        return ((position.i - toPosition.i) * (position.i - toPosition.i) +
                2 * (position.j - toPosition.j) * (position.j - toPosition.j)).toDouble()

    }

    fun calcMovementTowardEnemyTownValue(): Double {

        val enemyTown = state.getTownPosition(player.otherPlayer())

        if (enemyTown != null && move != null && move!!.from.i != -1 && move!!.to.i != -1) {

            val distanceBefore = calcDistance(move!!.from, enemyTown!!)
            val disanceNow = calcDistance(move!!.to, enemyTown)

            return distanceBefore - disanceNow
        }
        return 0.0
    }

    var chNodes: List<Node>? = null

    fun getMoves(): MutableList<Move> {
        return engine.getPossibleMoves(player, state).toMutableList()
    }


    var bestMove: Move? = null
//     var children: List<Node>


    fun getNodeForMove(move: Move): Node {
        val newBoard = state.copy()

        move.applyMove(newBoard, player)

        val node = Node(
            newBoard, engine, (player + 1).mod(2), depth - 1, isMax.not(), move
        )
        node.parent = this

        return node

    }


}
