package com.example.ocrproject

import android.provider.SimPhonebookContract
import android.util.Log

class Reliance(stoL: HashMap<String,Int>) {
    private val stringToLine:HashMap<String,Int>
    private val keyWords = ArrayList<Pair<String,String>>()
    val hashmap =  HashMap<String,String>()
    init {
        stringToLine = stoL
        keyWords.add(Pair("Insured Name","Period of Insurance"))
        keyWords.add(Pair("Policy Number","Proposal/Covernote No"))
        keyWords.add(Pair("Mobile No","Tax Invoice No. & Date"))
        keyWords.add(Pair("Email-ID","GSTIN/UIN & Place of Supply"))
        keyWords.add(Pair("Registration No","Mfg. Month & Year"))
        keyWords.add(Pair("Make / Model & Variant","CC / HP / Watt"))
        keyWords.add(Pair("Engine No./Chassis No.","Seating Capacity Including"))
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
    private fun isUnrequiredChar(s: Char):Boolean{
        if(s == ' ' || s == '\'' || s == '\"' || s == '.' || s == ':' || s == ',' || s == ';')
        return true
        return false
    }

    private fun myTrim(str:String):String{
        val n = str.length
        var start = 0
        var end = n-1
        while(start < n && isUnrequiredChar(str[start]))
            ++start
        while(end >= 0 && isUnrequiredChar(str[end]))
            --end
        if(start > end)
            return ""
       return str.substring(start,end+1)
    }

    private fun detectPair(pair:Pair<String,String>,line: String):Pair<String,String>{
        var cp = Pair(0.0,0.0)
        var ca = Pair("","")
        line.trim(' ')
        var n = line.length
        var i = 0
        var v = ArrayList<Int>()
        while(i < n){
            if(line[i].equals(' '))
                v.add(i)
            ++i
        }
        n = v.size
        for(i in 0..n-1){
            for(j in i+1..n-1){
                for(k in j+1..n-1){
                    var a = line.substring(0,v[i])
                    var b = line.substring(v[j]+1,v[k])
                    a = myTrim(a)
                    b = myTrim(b)
                    val op = Pair(LCS(a,pair.first),LCS(b,pair.second))
                    if(op.first > 0.5 && op.second > 0.5)
                    {
                        if((cp.first < op.first && cp.second < op.second) || (cp.first == op.first && cp.second < op.second)
                            || (cp.first < op.first && cp.second == op.second)){
                            cp = op
                            var c = line.substring(v[i]+1,v[j])
                            var d = line.substring(v[k]+1)
                            c = myTrim(c)
                            d = myTrim(d)
                            ca = Pair(c, d)
                        }
                    }
                }
            }
        }
        if(ca.first == "" && ca.second == ""){
            var po = 0.0
            var wd = ""
            for(i in 0..n-1){
                var a = line.substring(0,v[i])
                a = myTrim(a)
                val op = LCS(a,pair.first)
                if(op > 0.75 && po < op)
                {
                    po = op
                    wd = line.substring(v[i]+1)
                    wd = myTrim(wd)
                }
            }
            return Pair(wd,"")
        }
        return ca
    }

    private fun processOutput(pair:Pair<String,String>, out:Pair<String,String>){
        when(pair.first){
            "Insured Name"->{
                hashmap["Name"] = out.first
            }
            "Policy Number"->{
                hashmap["Policy Number"] = out.first
            }
            "Mobile No"->{
                hashmap["Mobile No"] = out.first
            }
            "Email-ID"->{
                hashmap["Email"] = out.first
            }
            "Registration No"->{
                hashmap["Vehicle Registration No"] = out.first
            }
            "Make / Model & Variant"->{
                hashmap["Vehicle Model"] = out.first
            }
            "Engine No./Chassis No." ->{
                hashmap["Engine No / Chassis No"] = out.first
            }
            else->{
                Log.e("Error: ","Cannot interpret the key")
            }
        }
        when(pair.second){
            "Period of Insurance"->{
                hashmap["Period of Insurance"] = out.second
            }
            "Proposal/Covernote No"->{
                hashmap["Covernote No"] = out.second
            }
        }
    }

    fun lineMatching(){
        for(pairs in keyWords){
            var cp = Pair("","")
            for(keys in stringToLine.keys){
                var op:Pair<String,String> = detectPair(pairs,keys)
                if(op.first != "" || op.second != ""){
                    cp = op
                    break
                }
            }
            if(cp.first == "" && cp.second == "")
                continue
            processOutput(pairs,cp)
        }
    }

}