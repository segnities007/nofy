package com.segnities007.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.segnities007.database.dao.NoteDao
import com.segnities007.database.entity.NoteEntity
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [NoteEntity::class],
    version = 2,
    exportSchema = false
)
abstract class NofyDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        private const val DATABASE_NAME = "nofy_secure.db"

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

        /**
         * データベースを安全に構築する。
         * パスフレーズが渡された場合、SQLCipherによってファイル全体が暗号化される。
         */
        fun build(context: Context, passphrase: ByteArray? = null): NofyDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                NofyDatabase::class.java,
                DATABASE_NAME
            ).addMigrations(MIGRATION_1_2)

            if (passphrase != null) {
                // SQLCipherを使用して暗号化
                val factory = SupportFactory(passphrase)
                builder.openHelperFactory(factory)
            }

            return builder.build()
        }

        fun delete(context: Context) {
            context.applicationContext.deleteDatabase(DATABASE_NAME)
        }
    }
}
