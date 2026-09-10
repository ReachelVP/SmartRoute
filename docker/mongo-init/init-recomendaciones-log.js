// Se ejecuta una sola vez, cuando el contenedor de Mongo arranca con un volumen de datos vacío
// (docker-entrypoint-initdb.d). Deja la colección recomendaciones_log lista con validador, índice
// y datos de ejemplo con el solo "docker compose up" — sin depender de que la aplicación arranque.
// Es el mismo espíritu que sqlserver-init para SQL Server.

db = db.getSiblingDB("smartroute");

// El validador exige la forma mínima de un evento de recomendación (Proceso 1 del dominio):
// quién lo pidió, cuándo y con qué criterios. lugaresSugeridos/planesGenerados quedan sin
// validación estricta a propósito — es la parte de esquema flexible del documento.
db.createCollection("recomendaciones_log", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["usuarioId", "fecha", "criterios"],
      properties: {
        usuarioId: {
          bsonType: "long",
          description: "referencia al usuario real en SQL Server (persistencia políglota)",
        },
        fecha: {
          bsonType: "date",
        },
        criterios: {
          bsonType: "object",
          required: ["provinciaId", "categorias", "presupuestoMaximo", "tieneTransportePropio"],
          properties: {
            provinciaId: { bsonType: "long" },
            categorias: {
              bsonType: "array",
              items: { bsonType: "string" },
            },
            presupuestoMaximo: { bsonType: ["decimal", "double", "int", "long"] },
            tieneTransportePropio: { bsonType: "bool" },
          },
        },
        lugaresSugeridos: { bsonType: "array" },
        planesGenerados: { bsonType: "array" },
      },
    },
  },
});

// El propio documento del modelo dice que el usuario consulta su historial más reciente primero:
// { usuarioId: 1, fecha: -1 } es exactamente ese patrón de lectura.
db.recomendaciones_log.createIndex({ usuarioId: 1, fecha: -1 }, { name: "idx_usuario_fecha" });

function diasAtras(n) {
  const fecha = new Date();
  fecha.setUTCDate(fecha.getUTCDate() - n);
  return fecha;
}

