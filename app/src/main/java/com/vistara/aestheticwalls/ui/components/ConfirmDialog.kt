package com.vistara.aestheticwalls.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

/**
 * 通用确认对话框
 * 用于各种需要用户确认的场景
 *
 * @param onDismiss 取消回调
 * @param onConfirm 确认回调
 * @param title 对话框标题
 * @param message 对话框消息
 * @param confirmText 确认按钮文本
 * @param dismissText 取消按钮文本
 * @param isLoading 是否正在加载中
 * @param properties 对话框属性
 */
@Composable
fun ConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    isLoading: Boolean = false,
    properties: DialogProperties = DialogProperties()
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            ) 
        },
        text = { 
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(dismissText)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        properties = properties,
        modifier = Modifier.fillMaxWidth()
    )
}
