package com.github.caoyfcode.todo.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.caoyfcode.todo.R
import com.github.caoyfcode.todo.db.entity.Group
import com.github.caoyfcode.todo.db.entity.Todo
import com.github.caoyfcode.todo.viewmodel.TodoViewModel
import java.io.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(viewModel: TodoViewModel) {
    val groups by viewModel.groups.collectAsState()
    val todos by viewModel.filteredTodos.collectAsState()
    val selectedGroup by viewModel.selectedGroupUid.collectAsState()
    val todoEditorMode by viewModel.todoEditorMode.collectAsState()
    val groupsEmptyAlertShown by viewModel.groupsEmptyAlertShown.collectAsState()
    val groupsEditorShown by viewModel.groupsEditorShown.collectAsState()

    val navigationGroups: List<Group> = listOf(
        Group(
            uid = -1,
            icon = stringResource(id = R.string.all_todo_group_icon),
            name = stringResource(id = R.string.all_todo_group_name)
        )
    ) + groups // 左侧导航第一个组为所有待办
    val selectedName = navigationGroups.find {
        it.uid == selectedGroup
    }!!.name
    val uncheckedTodos: MutableList<Pair<String, Todo>> = mutableListOf()
    val checkedTodos: MutableList<Pair<String, Todo>> = mutableListOf()
    for (todo in todos) {
        val groupIcon = groups.find {
            it.uid == todo.groupUid
        }!!.icon
        if (todo.checked) {
            checkedTodos += Pair(groupIcon, todo)
        } else {
            uncheckedTodos += Pair(groupIcon, todo)
        }
    }
    uncheckedTodos.sortByDescending {
        it.second.createTime
    }
    checkedTodos.sortByDescending {
        it.second.checkTime
    }
    Navigation(
        groups = navigationGroups,
        selectedGroup = selectedGroup,
        onGroupSelected = { selected ->
            viewModel.setSelectedGroupUid(selected)
        },
        onGroupsEditorRequest = {
            viewModel.setGroupsEditorShown(true)
        }
    ) { openNavigation ->
        Scaffold(
            modifier = Modifier.padding(10.dp),
            topBar = {
                TopBar(
                    group = selectedName,
                    onNavigationClick = openNavigation,
                    onAddClick = {
                        if (groups.isEmpty()) {
                            viewModel.setGroupsEmptyAlertShown(true)
                        } else {
                            viewModel.setTodoEditorMode(TodoEditorMode.Add)
                        }
                    }
                )
            },
        ) { paddingValues ->
            Content(
                paddingValues = paddingValues,
                checkedTodos = checkedTodos,
                uncheckedTodos = uncheckedTodos,
                onToggleCheckedTodo = { todo ->
                    viewModel.toggleCheckedTodo(todo)
                },
                onEditTodo = {
                    viewModel.setTodoEditorMode(TodoEditorMode.Modify(todos.find { todo ->  todo.uid == it }!!))
                },
                onDeleteTodo = {
                    viewModel.deleteTodo(it)
                }
            )
        }
    }

    // dialogs
    val todoEditorDialogMode = todoEditorMode
    if (groupsEmptyAlertShown) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(text = stringResource(id = R.string.warn_no_group_title))
            },
            text = {
                Text(text = stringResource(id = R.string.warn_no_group_text))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setGroupsEmptyAlertShown(false)
                    viewModel.setGroupsEditorShown(true)
                }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            }
        )
    } else if (todoEditorDialogMode != null) {
        TodoEditorDialog(
            mode = todoEditorDialogMode,
            groups = groups,
            onDismiss = { viewModel.setTodoEditorMode(null) },
            onConfirm = {
                when (todoEditorDialogMode) {
                    is TodoEditorMode.Add -> viewModel.addTodo(it)
                    is TodoEditorMode.Modify -> viewModel.modifyTodo(it)
                }
                viewModel.setTodoEditorMode(null)
            }
        )
    } else if (groupsEditorShown) {
        GroupsEditorDialog(
            groups = groups,
            onDismiss = { viewModel.setGroupsEditorShown(false) },
            onModifyGroup = { viewModel.modifyGroup(it) },
            onDeleteGroup = { viewModel.deleteEmptyGroup(it) },
            onAddGroup = { viewModel.addGroup(it) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    group: String,
    onNavigationClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = group) },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
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
            IconButton(onClick = onAddClick) {
                Icon(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = stringResource(id = R.string.add_todo)
                )
            }
        }
    )
}

data class TodoItemsKey(val uid: Int, val checked: Boolean): Serializable

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
    onToggleCheckedTodo: (todo: Todo) -> Unit,
    onDeleteTodo: (todo: Todo) -> Unit,
    onEditTodo: (Int) -> Unit,
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
                items(uncheckedTodos, key = { TodoItemsKey(it.second.uid, it.second.checked) }) {
                    TodoItem(
                        modifier = Modifier.animateItemPlacement(),
                        emoji = it.first,
                        subject = it.second.subject,
                        content = it.second.content,
                        checked = false,
                        onRightOrIconClick = { onToggleCheckedTodo(it.second) },
                        onEditClick = { onEditTodo(it.second.uid) },
                        onDeleteClick = { onDeleteTodo(it.second) },
                    )
                }
                item(key = -1) {
                    Divider(color = MaterialTheme.colorScheme.secondary, modifier = Modifier.animateItemPlacement())
                }
                items(checkedTodos, key = { TodoItemsKey(it.second.uid, it.second.checked) }) {
                    TodoItem(
                        modifier = Modifier.animateItemPlacement(),
                        emoji = it.first,
                        subject = it.second.subject,
                        content = it.second.content,
                        checked = true,
                        onRightOrIconClick = { onToggleCheckedTodo(it.second) },
                        onEditClick = { onEditTodo(it.second.uid) },
                        onDeleteClick = { onDeleteTodo(it.second) },
                    )
                }
            }
        }
    }
}
