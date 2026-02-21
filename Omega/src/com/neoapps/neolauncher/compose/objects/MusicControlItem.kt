package com.neoapps.neolauncher.compose.objects

import android.media.AudioManager
import android.view.KeyEvent
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.icons.Phosphor
import com.neoapps.neolauncher.compose.icons.phosphor.Pause
import com.neoapps.neolauncher.compose.icons.phosphor.Play
import com.neoapps.neolauncher.compose.icons.phosphor.SkipBack
import com.neoapps.neolauncher.compose.icons.phosphor.SkipForward

class MusicControlItem(
    val icon: ImageVector,
    @StringRes val description: Int,
    val onClick: (AudioManager) -> Unit
) {
    // TODO fix descriptions
    companion object {
        val PLAY = MusicControlItem(
            Phosphor.Play,
            R.string.dash_media_player
        ) { musicManager ->
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PLAY
                )
            )
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_MEDIA_PLAY
                )
            )
        }

        val PAUSE = MusicControlItem(
            Phosphor.Pause,
            R.string.dash_media_player
        ) { musicManager ->
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PAUSE
                )
            )
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_MEDIA_PAUSE
                )
            )
        }

        val PREVIOUS = MusicControlItem(
            Phosphor.SkipBack,
            R.string.dash_media_player
        ) { musicManager ->
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS
                )
            )
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS
                )
            )
        }

        val NEXT = MusicControlItem(
            Phosphor.SkipForward,
            R.string.dash_media_player
        ) { musicManager ->
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_NEXT
                )
            )
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_MEDIA_NEXT
                )
            )
        }
    }
}
