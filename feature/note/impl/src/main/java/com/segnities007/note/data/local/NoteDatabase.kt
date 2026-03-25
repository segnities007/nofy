package com.segnities007.note.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/** SQLCipher で暗号化したノートテーブルを保持する Room データベース。 */
@Database(
    entities = [NoteEntity::class],
    version = 2,
    exportSchema = true
)
internal abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        internal const val DATABASE_NAME = "nofy_secure.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `notes_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `encryptedContent` BLOB NOT NULL,
                        `iv` BLOB NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `notes_new` (`id`, `encryptedContent`, `iv`, `createdAt`, `updatedAt`)
                    SELECT `id`, `encryptedContent`, `iv`, `createdAt`, `updatedAt`
                    FROM `notes`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE IF EXISTS `notes`")
                db.execSQL("ALTER TABLE `notes_new` RENAME TO `notes`")
                db.execSQL("DROP TABLE IF EXISTS `users`")
            }
        }

        fun build(context: Context, passphrase: ByteArray): NoteDatabase {
            val factory = SupportOpenHelperFactory(passphrase)
            return Room.databaseBuilder(
                context.applicationContext,
                NoteDatabase::class.java,
                DATABASE_NAME
            ).addMigrations(MIGRATION_1_2)
                .openHelperFactory(factory)
                .build()
        }

        fun delete(context: Context) {
            context.applicationContext.deleteDatabase(DATABASE_NAME)
        }
    }
}
