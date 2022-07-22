package com.example.ocrproject

import android.util.Log

class Reliance(private val stringToLine: HashMap<String,Int>) {
    private val keyWords = ArrayList<Pair<String,String>>()
    var hashmap =  HashMap<String,String>()
        private set
    init {
        keyWords.add(Pair("Insured Name","Period of Insurance"))
        keyWords.add(Pair("Policy Number","Proposal/Covernote No"))
        keyWords.add(Pair("Mobile No","Tax Invoice No. & Date"))
        keyWords.add(Pair("Email-ID","GSTIN/UIN & Place of Supply"))
        keyWords.add(Pair("Registration No","Mfg. Month & Year"))
        keyWords.add(Pair("Make / Model & Variant","CC / HP / Watt"))
        keyWords.add(Pair("Engine No./Chassis No.","Seating Capacity Including"))
    }


    private fun detectPair(pair:Pair<String,String>,line: String):Pair<String,String>{
        var currPair = Pair(0.0,0.0)
        var currAns = Pair("","")
        line.trim(' ')
        var n = line.length
        var i = 0
        val posOfSpaces = ArrayList<Int>()
        while(i < n){
            if(line[i].equals(' '))
                posOfSpaces.add(i)
            ++i
        }
        n = posOfSpaces.size
        // finding 2 key value pairs in a line
        for(i in 0..n-1){
            for(j in i+1..n-1){
                for(k in j+1..n-1){
                    var firstKey = line.substring(0,posOfSpaces[i])
                    var secondKey = line.substring(posOfSpaces[j]+1,posOfSpaces[k])
                    firstKey = Utils.myTrim(firstKey)
                    secondKey = Utils.myTrim(secondKey)
                    val bestMatchPair = Pair(Utils.LCS(firstKey,pair.first),Utils.LCS(secondKey,pair.second))
                    if(bestMatchPair.first > 0.5 && bestMatchPair.second > 0.5)
                    {
                        if((currPair.first < bestMatchPair.first && currPair.second < bestMatchPair.second) || (currPair.first == bestMatchPair.first && currPair.second < bestMatchPair.second)
                            || (currPair.first < bestMatchPair.first && currPair.second == bestMatchPair.second)){
                            currPair = bestMatchPair
                            var firstValue = line.substring(posOfSpaces[i]+1,posOfSpaces[j])
                            var secondValue = line.substring(posOfSpaces[k]+1)
                            firstValue = Utils.myTrim(firstValue)
                            secondValue = Utils.myTrim(secondValue)
                            currAns = Pair(firstValue, secondValue)
                        }
                    }
                }
            }
        }
        if(currAns.first == "" && currAns.second == ""){
            // checking for single key value pair in a line
            var bestMatchPercent = 0.0
            var value = ""
            for(iterate in 0..n-1){
                var key = line.substring(0,posOfSpaces[iterate])
                key = Utils.myTrim(key)
                val currMatchPercent = Utils.LCS(key,pair.first)
                if(currMatchPercent > 0.75 && bestMatchPercent < currMatchPercent)
                {
                    bestMatchPercent = currMatchPercent
                    value = line.substring(posOfSpaces[iterate]+1)
                    value = Utils.myTrim(value)
                }
            }
            return Pair(value,"")
        }
        return currAns
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
            var bestPair = Pair("","")
            for(keys in stringToLine.keys){
                val currPair = detectPair(pairs,keys)
                if(currPair.first != "" || currPair.second != ""){
                    bestPair = currPair
                    break
                }
            }
            if(bestPair.first == "" && bestPair.second == "")
                continue
            processOutput(pairs,bestPair)
        }
    }

}