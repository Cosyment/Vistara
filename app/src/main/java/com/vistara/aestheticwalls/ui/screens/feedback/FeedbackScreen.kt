package com.vistara.aestheticwalls.ui.screens.feedback

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vistara.aestheticwalls.ui.theme.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 评分与反馈页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onBackPressed: () -> Unit, viewModel: FeedbackViewModel = hiltViewModel()
) {
    val feedbackText by viewModel.feedbackText.collectAsState()
    val contactInfo by viewModel.contactInfo.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submitResult by viewModel.submitResult.collectAsState()
    val shouldNavigateBack by viewModel.shouldNavigateBack.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // 显示提交结果
    LaunchedEffect(submitResult) {
        submitResult?.let {
            when (it) {
                is SubmitResult.Success -> {
                    snackbarHostState.showSnackbar(it.message)
                    viewModel.clearSubmitResult()
                }

                is SubmitResult.Error -> {
                    snackbarHostState.showSnackbar(it.message)
                    viewModel.clearSubmitResult()
                }
            }
        }
    }

    // 当提交成功后返回上级页面
    LaunchedEffect(shouldNavigateBack) {
        if (shouldNavigateBack) {
            // 重置导航状态
            viewModel.resetNavigationState()
            // 返回上级页面
            onBackPressed()
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.rate_feedback)) }, navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        })
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 应用商店评分卡片
            RatingCard(
                onClick = { viewModel.openAppRating() })

            Spacer(modifier = Modifier.height(16.dp))

            // 邮件反馈卡片
            EmailFeedbackCard(
                onClick = { viewModel.sendEmailFeedback() })

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.direct_feedback),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = feedbackText,
                onValueChange = { viewModel.updateFeedbackText(it) },
                label = { Text(stringResource(R.string.your_feedback_or_suggestion)) },
                placeholder = { Text(stringResource(R.string.enter_feedback_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contactInfo,
                onValueChange = { viewModel.updateContactInfo(it) },
                label = { Text(stringResource(R.string.contact_info_optional)) },
                placeholder = { Text(stringResource(R.string.email_or_other_contact)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.submitFeedback() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting && feedbackText.isNotBlank()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.submit_feedback))
                }
            }
        }
    }
}

/**
 * 应用商店评分卡片
 */
@Composable
private fun RatingCard(
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.rate_in_app_store),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.like_our_app_rate_us),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * 邮件反馈卡片
 */
@Composable
private fun EmailFeedbackCard(
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.send_email_feedback),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.have_questions_email_us),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedbackScreenPreview() {
    VistaraTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // 注意：预览中不会显示真实数据，因为没有提供真实的ViewModel
            // 这里只是UI预览
            FeedbackScreen(onBackPressed = {})
        }
    }
}
