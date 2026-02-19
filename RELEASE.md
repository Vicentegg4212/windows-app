# Cómo publicar un Release con el .exe

## Opción A — Subir el .exe manualmente (rápido)

1. El ejecutable ya está generado en tu máquina:
   ```
   windows-app/publish/win-x64/DetectorSismos.exe
   ```
2. Ve a **https://github.com/Vicentegg4212/windows-app/releases**
3. Clic en **"Draft a new release"**
4. **Tag:** escribe `v1.0.0` (o el que quieras) y elige **"Create new tag"**
5. **Release title:** por ejemplo `SASMEX v1.0.0`
6. En **"Attach binaries"** arrastra o selecciona `DetectorSismos.exe` (está en `publish/win-x64/`)
7. Clic en **"Publish release"**

Así los usuarios podrán descargar el .exe listo para usar.

---

## Opción B — Releases automáticos con GitHub Actions

El workflow en `.github/workflows/release.yml` crea un release con el .exe cada vez que subes un tag `v*`.

**Para poder subir el workflow** hace falta un token con permiso **workflow**:

1. GitHub → Settings → Developer settings → Personal access tokens
2. Crea un token con la casilla **workflow** marcada
3. En tu equipo: `git remote set-url origin https://TU_USUARIO:TU_TOKEN@github.com/Vicentegg4212/windows-app.git`
4. Luego: `git push origin main` (subirá el workflow)

**Para crear un release desde un tag:**

```bash
cd windows-app
git tag v1.0.0
git push origin v1.0.0
```

En unos minutos aparecerá el release en la pestaña Releases con el .exe adjunto.
