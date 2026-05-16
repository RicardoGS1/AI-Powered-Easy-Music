# AI-Powered Easy Music

Reproductor de música local para Android con interfaz moderna, ecualizador integrado, letras sincronizadas y datos curiosos sobre cada canción generados con IA.

## Características

### Reproducción
- Reproduce archivos de audio del dispositivo mediante **MediaStore** (sin subir música a la nube).
- Servicio en primer plano con **Media3 / ExoPlayer** y sesión de medios para controles del sistema.
- Controles de reproducción: play/pausa, anterior/siguiente, barra de progreso, volumen, **aleatorio** y **repetición** (una o todas).
- Cola de reproducción visible y opción para quitar pistas del reproductor (ocultar de la biblioteca sin borrar el archivo).
- Portadas de álbum con **Coil**.
- Recuerda la última canción reproducida.

### Biblioteca
- Navegación por **canciones**, **álbumes**, **artistas** y **favoritos**.
- Búsqueda unificada por título, álbum o artista.
- Vista de colección por álbum o artista con «Reproducir todo».
- Marcar canciones como favoritas desde el reproductor.

### Ecualizador
- Ecualizador de 5 bandas del sistema Android vinculado a la sesión de audio de ExoPlayer.
- Presets: Plano, Rock, Pop, Jazz, Clásica, Dance, Hip Hop, Electrónica, R&B, Heavy Metal, Acústica, Latino, Vocal, Graves y Agudos.
- Curva personalizada por bandas y **Bass Boost**.

### Letras
- Búsqueda de letras en **[LRCLIB](https://lrclib.net/)** por título, artista, álbum y duración.
- Resolución de coincidencias ambiguas cuando hay varios resultados.
- Soporte para pistas instrumentales.

### Información con IA (Gemini)
- Resumen de la canción, género o estilo, época o contexto, dato curioso y artistas o temas similares.
- La respuesta se genera en el idioma configurado en la app.
- Activación/desactivación remota mediante **Firebase Remote Config** (`enable_ai_insight`).

### Ajustes e idioma
- Selector de idioma de la interfaz (19 idiomas + predeterminado del sistema).
- Menú lateral: compartir app, valorar en Play Store y ajustes.

## Tecnologías

| Área | Stack |
|------|--------|
| Lenguaje | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Arquitectura | Clean Architecture (capas `data`, `domain`, `ui`) |
| Inyección de dependencias | Hilt + KSP |
| Navegación | Navigation Compose |
| Reproducción | Media3 (ExoPlayer + MediaSession) |
| Imágenes | Coil 3 |
| Persistencia local | DataStore Preferences |
| Red | Retrofit, OkHttp, Gson |
| IA | Google Gemini API (REST) |
| Letras | LRCLIB API |
| Backend / analítica | Firebase (Analytics, Crashlytics, Remote Config) |
| Build | Gradle con catálogo de versiones (`libs.versions.toml`) |

### Requisitos del proyecto

- **minSdk:** 24  
- **targetSdk / compileSdk:** 36  
- **Java:** 11  

## Arquitectura

```
app/src/main/java/com/virtualworld/easymusic/
├── data/          # Repositorios, MediaStore, APIs (Gemini, LRCLIB), preferencias
├── domain/        # Modelos, casos de uso, contratos de repositorio
├── ui/            # Pantallas Compose, ViewModels, tema y navegación
├── playback/      # MusicPlaybackService, PlaybackController, EqualizerManager
├── di/            # Módulos Hilt (App, Network, Firebase)
└── firebase/      # Remote Config y arranque de Firebase
```

Los casos de uso encapsulan la lógica de negocio (obtener canciones, favoritos, letras, insight de IA, etc.) y los ViewModels exponen el estado a la UI con `StateFlow`.

## Idiomas soportados

`en`, `es`, `ar`, `bn`, `de`, `fr`, `hi`, `in`, `it`, `ja`, `ko`, `nl`, `pt`, `ru`, `th`, `tr`, `uk`, `vi`, `zh`

## Configuración para desarrollo

### 1. Clonar el repositorio

```bash
git clone https://github.com/<tu-usuario>/EasyMusic.git
cd EasyMusic
```

### 2. Firebase

Coloca el archivo `google-services.json` en `app/` (descárgalo desde la consola de Firebase del proyecto). Sin este archivo, el build de release/debug con los plugins de Google Services fallará.

### 3. Clave de Gemini (opcional, para datos con IA)

Crea o edita `local.properties` en la raíz del proyecto:

```properties
GEMINI_API_KEY=tu_clave_de_google_ai_studio
```

Obtén la clave en [Google AI Studio](https://aistudio.google.com/). Sin ella, el resto de la app funciona; solo la sección de información con IA mostrará un aviso.

### 4. Compilar y ejecutar

Abre el proyecto en **Android Studio** (Ladybug o superior recomendado) y ejecuta la variante `debug` en un dispositivo o emulador con archivos de música locales.

```bash
./gradlew :app:assembleDebug
```

### Permisos

La app solicita acceso a los archivos de audio del dispositivo (`READ_MEDIA_AUDIO` en Android 13+ o `READ_EXTERNAL_STORAGE` en versiones anteriores), notificaciones para el servicio de reproducción y, de forma opcional, `MODIFY_AUDIO_SETTINGS` para el ecualizador en algunos dispositivos.

## Pantallas principales

| Ruta | Descripción |
|------|-------------|
| Reproductor | Pantalla principal con controles, cola, letras e insight de IA |
| Biblioteca | Pestañas de favoritos, canciones, álbumes y artistas |
| Colección | Listado de canciones de un álbum o artista |
| Ecualizador | Presets, bandas y bass boost |
| Ajustes | Idioma de la aplicación |
