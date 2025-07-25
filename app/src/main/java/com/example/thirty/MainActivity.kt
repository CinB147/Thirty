package com.example.thirty

import android.animation.ObjectAnimator
import android.view.animation.AlphaAnimation
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.Serializable

class MainActivity : AppCompatActivity() {
    private lateinit var diceImageViews: List<ImageView>
    private lateinit var rollButton: Button
    private lateinit var confirmScoreButton: Button
    private lateinit var scoreSpinner: Spinner
    private lateinit var roundText: TextView
    private lateinit var resultText: TextView
    private lateinit var backButton: ImageButton
    private lateinit var roundDots: List<ImageView>
    private lateinit var roundLabels: List<TextView>
    
    private val gameState = GameState()
    private var roundLocked = false
    private val blinkRunnables = mutableMapOf<Int, Runnable>()
    private val roundLabelTexts = mutableMapOf<Int, String>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize UI elements
        diceImageViews = listOf(
            findViewById(R.id.dice1),
            findViewById(R.id.dice2),
            findViewById(R.id.dice3),
            findViewById(R.id.dice4),
            findViewById(R.id.dice5),
            findViewById(R.id.dice6)
        )
        
        rollButton = findViewById(R.id.rollButton)
        confirmScoreButton = findViewById(R.id.confirmScoreButton)
        scoreSpinner = findViewById(R.id.scoreSpinner)
        roundText = findViewById(R.id.roundText)
        resultText = findViewById(R.id.resultText)
        backButton = findViewById(R.id.backButton)
        
        // Initialize round dots and labels
        roundDots = listOf(
            findViewById(R.id.roundDot1),
            findViewById(R.id.roundDot2),
            findViewById(R.id.roundDot3),
            findViewById(R.id.roundDot4),
            findViewById(R.id.roundDot5),
            findViewById(R.id.roundDot6),
            findViewById(R.id.roundDot7),
            findViewById(R.id.roundDot8),
            findViewById(R.id.roundDot9),
            findViewById(R.id.roundDot10)
        )
        
        roundLabels = listOf(
            findViewById(R.id.roundLabel1),
            findViewById(R.id.roundLabel2),
            findViewById(R.id.roundLabel3),
            findViewById(R.id.roundLabel4),
            findViewById(R.id.roundLabel5),
            findViewById(R.id.roundLabel6),
            findViewById(R.id.roundLabel7),
            findViewById(R.id.roundLabel8),
            findViewById(R.id.roundLabel9),
            findViewById(R.id.roundLabel10)
        )
        
        // Set up dice click listeners
        diceImageViews.forEachIndexed { i, imageView ->
            imageView.setOnClickListener {
                gameState.selectDie(i)
                updateDiceImage(i)
            }
        }
        
        // Set up button listeners
        rollButton.setOnClickListener {
            animateAndRollDice()
        }
        
        confirmScoreButton.setOnClickListener {
            val selectedDisplayName = scoreSpinner.selectedItem as String
            val option = ScoreOption.values().find { it.displayName == selectedDisplayName }
            if (option != null) {
                val score = gameState.scoreRound(option)
                roundLocked = true
                
                // Update the round label with the score
                val roundIndex = gameState.round - 1
                val scoreText = "${scoreOptionToString(option)}: $score"
                roundLabels[roundIndex].text = scoreText
                roundLabelTexts[roundIndex] = scoreText
                
                // Delay before next round
                Handler(Looper.getMainLooper()).postDelayed({
                    gameState.nextRound()
                    roundLocked = false
                    updateUI()
                    updateScoreSpinner()
                }, 1000)
            }
        }
        
        backButton.setOnClickListener {
            onBackPressed()
        }
        
        updateUI()
        updateScoreSpinner()
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("gameState", gameState)
        outState.putBoolean("roundLocked", roundLocked)
        outState.putStringArray("roundLabelTexts", roundLabelTexts.values.toTypedArray())
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restored = savedInstanceState.getSerializable("gameState") as? GameState
        if (restored != null) {
            // Manually copy state from restored object to current gameState instance
            gameState.round = restored.round
            gameState.roll = restored.roll
            gameState.dice.forEachIndexed { i, die ->
                die.value = restored.dice[i].value
                die.isSelected = restored.dice[i].isSelected
                die.wasRolled = restored.dice[i].wasRolled
            }
            gameState.usedScoreOptions.clear()
            gameState.usedScoreOptions.addAll(restored.usedScoreOptions)
            gameState.scores.clear()
            gameState.scores.putAll(restored.scores)
        }
        roundLocked = savedInstanceState.getBoolean("roundLocked", false)
        
