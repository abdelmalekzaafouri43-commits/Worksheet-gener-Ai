package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.data.Worksheet
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.launch

import com.example.viewmodel.AppThemeStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isDarkMode by viewModel.isDarkMode.collectAsState()
    var showThemeMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Worksheet AI") },
                actions = {
                    IconButton(onClick = { viewModel.toggleDarkMode() }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.Nightlight,
                            contentDescription = "Toggle Night Mode"
                        )
                    }
                    IconButton(onClick = { showThemeMenu = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Themes")
                    }
                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false }
                    ) {
                        AppThemeStyle.values().forEach { style ->
                            DropdownMenuItem(
                                text = { Text(style.name.replace("_", " ")) },
                                onClick = {
                                    viewModel.setAppTheme(style)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Row(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                Spacer(Modifier.weight(1f))
                val items = listOf(
                    Triple("dashboard", "Home", Icons.Default.Home),
                    Triple("generator", "Create", Icons.Default.AddCircle),
                    Triple("tutor", "AI Tutor", Icons.Default.Chat),
                    Triple("premium", "Premium", Icons.Default.WorkspacePremium)
                )
                items.forEach { (route, label, icon) ->
                    NavigationRailItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentRoute == route || (currentRoute == "viewer" && route == "generator"),
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.weight(1f)
            ) {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToGenerator = { navController.navigate("generator") },
                    onNavigateToPremium = { navController.navigate("premium") },
                    onWorksheetSelected = { worksheet ->
                        viewModel.viewWorksheet(worksheet)
                        navController.navigate("viewer")
                    }
                )
            }
            composable("generator") {
                GeneratorScreen(
                    viewModel = viewModel,
                    onWorksheetGenerated = { navController.navigate("viewer") },
                    onNavigateToPremium = { navController.navigate("premium") }
                )
            }
            composable("viewer") {
                WorksheetViewerScreen(
                    viewModel = viewModel,
                    onNavigateToPremium = { navController.navigate("premium") }
                )
            }
            composable("tutor") {
                AITutorScreen(viewModel = viewModel)
            }
            composable("premium") {
                PremiumScreen(viewModel = viewModel)
            }
        }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToGenerator: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onWorksheetSelected: (Worksheet) -> Unit
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val tokens by viewModel.tokens.collectAsState()
    val recentWorksheets by viewModel.recentWorksheets.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Welcome Back, Scholar!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (!isPremium) {
            item {
                PremiumGlassCard(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToPremium() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Free Plan (Tap to Upgrade)", fontWeight = FontWeight.Bold)
                            Text("Tokens remaining: $tokens", style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.Default.WorkspacePremium, contentDescription = "Premium", tint = Color(0xFFEAB308), modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        item {
            Button(
                onClick = onNavigateToGenerator,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate New Worksheet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text(
                text = "Recent Worksheets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (recentWorksheets.isEmpty()) {
            item {
                Text(
                    "No worksheets yet. Create one to get started!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(recentWorksheets) { worksheet ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onWorksheetSelected(worksheet) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = "Worksheet", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(worksheet.title, fontWeight = FontWeight.Bold)
                            Text(worksheet.difficulty, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    viewModel: AppViewModel,
    onWorksheetGenerated: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    var selectedDifficulty by remember { mutableStateOf("Elementary") }
    val difficulties = listOf("Elementary", "Middle School", "High School")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create Worksheet", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Text("Select Difficulty:", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            difficulties.forEach { diff ->
                FilterChip(
                    selected = selectedDifficulty == diff,
                    onClick = { selectedDifficulty = diff },
                    label = { Text(diff) }
                )
            }
        }

        PremiumGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Premium Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (!isPremium) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(16.dp))
                    }
                }
                Text("Answer Keys, Custom Branding, and Difficulty Scaling.", style = MaterialTheme.typography.bodySmall)
                if (!isPremium) {
                    OutlinedButton(onClick = onNavigateToPremium) {
                        Text("Unlock with Premium")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isGenerating) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Generating with AI...")
            }
        } else {
            Button(
                onClick = {
                    viewModel.generateWorksheet(selectedDifficulty, "Grammar Practice", "General")
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Generate Worksheet")
            }
        }
    }
    
    var isWaitingForGeneration by remember { mutableStateOf(false) }

    if (isGenerating) {
        isWaitingForGeneration = true
    }

    LaunchedEffect(isGenerating) {
        if (!isGenerating && isWaitingForGeneration && viewModel.currentWorksheet.value != null) {
            isWaitingForGeneration = false
            onWorksheetGenerated()
        }
    }
}

@Composable
fun WorksheetViewerScreen(
    viewModel: AppViewModel,
    onNavigateToPremium: () -> Unit
) {
    val worksheet by viewModel.currentWorksheet.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    var showAnswerKey by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (worksheet == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No worksheet selected.")
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Sending '${worksheet!!.title}' to printer...")
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Print, contentDescription = "Print PDF")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(worksheet!!.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(worksheet!!.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            HorizontalDivider()
            
            // A4 Paper Representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f / 1.414f)
                        .shadow(8.dp, RoundedCornerShape(2.dp)),
                    shape = RoundedCornerShape(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = worksheet!!.title,
                            style = MaterialTheme.typography.titleLarge.copy(color = Color.Black),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        worksheet!!.illustrationRes?.let { resId ->
                            Spacer(modifier = Modifier.height(16.dp))
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = resId),
                                contentDescription = "Worksheet Illustration",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = worksheet!!.content,
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.DarkGray, lineHeight = 24.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Answer Key (Premium)", fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = showAnswerKey,
                    onCheckedChange = { 
                        if (isPremium) {
                            showAnswerKey = it
                        } else {
                            onNavigateToPremium()
                        }
                    }
                )
            }

            if (showAnswerKey && isPremium) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Answer Key", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(worksheet!!.answerKey)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AITutorScreen(viewModel: AppViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isTutorTyping by viewModel.isTutorTyping.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isTutorTyping) {
        try {
            if (isTutorTyping) {
                listState.animateScrollToItem(messages.size)
            } else if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        } catch (e: Exception) {
            // Ignore scroll out of bounds errors if layout hasn't caught up
        }
    }

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = false,
                onClick = { viewModel.sendChatMessage("Explain Present Perfect") },
                label = { Text("Present Perfect") }
            )
            FilterChip(
                selected = false,
                onClick = { viewModel.sendChatMessage("Give me an idiom example") },
                label = { Text("Idiom Example") }
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val alignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                val bgColor = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val textColor = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                val shape = if (msg.isUser) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
                    if (!msg.isUser) {
                        PremiumGlassBubble(shape = shape) {
                            Text(
                                text = msg.text,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .widthIn(max = 340.dp)
                            )
                        }
                    } else {
                        Text(
                            text = msg.text,
                            color = textColor,
                            modifier = Modifier
                                .background(bgColor, shape)
                                .padding(12.dp)
                                .widthIn(max = 340.dp)
                        )
                    }
                }
            }
            if (isTutorTyping) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        PremiumGlassBubble(shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)) {
                            TypingIndicator()
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask your tutor...") },
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    viewModel.sendChatMessage(inputText)
                    inputText = ""
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
fun PremiumScreen(viewModel: AppViewModel) {
    val isPremium by viewModel.isPremium.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Worksheet AI Premium",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Unlock your full teaching potential.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        PremiumGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureRow("Unlimited AI generation")
                FeatureRow("Answer Keys unlocked")
                FeatureRow("Advanced Reading Comprehension texts")
                FeatureRow("Ad-free experience")
                FeatureRow("Custom PDF styling")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (isPremium) {
            Text(
                "You are a Premium User!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { viewModel.setPremium(false) }) {
                Text("Revert to Free Plan (Mock)")
            }
        } else {
            Button(
                onClick = { viewModel.setPremium(true) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Upgrade Now - $9.99/mo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FeatureRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, contentDescription = "Included", tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun PremiumGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "premium_border")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_translation"
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            Color(0xFFEAB308), // Gold/Amber
            MaterialTheme.colorScheme.primary
        ),
        start = Offset(0f, translateAnim),
        end = Offset(translateAnim, 0f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, borderBrush, RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun PremiumGlassBubble(
    shape: androidx.compose.ui.graphics.Shape,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "premium_bubble")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_translation"
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.primary,
            Color(0xFFEAB308),
            MaterialTheme.colorScheme.secondary
        ),
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 500f, translateAnim + 500f)
    )

    Box(
        modifier = Modifier
            .clip(shape)
            .border(1.5.dp, borderBrush, shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dotCount = 3
    val alphas = List(dotCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, delayMillis = index * 200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_alpha_$index"
        ).value
    }
    
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        alphas.forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
            )
        }
    }
}
