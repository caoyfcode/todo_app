package com.github.caoyfcode.todo.ui

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.github.caoyfcode.todo.R
import com.github.caoyfcode.todo.entity.Group

/**
 * 侧边栏组件
 * @param groups a list of (emoji icon, name)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun Navigation(
    groups: List<Group>,
    selectedGroup: Int,
    onGroupSelected: (selected: Int) -> Unit,
    content: @Composable (openNavigation: () -> Unit) -> Unit
) {
    val windowWidthSizeClass = calculateWindowSizeClass(activity = LocalView.current.context as Activity).widthSizeClass
    if (windowWidthSizeClass == WindowWidthSizeClass.Compact) {
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen, // 展开时需要点击空白处来关闭, 关闭时禁用防止与列表项右滑冲突
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.fillMaxWidth(0.66f),
                    drawerContainerColor = MaterialTheme.colorScheme.secondary,
                ) {
                    NavigationContent(
                        groups = groups,
                        selectedGroup = selectedGroup,
                        onGroupSelected = {
                            scope.launch {
                                drawerState.close()
                            }
                            onGroupSelected(it)
                        }
                    )
                }
            }
        ) {
            content {
                scope.launch {
                    drawerState.open()
                }
            }
        }
    } else {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet (
                    modifier = Modifier.fillMaxWidth(0.3f),
                    drawerContainerColor = MaterialTheme.colorScheme.secondary,
                ) {
                    NavigationContent(
                        groups = groups,
                        selectedGroup = selectedGroup,
                        onGroupSelected = onGroupSelected
                    )
                }
            }
        ) {
            content { } // 默认就是打开的, 故传入的函数啥也不做
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationContent(
    groups: List<Group>, // (emoji icon, name)
    selectedGroup: Int, // uid of group, -1 if all todos
    onGroupSelected: (Int) -> Unit,
) {
    var hasDialog by rememberSaveable {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 30.dp,
                bottom = 20.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
        )
    }
    LazyColumn(
        modifier = Modifier.padding(top = 10.dp)
    ) {
        items(groups) {
            NavigationDrawerItem(
                icon = {
                    Text(text = it.icon)
                },
                label = {
                    Text(text = it.name)
                },
                selected = it.uid == selectedGroup,
                onClick = {
                    onGroupSelected(it.uid)
                },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                    unselectedContainerColor = MaterialTheme.colorScheme.secondary,
                )
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NavigationDrawerItemDefaults.ItemPadding),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { hasDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.config),
                        contentDescription = stringResource(id = R.string.config_group)
                    )
                }
            }
        }
    }

    if (hasDialog) {
        GroupsEditorDialog(
            groups = groups.filter { it.uid >= 0 },
            onDismiss = { hasDialog = false }
        )
    }
}

@Preview
@Composable
fun NavigationPreview() {
    val groups = listOf(
        Group(0, "\uD83D\uDCBC", "工作"),
        Group(1, "\uD83D\uDCD6", "学习"),
        Group(2, "😊", "娱乐"),
        Group(3, "\uD83E\uDDFA", "杂务")
    )
    val selectedGroup = remember { mutableStateOf(-1) }
    com.github.caoyfcode.todo.ui.theme.TodoTheme {
        Navigation(
            groups = groups,
            selectedGroup = selectedGroup.value,
            onGroupSelected = { }
        ) { openNavigation ->
            Button(onClick = { openNavigation() }) {
                Text(text = "打开")
            }
        }
    }
}