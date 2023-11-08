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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.github.caoyfcode.todo.R
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TodoItem(
    modifier: Modifier = Modifier,
    emoji: String,
    subject: String,
    content: String,
    checked: Boolean,
    onRightOrIconClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var visible by remember {
        mutableStateOf(false)
    }
    var showingReversedChecked by remember {
        mutableStateOf(false)
    }
    val showingChecked = if (showingReversedChecked) {
        !checked
    } else {
        checked
    }

    if (!visible) { // 初始不可见后再可见
        LaunchedEffect(Unit) {
            visible = true
        }
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(),
        modifier = modifier.fillMaxWidth()
    ) {
        val contentColor = if (showingChecked) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.onBackground
        }
        DragLayout(
            onRight = onRightOrIconClick,
            onBelowHalfRight = { showingReversedChecked = false},
            onAboveHalfRight = { showingReversedChecked = true },
            background =  { modifier ->
                Surface(
                    modifier = modifier,
                    color = MaterialTheme.colorScheme.background,
                    contentColor = contentColor,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!showingChecked) {
                            IconButton(onClick = onEditClick) {
                                Icon(
                                    painter = painterResource(id = R.drawable.edit),
                                    contentDescription = stringResource(id = R.string.edit)
                                )
                            }
                        }
                        IconButton(onClick = onDeleteClick) {
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
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20),
                border = BorderStroke(2.dp, contentColor),
                color = MaterialTheme.colorScheme.background,
                contentColor = contentColor,
            ) {
                TodoItemContent(
                    emoji = emoji,
                    subject = subject,
                    content = content,
                    checked = showingChecked,
                    onIconClick = onRightOrIconClick,
                )
            }
        }
    }
}

/**
 * 一个带有前景与背景两层的 layout
 * - 向左拖动时, 前景一起移动, 拖动到背景大小时松手将停在此处, 否则回到原点
 * - 向右拖动时, 前景背景一起移动, 若拖动超过一半后松手, 将移动到右侧, 并调用 onRight, 否则回到原点
 *
 * @param onRight 移动到右侧之后调用
 * @param onBelowHalfRight 当从右侧一半的右侧至左侧调用
 * @param onAboveHalfRight 当从右侧一半的左侧至右侧调用
 * @param background 背景, 左移后看到
 * @param content 内容(前景)
 */
@Composable
fun DragLayout(
    onRight: () -> Unit = {},
    onBelowHalfRight: () -> Unit = {},
    onAboveHalfRight: () -> Unit = {},
    background: @Composable ((Modifier) -> Unit),
    content: @Composable ((Modifier) -> Unit),
) {
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) } // 当前内容偏移量
    var backgroundWidth by remember { mutableStateOf(0f) } // 储存背景组件宽度(可交互宽度)
    var allWidth by remember { mutableStateOf(0f) }

    val draggableState = rememberDraggableState {
        scope.launch {
            val prevOffset = offset.value
            offset.snapTo(offset.value + it)
            val curOffset = offset.value
            if (prevOffset < 0.5 * allWidth && curOffset >= 0.5 * allWidth) {
                onAboveHalfRight()
            } else if (prevOffset >= 0.5 * allWidth && curOffset < 0.5 * allWidth) {
                onBelowHalfRight()
            }
        }
    }

    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .offset {
                IntOffset(
                    max(offset.value.roundToInt(), 0),
                    0
                )
            }
            .onSizeChanged {
                allWidth = it.width.toFloat()
                offset.updateBounds(upperBound = 1.5f * allWidth)
            }
    ) {
        background(
            Modifier.onSizeChanged {
                backgroundWidth = it.width.toFloat()
                offset.updateBounds(lowerBound = -backgroundWidth)
            }
        )
        content(
            Modifier
                .offset {
                    IntOffset(
                        min(offset.value.roundToInt(), 0),
                        0
                    )
                }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        if (offset.value <= -backgroundWidth) {
                            offset.animateTo(
                                -backgroundWidth
                            )
                        } else if (offset.value >= 0.5 * allWidth) {
                            offset.animateTo(
                                1.5f * allWidth,
                                initialVelocity = velocity
                            )
                            onRight()
                        } else {
                            offset.animateTo(0f, initialVelocity = velocity)
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
    onIconClick: () -> Unit,
) {
    var folding by remember { mutableStateOf(true) } // 是否收起内容
    TodoItemContentLayout(
        folding = folding || content.isEmpty(),
        icon = {
            IconButton(onClick = onIconClick) {
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
        Layout(
            modifier = Modifier.wrapContentSize(),
            content = {
                icon()
                subject()
                foldingIcon() // 注意, 这个可能为空
            }
        ) { measurables, constraints -> // 子项列表, 来自父项的约束
            val iconPlaceable = measurables[0].measure(constraints) // icon
            val foldingIconPlaceable = if (measurables.size == 3) { // 末尾的折叠图标可能不存在
                measurables[2].measure(constraints)
            } else {
                null
            }
            val subjectMaxWidth = if (constraints.maxWidth == Constraints.Infinity) {
                constraints.maxWidth
            } else {
                (constraints.maxWidth - iconPlaceable.width - (foldingIconPlaceable?.width ?: 0))
                    .coerceAtLeast(0)
            }
            val subjectPlaceable = measurables[1].measure(constraints.copy(maxWidth = subjectMaxWidth))
            val height = maxOf(iconPlaceable.height, subjectPlaceable.height, foldingIconPlaceable?.width ?: 0)
            layout(width = constraints.maxWidth, height = height) {
                iconPlaceable.placeRelative(
                    x = 0,
                    y = (height - iconPlaceable.height) / 2
                )
                subjectPlaceable.placeRelative(
                    x = iconPlaceable.width,
                    y = (height - subjectPlaceable.height) / 2
                )
                foldingIconPlaceable?.place(
                    x = constraints.maxWidth - foldingIconPlaceable.width,
                    y = (height - foldingIconPlaceable.height) / 2
                )
            }
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
            onRightOrIconClick = { checked = ! checked },
            onEditClick = {},
            onDeleteClick = {},
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
                onIconClick = {},
            )
        }
    }
}