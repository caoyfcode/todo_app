package com.github.caoyfcode.todo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.caoyfcode.todo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen() {
    val groups = listOf(
        Pair("\uD83D\uDD18", "所有待办"),
        Pair("\uD83D\uDCBC", "工作"),
        Pair("\uD83D\uDCD6", "学习"),
        Pair("😊", "娱乐"),
        Pair("\uD83E\uDDFA", "杂务")
    )
    val selectedGroup = remember { mutableStateOf(groups[0].second) }
    Navigation(
        groups = groups,
        selectedGroup = selectedGroup.value,
        onGroupSelected = { selected ->
            selectedGroup.value = selected
        }
    ) { openNavigation ->
        Scaffold(
            modifier = Modifier.padding(10.dp),
            topBar = {
                TopBar(
                    group = selectedGroup.value,
                    onNavigationCLick = openNavigation,
                )
            },
        ) { paddingValues ->
            Content(paddingValues = paddingValues)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    group: String,
    onNavigationCLick: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = group) },
        navigationIcon = {
            IconButton(onClick = onNavigationCLick) {
                Icon(
                    painter = painterResource(id = R.drawable.list),
                    contentDescription = stringResource(id = R.string.all_groups)
                )
            }
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = stringResource(id = R.string.search)
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = stringResource(id = R.string.add_todo)
                )
            }
        }
    )
}

@Composable
fun Content(paddingValues: PaddingValues) {
    val unchecked_messages: List<String> = listOf(
        "Sleep",
        "Go to School",
        "Coding",
    )
    val checked_messages: List<String> = listOf(
        "吃饭",
        "睡觉",
        "打游戏",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.TopCenter
    ) {
        if (unchecked_messages.isEmpty() && checked_messages.isEmpty()) {
            Box (
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = stringResource(id = R.string.no_todo))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight()
                    .padding(vertical = 20.dp), // 前导 20dp
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp), // 没项相隔 20dp
            ) {
                items(unchecked_messages) {
                    TodoItem(message = it, false)
                }
                item {
                    Divider(color = MaterialTheme.colorScheme.secondary)
                }
                items(checked_messages) {
                    TodoItem(message = it, true)
                }
            }
        }
    }
}