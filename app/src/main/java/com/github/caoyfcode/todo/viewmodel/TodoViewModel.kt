package com.github.caoyfcode.todo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.caoyfcode.todo.db.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.github.caoyfcode.todo.db.entity.Group
import com.github.caoyfcode.todo.db.entity.Todo
import com.github.caoyfcode.todo.ui.TodoEditorMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class TodoViewModel(private val database: AppDatabase): ViewModel() {

    // 所有的待办组
    val groups: StateFlow<List<Group>> = database
        .groupDao()
        .getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())
    // 左侧导航栏中选中的组 id, 选择全部则为 -1
    private var _selectedGroupUid: MutableStateFlow<Int> = MutableStateFlow(-1)
    val selectedGroupUid: StateFlow<Int>
        get() = _selectedGroupUid
    // 主页面显示的待办列表
    val filteredTodos: StateFlow<List<Todo>> = combine(
        database.todoDao().getAll(),
        _selectedGroupUid
    ) {
        all, selected ->
        all.filter { selected < 0 || it.groupUid == selected }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())
    // 待办编辑对话框状态: 未打开(null), 添加, 修改
    private var _todoEditorMode: MutableStateFlow<TodoEditorMode?> = MutableStateFlow(null)
    val todoEditorMode: StateFlow<TodoEditorMode?>
        get() = _todoEditorMode
    // 添加待办时若无组则显示的警告对话框的打开与否
    private var _groupsEmptyAlertShown: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val groupsEmptyAlertShown: StateFlow<Boolean> = _groupsEmptyAlertShown
    // 组编辑对话框的打开与否
    private var _groupsEditorShown: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val groupsEditorShown: StateFlow<Boolean> = _groupsEditorShown

    // < 0 代表选择全部
    fun setSelectedGroupUid(uid: Int) {
        _selectedGroupUid.value = uid
    }

    fun setTodoEditorMode(mode: TodoEditorMode?) {
        _todoEditorMode.value = mode
    }

    fun setGroupsEmptyAlertShown(shown: Boolean) {
        _groupsEmptyAlertShown.value = shown
    }

    fun setGroupsEditorShown(shown: Boolean) {
        _groupsEditorShown.value = shown
    }

    fun addTodo(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            database.todoDao().insert(todo)
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            database.todoDao().delete(todo)
        }
    }

    fun modifyTodo(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            database.todoDao().update(todo)
        }
    }

    fun toggleCheckedTodo(todo: Todo) {
        modifyTodo(todo.copy(
            checked = !todo.checked,
            checkTime = LocalDateTime.now()
        ))
    }

    fun addGroup(group: Group) {
        viewModelScope.launch(Dispatchers.IO) {
            database.groupDao().insert(group)
        }
    }

    fun deleteEmptyGroup(group: Group) {
        viewModelScope.launch(Dispatchers.IO) {
            val deletedLines = database.groupDao().deleteIfEmpty(group.uid)
            if (deletedLines > 0 && selectedGroupUid.value == group.uid) {
                setSelectedGroupUid(-1)
            }
        }
    }

    fun modifyGroup(modified: Group) {
        viewModelScope.launch(Dispatchers.IO) {
            database.groupDao().update(modified)
        }
    }
}