package io.github.droidkaigi.confsched2017.view.fragment

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import io.github.droidkaigi.confsched2017.R
import io.github.droidkaigi.confsched2017.databinding.FragmentSessionsBinding
import io.github.droidkaigi.confsched2017.databinding.ViewSessionCellBinding
import io.github.droidkaigi.confsched2017.model.Room
import io.github.droidkaigi.confsched2017.util.ViewUtil
import io.github.droidkaigi.confsched2017.util.asyncUI
import io.github.droidkaigi.confsched2017.view.activity.MySessionsActivity
import io.github.droidkaigi.confsched2017.view.activity.SearchActivity
import io.github.droidkaigi.confsched2017.view.customview.ArrayRecyclerAdapter
import io.github.droidkaigi.confsched2017.view.customview.BindingHolder
import io.github.droidkaigi.confsched2017.view.customview.TouchlessTwoWayView
import io.github.droidkaigi.confsched2017.viewmodel.SessionViewModel
import io.github.droidkaigi.confsched2017.viewmodel.SessionsViewModel
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import org.lucasr.twowayview.TwoWayLayoutManager
import org.lucasr.twowayview.widget.DividerItemDecoration
import org.lucasr.twowayview.widget.SpannableGridLayoutManager
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SessionsFragment : BaseFragment() {

    @Inject
    internal lateinit var viewModel: SessionsViewModel

    @Inject
    internal lateinit var cancellation: Job

    private lateinit var adapter: SessionsAdapter

    private lateinit var binding: FragmentSessionsBinding

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentSessionsBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        initView()

        return binding.getRoot()
    }

    override fun onCreateOptionsMenu(menu: Menu?, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_sessions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.item_search -> startActivity(SearchActivity.createIntent(activity))
            R.id.item_my_sessions -> startActivity(MySessionsActivity.createIntent(activity))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        showSessions()
    }

    override fun onStop() {
        super.onStop()
        cancellation.cancel()
        cancellation = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroy()
        cancellation.cancel()
    }

    private val screenWidth: Int
        get() {
            val display = activity.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            return size.x
        }

    private fun showSessions() = asyncUI {
        try {
            val sessions = viewModel.getSessions(CommonPool + cancellation, Locale.getDefault(), this@SessionsFragment.context)
            renderSessions(sessions.await())
        } catch (e: CancellationException) {
            // Do nothing
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Failed to show sessions.")
        }
    }

    private fun initView() {
        binding.recyclerView.setHasFixedSize(true)

        val minWidth = resources.getDimension(R.dimen.session_table_min_width).toInt()
        val sessionsTableWidth = maxOf(screenWidth, minWidth)
        binding.recyclerView.minimumWidth = sessionsTableWidth

        val divider = ResourcesCompat.getDrawable(resources, R.drawable.divider, null)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(divider))

        adapter = SessionsAdapter(context)
        binding.recyclerView.adapter = adapter

        val clickCanceller = ClickGestureCanceller(context, binding.recyclerView)

        binding.root.setOnTouchListener { _, event ->
            clickCanceller.sendCancelIfScrolling(event)

            val e = MotionEvent.obtain(event)
            e.setLocation(e.x + binding.root.scrollX, e.y - binding.headerRow.height)
            binding.recyclerView.forceToDispatchTouchEvent(e)
            return@setOnTouchListener false
        }

        ViewUtil.addOneTimeOnGlobalLayoutListener(binding.headerRow) {
            if (binding.headerRow.height > 0) {
                binding.recyclerView.layoutParams.height =
                        binding.root.height - binding.border.height - binding.headerRow.height
                binding.recyclerView.requestLayout()
                return@addOneTimeOnGlobalLayoutListener true
            } else {
                return@addOneTimeOnGlobalLayoutListener false
            }
        }

        binding.recyclerView.clearOnScrollListeners()
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, scrollState: Int) {
                // Do nothing
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val viewModel = adapter.getItem(binding.recyclerView.firstVisiblePosition)
                if (viewModel.formattedDate.isNotEmpty()) {
                    binding.txtDate.text = viewModel.formattedDate
                }
            }
        })
    }

    private fun renderSessions(adjustedSessionViewModels: List<SessionViewModel>) {
        val stimes = viewModel.stimes!!
        val rooms = viewModel.rooms!!

        if (binding.recyclerView.layoutManager == null) {
            val lm = SpannableGridLayoutManager(
                    TwoWayLayoutManager.Orientation.VERTICAL, rooms.size, stimes.size)
            binding.recyclerView.layoutManager = lm
        }

        renderHeaderRow(rooms)

        adapter.reset(adjustedSessionViewModels)

        if (binding.txtDate.text.isEmpty()) {
            binding.txtDate.text = adjustedSessionViewModels[0].formattedDate
            binding.txtDate.visibility = View.VISIBLE
        }
    }

    private fun renderHeaderRow(rooms: List<Room>) {
        if (binding.headerRow.childCount == 0) {
            for (room in rooms) {
                val view = LayoutInflater.from(context).inflate(R.layout.view_sessions_header_cell, null)
                val txtRoomName = view.findViewById(R.id.txt_room_name) as TextView
                txtRoomName.text = room.name
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                txtRoomName.layoutParams = params
                binding.headerRow.addView(view)
            }
        }
    }

    inner class SessionsAdapter internal constructor(context: Context)
        : ArrayRecyclerAdapter<SessionViewModel, BindingHolder<ViewSessionCellBinding>>(context) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ViewSessionCellBinding> {
            return BindingHolder(context, parent, R.layout.view_session_cell)
        }

        override fun onBindViewHolder(holder: BindingHolder<ViewSessionCellBinding>, position: Int) {
            val viewModel = getItem(position)
            holder.binding.viewModel = viewModel
            holder.binding.executePendingBindings()
        }
    }

    private class ClickGestureCanceller internal constructor(context: Context, targetView: TouchlessTwoWayView) {

        private val gestureDetector: GestureDetector

        init {
            gestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
                private var ignoreMotionEventOnScroll = false

                override fun onDown(motionEvent: MotionEvent): Boolean {
                    ignoreMotionEventOnScroll = true
                    return false
                }

                override fun onScroll(motionEvent: MotionEvent, motionEvent1: MotionEvent, v: Float, v1: Float): Boolean {
                    // Send cancel event for item clicked when horizontal scrolling.
                    if (ignoreMotionEventOnScroll) {
                        val now = SystemClock.uptimeMillis()
                        val cancelEvent = MotionEvent.obtain(
                                now, now, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0)
                        targetView.forceToDispatchTouchEvent(cancelEvent)
                        ignoreMotionEventOnScroll = false
                    }

                    return false
                }

                override fun onShowPress(motionEvent: MotionEvent) {
                    // Do nothing
                }

                override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
                    // Do nothing
                    return false
                }


                override fun onLongPress(motionEvent: MotionEvent) {
                    // Do nothing
                }

                override fun onFling(motionEvent: MotionEvent, motionEvent1: MotionEvent, v: Float, v1: Float): Boolean {
                    // Do nothing
                    return false
                }
            })
        }

        fun sendCancelIfScrolling(event: MotionEvent) {
            gestureDetector.onTouchEvent(event)
        }
    }

    companion object {

        @JvmField // TODO remove
        val TAG: String = SessionsFragment::class.java.simpleName

        @JvmStatic // TODO remove
        fun newInstance(): SessionsFragment {
            return SessionsFragment()
        }
    }

}
