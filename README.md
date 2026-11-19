# CentroTareas

## Observación del Paso 1

Pulsé **Tarea larga (MAL)** y enseguida **Tócame** varias veces: durante unos 4 segundos la app no hizo nada. El contador no subió, los botones no reaccionaron y al final apareció **Terminó**, sin que se viera nunca **Trabajando...**.

- **¿Qué le pasa a la pantalla?** Se queda congelada, porque `Thread.sleep(4000)` duerme el **hilo principal (main)**, que es el que dibuja la interfaz y recibe los toques.
- **¿Por qué no aparece "Trabajando..."?** El valor del mensaje sí cambia, pero la pantalla se redibuja recién cuando el hilo principal queda libre. Como se durmió justo después, cuando se despierta el mensaje ya es "Terminó" y solo se dibuja ese.
- **¿Y con 10 segundos?** Saldría el aviso **ANR ("CentroTareas no responde")**, con la opción de cerrar la app o esperar. Android lo muestra cuando el hilo principal pasa unos 5 segundos sin responder a un toque.
