package com.example.service

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.engine.OptimizationEngine
import com.example.engine.SystemMonitor
import com.example.shizuku.ShizukuManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class GameOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var floatingBubbleView: FrameLayout? = null
    private var bubbleFpsTextView: TextView? = null
    private var expandedHudView: FrameLayout? = null
    private var crosshairView: View? = null
    private var isSmoothing = false

    private var bubbleLayoutParams: WindowManager.LayoutParams? = null
    private var hudLayoutParams: WindowManager.LayoutParams? = null
    private var crosshairLayoutParams: WindowManager.LayoutParams? = null

    private var isExpanded = false
    private var isCrosshairEnabled = false
    private var currentGamePackage: String = ""
    private var currentGameName: String = "Game"
    private var currentProfile: String = "EXTREME_PERFORMANCE"

    private lateinit var shizukuManager: ShizukuManager
    private lateinit var systemMonitor: SystemMonitor
    private lateinit var optimizationEngine: OptimizationEngine

    private var telemetryJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        shizukuManager = ShizukuManager(applicationContext)
        systemMonitor = SystemMonitor(applicationContext)
        optimizationEngine = OptimizationEngine(applicationContext, shizukuManager, systemMonitor)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("ExtremeBooster Active"))
        startTelemetryPolling()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        intent?.let {
            if (it.hasExtra(EXTRA_GAME_PACKAGE)) {
                currentGamePackage = it.getStringExtra(EXTRA_GAME_PACKAGE) ?: ""
            }
            if (it.hasExtra(EXTRA_GAME_NAME)) {
                currentGameName = it.getStringExtra(EXTRA_GAME_NAME) ?: "Game"
            }
            if (it.hasExtra(EXTRA_PROFILE_NAME)) {
                currentProfile = it.getStringExtra(EXTRA_PROFILE_NAME) ?: "EXTREME_PERFORMANCE"
            }
        }

        if (floatingBubbleView == null) {
            initFloatingBubble()
        }
        return START_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFloatingBubble() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val bubbleWidth = dpToPx(78)
        val bubbleHeight = dpToPx(34)

        bubbleLayoutParams = WindowManager.LayoutParams(
            bubbleWidth,
            bubbleHeight,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = screenWidth - bubbleWidth - dpToPx(10)
            y = dpToPx(160)
        }

        val bubbleContainer = FrameLayout(this).apply {
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(17).toFloat()
                setColor(Color.parseColor("#F00A0B0E"))
                setStroke(dpToPx(1), Color.parseColor("#10B981"))
            }
            background = bg
            elevation = dpToPx(8).toFloat()
        }

        val innerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
        }

        val boltIcon = TextView(this).apply {
            text = "⚡"
            textSize = 12f
            setTextColor(Color.parseColor("#10B981"))
            gravity = Gravity.CENTER
        }

        val fpsText = TextView(this).apply {
            text = "120"
            textSize = 11f
            setTextColor(Color.WHITE)
            setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dpToPx(4), 0, 0, 0)
        }
        bubbleFpsTextView = fpsText

        innerRow.addView(boltIcon)
        innerRow.addView(fpsText)

        val layoutParamsInner = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).apply {
            gravity = Gravity.CENTER
        }
        bubbleContainer.addView(innerRow, layoutParamsInner)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var touchDownTime = 0L

        bubbleContainer.setOnTouchListener { _, event ->
            val params = bubbleLayoutParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    touchDownTime = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    params.x = initialX + deltaX
                    params.y = initialY + deltaY
                    windowManager.updateViewLayout(bubbleContainer, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = abs(event.rawX - initialTouchX)
                    val diffY = abs(event.rawY - initialTouchY)
                    val duration = System.currentTimeMillis() - touchDownTime
                    if (diffX < 18 && diffY < 18) {
                        if (duration >= 450) {
                            // Long press: Expand detailed HUD menu
                            toggleHudExpansion()
                        } else {
                            // Single tap on floating HUD: Automatically Smooth the game!
                            triggerAutoSmooth()
                        }
                    } else {
                        // Drag completed: Apply edge-snapping physics
                        snapToScreenEdge(params.x, bubbleContainer)
                    }
                    true
                }
                else -> false
            }
        }

        floatingBubbleView = bubbleContainer
        try {
            windowManager.addView(bubbleContainer, bubbleLayoutParams)
        } catch (_: Exception) {}
    }

    private fun triggerAutoSmooth() {
        if (isSmoothing) return
        isSmoothing = true

        // Visual feedback on HUD
        bubbleFpsTextView?.text = "SMOOTH"
        bubbleFpsTextView?.setTextColor(Color.parseColor("#10B981"))
        floatingBubbleView?.let { bubble ->
            val activeBg = GradientDrawable().apply {
                cornerRadius = dpToPx(17).toFloat()
                setColor(Color.parseColor("#E60F251E"))
                setStroke(dpToPx(2), Color.parseColor("#10B981"))
            }
            bubble.background = activeBg
        }

        serviceScope.launch {
            val resultMsg = optimizationEngine.autoSmoothGame(currentGamePackage)
            Toast.makeText(applicationContext, "⚡ $resultMsg", Toast.LENGTH_SHORT).show()

            delay(2000)
            isSmoothing = false
            floatingBubbleView?.let { bubble ->
                val standardBg = GradientDrawable().apply {
                    cornerRadius = dpToPx(17).toFloat()
                    setColor(Color.parseColor("#F00A0B0E"))
                    setStroke(dpToPx(1), Color.parseColor("#10B981"))
                }
                bubble.background = standardBg
            }
            bubbleFpsTextView?.setTextColor(Color.WHITE)
        }
    }

    private fun snapToScreenEdge(currentX: Int, view: View) {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val bubbleWidth = dpToPx(78)
        val targetX = if (currentX + bubbleWidth / 2 < screenWidth / 2) {
            dpToPx(6)
        } else {
            screenWidth - bubbleWidth - dpToPx(6)
        }

        val animator = ValueAnimator.ofInt(currentX, targetX)
        animator.duration = 200
        animator.interpolator = OvershootInterpolator(1.1f)
        animator.addUpdateListener { animation ->
            bubbleLayoutParams?.let { params ->
                params.x = animation.animatedValue as Int
                try {
                    windowManager.updateViewLayout(view, params)
                } catch (_: Exception) {}
            }
        }
        animator.start()
    }

    private fun toggleHudExpansion() {
        if (isExpanded) {
            collapseHud()
        } else {
            expandHud()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun expandHud() {
        if (isExpanded) return
        isExpanded = true

        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.85f).toInt().coerceAtMost(dpToPx(290))

        hudLayoutParams = WindowManager.LayoutParams(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(16).toFloat()
                setColor(Color.parseColor("#F20A0B0E"))
                setStroke(dpToPx(1), Color.parseColor("#1E293B"))
            }
            background = bg
            elevation = dpToPx(16).toFloat()
        }

        // Header Row (Compact & Minimal)
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val titleText = TextView(this).apply {
            text = "⚡ HUD: $currentGameName"
            setTextColor(Color.parseColor("#10B981"))
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val closeBtn = TextView(this).apply {
            text = "✕"
            textSize = 15f
            setTextColor(Color.parseColor("#94A3B8"))
            setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2))
            setOnClickListener { collapseHud() }
        }
        headerRow.addView(titleText)
        headerRow.addView(closeBtn)
        rootLayout.addView(headerRow)

        // Telemetry Row (Live FPS, CPU, Temp in minimal badge)
        val telemetryText = TextView(this).apply {
            id = View.generateViewId()
            text = "FPS: 120  |  CPU: --%  |  TEMP: --°C"
            setTextColor(Color.parseColor("#94A3B8"))
            setTypeface(android.graphics.Typeface.MONOSPACE)
            textSize = 11f
            setPadding(0, dpToPx(8), 0, dpToPx(8))
        }
        rootLayout.addView(telemetryText)

        // Primary Hero Button: ⚡ AUTO-SMOOTH GAME
        val autoSmoothBtn = TextView(this).apply {
            text = "⚡ AUTO-SMOOTH GAME"
            setTextColor(Color.parseColor("#0A0B0E"))
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(10).toFloat()
                setColor(Color.parseColor("#10B981"))
            }
            background = bg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(4), 0, dpToPx(8))
            }
            setOnClickListener {
                triggerAutoSmooth()
                collapseHud()
            }
        }
        rootLayout.addView(autoSmoothBtn)

        // Secondary Action Row (Flush RAM & Reticle)
        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, dpToPx(6))
        }

        val flushBtn = createStyledButton("🧹 Flush RAM", Color.parseColor("#141E28"), Color.parseColor("#38BDF8")) {
            serviceScope.launch {
                val msg = optimizationEngine.flushMemory()
                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
            }
        }

        val crosshairBtn = createStyledButton(if (isCrosshairEnabled) "🎯 Reticle ON" else "🎯 Reticle OFF", Color.parseColor("#141E28"), Color.parseColor("#F59E0B")) {
            toggleCrosshair()
            (it as TextView).text = if (isCrosshairEnabled) "🎯 Reticle ON" else "🎯 Reticle OFF"
        }

        actionRow.addView(flushBtn)
        actionRow.addView(crosshairBtn)
        rootLayout.addView(actionRow)

        // Bottom Row (Exit HUD)
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }
        val exitOverlayBtn = TextView(this).apply {
            text = "🛑 Stop HUD"
            setTextColor(Color.parseColor("#F43F5E"))
            textSize = 11f
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
            setOnClickListener { stopSelf() }
        }
        bottomRow.addView(exitOverlayBtn)
        rootLayout.addView(bottomRow)

        val container = FrameLayout(this).apply {
            addView(rootLayout)
        }

        expandedHudView = container
        try {
            windowManager.addView(container, hudLayoutParams)
        } catch (_: Exception) {}

        expandedTelemetryView = telemetryText
    }

    private var expandedTelemetryView: TextView? = null

    private fun cycleProfile(button: TextView) {
        currentProfile = when (currentProfile) {
            "EXTREME_PERFORMANCE" -> "BALANCED"
            "BALANCED" -> "BATTERY_SAVER"
            else -> "EXTREME_PERFORMANCE"
        }
        button.text = "🚀 Mode: ${currentProfile.take(7)}"
        serviceScope.launch {
            if (currentGamePackage.isNotEmpty()) {
                val cmd = when (currentProfile) {
                    "EXTREME_PERFORMANCE" -> "device_config put game_overlay $currentGamePackage mode=2,downscaleFactor=0.8,fps=120 && cmd game mode performance $currentGamePackage"
                    "BALANCED" -> "device_config put game_overlay $currentGamePackage mode=2,downscaleFactor=0.9,fps=90 && cmd game mode standard $currentGamePackage"
                    else -> "device_config put game_overlay $currentGamePackage mode=3,downscaleFactor=0.6,fps=60 && cmd game mode battery $currentGamePackage"
                }
                shizukuManager.executeCommand(cmd)
            }
            Toast.makeText(applicationContext, "Switched to $currentProfile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun collapseHud() {
        if (!isExpanded) return
        isExpanded = false
        expandedTelemetryView = null
        expandedHudView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        expandedHudView = null
    }

    private fun toggleCrosshair() {
        if (isCrosshairEnabled) {
            isCrosshairEnabled = false
            crosshairView?.let {
                try {
                    windowManager.removeView(it)
                } catch (_: Exception) {}
            }
            crosshairView = null
        } else {
            isCrosshairEnabled = true
            val size = dpToPx(36)
            crosshairLayoutParams = WindowManager.LayoutParams(
                size,
                size,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val reticle = View(this).apply {
                val circle = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.TRANSPARENT)
                    setStroke(dpToPx(2), Color.parseColor("#00FF66"))
                }
                background = circle
            }

            crosshairView = reticle
            try {
                windowManager.addView(reticle, crosshairLayoutParams)
            } catch (_: Exception) {}
        }
    }

    private fun startTelemetryPolling() {
        telemetryJob?.cancel()
        telemetryJob = serviceScope.launch {
            while (isActive) {
                val telemetry = withContext(Dispatchers.IO) {
                    systemMonitor.getHardwareTelemetry()
                }
                val fps = telemetry.refreshRateHz.toInt()
                if (!isSmoothing) {
                    bubbleFpsTextView?.text = "$fps"
                }
                expandedTelemetryView?.text = "FPS: $fps Hz  |  CPU: ${telemetry.cpuLoadPercent}%  |  ${"%.1f".format(telemetry.batteryTempC)}°C  |  RAM: ${telemetry.ramUsagePercent}%"
                delay(1200)
            }
        }
    }

    private fun createStyledButton(label: String, bgColor: Int, textColor: Int, onClick: (View) -> Unit): TextView {
        return TextView(this).apply {
            text = label
            this.setTextColor(textColor)
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(10).toFloat()
                setColor(bgColor)
                setStroke(dpToPx(1), textColor)
            }
            background = bg
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            }
            setOnClickListener(onClick)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ExtremeBooster HUD Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows floating HUD controller in games"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ExtremeBooster Engine Active")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        floatingBubbleView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        expandedHudView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        crosshairView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        shizukuManager.cleanup()
    }

    companion object {
        const val CHANNEL_ID = "extreme_booster_hud_channel"
        const val NOTIFICATION_ID = 9012
        const val EXTRA_GAME_PACKAGE = "extra_game_package"
        const val EXTRA_GAME_NAME = "extra_game_name"
        const val EXTRA_PROFILE_NAME = "extra_profile_name"
    }
}
