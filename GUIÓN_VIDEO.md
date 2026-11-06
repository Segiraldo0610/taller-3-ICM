# Guion sugerido — máximo 6 minutos

## 0:00–0:35 — Presentación

- Mostrar brevemente el enunciado y explicar que la app usa Kotlin, Jetpack Compose, Single Activity, ViewModel y Navigation 3.
- Mostrar `MainActivity.kt` y señalar que no contiene ningún composable.

## 0:35–1:35 — Registro y autenticación

- Abrir `AuthScreen.kt`, enseñar el formulario y la solicitud de cámara.
- Registrar un usuario: nombre, identificación, correo, contraseña, teléfono y foto.
- Mostrar `AuthViewModel.kt`, `FirebaseAuthRepository` y `FirebaseProfileRepository`.
- En Firebase Console, mostrar el usuario en Authentication, su documento en Firestore y la foto en Storage.

## 1:35–2:20 — Navigation 3 y arquitectura

- Mostrar `Routes.kt` con `AuthRoute` y `MapRoute` serializables.
- Mostrar `AppNavigation.kt`, `rememberNavBackStack`, `NavDisplay` y el cambio automático según la sesión.
- Señalar StateFlow privado/público en los ViewModels.

## 2:20–3:35 — Ubicación propia

- Ingresar y activar el switch; aceptar ubicación precisa.
- Mostrar el marcador personalizado con la foto y mover la ubicación.
- Mostrar cómo crece la polyline.
- Enseñar `FusedLocationTracker` y `FirebaseTrackingRepository.updateLocation`.

## 3:35–4:45 — Otro usuario en tiempo real

- Abrir el segundo dispositivo con otra cuenta y activar su switch.
- Mover su ubicación y comprobar en el primer dispositivo que cambian marcador y polyline sin recargar.
- Mostrar la consulta `whereEqualTo("connected", true).limit(100)` y el snapshot listener.

## 4:45–5:20 — Desconexión

- Apagar el switch del segundo usuario.
- Confirmar que el marcador y la ruta desaparecen del primero.
- Mostrar `stopTracking`: `connected=false` y `route=[]`.

## 5:20–5:55 — Requisitos finales

- Cambiar el teléfono entre tema claro y oscuro.
- Mostrar `strings.xml`, permisos del manifest y reglas de Firestore/Storage.
- Cerrar sesión y regresar a autenticación.

## 5:55–6:00 — Cierre

- Mostrar el APK generado y finalizar antes de superar seis minutos.

