package com.example.ocrproject

import android.util.Log
import java.lang.Integer.max


class ICICI(stoL: HashMap<String,Int>, ltoS: HashMap<Int,String>){
    val hashmap =  HashMap<String,String>()
    private val stringToLine:HashMap<String,Int>
    private val lineToString:HashMap<Int,String>
    private val lines = ArrayList<String>()
    init {
        stringToLine = stoL
        lineToString = ltoS
        lines.add("Name Telephone no Mobile no Email")
        lines.add("Address Policy No E-Policy No")
        lines.add("Policy Issued On Covernote No")
        lines.add("Vehicle Registration No Vehicle Registration Date")
        lines.add("Previous Policy No Previous Policy Period Previous Insurer Name Previous Policy Type")
        lines.add("Registration No. Make Model Type of Body CC/KW Mfg Yr Seating Capacity Chassis No. Engine No.")
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
        val d = max(n,m).toDouble()
        val dp = Array(n+1){IntArray(m+1){0}}
        for(i in 1..n){
            for(j in 1..m){
                if(x[i-1].equals(y[j-1]))
                    dp[i][j] = 1+dp[i-1][j-1]
                dp[i][j] = max(dp[i][j],max(dp[i-1][j],dp[i][j-1]))
            }
        }
        return dp[n][m].toDouble()/d
    }
    private fun processLine(l: String,ans: String){
        var n = ans.length
        when(l){
            "Name Telephone no Mobile no Email"->{
                var i = n-1
                var wd = ""
                var k = 4
                while(i >= 0){
                    if(k == 0)
                        break
                    while(k > 1 && i >= 0 && !ans[i].equals(' '))
                        wd += ans[i--]
                    if(k == 1)
                        wd = ans.substring(0,i+1)
                    else
                         wd = wd.reversed()
                    if(k == 4)
                        hashmap["Email"] = wd
                    else if(k == 3)
                        hashmap["Mobile No"] = wd
                    else if(k == 2)
                        hashmap["Telephone No"] = wd
                    else
                        hashmap["Name"] = wd
                    wd = ""
                    --k;--i
                }
            }
            "Address Policy No E-Policy No"->{
                var i = n-1
                var k = 2
                var wd = ""
                while(i >= 0){
                    if(k == 0)
                        break
                    while(i >= 0 && !ans[i].equals(' '))
                        wd += ans[i--]
                    wd = wd.reversed()
                    if(k == 2)
                        hashmap["E-Policy No"] = wd
                    else
                        hashmap["Policy No"] = wd
                    wd = ""
                    --i; --k
                }
            }
            "Policy Issued On Covernote No"->{
                var i = n-1
                var wd = ""
                while(i >= 0 && !ans[i].equals(' '))
                    wd += ans[i--]
                wd = wd.reversed()
                hashmap["Covernote No"] = wd
            }
            "Vehicle Registration No Vehicle Registration Date"->{
                var i = 0
                var wd = ""
                while(i < n && !ans[i].equals(' '))
                    wd += ans[i++]
                hashmap["Vehicle Registration No"] = wd
                wd = ans.substring(i+1)
                hashmap["Vehicle Registration Date"] = wd
            }
            "Previous Policy No Previous Policy Period Previous Insurer Name Previous Policy Type"->{
                var i = 0
                var wd = ""
                while(i < n && !ans[i].equals(' '))
                    wd += ans[i++]
                hashmap["Previous Policy No"] = wd
            }
            "Registration No. Make Model Type of Body CC/KW Mfg Yr Seating Capacity Chassis No. Engine No."->{

            }
            else->{
                Log.d("Error", "Unable to Process line: $l")
            }
        }
    }
    fun lineMatching(){
        for(line in lines){
            var bm = 0.0
            var l = ""
            for(key in stringToLine.keys){
                var cm = LCS(line,key)
                if(cm > 0.5 && cm > bm){
                    bm = cm
                    l = key
                }
            }
            if(l == "")
                continue
            var i = stringToLine[l] as Int
            ++i
            while(lineToString[i] != null && (lineToString[i] == "" || lineToString[i] == "\n"))
                ++i
            if(lineToString[i] == null)
                continue
            processLine(line,lineToString[i].toString())
        }
    }
}