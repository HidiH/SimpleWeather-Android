package com.thewizrd.simpleweather.preferences

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.core.view.updatePaddingRelative
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thewizrd.shared_resources.lifecycle.LifecycleAwarePreferenceFragmentCompat
import com.thewizrd.shared_resources.utils.ContextUtils.dpToPx
import com.thewizrd.simpleweather.adapters.SpacerAdapter
import com.thewizrd.simpleweather.snackbar.Snackbar
import com.thewizrd.simpleweather.snackbar.SnackbarManager
import com.thewizrd.simpleweather.snackbar.SnackbarManagerInterface
import kotlinx.coroutines.launch
import com.google.android.material.snackbar.Snackbar as MaterialSnackbar

abstract class CustomPreferenceFragmentCompat : LifecycleAwarePreferenceFragmentCompat(),
    SnackbarManagerInterface {
    private var mSnackMgr: SnackbarManager? = null

    abstract override fun createSnackManager(activity: Activity): SnackbarManager?

    @CallSuper
    override fun initSnackManager(activity: Activity) {
        if (mSnackMgr == null) {
            mSnackMgr = createSnackManager(activity)
        }
    }

    fun showSnackbar(snackbar: Snackbar) {
        showSnackbar(snackbar, null)
    }

    override fun showSnackbar(
        snackbar: Snackbar,
        callback: MaterialSnackbar.Callback?
    ) {
        runWithView {
            activity?.let {
                if (isAlive) {
                    if (mSnackMgr == null) {
                        mSnackMgr = createSnackManager(it)
                    }
                    mSnackMgr?.show(snackbar, callback)
                }
            }
        }
    }

    override fun dismissAllSnackbars() {
        runOnUiThread { mSnackMgr?.dismissAll() }
    }

    override fun unloadSnackManager() {
        dismissAllSnackbars()
        mSnackMgr = null
    }

    @IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ToastDuration

    fun showToast(@StringRes resId: Int, @ToastDuration duration: Int) {
        lifecycleScope.launch {
            context?.let {
                Toast.makeText(it, resId, duration).show()
            }
        }
    }

    fun showToast(message: CharSequence?, @ToastDuration duration: Int) {
        lifecycleScope.launch {
            context?.let {
                Toast.makeText(it, message, duration).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            initSnackManager(requireActivity())
        } else {
            dismissAllSnackbars()
        }
    }

    override fun onPause() {
        unloadSnackManager()
        super.onPause()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Don't allow any divider in between the preferences in expressive design.
        setDivider(null)
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        return ConcatAdapter(
            SettingsPreferenceGroupAdapter(preferenceScreen),
            SpacerAdapter(preferenceScreen.context.dpToPx(16f).toInt())
        )
    }
}