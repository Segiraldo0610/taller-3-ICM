# Taller 3 — Firebase: En Ruta

Aplicación Android mono-usuario para registro, autenticación y visualización de usuarios conectados sobre un mapa en tiempo real.

## Funcionalidades

- Registro e ingreso con Firebase Authentication.
- Perfil con nombre, identificación, correo, teléfono y fotografía tomada con cámara.
- Fotografías en Firebase Storage y perfiles/ubicaciones en Cloud Firestore.
- Switch Conectado/No conectado que inicia o detiene Fused Location.
- Consulta en tiempo real de máximo 100 usuarios conectados.
- Marcadores personalizados con foto o inicial; no se usan marcadores predeterminados.
- Polyline del recorrido propio y de cada usuario en línea.
- Eliminación de marcador y polyline al desconectarse.
- Jetpack Compose, Material 3, Single Activity, ViewModel, StateFlow y Navigation 3 1.1.6.
- Tema claro y oscuro.

## Estructura principal

- `auth/`: estado, ViewModel y pantalla de registro/ingreso.
- `data/`: Firebase Auth, Firestore, Storage y Fused Location.
- `tracking/`: estado y lógica del seguimiento.
- `map/`: mapa, capas, marcadores y polylines.
- `navigation/`: rutas y `NavDisplay` de Navigation 3.
- `ui/theme/`: esquema fijo claro/oscuro.

## Puesta en marcha

Siga [CONFIGURACION_FIREBASE.md](CONFIGURACION_FIREBASE.md), sincronice Gradle y ejecute `app` en un teléfono o emulador con Google Play Services.

## Verificación

```text
gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

