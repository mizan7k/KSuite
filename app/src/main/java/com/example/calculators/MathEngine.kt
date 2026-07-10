package com.example.calculators

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.*

object MathEngine {

    // --- 1. SCIENTIFIC EXPRESSION EVALUATOR ---
    // Safely evaluates basic formulas with standard scientific functions
    fun evaluateScientific(expr: String): Double {
        if (expr.isBlank()) return 0.0
        val sanitized = expr.replace(" ", "")
            .replace("π", Math.PI.toString())
            .replace("e", Math.E.toString())
            .replace("×", "*")
            .replace("÷", "/")
        return try {
            Parser(sanitized).parse()
        } catch (e: Exception) {
            Double.NaN
        }
    }

    private class Parser(val input: String) {
        var pos = -1
        var ch = 0

        fun nextChar() {
            pos++
            ch = if (pos < input.length) input[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < input.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm() // addition
                else if (eat('-'.code)) x -= parseTerm() // subtraction
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor() // multiplication
                else if (eat('/'.code)) x /= parseFactor() // division
                else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor() // unary plus
            if (eat('-'.code)) return -parseFactor() // unary minus

            var x: Double
            val startPos = this.pos
            if (eat('('.code)) { // parentheses
                x = parseExpression()
                eat(')'.code)
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                x = input.substring(startPos, this.pos).toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val func = input.substring(startPos, this.pos)
                x = parseFactor()
                x = when (func) {
                    "sin" -> sin(Math.toRadians(x))
                    "cos" -> cos(Math.toRadians(x))
                    "tan" -> tan(Math.toRadians(x))
                    "asin" -> Math.toDegrees(asin(x))
                    "acos" -> Math.toDegrees(acos(x))
                    "atan" -> Math.toDegrees(atan(x))
                    "sinr" -> sin(x)
                    "cosr" -> cos(x)
                    "tanr" -> tan(x)
                    "log" -> log10(x)
                    "ln" -> ln(x)
                    "sqrt" -> sqrt(x)
                    "cbrt" -> cbrt(x)
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            if (eat('^'.code)) x = x.pow(parseFactor()) // exponentiation

            return x
        }
    }

    // --- MATRIX CALCULATOR ENGINE ---
    fun matrixDeterminant2x2(m: List<List<Double>>): Double {
        return m[0][0] * m[1][1] - m[0][1] * m[1][0]
    }

    fun matrixInverse2x2(m: List<List<Double>>): List<List<Double>>? {
        val det = matrixDeterminant2x2(m)
        if (det == 0.0) return null
        return listOf(
            listOf(m[1][1] / det, -m[0][1] / det),
            listOf(-m[1][0] / det, m[0][0] / det)
        )
    }

    fun matrixDeterminant3x3(m: List<List<Double>>): Double {
        return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) -
               m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) +
               m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0])
    }

    fun matrixInverse3x3(m: List<List<Double>>): List<List<Double>>? {
        val det = matrixDeterminant3x3(m)
        if (abs(det) < 1e-9) return null
        
        val inv = mutableListOf<List<Double>>()
        val a11 = (m[1][1] * m[2][2] - m[1][2] * m[2][1]) / det
        val a12 = (m[0][2] * m[2][1] - m[0][1] * m[2][2]) / det
        val a13 = (m[0][1] * m[1][2] - m[0][2] * m[1][1]) / det

        val a21 = (m[1][2] * m[2][0] - m[1][0] * m[2][2]) / det
        val a22 = (m[0][0] * m[2][2] - m[0][2] * m[2][0]) / det
        val a23 = (m[0][2] * m[1][0] - m[0][0] * m[1][2]) / det

        val a31 = (m[1][0] * m[2][1] - m[1][1] * m[2][0]) / det
        val a32 = (m[0][1] * m[2][0] - m[0][0] * m[2][1]) / det
        val a33 = (m[0][0] * m[1][1] - m[0][1] * m[1][0]) / det

        inv.add(listOf(a11, a12, a13))
        inv.add(listOf(a21, a22, a23))
        inv.add(listOf(a31, a32, a33))
        return inv
    }

    fun transposeMatrix(m: List<List<Double>>): List<List<Double>> {
        val rows = m.size
        val cols = m[0].size
        val result = MutableList(cols) { MutableList(rows) { 0.0 } }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[j][i] = m[i][j]
            }
        }
        return result
    }

    // --- 2. FINANCIAL MORTGAGE ENGINE ---
    fun calculateMortgage(
        homePrice: Double,
        downPayment: Double,
        interestRate: Double,
        loanTermYears: Int,
        propertyTaxRate: Double = 1.2,
        insuranceYr: Double = 1200.0
    ): Map<String, Any> {
        val principal = homePrice - downPayment
        val r = interestRate / 100.0 / 12.0
        val n = loanTermYears * 12
        val monthlyPI = if (r == 0.0) {
            principal / n
        } else {
            principal * (r * (1 + r).pow(n)) / ((1 + r).pow(n) - 1)
        }
        val monthlyTax = (homePrice * (propertyTaxRate / 100.0)) / 12.0
        val monthlyInsurance = insuranceYr / 12.0
        val totalMonthly = monthlyPI + monthlyTax + monthlyInsurance

        return mapOf(
            "principal_interest" to monthlyPI,
            "property_tax" to monthlyTax,
            "insurance" to monthlyInsurance,
            "total_monthly" to totalMonthly,
            "total_interest" to (monthlyPI * n) - principal
        )
    }

    // --- 3. LOAN CALCULATOR ---
    fun calculateLoan(amount: Double, rate: Double, months: Int): Map<String, Double> {
        val r = rate / 100.0 / 12.0
        val monthly = if (r == 0.0) amount / months else amount * (r * (1 + r).pow(months)) / ((1 + r).pow(months) - 1)
        val totalPay = monthly * months
        val totalInterest = totalPay - amount
        return mapOf("monthly" to monthly, "total_interest" to totalInterest, "total_pay" to totalPay)
    }

    // --- 4. AUTO LOAN ---
    fun calculateAutoLoan(price: Double, down: Double, trade: Double, taxRate: Double, rate: Double, months: Int): Map<String, Double> {
        val priceWithTax = price * (1 + taxRate / 100.0)
        val financeAmount = max(0.0, priceWithTax - down - trade)
        val r = rate / 100.0 / 12.0
        val monthly = if (r == 0.0) financeAmount / months else financeAmount * (r * (1 + r).pow(months)) / ((1 + r).pow(months) - 1)
        return mapOf(
            "finance_amount" to financeAmount,
            "monthly" to monthly,
            "total_interest" to (monthly * months) - financeAmount,
            "sales_tax" to (price * taxRate / 100.0)
        )
    }

    // --- 5. RETIREMENT SAVINGS ---
    fun calculateRetirement(currentAge: Int, retireAge: Int, savings: Double, monthlyContribution: Double, returnRate: Double, inflation: Double): Map<String, Double> {
        val years = retireAge - currentAge
        val months = years * 12
        val nominalRate = returnRate / 100.0 / 12.0
        val realRate = (returnRate - inflation) / 100.0 / 12.0

        var balanceNominal = savings
        var balanceReal = savings
        for (i in 1..months) {
            balanceNominal = balanceNominal * (1 + nominalRate) + monthlyContribution
            balanceReal = balanceReal * (1 + realRate) + monthlyContribution
        }

        return mapOf("nominal_balance" to balanceNominal, "purchasing_power" to balanceReal)
    }

    // --- 6. AMORTIZATION ---
    fun generateAmortization(amount: Double, rate: Double, years: Int): List<Map<String, Any>> {
        val months = years * 12
        val r = rate / 100.0 / 12.0
        val monthlyPayment = if (r == 0.0) amount / months else amount * (r * (1 + r).pow(months)) / ((1 + r).pow(months) - 1)

        var remaining = amount
        val list = mutableListOf<Map<String, Any>>()
        for (i in 1..months) {
            val interest = remaining * r
            val principal = monthlyPayment - interest
            remaining = max(0.0, remaining - principal)
            list.add(
                mapOf(
                    "month" to i,
                    "payment" to monthlyPayment,
                    "principal" to principal,
                    "interest" to interest,
                    "remaining" to remaining
                )
            )
            if (remaining <= 0.0) break
        }
        return list
    }

    // --- 7. GENERAL TVM SOLVER ---
    fun solveTVM(pv: Double?, fv: Double?, pmt: Double?, n: Double?, rate: Double?): Map<String, Double> {
        val result = mutableMapOf<String, Double>()
        // Simplified solver logic
        if (pv == null && fv != null && pmt != null && n != null && rate != null) {
            val r = rate / 100.0
            val disc = (1 + r).pow(n)
            result["pv"] = -(fv + pmt * ((disc - 1) / r)) / disc
        } else if (fv == null && pv != null && pmt != null && n != null && rate != null) {
            val r = rate / 100.0
            val disc = (1 + r).pow(n)
            result["fv"] = -(pv * disc + pmt * ((disc - 1) / r))
        } else if (pmt == null && pv != null && fv != null && n != null && rate != null) {
            val r = rate / 100.0
            val disc = (1 + r).pow(n)
            result["pmt"] = -(pv * disc + fv) * r / (disc - 1)
        } else if (n == null && pv != null && fv != null && pmt != null && rate != null) {
            val r = rate / 100.0
            result["n"] = ln(-(fv * r - pmt) / (pv * r + pmt)) / ln(1 + r)
        }
        return result
    }

    // --- 8. INCOME TAX ---
    fun calculateIncomeTax(income: Double, filingStatus: String): Map<String, Double> {
        val standardDeduction = if (filingStatus.equals("married", true)) 30000.0 else 15000.0
        val taxableIncome = max(0.0, income - standardDeduction)

        // Standard progressive tiers (Simplified US Brackets)
        val tiers = listOf(
            Pair(11600.0, 0.10),
            Pair(47150.0, 0.12),
            Pair(100525.0, 0.22),
            Pair(191950.0, 0.24),
            Pair(243725.0, 0.32),
            Pair(609350.0, 0.35)
        )

        var tax = 0.0
        var remainingIncome = taxableIncome
        var lastLimit = 0.0

        for (tier in tiers) {
            val limit = tier.first
            val rate = tier.second
            val range = limit - lastLimit
            if (remainingIncome > range) {
                tax += range * rate
                remainingIncome -= range
                lastLimit = limit
            } else {
                tax += remainingIncome * rate
                remainingIncome = 0.0
                break
            }
        }
        if (remainingIncome > 0.0) {
            tax += remainingIncome * 0.37
        }

        return mapOf(
            "taxable_income" to taxableIncome,
            "tax_owed" to tax,
            "take_home" to (income - tax),
            "effective_rate" to if (income > 0) (tax / income) * 100 else 0.0
        )
    }

    // --- 9. BMI & HEALTH FORMULAS ---
    fun calculateBMI(weightKg: Double, heightCm: Double): Pair<Double, String> {
        val h = heightCm / 100.0
        if (h == 0.0) return Pair(0.0, "Invalid Input")
        val bmi = weightKg / (h * h)
        val category = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
        return Pair(bmi, category)
    }

    fun calculateCalorie(age: Int, gender: String, weight: Double, height: Double, activity: String, goal: String): Double {
        val bmr = if (gender.equals("male", true)) {
            10.0 * weight + 6.25 * height - 5.0 * age + 5.0
        } else {
            10.0 * weight + 6.25 * height - 5.0 * age - 161.0
        }
        val factor = when (activity.lowercase()) {
            "sedentary" -> 1.2
            "light" -> 1.375
            "active" -> 1.55
            "extreme" -> 1.725
            else -> 1.2
        }
        val tdee = bmr * factor
        return when (goal.lowercase()) {
            "lose" -> tdee - 500.0
            "gain" -> tdee + 400.0
            else -> tdee
        }
    }

    fun calculateBodyFat(gender: String, height: Double, neck: Double, waist: Double, hip: Double = 0.0): Double {
        return if (gender.equals("male", true)) {
            if (waist - neck <= 0) return 0.0
            86.010 * log10(waist - neck) - 70.041 * log10(height) + 36.76
        } else {
            if (waist + hip - neck <= 0) return 0.0
            163.205 * log10(waist + hip - neck) - 97.684 * log10(height) - 78.387
        }
    }

    // --- 10. DATE & TIME CALCULATORS ---
    fun calculateAge(birthDate: Date): Map<String, Long> {
        val today = Calendar.getInstance()
        val birth = Calendar.getInstance().apply { time = birthDate }

        var years = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        var months = today.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
        var days = today.get(Calendar.DAY_OF_MONTH) - birth.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months--
            today.add(Calendar.MONTH, -1)
            days += today.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (months < 0) {
            years--
            months += 12
        }

        val totalDays = (today.timeInMillis - birth.timeInMillis) / (24 * 60 * 60 * 1000)
        return mapOf(
            "years" to years.toLong(),
            "months" to months.toLong(),
            "days" to days.toLong(),
            "total_days" to totalDays
        )
    }

    fun calculateDueDate(lmp: Date): Map<String, Any> {
        val calendar = Calendar.getInstance().apply { time = lmp }
        calendar.add(Calendar.DAY_OF_YEAR, 280) // Naegele's rule equivalent: LMP + 280 days
        val dueDate = calendar.time

        val today = Calendar.getInstance()
        val diffMs = today.timeInMillis - lmp.time
        val gestationalDays = diffMs / (24 * 60 * 60 * 1000)
        val weeks = gestationalDays / 7
        val remainingDays = gestationalDays % 7

        return mapOf(
            "due_date" to dueDate,
            "gestational_weeks" to weeks,
            "gestational_days" to remainingDays,
            "trimester" to when {
                weeks < 13 -> 1
                weeks < 27 -> 2
                else -> 3
            }
        )
    }

    // --- 11. GENERAL UTILITIES (SUBNET, PASSWORD) ---
    fun calculateSubnet(ip: String, cidr: Int): Map<String, String> {
        try {
            val mask = -1 shl (32 - cidr)
            val maskStr = "${(mask ushr 24) and 0xff}.${(mask ushr 16) and 0xff}.${(mask ushr 8) and 0xff}.${mask and 0xff}"
            val parts = ip.split(".").map { it.toInt() }
            if (parts.size != 4) throw Exception()
            val ipInt = (parts[0] shl 24) or (parts[1] shl 16) or (parts[2] shl 8) or parts[3]
            val netInt = ipInt and mask
            val broadInt = netInt or mask.inv()

            val netStr = "${(netInt ushr 24) and 0xff}.${(netInt ushr 16) and 0xff}.${(netInt ushr 8) and 0xff}.${netInt and 0xff}"
            val broadStr = "${(broadInt ushr 24) and 0xff}.${(broadInt ushr 16) and 0xff}.${(broadInt ushr 8) and 0xff}.${broadInt and 0xff}"
            val hostCount = (2.0.pow(32 - cidr) - 2).toInt().coerceAtLeast(0)

            val minHost = netInt + 1
            val maxHost = broadInt - 1
            val minHostStr = "${(minHost ushr 24) and 0xff}.${(minHost ushr 16) and 0xff}.${(minHost ushr 8) and 0xff}.${minHost and 0xff}"
            val maxHostStr = "${(maxHost ushr 24) and 0xff}.${(maxHost ushr 16) and 0xff}.${(maxHost ushr 8) and 0xff}.${maxHost and 0xff}"

            return mapOf(
                "subnet_mask" to maskStr,
                "network_address" to netStr,
                "broadcast_address" to broadStr,
                "usable_range" to "$minHostStr - $maxHostStr",
                "total_hosts" to hostCount.toString()
            )
        } catch (e: Exception) {
            return mapOf("error" to "Invalid IP or CIDR Subnet mask.")
        }
    }

    fun generatePassword(length: Int, upper: Boolean, lower: Boolean, digits: Boolean, symbols: Boolean): String {
        val uppercasePool = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercasePool = "abcdefghijklmnopqrstuvwxyz"
        val digitsPool = "0123456789"
        val symbolsPool = "!@#$%^&*()_+-=[]{}|;':\",./<>?"

        val poolBuilder = StringBuilder()
        if (upper) poolBuilder.append(uppercasePool)
        if (lower) poolBuilder.append(lowercasePool)
        if (digits) poolBuilder.append(digitsPool)
        if (symbols) poolBuilder.append(symbolsPool)

        val pool = poolBuilder.toString()
        if (pool.isEmpty()) return ""

        val random = java.security.SecureRandom()
        return (1..length)
            .map { pool[random.nextInt(pool.length)] }
            .joinToString("")
    }

    // --- 12. CONCRETE ESTIMATOR ---
    fun calculateConcrete(lengthFt: Double, widthFt: Double, thicknessIn: Double): Map<String, Double> {
        val thickFt = thicknessIn / 12.0
        val volCuFt = lengthFt * widthFt * thickFt
        val volCuYd = volCuFt / 27.0
        // Standard bags
        val bags80lb = ceil(volCuYd * 45.0)
        val bags60lb = ceil(volCuYd * 60.0)
        return mapOf(
            "cubic_feet" to volCuFt,
            "cubic_yards" to volCuYd,
            "bags_80lb" to bags80lb,
            "bags_60lb" to bags60lb
        )
    }

    // --- 13. GPA CALCULATOR ---
    fun calculateGPA(grades: List<Pair<String, Double>>): Double {
        var totalPoints = 0.0
        var totalCredits = 0.0
        for (g in grades) {
            val weight = when (g.first.uppercase()) {
                "A", "A+" -> 4.0
                "A-" -> 3.7
                "B+" -> 3.3
                "B" -> 3.0
                "B-" -> 2.7
                "C+" -> 2.3
                "C" -> 2.0
                "C-" -> 1.7
                "D+" -> 1.3
                "D" -> 1.0
                "F" -> 0.0
                else -> 0.0
            }
            totalPoints += weight * g.second
            totalCredits += g.second
        }
        return if (totalCredits == 0.0) 0.0 else totalPoints / totalCredits
    }
}
