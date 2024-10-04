package com.saggitt.omega.smartspace.provider

import android.app.Notification
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Handler
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.KeyEvent
import androidx.core.util.Consumer
import com.saggitt.omega.util.FlowCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.Objects

class MediaListener(private val mContext: Context, onChange: Consumer<MediaListener>) :
    MediaController.Callback() {
    private val mOnChange = Runnable { onChange.accept(this) }
    private var mControllers = emptyList<MediaNotificationController>()
    var tracking: MediaNotificationController? = null
        private set
    private val mHandler = Handler()
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
        for (mnc in mControllers) {
            mnc.controller.unregisterCallback(this)
        }
        for (mnc in controllers) {
            mnc.controller.registerCallback(this)
        }
        mControllers = controllers
    }

    private fun updateTracking() {
        updateControllers(controllers)

        if (tracking != null) {
            tracking!!.reloadInfo()
        }

        // If the current controller is not playing, stop tracking it.
        if (tracking != null
            && (!mControllers.contains(tracking) || !tracking!!.isPlaying)
        ) {
            tracking = null
        }

        for (mnc in mControllers) {
            // Either we are not tracking a controller and this one is valid,
            // or this one is playing while the one we track is not.
            if ((tracking == null && mnc.isPlaying)
                || (tracking != null && mnc.isPlaying && !tracking!!.isPlaying)
            ) {
                tracking = mnc
            }
        }

        mHandler.removeCallbacks(mOnChange)
        mHandler.post(mOnChange)
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
        get() {
            val controllers: MutableList<MediaNotificationController> =
                ArrayList()
            for (notif in mNotifications) {
                val extras = notif.notification.extras
                val notifToken =
                    extras.getParcelable<MediaSession.Token>(Notification.EXTRA_MEDIA_SESSION)
                if (notifToken != null) {
                    val controller =
                        MediaController(mContext, notifToken)
                    controllers.add(MediaNotificationController(controller, notif))
                }
            }
            return controllers
        }

    /**
     * Events that refresh the current handler.
     */
    override fun onPlaybackStateChanged(state: PlaybackState?) {
        super.onPlaybackStateChanged(state)
        updateTracking()
    }

    override fun onMetadataChanged(metadata: MediaMetadata?) {
        super.onMetadataChanged(metadata)
        updateTracking()
    }

    inner class MediaInfo {
        var title: CharSequence? = null
        var artist: CharSequence? = null
        var album: CharSequence? = null

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val mediaInfo = o as MediaInfo
            return title == mediaInfo.title && artist == mediaInfo.artist && album == mediaInfo.album
        }

        override fun hashCode(): Int {
            return Objects.hash(title, artist, album)
        }
    }

    inner class MediaNotificationController(
        val controller: MediaController,
        sbn: StatusBarNotification
    ) {
        val sbn: StatusBarNotification? = sbn
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
            } else if (sbn != null) {
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