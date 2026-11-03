package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class Particle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  var size: Float,
  val color: Color
)

@Composable
fun CannonConfettiAnimation(
  visible: Boolean,
  onDismiss: () -> Unit
) {
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(tween(250)),
    exit = fadeOut(tween(400))
  ) {
    val progress = remember { Animatable(0f) }

    val particles = remember {
      val colors = listOf(
        Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFE5B869),
        Color(0xFF4CAF50), Color(0xFF64B5F6), Color(0xFFE91E63),
        Color(0xFFFFFFFF), Color(0xFFFFCC80)
      )
      List(75) {
        val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
        val speed = 250f + Random.nextFloat() * 450f
        Particle(
          x = 0.5f,
          y = 0.5f,
          vx = kotlin.math.cos(angle) * speed,
          vy = kotlin.math.sin(angle) * speed - 150f,
          size = 6f + Random.nextFloat() * 10f,
          color = colors[Random.nextInt(colors.size)]
        )
      }
    }

    LaunchedEffect(visible) {
      if (visible) {
        progress.snapTo(0f)
        progress.animateTo(
          targetValue = 1f,
          animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
        )
        delay(300)
        onDismiss()
      }
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.65f))
        .testTag("cannon_confetti_overlay"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = progress.value

        particles.forEach { p ->
          val currX = (p.x * w) + (p.vx * t)
          val currY = (p.y * h) + (p.vy * t) + (500f * t * t) // gravity
          val alpha = (1f - t).coerceIn(0f, 1f)
          if (currX in 0f..w && currY in 0f..h) {
            drawCircle(
              color = p.color.copy(alpha = alpha),
              radius = p.size * (1f - t * 0.4f),
              center = Offset(currX, currY)
            )
          }
        }
      }

      Card(
        modifier = Modifier
          .padding(24.dp)
          .testTag("cannon_celebration_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
      ) {
        Column(
          modifier = Modifier.padding(28.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(text = "💥 🕌 🌟", fontSize = 42.sp)
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Ramazan Topu Patladı!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "İftar vakti geldi!\nAllah oruçlarınızı kabul eylesin.\nHayırlı, bereketli iftarlar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
          )
          Spacer(modifier = Modifier.height(20.dp))
          Button(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_cannon_dialog_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Afiyet Olsun")
          }
        }
      }
    }
  }
}
