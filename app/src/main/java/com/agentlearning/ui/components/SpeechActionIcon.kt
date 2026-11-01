package com.agentlearning.ui.components

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.ui.speech.SpeechSynthesizer
import com.agentlearning.ui.speech.textForSpeech

private val VolumeUpIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "VolumeUp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 9f)
            verticalLineToRelative(6f)
            horizontalLineToRelative(4f)
            lineToRelative(5f, 5f)
            verticalLineTo(4f)
            lineTo(7f, 9f)
            horizontalLineTo(3f)
            close()
            moveTo(16.5f, 12f)
            curveToRelative(0f, -1.77f, -1.02f, -3.29f, -2.5f, -4.03f)
            verticalLineToRelative(8.05f)
            curveToRelative(1.48f, -0.73f, 2.5f, -2.25f, 2.5f, -4.02f)
            close()
            moveTo(14f, 3.23f)
            verticalLineToRelative(2.06f)
            curveToRelative(2.89f, 0.86f, 5f, 3.54f, 5f, 6.71f)
            reflectiveCurveToRelative(-2.11f, 5.85f, -5f, 6.71f)
            verticalLineToRelative(2.06f)
            curveToRelative(4.01f, -0.91f, 7f, -4.49f, 7f, -8.77f)
            reflectiveCurveToRelative(-2.99f, -7.86f, -7f, -8.77f)
            close()
        }
    }.build()
}

private val PauseIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Pause",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 5f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(14f)
            horizontalLineTo(6f)
            close()
            moveTo(14f, 5f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(14f)
            horizontalLineTo(14f)
            close()
        }
    }.build()
}

private val PlayIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Play",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(8f, 5f)
            lineTo(19f, 12f)
            lineTo(8f, 19f)
            close()
        }
    }.build()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpeechActionIcon(text: String) {
    val context = LocalContext.current
    val latestText by rememberUpdatedState(text)
    val synthesizer = remember(context.applicationContext) {
        SpeechSynthesizer.getInstance(context.applicationContext)
    }
    val playbackState by synthesizer.state.collectAsStateWithLifecycle()

    LaunchedEffect(playbackState.notice) {
        playbackState.notice?.let { notice ->
            Toast.makeText(context, notice, Toast.LENGTH_SHORT).show()
            synthesizer.consumeNotice(notice)
        }
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .combinedClickable(
                enabled = playbackState.isReady &&
                    (playbackState.isSpeaking || text.isNotBlank()),
                role = Role.Button,
                onClick = { synthesizer.toggle(textForSpeech(latestText)) },
                onLongClick = {
                    if (playbackState.isSpeaking) synthesizer.stop()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (playbackState.isPreparing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            val (icon, description) = when {
                playbackState.isSpeaking && playbackState.isPaused ->
                    PlayIcon to "继续朗读（长按停止）"
                playbackState.isSpeaking ->
                    PauseIcon to "暂停朗读（长按停止）"
                else ->
                    VolumeUpIcon to "开始朗读"
            }
            Icon(
                imageVector = icon,
                contentDescription = description,
            )
        }
    }
}
