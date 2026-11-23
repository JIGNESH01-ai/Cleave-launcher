package net.kdt.pojavlaunch.cleave.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import net.kdt.pojavlaunch.cleave.bridge.PojavAccountBridge
import net.kdt.pojavlaunch.cleave.social.SocialBackend
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.ui.CleaveNav
import net.kdt.pojavlaunch.cleave.ui.LauncherSection

@Composable
fun CleaveAccountSetupScreen(activity: FragmentActivity, onBack: () -> Unit) {
    LaunchedEffect(Unit) {
        PojavAccountBridge.ensureLoaded()
        SocialBackend.attach(activity)
    }
    if (SocialBackend.signedIn) {
        ProfileCosmeticsPage(activity, setupFlow = true, onBack = onBack)
        return
    }
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(20.dp), color = CleaveSurface.panelRaised,
            border = BorderStroke(1.dp, CleaveSurface.border), modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("CLEAVE account", color = CleaveSurface.textPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp)
                Text("Sign in or create an account with Google. Then choose your Minecraft name, skin and cape.", color = CleaveSurface.textSecondary, fontSize = 13.sp)
                Button(onClick = {
                    if (!SocialBackend.beginGoogleSignIn(activity, true))
                        Toast.makeText(activity, "Could not open Google sign-in", Toast.LENGTH_LONG).show()
                }, modifier = Modifier.fillMaxWidth()) { Text("Continue with Google") }
                OutlinedButton(onClick = { CleaveNav.navigate(LauncherSection.SOCIALS) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Use email instead")
                }
                Text("Your CLEAVE account works on servers configured for CLEAVE authentication. Microsoft-only servers require a Microsoft account; skins depend on the server and game version.", color = CleaveSurface.textSecondary, fontSize = 11.sp)
                OutlinedButton(onClick = onBack) { Text("Back") }
            }
        }
    }
}
