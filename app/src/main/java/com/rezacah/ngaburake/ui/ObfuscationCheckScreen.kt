package com.rezacah.ngaburake.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.rezacah.ngaburake.presentation.SampleViewModelFactory
import com.rezacah.ngaburake.presentation.UiEvent
import com.rezacah.ngaburake.presentation.UiState
import com.rezacah.ngaburake.report.ReportFormat
import com.rezacah.ngaburake.ui.components.AddClassForm
import com.rezacah.ngaburake.ui.components.EmptyStateHint
import com.rezacah.ngaburake.ui.components.ErrorStatus
import com.rezacah.ngaburake.ui.components.FindingsList
import com.rezacah.ngaburake.ui.components.FormatSelector
import com.rezacah.ngaburake.ui.components.LoadingRow
import com.rezacah.ngaburake.ui.components.ReportPreviewSection
import com.rezacah.ngaburake.ui.components.ResultSummary
import com.rezacah.ngaburake.ui.components.ScreenHeader
import com.rezacah.ngaburake.ui.components.SectionTitle
import com.rezacah.ngaburake.ui.components.SensitiveClassList
import com.rezacah.ngaburake.ui.components.VerificationLoadingIndicator
import com.rezacah.ngaburake.ui.components.VerificationOptions
import com.rezacah.ngaburake.ui.components.rememberCopyAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObfuscationCheckScreen() {
    val context = LocalContext.current.applicationContext as android.app.Application
    val viewModel: com.rezacah.ngaburake.presentation.ObfuscationCheckViewModel =
        viewModel(factory = SampleViewModelFactory.create(context))

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sensitivePackages by viewModel.sensitivePackages.collectAsStateWithLifecycle()
    val useMappingFile by viewModel.useMappingFile.collectAsStateWithLifecycle()
    val customKeywords by viewModel.customKeywords.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val copyToClipboard = rememberCopyAction()
    val scope = rememberCoroutineScope()
    var newClass by rememberSaveable { mutableStateOf("") }
    var selectedFormat by rememberSaveable { mutableStateOf(ReportFormat.CONSOLE) }
    val successResult = (uiState as? UiState.Success)?.result

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "Obfuscation Verify",
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.verify() },
                        enabled = uiState !is UiState.Loading,
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Re-run verification")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ScreenHeader() }

            item { SectionTitle("Sensitive classes", topPadding = true) }
            item {
                SensitiveClassList(
                    classes = sensitivePackages,
                    onRemove = viewModel::removeSensitivePackage,
                )
            }
            item {
                AddClassForm(
                    value = newClass,
                    onValueChange = { newClass = it },
                    onAdd = {
                        viewModel.addSensitivePackage(newClass)
                        newClass = ""
                    },
                )
            }

            item { HorizontalDivider() }

            item { SectionTitle("Options", topPadding = true) }
            item {
                VerificationOptions(
                    useMappingFile = useMappingFile,
                    onToggleMappingFile = viewModel::setUseMappingFile,
                    customKeywords = customKeywords,
                    onToggleCustomKeywords = viewModel::setCustomKeywords,
                )
            }
            item {
                Button(
                    onClick = { viewModel.verify() },
                    enabled = uiState !is UiState.Loading && sensitivePackages.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text("Run verification", modifier = Modifier.padding(start = 8.dp))
                }
            }

            if (uiState is UiState.Loading) {
                item { VerificationLoadingIndicator() }
            }

            item {
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                ) { state ->
                    when (state) {
                        is UiState.Idle -> EmptyStateHint()
                        is UiState.Loading -> LoadingRow()
                        is UiState.Error -> ErrorStatus(state.message)
                        is UiState.Success -> ResultSummary(state.result)
                    }
                }
            }

            if (successResult != null) {
                item { HorizontalDivider() }

                item { SectionTitle("Findings", topPadding = true) }
                item {
                    FindingsList(result = successResult)
                }

                item { HorizontalDivider() }

                item { SectionTitle("Report", topPadding = true) }
                item {
                    FormatSelector(selected = selectedFormat, onSelect = { selectedFormat = it })
                }
                item {
                    val reportText = viewModel.generateReportText(successResult, selectedFormat)
                    ReportPreviewSection(
                        format = selectedFormat,
                        reportText = reportText,
                        onCopy = { text ->
                            copyToClipboard(text)
                            scope.launch { snackbarHostState.showSnackbar("Report copied") }
                        },
                    )
                }
            }
        }
    }
}
