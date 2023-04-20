package com.github.caoyfcode.todo.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
    content: String,
    checked: Boolean,
    scaleIn: Boolean = false,
    onToggleChecked: () -> Unit,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    var reverseChecked by remember { mutableStateOf(false) } // 是否显示为另一种形态
    val shownChecked = if (reverseChecked) !checked else checked
    var shown by remember { mutableStateOf(!scaleIn) } // 是否可见
    if (!shown) { // 若 scaleIn, 则初始不可见, 第一次组合之后变为可见
        LaunchedEffect(Unit) {
            shown = true
        }
    }
    AnimatedVisibility(
        visible = shown,
        enter = scaleIn(),
        modifier = modifier
    ) {
        val contentColor = if (shownChecked) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.onBackground
        }
        SwipeLayout(
            onRightThreshold = { reverseChecked = true },
            onBelowRightThreshold = { reverseChecked = false },
            onRight = onToggleChecked,
            background =  { modifier ->
                Surface(
                    modifier = modifier,
                    color = MaterialTheme.colorScheme.background,
                    contentColor = contentColor,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!shownChecked) {
                            IconButton(onClick = onEditClicked) {
                                Icon(
                                    painter = painterResource(id = R.drawable.edit),
                                    contentDescription = stringResource(id = R.string.edit)
                                )
                            }
                        }
                        IconButton(onClick = onDeleteClicked) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = stringResource(id = R.string.delete)
                            )
                        }
                    }
                }
            }
        ) { modifier ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(modifier),
                shape = RoundedCornerShape(20),
                border = BorderStroke(2.dp, contentColor),
                color = MaterialTheme.colorScheme.background,
                contentColor = contentColor,
            ) {
                TodoItemContent(
                    emoji = emoji,
                    subject = subject,
                    content = content,
                    checked = shownChecked,
                    onToggleChecked = onToggleChecked,
                )
            }
        }
    }
}

enum class DragState {
    Left,
    Right,
    None
}

/**
 * 一个可以左右滑动的 layout, 有两个槽, 左滑移动内容(前景), 右滑移动整体.
 * - 向右拖动时, 当拖动到右阈值时松手或者松手后速度够大, 将移出屏幕, 否则回到原点
 * - 向左拖动时, 拖动到背景大小时松手将停在此处, 否则回到原点
 * 当拖动经过阈值时, 将调用 `onRightThreshold` 与 `onBelowRightThreshold`
 *
 * @param onRightThreshold 整体移动时, 向右经过右阈值调用
 * @param onBelowRightThreshold 整体移动时, 向左经过右阈值调用
 * @param onRight 向右移出屏幕后调用
 * @param background 背景, 左移后看到
 * @param content 内容(前景)
 */
