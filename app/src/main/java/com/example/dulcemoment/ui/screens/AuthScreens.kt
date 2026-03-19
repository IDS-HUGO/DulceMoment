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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // LOGO GRANDE Y HERMOSO
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(16.dp, shape = RoundedCornerShape(28.dp))
                    .background(Color.White, shape = RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_dulce_moment),
                    contentDescription = "DulceMoment Logo",
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Inside
                )
            }

            // TÍTULOS MEJORADÍSIMOS
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "DulceMoment",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF3E2723),
                    fontSize = 36.sp
                )
                Text(
                    "Un sabor que enamora ✨",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF8D6E63),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CARD PRINCIPAL - SUPER ELEGANTE
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(20.dp, shape = RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(36.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
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

            Spacer(modifier = Modifier.height(24.dp))
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
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(16.dp, shape = RoundedCornerShape(28.dp))
                    .background(Color.White, shape = RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_dulce_moment),
                    contentDescription = "DulceMoment Logo",
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Inside
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "DulceMoment",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF3E2723),
                    fontSize = 36.sp
                )
                Text(
                    "Únete a nuestra comunidad",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF8D6E63),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(20.dp, shape = RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(36.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
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

            Spacer(modifier = Modifier.height(24.dp))
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
    var email by rememberSaveable { mutableStateOf("cliente@dulce.com") }
    var password by rememberSaveable { mutableStateOf("123456") }

    val emailError = email.isNotBlank() && !email.contains("@")
    val passwordError = password.isNotBlank() && password.length < 6
    val isFormValid = !emailError && !passwordError && email.isNotBlank() && password.isNotBlank()

    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // HEADERS MEJORADOS
        Text(
            "Bienvenido de vuelta 👋",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF3E2723),
            fontSize = 20.sp
        )

        // EMAIL FIELD - HERMOSO
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("📧 Email") },
            placeholder = { Text("tu@email.com") },
            isError = emailError,
            supportingText = { if (emailError) Text("Email inválido", color = Color(0xFFB3261E)) },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        // PASSWORD FIELD - HERMOSO
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("🔐 Contraseña") },
            placeholder = { Text("Min. 6 caracteres") },
            isError = passwordError,
            supportingText = { if (passwordError) Text("Mínimo 6 caracteres", color = Color(0xFFB3261E)) },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        // LOGIN BUTTON - PREMIUM
        Button(
            onClick = { onLogin(email.trim(), password) },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(10.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3E2723),
                disabledContainerColor = Color(0xFFBCAAA4)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔐", fontSize = 20.sp)
                Text(
                    "INICIAR SESIÓN",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }
        }

        // DIVIDER ELEGANTE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFD7CCC8), thickness = 1.dp)
            Text("o", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8D6E63))
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFD7CCC8), thickness = 1.dp)
        }

        // CREAR CUENTA BUTTON
        OutlinedButton(
            onClick = onGoRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.5.dp, Color(0xFF3E2723)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF3E2723)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✨", fontSize = 20.sp)
                Text(
                    "CREAR CUENTA",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
        }

        // DEMO INFO - SUPER DESTACADO
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
            color = Color(0xFFF4E0DB).copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎯", fontSize = 22.sp)
                    Text(
                        "Cuentas Demo (Prueba gratis)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF3E2723),
                        fontSize = 14.sp
                    )
                }

                Divider(color = Color(0xFF3E2723).copy(alpha = 0.2f), thickness = 1.dp)

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("👤", fontSize = 16.sp)
                        Column {
                            Text(
                                "Cliente",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                            Text(
                                "cliente@dulce.com / 123456",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6D4C41),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🏪", fontSize = 16.sp)
                        Column {
                            Text(
                                "Vendedor",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                            Text(
                                "tienda@dulce.com / 123456",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6D4C41),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
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
    var role by remember { mutableStateOf("customer") }

    val emailError = email.isNotBlank() && !email.contains("@")
    val passwordError = password.isNotBlank() && password.length < 6
    val isFormValid = name.isNotBlank() && !emailError && !passwordError && email.isNotBlank()

    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // HEADER
        Text(
            "Crear tu cuenta",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF3E2723),
            fontSize = 20.sp
        )

        // ROLE SELECTOR - VISUAL
        Text(
            "¿Cuál es tu rol? 🎭",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3E2723)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF4E0DB).copy(alpha = 0.4f))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { role = "customer" },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (role == "customer") Color(0xFF3E2723) else Color.White,
                    contentColor = if (role == "customer") Color.White else Color(0xFF3E2723)
                )
            ) {
                Text("👤 Cliente", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }

            Button(
                onClick = { role = "store" },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (role == "store") Color(0xFF3E2723) else Color.White,
                    contentColor = if (role == "store") Color.White else Color(0xFF3E2723)
                )
            ) {
                Text("🏪 Vendedor", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }

        // NAME FIELD
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("🙋 Nombre completo") },
            placeholder = { Text("Tu nombre") },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        // EMAIL FIELD
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("📧 Email") },
            placeholder = { Text("tu@email.com") },
            isError = emailError,
            supportingText = { if (emailError) Text("Email inválido", color = Color(0xFFB3261E)) },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        // PASSWORD FIELD - HERMOSO
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("🔐 Contraseña") },
            placeholder = { Text("Min. 6 caracteres") },
            isError = passwordError,
            supportingText = { if (passwordError) Text("Mínimo 6 caracteres", color = Color(0xFFB3261E)) },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        // CREAR CUENTA BUTTON
        Button(
            onClick = { onRegister(name.trim(), email.trim(), password, role) },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(10.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3E2723),
                disabledContainerColor = Color(0xFFBCAAA4)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✨", fontSize = 20.sp)
                Text(
                    "CREAR CUENTA",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }
        }

        // DIVIDER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFD7CCC8), thickness = 1.dp)
            Text("o", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8D6E63))
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFD7CCC8), thickness = 1.dp)
        }

        // LOGIN BUTTON
        OutlinedButton(
            onClick = onGoLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.5.dp, Color(0xFF3E2723)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF3E2723)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔐", fontSize = 20.sp)
                Text(
                    "INICIAR SESIÓN",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
