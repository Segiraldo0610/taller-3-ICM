# Configuración de Firebase y Google Maps

El código compila sin credenciales y muestra una pantalla de configuración. Para ejecutar todas las funciones se requieren los siguientes pasos una sola vez.

## 1. Firebase

1. Ingrese a <https://console.firebase.google.com/> y cree un proyecto llamado, por ejemplo, `taller3-firebase-samuel`.
2. Agregue una aplicación Android con el paquete exacto `com.example.taller3firebase`.
3. Descargue `google-services.json` y cópielo en `app/google-services.json`.
4. En **Authentication > Sign-in method**, habilite **Correo electrónico/contraseña**.
5. En **Firestore Database**, cree la base de datos. Para una demostración puede elegir la región más cercana disponible.
6. En **Storage**, cree el bucket predeterminado.
7. Publique el contenido de `firestore.rules` en **Firestore Database > Rules**.
8. Publique el contenido de `storage.rules` en **Storage > Rules**.

También se pueden desplegar las reglas mediante Firebase CLI desde la raíz:

```text
firebase login
firebase use --add
firebase deploy --only firestore:rules,storage
```

## 2. Google Maps

1. Abra Google Cloud Console para el mismo proyecto.
2. Habilite **Maps SDK for Android**.
3. Cree una API key y restrínjala a aplicaciones Android.
4. Restrinja el paquete a `com.example.taller3firebase` y agregue la huella SHA-1 de depuración.
5. Copie `secrets.properties.example` como `secrets.properties` y escriba:

```text
MAPS_API_KEY=SU_API_KEY_REAL
```

No suba `google-services.json` ni `secrets.properties` a un repositorio público; ambos están incluidos en `.gitignore`.

## 3. Prueba con dos usuarios

1. Instale el APK en dos emuladores o teléfonos con Google Play Services.
2. Registre una cuenta diferente en cada dispositivo y tome una fotografía de perfil.
3. Active el switch en ambos dispositivos y permita ubicación precisa.
4. Cambie la ubicación de cada emulador desde **Extended controls > Location** o camine con los dispositivos.
5. Confirme que ambos marcadores, fotos y polylines se actualizan en tiempo real.
6. Apague un switch y confirme que ese marcador y su polyline desaparecen en el otro dispositivo.

