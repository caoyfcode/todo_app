package com.github.caoyfcode.todo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.scaleIn
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.caoyfcode.todo.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
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
    val contentColor = if (checked) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.onBackground
    }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!shown) {
            shown = true
        }
    }
    AnimatedVisibility(
        visible = shown,
        enter = scaleIn(),
        modifier = modifier.swipeable { onToggleChecked() }
    ) {
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
                    if (!checked) {
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
 * 设置组件可左右滑动
 * @param onRight 当滑动到右边界后调用
 */
fun Modifier.swipeable(
    onRight: () -> Unit
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    pointerInput(Unit) {
        offsetX.updateBounds(
            lowerBound = 0f,
            upperBound = 2 * size.width.toFloat()
        )
        val decay = splineBasedDecay<Float>(this) // 得到一个时间间隔(但是代表什么不知道)
        coroutineScope {
            while (true) {
                val pointerId = awaitPointerEventScope { awaitFirstDown().id }
                offsetX.stop()
                val velocityTracker = VelocityTracker() // 用于计算滑动速度, 方便计算松手后是否会滑到界限
                awaitPointerEventScope {
                    horizontalDrag(pointerId) { change ->
                        launch {
                            offsetX.snapTo(offsetX.value + change.positionChange().x)
                        }
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                    }
                }
                // 手指松开后(horizontalDrag事件结束)
                val velocity = velocityTracker.calculateVelocity().x // 计算当前手指速度
                val targetOffsetX = decay.calculateTargetValue(offsetX.value, velocity)
                launch {
                    if (targetOffsetX.absoluteValue > 2 * size.width) {
                        offsetX.animateDecay(velocity, decay) // 减速(decay)动画
                        onRight()
                    } else {
                        offsetX.animateTo(targetValue = 0f, initialVelocity = velocity)
                    }
                }
            }
        }
    }.offset { IntOffset(offsetX.value.roundToInt(), 0) }
}