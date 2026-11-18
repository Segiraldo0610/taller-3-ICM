# Configuración de Firebase, Cloudinary y OpenStreetMap

El código compila sin credenciales y muestra una pantalla de configuración. Para ejecutar todas las funciones se requieren los siguientes pasos una sola vez.

## 1. Firebase

1. Ingrese a <https://console.firebase.google.com/> y cree un proyecto llamado, por ejemplo, `taller3-firebase-samuel`.
2. Agregue una aplicación Android con el paquete exacto `com.example.taller3firebase`.
3. Descargue `google-services.json` y cópielo en `app/google-services.json`.
4. En **Authentication > Sign-in method**, habilite **Correo electrónico/contraseña**.
5. En **Firestore Database**, cree la base de datos. Para una demostración puede elegir la región más cercana disponible.
6. Publique el contenido de `firestore.rules` en **Firestore Database > Rules**.

También se pueden desplegar las reglas mediante Firebase CLI desde la raíz:

```text
firebase login
firebase use --add
firebase deploy --only firestore:rules
```

## 2. Cloudinary para fotografías

Cloudinary reemplaza Firebase Storage y permite realizar la demostración sin activar un plan de pago de Firebase.

1. Cree una cuenta gratuita en <https://cloudinary.com/> y verifique el correo electrónico.
2. En el panel de Cloudinary copie el valor **Cloud name**.
3. Abra **Settings > Upload > Upload presets** y cree un preset.
4. Configure el preset con **Signing mode: Unsigned**. Como protección adicional puede limitar los formatos a `jpg`, `jpeg`, `png` y `webp`.
5. Copie `secrets.properties.example` como `secrets.properties` en la raíz del proyecto.
6. Escriba el `cloud_name` y el nombre del preset unsigned:

```text
CLOUDINARY_CLOUD_NAME=SU_CLOUD_NAME
CLOUDINARY_UPLOAD_PRESET=SU_PRESET_UNSIGNED
```

No agregue `api_key` ni `api_secret` a la aplicación. La foto se sube directamente desde Android mediante el preset unsigned y Firestore guarda únicamente la URL HTTPS resultante.

## 3. OpenStreetMap

El mapa utiliza OpenStreetMap mediante osmdroid. No requiere API key, cuenta de Google Cloud ni método de pago. La aplicación configura un identificador propio para solicitar los mosaicos y muestra la atribución de OpenStreetMap dentro del mapa.

No suba `google-services.json` ni `secrets.properties` a un repositorio público; ambos están incluidos en `.gitignore`.

## 4. Prueba con dos usuarios

1. Instale el APK en dos emuladores o teléfonos con Google Play Services.
2. Registre una cuenta diferente en cada dispositivo y tome una fotografía de perfil.
3. Active el switch en ambos dispositivos y permita ubicación precisa.
4. Cambie la ubicación de cada emulador desde **Extended controls > Location** o camine con los dispositivos.
5. Confirme que ambos marcadores, fotos y polylines se actualizan en tiempo real.
6. Apague un switch y confirme que ese marcador y su polyline desaparecen en el otro dispositivo.

