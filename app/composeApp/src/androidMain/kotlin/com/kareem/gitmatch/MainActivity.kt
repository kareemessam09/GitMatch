package com.kareem.gitmatch

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {

    private var deepLinkToken by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handleDeepLink(intent)

        setContent {
            val context = LocalContext.current
            App(
                onOpenOAuthUrl = { url ->
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(browserIntent)
                },
                onOpenUrl = { url ->
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(browserIntent)
                },
                deepLinkToken = deepLinkToken
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "gitmatch" && uri.host == "login") {
            deepLinkToken = uri.getQueryParameter("token")
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}