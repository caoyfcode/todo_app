package com.github.caoyfcode.todo.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.caoyfcode.todo.R
import com.github.caoyfcode.todo.db.entity.Group

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GroupsEditorDialog(
    groups: List<Group>,
    onDismiss: () -> Unit,
    onModifyGroup: (Group) -> Unit,
    onDeleteGroup: (Group) -> Unit,
    onAddGroup: (Group) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_back),
                                contentDescription = stringResource(id = R.string.groups_editor_back)
                            )
                        }
                    },
                    title = { 
                        Text(text = stringResource(id = R.string.groups_editor_title))
                    },
                    actions = {
                        IconButton(onClick = {
                            onAddGroup(Group(icon = groupIcons[0], name = ""))
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.add),
                                contentDescription = stringResource(id = R.string.add)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 10.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(groups, key = { it.uid }) {
                        GroupItem(
                            group = it,
                            onModify = onModifyGroup,
                            onDeleteClicked = { onDeleteGroup(it) }
                        )
                    }
                }
            }
        }
    }
}

val groupIcons = listOf(
    "\uD83D\uDCBC", "\uD83D\uDCD6", "\uD83D\uDE0A", "\uD83E\uDDFA",
    "\uD83D\uDC5F", "\uD83D\uDD14", "\uD83C\uDFB5", "\uD83C\uDFA7",
    "\uD83C\uDFBB", "\uD83D\uDCDE", "\uD83D\uDCBB", "\uD83D\uDCFD️",
    "\uD83D\uDD0D", "\uD83D\uDCA1", "\uD83D\uDD6F️", "\uD83C\uDFF7️",
    "\uD83D\uDCB0", "✉️", "\uD83D\uDCC1", "\uD83D\uDD11",
    "\uD83D\uDD27", "\uD83E\uDDEA", "\uD83D\uDD2D", "\uD83D\uDC8A",
    "\uD83D\uDED2",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun GroupItem(
    group: Group,
    onModify: (Group) -> Unit,
    onDeleteClicked: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current // 用来关闭键盘
    val focusManager = LocalFocusManager.current // 用来释放焦点

    var iconPickerExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    
    OutlinedTextField(
        singleLine = true,
        leadingIcon = {
            IconButton(onClick = {
                iconPickerExpanded = true
            }) {
                Text(text = group.icon)
            }
            GroupIconPicker(
                expanded = iconPickerExpanded,
                onDismiss = { iconPickerExpanded = false },
                onIconClick = {
                    iconPickerExpanded = false
                    if (it != group.icon) {
                        onModify(group.copy(icon = it))
                    }
                },
                icons = groupIcons,
            )
        },
        trailingIcon = {
            IconButton(onClick = onDeleteClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.cross_small),
                    contentDescription = null
                )
            }
        },
        shape = RoundedCornerShape(100),
        value = group.name,
        onValueChange = {
            onModify(group.copy(name = it))
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide() // 关闭软键盘
                focusManager.clearFocus() // 释放焦点
            }
        )
    )
}

@Composable
fun GroupIconPicker(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onIconClick: (icon: String) -> Unit,
    icons: List<String>
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.3f)
    ) {
        for (icon in icons) {
            Text(
                text = icon,
                modifier = Modifier
                    .padding(
                        vertical = 10.dp,
                        horizontal = 20.dp
                    )
                    .clickable(
                        onClick = { onIconClick(icon) }
                    )
            )
        }
    }

}