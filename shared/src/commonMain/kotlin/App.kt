import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.sebi.kwordle.game.Game
import io.sebi.kwordle.ui.WordleGame
import org.jetbrains.compose.resources.ExperimentalResourceApi

val wordleGame = Game("CRANE")

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme {
        Box(
            Modifier.fillMaxSize().background(Color(0xFF121213)).padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.widthIn(max = 560.dp).fillMaxSize()) {
                WordleGame(wordleGame)
            }
        }
    }
}

expect fun getPlatformName(): String