package com.example.ocrproject

import android.util.Log

class Bajaj(stoL: HashMap<String,Int>) {
    private val stringToLine:HashMap<String,Int>
    private val lines = ArrayList<String>()
    val hashmap =  HashMap<String,String>()
    init {
        stringToLine = stoL
        lines.add("We wish to inform you that the contract under policy number")
        lines.add("Proposer Name :")
        lines.add("Proposer email id :")
        lines.add("Proposer Mobile Number :")

    }

    // LCS function to calculate matching percentage
    private fun LCS(a:String, b:String):Double{
        var x = a
        var y = b
        x.toUpperCase()
        y.toUpperCase()
        x.trim(' ')
        y.trim(' ')
        var n = x.length
        var m = y.length
        val d = Integer.max(n, m).toDouble()
        val dp = Array(n+1){IntArray(m+1){0}}
        for(i in 1..n){
            for(j in 1..m){
                if(x[i-1].equals(y[j-1]))
                    dp[i][j] = 1+dp[i-1][j-1]
                dp[i][j] = Integer.max(dp[i][j], Integer.max(dp[i - 1][j], dp[i][j - 1]))
            }
        }
        return dp[n][m].toDouble()/d
    }
    private fun myTrim(str:String):String{
        var s = str
        s.trim(' ')
        var n = s.length
        if(n == 0)
            return s
        if(s[0] == ' ' || s[0] == '.' || s[0] == ':' || s[0] == ',' || s[0] == ';' || s[0] == '\"' || s[0] == '\'')
            s = s.substring(1)
        n = s.length
        if(n == 0)
            return s
        if(s[n-1] == ' ' || s[n-1] == '.' || s[n-1] == ':' || s[n-1] == ',' || s[n-1] == ';' || s[0] == '\"' || s[0] == '\'')
            s = s.substring(0,n-1)
        s.trim(' ')
        return s
    }

    private fun processLines(l: String, key: String):Pair<String,Double>{
        var n = key.length
        var wd = ""
        var mp = 0.0    // matching percentage
        for(i in 0..n-1){
            val a = key.substring(0,i+1)
            val cm = LCS(l,a)
            if(cm > mp && cm >= 0.7){
                mp = cm
                if(l == "We wish to inform you that the contract under policy number")
                {
                    var j = i+1
                    var ans = ""
                    if(j < n && key[j].equals(' '))
                        ++j
                    while(j < n && !key[j].equals(' '))
                        ans += key[j++]
                    wd = ans
                }
                else
                wd = key.substring(i+1)
            }
        }
        return Pair(wd,mp)
    }

    private fun saveInfo(l:String, wd: String){
        when(l){
            "We wish to inform you that the contract under policy number"->{
                hashmap["Policy No"] = wd
            }
            "Proposer Name :"->{
                hashmap["Name"] = wd
            }
            "Proposer email id :"->{
                hashmap["Email"] = wd
            }
            "Proposer Mobile Number :"->{
                hashmap["Mobile No"] = wd
            }
            else->{
                Log.d("Error", "Unable to Process line: $l")
            }
        }
    }

    fun lineMatching(){
        for(l in lines){
            var wd = ""
            var bm = 0.0  // best matching percentage
            for(keys in stringToLine.keys){
                var op = processLines(l,keys)
                if(op.second > bm){
                    bm = op.second
                    wd = op.first
                }
            }
            if(wd == "")
                continue
            wd = myTrim(wd)
            saveInfo(l,wd)
        }
    }

}