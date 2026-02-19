# Mejoras de UI e interfaz de usuario — SASMEX

Propuestas ordenadas por impacto y esfuerzo.

---

## Alta prioridad (rápido y muy visible)

| Mejora | Descripción |
|--------|-------------|
| **Tooltips** | En "Actualizar" y "Monitoreo automático" para explicar la acción (ya aplicado en código). |
| **Estado del botón al cargar** | Botón "Actualizar" muestra "Cargando…" y está deshabilitado mientras se obtienen datos (ya aplicado). |
| **Color dinámico en la tarjeta de alerta** | La barra lateral de la última alerta en verde (menor), naranja (moderada) o rojo (mayor) según severidad (ya aplicado). |
| **Indicador de conexión** | En la barra de estado, un punto verde "Conectado" o rojo "Sin conexión" según el resultado de la última actualización. (Pendiente.) |
| **Atajo de teclado** | F5 = Actualizar (ya aplicado). |

---

## Prioridad media (mejora clara de uso)

| Mejora | Descripción |
|--------|-------------|
| **Filtro por severidad** | ComboBox o chips: "Todas / Mayor / Moderada / Menor" para filtrar la lista sin cambiar la fuente de datos. |
| **Buscar en alertas** | Cuadro de búsqueda que filtre por evento o descripción en la lista. |
| **Detalle al hacer doble clic** | Al hacer doble clic en una fila, abrir un panel o ventana con la alerta completa (evento, fecha, severidad, descripción larga). |
| **Hover en filas del DataGrid** | Resaltar la fila bajo el mouse para mejorar legibilidad. |
| **Recordar tamaño y posición** | Guardar en configuración el tamaño y la posición de la ventana y restaurarlos al abrir. |

---

## Prioridad media–baja (pulido)

| Mejora | Descripción |
|--------|-------------|
| **Icono en cabecera** | Reemplazar el texto "SAS" por un icono .ico o PNG (logo o ícono de alerta). |
| **Menú o botones en cabecera** | "Acerca de", "Configuración" (intervalo, notificaciones), "Salir" desde la barra superior. |
| **Tiempo relativo** | En la tarjeta y/o en la tabla, mostrar "hace 5 min" además de la fecha/hora absoluta. |
| **Notificaciones tipo toast** | En lugar de (o además de) MessageBox para nuevas alertas, una notificación pequeña en una esquina que se cierre sola. |

---

## Opcional (más desarrollo)

| Mejora | Descripción |
|--------|-------------|
| **Tema oscuro** | Toggle o detección del tema de Windows para alternar paleta clara/oscura. |
| **Panel lateral colapsable** | Poder colapsar la tarjeta "Última alerta" para dar más espacio a la tabla. |
| **Sonido en alertas** | Reproducir un sonido corto (configurable on/off) para severidad "Mayor". |
| **Exportar** | Botón "Exportar" para guardar la lista actual en CSV o TXT. |

---

## Resumen de lo ya implementado en código

- Tooltips en botón Actualizar y en el CheckBox de monitoreo.
- Botón "Actualizar" muestra "Cargando…" y se deshabilita mientras se cargan datos.
- Barra lateral de la tarjeta de última alerta en color según severidad (Menor=verde, Moderada=naranja, Mayor=rojo).
- Atajo F5 para actualizar.

Si quieres, el siguiente paso puede ser implementar filtro por severidad + búsqueda o el detalle al doble clic.
