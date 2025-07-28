package com.example.thirty

import java.io.Serializable

class GameState : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
    
    var round: Int = 1
    var roll: Int = 0
    val dice: List<Dice> = List(6) { Dice() }
    val usedScoreOptions: MutableSet<ScoreOption> = mutableSetOf()
    val scores: MutableMap<ScoreOption, Int> = mutableMapOf()
    
    fun reset() {
        round = 1
        roll = 0
        dice.forEach { die ->
            die.value = 1
            die.isSelected = false
            die.wasRolled = false
        }
        usedScoreOptions.clear()
        scores.clear()
    }
    
    fun rollDice() {
        dice.forEachIndexed { index, die ->
            if (!die.isSelected) {
                die.value = (1..6).random()
                die.wasRolled = true
            }
        }
        roll++
    }
    
    fun selectDie(index: Int) {
        if (index in dice.indices && roll < 3) {
            dice[index].isSelected = !dice[index].isSelected
        }
    }
    
    fun scoreRound(option: ScoreOption): Int {
        if (option in usedScoreOptions) return 0
        
        val diceValues = dice.map { it.value }
        val score = when (option) {
            ScoreOption.LOW -> diceValues.filter { it <= 3 }.sum()
            else -> {
                //find optimal grouping of dice that sum to the target
                //each die can only be used ONCE
                maxSumOfGroups(diceValues, optionValue(option))
            }
        }
        
        usedScoreOptions.add(option)
        scores[option] = score
        return score
    }
    
    fun nextRound() {
        round++
        roll = 0
        dice.forEach { die ->
            die.value = 1
            die.isSelected = false
            die.wasRolled = false
        }
    }
    
    fun isGameOver(): Boolean = usedScoreOptions.size == 10
    
    private fun optionValue(option: ScoreOption): Int {
        return when (option) {
            ScoreOption.LOW -> 0
            ScoreOption.FOUR -> 4
            ScoreOption.FIVE -> 5
            ScoreOption.SIX -> 6
            ScoreOption.SEVEN -> 7
            ScoreOption.EIGHT -> 8
            ScoreOption.NINE -> 9
            ScoreOption.TEN -> 10
            ScoreOption.ELEVEN -> 11
            ScoreOption.TWELVE -> 12
        }
    }
    
    private fun maxSumOfGroups(dice: List<Int>, targetSum: Int): Int {
        //use backtracking to find the optimal combination of dice groups to find the maximum possible score by trying all possible
        //groupings of dice that sum to the target value
        return findOptimalGroups(dice, targetSum)
    }
    
    private fun findOptimalGroups(dice: List<Int>, targetSum: Int): Int {
        val sortedDice = dice.sorted()
        var maxScore = 0

        /* Backtracking procedure (credit: https://www.geeksforgeeks.org/dsa/backtracking-algorithms/ )
        * The following is a general outline of how a backtracking algorithm works:
        * Choose an initial solution.
        * Explore all possible extensions of the current solution.
        * If an extension leads to a solution, return that solution.
        * If an extension does not lead to a solution, backtrack to the previous solution and try a different extension.
        * Repeat steps 2-4 until all possible solutions have been explored.
        * */
        //starts with all dice and currentScore = 0
        fun backtrack(remainingDice: List<Int>, currentScore: Int) {
            if (remainingDice.isEmpty()) {
                maxScore = maxOf(maxScore, currentScore)
                return // found a complete solution
            }
            /// tries all possible combinations of all possible sizes
            for (size in 1..remainingDice.size) {
                val combinations = remainingDice.combinations(size)
                for (combination in combinations) {
                    if (combination.sum() == targetSum) {
                        val newRemaining = remainingDice.toMutableList()
                        combination.forEach { die ->
                            newRemaining.remove(die) //make the choice
                        }
                        backtrack(newRemaining, currentScore + targetSum) //explore
                        //implicit backtrack - when recursion returns, the original
                        // remainingDice list is unchanged for next iteration
                    }
                }
            }

            backtrack(emptyList(), currentScore) //if the rest of dice are not sum able to target, skip
        }

        backtrack(sortedDice, 0)
        return maxScore
    }
    
    private fun <T> List<T>.combinations(k: Int): List<List<T>> {
        if (k == 0) return listOf(emptyList())
        if (k > size) return emptyList()
        if (k == 1) return map { listOf(it) }
        
        val result = mutableListOf<List<T>>()
        for (i in 0..size - k) {
            val head = this[i]
            val tailCombinations = drop(i + 1).combinations(k - 1)
            for (tail in tailCombinations) {
                result.add(listOf(head) + tail)
            }
        }
        return result
    }
} 