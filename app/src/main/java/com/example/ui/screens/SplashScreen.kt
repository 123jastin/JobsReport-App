package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
  // Navigation trigger
  LaunchedEffect(Unit) {
    delay(2500L)
    onSplashComplete()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F172A)),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // SVG Logo Representation: Rounded box with #1a1a2e fill
      Box(
        modifier = Modifier
          .size(120.dp)
          .clip(RoundedCornerShape(28.dp))
          .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
      ) {
        // Gradient text "JR" matching the SVG linearGradient from #7c3aed to #3b82f6
        Text(
          text = "JR",
          style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 54.sp,
            brush = Brush.linearGradient(
              colors = listOf(Color(0xFF7C3AED), Color(0xFF3B82F6))
            )
          )
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Logo Text Label
      Text(
        text = "JobsReport",
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        color = Color.White
      )
      
      Text(
        text = "SECURE INTELLIGENCE PORTAL",
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        color = Color(0xFF10B981),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(56.dp))

      // Bouncing dots loader resembling Facebook loading dots
      FacebookLoadingDots()

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "CONNECTING TERMINAL...",
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = Color(0xFF64748B)
      )
    }
  }
}

@Composable
fun FacebookLoadingDots() {
  val dots = remember { listOf(Animatable(0f), Animatable(0f), Animatable(0f), Animatable(0f)) }

  dots.forEachIndexed { index, animatable ->
    LaunchedEffect(animatable) {
      // Stagger start of each dot animation to create wave bounce effect
      delay(index * 150L)
      while (true) {
        // Bounce up
        animatable.animateTo(
          targetValue = 1f,
          animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
          )
        )
        // Bounce down
        animatable.animateTo(
          targetValue = 0f,
          animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
          )
        )
        // Stagger rest between cycles
        delay(200L)
      }
    }
  }

  Row(
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    dots.forEach { animatable ->
      val size = 12.dp
      // Translate Y based on animation value
      val translationY = -(animatable.value * 14.dp.value)
      
      Box(
        modifier = Modifier
          .size(size)
          .offset(y = translationY.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              colors = listOf(Color(0xFF7C3AED), Color(0xFF3B82F6))
            )
          )
      )
    }
  }
}
