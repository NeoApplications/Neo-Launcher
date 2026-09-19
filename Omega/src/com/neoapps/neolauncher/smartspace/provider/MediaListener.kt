package com.neoapps.neolauncher.smartspace.provider

import android.app.Notification
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.service.notification.StatusBarNotification
import android.view.KeyEvent
import com.android.launcher3.util.MainThreadInitializedObject
import com.neoapps.neolauncher.util.FlowCollector
import java.util.Objects

class MediaListener private constructor(private val mContext: Context) {
    private val listeners = mutableSetOf<(MediaListener) -> Unit>()
    private val activeControllers = mutableMapOf<MediaSession.Token, MediaNotificationController>()

    var tracking: MediaNotificationController? = null
        private set

    private var lastNotifiedToken: MediaSession.Token? = null
    private var lastNotifiedInfo: MediaInfo? = null
    private var lastNotifiedPlaying: Boolean? = null

    private val mFlowCollector: FlowCollector<List<StatusBarNotification>>

    init {
        val notificationManager = NotificationsManager.INSTANCE[mContext]
        mFlowCollector = FlowCollector(notificationManager.notifications) { list ->
            updateNotifications(list)
        }
    }

    @Synchronized
    fun addListener(listener: (MediaListener) -> Unit) {
        val wasEmpty = listeners.isEmpty()
        listeners.add(listener)
        if (wasEmpty) {
            mFlowCollector.start()
        }
        listener(this)
    }

