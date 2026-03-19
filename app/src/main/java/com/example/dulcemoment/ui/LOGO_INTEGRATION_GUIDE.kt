package com.example.dulcemoment.ui.theme

/**
 * GUÍA COMPLETA PARA AGREGAR TU LOGO A DULCEMOMENT
 * 
 * ============================================
 * PASO 1: PREPARAR TU LOGO
 * ============================================
 * 
 * Formatos soportados:
 * - SVG (RECOMENDADO) - Se escala perfecto en cualquier resolución
 * - PNG con fondo transparente - 192x192 píxeles mínimo
 * - WebP - Formato moderno y ligero
 * 
 * Requisitos:
 * ✓ Fondo TRANSPARENTE (no debe tener fondo blanco o de color)
 * ✓ Proportions: Cuadrado (1:1) es lo mejor
 * ✓ Tamaño: Si es PNG, mínimo 192x192 píxeles
 * ✓ Nombre del archivo: sin espacios, en minúsculas
 *   Ejemplos: ic_logo.svg, ic_logo_white.svg, ic_dulcemoment.png
 * 
 * ============================================
 * PASO 2: SUBIR TU LOGO AL PROYECTO
 * ============================================
 * 
 * Ubicación exacta:
 * d:\UNIVERSIDAD\DulceMoment\app\src\main\res\drawable\
 * 
 * Archivos a crear:
 * 1. ic_logo.svg (o .png) - Logo NORMAL (para fondo claro)
 * 2. ic_logo_white.svg (o .png) - Logo BLANCO (para fondo oscuro/gradientes)
 * 3. ic_logo_app.png - Icono de la app (192x192 recomendado)
 * 
 * Pasos:
 * 1. Abre el explorador de archivos
 * 2. Ve a: d:\UNIVERSIDAD\DulceMoment\app\src\main\res\drawable\
 * 3. Copia tu logo SVG o PNG ahí
 * 4. En Visual Studio Code, recarga el proyecto
 * 
 * ============================================
 * PASO 3: USAR TU LOGO EN EL CÓDIGO
 * ============================================
 * 
 * Opción A: Usando Image() para PNG
 * 
 *   Image(
 *       painter = painterResource(id = R.drawable.ic_logo),
 *       contentDescription = "DulceMoment Logo",
 *       modifier = Modifier.size(80.dp)
 *   )
 * 
 * Opción B: Usando AsyncImage para URL
 * 
 *   AsyncImage(
 *       model = "https://tu-cdn.com/logo.png",
 *       contentDescription = "DulceMoment Logo",
 *       modifier = Modifier.size(80.dp),
 *       contentScale = ContentScale.Fit
 *   )
 * 
 * Opción C: RECOMENDADA - Usar Icon con drawable vector
 * 
 *   Icon(
 *       painter = painterResource(id = R.drawable.ic_logo),
 *       contentDescription = "Logo",
 *       modifier = Modifier.size(80.dp),
 *       tint = MaterialTheme.colorScheme.secondary
 *   )
 * 
 * ============================================
 * PASO 4: REEMPLAZAR EN COMPONENTES
 * ============================================
 * 
 * En el archivo: ui/components/ProfessionalComponents.kt
 * 
 * Función: LogoHeader()
 * 
 * ACTUAL (con emoji):
 *     Box(
 *         modifier = Modifier
 *             .size(80.dp)
 *             .background(...)
 *             .border(...)
 *     ) {
 *         Text("🧁", fontSize = 40.sp)  // <-- EMOJI PLACEHOLDER
 *     }
 * 
 * FUTURO (con tu logo):
 *     Image(
 *         painter = painterResource(id = R.drawable.ic_logo),
 *         contentDescription = "DulceMoment",
 *         modifier = Modifier
 *             .size(80.dp)
 *             .clip(RoundedCornerShape(16.dp))
 *     )
 * 
 * ============================================
 * PASO 5: PROBAR EN EL EMULADOR
 * ============================================
 * 
 * 1. Compile el proyecto: ./gradlew :app:build
 * 2. Ejecuta en emulador/dispositivo
 * 3. Verás tu logo en:
 *    - Pantalla de Login
 *    - Pantalla de Register
 *    - Dashboard del usuario
 * 
 * ============================================
 * FORMATOS RECOMENDADOS COMPARACIÓN
 * ============================================
 * 
 * SVG:
 * ✓ Escala a cualquier tamaño sin perder calidad
 * ✓ Archivo pequeño (KB)
 * ✓ Soporta animaciones y colores dinámicos
 * ✗ Requiere parseo
 * 
 * PNG:
 * ✓ Compatible con todos los dispositivos
 * ✓ Fácil de integrar
 * ✓ Soporta transparencia
 * ✗ Se ve pixelado si se escala mucho
 * ✗ Archivo más grande
 * 
 * WebP:
 * ✓ Mejor compresión que PNG
 * ✓ Soporta transparencia
 * ✗ Menos compatible con dispositivos antiguos
 * 
 * ============================================
 * COLORIZACIÓN DINÁMICA DEL LOGO
 * ============================================
 * 
 * Si tu logo es un vector (SVG), puedes cambiar el color dinámicamente:
 * 
 *   Icon(
 *       painter = painterResource(id = R.drawable.ic_logo),
 *       contentDescription = "Logo",
 *       tint = MaterialTheme.colorScheme.secondary,
 *       modifier = Modifier.size(80.dp)
 *   )
 * 
 * Colores disponibles por paleta:
 * - PRIMARY: MaterialTheme.colorScheme.primary
 * - SECONDARY: MaterialTheme.colorScheme.secondary (Chocolate)
 * - TERTIARY: MaterialTheme.colorScheme.tertiary (Pastel)
 * - ERROR: MaterialTheme.colorScheme.error
 * 
 * ============================================
 * CHECKLIST FINAL
 * ============================================
 * 
 * ☐ Logo preparado en formato SVG o PNG
 * ☐ Logo con fondo transparente
 * ☐ Archivo copiado a drawable/
 * ☐ Código actualizado para usar painterResource()
 * ☐ Proyecto compilado sin errores
 * ☐ Logo visible en emulador/dispositivo
 * ☐ Logo se ve bien en todas las pantallas
 * 
 * ============================================
 * EJEMPLOS DE CÓDIGO COMPLETO
 * ============================================
 * 
 * VERSIÓN SIMPLE:
 * 
 *   @Composable
 *   fun LogoHeaderConTuLogo() {
 *       Image(
 *           painter = painterResource(id = R.drawable.ic_logo),
 *           contentDescription = "DulceMoment",
 *           modifier = Modifier
 *               .size(100.dp)
 *               .clip(RoundedCornerShape(16.dp)),
 *           contentScale = ContentScale.Crop
 *       )
 *   }
 * 
 * VERSIÓN PROFESIONAL CON SOMBRA:
 * 
 *   Card(
 *       modifier = Modifier
 *           .size(100.dp)
 *           .shadow(8.dp, RoundedCornerShape(16.dp)),
 *       shape = RoundedCornerShape(16.dp)
 *   ) {
 *       Image(
 *           painter = painterResource(id = R.drawable.ic_logo),
 *           contentDescription = "DulceMoment",
 *           modifier = Modifier.fillMaxSize(),
 *           contentScale = ContentScale.Inside
 *       )
 *   }
 * 
 * VERSIÓN CON ANIMACIÓN:
 * 
 *   var isHovered by remember { mutableStateOf(false) }
 *   
 *   Image(
 *       painter = painterResource(id = R.drawable.ic_logo),
 *       contentDescription = "DulceMoment",
 *       modifier = Modifier
 *           .size(
 *               animateDpAsState(
 *                   targetValue = if (isHovered) 110.dp else 100.dp
 *               ).value
 *           )
 *           .pointerHoverIcon(PointerIcon.Hand)
 *           .onPointerEvent(PointerEventType.Enter) { isHovered = true }
 *           .onPointerEvent(PointerEventType.Exit) { isHovered = false }
 *   )
 * 
 * ============================================
 * SOPORTE PARA MÚLTIPLES DENSIDADES (AVANZADO)
 * ============================================
 * 
 * Para PNG en múltiples densidades:
 * 
 * drawable-mdpi/ic_logo.png (96x96)
 * drawable-hdpi/ic_logo.png (144x144)
 * drawable-xhdpi/ic_logo.png (192x192)
 * drawable-xxhdpi/ic_logo.png (288x288)
 * drawable-xxxhdpi/ic_logo.png (384x384)
 * 
 * Con SVG solo necesitas: drawable/ic_logo.svg
 * SVG se escala automáticamente a todas las densidades
 * 
 * ============================================
 * PREGUNTAS FRECUENTES
 * ============================================
 * 
 * P: ¿Mi logo se verá borroso en dispositivos de alta resolución?
 * R: Con SVG NO. Con PNG sí si es muy pequeño. Usa SVG o PNG grande.
 * 
 * P: ¿Necesito muchas copias del logo en diferentes tamaños?
 * R: Con SVG NO. Con PNG sí (mdpi, hdpi, xhdpi, etc). SVG es mejor.
 * 
 * P: ¿Puedo usar JPG?
 * R: NO RECOMENDADO. JPG no soporta transparencia. Usa PNG o SVG.
 * 
 * P: ¿Cómo animo el logo?
 * R: Con SVG puedes. Con Compose puedes animar el size/rotation.
 * 
 * P: ¿El logo se coloreará automáticamente con el tema?
 * R: Solo si es SVG/Vector y configuras tint= en Icon/Image.
 * 
 * ============================================
 */

// Este archivo es solo DOCUMENTACIÓN
// NO contiene código ejecutable
// Es una guía de referencia para integrar tu logo

// ARCHIVO REEMPLAZADO - Ver documentación arriba
