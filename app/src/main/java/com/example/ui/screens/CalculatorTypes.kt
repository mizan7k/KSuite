package com.example.ui.screens

enum class CalculatorCategory(val displayName: String) {
    FINANCIAL("Financial"),
    HEALTH("Health & Fitness"),
    MATH("Math & Algebra"),
    OTHER("Other Tools")
}

data class CalculatorMeta(
    val id: String,
    val name: String,
    val category: CalculatorCategory,
    val description: String,
    val iconName: String
)

object CalculatorRegistry {
    val list = listOf(
        // Math Calculators
        CalculatorMeta(
            id = "scientific",
            name = "Scientific Calculator",
            category = CalculatorCategory.MATH,
            description = "Trigonometric, logarithmic, exponentiation, and advanced matrix calculations.",
            iconName = "calculate"
        ),
        CalculatorMeta(
            id = "fraction",
            name = "Fraction Calculator",
            category = CalculatorCategory.MATH,
            description = "Add, subtract, multiply, and divide fractions and mixed numbers.",
            iconName = "fraction"
        ),
        CalculatorMeta(
            id = "percentage",
            name = "Percentage Calculator",
            category = CalculatorCategory.MATH,
            description = "Calculate percentage increases, decreases, differences, or parts.",
            iconName = "percent"
        ),
        CalculatorMeta(
            id = "random",
            name = "Random Number Generator",
            category = CalculatorCategory.MATH,
            description = "Generate random integers or decimals within a custom range.",
            iconName = "shuffle"
        ),
        CalculatorMeta(
            id = "triangle",
            name = "Triangle Calculator",
            category = CalculatorCategory.MATH,
            description = "Solve sides, angles, area, and perimeter of any triangle.",
            iconName = "change_history"
        ),
        CalculatorMeta(
            id = "std_dev",
            name = "Standard Deviation",
            category = CalculatorCategory.MATH,
            description = "Calculate mean, variance, and standard deviation for population or sample data.",
            iconName = "analytics"
        ),

        // Financial Calculators
        CalculatorMeta(
            id = "mortgage",
            name = "Mortgage Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Calculate monthly mortgage payments, property tax, and amortization.",
            iconName = "home"
        ),
        CalculatorMeta(
            id = "loan",
            name = "Loan Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Determine monthly payments, interest cost, and payoff times for standard loans.",
            iconName = "credit_card"
        ),
        CalculatorMeta(
            id = "auto_loan",
            name = "Auto Loan Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Estimate car loan payments including sales tax, trade-in, and interest.",
            iconName = "directions_car"
        ),
        CalculatorMeta(
            id = "interest",
            name = "Interest Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Calculate simple or compound interest earned over time.",
            iconName = "trending_up"
        ),
        CalculatorMeta(
            id = "payment",
            name = "Payment Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Find the exact payment amounts required to pay off a balance over a fixed term.",
            iconName = "payment"
        ),
        CalculatorMeta(
            id = "retirement",
            name = "Retirement Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Estimate how much you need to save to achieve your retirement goals.",
            iconName = "savings"
        ),
        CalculatorMeta(
            id = "amortization",
            name = "Amortization Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Generate full amortization schedules showing interest and principal components.",
            iconName = "table_chart"
        ),
        CalculatorMeta(
            id = "investment",
            name = "Investment Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Calculate future portfolio value based on regular contributions and interest.",
            iconName = "insights"
        ),
        CalculatorMeta(
            id = "inflation",
            name = "Inflation Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Compare money purchasing power over time using historical CPI rates.",
            iconName = "price_change"
        ),
        CalculatorMeta(
            id = "finance",
            name = "Finance Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Calculate Time Value of Money (TVM) parameters: PV, FV, PMT, N, and I/Y.",
            iconName = "monetization_on"
        ),
        CalculatorMeta(
            id = "income_tax",
            name = "Income Tax Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Estimate federal income tax brackets, deductions, and take-home pay.",
            iconName = "account_balance"
        ),
        CalculatorMeta(
            id = "compound_interest",
            name = "Compound Interest",
            category = CalculatorCategory.FINANCIAL,
            description = "Calculate the growth of your investments with standard compounding intervals.",
            iconName = "show_chart"
        ),
        CalculatorMeta(
            id = "salary",
            name = "Salary Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Convert hourly wages to weekly, biweekly, monthly, or annual salary equivalents.",
            iconName = "work"
        ),
        CalculatorMeta(
            id = "interest_rate",
            name = "Interest Rate Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Find the effective interest rate of an investment or financing deal.",
            iconName = "percent"
        ),
        CalculatorMeta(
            id = "sales_tax",
            name = "Sales Tax Calculator",
            category = CalculatorCategory.FINANCIAL,
            description = "Calculate net price, gross price, and tax amounts for purchases.",
            iconName = "receipt"
        ),

        // Fitness & Health Calculators
        CalculatorMeta(
            id = "bmi",
            name = "BMI Calculator",
            category = CalculatorCategory.HEALTH,
            description = "Calculate Body Mass Index (BMI) and find your weight category.",
            iconName = "accessibility"
        ),
        CalculatorMeta(
            id = "calorie",
            name = "Calorie Calculator",
            category = CalculatorCategory.HEALTH,
            description = "Estimate daily calorie requirements based on your activity level and fitness goals.",
            iconName = "restaurant"
        ),
        CalculatorMeta(
            id = "body_fat",
            name = "Body Fat Calculator",
            category = CalculatorCategory.HEALTH,
            description = "Estimate your body fat percentage using US Navy circumference measurements.",
            iconName = "fitness_center"
        ),
        CalculatorMeta(
            id = "bmr",
            name = "BMR Calculator",
            category = CalculatorCategory.HEALTH,
            description = "Find your Basal Metabolic Rate (BMR) using Mifflin-St Jeor formulas.",
            iconName = "bolt"
        ),
        CalculatorMeta(
            id = "ideal_weight",
            name = "Ideal Weight Calculator",
            category = CalculatorCategory.HEALTH,
            description = "Determine healthy weight ranges based on popular medical formulas.",
            iconName = "scale"
        ),
        CalculatorMeta(
            id = "pace",
            name = "Pace Calculator",
            category = CalculatorCategory.HEALTH,
            description = "Convert running speed, distance, and time to calculate average pace.",
            iconName = "speed"
        ),
        CalculatorMeta(
            id = "pregnancy",
            name = "Pregnancy Calculator",
            category = CalculatorCategory.HEALTH,
            description = "Generate a comprehensive pregnancy timeline, trimesters, and baby milestones.",
            iconName = "child_care"
        ),
        CalculatorMeta(
            id = "conception",
            name = "Pregnancy Conception",
            category = CalculatorCategory.HEALTH,
            description = "Estimate conception dates, ovulation windows, and fertile cycles.",
            iconName = "calendar_today"
        ),
        CalculatorMeta(
            id = "due_date",
            name = "Due Date Calculator",
            category = CalculatorCategory.HEALTH,
            description = "Calculate the estimated due date based on LMP (Last Menstrual Period).",
            iconName = "event"
        ),

        // Other Calculators
        CalculatorMeta(
            id = "age",
            name = "Age Calculator",
            category = CalculatorCategory.OTHER,
            description = "Calculate exact age in years, months, weeks, days, hours, and minutes.",
            iconName = "cake"
        ),
        CalculatorMeta(
            id = "date",
            name = "Date Calculator",
            category = CalculatorCategory.OTHER,
            description = "Add or subtract days, weeks, months, or years from any date.",
            iconName = "date_range"
        ),
        CalculatorMeta(
            id = "time",
            name = "Time Calculator",
            category = CalculatorCategory.OTHER,
            description = "Add or subtract time durations (hours, minutes, seconds) together.",
            iconName = "schedule"
        ),
        CalculatorMeta(
            id = "hours",
            name = "Hours Calculator",
            category = CalculatorCategory.OTHER,
            description = "Sum timesheet records or calculate working hours between specific times.",
            iconName = "hourglass_empty"
        ),
        CalculatorMeta(
            id = "gpa",
            name = "GPA Calculator",
            category = CalculatorCategory.OTHER,
            description = "Calculate high school or college GPA based on letter grades and credits.",
            iconName = "school"
        ),
        CalculatorMeta(
            id = "grade",
            name = "Grade Calculator",
            category = CalculatorCategory.OTHER,
            description = "Determine current class grade or find the score needed on a final exam.",
            iconName = "assignment"
        ),
        CalculatorMeta(
            id = "concrete",
            name = "Concrete Calculator",
            category = CalculatorCategory.OTHER,
            description = "Estimate concrete volume and bags needed for slabs, walls, or columns.",
            iconName = "foundation"
        ),
        CalculatorMeta(
            id = "subnet",
            name = "Subnet Calculator",
            category = CalculatorCategory.OTHER,
            description = "Calculate network CIDR ranges, subnets, host IP ranges, and masks.",
            iconName = "dns"
        ),
        CalculatorMeta(
            id = "password",
            name = "Password Generator",
            category = CalculatorCategory.OTHER,
            description = "Generate highly secure random passwords with custom parameters.",
            iconName = "vpn_key"
        ),
        CalculatorMeta(
            id = "conversion",
            name = "Conversion Calculator",
            category = CalculatorCategory.OTHER,
            description = "Quickly convert units of length, weight, area, volume, temperature, and more.",
            iconName = "compare_arrows"
        )
    )
}
