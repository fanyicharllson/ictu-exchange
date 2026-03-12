package com.fanyiadrien.ictu_ex.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    auth: FirebaseAuth, // Injected via Hilt in your NavGraph or Activity
    onAddListingClick: () -> Unit = {}
) {
    // Access user from the Hilt-provided FirebaseAuth instance
    val userName = auth.currentUser?.displayName ?: "ICTU Student"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ICTU-Ex Marketplace", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* Search logic */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddListingClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Post Item", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // Personalised Header
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Welcome back,", color = Color.Gray)
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Recently Added",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(6) {
                    ProductItemCard()
                }
            }
        }
    }
}

@Composable
fun ProductItemCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            AsyncImage(
                model = "https://via.placeholder.com",
                contentDescription = "Listing Image",
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Textbook", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("2,500 XAF", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
