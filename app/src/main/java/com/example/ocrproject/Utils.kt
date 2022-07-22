package com.example.ocrproject

object Utils {

    // LCS function to calculate matching percentage
    fun LCS(line1: String, line2: String): Double {
        var cleanLine1 = line1
        var cleanLine2 = line2
        cleanLine1.uppercase()
        cleanLine2.uppercase()
        cleanLine1.trim(' ')
        cleanLine2.trim(' ')
        var cleanLine1Size = cleanLine1.length
        var cleanLine2Size = cleanLine2.length
        val maxLineSize = Integer.max(cleanLine1Size, cleanLine2Size).toDouble()
        val dp = Array(cleanLine1Size + 1) { IntArray(cleanLine2Size + 1) { 0 } }
        for (i in 1..cleanLine1Size) {
            for (j in 1..cleanLine2Size) {

                if (cleanLine1[i - 1].equals(cleanLine2[j - 1]))
                    dp[i][j] = 1 + dp[i - 1][j - 1]
                dp[i][j] = Integer.max(dp[i][j], Integer.max(dp[i - 1][j], dp[i][j - 1]))
            }
        }
        return dp[cleanLine1Size][cleanLine2Size].toDouble() / maxLineSize
    }

    private fun isUnrequiredChar(char: Char): Boolean {
        if (char == ' ' || char == '\'' || char == '\"' || char == '.' || char == ':' || char == ',' || char == ';')
            return true
        return false
    }

    fun myTrim(str: String): String {
        val strSize = str.length
        var start = 0
        var end = strSize - 1
        while (start < strSize && isUnrequiredChar(str[start]))
            ++start
        while (end >= 0 && isUnrequiredChar(str[end]))
            --end
        if (start > end)
            return ""
        return str.substring(start, end + 1)
    }

}