package com.asdroid.jetpack_ui.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.asdroid.jetpack_ui.R
import com.asdroid.jetpack_ui.ui.components.SectionCard
import com.asdroid.jetpack_ui.ui.components.SelectableChip
import com.asdroid.jetpack_ui.ui.theme.SignalLime
import com.asdroid.jetpack_ui.ui.theme.TextMuted

private data class Variant(
    val titleRes: Int,
    val subtitleRes: Int,
    val pickRes: Int,
    val imageRes: Int,
)

private val Variants = listOf(
    Variant(
        titleRes = R.string.variant_all_title,
        subtitleRes = R.string.variant_all_subtitle,
        pickRes = R.string.variant_all_pick,
        imageRes = R.drawable.best_for_all,
    ),
    Variant(
        titleRes = R.string.variant_4g_title,
        subtitleRes = R.string.variant_4g_subtitle,
        pickRes = R.string.variant_4g_pick,
        imageRes = R.drawable.best_for_4g,
    ),
    Variant(
        titleRes = R.string.variant_5g_title,
        subtitleRes = R.string.variant_5g_subtitle,
        pickRes = R.string.variant_5g_pick,
        imageRes = R.drawable.best_for_5g,
    ),
)

private val PickerLabels = listOf(
    R.string.picker_auto,
    R.string.picker_4g,
    R.string.picker_5g,
)

/**
 * The three real screenshots, with a picker that scrolls straight to the matching
 * card, page dots, and tap-to-zoom (pinch to magnify further).
 */
@Composable
fun GuideScreen(modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var zoomedIndex by rememberSaveable { mutableStateOf(-1) }

    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }
    val visibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(R.string.guide_heading),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.guide_intro),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(R.string.picker_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PickerLabels.forEachIndexed { index, labelRes ->
                    SelectableChip(
                        text = stringResource(labelRes),
                        selected = index == selectedIndex,
                        onClick = { selectedIndex = index },
                    )
                }
            }
        }

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(Variants) { index, variant ->
                VariantCard(
                    variant = variant,
                    selected = index == selectedIndex,
                    onClick = { zoomedIndex = index },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Variants.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (index == visibleIndex) {
                                SignalLime
                            } else {
                                Color.White.copy(alpha = 0.25f)
                            },
                        ),
                )
            }
        }

        SectionCard(
            title = stringResource(Variants[selectedIndex].titleRes),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Text(
                text = stringResource(Variants[selectedIndex].subtitleRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Variants[selectedIndex].pickRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    if (zoomedIndex in Variants.indices) {
        val variant = Variants[zoomedIndex]
        ZoomDialog(
            imageRes = variant.imageRes,
            contentDescription = stringResource(variant.titleRes),
            onDismiss = { zoomedIndex = -1 },
        )
    }
}

@Composable
private fun VariantCard(
    variant: Variant,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .width(280.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = if (selected) {
                    SignalLime.copy(alpha = 0.85f)
                } else {
                    Color.White.copy(alpha = 0.07f)
                },
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(variant.titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = SignalLime,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(variant.subtitleRes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Image(
            painter = painterResource(variant.imageRes),
            contentDescription = stringResource(variant.titleRes),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(variant.pickRes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.guide_pick_hint),
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted,
        )
    }
}

@Composable
private fun ZoomDialog(
    imageRes: Int,
    contentDescription: String,
    onDismiss: () -> Unit,
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        offset = if (scale <= 1f) Offset.Zero else offset + pan
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    ),
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.zoom_close),
                    tint = Color.White,
                )
            }
        }
    }
}
