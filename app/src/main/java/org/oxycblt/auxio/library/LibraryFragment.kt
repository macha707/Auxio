package org.oxycblt.auxio.library

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.transition.Fade
import androidx.transition.TransitionManager
import org.oxycblt.auxio.R
import org.oxycblt.auxio.databinding.FragmentLibraryBinding
import org.oxycblt.auxio.library.adapters.LibraryAdapter
import org.oxycblt.auxio.library.adapters.SearchAdapter
import org.oxycblt.auxio.music.Album
import org.oxycblt.auxio.music.Artist
import org.oxycblt.auxio.music.BaseModel
import org.oxycblt.auxio.music.Genre
import org.oxycblt.auxio.music.MusicStore
import org.oxycblt.auxio.music.Song
import org.oxycblt.auxio.playback.PlaybackViewModel
import org.oxycblt.auxio.playback.state.PlaybackMode
import org.oxycblt.auxio.ui.applyColor
import org.oxycblt.auxio.ui.resolveAttr
import org.oxycblt.auxio.ui.setupAlbumActions
import org.oxycblt.auxio.ui.setupArtistActions
import org.oxycblt.auxio.ui.setupGenreActions
import org.oxycblt.auxio.ui.setupSongActions

// A Fragment to show all the music in the Library.
class LibraryFragment : Fragment(), SearchView.OnQueryTextListener {

    private val libraryModel: LibraryViewModel by activityViewModels()
    private val playbackModel: PlaybackViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLibraryBinding.inflate(inflater)

        val musicStore = MusicStore.getInstance()

        val libraryAdapter = LibraryAdapter(
            libraryModel.showMode.value!!,
            doOnClick = { navToItem(it) },
            doOnLongClick = { data, view -> showActionsForItem(data, view) }
        )

        val searchAdapter = SearchAdapter(
            doOnClick = { navToItem(it) },
            doOnLongClick = { data, view -> showActionsForItem(data, view) }
        )

        // --- UI SETUP ---

        binding.libraryToolbar.apply {
            overflowIcon = ContextCompat.getDrawable(
                requireContext(), R.drawable.ic_sort_none
            )

            setOnMenuItemClickListener {
                if (it.itemId != R.id.action_search) {
                    libraryModel.updateSortMode(it)
                } else {
                    // Do whatever this is in order to make the SearchView focusable.
                    (it.actionView as SearchView).isIconified = false

                    // Then also do a basic animation
                    TransitionManager.beginDelayedTransition(
                        binding.libraryToolbar, Fade()
                    )
                    it.expandActionView()
                }
                true
            }

            menu.apply {
                val item = findItem(R.id.action_search)
                val searchView = item.actionView as SearchView

                searchView.queryHint = getString(R.string.hint_search_library)
                searchView.maxWidth = Int.MAX_VALUE
                searchView.setOnQueryTextListener(this@LibraryFragment)
                searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
                    libraryModel.updateSearchFocusStatus(hasFocus)
                    libraryModel.updateSearchQuery(searchView.query.toString(), requireContext())
                    item.isVisible = !hasFocus
                }

                item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                        binding.libraryRecycler.adapter = searchAdapter
                        setGroupVisible(R.id.group_sorting, false)
                        libraryModel.resetQuery()

                        return true
                    }

                    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                        binding.libraryRecycler.adapter = libraryAdapter
                        setGroupVisible(R.id.group_sorting, true)
                        libraryModel.resetQuery()

                        return true
                    }
                })
            }
        }

        binding.libraryRecycler.apply {
            adapter = libraryAdapter
            setHasFixedSize(true)
        }

        // --- VIEWMODEL SETUP ---

        libraryModel.sortMode.observe(viewLifecycleOwner) { mode ->
            Log.d(this::class.simpleName, "Updating sort mode to $mode")

            // Update the adapter with the new data
            libraryAdapter.updateData(
                mode.getSortedBaseModelList(
                    musicStore.getListForShowMode(libraryModel.showMode.value!!)
                )
            )

            // Then update the menu item in the toolbar to reflect the new mode
            binding.libraryToolbar.menu.forEach {
                if (it.itemId == libraryModel.sortMode.value!!.toMenuId()) {
                    it.applyColor(resolveAttr(requireContext(), R.attr.colorPrimary))
                } else {
                    it.applyColor(resolveAttr(requireContext(), android.R.attr.textColorPrimary))
                }
            }
        }

        libraryModel.searchResults.observe(viewLifecycleOwner) {
            if (libraryModel.searchHasFocus) {
                searchAdapter.submitList(it) {
                    binding.libraryRecycler.scrollToPosition(0)
                }
            }
        }

        playbackModel.navToItem.observe(viewLifecycleOwner) {
            if (it != null) {
                libraryModel.updateNavigationStatus(false)

                if (it is Song) {
                    navToItem(it.album)
                } else {
                    navToItem(it)
                }
            }
        }

        Log.d(this::class.simpleName, "Fragment created.")

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        libraryModel.updateNavigationStatus(false)
    }

    override fun onQueryTextSubmit(query: String): Boolean = false

    override fun onQueryTextChange(query: String): Boolean {
        libraryModel.updateSearchQuery(query, requireContext())

        return true
    }

    private fun showActionsForItem(data: BaseModel, view: View) {
        val menu = PopupMenu(requireContext(), view)
        when (data) {
            is Song -> menu.setupSongActions(data, requireContext(), playbackModel)
            is Album -> menu.setupAlbumActions(data, requireContext(), playbackModel)
            is Artist -> menu.setupArtistActions(data, requireContext(), playbackModel)
            is Genre -> menu.setupGenreActions(data, requireContext(), playbackModel)

            else -> {
            }
        }
    }

    private fun navToItem(baseModel: BaseModel) {
        // If the item is a song [That was selected through search], then update the playback
        // to that song instead of doing any navigation
        if (baseModel is Song) {
            playbackModel.playSong(baseModel, PlaybackMode.ALL_SONGS)
            return
        }

        if (!libraryModel.isNavigating) {
            libraryModel.updateNavigationStatus(true)

            Log.d(this::class.simpleName, "Navigating to the detail fragment for ${baseModel.name}")

            findNavController().navigate(
                when (baseModel) {
                    is Genre -> LibraryFragmentDirections.actionShowGenre(baseModel.id)
                    is Artist -> LibraryFragmentDirections.actionShowArtist(baseModel.id)
                    is Album -> LibraryFragmentDirections.actionShowAlbum(baseModel.id, true)

                    // If given model wasn't valid, then reset the navigation status
                    // and abort the navigation.
                    else -> {
                        libraryModel.updateNavigationStatus(false)
                        return
                    }
                }
            )
        }
    }
}
