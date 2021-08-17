/*
 * Copyright (c) 2021 Auxio Project
 * PlayPauseButton.kt is part of Auxio.
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

package org.oxycblt.auxio.playback

import android.content.Context
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import org.oxycblt.auxio.R
import org.oxycblt.auxio.toAnimDrawable

/**
 * Custom [AppCompatImageButton] that handles the animated play/pause icons.
 * @author OxygenCobalt
 */
class PlayPauseButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : AppCompatImageButton(context, attrs, defStyleAttr) {
    private val iconPauseToPlay = R.drawable.ic_pause_to_play.toAnimDrawable(context)
    private val iconPlayToPause = R.drawable.ic_play_to_pause.toAnimDrawable(context)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fixSeams()
        }
    }

    /**
     * Update the play/pause icon to reflect [isPlaying]
     * @param animate Whether the icon change should be animated or not.
     */
    fun setPlaying(isPlaying: Boolean, animate: Boolean) {
        if (isPlaying) {
            if (animate) {
                setImageDrawable(iconPlayToPause)
                iconPlayToPause.start()
            } else {
                setImageResource(R.drawable.ic_pause_large)
            }
        } else {
            if (animate) {
                setImageDrawable(iconPauseToPlay)
                iconPauseToPlay.start()
            } else {
                setImageResource(R.drawable.ic_play_large)
            }
        }
    }

    /**
     * Hack that fixes an issue where a seam would display in the middle of the play button,
     * probably as a result of floating point precision errors. Thanks IEEE 754.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun fixSeams() {
        iconPauseToPlay.registerAnimationCallback(object : Animatable2.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                // ic_play_large is a unified vector, compared to the two paths on the
                // animated vector. So switch to that when the animation completes to prevent the
                // seam from displaying.
                setImageResource(R.drawable.ic_play_large)
            }
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            iconPauseToPlay.clearAnimationCallbacks()
        }
    }
}
