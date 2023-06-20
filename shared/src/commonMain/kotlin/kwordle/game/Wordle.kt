package io.sebi.kwordle.game

import io.sebi.kwordle.game.LetterState.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

enum class LetterState {
    CORRECT, WRONG_POSITION, INCORRECT, UNGUESSED
}

data class WordleWord(val letters: List<WordleLetter>)
data class WordleLetter(val letter: Char, val letterState: LetterState)

class Game(val realWord: String) {
    fun bestGuessForLetter(c: Char) =
        guesses.value.flatMap { it.letters }.filter { it.letter == c }.map { it.letterState }.minByOrNull { it.ordinal }
            ?: UNGUESSED

    var guesses = MutableStateFlow(listOf<WordleWord>())

    fun guess(guess: String) {
        val normalizedGuess = guess.trim().uppercase()
        val wordleWord = normalizedGuess.asIterable()
            .zip(wordle(realWord, normalizedGuess)) { letter, state -> WordleLetter(letter, state) }
        guesses.update {
            it + WordleWord(wordleWord)
        }
    }
}

fun wordle(realWord: String, guess: String): List<LetterState> {
    val normalizedRealWord = realWord.uppercase()
    val normalizedGuess = guess.uppercase()
    val output = MutableList(normalizedRealWord.length) { INCORRECT }

    val lettersInRealWord = normalizedRealWord.toMutableList()

    for (index in normalizedRealWord.indices intersect normalizedGuess.indices) {
        val guessLetter = normalizedGuess[index]
        val realLetter = normalizedRealWord[index]
        if (guessLetter == realLetter) {
            output[index] = CORRECT
            lettersInRealWord -= realLetter
        }
    }

    for (index in output.indices) {
        if (output[index] != INCORRECT) continue
        val guessLetter = normalizedGuess.getOrNull(index) ?: continue
        if (guessLetter in lettersInRealWord) {
            output[index] = WRONG_POSITION
            lettersInRealWord -= guessLetter
        }
    }
    return output
}
