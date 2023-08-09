package com.github.caoyfcode.todo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.github.caoyfcode.todo.R
import com.github.caoyfcode.todo.db.entity.Group
import com.github.caoyfcode.todo.db.entity.Todo


sealed class TodoEditorMode {
    object Add: TodoEditorMode()
    class Modify(val todo: Todo): TodoEditorMode()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditorDialog(
    mode: TodoEditorMode,
    groups: List<Group>,
    onDismiss: () -> Unit,
    onConfirm: (Todo) -> Unit,
) {
    val editTodo = when (mode) {
        is TodoEditorMode.Add -> Todo(groupUid = -1, subject = "")
        is TodoEditorMode.Modify -> mode.todo
    }
    var groupUid by rememberSaveable {
        mutableStateOf(editTodo.groupUid)
    }
    var value by rememberSaveable {
        mutableStateOf(editTodo.subject + if (editTodo.content.isEmpty()) "" else "\n" + editTodo.content)
    }
    var menuExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    val group = groups.find {
        it.uid == groupUid
    } ?: groups[0]
    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.66f),
        onDismissRequest = {},
        icon = {
            Box {
                AssistChip(
                    onClick = {
                        menuExpanded = true
                    },
                    label = {
                        Text(text = group.name)
                    },
                    leadingIcon = {
                        Text(text = group.icon)
                    }
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = {
                        menuExpanded = false
                    }
                ) {
                    for (g in groups) {
                        DropdownMenuItem(
                            onClick = {
                                groupUid = g.uid
                                menuExpanded = false
                            },
                            text = {
                                Text(text = g.name)
                            },
                            leadingIcon = {
                                Text(text = g.icon)
                            }
                        )
                    }
                }
            }

        },
        text = {
            OutlinedTextField(
                modifier = Modifier.fillMaxHeight(),
                value = value,
                onValueChange = { value = it },
                label = { Text(text = stringResource(id = R.string.todo_content)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None, // None 是因为需要回车换行
                )
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var subject = ""
                    var content = ""
                    var firstLine = true
                    value.split("\n").forEach {
                        if (firstLine) {
                            subject += it
                            firstLine = false
                        } else {
                            content += it
                            content += "\n"
                        }
                    }
                    val todo = editTodo.copy(
                        groupUid = group.uid,
                        subject = subject.trim(),
                        content = content.trim()
                    )
                    onConfirm(todo)
            }) {
                Text(
                    text = when (mode) {
                        is TodoEditorMode.Add -> stringResource(id = R.string.add)
                        is TodoEditorMode.Modify -> stringResource(id = R.string.modify)
                    }
                )
            }
        },
    )
}