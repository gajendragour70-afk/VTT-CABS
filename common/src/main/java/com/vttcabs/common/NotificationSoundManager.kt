package com.vttcabs.common

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.min

/**
 * Professional Notification Sound Manager
 * Handles all notification sounds with proper audio focus management
 */
object NotificationSoundManager {
    private const val TAG = "NotificationSound"
    
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var continuousPlayer: MediaPlayer? = null
    private var continuousJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _activeSound = MutableStateFlow<SoundType?>(null)
    val activeSound: StateFlow<SoundType?> = _activeSound.asStateFlow()
    
    // Sound resource mapping - using raw resource IDs
    enum class SoundType(val channelId: String, val channelName: String, val importance: Int) {
        // Customer Sounds
        RIDE_ACCEPTED("customer_ride", "Ride Notifications", NotificationCompat.PRIORITY_HIGH),
        DRIVER_ARRIVED("customer_driver", "Driver Arrived", NotificationCompat.PRIORITY_MAX),
        TRIP_STARTED("customer_trip", "Trip Updates", NotificationCompat.PRIORITY_HIGH),
        TRIP_COMPLETED("customer_complete", "Trip Completed", NotificationCompat.PRIORITY_DEFAULT),
        PAYMENT_SUCCESS("customer_payment", "Payment Updates", NotificationCompat.PRIORITY_DEFAULT),
        NEW_NOTIFICATION("customer_general", "General Notifications", NotificationCompat.PRIORITY_DEFAULT),
        
        // Driver Sounds
        INCOMING_RIDE("driver_ride", "Ride Requests", NotificationCompat.PRIORITY_MAX),
        RIDE_CANCELLED("driver_cancelled", "Cancelled Rides", NotificationCompat.PRIORITY_HIGH),
        NEW_MESSAGE("driver_message", "Messages", NotificationCompat.PRIORITY_DEFAULT),
        DRIVER_TRIP_START("driver_start", "Trip Start", NotificationCompat.PRIORITY_HIGH),
        DRIVER_TRIP_COMPLETE("driver_complete", "Trip Complete", NotificationCompat.PRIORITY_HIGH),
        PAYMENT_RECEIVED("driver_payment", "Payment Received", NotificationCompat.PRIORITY_DEFAULT),
        
        // Admin Sounds
        NEW_BOOKING_ALERT("admin_booking", "New Bookings", NotificationCompat.PRIORITY_MAX),
        NEW_DRIVER_REGISTRATION("admin_driver", "Driver Registrations", NotificationCompat.PRIORITY_HIGH),
        DOCUMENT_UPLOADED("admin_document", "Document Uploads", NotificationCompat.PRIORITY_DEFAULT),
        DRIVER_APPROVED("admin_approved", "Driver Approved", NotificationCompat.PRIORITY_DEFAULT),
        DRIVER_REJECTED("admin_rejected", "Driver Rejected", NotificationCompat.PRIORITY_DEFAULT),
        BOOKING_CANCELLED("admin_ride_cancelled", "Cancelled Bookings", NotificationCompat.PRIORITY_HIGH),
        EMERGENCY_SOS("admin_sos", "Emergency SOS", NotificationCompat.PRIORITY_MAX);
    }
    