        // Restore round label texts
        val savedRoundLabels = savedInstanceState.getStringArray("roundLabelTexts")
        if (savedRoundLabels != null) {
            roundLabelTexts.clear()
            savedRoundLabels.forEachIndexed { index, text ->
                roundLabelTexts[index] = text
            }
        }
        
        updateScoreSpinner()
        updateUI()
    }
    
    private fun animateAndRollDice() {
        val animations = mutableListOf<ObjectAnimator>()
        
        gameState.dice.forEachIndexed { index, die ->
            if (!die.isSelected) {
                val imageView = diceImageViews[index]
                val direction = if (Math.random() > 0.5) 1f else -1f
                val animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f * direction)
                animator.duration = 500
                animations.add(animator)
            }
        }
        
        // Start all animations
        animations.forEach { it.start() }
        
        // Roll dice after animation starts
        Handler(Looper.getMainLooper()).postDelayed({
            gameState.rollDice()
            updateUI()
        }, 250)
    }
    
    private fun updateDiceImage(index: Int) {
        val die = gameState.dice[index]
        val imageView = diceImageViews[index]
        
        val imageName = when {
            die.isSelected -> "red${die.value}"
            die.wasRolled -> "grey${die.value}"
            else -> "white${die.value}"
        }
        
        val imageId = resources.getIdentifier(imageName, "drawable", packageName)
        imageView.setImageResource(imageId)
    }
    
    private fun updateUI() {
        // Update dice images
        gameState.dice.forEachIndexed { i, _ ->
            updateDiceImage(i)
        }
        
        // Update round text
        roundText.text = "Kast: ${gameState.roll}/3"
        
        // Update round dots and labels
        for (i in 0 until 10) {
            when {
                i < gameState.round - 1 -> {
                    // Completed rounds
                    roundDots[i].setImageResource(R.drawable.dot_filled)
                    roundDots[i].clearAnimation()
                    // Stop blinking animation if it exists
                    blinkRunnables[i]?.let { runnable ->
                        roundDots[i].removeCallbacks(runnable)
                        blinkRunnables.remove(i)
                    }
                    // Show saved score text for completed rounds
                    roundLabels[i].text = roundLabelTexts[i] ?: ""
                }
                i == gameState.round - 1 -> {
                    // Current round - blinking between filled and empty
                    roundDots[i].setImageResource(R.drawable.dot_filled)
                    roundLabels[i].text = "?"
                    
                    // Stop any existing blinking animation for this dot
                    blinkRunnables[i]?.let { runnable ->
                        roundDots[i].removeCallbacks(runnable)
                    }
                    
                    // Create blinking animation that switches between filled and empty
                    val blinkRunnable = object : Runnable {
                        var isFilled = true
                        override fun run() {
                            roundDots[i].setImageResource(
                                if (isFilled) R.drawable.dot_empty else R.drawable.dot_filled
                            )
                            isFilled = !isFilled
                            roundDots[i].postDelayed(this, 500) // Switch every 500ms
                        }
                    }
                    blinkRunnables[i] = blinkRunnable
                    roundDots[i].post(blinkRunnable)
                }
                else -> {
                    // Future rounds
                    roundDots[i].setImageResource(R.drawable.dot_empty)
                    roundDots[i].clearAnimation()
                    roundLabels[i].text = ""
                    // Stop blinking animation if it exists
                    blinkRunnables[i]?.let { runnable ->
                        roundDots[i].removeCallbacks(runnable)
                        blinkRunnables.remove(i)
                    }
                }
            }
        }
        
        // Update button states
        rollButton.isEnabled = !roundLocked && gameState.roll < 3
        confirmScoreButton.isEnabled = !roundLocked && gameState.roll > 0
        diceImageViews.forEach { it.isEnabled = !roundLocked && gameState.roll < 3 }
        
        // Update result display based on layout (portrait or landscape)
        try {
            updateResultGrid() // For landscape layout
        } catch (e: Exception) {
            resultText.text = buildResultString() // For portrait layout
        }
        
        // Check if game is over
        if (gameState.isGameOver()) {
            val intent = Intent(this, ResultActivity::class.java)
            val scoreMap = HashMap<String, Int>()
            for ((option, score) in gameState.scores) {
                scoreMap[scoreOptionToString(option)] = score
            }
            intent.putExtra("scores", scoreMap)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
    
    private fun updateScoreSpinner() {
        val availableOptions = ScoreOption.values().filter { it !in gameState.usedScoreOptions }
        val displayNames = availableOptions.map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, displayNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        scoreSpinner.adapter = adapter
    }
    
    private fun buildResultString(): String {
        val result = StringBuilder()
        var total = 0
        var counter = 0
        
        for (option in listOf("Low", "4", "5", "6", "7", "8", "9", "10", "11", "12")) {
            counter++
            val score = gameState.scores[scoreOptionFromString(option)]
            if (score != null) {
                result.append("(Omgång $counter) $option: $score\n")
                total += score
            }
        }
        
        return if (total > 0) "Resultat:\n${result.toString().trim()}\n\nTotal: $total" else ""
    }
    
    private fun updateResultGrid() {
        val resultLow = findViewById<TextView>(R.id.resultLow)
        val result4 = findViewById<TextView>(R.id.result4)
        val result5 = findViewById<TextView>(R.id.result5)
        val result6 = findViewById<TextView>(R.id.result6)
        val result7 = findViewById<TextView>(R.id.result7)
        val result8 = findViewById<TextView>(R.id.result8)
        val result9 = findViewById<TextView>(R.id.result9)
        val result10 = findViewById<TextView>(R.id.result10)
        val result11 = findViewById<TextView>(R.id.result11)
        val result12 = findViewById<TextView>(R.id.result12)
        val resultTotal = findViewById<TextView>(R.id.resultTotal)
        
        var total = 0
        var counter = 0
        
        val scorePairs = listOf(
            "Low" to resultLow,
            "4" to result4,
            "5" to result5,
            "6" to result6,
            "7" to result7,
            "8" to result8,
            "9" to result9,
            "10" to result10,
            "11" to result11,
            "12" to result12
        )
        
        for ((option, textView) in scorePairs) {
            counter++
            val score = gameState.scores[scoreOptionFromString(option)]
            if (score != null) {
                textView.text = "(Omgång $counter) $option: $score"
                total += score
            } else {
                textView.text = "$option: -"
            }
        }
        
        resultTotal.text = "Total: $total"
    }
    
    private fun scoreOptionToString(option: ScoreOption): String {
        return when (option) {
            ScoreOption.LOW -> "Low"
            ScoreOption.FOUR -> "4"
            ScoreOption.FIVE -> "5"
            ScoreOption.SIX -> "6"
            ScoreOption.SEVEN -> "7"
            ScoreOption.EIGHT -> "8"
            ScoreOption.NINE -> "9"
            ScoreOption.TEN -> "10"
            ScoreOption.ELEVEN -> "11"
            ScoreOption.TWELVE -> "12"
        }
    }
    
    private fun scoreOptionFromString(str: String): ScoreOption {
        return when (str) {
            "Low" -> ScoreOption.LOW
            "4" -> ScoreOption.FOUR
            "5" -> ScoreOption.FIVE
            "6" -> ScoreOption.SIX
            "7" -> ScoreOption.SEVEN
            "8" -> ScoreOption.EIGHT
            "9" -> ScoreOption.NINE
            "10" -> ScoreOption.TEN
            "11" -> ScoreOption.ELEVEN
            "12" -> ScoreOption.TWELVE
            else -> ScoreOption.LOW
        }
    }
    
    override fun onBackPressed() {
        if (gameState.round > 1 || gameState.roll > 0) {
            AlertDialog.Builder(this)
                .setTitle("Varning")
                .setMessage("Är du säker på att du vill avsluta spelet? All framsteg kommer att gå förlorat.")
                .setPositiveButton("Ja") { _, _ ->
                    val intent = Intent(this, StartActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Nej", null)
                .show()
        } else {
            val intent = Intent(this, StartActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        super.onBackPressed()
    }
}