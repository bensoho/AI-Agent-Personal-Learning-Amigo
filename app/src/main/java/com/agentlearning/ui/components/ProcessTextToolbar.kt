package com.agentlearning.ui.components

import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

/** Replaces Compose's selection toolbar with one that also lists the system's text actions such as "翻译". */
@Composable
fun ProvideProcessTextToolbar(content: @Composable () -> Unit) {
    val view = LocalView.current
    val toolbar = remember(view) { ProcessTextToolbar(view) }
    CompositionLocalProvider(LocalTextToolbar provides toolbar, content = content)
}

/**
 * Floating text toolbar with copy / paste / cut / select all plus the installed ACTION_PROCESS_TEXT
 * handlers (the phone's translator, ...), like a platform TextView offers; duplicate translators
 * from browsers and Google Translate are hidden. Compose's own toolbar does not include these actions.
 */
private class ProcessTextToolbar(private val view: View) : TextToolbar {

    private var actionMode: ActionMode? = null
    private var contentRect = Rect.Zero
    private var onCopy: (() -> Unit)? = null
    private var onPaste: (() -> Unit)? = null
    private var onCut: (() -> Unit)? = null
    private var onSelectAll: (() -> Unit)? = null
    private var processTextActivities: List<ResolveInfo> = emptyList()

    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
        private set

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
    ) {
        contentRect = rect
        onCopy = onCopyRequested
        onPaste = onPasteRequested
        onCut = onCutRequested
        onSelectAll = onSelectAllRequested
        if (actionMode == null) {
            status = TextToolbarStatus.Shown
            actionMode = view.startActionMode(callback, ActionMode.TYPE_FLOATING)
        } else {
            actionMode?.invalidate()
        }
    }

    override fun hide() {
        status = TextToolbarStatus.Hidden
        actionMode?.finish()
        actionMode = null
    }

    private val callback = object : ActionMode.Callback2() {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            populate(menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.clear()
            populate(menu)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                ID_COPY -> onCopy?.invoke()
                ID_PASTE -> onPaste?.invoke()
                ID_CUT -> onCut?.invoke()
                ID_SELECT_ALL -> onSelectAll?.invoke()
                else -> {
                    val info = processTextActivities.getOrNull(item.itemId - ID_PROCESS_TEXT_BASE) ?: return false
                    processSelectedText(info)
                }
            }
            mode.finish()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            status = TextToolbarStatus.Hidden
        }

        override fun onGetContentRect(mode: ActionMode, view: View, outRect: android.graphics.Rect) {
            outRect.set(
                contentRect.left.toInt(),
                contentRect.top.toInt(),
                contentRect.right.toInt(),
                contentRect.bottom.toInt(),
            )
        }
    }

    private fun populate(menu: Menu) {
        var order = 0
        onCopy?.let { menu.add(0, ID_COPY, order++, android.R.string.copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS) }
        onPaste?.let { menu.add(0, ID_PASTE, order++, android.R.string.paste).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS) }
        onCut?.let { menu.add(0, ID_CUT, order++, android.R.string.cut).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS) }
        onSelectAll?.let {
            menu.add(0, ID_SELECT_ALL, order++, android.R.string.selectAll).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }

        // The selected text can only be read through the copy callback, so these need a copyable selection.
        processTextActivities = if (onCopy != null) queryProcessTextActivities() else emptyList()
        val pm = view.context.packageManager
        processTextActivities.forEachIndexed { index, info ->
            menu.add(0, ID_PROCESS_TEXT_BASE + index, order++, info.loadLabel(pm))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
    }

    private fun queryProcessTextActivities(): List<ResolveInfo> {
        val pm = view.context.packageManager
        val intent = Intent(Intent.ACTION_PROCESS_TEXT).setType(MIME_TEXT)
        val found = queryActivities(intent, 0)
        val browsers = browserPackages()
        return found
            .filter { it.activityInfo.exported && it.activityInfo.permission == null }
            .map { it to it.loadLabel(pm).toString() }
            .onEach { (info, label) -> logHandler(info, label) }
            // Keep only the phone's built-in translator: a browser's "翻译" just opens a web page and
            // Google Translate duplicates it. Non-translation actions are left untouched.
            .filterNot { (info, label) ->
                isTranslator(label) && (info.activityInfo.packageName in browsers || info.activityInfo.packageName == GOOGLE_TRANSLATE)
            }
            // Translators first, since that is the action this toolbar is mostly used for.
            .sortedByDescending { (_, label) -> isTranslator(label) }
            .map { (info, _) -> info }
    }

    /** Packages that handle arbitrary web links, i.e. browsers. */
    private fun browserPackages(): Set<String> {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com"))
            .addCategory(Intent.CATEGORY_BROWSABLE)
        return queryActivities(intent, PackageManager.MATCH_ALL).map { it.activityInfo.packageName }.toSet()
    }

    private fun queryActivities(intent: Intent, flags: Int): List<ResolveInfo> {
        val pm = view.context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, flags)
        }
    }

    // Debug builds log every handler so unwanted entries on a new phone can be identified in logcat.
    private fun logHandler(info: ResolveInfo, label: String) {
        if ((view.context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            Log.d(TAG, "PROCESS_TEXT handler: label=$label, activity=${info.activityInfo.packageName}/${info.activityInfo.name}")
        }
    }

    /**
     * Compose only exposes the selection through "copy", so the text goes through the clipboard.
     * It stays there afterwards: reading the previous clip to restore it would trigger Android 12+'s
     * "app pasted from clipboard" notice for content copied from other apps.
     */
    private fun processSelectedText(info: ResolveInfo) {
        val context = view.context
        onCopy?.invoke()
        val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
        val text = clipboard.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
        if (text.isNullOrEmpty()) return

        val intent = Intent(Intent.ACTION_PROCESS_TEXT)
            .setType(MIME_TEXT)
            .setClassName(info.activityInfo.packageName, info.activityInfo.name)
            .putExtra(Intent.EXTRA_PROCESS_TEXT, text)
            .putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // The handler was uninstalled between building the menu and the click.
        } catch (e: SecurityException) {
            // Some handlers reject callers at runtime; nothing useful to do.
        }
    }

    private fun isTranslator(label: String): Boolean =
        label.contains("翻译") || label.contains("翻譯") || label.contains("translat", ignoreCase = true)

    private companion object {
        const val TAG = "ProcessTextToolbar"
        const val GOOGLE_TRANSLATE = "com.google.android.apps.translate"
        const val MIME_TEXT = "text/plain"
        const val ID_COPY = 1
        const val ID_PASTE = 2
        const val ID_CUT = 3
        const val ID_SELECT_ALL = 4
        const val ID_PROCESS_TEXT_BASE = 100
    }
}
