package com.example.thirty

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        
        val scoreMap = intent.getSerializableExtra("scores") as? Map<String, Int> ?: emptyMap()
        
        try {
            updateResultGrid(scoreMap) // Landscape layout
        } catch (e: Exception) {
            val resultList = findViewById<TextView>(R.id.resultList)
            val totalText = findViewById<TextView>(R.id.totalText)
            
            val resultString = StringBuilder()
            var total = 0
            var counter = 0
            
            for (option in listOf("Low", "4", "5", "6", "7", "8", "9", "10", "11", "12")) {
                counter++
                val score = scoreMap[option]
                if (score != null) {
                    resultString.append("(Omgång $counter) $option: $score\n")
                    total += score
                }
            }
            
            resultList.text = resultString.toString().trim()
            totalText.text = "Total: $total"
        }
        
        val newGameButton = findViewById<Button>(R.id.newGameButton)
        val menuButton = findViewById<Button>(R.id.menuButton)
        
        newGameButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        
        menuButton.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
    
    private fun updateResultGrid(scoreMap: Map<String, Int>) {
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
            val score = scoreMap[option]
            if (score != null) {
                textView.text = "(Omgång $counter) $option: $score"
                total += score
            } else {
                textView.text = "$option: -"
            }
        }
        
        resultTotal.text = "Total: $total"
    }
} 