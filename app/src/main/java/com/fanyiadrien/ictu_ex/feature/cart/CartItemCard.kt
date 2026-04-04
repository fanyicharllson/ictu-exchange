package com.fanyiadrien.ictu_ex.feature.cart

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fanyiadrien.ictu_ex.data.model.CartItem
import java.text.NumberFormat
import java.util.Locale

/**
 * A single cart row.
 *
 * Layout (left → right):
 *   [Image 88×100] | [Name / ID / Size+Color / QuantitySelector] | [Price]
 *
 * Swipe-to-dismiss is handled by the parent LazyColumn via SwipeToDismissBox.
 */
@Composable
fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Parse colorHex safely
    val dotColor = remember(item.colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(item.colorHex)) }
            .getOrDefault(Color.Gray)
    }

    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .semantics { contentDescription = "Cart item: ${item.title}" },
        verticalAlignment = Alignment.Top
    ) {
        // ── Product image ─────────────────────────────────────────────────────
        AsyncImage(
            model              = item.imageUrl,
            contentDescription = item.title,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .size(width = 88.dp, height = 100.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(Modifier.width(14.dp))

        // ── Middle column: name / meta / controls ─────────────────────────────
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Title
            Text(
                text     = item.title,
                style    = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp,
                    lineHeight = 19.sp
                ),
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Item ID
            Text(
                text  = "ID: ${item.listingId}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Size + Color row
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Size chip — circular border
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = item.size,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 9.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Color dot
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                        .semantics { contentDescription = "Color ${item.colorHex}" }
                )

                // Color label
                Text(
                    text  = item.colorHex,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(2.dp))

            // Quantity selector + delete icon
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuantitySelector(
                    quantity    = item.quantity,
                    onDecrement = onDecrement,
                    onIncrement = onIncrement
                )

                IconButton(
                    onClick  = onRemove,
                    modifier = Modifier
                        .size(30.dp)
                        .semantics { contentDescription = "Remove ${item.title} from cart" }
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        // ── Price (right-aligned, top-aligned) ────────────────────────────────
        Text(
            text  = formatXaf(item.lineTotal),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private val xafFormat = NumberFormat.getNumberInstance(Locale.US).apply {
    maximumFractionDigits = 0
}

fun formatXaf(amount: Double): String = "XAF ${xafFormat.format(amount)}"
