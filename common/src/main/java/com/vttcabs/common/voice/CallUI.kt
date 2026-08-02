package com.vttcabs.common.voice

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vttcabs.common.voice.CallSignal.CallEndReason
import com.vttcabs.common.voice.CallSignal.CallRole
import com.vttcabs.common.voice.CallSignal.CallState
import kotlinx.coroutines.delay

/**
 * Incoming Call Screen
 * Shown when receiving a voice call during an active trip
 */
@Composable
fun IncomingCallScreen(
    callerName: String,
    callerRole: CallRole,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated caller avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Caller info
            Text(
                text = callerName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = if (callerRole == CallRole.CALLER) "is calling you" else "wants to talk",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Role badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (callerRole == CallRole.CALLER) "👤 Customer" else "🚗 Driver",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Call buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                // Reject button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = onReject,
                        containerColor = Color(0xFFE53935),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Reject",
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Decline",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
                
                // Accept button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = onAccept,
                        containerColor = Color(0xFF4CAF50),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Accept",
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Accept",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Active Call Screen
 * Shown during an active voice call
 */
@Composable
fun ActiveCallScreen(
    remoteUserName: String,
    remoteUserRole: CallRole,
    callState: CallState,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    durationSeconds: Long,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayTime by remember { mutableStateOf("00:00") }
    
    LaunchedEffect(durationSeconds) {
        while (true) {
            val mins = (durationSeconds / 1000 / 60).toInt()
            val secs = (durationSeconds / 1000 % 60).toInt()
            displayTime = String.format("%02d:%02d", mins, secs)
            delay(1000)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Remote user avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        if (callState == CallState.CONNECTED) Color(0xFF4CAF50)
                        else Color(0xFFFF9800)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Remote user info
            Text(
                text = remoteUserName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = if (remoteUserRole == CallRole.CALLER) "Customer" else "Driver",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Call status
            Text(
                text = when (callState) {
                    CallState.CALLING -> "Calling..."
                    CallState.CONNECTING -> "Connecting..."
                    CallState.RECONNECTING -> "Reconnecting..."
                    CallState.CONNECTED -> displayTime
                    else -> ""
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = if (callState == CallState.CONNECTED) Color(0xFF4CAF50) else Color.White
            )
            
            if (callState == CallState.RECONNECTING) {
                Text(
                    text = "Connection lost, please wait...",
                    fontSize = 14.sp,
                    color = Color(0xFFFF9800)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Call controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = onToggleMute,
                        containerColor = if (isMuted) Color(0xFFE53935) else Color(0xFF424242),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isMuted) "Unmute" else "Mute",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                
                // End call button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = onEndCall,
                        containerColor = Color(0xFFE53935),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "End",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                
                // Speaker button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = onToggleSpeaker,
                        containerColor = if (isSpeakerOn) Color(0xFF424242) else Color(0xFF4CAF50),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            contentDescription = if (isSpeakerOn) "Speaker Off" else "Speaker On",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isSpeakerOn) "Speaker" else "Earpiece",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Call info
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "End-to-end encrypted",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Call Button for trip screens
 * Floating action button to initiate voice call
 */
@Composable
fun VoiceCallButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = if (enabled) Color(0xFF4CAF50) else Color.Gray,
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Voice Call"
        )
    }
}

/**
 * Voice call unavailable message
 */
@Composable
fun VoiceCallUnavailableMessage(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = null,
                tint = Color(0xFFE53935)
            )
        },
        title = {
            Text("Voice Call Unavailable")
        },
        text = {
            Text("Voice calls are only available during active trips. Please start a trip first.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

/**
 * Call ended screen
 */
@Composable
fun CallEndedScreen(
    durationSeconds: Long,
    reason: CallEndReason?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayTime = remember(durationSeconds) {
        val mins = (durationSeconds / 1000 / 60).toInt()
        val secs = (durationSeconds / 1000 % 60).toInt()
        String.format("%02d:%02d", mins, secs)
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (reason) {
                    CallEndReason.REJECTED -> Icons.Default.CallMissed
                    CallEndReason.MISSED -> Icons.Default.CallMissed
                    CallEndReason.TRIP_ENDED, CallEndReason.TRIP_CANCELLED -> Icons.Default.Call
                    else -> Icons.Default.CallEnd
                },
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = when (reason) {
                    CallEndReason.REJECTED, CallEndReason.MISSED -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = when (reason) {
                    CallEndReason.REJECTED -> "Call Declined"
                    CallEndReason.MISSED -> "Missed Call"
                    CallEndReason.TRIP_ENDED -> "Call Ended"
                    CallEndReason.TRIP_CANCELLED -> "Call Ended - Trip Cancelled"
                    CallEndReason.NETWORK_ERROR -> "Connection Lost"
                    else -> "Call Ended"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            if (durationSeconds > 0) {
                Text(
                    text = "Duration: $displayTime",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Done")
            }
        }
    }
}
