package com.saf.doubletap

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object BindingStore {
    private const val PREFS = "doubletap_settings"
    private const val KEY_BINDINGS = "bindings"
    private const val KEY_WINDOW_MS = "double_press_window_ms"
    private const val KEY_KEEP_ACTIVE = "keep_active"

    fun getBindings(context: Context): List<AppBinding> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_BINDINGS, "[]") ?: "[]"

        val array = try {
            JSONArray(raw)
        } catch (_: Exception) {
            JSONArray()
        }

        val result = mutableListOf<AppBinding>()

        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue

            val id = obj.optLong("id", 0L)
            val trigger = obj.optString("trigger", "")
            val packageName = obj.optString("packageName", "")
            val appLabel = obj.optString("appLabel", "")

            if (id > 0L && trigger.isNotBlank() && packageName.isNotBlank() && appLabel.isNotBlank()) {
                result.add(
                    AppBinding(
                        id = id,
                        trigger = trigger,
                        packageName = packageName,
                        appLabel = appLabel
                    )
                )
            }
        }

        return result
    }

    fun addBinding(context: Context, trigger: String, packageName: String, appLabel: String) {
        val bindings = getBindings(context).toMutableList()
        bindings.removeAll { it.trigger == trigger }

        bindings.add(
            AppBinding(
                id = System.currentTimeMillis(),
                trigger = trigger,
                packageName = packageName,
                appLabel = appLabel
            )
        )

        saveBindings(context, bindings)
    }

    fun removeBinding(context: Context, id: Long) {
        val bindings = getBindings(context).filterNot { it.id == id }
        saveBindings(context, bindings)
    }

    fun findBindingForTrigger(context: Context, trigger: String): AppBinding? =
        getBindings(context).firstOrNull { it.trigger == trigger }

    private fun saveBindings(context: Context, bindings: List<AppBinding>) {
        val array = JSONArray()

        bindings.forEach { binding ->
            array.put(
                JSONObject()
                    .put("id", binding.id)
                    .put("trigger", binding.trigger)
                    .put("packageName", binding.packageName)
                    .put("appLabel", binding.appLabel)
            )
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BINDINGS, array.toString())
            .apply()
    }

    fun setWindowMs(context: Context, value: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_WINDOW_MS, value.coerceIn(250L, 1200L))
            .apply()
    }

    fun getWindowMs(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_WINDOW_MS, 500L)

    fun setKeepActive(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_KEEP_ACTIVE, enabled)
            .apply()
    }

    fun isKeepActiveEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_KEEP_ACTIVE, false)
}
