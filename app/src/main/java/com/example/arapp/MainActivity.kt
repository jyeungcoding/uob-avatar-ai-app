package com.example.arapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.arapp.ui.ArScreen
import com.example.arapp.ui.MainViewModel
import com.example.arapp.ui.AvatarViewModel
import com.example.arapp.ui.components.CameraPermissionRequestProvider
import com.example.arapp.ui.components.PermissionDialog
import com.example.arapp.ui.components.RecordAudioPermissionRequestProvider
import com.example.arapp.ui.theme.ARAppTheme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    // Delegate to viewModels to retain its value through
    // configuration changes.
    private val mainViewModel: MainViewModel by viewModels()
    private val avatarViewModel: AvatarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.initController(this)

        setContent {
            ARAppTheme {
                val dialogQueue = mainViewModel.visiblePermissionDialogQueue
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ArScreen(
                        mainViewModel,
                        avatarViewModel
                    )
                    dialogQueue
                        .reversed()
                        .forEach { permission ->
                            PermissionDialog(
                                permissionTextProvider = when (permission) {
                                    Manifest.permission.CAMERA -> {
                                        CameraPermissionRequestProvider()
                                    }

                                    Manifest.permission.RECORD_AUDIO -> {
                                        RecordAudioPermissionRequestProvider()
                                    }

                                    else -> return@forEach
                                },
                                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                                    permission
                                ),
                                onDismiss = mainViewModel::dismissDialog,
                                onEnableClick = {
                                    mainViewModel.dismissDialog()
                                },
                                onGoToAppSettingsClick = ::openAppSettings
                            )
                        }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.controller.release()
        mainViewModel.resetControllerState()
    }
}

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}