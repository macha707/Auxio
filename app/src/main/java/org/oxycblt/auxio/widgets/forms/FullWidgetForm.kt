package org.oxycblt.auxio.widgets.forms

import android.content.Context
import android.widget.RemoteViews
import org.oxycblt.auxio.R
import org.oxycblt.auxio.playback.state.LoopMode
import org.oxycblt.auxio.playback.system.PlaybackService
import org.oxycblt.auxio.ui.newBroadcastIntent
import org.oxycblt.auxio.widgets.WidgetState

class FullWidgetForm : WidgetForm(R.layout.widget_full) {
    override fun createViews(context: Context, state: WidgetState): RemoteViews {
        val views = super.createViews(context, state)

        views.setOnClickPendingIntent(
            R.id.widget_loop,
            context.newBroadcastIntent(
                PlaybackService.ACTION_LOOP
            )
        )

        views.setOnClickPendingIntent(
            R.id.widget_skip_prev,
            context.newBroadcastIntent(
                PlaybackService.ACTION_SKIP_PREV
            )
        )

        views.setOnClickPendingIntent(
            R.id.widget_play_pause,
            context.newBroadcastIntent(
                PlaybackService.ACTION_PLAY_PAUSE
            )
        )

        views.setOnClickPendingIntent(
            R.id.widget_skip_next,
            context.newBroadcastIntent(
                PlaybackService.ACTION_SKIP_NEXT
            )
        )

        views.setOnClickPendingIntent(
            R.id.widget_shuffle,
            context.newBroadcastIntent(
                PlaybackService.ACTION_SHUFFLE
            )
        )

        views.setTextViewText(R.id.widget_song, state.song.name)
        views.setTextViewText(R.id.widget_artist, state.song.album.artist.name)

        views.setImageViewResource(
            R.id.widget_play_pause,
            if (state.isPlaying) {
                R.drawable.ic_pause
            } else {
                R.drawable.ic_play
            }
        )

        if (state.albumArt != null) {
            views.setImageViewBitmap(R.id.widget_cover, state.albumArt)
            views.setCharSequence(
                R.id.widget_cover, "setContentDescription",
                context.getString(R.string.description_album_cover, state.song.album.name)
            )
        } else {
            views.setImageViewResource(R.id.widget_cover, R.drawable.ic_song)
            views.setCharSequence(
                R.id.widget_cover,
                "setContentDescription",
                context.getString(R.string.description_placeholder_cover)
            )
        }

        // The main way the large widget differs from the other widgets is the addition of extra
        // controls. However, since the context we use to load attributes is from the main process,
        // attempting to dynamically color anything will result in an error. More duplicate
        // resources it is, then. This is getting really tiring.

        val shuffleRes = if (state.isShuffled)
            R.drawable.ic_shuffle_tinted
        else
            R.drawable.ic_shuffle

        val loopRes = when (state.loopMode) {
            LoopMode.NONE -> R.drawable.ic_loop
            LoopMode.ALL -> R.drawable.ic_loop_all_tinted
            LoopMode.TRACK -> R.drawable.ic_loop_one_tinted
        }

        views.setImageViewResource(R.id.widget_shuffle, shuffleRes)
        views.setImageViewResource(R.id.widget_loop, loopRes)

        return views
    }
}
