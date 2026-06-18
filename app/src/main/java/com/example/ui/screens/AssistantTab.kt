package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun AssistantTab(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()

    var chatInputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("assistant_tab_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Chat Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Redline, modifier = Modifier.size(22.dp))
                    Text(
                        text = "AI Study Assistant",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnDark
                    )
                }
                Text(
                    text = "Personal academic companion driven by Gemini 3.5.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnDarkVariant
                )
            }

            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Chat", tint = OnDarkVariant)
            }
        }

        Divider(color = OutlineDark, thickness = 1.dp)

        // Chat Bubble Scroll Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDarker)
                .border(1.dp, OutlineDark, RoundedCornerShape(12.dp))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(chatMessages) { (text, isUserScroll) ->
                    ChatBubbleRow(text = text, isUser = isUserScroll)
                }

                if (isChatLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = Redline,
                                strokeWidth = 1.5.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Advisor is formulating study advice...",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnDarkVariant
                            )
                        }
                    }
                }
            }
        }

        // Suggestions Chips Row
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "SUGGESTED DISCUSSIONS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Redline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf(
                    "Explain merge sort" to "Explain Merge Sort in simple terms",
                    "How to stop slacking" to "Suggest a focus strategy for extreme procrastination",
                    "Math derivatives list" to "List core derivatives formulas for my quiz today"
                )

                suggestions.forEach { (label, actionPrompt) ->
                    Box(
                        modifier = Modifier
                            .background(SurfaceDark, RoundedCornerShape(6.dp))
                            .border(1.dp, OutlineDark, RoundedCornerShape(6.dp))
                            .clickable {
                                chatInputText = ""
                                viewModel.sendChatMessage(actionPrompt)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OnDark)
                    }
                }
            }
        }

        // Send Command Text bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInputText,
                onValueChange = { chatInputText = it },
                placeholder = { Text("Ask anything... (e.g. explain Dijkstra's algorithm)") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_textfield"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Redline,
                    unfocusedBorderColor = OutlineDark,
                    focusedTextColor = OnDark,
                    unfocusedTextColor = OnDarkVariant
                ),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (chatInputText.isNotBlank()) {
                        viewModel.sendChatMessage(chatInputText)
                        chatInputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Redline)
                    .testTag("send_chat_button"),
                colors = IconButtonDefaults.iconButtonColors(contentColor = SurfaceBlack)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ChatBubbleRow(text: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomStart = if (isUser) 8.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 8.dp
                    )
                )
                .background(if (isUser) Redline.copy(alpha = 0.15f) else SurfaceDark)
                .border(
                    1.dp,
                    if (isUser) Redline.copy(alpha = 0.5f) else OutlineDark,
                    RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomStart = if (isUser) 8.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 8.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = OnDark
            )
        }
    }
}
