// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import core.engine.GameState
import core.engine.ai.Player
import core.engine.ai.toPlayerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import core.engine.Engine
import core.engine.Move
import core.engine.Position
import core.engine.util.Simulator

@Composable
@Preview

fun App() {
    var text by remember { mutableStateOf("Hello, World!") }
    var isGameStarted by remember { mutableStateOf(false) }
    var board by remember { mutableStateOf(engine.board) }

    fun doNextTurn() {

        if (engine.hasPlayerLost())
            return

        engine.playATurn()

        engine.board.printBoard()


        CoroutineScope(Dispatchers.Main).launch {
            board = engine.board.copy()

        }

    }

    fun reset() {
        engine.board = GameState()
        engine.turnsPassed = 0
        engine.playerTurn = 0
        board = engine.board
        isGameStarted = false
    }

    val simulator = Simulator()
    fun startGame() {

//        simulator.generateAIPool()
//        simulator.startRandomMatches()
//        return
        CoroutineScope(Dispatchers.Default).launch {

            if (isGameStarted.not())
                return@launch
            doNextTurn()
            delay(100)
            startGame()


        }


    }

    val imageModifier = Modifier
        .width(515.dp)
        .fillMaxWidth()

    DesktopMaterialTheme {
        Column {
            Box {
                Image(
                    painter = painterResource("bg.png"),
                    contentDescription = "image",
                    imageModifier,
                    contentScale = ContentScale.FillWidth
                )
                Grid(board,
                    onUpdate = {
                        board = board.copy()
                        engine.board = board

                        engine.board.printBoard()
//                        doNextTurn()
//                        if (engine.turnsPassed == 2)
//                            startGame()
                    }
                )


            }

            Row {
//                Text("Player 0 : ", modifier = Modifier.padding(20.dp))
//
//                playerSelector(0) {
//
////                    engine.playerOne = it
//                }
//
//                Text("Player 1 : ", modifier = Modifier.padding(20.dp))
//
//                playerSelector(1) {
//
////                    engine.playerTwo = it
//                }

            }

            Button({
                isGameStarted = true
                startGame()
            }, Modifier.padding(20.dp), isGameStarted.not()) {
                Text("Start the game")

            }

            Button(::doNextTurn, Modifier.padding(20.dp), isGameStarted.not()) {
                Text("Next Turn")

            }

            if (engine.hasPlayerLost()) {
                Text("Game Ended, Player ${engine.playerTurn + 1} Lost")

                Button(::reset, Modifier.padding(20.dp)) {
                    Text("Reset")

                }
            }

        }

    }
}


@Composable
fun playerSelector(player: Int, onSelectPlayer: (Player) -> Unit) {

    var expanded by remember { mutableStateOf(false) }
    val items = listOf("AI", "Human")
    var selectedIndex by remember { mutableStateOf(0) }
    Box(modifier = Modifier.padding(20.dp)) {
        Text(items[selectedIndex], modifier = Modifier.clickable(onClick = { expanded = true }))
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
//            modifier = Modifier.fillMaxWidth().background(
//                Color.Red
//            )
        ) {
            items.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    selectedIndex = index
                    expanded = false

                    onSelectPlayer(Player(player, s.toPlayerType()))
                }) {

                    Text(text = s)
                }
            }
        }
    }


}

val engine = Engine()



@OptIn(ExperimentalDesktopApi::class)
@Composable
fun Grid(board: GameState, onUpdate: () -> Unit) {
    val rows = arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    val columns = arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)

    var selectedPosition by remember { mutableStateOf(Position(-1, -1)) }
    var isAPieceSelected by remember { mutableStateOf(false) }


    val possibleMoves = engine.getPossibleMoves(engine.playerTurn, engine.board)


    fun isSelectable(i: Int, j: Int): Boolean {

        if (selectedPosition.i != -1) {

            if (possibleMoves.filter { it.from == selectedPosition }.any {
                    it.to.i == i && it.to.j == j
                })
                return true

        }

        if (possibleMoves.any {
                ((it.type == Move.Type.PlaceTown || it.type == Move.Type.Shoot) && it.to == Position(
                    i,
                    j
                ))
            }
        ) return true


        if (possibleMoves.any {
                ((it.type == Move.Type.Capture || it.type == Move.Type.Forward || it.type == Move.Type.Slide) && it.from == Position(
                    i,
                    j
                ))
            }
        ) return true


        return false
    }

    LazyColumn {
        itemsIndexed(rows) { index, i ->
            LazyRow {
                itemsIndexed(columns) { index, j ->
                    Cell(
                        Position(
                            i,
                            j
                        ), board.board[i][j], isSelectable(i, j)
                    ) { position ->

                        if (isSelectable(position.row, position.column)) {

                            val placeMove = possibleMoves.find {
                                it.type == Move.Type.PlaceTown && it.to == Position(
                                    i,
                                    j
                                )

                            }

                            if (placeMove != null) {

                                placeMove.applyPlaceTownMove(board, engine.playerTurn)
                                engine.turnsPassed++
                                engine.playerTurn = engine.turnsPassed.mod(2)
                                onUpdate()
                                return@Cell

                            }

                            val shootMove = possibleMoves.find {
                                it.type == Move.Type.Shoot && it.to == Position(
                                    i,
                                    j
                                )
                            }

                            if (shootMove != null) {
                                shootMove.applyMove(board, engine.playerTurn)
                                engine.turnsPassed++
                                engine.playerTurn = engine.turnsPassed.mod(2)
                                onUpdate()

                                return@Cell

                            }


                            if (selectedPosition.i == -1) {

                                val selectableMoves = possibleMoves.filter {
                                    it.from == Position(
                                        i,
                                        j
                                    )
                                }.find {
                                    (it.type == Move.Type.Slide || it.type == Move.Type.Forward ||
                                            it.type == Move.Type.Retreat || it.type == Move.Type.Capture)
                                }
                                if (selectableMoves != null) {
                                    selectedPosition = Position(i, j)
                                }
                            } else {

                                val move = possibleMoves.filter { it.from == selectedPosition }
                                    .find { it.to == Position(i, j) }
                                if (move != null) {

                                    move.applyMove(board, engine.playerTurn)
                                    engine.turnsPassed++
                                    engine.playerTurn = engine.turnsPassed.mod(2)
                                    onUpdate()

                                } else {
                                    selectedPosition = Position(-1, -1)
                                }

                            }


//                            onUpdate()
                        }

                    }
//

                }
            }

        }


    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        this.window.setSize(520.dp.value.toInt(), 700.dp.value.toInt())
        App()
    }
}
