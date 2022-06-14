/*
 * Copyright (c) 2021 Auxio Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
 
package org.oxycblt.auxio.detail

import android.view.LayoutInflater
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.oxycblt.auxio.R
import org.oxycblt.auxio.databinding.FragmentDetailBinding
import org.oxycblt.auxio.music.MusicParent
import org.oxycblt.auxio.playback.PlaybackViewModel
import org.oxycblt.auxio.ui.NavigationViewModel
import org.oxycblt.auxio.ui.Sort
import org.oxycblt.auxio.ui.ViewBindingFragment
import org.oxycblt.auxio.util.androidActivityViewModels
import org.oxycblt.auxio.util.logD
import org.oxycblt.auxio.util.unlikelyToBeNull

/**
 * A Base [Fragment] implementing the base features shared across all detail fragments.
 * @author OxygenCobalt
 */
abstract class DetailFragment :
    ViewBindingFragment<FragmentDetailBinding>(), Toolbar.OnMenuItemClickListener {
    protected val detailModel: DetailViewModel by androidActivityViewModels()
    protected val navModel: NavigationViewModel by activityViewModels()
    protected val playbackModel: PlaybackViewModel by activityViewModels()

    override fun onCreateBinding(inflater: LayoutInflater): FragmentDetailBinding =
        FragmentDetailBinding.inflate(inflater)

    override fun onDestroyBinding(binding: FragmentDetailBinding) {
        super.onDestroyBinding(binding)
        binding.detailToolbar.setOnMenuItemClickListener(null)
        binding.detailRecycler.adapter = null
    }

    /**
     * Shortcut method for doing setup of the detail toolbar.
     * @param data Parent data to use as the toolbar title
     * @param menuId Menu resource to use
     */
    protected fun setupToolbar(data: MusicParent, @MenuRes menuId: Int) {
        requireBinding().detailToolbar.apply {
            title = data.resolveName(context)
            inflateMenu(menuId)
            setNavigationOnClickListener { findNavController().navigateUp() }
            setOnMenuItemClickListener(this@DetailFragment)
        }
    }

    /**
     * Shortcut method for spinning up the sorting [PopupMenu]
     * @param anchor The view to anchor the sort menu to
     * @param sort The initial sort
     * @param onConfirm What to do when the sort is confirmed
     * @param showItem Which menu items to keep
     */
    protected fun showSortMenu(
        anchor: View,
        sort: Sort,
        onConfirm: (Sort) -> Unit,
        showItem: ((Int) -> Boolean)? = null,
    ) {
        logD("Launching menu")

        PopupMenu(anchor.context, anchor).apply {
            inflate(R.menu.menu_detail_sort)

            setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.option_sort_asc) {
                    item.isChecked = !item.isChecked
                    onConfirm(sort.ascending(item.isChecked))
                } else {
                    item.isChecked = true
                    onConfirm(unlikelyToBeNull(sort.assignId(item.itemId)))
                }

                true
            }

            if (showItem != null) {
                for (item in menu.children) {
                    item.isVisible = showItem(item.itemId)
                }
            }

            menu.findItem(sort.itemId).isChecked = true
            menu.findItem(R.id.option_sort_asc).isChecked = sort.isAscending

            show()
        }
    }
}
