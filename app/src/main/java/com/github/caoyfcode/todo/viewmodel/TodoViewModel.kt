package com.github.caoyfcode.todo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.github.caoyfcode.todo.entity.Group
import com.github.caoyfcode.todo.entity.Todo
import com.github.caoyfcode.todo.ui.EditorMode

class TodoViewModel: ViewModel() {
    private val _groups: MutableStateFlow<List<Group>> = MutableStateFlow(
        listOf(
            Group(0, "\uD83D\uDCBC", "工作"),
            Group(1, "\uD83D\uDCD6", "学习"),
            Group(2, "😊", "娱乐"),
            Group(3, "\uD83E\uDDFA", "杂务")
        )
    )
    private val _todos: MutableStateFlow<List<Todo>> = MutableStateFlow(
        listOf(
            Todo(0, 3, "打扫", "1.地板\n2.窗户"),
            Todo(1, 1, "读代码", "1.kotlin\n2.android"),
            Todo(2, 1, "学习视频", "打开bilibili学习"),
            Todo(3, 0, "领钱", "白日做梦"),
            Todo(4, 3, "发呆"),
            Todo(5, 2, "打游戏"),
            Todo(6, 0, "写代码"),
            Todo(7, 1, "读书", "", true),
        )
    )
    private var _selectedGroupUid: MutableStateFlow<Int> = MutableStateFlow(-1)
    private var _editorMode: MutableStateFlow<EditorMode?> = MutableStateFlow(null)
    val groups: StateFlow<List<Group>>
        get() = _groups
    val filteredTodos: StateFlow<List<Todo>> = combine(_todos, _selectedGroupUid) {
        all, selected ->
        all.filter { selected < 0 || it.groupUid == selected }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())
    val selectedGroupUid: StateFlow<Int>
        get() = _selectedGroupUid
    val editorMode: StateFlow<EditorMode?>
        get() = _editorMode

    // < 0 代表选择全部
    fun selectGroup(uid: Int) {
        _selectedGroupUid.value = uid
    }

    fun setEditorMode(mode: EditorMode?) {
        _editorMode.value = mode
    }

    fun toggleCheckedTodo(uid: Int) {
        _todos.value = _todos.value.map {
            if (it.uid == uid) {
                it.copy(checked = !it.checked)
            } else {
                it
            }
        }
    }

    fun addTodo(todo: Todo) {
        var biggestUid = -1
        _todos.value.map {
            if (it.uid > biggestUid) {
                biggestUid = it.uid
            }
        }
        _todos.value = _todos.value.plus(
            todo.copy(
                uid = biggestUid + 1
            )
        )
    }

    fun deleteTodo(uid: Int) {
        _todos.value = _todos.value.filter { it.uid != uid }
    }

    fun modifyTodo(modified: Todo) {
        _todos.value = _todos.value.map {
            if (it.uid == modified.uid) {
                modified
            } else {
                it
            }
        }
    }

    fun addGroup(group: Group) {
        var biggestUid = -1
        _groups.value.map {
            if (it.uid > biggestUid) {
                biggestUid = it.uid
            }
        }
        _groups.value = _groups.value.plus(
            group.copy(uid = biggestUid + 1)
        )
    }

    fun deleteGroup(uid: Int) {
        _groups.value = _groups.value.filter { it.uid != uid }
        _todos.value = _todos.value.filter { it.groupUid != uid }
    }

    fun modifyGroup(modified: Group) {
        _groups.value = _groups.value.map {
            if (it.uid == modified.uid) {
                modified
            } else {
                it
            }
        }
    }
}