package com.neoapps.neolauncher.smartspace.provider

import android.app.Notification
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.KeyEvent
import com.neoapps.neolauncher.util.FlowCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Objects

class MediaListener(private val mContext: Context, onChange: (MediaListener) -> Unit) {
    private val mOnChange = { onChange(this) }
    private var mControllers = emptyList<MediaNotificationController>()
    var tracking: MediaNotificationController? = null
        private set
    private val mScope = CoroutineScope(Dispatchers.Main + Job())
    private val mFlowCollector: FlowCollector<List<StatusBarNotification>>
    private var mNotifications = emptyList<StatusBarNotification>()

    init {
        val notificationManager = NotificationsManager.INSTANCE[mContext]
        mFlowCollector = FlowCollector(notificationManager.notifications) { list ->
            mNotifications = list
            updateTracking()
        }
    }

    fun onResume() {
        updateTracking()
        mFlowCollector.start()
    }

    fun onPause() {
        updateTracking()
        mFlowCollector.stop()
    }

    val `package`: String
        get() = tracking!!.controller.packageName

    private fun updateControllers(controllers: List<MediaNotificationController>) {
        mControllers.forEach { it.controller.unregisterCallback(mediaControllerCallback) }
        controllers.forEach { it.controller.registerCallback(mediaControllerCallback) }
        mControllers = controllers
    }

    private fun updateTracking() {
        updateControllers(controllers)
        tracking?.reloadInfo()

        // If the current controller is not playing, stop tracking it.
        if (tracking != null && (!mControllers.contains(tracking) || !tracking!!.isPlaying)) {
            tracking = null
        }
        tracking = mControllers.find { it.isPlaying } ?: tracking

        mScope.launch { mOnChange() }
    }

    private fun pressButton(keyCode: Int) {
        if (tracking != null) {
            tracking!!.pressButton(keyCode)
        }
    }

    fun toggle(finalClick: Boolean) {
        if (!finalClick) {
            Log.d(TAG, "Toggle")
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        }
    }

    fun next(finalClick: Boolean) {
        if (finalClick) {
            Log.d(TAG, "Next")
            pressButton(KeyEvent.KEYCODE_MEDIA_NEXT)
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY)
        }
    }

    fun previous(finalClick: Boolean) {
        if (finalClick) {
            Log.d(TAG, "Previous")
            pressButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY)
        }
    }

    private val controllers: List<MediaNotificationController>
        get() = mNotifications.mapNotNull { notif ->
            val extras = notif.notification.extras
            val notifToken =
                extras.getParcelable<MediaSession.Token>(Notification.EXTRA_MEDIA_SESSION)
            notifToken?.let { MediaNotificationController(MediaController(mContext, it), notif) }
        }

    private val mediaControllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)
            updateTracking()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)
            updateTracking()
        }
    }

    inner class MediaInfo {
        var title: CharSequence? = null
        var artist: CharSequence? = null
        var album: CharSequence? = null

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val mediaInfo = other as MediaInfo
            return title == mediaInfo.title
                    && artist == mediaInfo.artist
                    && album == mediaInfo.album
        }

        override fun hashCode(): Int {
            return Objects.hash(title, artist, album)
        }
    }

    inner class MediaNotificationController(
        val controller: MediaController,
        val sbn: StatusBarNotification,
    ) {
        var info: MediaInfo? = null
            private set

        init {
            reloadInfo()
        }

        private fun hasTitle(): Boolean {
            return info != null && info!!.title != null
        }

        val isPlaying: Boolean
            get() {
                if (!hasTitle()) return false
                val playbackState = controller.playbackState ?: return false
                return playbackState.state == PlaybackState.STATE_PLAYING
            }

        fun pressButton(keyCode: Int) {
            controller.dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            controller.dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        }

        fun reloadInfo() {
            val metadata = controller.metadata
            if (metadata != null) {
                info = MediaInfo()
                info!!.title = metadata.getText(MediaMetadata.METADATA_KEY_TITLE)
                info!!.artist = metadata.getText(MediaMetadata.METADATA_KEY_ARTIST)
                info!!.album = metadata.getText(MediaMetadata.METADATA_KEY_ALBUM)
            } else {
                info = MediaInfo()
                info!!.title = sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE)
            }
        }

        val packageName: String
            get() = controller.packageName
    }

    companion object {
        private const val TAG = "MediaListener"
    }
}