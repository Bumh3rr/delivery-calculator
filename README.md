# Calculadora de Rentabilidad para Repartidor

App Android nativa (Kotlin + Jetpack Compose) que ayuda a un repartidor en
moto a decidir si le conviene aceptar un pedido, antes de aceptarlo:
calcula la ganancia neta, el costo real por kilómetro y una recomendación
(CONVIENE / AL LÍMITE / NO CONVIENE) según el pago, la distancia y el tipo
de terreno. Guarda cada cálculo en un historial agrupado por día.

## Requisitos

- Android Studio (versión reciente, compatible con AGP 9.2.1 / Kotlin 2.2.10)
- JDK 17+
- Un dispositivo o emulador con Android 8.0 (API 26) o superior

## Compilar y ejecutar

```bash
./gradlew assembleDebug
# o, con un dispositivo/emulador conectado:
./gradlew installDebug
```

Para correr el único test unitario (`CalcularRentabilidadUseCaseTest`):

```bash
./gradlew testDebugUnitTest
```

## Ajustar los parámetros del cálculo

Desde la pantalla **Ajustes** de la app se editan, sin tocar código:

- **Precio de la gasolina** ($/litro) — default 24.0
- **Costo de desgaste base** ($/km) — default 0.50
- **Umbral de rentabilidad** ($/km) — default 2.50

El botón "Restaurar valores por defecto" regresa los tres a sus valores
originales. Los cambios se guardan en Room y afectan todos los cálculos
posteriores (no los ya guardados en el historial).

Para cambiar el rendimiento de gasolina o el factor de desgaste por tipo
de terreno (Plano/Alto/Cerro), edita el enum `TipoTerreno` en
`app/src/main/java/com/securitech/repartidor/domain/model/TipoTerreno.kt`.

## Burbuja flotante (acceso rápido)

En **Ajustes → Burbuja flotante activa** se puede encender un ícono
flotante visible sobre cualquier app (como los "chat heads" de Messenger).
Tocarlo abre la app directo en la Calculadora.

La primera vez que se activa, Android pide dos permisos:

1. **Mostrar sobre otras apps** (obligatorio para dibujar la burbuja).
2. **Notificaciones** (Android 13+; la burbuja corre como un servicio en
   primer plano, que por requisito de Android debe mostrar una
   notificación fija mientras esté activo — se puede detener desde ahí o
   desde el switch de Ajustes).

Si el sistema mata el proceso en segundo plano (ahorro de batería
agresivo de algunos fabricantes), la burbuja no se reinicia sola: hay que
volver a activarla desde Ajustes (decisión de alcance: no se usa
`RECEIVE_BOOT_COMPLETED`).

## Arquitectura

- **MVVM + UDF**: cada pantalla tiene un `ViewModel` que expone un único
  `StateFlow<UiState>`; los composables solo leen ese estado y llaman
  funciones del ViewModel.
- **Capas**: `domain` (modelos y `CalcularRentabilidadUseCase`, sin
  dependencias de Android) → `data` (Room + repositorio) → `ui` (Compose).
- **Inyección de dependencias**: Hilt (`di/DatabaseModule.kt`,
  `di/RepositoryModule.kt`).
- **Persistencia**: Room, con `Flow` para observar cambios en tiempo real.
