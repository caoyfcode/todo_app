package com.github.caoyfcode.todo.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.caoyfcode.todo.R
import com.github.caoyfcode.todo.entity.Todo
import com.github.caoyfcode.todo.model.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(viewModel: TodoViewModel) {
    val groups by viewModel.groups.observeAsState(viewModel.groups.value!!)
    val todos by viewModel.todos.observeAsState(viewModel.todos.value!!)

    var selectedGroup by rememberSaveable { mutableStateOf(-1) }

    val selectedName = if (selectedGroup < 0) {
        stringResource(id = R.string.all_todo_group_name)
    } else {
        groups.find {
            it.uid == selectedGroup
        }!!.name
    }

    val uncheckedTodos: MutableList<Pair<String, Todo>> = mutableListOf()
    val checkedTodos: MutableList<Pair<String, Todo>> = mutableListOf()
    for (todo in todos) {
        if (selectedGroup >= 0 && selectedGroup != todo.groupUid) {
            continue
        }
        val groupIcon = groups.find {
            it.uid == todo.groupUid
        }!!.icon
        if (todo.checked) {
            checkedTodos += Pair(groupIcon, todo)
        } else {
            uncheckedTodos += Pair(groupIcon, todo)
        }
    }
    Navigation(
        groups = groups,
        selectedGroup = selectedGroup,
        onGroupSelected = { selected ->
            selectedGroup = selected
        }
    ) { openNavigation ->
        Scaffold(
            modifier = Modifier.padding(10.dp),
            topBar = {
                TopBar(
                    group = selectedName,
                    onNavigationCLick = openNavigation,
                )
            },
        ) { paddingValues ->
            Content(
                paddingValues = paddingValues,
                checkedTodos = checkedTodos,
                uncheckedTodos = uncheckedTodos,
                onToggleCheckedTodo = { uid ->
                    viewModel.toggleCheckedTodo(uid)
                }
            )
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

/**
 * 主要内容
 * @param paddingValues 由 Scaffold 获得
 * @param checkedTodos a list of (group emoji, todo_item)
 * @param uncheckedTodos same
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Content(
    paddingValues: PaddingValues,
    checkedTodos: List<Pair<String, Todo>>, // group emoji, todo_item
    uncheckedTodos: List<Pair<String, Todo>>, // group emoji, todo_item
    onToggleCheckedTodo: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.TopCenter
    ) {
        if (uncheckedTodos.isEmpty() && checkedTodos.isEmpty()) {
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
                items(uncheckedTodos, key = { it.second.uid }) {
                    TodoItem(
                        modifier = Modifier.animateItemPlacement(),
                        emoji = it.first,
                        subject = it.second.subject,
                        content = it.second.content,
                        checked = false,
                        onToggleChecked = {
                            onToggleCheckedTodo(it.second.uid)
                        }
                    )
                }
                item(key = -1) {
                    Divider(color = MaterialTheme.colorScheme.secondary, modifier = Modifier.animateItemPlacement())
                }
                items(checkedTodos, key = { it.second.uid }) {
                    TodoItem(
                        modifier = Modifier.animateItemPlacement(),
                        emoji = it.first,
                        subject = it.second.subject,
                        content = it.second.content,
                        checked = true,
                        onToggleChecked = {
                            onToggleCheckedTodo(it.second.uid)
                        }
                    )
                }
            }
        }
    }
}