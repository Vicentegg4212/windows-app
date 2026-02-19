# Cómo tener la APK en el release v1.0.0

El .exe ya está en [Releases v1.0.0](https://github.com/Vicentegg4212/windows-app/releases/tag/v1.0.0). Para que la **APK** aparezca ahí, el workflow de GitHub Actions debe existir en el repo y ejecutarse una vez.

## Opción recomendada: añadir el workflow desde GitHub

1. Entra en el repo: **https://github.com/Vicentegg4212/windows-app**
2. Crea el archivo del workflow:
   - Clic en **Add file** → **Create new file**
   - Ruta del archivo: **`.github/workflows/android-release.yml`**
   - Copia y pega el contenido del archivo **`android-release.yml.content`** (está en esta misma carpeta) y guarda con **Commit main**.

3. Ejecuta el workflow:
   - Pestaña **Actions** → **Android APK Release** → **Run workflow** → **Run workflow**
   - O haz un pequeño cambio en algo dentro de `sasmex-android/` y haz push a `main`; el workflow se dispara solo.

4. Cuando termine, en **Releases** → **v1.0.0** deberías ver **SasmexAlertas.apk** junto al .exe.

## Si prefieres subir la APK a mano

1. En tu máquina: instala **Android Studio** (o JDK 17 + Android SDK) y abre el proyecto `sasmex-android/`.
2. Genera el wrapper: en la raíz de `sasmex-android/` ejecuta  
   `gradle wrapper --gradle-version 8.4`
3. Compila:  
   `./gradlew assembleDebug`
4. La APK queda en:  
   `app/build/outputs/apk/debug/app-debug.apk`
5. En GitHub: **Releases** → **v1.0.0** → **Edit** → arrastra el APK como nuevo asset (o usa la API con un token que tenga permisos de release).

---

El workflow que está en tu copia local (`.github/workflows/android-release.yml`) ya está preparado para construir la APK y subirla al release v1.0.0; solo falta que ese archivo exista en el repo en GitHub (por eso se creó **android-release.yml.content** para copiar/pegar en la web).
