package io.sebi.kwordle.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.sebi.kwordle.game.Game
import io.sebi.kwordle.game.LetterState
import io.sebi.kwordle.game.LetterState.*
import io.sebi.kwordle.game.WordleLetter
import io.sebi.kwordle.game.WordleWord
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val Shapes = Shapes(
    small = RoundedCornerShape(0f),
    medium = RoundedCornerShape(0f),
    large = RoundedCornerShape(0f),
)

@Composable
fun WordleGame(game: Game) {
    val gameState by game.guesses.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Spacer(Modifier.height(10.dp))
        ColoredWords(gameState, scrollState)
        WordInputWithOnScreenKeyboard(game, onWordSubmitted = {
            game.guess(it)
            scope.launch {
                delay(100)
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        })
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColumnScope.WordInputWithOnScreenKeyboard(game: Game, onWordSubmitted: (String) -> Unit) {
    var wordInput by remember { mutableStateOf("") }
    WordleWordInput(wordInput) { keyEvent ->
        println(keyEvent)
        when {
            keyEvent.key == Key.Enter -> {
                onWordSubmitted(wordInput)
                wordInput = ""
            }

            keyEvent.key == Key.Backspace && keyEvent.type == KeyEventType.KeyUp -> {
                wordInput = wordInput.dropLast(1)
            }

//            keyEvent.isTypedEvent -> {
//                wordInput += keyEvent.utf16CodePoint.toChar()
//            }
        }
        wordInput = wordInput.take(5)
        false
    }
    Spacer(Modifier.height(10.dp))
    Keyboard(game) { char ->
        when (char) {
            '⏎' -> {
                onWordSubmitted(wordInput)
                wordInput = ""
            }

            '⌫' -> {
                wordInput = wordInput.dropLast(1)
            }

            else -> {
                wordInput += char
                wordInput = wordInput.take(5)
            }
        }
    }
}

@Composable
fun ColumnScope.ColoredWords(gameState: List<WordleWord>, scrollState: ScrollState) {
    Column(modifier = Modifier.fillMaxHeight().weight(1.0f).verticalScroll(scrollState)) {
        for (g in gameState) {
            ColoredWord(g)
        }
    }
}

@Composable
fun ColoredWord(w: WordleWord) {
    Row(Modifier.fillMaxWidth()) {
        for ((idx, wordleLetter) in w.letters.withIndex()) {
            val (char, state) = wordleLetter
            Box(Modifier.weight(1f)) {
                WordleLetter(state, char, idx)
            }
        }
    }
}

@Composable
fun ColumnScope.Keyboard(g: Game, onKeyClicked: (Char) -> Unit) {
    val keyboard = listOf(
        "QWERTYUIOP", "ASDFGHJKL", "⏎ZXCVBNM⌫"
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            for (row in keyboard) {
                Row(horizontalArrangement = Arrangement.Center) {
                    for (letter in row) {
                        val state = g.bestGuessForLetter(letter)
                        Crossfade(targetState = state) { state ->
                            Box(
                                Modifier.clickable { onKeyClicked(letter) }.padding(1.dp).border(1.dp, White)
                                    .width(24.dp).height(32.dp)
                                    .background(state.color), contentAlignment = Alignment.Center
                            ) {
                                Text("$letter", color = state.textColor, fontSize = 1.em)
                            }
                        }
                    }
                }
            }
        }
    }
}

val LetterState.color: Color
    get() {
        return when (this) {
            CORRECT -> Color(0xFF67A760) // Green
            INCORRECT -> Black
            WRONG_POSITION -> Color(0xFFC8B359) // Yellow
            UNGUESSED -> White
        }
    }

val LetterState.textColor: Color
    get() {
        return when (this) {
            UNGUESSED -> Black
            else -> White
        }
    }

@Composable
fun WordleWordInput(currentWord: String, onKeyEvent: (KeyEvent) -> Boolean) {
    Box(Modifier
        .clickable { }
        .onKeyEvent(onKeyEvent)
    ) {
        ColoredWord(WordleWord(List(5) {
            WordleLetter(currentWord.getOrElse(it) { ' ' }.uppercaseChar(), INCORRECT)
        }))
    }
}

@Composable
fun WordleLetterA() {
    WordleLetter(INCORRECT, 'A', 0)
}

@Composable
fun WordleLetter(state: LetterState, letter: Char, index: Int) {
    var shouldBeVisible by remember { mutableStateOf(false) }
    LaunchedEffect(letter) {
        delay(200L * index)
        shouldBeVisible = true
    }
    AnimatedVisibility(visible = shouldBeVisible, enter = slideInHorizontally() + fadeIn()) {
        Box(
            Modifier.aspectRatio(1.0f).border(1.dp, White).fillMaxWidth().background(state.color),
            contentAlignment = Alignment.Center
        ) {
            Text("$letter", color = state.textColor, fontSize = 3.em)
        }
    }
}