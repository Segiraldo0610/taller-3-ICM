package com.example.taller3firebase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taller3firebase.navigation.AppNavigation
import com.example.taller3firebase.ui.theme.Taller3Theme

@Composable
fun Taller3App(
    modifier: Modifier = Modifier,
    application: Taller3Application,
) {
    Taller3Theme(modifier = modifier.fillMaxSize()) {
        val container = application.container
        if (application.firebaseReady && container != null) {
            AppNavigation(container = container)
        } else {
            SetupRequiredScreen()
        }
    }
}

@Composable
private fun SetupRequiredScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.firebase_setup_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.firebase_setup_message),
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