@Composable
fun SwipeLayout(
    onRightThreshold: () -> Unit,
    onBelowRightThreshold: () -> Unit,
    onRight: () -> Unit,
    background: @Composable ((Modifier) -> Unit),
    content: @Composable ((Modifier) -> Unit),
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) } // 当前整体偏移量
    val contentOffsetX = remember { Animatable(0f) } // 当前内容偏移量

    var dragState = remember { DragState.None } // 当前在左边、右边还是没动

    var width by remember { mutableStateOf(0f) } // 储存组件宽度(可交互宽度)
    var backgroundWidth by remember { mutableStateOf(0f) } // 储存背景组件宽度(可交互宽度)

    val rightThreshold = 0.4f * width // 滑动到此认为需要调用 onRight
    val rightUpperbound = 1.5f * width // 右滑边界
    val draggableState = rememberDraggableState {
        scope.launch {
            if (dragState == DragState.None) {
                if (it > 0f) {
                    dragState = DragState.Right
                }
                if (it < 0f) {
                    dragState = DragState.Left
                }
            }
            when (dragState) {
                DragState.Right -> {
                    val targetOffsetX = offsetX.value + it
                    val beforeOffsetX = offsetX.value
                    offsetX.snapTo(targetOffsetX)
                    contentOffsetX.snapTo(0f)
                    val afterOffsetX = offsetX.value
                    if (beforeOffsetX < rightThreshold && afterOffsetX >= rightThreshold) {
                        onRightThreshold()
                    } else if (afterOffsetX < rightThreshold && beforeOffsetX >= rightThreshold) {
                        onBelowRightThreshold()
                    }
                    if (offsetX.value == 0f) {
                        dragState = if (targetOffsetX < 0f) {
                            DragState.Left
                        } else {
                            DragState.None
                        }
                    }
                }
                DragState.Left -> {
                    val targetContentOffsetX = contentOffsetX.value + it
                    offsetX.snapTo(0f)
                    contentOffsetX.snapTo(targetContentOffsetX)
                    if (contentOffsetX.value == 0f) {
                        dragState = if (targetContentOffsetX > 0f) {
                            DragState.Right
                        } else {
                            DragState.None
                        }
                    }
                }
                else -> {}
            }
        }
    }

    Box(
        modifier = Modifier.offset {
            IntOffset(offsetX.value.roundToInt(), 0)
        },
        contentAlignment = Alignment.CenterEnd
    ) {
        background(
            Modifier.pointerInput(Unit) {
                backgroundWidth = size.width.toFloat()
                contentOffsetX.updateBounds(
                    lowerBound = -backgroundWidth,
                    upperBound = 0f
                )
            }
        )
        content(
            Modifier
                .offset {
                    IntOffset(contentOffsetX.value.roundToInt(), 0)
                }
                .pointerInput(Unit) {
                    width = size.width.toFloat()
                    offsetX.updateBounds(
                        lowerBound = 0f,
                        upperBound = 1.5f * width
                    )
                }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        if (dragState == DragState.Left
                            && contentOffsetX.value <= -backgroundWidth) {
                            contentOffsetX.animateTo(
                                -backgroundWidth
                            )
                        } else {
                            contentOffsetX.animateTo(0f, initialVelocity = velocity)
                        }
                        // 经过打印发现 width 在 900 多, velocity 轻轻的时为一两千, 稍微不轻就接近万了
                        if (dragState == DragState.Right
                            && (velocity >= 2000f
                                    || offsetX.value >= rightThreshold)
                        ) {
                            onRightThreshold()
                            offsetX.animateTo(rightUpperbound, initialVelocity = velocity)
                            onRight()
                        } else {
                            onBelowRightThreshold()
                            offsetX.animateTo(0f, initialVelocity = velocity)
                        }
                    }
                )
        )
    }
}

@Composable
fun TodoItemContent(
    emoji: String,
    subject: String,
    content: String,
    checked: Boolean,
    onToggleChecked: () -> Unit,
) {
    var folding by remember { mutableStateOf(true) } // 是否收起内容
    TodoItemContentLayout(
        folding = folding || content.isEmpty(),
        icon = {
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
        },
        subject = {
            Text(text = subject)
        },
        foldingIcon = {
            if (content.isNotEmpty()) {
                IconButton(onClick = { folding = !folding }) {
                    Icon(
                        painter = painterResource(id = R.drawable.folding),
                        contentDescription = stringResource(id = R.string.toggle_folding),
                        modifier = Modifier.rotate(if (folding) 0f else -90f)
                    )
                }
            }
        },
        foldingContent = { modifier ->
            Text(text = content, modifier = modifier)
        }
    )
}

/**
 * 一个具有四个槽的 layout
 */
@Composable
fun TodoItemContentLayout(
    folding: Boolean,
    icon: @Composable (() -> Unit),
    subject: @Composable (() -> Unit),
    foldingIcon: @Composable (() -> Unit),
    foldingContent: @Composable ((Modifier) -> Unit)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.padding(end = 10.dp), // 与右边按钮最少间隔 10dp
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start), // 子项水平相隔 10dp
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon()
                subject()
            }
            foldingIcon()
        }
        if (!folding) {
            foldingContent(
                Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 10.dp))
        }
    }
}

@Preview
@Composable
fun ItemPreview() {
    var checked by remember {
        mutableStateOf(false)
    }
    com.github.caoyfcode.todo.ui.theme.TodoTheme {
        TodoItem(
            emoji = "😀",
            subject = "subject",
            content = "content content",
            checked = checked,
            scaleIn = false,
            onToggleChecked = { checked = ! checked },
            onEditClicked = {},
            onDeleteClicked = {},
        )
    }
}

@Preview
@Composable
fun TestBox() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(text = "back")
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(20),
            border = BorderStroke(2.dp, Color.Black),
            color = Color.White,
            contentColor = Color.Black,
        ) {
            TodoItemContent(
                emoji = "😀",
                subject = "sub",
                content = "content",
                checked = false,
                onToggleChecked = {},
            )
        }
    }
}