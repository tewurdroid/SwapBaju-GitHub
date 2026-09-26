package com.example.swapbaju

import android.graphics.Bitmap

interface HumanParser {
    fun parse(person: Bitmap): HumanParsingResult
}
