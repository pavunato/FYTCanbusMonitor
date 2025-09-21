package com.aoe.canbusmonitor

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_CANBUS
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_CAN_UP
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_MAIN
import com.aoe.fytcanbusmonitor.ModuleCodes.MODULE_CODE_SOUND
import com.aoe.fytcanbusmonitor.MsToolkitConnection

class MainActivity : AppCompatActivity() {
    private lateinit var pipButton: ImageButton

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configurePipButton()
        ModuleCallback.init(this)
        connectMain()
        connectCanbus()
        connectSound()
        connectCanUp()
        MsToolkitConnection.instance.connect(this)
    }

    private fun configurePipButton() {
        pipButton = findViewById(R.id.pip_button)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pipButton.visibility = View.VISIBLE
            pipButton.setOnClickListener {
                if (!isInPictureInPictureMode) {
                    enterPictureInPictureMode(buildPictureInPictureParams())
                }
            }
        } else {
            pipButton.visibility = View.GONE
        }
    }

    private fun connectMain() {
        val callback = ModuleCallback("Main", findViewById(R.id.text_view))
        val connection = IPCConnection(MODULE_CODE_MAIN)
        for (i in 0..119) {
            connection.addCallback(callback, i)
        }
        MsToolkitConnection.instance.addObserver(connection)
    }

    private fun connectCanbus() {
        val callback = ModuleCallback("Canbus", findViewById(R.id.text_view))
        val connection = IPCConnection(MODULE_CODE_CANBUS)
        for (i in 0..50) {
            connection.addCallback(callback, i)
        }
        for (i in 1000..1036) {
            connection.addCallback(callback, i)
        }
        MsToolkitConnection.instance.addObserver(connection)
    }

    private fun connectSound() {
        val callback = ModuleCallback("Sound", findViewById(R.id.text_view))
        val connection = IPCConnection(MODULE_CODE_SOUND)
        for (i in 0..49) {
            connection.addCallback(callback, i)
        }
        MsToolkitConnection.instance.addObserver(connection)
    }

    private fun connectCanUp() {
        val callback = ModuleCallback("CanUp", findViewById(R.id.text_view))
        val connection = IPCConnection(MODULE_CODE_CAN_UP)
        connection.addCallback(callback, 100)
        MsToolkitConnection.instance.addObserver(connection)
    }

    override fun onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isInPictureInPictureMode) {
            enterPictureInPictureMode(buildPictureInPictureParams())
        }
        super.onUserLeaveHint()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (::pipButton.isInitialized) {
            pipButton.visibility = if (isInPictureInPictureMode) View.GONE else View.VISIBLE
        }
        if (isInPictureInPictureMode) {
            supportActionBar?.hide()
        } else {
            supportActionBar?.show()
        }
    }

    private fun buildPictureInPictureParams(): PictureInPictureParams {
        val textView = findViewById<TextView>(R.id.text_view)
        val width = textView.width.coerceAtLeast(1)
        val height = textView.height.coerceAtLeast(1)
        val ratio = if (width > 0 && height > 0) {
            Rational(width, height)
        } else {
            Rational(16, 9)
        }
        return PictureInPictureParams.Builder()
            .setAspectRatio(ratio)
            .build()
    }
}
