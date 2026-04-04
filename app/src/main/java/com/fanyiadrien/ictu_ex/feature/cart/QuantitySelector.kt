package com.fanyiadrien.ictu_ex.feature.cart

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pill-shaped  ─  quantity control.
 *
 *   [ − ]  [ 2 ]  [ + ]
 *
 * The count animates vertically when it changes.
 * The − button is disabled (greyed) when quantity == 1.
 */
@Composable
fun QuantitySelector(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape       = RoundedCornerShape(10.dp)
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val disabledAlpha = 0.35f

    Row(
        modifier          = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .height(34.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Decrement ─────────────────────────────────────────────────────────
        val decSource = remember { MutableInteractionSource() }
        val decPressed by decSource.collectIsPressedAsState()
        val canDecrement = quantity > 1

        Box(
            modifier = Modifier
                .width(34.dp)
                .fillMaxHeight()
                .scale(if (decPressed) 0.88f else 1f)
                .clickable(
                    interactionSource = decSource,
                    indication        = ripple(bounded = true, color = MaterialTheme.colorScheme.primary),
                    enabled           = canDecrement,
                    onClick           = onDecrement
                )
                .semantics { contentDescription = "Decrease quantity" },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "−",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Light,
                    fontSize   = 20.sp
                ),
                color = if (canDecrement)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha)
            )
        }

        // ── Divider ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight(0.55f)
                .background(borderColor)
        )

        // ── Count (animated) ──────────────────────────────────────────────────
        AnimatedContent(
            targetState   = quantity,
            transitionSpec = {
                if (targetState > initialState)
                    slideInVertically { -it } togetherWith slideOutVertically { it }
                else
                    slideInVertically { it } togetherWith slideOutVertically { -it }
            },
            label = "quantityAnim"
        ) { count ->
            Box(
                modifier         = Modifier
                    .width(38.dp)
                    .fillMaxHeight()
                    .semantics { contentDescription = "Quantity $count" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = count.toString(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // ── Divider ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight(0.55f)
                .background(borderColor)
        )

        // ── Increment ─────────────────────────────────────────────────────────
        val incSource = remember { MutableInteractionSource() }
        val incPressed by incSource.collectIsPressedAsState()

        Box(
            modifier = Modifier
                .width(34.dp)
                .fillMaxHeight()
                .scale(if (incPressed) 0.88f else 1f)
                .clickable(
                    interactionSource = incSource,
                    indication        = ripple(bounded = true, color = MaterialTheme.colorScheme.primary),
                    onClick           = onIncrement
                )
                .semantics { contentDescription = "Increase quantity" },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "+",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Light,
                    fontSize   = 20.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
