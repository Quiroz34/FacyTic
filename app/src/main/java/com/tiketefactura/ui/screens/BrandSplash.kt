package com.tiketefactura.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tiketefactura.R

@Composable
fun AppRoot(vm: MainViewModel = hiltViewModel()) {
    val view = LocalView.current
    @Suppress("DEPRECATION") // Se conserva para Android anteriores a la política edge-to-edge obligatoria.
    SideEffect {
        val window = (view.context as android.app.Activity).window
        val color = if (vm.ready) android.graphics.Color.WHITE else android.graphics.Color.rgb(16, 26, 54)
        window.statusBarColor = color; window.navigationBarColor = color
        WindowCompat.getInsetsController(window, view).apply { isAppearanceLightStatusBars = vm.ready; isAppearanceLightNavigationBars = vm.ready }
    }
    if (vm.ready) MainScreen(vm) else {
        BrandSplash()
        vm.startupError?.let { message -> AlertDialog(onDismissRequest = {}, title = { Text("No se pudieron cargar los datos") }, text = { Text(message) }, confirmButton = { TextButton(onClick = vm::load) { Text("Reintentar") } }) }
    }
}

@Composable
private fun BrandSplash() {
    val transition = rememberInfiniteTransition(label = "Carga")
    val scale by transition.animateFloat(0.96f, 1.04f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "Marca")
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF101A36), Color(0xFF30265E), Color(0xFF15566A))))) {
        Box(Modifier.size(320.dp).align(Alignment.TopEnd).offset(x = 150.dp, y = (-100).dp).background(Color.White.copy(alpha = 0.04f), CircleShape))
        Box(Modifier.size(250.dp).align(Alignment.BottomStart).offset(x = (-140).dp, y = 70.dp).background(Color(0xFF39DFC6).copy(alpha = 0.08f), CircleShape))
        Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.size(124.dp).scale(scale).background(Brush.linearGradient(listOf(Color(0xFF6D6CF7), Color(0xFF4F46BC))), RoundedCornerShape(34.dp)).border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(34.dp)), contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.ic_launcher_foreground), "Tikete y Factura", Modifier.size(124.dp))
            }
            Text("Tikete y Factura", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 30.sp, textAlign = TextAlign.Center)
            Text("Tu negocio, en orden.", color = Color(0xFFDCE4F7), fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(Modifier.width(150.dp).height(3.dp), color = Color(0xFF5BE4CB), trackColor = Color.White.copy(alpha = 0.12f))
            Text("Preparando tu espacio", color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.labelMedium)
        }
        Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Desarrollada por", color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp)
            Text("QZ Web Solutions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, letterSpacing = 1.sp)
        }
    }
}
