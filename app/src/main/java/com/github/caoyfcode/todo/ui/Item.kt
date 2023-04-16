package com.github.caoyfcode.todo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.caoyfcode.todo.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TodoItem(
    modifier: Modifier = Modifier,
    emoji: String,
    subject: String,
    checked: Boolean,
    onToggleChecked: () -> Unit,
) {
    var reverseChecked by remember { mutableStateOf(false) } // 是否显示为另一种形态
    val shownChecked = if (reverseChecked) !checked else checked
    var shown by remember { mutableStateOf(false) } // 是否可见
    LaunchedEffect(Unit) {
        if (!shown) {
            shown = true
        }
    }
    AnimatedVisibility(
        visible = shown,
        enter = scaleIn(),
        modifier = modifier.swipeable(
            onThreshold = { reverseChecked = true },
            onBelowThreshold = { reverseChecked = false },
            onRight = onToggleChecked,
        )
    ) {
        val contentColor = if (shownChecked) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.onBackground
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20),
            border = BorderStroke(2.dp, contentColor),
            color = MaterialTheme.colorScheme.background,
            contentColor = contentColor,
        ) {
            Row(
                modifier = Modifier.padding(10.dp), // 10dp 水平: 与子项间隔相同, 竖直: 保持最小高度为 20dp + content size
                horizontalArrangement = Arrangement.spacedBy(10.dp), // 子项水平相隔 10dp
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onToggleChecked) {
                    if (!shownChecked) {
                        Text(text = emoji)
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = stringResource(id = R.string.checked_todo)
                        )
                    }
                }
                Text(text = subject)
            }
        }
    }
}

/**
 * 设置组件可右滑, 右滑超过阈值则将组件滑出屏幕, 并调用 onRight, 否则复原偏移量.
 *
 * 这里拖动代表手指未离开屏幕时, 滑动到阈值代表拖动结束且已经超过了拖动阈值或者计算惯性后到达了惯性阈值
 * @param onRight 当滑动到阈值后调用
 * @param onThreshold 当拖动到阈值或滑动到阈值调用
 * @param onBelowThreshold 当拖动到阈值后又拖回后或者复原时调用
 */
fun Modifier.swipeable(
    onThreshold: () -> Unit,
    onBelowThreshold: () -> Unit,
    onRight: () -> Unit,
): Modifier = composed {
    val offsetX = remember { Animatable(0f) } // 当前偏移量
    val scope = rememberCoroutineScope()
    var width by remember { mutableStateOf(0f) } // 储存组件宽度(可交互宽度)
    val thresholdOffsetX = 0.4f * width // 滑动到此认为需要调用 onRight
    val flingThresholdOffsetX = 1.5f * width // 计算惯性后滑动到此也调用 onRight
    val draggableState = rememberDraggableState {
        scope.launch {
            val beforeOffsetX = offsetX.value
            offsetX.snapTo(offsetX.value + it)
            val afterOffsetX = offsetX.value
            if (beforeOffsetX < thresholdOffsetX && afterOffsetX >= thresholdOffsetX) {
                onThreshold()
            } else if (afterOffsetX < thresholdOffsetX && beforeOffsetX >= thresholdOffsetX) {
                onBelowThreshold()
            }
        }
    }
    offset {
        IntOffset(offsetX.value.roundToInt(), 0)
    }.pointerInput(Unit) {
        width = size.width.toFloat()
        offsetX.updateBounds(
            lowerBound = 0f,
            upperBound = 2 * width
        )
    }.draggable(
        state = draggableState,
        orientation = Orientation.Horizontal,
        onDragStopped = { velocity ->
//            Log.i("DRAG_STOP", "width is $width")
//            Log.i("DRAG_STOP", "velocity is $velocity")
            // 经过打印大砍 width 在 900 多, velocity 轻轻的时为一两千, 稍微不轻就接近万了
            val targetOffsetX = offsetX.value + velocity
            if (targetOffsetX > flingThresholdOffsetX || offsetX.value >= thresholdOffsetX) {
                onThreshold()
                offsetX.animateTo(flingThresholdOffsetX, initialVelocity = velocity)
                onRight()
            } else {
                onBelowThreshold()
                offsetX.animateTo(0f, initialVelocity = velocity)
            }
        }
    )
}