import { open, type DB } from '@op-engineering/op-sqlite';
import { DB_NAME, CREATE_TABLES } from './schema';

let db: DB | null = null;

export const getDatabase = (): DB => {
  if (!db) {
    throw new Error('Base de datos no inicializada. Llama initDatabase() primero.');
  }
  return db;
};

export const initDatabase = async (): Promise<void> => {
  // Evitar doble apertura (React 19 Strict Mode ejecuta effects dos veces en desarrollo)
  if (db) {
    console.log('[DB] Ya inicializada, omitiendo.');
    return;
  }

  try {
    console.log('[DB] Abriendo Base de Datos:', DB_NAME);
    db = open({ name: DB_NAME });

    // Habilitar llaves foráneas
    await db.execute('PRAGMA foreign_keys = ON;');

    // Verificar si las tablas ya existen para evitar pasos innecesarios
    const tableCheck = await db.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='configuracion';");
    const isFirstTime = !tableCheck.rows || tableCheck.rows.length === 0;
    
    if (isFirstTime) {
      console.log('[DB] Primera ejecución detectada. Creando tablas...');
    }

    const statements = CREATE_TABLES
      .split(';')
      .map(s => s.trim())
      .filter(s => s.length > 0);

    for (const statement of statements) {
      try {
        await db.execute(statement + ';');
      } catch (e) {
        console.error('[DB] Error ejecutando sentencia SQL:', statement, e);
      }
    }

    console.log('[DB] Base de datos lista.');
  } catch (error) {
    console.error('[DB] Error FATAL al inicializar la base de datos:', error);
    db = null; // resetear para que el próximo intento pueda reintentar
    throw error;
  }
};

export const closeDatabase = (): void => {
  if (db) {
    db.close();
    db = null;
  }
};
