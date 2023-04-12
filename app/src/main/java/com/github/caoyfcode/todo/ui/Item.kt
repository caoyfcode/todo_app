package com.github.caoyfcode.todo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.caoyfcode.todo.R

@Composable
fun TodoItem(message: String, checked: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.inverseSurface)
    ) {
        Row(
            modifier = Modifier.padding(10.dp), // 10dp 水平: 与子项间隔相同, 竖直: 保持最小高度为 20dp + content size
            horizontalArrangement = Arrangement.spacedBy(10.dp), // 子项水平相隔 10dp
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (checked) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.check),
                        contentDescription = stringResource(id = R.string.checked_todo)
                    )
                }
            } else {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.bookmark),
                        contentDescription = stringResource(id = R.string.unchecked_todo)
                    )
                }
            }
            Text(text = message)
        }
    }
}