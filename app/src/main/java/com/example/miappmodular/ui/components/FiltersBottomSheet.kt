package com.example.miappmodular.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersBottomSheet(
    onDismissRequest: () -> Unit,
    onApplyFilters: () -> Unit,
    minPrice: Double?,
    maxPrice: Double?,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF1A1B25), // Dark background like reference
        contentColor = Color.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.Gray)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Text(
                text = "FILTERS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Price Range Section
            Text(
                text = "Price Range",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Price Values Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$${minPrice?.toInt() ?: 0}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
                Text(
                    text = "$${maxPrice?.toInt() ?: 1000}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }

            // Slider (RangeSlider placeholder - Material3 RangeSlider requires 2 values)
            // Using a simple Slider for demo purposes or standard RangeSlider
            var sliderPosition by remember { mutableStateOf(0f..1000f) }
            
            RangeSlider(
                value = sliderPosition,
                onValueChange = { range -> 
                    sliderPosition = range 
                    onPriceRangeChange(range)
                },
                valueRange = 0f..1000f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFF64B5F6), // Light Blue
                    inactiveTrackColor = Color.DarkGray
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Categories / Type Section
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Chips (Visual Only for this demo based on image)
                CategoryFilterChip(text = "All", selected = true)
                CategoryFilterChip(text = "Fruits", selected = false)
                CategoryFilterChip(text = "Veg", selected = false)
                CategoryFilterChip(text = "Meat", selected = false)
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Apply Button
            Button(
                onClick = onApplyFilters,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF64B5F6) // Light Blue
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Apply",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CategoryFilterChip(text: String, selected: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(if (selected) Color(0xFF64B5F6) else Color(0xFF2A2B36))
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else Color.Gray,
                shape = CircleShape
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else Color.Gray
        )
    }
}