// 12 documentos entre 2 usuarios (los mismos sembrados en SQL Server, V2__datos_semilla.sql) y a
// lo largo de ~2 semanas, para que la colección muestre el patrón de crecimiento real: varios
// eventos de recomendación por usuario en el tiempo, no una sola foto aislada.
db.recomendaciones_log.insertMany([
  // Usuario 1 — Reachel (San José, interés histórico/cultural, sin transporte propio)
  {
    usuarioId: NumberLong(1),
    fecha: diasAtras(14),
    criterios: {
      provinciaId: NumberLong(1),
      categorias: ["Histórica", "Cultural"],
      presupuestoMaximo: NumberDecimal("20000.00"),
      tieneTransportePropio: false,
    },
    lugaresSugeridos: [
      { lugarId: NumberLong(3), nombre: "Museo Nacional de Costa Rica", categoria: "Histórica", costoEstimado: NumberDecimal("3000.00"), score: 0.91 },
      { lugarId: NumberLong(4), nombre: "Teatro Nacional", categoria: "Cultural", costoEstimado: NumberDecimal("2000.00"), score: 0.83 },
    ],
    planesGenerados: [
      {
        paradas: [
          { lugarId: NumberLong(3), nombre: "Museo Nacional de Costa Rica", orden: 1, horaInicio: "09:00", horaFin: "11:00" },
          { lugarId: NumberLong(4), nombre: "Teatro Nacional", orden: 2, horaInicio: "11:30", horaFin: "13:00" },
        ],
        costoTotal: NumberDecimal("5000.00"),
        duracionMinutos: 240,
        score: 0.87,
      },
    ],
  },
  {
    usuarioId: NumberLong(1),
    fecha: diasAtras(10),
    criterios: {
      provinciaId: NumberLong(1),
      categorias: ["Social"],
      presupuestoMaximo: NumberDecimal("20000.00"),
      tieneTransportePropio: false,
    },
    lugaresSugeridos: [
      { lugarId: NumberLong(5), nombre: "Mercado Central de San José", categoria: "Social", costoEstimado: NumberDecimal("5000.00"), score: 0.75 },
    ],
    planesGenerados: [],
  },
  {
    usuarioId: NumberLong(1),
    fecha: diasAtras(7),
    criterios: {
      provinciaId: NumberLong(3),
      categorias: ["Histórica"],
      presupuestoMaximo: NumberDecimal("20000.00"),
      tieneTransportePropio: false,
    },
    lugaresSugeridos: [
      { lugarId: NumberLong(6), nombre: "Basílica de Nuestra Señora de los Ángeles", categoria: "Histórica", costoEstimado: NumberDecimal("0.00"), score: 0.95 },
      { lugarId: NumberLong(7), nombre: "Ruinas de Cartago", categoria: "Histórica", costoEstimado: NumberDecimal("0.00"), score: 0.9 },
    ],
    planesGenerados: [
      {
        paradas: [
          { lugarId: NumberLong(6), nombre: "Basílica de Nuestra Señora de los Ángeles", orden: 1, horaInicio: "09:00", horaFin: "10:00" },
          { lugarId: NumberLong(7), nombre: "Ruinas de Cartago", orden: 2, horaInicio: "10:30", horaFin: "12:00" },
        ],
        costoTotal: NumberDecimal("0.00"),
        duracionMinutos: 150,
        score: 0.92,
      },
    ],
  },
  {
    usuarioId: NumberLong(1),
    fecha: diasAtras(5),
    criterios: {
      provinciaId: NumberLong(1),
      categorias: ["Cultural"],
      presupuestoMaximo: NumberDecimal("15000.00"),
      tieneTransportePropio: false,
    },
    lugaresSugeridos: [
      { lugarId: NumberLong(4), nombre: "Teatro Nacional", categoria: "Cultural", costoEstimado: NumberDecimal("2000.00"), score: 0.8 },
    ],
    planesGenerados: [],
  },
  {
    usuarioId: NumberLong(1),
    fecha: diasAtras(2),
    criterios: {
      provinciaId: NumberLong(1),
      categorias: ["Histórica", "Social"],
      presupuestoMaximo: NumberDecimal("20000.00"),
      tieneTransportePropio: false,
    },
    lugaresSugeridos: [
      { lugarId: NumberLong(3), nombre: "Museo Nacional de Costa Rica", categoria: "Histórica", costoEstimado: NumberDecimal("3000.00"), score: 0.88 },
      { lugarId: NumberLong(5), nombre: "Mercado Central de San José", categoria: "Social", costoEstimado: NumberDecimal("5000.00"), score: 0.7 },
    ],
    planesGenerados: [
      {
        paradas: [
          { lugarId: NumberLong(3), nombre: "Museo Nacional de Costa Rica", orden: 1, horaInicio: "09:00", horaFin: "11:00" },
          { lugarId: NumberLong(5), nombre: "Mercado Central de San José", orden: 2, horaInicio: "11:30", horaFin: "13:00" },
        ],
        costoTotal: NumberDecimal("8000.00"),
        duracionMinutos: 240,
        score: 0.85,
      },
    ],
  },
  {
    usuarioId: NumberLong(1),
    fecha: diasAtras(1),
    criterios: {
      provinciaId: NumberLong(4),
      categorias: ["Social"],
      presupuestoMaximo: NumberDecimal("10000.00"),
      tieneTransportePropio: false,
    },
    lugaresSugeridos: [
      { lugarId: NumberLong(10), nombre: "Feria del Agricultor de Heredia", categoria: "Social", costoEstimado: NumberDecimal("4000.00"), score: 0.82 },
    ],
    planesGenerados: [],
  },

  // Usuario 2 — Kimberley (interés natural/social, con transporte propio)
  {
    usuarioId: NumberLong(2),
    fecha: diasAtras(13),
    criterios: {
      provinciaId: NumberLong(6),
      categorias: ["Natural"],
      presupuestoMaximo: NumberDecimal("35000.00"),
      tieneTransportePropio: true,
    },
    lugaresSugeridos: [
      { lugarId: NumberLong(9), nombre: "Parque Nacional Manuel Antonio", categoria: "Natural", costoEstimado: NumberDecimal("18000.00"), score: 0.95 },
      { lugarId: NumberLong(8), nombre: "Playa Jacó", categoria: "Natural", costoEstimado: NumberDecimal("10000.00"), score: 0.8 },
    ],
    planesGenerados: [
      {
        paradas: [
          { lugarId: NumberLong(9), nombre: "Parque Nacional Manuel Antonio", orden: 1, horaInicio: "08:00", horaFin: "13:00" },
        ],
        costoTotal: NumberDecimal("18000.00"),
        duracionMinutos: 300,
        score: 0.9,
      },
    ],
  },
  {
    usuarioId: NumberLong(2),
    fecha: diasAtras(9),
    criterios: {
      provinciaId: NumberLong(2),
      categorias: ["Natural"],
      presupuestoMaximo: NumberDecimal("35000.00"),
      tieneTransportePropio: true,
    },
    lugaresSugeridos: [
      { lugarId: NumberLong(1), nombre: "Volcán Poás", categoria: "Natural", costoEstimado: NumberDecimal("8000.00"), score: 0.88 },
      { lugarId: NumberLong(2), nombre: "Catarata La Paz", categoria: "Natural", costoEstimado: NumberDecimal("15000.00"), score: 0.85 },
    ],
    planesGenerados: [
      {
        paradas: [
          { lugarId: NumberLong(1), nombre: "Volcán Poás", orden: 1, horaInicio: "07:00", horaFin: "10:00" },
          { lugarId: NumberLong(2), nombre: "Catarata La Paz", orden: 2, horaInicio: "11:00", horaFin: "13:00" },
        ],
        costoTotal: NumberDecimal("23000.00"),
        duracionMinutos: 360,
        score: 0.86,
      },
    ],
  },
  {
    usuarioId: NumberLong(2),
    fecha: diasAtras(6),
    criterios: {
      provinciaId: NumberLong(6),
      categorias: ["Natural", "Social"],
      presupuestoMaximo: NumberDecimal("20000.00"),
      tieneTransportePropio: true,
    },
    lugaresSugeridos: [
      { lugarId: NumberLong(8), nombre: "Playa Jacó", categoria: "Natural", costoEstimado: NumberDecimal("10000.00"), score: 0.83 },
    ],
    planesGenerados: [],
  },
  {
    usuarioId: NumberLong(2),
    fecha: diasAtras(4),
    criterios: {
      provinciaId: NumberLong(1),
      categorias: ["Social"],
      presupuestoMaximo: NumberDecimal("35000.00"),
      tieneTransportePropio: true,
    },
    lugaresSugeridos: [
      { lugarId: NumberLong(5), nombre: "Mercado Central de San José", categoria: "Social", costoEstimado: NumberDecimal("5000.00"), score: 0.7 },
    ],
    planesGenerados: [],
  },
  {
    usuarioId: NumberLong(2),
    fecha: diasAtras(3),
    criterios: {
      provinciaId: NumberLong(2),
      categorias: ["Natural"],
      presupuestoMaximo: NumberDecimal("9000.00"),
      tieneTransportePropio: true,
    },
    lugaresSugeridos: [
      { lugarId: NumberLong(1), nombre: "Volcán Poás", categoria: "Natural", costoEstimado: NumberDecimal("8000.00"), score: 0.79 },
    ],
    planesGenerados: [],
  },
  {
    // Ejemplo de "sin coincidencias": el presupuesto quedó por debajo de cualquier lugar natural
    // disponible en Puntarenas (el más barato, Playa Jacó, cuesta 10000) — también se registra.
    usuarioId: NumberLong(2),
    fecha: diasAtras(0),
    criterios: {
      provinciaId: NumberLong(6),
      categorias: ["Natural"],
      presupuestoMaximo: NumberDecimal("5000.00"),
      tieneTransportePropio: true,
    },
    lugaresSugeridos: [],
    planesGenerados: [],
  },
]);
