package com.example.thirty

import java.io.Serializable

data class Dice(
    var value: Int = 1,
    var isSelected: Boolean = false,
    var wasRolled: Boolean = false
) : Serializable 