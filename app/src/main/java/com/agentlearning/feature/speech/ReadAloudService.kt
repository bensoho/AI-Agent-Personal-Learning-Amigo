package com.agentlearning.feature.speech

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.agentlearning.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Exposes the read-aloud player to the media notification and lock screen, and keeps it alive in the background. */
@AndroidEntryPoint
class ReadAloudService : MediaSessionService() {

    @Inject lateinit var controller: ReadAloudController

    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        session = MediaSession.Builder(this, controller.obtainPlayer())
            .setSessionActivity(openApp)
            .build()
            .also { addSession(it) }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = session?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        // The player belongs to ReadAloudController and outlives this service; only the session is released.
        session?.release()
        session = null
        super.onDestroy()
    }
}
