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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.github.caoyfcode.todo.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun Navigation(
    groups: List<Pair<String, String>>, // // (emoji icon, name)
    selectedGroup: String,
    onGroupSelected: (selected: String) -> Unit,
    content: @Composable (openNavigation: () -> Unit) -> Unit
) {
    val windowWidthSizeClass = calculateWindowSizeClass(activity = LocalView.current.context as Activity).widthSizeClass
    if (windowWidthSizeClass == WindowWidthSizeClass.Compact) {
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        ModalNavigationDrawer(
            drawerState = drawerState,
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
    groups: List<Pair<String, String>>, // (emoji icon, name)
    selectedGroup: String,
    onGroupSelected: (String) -> Unit,
) {
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
    LazyColumn {
        items(groups) {
            NavigationDrawerItem(
                icon = {
                    Text(text = it.first)
                },
                label = {
                    Text(text = it.second)
                },
                selected = it.second == selectedGroup,
                onClick = {
                    onGroupSelected(it.second)
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
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = stringResource(id = R.string.add_group)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun NavigationPreview() {
    val groups = listOf(
        Pair("\uD83D\uDD18", "所有待办"),
        Pair("\uD83D\uDCBC", "工作"),
        Pair("\uD83D\uDCD6", "学习"),
        Pair("😊", "娱乐"),
        Pair("\uD83E\uDDFA", "杂务")
    )
    val selectedGroup = remember { mutableStateOf(groups[0].second) }
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