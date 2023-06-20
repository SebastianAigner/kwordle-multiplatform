import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Kwordle for Desktop",
        state = rememberWindowState(width = 5 * 128.dp, height = 8 * 128.dp),

        ) {
        MainView()
    }
}