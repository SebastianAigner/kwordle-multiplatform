package io.sebi.kwordle.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sebi.kwordle.game.Game
import io.sebi.kwordle.game.LetterState
import io.sebi.kwordle.game.LetterState.CORRECT
import io.sebi.kwordle.game.LetterState.INCORRECT
import io.sebi.kwordle.game.LetterState.UNGUESSED
import io.sebi.kwordle.game.LetterState.WRONG_POSITION
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
        Box(Modifier.fillMaxSize().weight(1.0f)) {
            Column(Modifier.fillMaxSize()) {
                WordInputWithOnScreenKeyboard(game, onWordSubmitted = {
                    game.guess(it)
                    scope.launch {
                        delay(100)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                })
            }
        }
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
    Column(modifier = Modifier.fillMaxHeight().weight(1.5f).verticalScroll(scrollState)) {
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
fun Keyboard(g: Game, onKeyClicked: (Char) -> Unit) {
    val keyboard = listOf(
        "QWERTYUIOP", "ASDFGHJKL", "⏎ZXCVBNM⌫"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
//            .border(1.dp, Color.Red)
            .fillMaxWidth().fillMaxSize()
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            for (row in keyboard) {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.weight(1.0f)) {
                    if (row.length == 9) {
                        Spacer(Modifier.weight(0.5f))
                    }
                    for (letter in row) {
                        Box(Modifier.fillMaxSize().weight(1.0f, true)) {
                            KeyboardKey(letter, g.bestGuessForLetter(letter), onKeyClicked)
                        }
                    }
                    if (row.length == 9) {
                        Spacer(Modifier.weight(0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun KeyboardKey(letter: Char, state: LetterState, onKeyClicked: (Char) -> Unit) {
    Crossfade(targetState = state) { state ->
        Box(
            Modifier.clickable { onKeyClicked(letter) }.padding(1.dp)
                .border(1.dp, White)
                .fillMaxSize()
                .background(state.color), contentAlignment = Alignment.Center
        ) {
            Text("$letter", color = state.textColor, fontSize = 15.sp)
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
            Text("$letter", color = state.textColor, fontSize = 40.sp)
        }
    }
}