    // Vibration patterns (in milliseconds)
    private val vibrationPatterns = mapOf(
        SoundType.RIDE_ACCEPTED to longArrayOf(0, 200, 100, 200),
        SoundType.DRIVER_ARRIVED to longArrayOf(0, 500, 200, 500, 200, 500),
        SoundType.TRIP_STARTED to longArrayOf(0, 300, 150, 300),
        SoundType.TRIP_COMPLETED to longArrayOf(0, 200, 100, 200, 100, 200),
        SoundType.PAYMENT_SUCCESS to longArrayOf(0, 150, 100, 150),
        SoundType.NEW_NOTIFICATION to longArrayOf(0, 100, 50, 100),
        SoundType.INCOMING_RIDE to longArrayOf(0, 500, 300, 500, 300, 500), // Loud pattern
        SoundType.RIDE_CANCELLED to longArrayOf(0, 300, 100, 300, 100, 300),
        SoundType.NEW_MESSAGE to longArrayOf(0, 100, 50, 100),
        SoundType.DRIVER_TRIP_START to longArrayOf(0, 400, 200, 400),
        SoundType.DRIVER_TRIP_COMPLETE to longArrayOf(0, 300, 150, 300, 150, 300),
        SoundType.PAYMENT_RECEIVED to longArrayOf(0, 200, 100, 200),
        SoundType.NEW_BOOKING_ALERT to longArrayOf(0, 500, 250, 500),
        SoundType.NEW_DRIVER_REGISTRATION to longArrayOf(0, 300, 150, 300),
        SoundType.DOCUMENT_UPLOADED to longArrayOf(0, 150, 75, 150),
        SoundType.DRIVER_APPROVED to longArrayOf(0, 200, 100, 200, 100, 200),
        SoundType.DRIVER_REJECTED to longArrayOf(0, 400, 200, 400),
        SoundType.BOOKING_CANCELLED to longArrayOf(0, 500, 250, 500, 250, 500),
        SoundType.EMERGENCY_SOS to longArrayOf(0, 1000, 500, 1000, 500, 1000) // Urgent pattern
    )
    
