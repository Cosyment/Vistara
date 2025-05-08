package com.vistara.aestheticwalls.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

/**
 * 文本输入对话框
 * 用于需要用户输入文本的场景，如登录、注册等
 *
 * @param onDismiss 取消回调
 * @param onConfirm 确认回调，传入用户输入的文本
 * @param title 对话框标题
 * @param label 输入框标签
 * @param placeholder 输入框占位文本
 * @param initialValue 初始值
 * @param confirmText 确认按钮文本
 * @param dismissText 取消按钮文本
 * @param isPassword 是否为密码输入
 * @param isLoading 是否正在加载中
 * @param keyboardType 键盘类型
 * @param properties 对话框属性
 */
@Composable
fun TextInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    title: String,
    label: String,
    placeholder: String = "",
    initialValue: String = "",
    confirmText: String,
    dismissText: String,
    isPassword: Boolean = false,
    isLoading: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    properties: DialogProperties = DialogProperties()
) {
    var inputText by remember { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            ) 
        },
        text = { 
            Column {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text(label) },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (inputText.isNotEmpty()) {
                                onConfirm(inputText)
                            }
                        }
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    focusManager.clearFocus()
                    onConfirm(inputText) 
                },
                enabled = !isLoading && inputText.isNotEmpty(),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { 
                    focusManager.clearFocus()
                    onDismiss() 
                },
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
