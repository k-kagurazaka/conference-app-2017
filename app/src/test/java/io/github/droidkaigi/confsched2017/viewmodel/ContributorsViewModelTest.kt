package io.github.droidkaigi.confsched2017.viewmodel

import com.sys1yagi.kmockito.any
import com.sys1yagi.kmockito.invoked
import com.sys1yagi.kmockito.mock
import com.sys1yagi.kmockito.verify
import com.taroid.knit.should
import io.github.droidkaigi.confsched2017.model.Contributor
import io.github.droidkaigi.confsched2017.repository.contributors.ContributorsRepository
import io.github.droidkaigi.confsched2017.view.helper.Navigator
import io.github.droidkaigi.confsched2017.view.helper.ResourceResolver
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never

class ContributorsViewModelTest {

    private companion object {
        val EXPECTED_CONTRIBUTORS = listOf(
                Contributor(
                        name = "Alice",
                        htmlUrl = "AliceUrl",
                        contributions = 10
                ),
                Contributor(
                        name = "Bob",
                        contributions = 20
                ),
                Contributor(
                        name = "Charlie",
                        contributions = 30
                )
        )
    }

    private val resourceResolver = object : ResourceResolver(null) {
        override fun getString(resId: Int): String = "Contributors"

        override fun getString(resId: Int, vararg formatArgs: Any?): String = "(${formatArgs[0]} people)"
    }

    private val toolbarViewModel = mock<ToolbarViewModel>()

    private val repository = mock<ContributorsRepository>()

    private lateinit var navigator: Navigator

    private lateinit var viewModel: ContributorsViewModel

    @Before
    fun setUp() = runBlocking {
        val expected = async(context) { EXPECTED_CONTRIBUTORS}.apply { await() }
        repository.findAll(any()).invoked.thenReturn(expected)
        navigator = mock<Navigator>()
        viewModel = ContributorsViewModel(
                resourceResolver, navigator, toolbarViewModel, repository, Job())
    }

    @After
    fun tearDown() {
        viewModel.destroy()
    }

    @Test
    @Throws(Exception::class)
    fun start() = runBlocking {
        viewModel.start().join()

        assertEq(viewModel.contributorViewModels, EXPECTED_CONTRIBUTORS)
        viewModel.loadingVisibility.should be 8 // GONE
        viewModel.refreshing.should be false
        toolbarViewModel.verify().toolbarTitle = "Contributors (3 people)"
    }

    @Test
    @Throws(Exception::class)
    fun onSwipeRefresh() = runBlocking {
        viewModel.onSwipeRefresh().join()

        assertEq(viewModel.contributorViewModels, EXPECTED_CONTRIBUTORS)
        viewModel.loadingVisibility.should be 8 // GONE
        viewModel.refreshing.should be false
        toolbarViewModel.verify().toolbarTitle = "Contributors (3 people)"
    }

    @Test
    @Throws(Exception::class)
    fun onContributorClick() = runBlocking {
        viewModel.start().join()

        navigator.verify(never()).navigateToWebPage(any())
        viewModel.contributorViewModels[0].onClickContributor(null)
        navigator.verify().navigateToWebPage("AliceUrl")
    }

    private fun assertEq(actual: List<ContributorViewModel>, expected: List<Contributor>) {
        actual.size.should be expected.size
        actual.map { it.name }.should be expected.map { it.name }
    }
}