    /**
     * Initialize the sound manager with context
     */
    fun initialize(context: Context) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        createNotificationChannels(context)
    }
    
    /**
     * Create notification channels for Android O+
     */
    private fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = NotificationManagerCompat.from(context)
            
            SoundType.entries.forEach { soundType ->
                val channel = android.app.NotificationChannel(
                    soundType.channelId,
                    soundType.channelName,
                    soundType.importance
                ).apply {
                    description = "Notifications for ${soundType.name.lowercase().replace("_", " ")}"
                    enableVibration(true)
                    setSound(getDefaultSoundUri(), AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    /**
     * Play a notification sound with vibration
     */
    fun playSound(context: Context, soundType: SoundType, durationMs: Long = 2000) {
        scope.launch {
            try {
                _isPlaying.value = true
                _activeSound.value = soundType
                
                // Start vibration
                startVibration(soundType)
                
                // Play sound
                playNotificationSound(context, soundType, durationMs)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error playing sound: ${e.message}")
            } finally {
                delay(durationMs)
                _isPlaying.value = false
                _activeSound.value = null
            }
        }
    }
    
    /**
     * Play continuous sound (for incoming ride) that loops until stopped
     */
    fun playContinuousSound(context: Context, soundType: SoundType, maxDurationMs: Long = 60000) {
        stopContinuousSound()
        
        scope.launch {
            try {
                _isPlaying.value = true
                _activeSound.value = soundType
                
                // Start vibration pattern
                startContinuousVibration(soundType)
                
                // Initialize and start continuous playback
                continuousPlayer = MediaPlayer().apply {
                    setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                    
                    // Use a system notification sound for continuous playback
                    val soundUri = getSoundUriForType(soundType)
                    setDataSource(context, soundUri)
                    isLooping = true
                    prepare()
                    setVolume(1.0f, 1.0f)
                    start()
                }
                
                // Stop after max duration
                delay(maxDurationMs)
                stopContinuousSound()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error playing continuous sound: ${e.message}")
                stopContinuousSound()
            }
        }
    }
    
    /**
     * Stop continuous sound immediately
     */
    fun stopContinuousSound() {
        continuousJob?.cancel()
        continuousJob = null
        
        try {
            continuousPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping continuous player: ${e.message}")
        }
        continuousPlayer = null
        
        stopVibration()
        _isPlaying.value = false
        _activeSound.value = null
    }
    
    /**
     * Play incoming ride sound with full-screen alert
     */
    fun playIncomingRideSound(context: Context, onTimeout: () -> Unit = {}, timeoutMs: Long = 60000) {
        playContinuousSound(context, SoundType.INCOMING_RIDE, timeoutMs)
        
        // Schedule timeout callback
        continuousJob = scope.launch {
            delay(timeoutMs)
            stopContinuousSound()
            onTimeout()
        }
    }
    
    /**
     * Play emergency SOS alarm
     */
    fun playEmergencyAlarm(context: Context, durationMs: Long = 10000) {
        scope.launch {
            try {
                _isPlaying.value = true
                _activeSound.value = SoundType.EMERGENCY_SOS
                
                // Start urgent vibration
                startEmergencyVibration()
                
                // Play alarm sound
                playAlarmSound(context, durationMs)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error playing emergency alarm: ${e.message}")
            } finally {
                delay(durationMs)
                stopVibration()
                _isPlaying.value = false
                _activeSound.value = null
            }
        }
    }
    
    /**
     * Stop all sounds and vibrations
     */
    fun stopAll() {
        stopContinuousSound()
        stopVibration()
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        _activeSound.value = null
    }
    
    /**
     * Check if a specific sound type is currently playing
     */
    fun isPlayingSound(soundType: SoundType): Boolean {
        return _activeSound.value == soundType && _isPlaying.value
    }
    
    // ==================== Private Methods ====================
    
    private fun playNotificationSound(context: Context, soundType: SoundType, durationMs: Long) {
        try {
            val soundUri = getSoundUriForType(soundType)
            
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                
                setDataSource(context, soundUri)
                prepare()
                start()
            }
            
            // Auto-stop after duration
            scope.launch {
                delay(durationMs)
                mediaPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                }
                mediaPlayer = null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
            // Fallback to default notification
            playDefaultNotification(context)
        }
    }
    
    private fun playDefaultNotification(context: Context) {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone?.play()
            scope.launch {
                delay(2000)
                ringtone?.stop()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing default notification: ${e.message}")
        }
    }
    
    private fun playAlarmSound(context: Context, durationMs: Long) {
        try {
            // Use alarm sound for emergency
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                
                setDataSource(context, alarmUri)
                isLooping = true
                prepare()
                setVolume(1.0f, 1.0f)
                start()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm: ${e.message}")
            playDefaultNotification(context)
        }
    }
    
    private fun getSoundUriForType(soundType: SoundType): Uri {
        // Use default system sounds - in production, use custom sounds from raw resources
        return when (soundType) {
            SoundType.INCOMING_RIDE, SoundType.DRIVER_ARRIVED, SoundType.NEW_BOOKING_ALERT -> {
                // High priority - use alarm or high-priority notification
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            SoundType.EMERGENCY_SOS -> {
                // Emergency - use alarm
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            SoundType.PAYMENT_SUCCESS, SoundType.PAYMENT_RECEIVED -> {
                // Money-related - use notification with different tone
                Uri.parse("content://settings/system/notification_sound")
            }
            else -> {
                // Default notification sound
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        }
    }
    
    private fun getDefaultSoundUri(): Uri {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }
    
    // ==================== Vibration Methods ====================
    
    private fun startVibration(soundType: SoundType) {
        val pattern = vibrationPatterns[soundType] ?: longArrayOf(0, 200)
        startVibrationPattern(pattern)
    }
    
    private fun startVibrationPattern(pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration: ${e.message}")
        }
    }
    
    private fun startContinuousVibration(soundType: SoundType) {
        // Use repeating vibration for continuous sounds
        val pattern = vibrationPatterns[soundType] ?: longArrayOf(0, 500, 300, 500)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting continuous vibration: ${e.message}")
        }
    }
    
    private fun startEmergencyVibration() {
        // Urgent vibration pattern
        val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting emergency vibration: ${e.message}")
        }
    }
    
    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibration: ${e.message}")
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopAll()
        scope.cancel()
    }
}
