package io.github.droidkaigi.confsched2017.view.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.github.droidkaigi.confsched2017.R
import io.github.droidkaigi.confsched2017.databinding.FragmentSettingsBinding
import io.github.droidkaigi.confsched2017.service.DebugOverlayService
import io.github.droidkaigi.confsched2017.util.LocaleUtil
import io.github.droidkaigi.confsched2017.util.SettingsUtil
import io.github.droidkaigi.confsched2017.view.activity.MainActivity
import io.github.droidkaigi.confsched2017.viewmodel.SettingsViewModel
import timber.log.Timber
import javax.inject.Inject

class SettingsFragment : BaseFragment(), SettingsViewModel.Callback {

    @Inject
    internal lateinit var viewModel: SettingsViewModel

    private lateinit var binding: FragmentSettingsBinding

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        viewModel.setCallback(this)
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onDetach() {
        viewModel.destroy()
        super.onDetach()
    }

    override fun changeHeadsUpEnabled(enabled: Boolean) {
        binding.headsUpSwitchRow.isEnabled = enabled
    }

    override fun showLanguagesDialog() {
        val locales = LocaleUtil.SUPPORT_LANG
        val languages = locales.map { LocaleUtil.getDisplayLanguage(context, it) }
        val languageIds = locales.map { LocaleUtil.getLocaleLanguageId(it) }

        val currentLanguageId = LocaleUtil.getCurrentLanguageId(activity)
        Timber.tag(TAG).d("current language_id: %s", currentLanguageId)
        Timber.tag(TAG).d("languageIds: %s", languageIds.toString())

        val defaultItem = languageIds.indexOf(currentLanguageId)
        Timber.tag(TAG).d("current language_id index: %s", defaultItem)

        val items = languages.toTypedArray()
        AlertDialog.Builder(activity, R.style.DialogTheme)
                .setTitle(R.string.settings_language)
                .setSingleChoiceItems(items, defaultItem) { dialog, which ->
                    val selectedLanguageId = languageIds[which]
                    if (currentLanguageId != selectedLanguageId) {
                        Timber.tag(TAG).d("Selected language_id: %s", selectedLanguageId)
                        LocaleUtil.setLocale(activity, selectedLanguageId)
                        dialog.dismiss()
                        restart()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    override fun debugOverlayViewEnabled(enabled: Boolean) {
        if (isDetached) {
            return
        }
        if (enabled && !SettingsUtil.canDrawOverlays(context)) {
            Timber.tag(TAG).d("not allowed to draw views on overlay")
            binding.debugOverlayViewSwitchRow.setChecked(false)
            Toast.makeText(context, R.string.settings_debug_overlay_view_toast, Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.packageName))
            startActivity(intent)
            return
        }
        if (enabled) {
            context.startService(Intent(context, DebugOverlayService::class.java))
        } else {
            context.stopService(Intent(context, DebugOverlayService::class.java))
        }
    }

    private fun restart() {
        activity.finish()
        startActivity(MainActivity.createIntent(activity))
        activity.overridePendingTransition(0, 0)
    }

    companion object {

        @JvmField // TODO remove
        val TAG = SettingsFragment::class.java.simpleName

        @JvmStatic // TODO remove
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

}
