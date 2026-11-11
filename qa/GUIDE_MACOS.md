# Guía de Notekea QA — macOS

Gracias por probar Notekea. No necesitas saber nada de programación. Instalas una sola app, **Notekea QA**: Notekea
con una ventanita abajo a la izquierda, el **gestor**, que lo prueba y te dice qué hacer. Notekea QA tiene sus propias
notas y ajustes, así que nunca toca un Notekea que uses a diario, y empieza en español (puedes cambiarlo en Ajustes ›
General). La parte automática corre sola en unos 20 minutos; los casos manuales toman de una a dos horas, y puedes
parar y seguir después. Si algo de esta guía no se entiende, es un problema de la guía, no tuyo: dilo al enviar el
reporte.

## 1. Descarga

Recibes un enlace a una **release** en GitHub, con un nombre como `Notekea m1-rc1`. En esa página, en **Assets**,
descarga `Notekea.QA-<versión>.dmg`.

## 2. Instala

Haz doble clic en `Notekea.QA-<versión>.dmg` y arrastra **Notekea QA** a **Aplicaciones** en la ventana que se
abre. La primera vez que lo abras, macOS puede decir que **no puede comprobar que no tenga software malicioso**: las
versiones todavía no están firmadas. Haz clic en **OK**, abre **Ajustes del Sistema › Privacidad y seguridad**, baja y
haz clic en **Abrir de todos modos** junto a Notekea QA, y luego en **Abrir**.

## 3. La parte automática

1. Abre **Notekea QA** desde **Aplicaciones** (o Launchpad). Se abre Notekea, y el gestor aparece abajo a la izquierda de la pantalla. Puedes
   moverlo, y minimizarlo cuando estorbe; cerrarlo solo lo minimiza.
2. En el gestor, haz clic en **Empezar las pruebas automáticas**. Dice **Corriendo pruebas** y muestra en todo momento
   el tiempo, cuántos casos van y cuánto falta, cuántos pasaron y fallaron, el caso que corre **ahora** con cada paso a
   medida que ocurre, y los últimos terminados. Corren sin que se vean, sobre copias de las notas de prueba: puedes
   seguir usando la computadora.
3. Al terminar, los casos que fallaron salen en rojo: no tienes que hacer nada con ellos ahora, van al reporte. Haz
   clic en **Siguiente: casos manuales**.

## 4. Los casos manuales

El gestor muestra un caso a la vez, y sus pasos como una línea: ✓ los hechos, el actual en negrita, los que faltan.

1. Haz el paso **en Notekea** y luego haz clic en **Listo, siguiente paso**. **Atrás** vuelve al paso anterior.
2. Cuando un paso necesita las notas de prueba del caso, la tarjeta **DATOS DE PRUEBA** dice en qué carpeta están: en
   Notekea, ábrela con **Abrir otra bóveda** (abajo a la izquierda) y luego **Abrir carpeta…**.
3. Algunos casos piden **Reiniciar Notekea para este caso**: haz clic, y Notekea y el gestor vuelven en ese caso.
4. Después del último paso, el gestor muestra lo que deberías haber visto. Haz clic en la respuesta verde si lo
   viste, o en la roja si no: escribe **¿Qué viste?**, haz clic en **Agregar una captura** si tomaste una, y en
   **Guardar e ir al siguiente caso**. Tus últimos pasos en Notekea se agregan solos al reporte. **Saltar este caso**
   lo deja para después.

**Capturas:** pulsa **Cmd + Shift + 4**, luego la **barra espaciadora**, y haz clic en la ventana. La imagen se guarda
en el Escritorio; elígela con **Agregar una captura**. En macOS los atajos que nombran los casos usan **Cmd**.

Tus respuestas se guardan: puedes cerrar Notekea QA y abrirlo después, y sigue donde lo dejaste. El último caso son
15 minutos de uso libre: usa Notekea como lo usarías de verdad y di si encontraste algo raro.

## 5. Envía el reporte

1. En el gestor, haz clic en **Reporte**, luego en **Copiar el reporte**, y pégalo en un mensaje (correo,
   WhatsApp…) para quien te pidió probar.
2. Haz clic en **Abrir la carpeta Report** y adjunta al mismo mensaje las imágenes que hay ahí y el archivo
   `output.txt`.

## 6. Termina

Cuando hayas enviado el reporte, puedes arrastrar Notekea QA desde **Aplicaciones** a la Papelera. Sus notas y ajustes eran propios: tu Notekea de todos los días, si tienes
uno, sigue como estaba. Cuando llegue una versión nueva (`m1-rc2`), instálala encima de la anterior y repite esta guía:
el gestor solo pide lo que falta.
