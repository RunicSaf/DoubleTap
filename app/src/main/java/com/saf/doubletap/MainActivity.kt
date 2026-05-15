package com.saf.doubletap

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var bindingsBox: LinearLayout
    private lateinit var windowSeek: SeekBar
    private lateinit var windowText: TextView
    private lateinit var keepActiveSwitch: Switch

    private val bg = Color.rgb(14, 15, 18)
    private val surface = Color.rgb(25, 27, 33)
    private val surfaceSoft = Color.rgb(32, 35, 43)
    private val textPrimary = Color.WHITE
    private val textSecondary = Color.rgb(178, 185, 202)
    private val accent = Color.rgb(47, 107, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun buildUi() {
        val root = ScrollView(this)
        root.setBackgroundColor(bg)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(28))
        }

        root.addView(content)
        setContentView(root)

        content.addView(header())
        content.addView(section("Bindings"))

        bindingsBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(bindingsBox)
        content.addView(primaryButton("Add binding") { showTriggerPicker() })

        content.addView(section("Keep active"))

        keepActiveSwitch = Switch(this).apply {
            text = "Keep Doubletap active"
            setTextColor(textPrimary)
            textSize = 16f
            isChecked = BindingStore.isKeepActiveEnabled(this@MainActivity)
            setOnCheckedChangeListener { _, checked ->
                BindingStore.setKeepActive(this@MainActivity, checked)

                if (checked) {
                    requestNotificationPermissionIfNeeded()
                    startKeepAliveService()
                } else {
                    stopKeepAliveService()
                }

                refreshUi()
            }
        }

        val keepActiveCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(keepActiveSwitch)
            addView(body("Keeps shortcuts available more reliably when Doubletap is not open."))
        }

        content.addView(card(keepActiveCard))

        content.addView(section("Timing"))

        windowText = bodyLarge("")

        windowSeek = SeekBar(this).apply {
            max = 950
            progress = (BindingStore.getWindowMs(this@MainActivity) - 250L).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = progress + 250L
                    BindingStore.setWindowMs(this@MainActivity, value)
                    windowText.text = "$value ms"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }

        val timingBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(windowText)
            addView(windowSeek)
            addView(body("Lower is faster. Higher is easier for longer gestures."))
        }

        content.addView(card(timingBox))

        content.addView(section("Status"))

        statusText = bodyLarge("")
        content.addView(card(statusText))

        content.addView(primaryButton("Open Accessibility settings") { openAccessibilitySettings() })
        content.addView(secondaryButton("Open battery settings") { openBatterySettings() })
        content.addView(secondaryButton("Open app settings") { openAppSettings() })

        refreshUi()
    }

    private fun header(): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(8))
        }

        val headline = TextView(this).apply {
            text = "Hardware shortcuts"
            setTextColor(textPrimary)
            textSize = 34f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(2), 0, dp(6))
        }

        val subhead = body("Bind button gestures to the apps you use fastest.")

        box.addView(headline)
        box.addView(subhead)

        return box
    }

    private fun showTriggerPicker() {
        val triggers = ShortcutTrigger.all()
        val list = ListView(this).apply {
            divider = null
            adapter = TriggerAdapter(triggers)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Choose trigger")
            .setView(list)
            .setNegativeButton("Cancel", null)
            .create()

        list.setOnItemClickListener { _, _, position, _ ->
            dialog.dismiss()
            showAppPicker(triggers[position])
        }

        dialog.show()
    }

    private fun showAppPicker(trigger: String) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = packageManager.queryIntentActivities(launcherIntent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase(Locale.getDefault()) }

        val list = ListView(this).apply {
            divider = null
            adapter = AppAdapter(apps)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Choose app")
            .setView(list)
            .setNegativeButton("Cancel", null)
            .create()

        list.setOnItemClickListener { _, _, position, _ ->
            val app = apps[position]
            val appLabel = app.loadLabel(packageManager).toString()

            BindingStore.addBinding(
                context = this,
                trigger = trigger,
                packageName = app.activityInfo.packageName,
                appLabel = appLabel
            )

            dialog.dismiss()
            refreshUi()
            Toast.makeText(this, "Bound ${ShortcutTrigger.label(trigger)} to $appLabel", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun refreshUi() {
        refreshBindings()

        val enabled = isDoubletapAccessibilityEnabled()
        val batteryIgnored = isIgnoringBatteryOptimisations()
        val keepActive = BindingStore.isKeepActiveEnabled(this)

        statusText.text =
            "Accessibility: ${if (enabled) "Enabled" else "Disabled"}\n" +
                "Keep active: ${if (keepActive) "Enabled" else "Disabled"}\n" +
                "Battery optimisation ignored: ${if (batteryIgnored) "Yes" else "No / unknown"}"

        if (::keepActiveSwitch.isInitialized) {
            keepActiveSwitch.isChecked = keepActive
        }

        val window = BindingStore.getWindowMs(this)
        windowText.text = "$window ms"
        windowSeek.progress = (window - 250L).toInt().coerceIn(0, 950)

        if (keepActive) startKeepAliveService()
    }

    private fun refreshBindings() {
        bindingsBox.removeAllViews()

        val bindings = BindingStore.getBindings(this)

        if (bindings.isEmpty()) {
            bindingsBox.addView(card(bodyLarge("No bindings yet.")))
            return
        }

        bindings.forEach { binding ->
            bindingsBox.addView(bindingCard(binding))
        }
    }

    private fun bindingCard(binding: AppBinding): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val icon = ImageView(this).apply {
            setImageDrawable(getAppIcon(binding.packageName))
            layoutParams = LinearLayout.LayoutParams(dp(48), dp(48)).apply {
                rightMargin = dp(12)
            }
        }

        val textBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        textBox.addView(bodyLarge(binding.appLabel))
        textBox.addView(body("${ShortcutTrigger.symbol(binding.trigger)}  ${ShortcutTrigger.label(binding.trigger)}"))

        val remove = TextView(this).apply {
            text = "Remove"
            setTextColor(Color.rgb(255, 120, 120))
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setOnClickListener {
                BindingStore.removeBinding(this@MainActivity, binding.id)
                refreshUi()
            }
        }

        row.addView(icon)
        row.addView(textBox)
        row.addView(remove)

        return card(row)
    }

    private fun getAppIcon(packageName: String) =
        try {
            packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            getDrawable(android.R.drawable.sym_def_app_icon)
        }

    private fun isDoubletapAccessibilityEnabled(): Boolean {
        val manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any { info ->
            info.resolveInfo.serviceInfo.packageName == packageName &&
                info.resolveInfo.serviceInfo.name == DoubletapAccessibilityService::class.java.name
        }
    }

    private fun isIgnoringBatteryOptimisations(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    private fun startKeepAliveService() {
        val intent = Intent(this, DoubletapKeepAliveService::class.java)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (_: Exception) {
            Toast.makeText(this, "Could not start keep-active service", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopKeepAliveService() {
        stopService(Intent(this, DoubletapKeepAliveService::class.java))
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 44)
        }
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun openBatterySettings() {
        val intents = listOf(
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )

        for (intent in intents) {
            try {
                startActivity(intent)
                return
            } catch (_: Exception) {
                // Try next generic settings screen.
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private inner class TriggerAdapter(
        private val triggers: List<String>
    ) : BaseAdapter() {
        override fun getCount(): Int = triggers.size
        override fun getItem(position: Int): Any = triggers[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val trigger = triggers[position]

            val row = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(14), dp(10), dp(14), dp(10))
            }

            val symbol = TextView(this@MainActivity).apply {
                text = ShortcutTrigger.symbol(trigger)
                textSize = if (ShortcutTrigger.symbol(trigger).length > 4) 16f else 24f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(accent)
                gravity = android.view.Gravity.CENTER
                background = rounded(surfaceSoft, dp(12))
                layoutParams = LinearLayout.LayoutParams(dp(82), dp(52)).apply {
                    rightMargin = dp(12)
                }
            }

            val text = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
            }

            text.addView(bodyLarge(ShortcutTrigger.label(trigger)))
            text.addView(body(ShortcutTrigger.hint(trigger)))

            row.addView(symbol)
            row.addView(text)

            return row
        }
    }

    private inner class AppAdapter(
        private val apps: List<ResolveInfo>
    ) : BaseAdapter() {
        override fun getCount(): Int = apps.size
        override fun getItem(position: Int): Any = apps[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val app = apps[position]
            val label = app.loadLabel(packageManager).toString()

            val row = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(14), dp(10), dp(14), dp(10))
            }

            val icon = ImageView(this@MainActivity).apply {
                setImageDrawable(app.loadIcon(packageManager))
                layoutParams = LinearLayout.LayoutParams(dp(42), dp(42)).apply {
                    rightMargin = dp(14)
                }
            }

            val labelView = TextView(this@MainActivity).apply {
                text = label
                setTextColor(Color.rgb(20, 22, 28))
                textSize = 17f
                typeface = Typeface.DEFAULT_BOLD
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, dp(42), 1f)
            }

            row.addView(icon)
            row.addView(labelView)

            return row
        }
    }

    private fun section(text: String): TextView = TextView(this).apply {
        this.text = text.uppercase(Locale.getDefault())
        setTextColor(textSecondary)
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
        letterSpacing = 0.08f
        setPadding(0, dp(24), 0, dp(8))
    }

    private fun body(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(textSecondary)
        textSize = 15f
        setLineSpacing(4f, 1.0f)
    }

    private fun bodyLarge(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(textPrimary)
        textSize = 16f
        setLineSpacing(4f, 1.0f)
    }

    private fun primaryButton(text: String, action: () -> Unit): Button =
        baseButton(text, accent, textPrimary, action)

    private fun secondaryButton(text: String, action: () -> Unit): Button =
        baseButton(text, surfaceSoft, textPrimary, action)

    private fun baseButton(text: String, color: Int, textColor: Int, action: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            isAllCaps = false
            textSize = 15f
            setTextColor(textColor)
            background = rounded(color, dp(14))
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        }

    private fun card(child: View): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(14), dp(16), dp(14))
        background = rounded(surface, dp(18))
        addView(child)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(10)
        }
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
