package com.saf.doubletap

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import kotlin.math.abs

class DoubletapAccessibilityService : AccessibilityService() {

    private var lastLaunchAt = 0L

    private val recentKeys = ArrayDeque<Pair<Int, Long>>()

    private var powerDownAt = 0L
    private var volumeUpDownAt = 0L
    private var volumeDownDownAt = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.repeatCount > 0) return false

        val now = SystemClock.elapsedRealtime()
        val matchedTriggers = mutableSetOf<String>()

        if (event.action == KeyEvent.ACTION_DOWN) {
            addRecentKey(event.keyCode, now)

            detectDoubleOrTriple(event.keyCode, now)?.let { matchedTriggers.add(it) }
            detectTwoKeySequence(now)?.let { matchedTriggers.add(it) }
            detectThreeKeySequence(now)?.let { matchedTriggers.add(it) }
        }

        detectHeldCombination(event, now)?.let { matchedTriggers.add(it) }

        matchedTriggers.forEach { trigger ->
            triggerLaunch(trigger, now)
        }

        return false
    }

    private fun addRecentKey(keyCode: Int, now: Long) {
        if (!isSupportedKey(keyCode)) return

        recentKeys.addLast(keyCode to now)

        while (recentKeys.size > 3) {
            recentKeys.removeFirst()
        }
    }

    private fun detectDoubleOrTriple(keyCode: Int, now: Long): String? {
        if (!isSupportedKey(keyCode)) return null

        val windowMs = BindingStore.getWindowMs(this)
        val sameRecent = recentKeys
            .filter { it.first == keyCode && now - it.second <= windowMs }
            .takeLast(3)

        if (sameRecent.size >= 3) {
            recentKeys.clear()
            return when (keyCode) {
                KeyEvent.KEYCODE_POWER -> ShortcutTrigger.POWER_TRIPLE
                KeyEvent.KEYCODE_VOLUME_UP -> ShortcutTrigger.VOLUME_UP_TRIPLE
                KeyEvent.KEYCODE_VOLUME_DOWN -> ShortcutTrigger.VOLUME_DOWN_TRIPLE
                else -> null
            }
        }

        if (sameRecent.size == 2) {
            return when (keyCode) {
                KeyEvent.KEYCODE_POWER -> ShortcutTrigger.POWER_DOUBLE
                KeyEvent.KEYCODE_VOLUME_UP -> ShortcutTrigger.VOLUME_UP_DOUBLE
                KeyEvent.KEYCODE_VOLUME_DOWN -> ShortcutTrigger.VOLUME_DOWN_DOUBLE
                else -> null
            }
        }

        return null
    }

    private fun detectTwoKeySequence(now: Long): String? {
        if (recentKeys.size < 2) return null

        val windowMs = BindingStore.getWindowMs(this)
        val lastTwo = recentKeys.takeLast(2)

        if (now - lastTwo.first().second > windowMs) return null

        val a = lastTwo[0].first
        val b = lastTwo[1].first

        if (a == b) return null

        return when (ShortcutTrigger.keyNameFor(a) + ShortcutTrigger.keyNameFor(b)) {
            "UD" -> ShortcutTrigger.VOLUME_UP_THEN_DOWN
            "DU" -> ShortcutTrigger.VOLUME_DOWN_THEN_UP
            "PU" -> ShortcutTrigger.POWER_THEN_VOLUME_UP
            "PD" -> ShortcutTrigger.POWER_THEN_VOLUME_DOWN
            "UP" -> ShortcutTrigger.VOLUME_UP_THEN_POWER
            "DP" -> ShortcutTrigger.VOLUME_DOWN_THEN_POWER
            else -> null
        }
    }

    private fun detectThreeKeySequence(now: Long): String? {
        if (recentKeys.size < 3) return null

        val windowMs = BindingStore.getWindowMs(this)
        val lastThree = recentKeys.takeLast(3)

        if (now - lastThree.first().second > windowMs * 2) return null

        val keys = lastThree.map { ShortcutTrigger.keyNameFor(it.first) }.joinToString("")

        return when (keys) {
            "PUD" -> ShortcutTrigger.POWER_UP_DOWN
            "PDU" -> ShortcutTrigger.POWER_DOWN_UP
            "UPD" -> ShortcutTrigger.UP_POWER_DOWN
            "UDP" -> ShortcutTrigger.UP_DOWN_POWER
            "DPU" -> ShortcutTrigger.DOWN_POWER_UP
            "DUP" -> ShortcutTrigger.DOWN_UP_POWER
            else -> null
        }
    }

    private fun detectHeldCombination(event: KeyEvent, now: Long): String? {
        when (event.keyCode) {
            KeyEvent.KEYCODE_POWER -> {
                if (event.action == KeyEvent.ACTION_DOWN) powerDownAt = now
                if (event.action == KeyEvent.ACTION_UP) powerDownAt = 0L
            }

            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (event.action == KeyEvent.ACTION_DOWN) volumeUpDownAt = now
                if (event.action == KeyEvent.ACTION_UP) volumeUpDownAt = 0L
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event.action == KeyEvent.ACTION_DOWN) volumeDownDownAt = now
                if (event.action == KeyEvent.ACTION_UP) volumeDownDownAt = 0L
            }

            else -> return null
        }

        val comboWindowMs = 350L

        val powerUp =
            powerDownAt > 0L &&
                volumeUpDownAt > 0L &&
                abs(powerDownAt - volumeUpDownAt) <= comboWindowMs

        val powerDown =
            powerDownAt > 0L &&
                volumeDownDownAt > 0L &&
                abs(powerDownAt - volumeDownDownAt) <= comboWindowMs

        val upDown =
            volumeUpDownAt > 0L &&
                volumeDownDownAt > 0L &&
                abs(volumeUpDownAt - volumeDownDownAt) <= comboWindowMs

        val trigger = when {
            powerUp -> ShortcutTrigger.POWER_AND_VOLUME_UP
            powerDown -> ShortcutTrigger.POWER_AND_VOLUME_DOWN
            upDown -> ShortcutTrigger.VOLUME_BOTH
            else -> null
        }

        if (trigger != null) {
            powerDownAt = 0L
            volumeUpDownAt = 0L
            volumeDownDownAt = 0L
        }

        return trigger
    }

    private fun triggerLaunch(trigger: String, now: Long) {
        if (now - lastLaunchAt <= 900L) return

        val binding = BindingStore.findBindingForTrigger(this, trigger) ?: return

        lastLaunchAt = now
        recentKeys.clear()
        launchBoundApp(binding)
    }

    private fun launchBoundApp(binding: AppBinding) {
        val launchIntent = packageManager.getLaunchIntentForPackage(binding.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

        if (launchIntent == null) {
            Toast.makeText(this, "Doubletap: ${binding.appLabel} not found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            startActivity(launchIntent)
        } catch (_: Exception) {
            Toast.makeText(this, "Doubletap: launch blocked by Android", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isSupportedKey(keyCode: Int): Boolean =
        keyCode == KeyEvent.KEYCODE_POWER ||
            keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
}
