package com.example.ocrproject

import android.util.Log

class Bajaj(private val stringToLine: HashMap<String, Int>) {
    private val lines = ArrayList<String>()
    var hashmap = HashMap<String, String>()
        private set

    init {
        lines.add("We wish to inform you that the contract under policy number")
        lines.add("1. Proposer Name :")
        lines.add("5. Proposer email id :")
        lines.add("3. Proposer Mobile Number :")
    }

    private fun processLines(line: String, key: String): Pair<String, Double> {
        val keyLength = key.length
        var bestLine = ""
        var matchingPercent = 0.0
        for (iterate1 in 0..keyLength - 1) {
            val a = key.substring(0, iterate1 + 1)
            val currMatchingPercent = Utils.LCS(line, a)
            if (currMatchingPercent > matchingPercent && currMatchingPercent >= 0.7) {
                matchingPercent = currMatchingPercent
                if (line == "We wish to inform you that the contract under policy number") {
                    var iterate2 = iterate1 + 1
                    var ans = ""
                    if (iterate2 < keyLength && key[iterate2].equals(' '))
                        ++iterate2
                    while (iterate2 < keyLength && !key[iterate2].equals(' '))
                        ans += key[iterate2++]
                    bestLine = ans
                } else
                    bestLine = key.substring(iterate1 + 1)
            }
        }
        return Pair(bestLine, matchingPercent)
    }

    private fun saveInfo(l: String, wd: String) {
        when (l) {
            "We wish to inform you that the contract under policy number" -> {
                hashmap["Policy No"] = wd
            }
            "1. Proposer Name :" -> {
                hashmap["Name"] = wd
            }
            "5. Proposer email id :" -> {
                hashmap["Email"] = wd
            }
            "3. Proposer Mobile Number :" -> {
                hashmap["Mobile No"] = wd
            }
            else -> {
                Log.d("Error", "Unable to Process line: $l")
            }
        }
    }

    fun lineMatching() {
        for (l in lines) {
            var bestLine = ""
            var bestMatchingPercent = 0.0
            for (keys in stringToLine.keys) {
                val bestPair = processLines(l, keys)
                if (bestPair.second > bestMatchingPercent) {
                    bestMatchingPercent = bestPair.second
                    bestLine = bestPair.first

                }
            }
            if (bestLine == "")
                continue
            bestLine = Utils.myTrim(bestLine)
            saveInfo(l, bestLine)
        }
    }

}