    @Synchronized
    fun removeListener(listener: (MediaListener) -> Unit) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            mFlowCollector.stop()
            clearControllers()
        }
    }

    val `package`: String?
        get() = tracking?.packageName

    private fun clearControllers() {
        activeControllers.values.forEach { it.unregister() }
        activeControllers.clear()
        tracking = null
        lastNotifiedToken = null
        lastNotifiedInfo = null
        lastNotifiedPlaying = null
        notifyListeners()
    }

    private fun updateNotifications(list: List<StatusBarNotification>) {
        val currentTokens = mutableSetOf<MediaSession.Token>()
        list.forEach { sbn ->
            val extras = sbn.notification.extras
            val token = extras.getParcelable<MediaSession.Token>(Notification.EXTRA_MEDIA_SESSION)
            if (token != null) {
                currentTokens.add(token)
                val existing = activeControllers[token]
                if (existing != null) {
                    existing.updateSbn(sbn)
                } else {
                    val controller = MediaNotificationController(
                        controller = MediaController(mContext, token),
                        sbn = sbn,
                        onChanged = { updateTracking() },
                        onDestroyed = { removeController(token) }
                    )
                    controller.register()
                    activeControllers[token] = controller
                }
            }
        }

        val removedTokens = activeControllers.keys.filter { it !in currentTokens }
        removedTokens.forEach { token ->
            val removed = activeControllers.remove(token)
            removed?.unregister()
            if (tracking === removed) {
                tracking = null
            }
        }

        updateTracking()
    }

    private fun removeController(token: MediaSession.Token) {
        val removed = activeControllers.remove(token) ?: return
        removed.unregister()
        if (tracking === removed) {
            tracking = null
            updateTracking()
        }
    }

    private fun updateTracking() {
        if (tracking != null && (tracking !in activeControllers.values || !tracking!!.isPlaying)) {
            tracking = null
        }
        if (tracking == null) {
            tracking = activeControllers.values.firstOrNull { it.isPlaying }
        }

        val currentToken = tracking?.controller?.sessionToken
        val currentInfo = tracking?.info?.copy()
        val currentPlaying = tracking?.isPlaying == true

        val hasChanged = currentToken != lastNotifiedToken
                || currentInfo != lastNotifiedInfo
                || currentPlaying != lastNotifiedPlaying

        if (hasChanged) {
            lastNotifiedToken = currentToken
            lastNotifiedInfo = currentInfo
            lastNotifiedPlaying = currentPlaying
            notifyListeners()
        }
    }

    private fun notifyListeners() {
        val currentListeners = synchronized(this) { listeners.toList() }
        currentListeners.forEach { it(this) }
    }

    private fun pressButton(keyCode: Int) {
        tracking?.pressButton(keyCode)
    }

    fun toggle(finalClick: Boolean = true) {
        pressButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    }

    fun next(finalClick: Boolean = true) {
        if (finalClick) {
            pressButton(KeyEvent.KEYCODE_MEDIA_NEXT)
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY)
        }
    }

    fun previous(finalClick: Boolean = true) {
        if (finalClick) {
            pressButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY)
        }
    }

    data class MediaInfo(
        var title: CharSequence? = null,
        var artist: CharSequence? = null,
        var album: CharSequence? = null,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MediaInfo) return false
            return title?.toString() == other.title?.toString()
                    && artist?.toString() == other.artist?.toString()
                    && album?.toString() == other.album?.toString()
        }

        override fun hashCode(): Int {
            return Objects.hash(title?.toString(), artist?.toString(), album?.toString())
        }
    }

    class MediaNotificationController(
        val controller: MediaController,
        var sbn: StatusBarNotification,
        private val onChanged: () -> Unit,
        private val onDestroyed: () -> Unit,
    ) {
        var info: MediaInfo? = null
            private set
        private var lastPlaybackState: Int? = null

        private val callback = object : MediaController.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackState?) {
                val newState = state?.state
                if (lastPlaybackState != newState) {
                    lastPlaybackState = newState
                    onChanged()
                }
            }

            override fun onMetadataChanged(metadata: MediaMetadata?) {
                reloadInfo()
                onChanged()
            }

            override fun onSessionDestroyed() {
                onDestroyed()
            }
        }

        fun register() {
            runCatching { controller.registerCallback(callback) }
            lastPlaybackState = runCatching { controller.playbackState?.state }.getOrNull()
            reloadInfo()
        }

        fun unregister() {
            runCatching { controller.unregisterCallback(callback) }
        }

        fun updateSbn(newSbn: StatusBarNotification) {
            sbn = newSbn
            reloadInfo()
            onChanged()
        }

        private fun hasTitle(): Boolean {
            return !info?.title.isNullOrBlank()
        }

        val isPlaying: Boolean
            get() {
                if (!hasTitle()) return false
                val playbackState =
                    runCatching { controller.playbackState }.getOrNull() ?: return false
                val state = playbackState.state
                return state == PlaybackState.STATE_PLAYING
                        || state == PlaybackState.STATE_BUFFERING
                        || state == PlaybackState.STATE_SKIPPING_TO_NEXT
                        || state == PlaybackState.STATE_SKIPPING_TO_PREVIOUS
            }

        fun pressButton(keyCode: Int) {
            runCatching {
                controller.dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                controller.dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
            }
        }

        fun reloadInfo() {
            val metadata = runCatching { controller.metadata }.getOrNull()
            val metaTitle =
                metadata?.getText(MediaMetadata.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }
            val metaArtist =
                metadata?.getText(MediaMetadata.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }
            val metaAlbum =
                metadata?.getText(MediaMetadata.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() }

            val notifExtras = sbn.notification.extras
            val notifTitle =
                notifExtras.getCharSequence(Notification.EXTRA_TITLE)?.takeIf { it.isNotBlank() }
            val notifText =
                notifExtras.getCharSequence(Notification.EXTRA_TEXT)?.takeIf { it.isNotBlank() }
            val notifSubText =
                notifExtras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.takeIf { it.isNotBlank() }

            info = MediaInfo(
                title = metaTitle ?: notifTitle,
                artist = metaArtist ?: notifText ?: notifSubText,
                album = metaAlbum
            )
        }

        val packageName: String
            get() = controller.packageName
    }

    companion object {
        private const val TAG = "MediaListener"

        @JvmField
        val INSTANCE = MainThreadInitializedObject(::MediaListener)
    }
}