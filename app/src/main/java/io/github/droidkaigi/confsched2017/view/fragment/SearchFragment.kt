package io.github.droidkaigi.confsched2017.view.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.Filter
import android.widget.Filterable
import io.github.droidkaigi.confsched2017.R
import io.github.droidkaigi.confsched2017.databinding.FragmentSearchBinding
import io.github.droidkaigi.confsched2017.databinding.ViewSearchResultBinding
import io.github.droidkaigi.confsched2017.util.asyncUI
import io.github.droidkaigi.confsched2017.view.customview.ArrayRecyclerAdapter
import io.github.droidkaigi.confsched2017.view.customview.BindingHolder
import io.github.droidkaigi.confsched2017.view.customview.itemdecoration.DividerItemDecoration
import io.github.droidkaigi.confsched2017.viewmodel.SearchResultViewModel
import io.github.droidkaigi.confsched2017.viewmodel.SearchViewModel
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SearchFragment : BaseFragment(), SearchViewModel.Callback {

    @Inject
    internal lateinit var viewModel: SearchViewModel

    @Inject
    internal lateinit var cancellation: Job

    private lateinit var adapter: SearchResultsAdapter

    private lateinit var binding: FragmentSearchBinding

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel.setCallback(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu.findItem(R.id.action_search)
        MenuItemCompat.setOnActionExpandListener(menuItem, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                activity?.onBackPressed()
                return false
            }
        })
        val searchView = menuItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return onQueryTextChange(query)
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.previousSearchText = newText
                adapter.filter.filter(newText)
                return true
            }
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        MenuItemCompat.expandActionView(menu.findItem(R.id.action_search))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        initRecyclerView()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancellation.cancel()
        cancellation = Job()
    }

    override fun onDetach() {
        cancellation.cancel()
        viewModel.destroy()
        super.onDetach()
    }

    override fun closeSearchResultList() {
        adapter.clearAllResults()
    }

    private fun initRecyclerView() {
        adapter = SearchResultsAdapter(context)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, R.drawable.divider))
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun loadData() = asyncUI {
        try {
            val context = this@SearchFragment.context
            val searchResults = viewModel.getSearchResultViewModels(CommonPool + cancellation, context).await()
            renderSearchResults(searchResults)
        } catch (e: CancellationException) {
            // Do nothing
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Search result load failed.")
        }
    }

    private fun renderSearchResults(searchResultViewModels: List<SearchResultViewModel>) {
        adapter.setAllList(searchResultViewModels)
        val searchText = adapter.previousSearchText
        if (!searchText.isNullOrEmpty()) {
            adapter.filter.filter(searchText)
        }
    }

    private inner class SearchResultsAdapter(context: Context)
        : ArrayRecyclerAdapter<SearchResultViewModel, BindingHolder<ViewSearchResultBinding>>(context), Filterable {

        private val filteredList: MutableList<SearchResultViewModel> = ArrayList()

        private var allList: List<SearchResultViewModel>? = null

        internal var previousSearchText: String? = null

        init {
            setHasStableIds(true)
        }

        fun setAllList(viewModels: List<SearchResultViewModel>) {
            allList = viewModels
        }

        fun clearAllResults() {
            clear()
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ViewSearchResultBinding> {
            return BindingHolder(context, parent, R.layout.view_search_result)
        }

        override fun onBindViewHolder(holder: BindingHolder<ViewSearchResultBinding>, position: Int) {
            val viewModel = getItem(position)
            val itemBinding = holder.binding
            itemBinding.viewModel = viewModel
            itemBinding.executePendingBindings()

            itemBinding.txtSearchResult.text = viewModel.getMatchedText(previousSearchText)
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence): Filter.FilterResults {
                    filteredList.clear()
                    val results = Filter.FilterResults()

                    if (constraint.isNotEmpty()) {
                        val filterPattern = constraint.toString().toLowerCase().trim()
                        allList?.let {
                            it.asSequence().filter { it.match(filterPattern) }.forEach {
                                filteredList.add(it)
                            }
                        }
                    }

                    results.values = filteredList
                    results.count = filteredList.size

                    return results
                }

                override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
                    clear()
                    addAll(results.values as List<SearchResultViewModel>)
                    notifyDataSetChanged()
                }
            }
        }

        override fun getItemId(position: Int): Long {
            val viewModel = getItem(position)
            return viewModel.searchResultId.toLong()
        }
    }

    companion object {

        @JvmField // TODO remove
        val TAG: String = SearchFragment::class.java.simpleName

        @JvmStatic // TODO remove
        fun newInstance(): SearchFragment = SearchFragment()
    }
}
