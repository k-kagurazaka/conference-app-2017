package io.github.droidkaigi.confsched2017.view.fragment

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sys1yagi.fragmentcreator.annotation.Args
import com.sys1yagi.fragmentcreator.annotation.FragmentCreator
import io.github.droidkaigi.confsched2017.R
import io.github.droidkaigi.confsched2017.databinding.FragmentSessionDetailBinding
import io.github.droidkaigi.confsched2017.util.asyncUI
import io.github.droidkaigi.confsched2017.view.helper.AnimationHelper
import io.github.droidkaigi.confsched2017.viewmodel.SessionDetailViewModel
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import timber.log.Timber
import javax.inject.Inject

@FragmentCreator
class SessionDetailFragment : BaseFragment(), SessionDetailViewModel.Callback {

    @Inject
    internal lateinit var viewModel: SessionDetailViewModel

    @Inject
    internal lateinit var cancellation: Job

    @JvmField
    @Args
    internal var sessionId: Int = -1

    private lateinit var binding: FragmentSessionDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionDetailFragmentCreator.read(this)
    }

    override fun onStop() {
        super.onStop()
        cancellation.cancel()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSessionDetailBinding.inflate(inflater, container, false)
        viewModel.setCallback(this)
        binding.viewModel = viewModel
        setHasOptionsMenu(true)

        asyncUI {
            try {
                viewModel.loadSession(CommonPool + cancellation, sessionId).join()
            } catch (e: CancellationException) {
                Timber.tag(TAG).d("Cancelled to find session.")
            } catch (e: Throwable) {
                Timber.tag(TAG).e(e, "Failed to find session.")
            }
            initTheme(activity)
            binding.viewModel = viewModel
        }

        initToolbar()
        initScroll()
        return binding.root
    }

    private fun initTheme(activity: Activity?) {
        if (activity == null) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Change theme by topic
            activity.setTheme(viewModel.topicThemeResId)

            val taskDescription = ActivityManager.TaskDescription(viewModel.sessionTitle, null,
                    ContextCompat.getColor(activity, viewModel.sessionVividColorResId))
            activity.setTaskDescription(taskDescription)

            // Change status bar scrim color
            val typedValue = TypedValue()
            activity.theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true)
            val colorPrimaryDark = typedValue.data
            if (colorPrimaryDark != 0) {
                binding.collapsingToolbar.setStatusBarScrimColor(colorPrimaryDark)
            }
        }
    }

    private fun initScroll() {
        val view = binding.nestedScroll
        view.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) {
                binding.fab.hide()
            }
            if (scrollY < oldScrollY) {
                binding.fab.show()
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onDetach() {
        viewModel.destroy()
        super.onDetach()
    }

    private fun initToolbar() {
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        val bar = activity.supportActionBar
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true)
            bar.setDisplayShowHomeEnabled(true)
            bar.setDisplayShowTitleEnabled(false)
            bar.setHomeButtonEnabled(true)
        }
    }

    override fun onClickFab(selected: Boolean) {
        AnimationHelper.startVDAnimation(binding.fab,
                R.drawable.avd_add_to_check_24dp, R.drawable.avd_check_to_add_24dp,
                resources.getInteger(R.integer.fab_vector_animation_mills))
        val textId: Int
        val actionTextId: Int
        if (selected) {
            textId = R.string.session_checked
            actionTextId = R.string.session_uncheck
        } else {
            textId = R.string.session_unchecked
            actionTextId = R.string.session_check
        }
        val typedValue = TypedValue()
        activity.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        val actionTextColor = typedValue.data
        Snackbar.make(binding.fab, textId, Snackbar.LENGTH_SHORT)
                .setAction(actionTextId) { _ -> binding.fab.performClick() }
                .setActionTextColor(actionTextColor)
                .show()
    }

    override fun onOverScroll() {
        activity.finish()
        activity.overridePendingTransition(0, 0)
    }

    private companion object {

        val TAG: String = SessionDetailFragment::class.java.simpleName
    }
}
