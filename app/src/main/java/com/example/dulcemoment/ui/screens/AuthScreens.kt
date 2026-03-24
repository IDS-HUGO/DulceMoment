package com.example.dulcemoment.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dulcemoment.R

// ==================== LOGIN SCREEN ====================

@Composable
fun LoginGlassScreen(
    onLogin: (String, String) -> Unit,
    onGoRegister: () -> Unit,
) {
    // GRADIENTE HERMOSO - Paleta profesional
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFDFBF5),          // Crema claro
            Color(0xFFF4E0DB).copy(alpha = 0.9f), // Pastel
            Color(0xFFFEF5E7)           // Crema cálido
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // LOGO GRANDE Y HERMOSO
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(16.dp, shape = RoundedCornerShape(28.dp))
                    .background(Color.White, shape = RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_dulce_moment),
                    contentDescription = "DulceMoment Logo",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Inside
                )
            }

            // TÍTULOS MEJORADÍSIMOS
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "DulceMoment",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF3E2723),
                    fontSize = 28.sp
                )
                Text(
                    "Un sabor que enamora ✨",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8D6E63),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            // CARD PRINCIPAL - SUPER ELEGANTE
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(20.dp, shape = RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedContent(
                        targetState = true,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                        label = "auth_login_transition",
                    ) {
                        LoginContent(onLogin = onLogin, onGoRegister = onGoRegister)
                    }
                }
            }
        }
    }
}

// ==================== REGISTER SCREEN ====================

