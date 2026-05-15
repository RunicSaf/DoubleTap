package com.saf.doubletap

object ShortcutTrigger {
    const val POWER_DOUBLE = "POWER_DOUBLE"
    const val VOLUME_UP_DOUBLE = "VOLUME_UP_DOUBLE"
    const val VOLUME_DOWN_DOUBLE = "VOLUME_DOWN_DOUBLE"

    const val POWER_TRIPLE = "POWER_TRIPLE"
    const val VOLUME_UP_TRIPLE = "VOLUME_UP_TRIPLE"
    const val VOLUME_DOWN_TRIPLE = "VOLUME_DOWN_TRIPLE"

    const val VOLUME_BOTH = "VOLUME_BOTH"
    const val POWER_AND_VOLUME_UP = "POWER_AND_VOLUME_UP"
    const val POWER_AND_VOLUME_DOWN = "POWER_AND_VOLUME_DOWN"

    const val VOLUME_UP_THEN_DOWN = "VOLUME_UP_THEN_DOWN"
    const val VOLUME_DOWN_THEN_UP = "VOLUME_DOWN_THEN_UP"
    const val POWER_THEN_VOLUME_UP = "POWER_THEN_VOLUME_UP"
    const val POWER_THEN_VOLUME_DOWN = "POWER_THEN_VOLUME_DOWN"
    const val VOLUME_UP_THEN_POWER = "VOLUME_UP_THEN_POWER"
    const val VOLUME_DOWN_THEN_POWER = "VOLUME_DOWN_THEN_POWER"

    const val POWER_UP_DOWN = "POWER_UP_DOWN"
    const val POWER_DOWN_UP = "POWER_DOWN_UP"
    const val UP_POWER_DOWN = "UP_POWER_DOWN"
    const val UP_DOWN_POWER = "UP_DOWN_POWER"
    const val DOWN_POWER_UP = "DOWN_POWER_UP"
    const val DOWN_UP_POWER = "DOWN_UP_POWER"

    fun all(): List<String> = listOf(
        VOLUME_UP_DOUBLE,
        VOLUME_DOWN_DOUBLE,
        VOLUME_UP_TRIPLE,
        VOLUME_DOWN_TRIPLE,
        VOLUME_BOTH,
        VOLUME_UP_THEN_DOWN,
        VOLUME_DOWN_THEN_UP,

        POWER_DOUBLE,
        POWER_TRIPLE,
        POWER_AND_VOLUME_UP,
        POWER_AND_VOLUME_DOWN,
        POWER_THEN_VOLUME_UP,
        POWER_THEN_VOLUME_DOWN,
        VOLUME_UP_THEN_POWER,
        VOLUME_DOWN_THEN_POWER,

        POWER_UP_DOWN,
        POWER_DOWN_UP,
        UP_POWER_DOWN,
        UP_DOWN_POWER,
        DOWN_POWER_UP,
        DOWN_UP_POWER
    )

    fun label(trigger: String): String = when (trigger) {
        POWER_DOUBLE -> "Double press power"
        VOLUME_UP_DOUBLE -> "Double press volume up"
        VOLUME_DOWN_DOUBLE -> "Double press volume down"

        POWER_TRIPLE -> "Triple press power"
        VOLUME_UP_TRIPLE -> "Triple press volume up"
        VOLUME_DOWN_TRIPLE -> "Triple press volume down"

        VOLUME_BOTH -> "Press volume up + down together"
        POWER_AND_VOLUME_UP -> "Press power + volume up together"
        POWER_AND_VOLUME_DOWN -> "Press power + volume down together"

        VOLUME_UP_THEN_DOWN -> "Volume up then down"
        VOLUME_DOWN_THEN_UP -> "Volume down then up"
        POWER_THEN_VOLUME_UP -> "Power then volume up"
        POWER_THEN_VOLUME_DOWN -> "Power then volume down"
        VOLUME_UP_THEN_POWER -> "Volume up then power"
        VOLUME_DOWN_THEN_POWER -> "Volume down then power"

        POWER_UP_DOWN -> "Power, up, down"
        POWER_DOWN_UP -> "Power, down, up"
        UP_POWER_DOWN -> "Up, power, down"
        UP_DOWN_POWER -> "Up, down, power"
        DOWN_POWER_UP -> "Down, power, up"
        DOWN_UP_POWER -> "Down, up, power"

        else -> trigger
    }

