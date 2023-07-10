package com.github.caoyfcode.todo.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.caoyfcode.todo.R
import com.github.caoyfcode.todo.entity.Group
import com.github.caoyfcode.todo.entity.Todo
import com.github.caoyfcode.todo.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(viewModel: TodoViewModel) {
    val groups by viewModel.groups.collectAsState()
    val todos by viewModel.filteredTodos.collectAsState()
    val selectedGroup by viewModel.selectedGroupUid.collectAsState()
    val editorMode by viewModel.editorMode.collectAsState()

    var groupsEditorEnabled by rememberSaveable {
        mutableStateOf(false)
    }

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
    Navigation(
        groups = navigationGroups,
        selectedGroup = selectedGroup,
        onGroupSelected = { selected ->
            viewModel.selectGroup(selected)
        },
        onGroupsEditorRequest = {
            groupsEditorEnabled = true
        }
    ) { openNavigation ->
        Scaffold(
            modifier = Modifier.padding(10.dp),
            topBar = {
                TopBar(
                    group = selectedName,
                    onNavigationClick = openNavigation,
                    onAddClick = {
                        viewModel.setEditorMode(EditorMode.Add)
                    }
                )
            },
        ) { paddingValues ->
            Content(
                paddingValues = paddingValues,
                checkedTodos = checkedTodos,
                uncheckedTodos = uncheckedTodos,
                onToggleCheckedTodo = { uid ->
                    viewModel.toggleCheckedTodo(uid)
                },
                onEditTodo = {
                    viewModel.setEditorMode(EditorMode.Modify(todos.find { todo ->  todo.uid == it }!!))
                },
                onDeleteTodo = {
                    viewModel.deleteTodo(it)
                }
            )
        }
    }

    val mode = editorMode
    if (mode != null) {
        TodoEditorDialog(
            mode = mode,
            groups = groups,
            onDismiss = { viewModel.setEditorMode(null) },
            onConfirm = {
                when (mode) {
                    is EditorMode.Add -> viewModel.addTodo(it)
                    is EditorMode.Modify -> viewModel.modifyTodo(it)
                }
                viewModel.setEditorMode(null)
            }
        )
    } else if (groupsEditorEnabled) {
        GroupsEditorDialog(
            groups = groups.filter { it.uid >= 0 },
            onDismiss = { groupsEditorEnabled = false },
            onModifyGroup = { viewModel.modifyGroup(it) },
            onDeleteGroup = { viewModel.deleteGroup(it) }
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
    onDeleteTodo: (Int) -> Unit,
    onEditTodo: (Int) -> Unit,
) {
    // 经观察, animateItemPlacement 的原理为重组时找到原来相同 key 的位置, 放置在此处，之后向目标移动
    // 因而，可以仅让 toggle 后的 item 进行 scale in (淡入), 其余不 scale in
    val shouldScaleInIds = remember {
        mutableSetOf<Int>()
    }
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
                        scaleIn = shouldScaleInIds.containsAndRemove(it.second.uid),
                        onToggleChecked = {
                            shouldScaleInIds.add(it.second.uid)
                            onToggleCheckedTodo(it.second.uid)
                        },
                        onEditClicked = { onEditTodo(it.second.uid) },
                        onDeleteClicked = { onDeleteTodo(it.second.uid) },
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
                        scaleIn = shouldScaleInIds.containsAndRemove(it.second.uid),
                        onToggleChecked = {
                            shouldScaleInIds.add(it.second.uid)
                            onToggleCheckedTodo(it.second.uid)
                        },
                        onEditClicked = { onEditTodo(it.second.uid) },
                        onDeleteClicked = { onDeleteTodo(it.second.uid) },
                    )
                }
            }
        }
    }
}

fun <T> MutableSet<T>.containsAndRemove(element: T): Boolean {
    val contains = this.contains(element)
    this.remove(element)
    return contains
}