@Composable
fun RegisterGlassScreen(
    onRegister: (String, String, String, String) -> Unit,
    onGoLogin: () -> Unit,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFDFBF5),
            Color(0xFFF4E0DB).copy(alpha = 0.9f),
            Color(0xFFFEF5E7)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(16.dp, shape = RoundedCornerShape(28.dp))
                    .background(Color.White, shape = RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_dulce_moment),
                    contentDescription = "DulceMoment Logo",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Inside
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "DulceMoment",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF3E2723),
                    fontSize = 28.sp
                )
                Text(
                    "Únete a nuestra comunidad",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8D6E63),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(20.dp, shape = RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedContent(
                        targetState = true,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                        label = "auth_register_transition",
                    ) {
                        RegisterContent(onRegister = onRegister, onGoLogin = onGoLogin)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==================== LOGIN CONTENT ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginContent(
    onLogin: (String, String) -> Unit,
    onGoRegister: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val emailError = email.isNotBlank() && !email.contains("@")
    val passwordError = password.isNotBlank() && password.length < 6
    val isFormValid = !emailError && !passwordError && email.isNotBlank() && password.isNotBlank()

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // HEADER
        Text(
            "Bienvenido de vuelta",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF3E2723),
            fontSize = 18.sp
        )

        // EMAIL FIELD - DISEÑO MEJORADO
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", fontSize = 12.sp) },
            placeholder = { Text("tu@email.com", fontSize = 13.sp) },
            isError = emailError,
            supportingText = { if (emailError) Text("Email inválido", color = Color(0xFFB3261E), fontSize = 10.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF0E6E1),
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color(0xFFE0D5CF),
                focusedBorderColor = Color(0xFF3E2723),
                cursorColor = Color(0xFF3E2723),
                focusedTextColor = Color(0xFF3E2723),
                unfocusedTextColor = Color(0xFF3E2723),
                focusedLabelColor = Color(0xFF3E2723),
                unfocusedLabelColor = Color(0xFF8D6E63),
                focusedPlaceholderColor = Color(0xFF8D6E63),
                unfocusedPlaceholderColor = Color(0xFF8D6E63)
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )

        // PASSWORD FIELD - CON TOGGLE DE VISIBILIDAD
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña", fontSize = 12.sp) },
            placeholder = { Text("Min. 6 caracteres", fontSize = 13.sp) },
            isError = passwordError,
            supportingText = { if (passwordError) Text("Mínimo 6 caracteres", color = Color(0xFFB3261E), fontSize = 10.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF0E6E1),
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color(0xFFE0D5CF),
                focusedBorderColor = Color(0xFF3E2723),
                cursorColor = Color(0xFF3E2723),
                focusedTextColor = Color(0xFF3E2723),
                unfocusedTextColor = Color(0xFF3E2723),
                focusedLabelColor = Color(0xFF3E2723),
                unfocusedLabelColor = Color(0xFF8D6E63),
                focusedPlaceholderColor = Color(0xFF8D6E63),
                unfocusedPlaceholderColor = Color(0xFF8D6E63)
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = Color(0xFF8D6E63)
                    )
                }
            }
        )

        // LOGIN BUTTON - ELEGANTE CON SOMBRA
        Button(
            onClick = { onLogin(email.trim(), password) },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(8.dp, shape = RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3E2723),
                disabledContainerColor = Color(0xFFBCAAA4)
            )
        ) {
            Text(
                "INICIAR SESIÓN",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }

        // DIVIDER ELEGANTE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD7CCC8), thickness = 1.dp)
            Text("o", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9D8B85), fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD7CCC8), thickness = 1.dp)
        }

        // CREAR CUENTA BUTTON - DISEÑO MEJORADO
        Button(
            onClick = onGoRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(6.dp, shape = RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF4E0DB)
            )
        ) {
            Text(
                "CREAR CUENTA",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF3E2723),
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ==================== REGISTER CONTENT ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterContent(
    onRegister: (String, String, String, String) -> Unit,
    onGoLogin: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf("customer") }

    val emailError = email.isNotBlank() && !email.contains("@")
    val passwordError = password.isNotBlank() && password.length < 6
    val isFormValid = name.isNotBlank() && !emailError && !passwordError && email.isNotBlank()

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // HEADER
        Text(
            "Crear tu cuenta",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF3E2723),
            fontSize = 18.sp
        )

        // ROLE SELECTOR - VISUAL MEJORADO
        Text(
            "¿Cuál es tu rol?",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF3E2723),
            fontSize = 12.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0E6E1))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { role = "customer" },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (role == "customer") Color(0xFF3E2723) else Color.Transparent,
                    contentColor = if (role == "customer") Color.White else Color(0xFF3E2723)
                )
            ) {
                Text("Cliente", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
                onClick = { role = "store" },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (role == "store") Color(0xFF3E2723) else Color.Transparent,
                    contentColor = if (role == "store") Color.White else Color(0xFF3E2723)
                )
            ) {
                Text("Vendedor", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // NAME FIELD - DISEÑO MEJORADO
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre completo", fontSize = 12.sp) },
            placeholder = { Text("Ej: María García", fontSize = 13.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF0E6E1),
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color(0xFFE0D5CF),
                focusedBorderColor = Color(0xFF3E2723),
                cursorColor = Color(0xFF3E2723),
                focusedTextColor = Color(0xFF3E2723),
                unfocusedTextColor = Color(0xFF3E2723),
                focusedLabelColor = Color(0xFF3E2723),
                unfocusedLabelColor = Color(0xFF8D6E63),
                focusedPlaceholderColor = Color(0xFF8D6E63),
                unfocusedPlaceholderColor = Color(0xFF8D6E63)
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )

        // EMAIL FIELD - DISEÑO MEJORADO
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", fontSize = 12.sp) },
            placeholder = { Text("tu@email.com", fontSize = 13.sp) },
            isError = emailError,
            supportingText = { if (emailError) Text("Email inválido", color = Color(0xFFB3261E), fontSize = 10.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF0E6E1),
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color(0xFFE0D5CF),
                focusedBorderColor = Color(0xFF3E2723),
                cursorColor = Color(0xFF3E2723),
                focusedTextColor = Color(0xFF3E2723),
                unfocusedTextColor = Color(0xFF3E2723),
                focusedLabelColor = Color(0xFF3E2723),
                unfocusedLabelColor = Color(0xFF8D6E63),
                focusedPlaceholderColor = Color(0xFF8D6E63),
                unfocusedPlaceholderColor = Color(0xFF8D6E63)
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )

        // PASSWORD FIELD - CON TOGGLE DE VISIBILIDAD
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña", fontSize = 12.sp) },
            placeholder = { Text("Min. 6 caracteres", fontSize = 13.sp) },
            isError = passwordError,
            supportingText = { if (passwordError) Text("Mínimo 6 caracteres", color = Color(0xFFB3261E), fontSize = 10.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF0E6E1),
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color(0xFFE0D5CF),
                focusedBorderColor = Color(0xFF3E2723),
                cursorColor = Color(0xFF3E2723),
                focusedTextColor = Color(0xFF3E2723),
                unfocusedTextColor = Color(0xFF3E2723),
                focusedLabelColor = Color(0xFF3E2723),
                unfocusedLabelColor = Color(0xFF8D6E63),
                focusedPlaceholderColor = Color(0xFF8D6E63),
                unfocusedPlaceholderColor = Color(0xFF8D6E63)
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = Color(0xFF8D6E63)
                    )
                }
            }
        )

        // CREAR CUENTA BUTTON - ELEGANTE
        Button(
            onClick = { onRegister(name.trim(), email.trim(), password, role) },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(8.dp, shape = RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3E2723),
                disabledContainerColor = Color(0xFFBCAAA4)
            )
        ) {
            Text(
                "CREAR CUENTA",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }

        // DIVIDER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD7CCC8), thickness = 1.dp)
            Text("o", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9D8B85), fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD7CCC8), thickness = 1.dp)
        }

        // LOGIN BUTTON - DISEÑO MEJORADO
        Button(
            onClick = onGoLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(6.dp, shape = RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF4E0DB)
            )
        ) {
            Text(
                "INICIAR SESIÓN",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF3E2723),
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