    fun symbol(trigger: String): String = when (trigger) {
        POWER_DOUBLE -> "⏻⏻"
        VOLUME_UP_DOUBLE -> "↑↑"
        VOLUME_DOWN_DOUBLE -> "↓↓"

        POWER_TRIPLE -> "⏻⏻⏻"
        VOLUME_UP_TRIPLE -> "↑↑↑"
        VOLUME_DOWN_TRIPLE -> "↓↓↓"

        VOLUME_BOTH -> "↑↓"
        POWER_AND_VOLUME_UP -> "⏻↑"
        POWER_AND_VOLUME_DOWN -> "⏻↓"

        VOLUME_UP_THEN_DOWN -> "↑→↓"
        VOLUME_DOWN_THEN_UP -> "↓→↑"
        POWER_THEN_VOLUME_UP -> "⏻→↑"
        POWER_THEN_VOLUME_DOWN -> "⏻→↓"
        VOLUME_UP_THEN_POWER -> "↑→⏻"
        VOLUME_DOWN_THEN_POWER -> "↓→⏻"

        POWER_UP_DOWN -> "⏻→↑→↓"
        POWER_DOWN_UP -> "⏻→↓→↑"
        UP_POWER_DOWN -> "↑→⏻→↓"
        UP_DOWN_POWER -> "↑→↓→⏻"
        DOWN_POWER_UP -> "↓→⏻→↑"
        DOWN_UP_POWER -> "↓→↑→⏻"

        else -> "•"
    }

    fun hint(trigger: String): String = when (trigger) {
        POWER_DOUBLE -> "Press power twice quickly."
        VOLUME_UP_DOUBLE -> "Press volume up twice quickly."
        VOLUME_DOWN_DOUBLE -> "Press volume down twice quickly."

        POWER_TRIPLE -> "Press power three times quickly."
        VOLUME_UP_TRIPLE -> "Press volume up three times quickly."
        VOLUME_DOWN_TRIPLE -> "Press volume down three times quickly."

        VOLUME_BOTH -> "Press both volume keys together."
        POWER_AND_VOLUME_UP -> "Press power and volume up together."
        POWER_AND_VOLUME_DOWN -> "Press power and volume down together."

        VOLUME_UP_THEN_DOWN -> "Press volume up, then volume down."
        VOLUME_DOWN_THEN_UP -> "Press volume down, then volume up."
        POWER_THEN_VOLUME_UP -> "Press power, then volume up."
        POWER_THEN_VOLUME_DOWN -> "Press power, then volume down."
        VOLUME_UP_THEN_POWER -> "Press volume up, then power."
        VOLUME_DOWN_THEN_POWER -> "Press volume down, then power."

        POWER_UP_DOWN -> "Press power, up, then down."
        POWER_DOWN_UP -> "Press power, down, then up."
        UP_POWER_DOWN -> "Press up, power, then down."
        UP_DOWN_POWER -> "Press up, down, then power."
        DOWN_POWER_UP -> "Press down, power, then up."
        DOWN_UP_POWER -> "Press down, up, then power."

        else -> ""
    }

    fun keyNameFor(keyCode: Int): String = when (keyCode) {
        android.view.KeyEvent.KEYCODE_POWER -> "P"
        android.view.KeyEvent.KEYCODE_VOLUME_UP -> "U"
        android.view.KeyEvent.KEYCODE_VOLUME_DOWN -> "D"
        else -> "?"
    }
}
