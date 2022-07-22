package com.example.ocrproject

import android.util.Log


class ICICI(
    private val stringToLine: HashMap<String, Int>,
    private val lineToString: HashMap<Int, String>
) {
    var hashmap = HashMap<String, String>()
        private set
    private val lines = ArrayList<String>()
    private val vehicleMake = ArrayList<String>()
    private val bodyType = ArrayList<String>()

    init {
        vehicleMake.add("CHEVROLET")
        vehicleMake.add("TATA")
        vehicleMake.add("MARUTI SUZUKI")
        bodyType.add("Saloon")
        bodyType.add("Sedan")
        bodyType.add("Hatchback")
        bodyType.add("Convertible")
        lines.add("Name Telephone no Mobile no Email")
        lines.add("Address Policy No E-Policy No")
        lines.add("Policy Issued On Covernote No")
        lines.add("Vehicle Registration No Vehicle Registration Date")
        lines.add("Previous Policy No Previous Policy Period Previous Insurer Name Previous Policy Type")
        lines.add("Registration No. Make Model Type of Body CC/KW Mfg Yr Seating Capacity Chassis No. Engine No.")
    }

    private fun processLine(l: String, ans: String) {
        var n = ans.length
        when (l) {
            "Name Telephone no Mobile no Email" -> {
                var i = n - 1
                var wd = ""
                var k = 4
                while (i >= 0) {
                    if (k == 0)
                        break
                    while (k > 1 && i >= 0 && !ans[i].equals(' '))
                        wd += ans[i--]
                    if (k == 1)
                        wd = ans.substring(0, i + 1)
                    else
                        wd = wd.reversed()
                    if (k == 4)
                        hashmap["Email"] = wd
                    else if (k == 3)
                        hashmap["Mobile No"] = wd
                    else if (k == 2)
                        hashmap["Telephone No"] = wd
                    else
                        hashmap["Name"] = wd
                    wd = ""
                    --k;--i
                }
            }
            "Address Policy No E-Policy No" -> {
                var i = n - 1
                var k = 2
                var wd = ""
                while (i >= 0) {
                    if (k == 0)
                        break
                    while (i >= 0 && !ans[i].equals(' '))
                        wd += ans[i--]
                    wd = wd.reversed()
                    if (k == 2)
                        hashmap["E-Policy No"] = wd
                    else
                        hashmap["Policy No"] = wd
                    wd = ""
                    --i; --k
                }
            }
            "Policy Issued On Covernote No" -> {
                var i = n - 1
                var wd = ""
                while (i >= 0 && !ans[i].equals(' '))
                    wd += ans[i--]
                wd = wd.reversed()
                hashmap["Covernote No"] = wd
            }
            "Vehicle Registration No Vehicle Registration Date" -> {
                var i = 0
                var wd = ""
                while (i < n && !ans[i].equals(' '))
                    wd += ans[i++]
                hashmap["Vehicle Registration No"] = wd
                wd = ans.substring(i + 1)
                hashmap["Vehicle Registration Date"] = wd
            }
            "Previous Policy No Previous Policy Period Previous Insurer Name Previous Policy Type" -> {
                var i = 0
                var wd = ""
                while (i < n && !ans[i].equals(' '))
                    wd += ans[i++]
                hashmap["Previous Policy No"] = wd
            }
            "Registration No. Make Model Type of Body CC/KW Mfg Yr Seating Capacity Chassis No. Engine No." -> {
                var i = n - 1
                var wd = ""
                while (i >= 0 && !ans[i].equals(' '))
                    wd += ans[i--]
                wd = wd.reversed()
                hashmap["Engine No"] = wd
                --i
                wd = ""
                while (i >= 0 && !ans[i].equals(' '))
                    wd += ans[i--]
                hashmap["Chassis No"] = wd
                extract_Make_Model_and_BodyType(ans)
            }
            else -> {
                Log.d("Error", "Unable to Process line: $l")
            }
        }
    }

    private fun extract_Make_Model_and_BodyType(line: String) {

        val words = line.split(' ').toTypedArray()
        val n = words.size
        var startInd = 1
        var endInd = n - 6  // 6th word from the right
        if (startInd > endInd)
            return
        var bestMatchPercent = 0.0
        var Make = ""
        var makeSize = 0
        for (make in vehicleMake) {
            val sz = make.split(' ').toTypedArray().size // number of words in make
            if (sz == 1) {
                val myWord = words[startInd]
                val currMatchPercent = Utils.LCS(make, myWord)
                if (currMatchPercent > bestMatchPercent && currMatchPercent > 0.5) {
                    bestMatchPercent = currMatchPercent
                    Make = myWord
                    makeSize = 1
                }
            } else if (sz == 2 && startInd + 1 < n) {
                val myWord = words[startInd] + " " + words[startInd + 1]
                val currMatchPercent = Utils.LCS(make, myWord)
                if (currMatchPercent > bestMatchPercent && currMatchPercent > 0.5) {
                    bestMatchPercent = currMatchPercent
                    Make = myWord
                    makeSize = 2
                }
            } else if (sz == 3 && startInd + 2 < n) {
                val myWord = words[startInd] + " " + words[startInd + 1] + " " + words[startInd + 2]
                val currMatchPercent = Utils.LCS(make, myWord)
                if (currMatchPercent > bestMatchPercent && currMatchPercent > 0.5) {
                    bestMatchPercent = currMatchPercent
                    Make = myWord
                    makeSize = 3
                }
            }
        }
        if (makeSize == 3)
            startInd += 3
        else if (makeSize == 2)
            startInd += 2
        else
            ++startInd

        bestMatchPercent = 0.0
        var body = ""
        for (BodyType in bodyType) {
            val currMatchPercent = Utils.LCS(BodyType, words[endInd]) // matching percentage
            if (currMatchPercent > bestMatchPercent && currMatchPercent > 0.5) {
                bestMatchPercent = currMatchPercent
                body = words[endInd]
            }
        }
        hashmap["Type of Body"] = body
        --endInd
        if (Make != "") {
            hashmap["Make"] = Make
            var model = " "
            for (k in startInd..endInd)
                model += words[k] + " "
            model.trim(' ')
            hashmap["Model"] = model
        }
    }

    fun lineMatching() {
        for (line in lines) {
            var bestMatchPercent = 0.0
            var bestMatchLine = ""
            for (key in stringToLine.keys) {
                val currMatchPercent = Utils.LCS(line, key)
                if (currMatchPercent > 0.5 && currMatchPercent > bestMatchPercent) {
                    bestMatchPercent = currMatchPercent
                    bestMatchLine = key
                }
            }
            if (bestMatchLine == "")
                continue
            var lineNoOfVal = stringToLine[bestMatchLine] as Int
            ++lineNoOfVal
            while (lineToString[lineNoOfVal] != null && (lineToString[lineNoOfVal] == "" || lineToString[lineNoOfVal] == "\n"))
                ++lineNoOfVal
            if (lineToString[lineNoOfVal] == null)
                continue
            processLine(line, lineToString[lineNoOfVal].toString())
        }
    }
}