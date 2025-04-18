package com.vistara.aestheticwalls.ui.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import com.vistara.aestheticwalls.ui.icons.AppIcons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.ui.theme.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 关于页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackPressed: () -> Unit,
    navController: NavController = rememberNavController(),
    viewModel: AboutViewModel = hiltViewModel()
) {
    val appVersion by viewModel.appVersion.collectAsState()
    val openSourceLibraries by viewModel.openSourceLibraries.collectAsState()

    // 设置导航控制器
    viewModel.setNavController(navController)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.about_credits)) }, navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)
                    )
                }
            })
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 应用信息
            AppInfoSection(
                appVersion = appVersion,
                onPrivacyPolicyClick = { viewModel.openPrivacyPolicy() },
                onTermsOfServiceClick = { viewModel.openTermsOfService() },
                onUserAgreementClick = { viewModel.openUserAgreement() })

            Spacer(modifier = Modifier.height(24.dp))

            // 开源库
            OpenSourceSection(
                libraries = openSourceLibraries, onLibraryClick = { viewModel.openLibraryUrl(it) })
        }
    }
}

/**
 * 应用信息部分
 */
@Composable
private fun AppInfoSection(
    appVersion: String, onPrivacyPolicyClick: () -> Unit, onTermsOfServiceClick: () -> Unit, onUserAgreementClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
    ) {
        // 应用图标
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher), contentDescription = stringResource(R.string.app_name), modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 应用名称
        Text(
            text = stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 应用版本
        Text(
            text = appVersion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 应用描述
        Text(
            text = stringResource(R.string.app_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinkItem(
            title = stringResource(R.string.user_agreement), onClick = onUserAgreementClick
        )

        // 链接
        LinkItem(
            title = stringResource(R.string.privacy_policy), onClick = onPrivacyPolicyClick
        )

        LinkItem(
            title = stringResource(R.string.terms_of_service), onClick = onTermsOfServiceClick
        )
    }
}

/**
 * 开源库部分
 */
@Composable
private fun OpenSourceSection(
    libraries: List<Library>, onLibraryClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.open_source_libraries), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                libraries.forEachIndexed { index, library ->
                    LibraryItem(
                        library = library, onClick = { onLibraryClick(library.url) })

                    if (index < libraries.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = DividerDefaults.Thickness,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 链接项
 */
@Composable
private fun LinkItem(
    title: String, onClick: () -> Unit
) {
    Surface(
        onClick = onClick, modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = AppIcons.Info, contentDescription = null,tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 开源库项
 */
@Composable
private fun LibraryItem(
    library: Library, onClick: () -> Unit
) {
    Surface(
        onClick = onClick, modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)) {
            Text(
                text = library.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = library.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    VistaraTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // 注意：预览中不会显示真实数据，因为没有提供真实的ViewModel
            // 这里只是UI预览
            AboutScreen(
                onBackPressed = {},
                navController = rememberNavController()
            )
        }
    }
}
