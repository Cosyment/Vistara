package com.vistara.aestheticwalls.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.vistara.aestheticwalls.ui.theme.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 错误状态组件
 * 显示错误消息和重试按钮
 *
 * @param message 错误消息
 * @param onRetry 重试操作回调
 * @param modifier 可选的修饰符
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 错误图标（如果有）
        /* Icon(
            painter = painterResource(id = R.drawable.ic_error),
            contentDescription = "Error",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        ) */

        Spacer(modifier = Modifier.height(16.dp))

        // 错误消息
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 重试按钮
        Button(
            onClick = onRetry
        ) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorStatePreview() {
    VistaraTheme {
        Surface {
            ErrorState(
                message = stringResource(R.string.error_network_check),
                onRetry = {}
            )
        }
    }
}
