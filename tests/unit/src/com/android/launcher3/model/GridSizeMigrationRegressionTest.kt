package com.android.launcher3.model

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteException
import android.os.Process
import com.android.launcher3.ConstantItem
import com.android.launcher3.EncryptionType
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.LauncherFiles
import com.android.launcher3.LauncherModel
import com.android.launcher3.LauncherPrefs
import com.android.launcher3.LauncherSettings.Favorites
import com.android.launcher3.Utilities
import com.android.launcher3.dagger.LauncherAppComponent
import com.android.launcher3.dagger.LauncherComponentProvider
import com.android.launcher3.backuprestore.LauncherRestoreEventLogger
import com.android.launcher3.folder.FolderNameProvider
import com.android.launcher3.homescreenfiles.HomeScreenFilesProvider
import com.android.launcher3.icons.IconCache
import com.android.launcher3.logging.StatsLogManager
import com.android.launcher3.model.data.LoaderParams
import com.android.launcher3.pm.InstallSessionHelper
import com.android.launcher3.pm.UserCache
import com.android.launcher3.provider.LauncherDbUtils
import com.android.launcher3.util.PackageManagerHelper
import com.android.launcher3.util.SettingsCache
import com.android.launcher3.widget.util.WidgetSizeHandler
import com.neoapps.neolauncher.allapps.NeoLauncherModelDelegate
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode

/**
 * Real controller, migration algorithm, preferences, and file-backed native SQLite. Launcher DI,
 * logging, and the smartspace-enabled setting are isolated from application startup.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class GridSizeMigrationRegressionTest {
    private lateinit var context: Context
    private lateinit var prefs: LauncherPrefs
    private lateinit var users: UserCache
    private lateinit var componentProvider: MockedStatic<LauncherComponentProvider>
    private lateinit var utilities: MockedStatic<Utilities>
    private lateinit var logic: GridSizeMigrationLogic
    private lateinit var delegate: NeoLauncherModelDelegate
    private val helpers = mutableListOf<DatabaseHelper>()

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        prefs = LauncherPrefs(context)
        users = mock(UserCache::class.java)
        `when`(users.userProfiles).thenReturn(listOf(Process.myUserHandle()))
        val component = mock(LauncherAppComponent::class.java)
        `when`(component.launcherPrefs).thenReturn(prefs)
        `when`(component.userCache).thenReturn(users)
        componentProvider = mockStatic(LauncherComponentProvider::class.java)
        componentProvider.`when`<LauncherAppComponent> { LauncherComponentProvider.get(context) }
            .thenReturn(component)
        utilities = mockStatic(Utilities::class.java) { invocation ->
            if (invocation.method.name == "qsbOnFirstScreen") false
            else invocation.callRealMethod()
        }
        val stats = mock(StatsLogManager::class.java, RETURNS_DEEP_STUBS)
        val factory = mock(StatsLogManager.StatsLogManagerFactory::class.java)
        `when`(factory.create(context)).thenReturn(stats)
        logic = GridSizeMigrationLogic(context, prefs, factory) { emptySet() }
        delegate = mock(NeoLauncherModelDelegate::class.java)
    }

    @After
    fun tearDown() {
        helpers.forEach { it.close() }
        utilities.close()
        componentProvider.close()
    }

    @Test
    fun sameDatabaseIsNoOpAndPreservesWidgetIds() {
        val source = helper(SOURCE)
        populate(source)
        state(4, 4, SOURCE).writeToPrefs(context)
        val before = rows(source)
        val controller = controller(4, 4, SOURCE, source)

        controller.attemptMigrateDb(null, delegate)

        assertSame(source, controller.mOpenHelper)
        assertEquals(before, rows(source))
        assertEquals(SOURCE, prefs.get(LauncherPrefs.DB_FILE))
        verifyNoInteractions(delegate)
    }

    @Test
    fun freshDatabaseKeepsDefaultInitializationFlag() {
        val controller = controller(4, 4, SOURCE)

        controller.attemptMigrateDb(null, delegate)
        val source = controller.mOpenHelper
        helpers += source

        assertEquals(SOURCE, source.databaseName)
        assertTrue(rows(source).isEmpty())
        assertTrue(prefs.get(emptyFlag(SOURCE)))
        assertEquals(SOURCE, prefs.get(LauncherPrefs.DB_FILE))
        assertEquals("4,4", prefs.get(LauncherPrefs.WORKSPACE_SIZE))
        assertEquals(4, prefs.get(LauncherPrefs.HOTSEAT_COUNT))
        verifyNoInteractions(delegate)
    }

    @Test
    fun staleFreshDatabaseFlagCannotReplaceExistingLayout() {
        val source = helper(SOURCE)
        populate(source)
        state(4, 4, SOURCE).writeToPrefs(context)
        prefs.putSync(emptyFlag(SOURCE).to(true))
        val before = rows(source)
        val controller = controller(3, 3, TARGET, source)

        assertThrows(IllegalStateException::class.java) {
            controller.attemptMigrateDb(null, delegate)
        }

        assertSame(source, controller.mOpenHelper)
        assertEquals(before, rows(source))
        assertEquals(SOURCE, prefs.get(LauncherPrefs.DB_FILE))
        verifyNoInteractions(delegate)
    }

    @Test
    fun invalidGridFailsWithoutReplacingHelperOrPublishingPreferences() {
        val source = helper(SOURCE)
        populate(source)
        state(4, 4, SOURCE).writeToPrefs(context)
        val before = rows(source)
        val controller = controller(9, 9, TARGET, source)

        assertThrows(Exception::class.java) { controller.attemptMigrateDb(null, delegate) }

        assertSame(source, controller.mOpenHelper)
        assertTrue(source.writableDatabase.isOpen)
        assertEquals(before, rows(source))
        assertEquals(SOURCE, prefs.get(LauncherPrefs.DB_FILE))
        assertFalse(context.getDatabasePath(TARGET).exists())
        verifyNoInteractions(delegate)
    }

    @Test
    fun inconsistentPreferencesCannotMigrateDatabaseOntoItself() {
        val current = helper(TARGET)
        populate(current)
        state(4, 4, SOURCE).writeToPrefs(context)
        val before = rows(current)
        val controller = controller(3, 3, TARGET, current)

        assertThrows(IllegalStateException::class.java) {
            controller.attemptMigrateDb(null, delegate)
        }

        assertSame(current, controller.mOpenHelper)
        assertEquals(before, rows(current))
        assertEquals(SOURCE, prefs.get(LauncherPrefs.DB_FILE))
        verifyNoInteractions(delegate)
    }

    @Test
    fun sqliteFailureRollsBackTargetAndRetainsSourceAcrossRestartAndRetry() {
        val source = helper(SOURCE)
        populate(source)
        val target = helper(TARGET)
        insert(target, 90, Favorites.ITEM_TYPE_APPWIDGET, widgetId = 9090)
        target.writableDatabase.execSQL(
            "CREATE TRIGGER fail_widget BEFORE INSERT ON favorites " +
                "WHEN NEW.itemType = ${Favorites.ITEM_TYPE_APPWIDGET} " +
                "BEGIN SELECT RAISE(ABORT, 'injected write failure'); END"
        )
        state(4, 4, SOURCE).writeToPrefs(context)
        val sourceBefore = rows(source)
        val targetBefore = rows(target)
        val controller = controller(3, 3, TARGET, source)

        val error = assertThrows(Exception::class.java) {
            controller.attemptMigrateDb(null, delegate)
        }
        assertTrue(
            error.stackTraceToString(),
            generateSequence(error as Throwable) { it.cause }.any { it is SQLiteException }
        )
        assertSame(source, controller.mOpenHelper)
        assertTrue(source.writableDatabase.isOpen)
        assertEquals(sourceBefore, rows(source))
        assertEquals(targetBefore, rows(target))
        assertEquals(SOURCE, prefs.get(LauncherPrefs.DB_FILE))
        assertEquals("4,4", prefs.get(LauncherPrefs.WORKSPACE_SIZE))
        verifyNoInteractions(delegate)

        // Re-open through the production selection path, not by injecting the old helper.
        source.close()
        val restarted = controller(3, 3, TARGET)
        restarted.query(null, null, null, null).use {
            assertEquals(SOURCE, it.extras.getString(ModelDbController.EXTRA_DB_NAME))
        }
        helpers += restarted.mOpenHelper
        assertEquals(sourceBefore, rows(restarted.mOpenHelper))
        target.writableDatabase.execSQL("DROP TRIGGER fail_widget")
        prefs.putSync(emptyFlag(TARGET).to(true))

        restarted.attemptMigrateDb(null, delegate)
        helpers += restarted.mOpenHelper

        assertEquals(TARGET, restarted.mOpenHelper.databaseName)
        assertEquals(TARGET, prefs.get(LauncherPrefs.DB_FILE))
        assertEquals("3,3", prefs.get(LauncherPrefs.WORKSPACE_SIZE))
        assertEquals(3, prefs.get(LauncherPrefs.HOTSEAT_COUNT))
        assertFalse(prefs.get(emptyFlag(TARGET)))
        assertMigratedLayout(restarted.mOpenHelper, 3, 3)
        verify(delegate).gridMigrationComplete(any(), any())
        val afterRestart = controller(3, 3, TARGET)
        afterRestart.query(null, null, null, null).use {
            assertEquals(TARGET, it.extras.getString(ModelDbController.EXTRA_DB_NAME))
        }
        helpers += afterRestart.mOpenHelper
        assertMigratedLayout(afterRestart.mOpenHelper, 3, 3)
    }

    @Test
    fun successfulResizePreservesFoldersChildrenAndWidgetIds() {
        val source = helper(SOURCE)
        populate(source)
        val before = rows(source)
        state(4, 4, SOURCE).writeToPrefs(context)
        val controller = controller(3, 3, TARGET, source)

        controller.attemptMigrateDb(null, delegate)
        helpers += controller.mOpenHelper

        assertMigratedLayout(controller.mOpenHelper, 3, 3)
        assertEquals(before, rows(source))
        assertEquals(TARGET, prefs.get(LauncherPrefs.DB_FILE))
        verify(delegate).gridMigrationComplete(any(), any())
    }

    @Test
    fun tallerGridCopyPreservesFolderAndWidgetRows() {
        val source = helper(SOURCE)
        populate(source)
        val target = helper(LauncherFiles.LAUNCHER_4_BY_6_DB)
        val before = rows(source)

        logic.migrateGrid(
            state(4, 4, SOURCE),
            state(4, 6, LauncherFiles.LAUNCHER_4_BY_6_DB),
            target,
            source.writableDatabase,
            true,
        )

        assertMigratedLayout(target, 4, 6)
        assertEquals(before, rows(source))
        assertFalse(LauncherDbUtils.tableExists(target.writableDatabase, "favorites_tmp"))
    }

    @Test
    fun missingSourceRowFailsRatherThanSilentlyDroppingItem() {
        val target = helper(TARGET)
        val missing = DbEntry().apply { id = 12345 }
        assertThrows(IllegalStateException::class.java) {
            GridSizeMigrationDBController.insertEntryInDb(
                target, missing, Favorites.TABLE_NAME, Favorites.TABLE_NAME, emptyList()
            )
        }
    }

    @Test
    fun failedAttachedCopyDetachesSourceSoNextCopyCanSucceed() {
        val source = helper(SOURCE)
        populate(source)
        val target = helper(TARGET)
        val before = rows(source)
        assertThrows(SQLiteException::class.java) {
            LauncherDbUtils.copyTable(
                source.writableDatabase, "missing_table",
                target.writableDatabase, "migration_copy", context
            )
        }
        assertFalse(target.writableDatabase.attachedDbs.any { it.first == "from_db" })

        LauncherDbUtils.copyTable(
            source.writableDatabase, Favorites.TABLE_NAME,
            target.writableDatabase, "migration_copy", context
        )

        assertEquals(
            4L, DatabaseUtils.queryNumEntries(target.writableDatabase, "migration_copy")
        )
        assertEquals(before, rows(source))
    }

    @Test
    fun loaderFailureSkipsDefaultsQueryModelClearGhostCleanupBindingAndCommit() {
        val source = helper(SOURCE)
        populate(source)
        state(4, 4, SOURCE).writeToPrefs(context)
        val before = rows(source)
        val controller = spy(controller(9, 9, TARGET, source))
        val model = mock(LauncherModel::class.java)
        `when`(model.modelDbController).thenReturn(controller)
        `when`(model.modelDelegate).thenReturn(delegate)
        val transaction = mock(LauncherModel.LoaderTransaction::class.java)
        val bgModel = mock(BgDataModel::class.java)
        val binder = mock(BaseLauncherBinder::class.java)
        val sessions = mock(InstallSessionHelper::class.java)
        `when`(sessions.activeSessions).thenReturn(hashMapOf())
        val homeFiles = mock(HomeScreenFilesProvider::class.java)
        `when`(homeFiles.query()).thenReturn(lazy { emptyMap() })
        val idp = mock(InvariantDeviceProfile::class.java)
        idp.dbFile = TARGET
        val loader = LoaderTask(
            context, idp, model, users, mock(PackageManagerHelper::class.java),
            sessions, mock(IconCache::class.java), mock(AllAppsList::class.java), bgModel,
            mock(LoaderCursor.LoaderCursorFactory::class.java),
            { mock(FolderNameProvider::class.java) }, false, binder,
            { mock(LauncherRestoreEventLogger::class.java) }, { emptySet() },
            mock(WidgetSizeHandler::class.java), LoaderParams(),
            mock(WorkspaceItemSpaceFinder::class.java), homeFiles,
            mock(FirstScreenBroadcastHelper::class.java), mock(SettingsCache::class.java),
        )
        `when`(model.beginLoader(loader)).thenReturn(transaction)

        loader.run()

        verify(controller).attemptMigrateDb(any(), same(delegate))
        verify(controller, never()).loadDefaultFavoritesIfNecessary()
        verify(controller, never()).query(any(), any(), any(), any())
        verify(controller, never()).removeGhostWidgets()
        verify(bgModel, never()).clear()
        verify(bgModel, never()).updateShortcutPinnedState(context)
        verifyNoInteractions(binder)
        verify(transaction, never()).commit()
        verify(transaction).close()
        verifyNoInteractions(delegate)
        assertEquals(before, rows(source))
        assertEquals(SOURCE, prefs.get(LauncherPrefs.DB_FILE))
    }

    private fun helper(name: String) =
        DatabaseHelper(context, name) {}.also {
            it.initIds()
            helpers += it
        }

    private fun controller(
        columns: Int,
        rows: Int,
        name: String,
        source: DatabaseHelper? = null,
    ): ModelDbController {
        val idp = mock(InvariantDeviceProfile::class.java)
        idp.numColumns = columns
        idp.numRows = rows
        idp.numDatabaseHotseatIcons = columns
        idp.dbFile = name
        return ModelDbController(
            context, idp, prefs, users, mock(LayoutParserFactory::class.java), { logic }
        ).also {
            it.mOpenHelper = source
        }
    }

    private fun state(columns: Int, rows: Int, name: String) =
        DeviceGridState(columns, rows, columns, InvariantDeviceProfile.TYPE_PHONE, name, 0)

    private fun emptyFlag(name: String): ConstantItem<Boolean> =
        LauncherPrefs.backedUpItem(
            "EMPTY_DATABASE_CREATED@$name", false, EncryptionType.ENCRYPTED
        )

    private fun populate(helper: DatabaseHelper) {
        insert(helper, 10, Favorites.ITEM_TYPE_FOLDER)
        insert(helper, 11, Favorites.ITEM_TYPE_APPLICATION, container = 10, intent = "child-one")
        insert(helper, 12, Favorites.ITEM_TYPE_APPLICATION, container = 10, intent = "child-two")
        insert(helper, 20, Favorites.ITEM_TYPE_APPWIDGET, x = 2, y = 2, widgetId = 4242)
    }

    private fun insert(
        helper: DatabaseHelper,
        id: Int,
        type: Int,
        container: Int = Favorites.CONTAINER_DESKTOP,
        x: Int = 0,
        y: Int = 0,
        widgetId: Int = -1,
        intent: String = "item-$id",
    ) {
        val values = ContentValues().apply {
            put(Favorites._ID, id)
            put(Favorites.ITEM_TYPE, type)
            put(Favorites.CONTAINER, container)
            put(Favorites.SCREEN, 0)
            put(Favorites.CELLX, x)
            put(Favorites.CELLY, y)
            put(Favorites.SPANX, if (type == Favorites.ITEM_TYPE_APPWIDGET) 2 else 1)
            put(Favorites.SPANY, if (type == Favorites.ITEM_TYPE_APPWIDGET) 2 else 1)
            put(Favorites.INTENT, "intent:#Intent;component=example.test/$intent;end")
            put(Favorites.APPWIDGET_ID, widgetId)
            put(Favorites.APPWIDGET_PROVIDER, "example.test/.Widget")
            put(Favorites.PROFILE_ID, 0)
            put(Favorites.RANK, if (id == 12) 1 else 0)
        }
        helper.writableDatabase.insertOrThrow(Favorites.TABLE_NAME, null, values)
    }

    private fun rows(helper: DatabaseHelper): List<List<String?>> =
        helper.readableDatabase.rawQuery("SELECT * FROM favorites ORDER BY _id", null).use { c ->
            buildList {
                while (c.moveToNext()) add((0 until c.columnCount).map { c.getString(it) })
            }
        }

    private fun assertMigratedLayout(helper: DatabaseHelper, columns: Int, rows: Int) {
        val db = helper.readableDatabase
        assertEquals(4L, DatabaseUtils.queryNumEntries(db, Favorites.TABLE_NAME))
        val folder = DatabaseUtils.longForQuery(
            db, "SELECT _id FROM favorites WHERE itemType=${Favorites.ITEM_TYPE_FOLDER}", null
        )
        assertEquals(
            2L, DatabaseUtils.queryNumEntries(db, Favorites.TABLE_NAME, "container=$folder")
        )
        db.rawQuery(
            "SELECT intent, rank, profileId FROM favorites WHERE container=$folder ORDER BY rank",
            null,
        ).use { children ->
            for ((rank, name) in listOf("child-one", "child-two").withIndex()) {
                assertTrue(children.moveToNext())
                assertEquals("intent:#Intent;component=example.test/$name;end", children.getString(0))
                assertEquals(rank, children.getInt(1))
                assertEquals(0, children.getInt(2))
            }
            assertFalse(children.moveToNext())
        }
        assertEquals(
            4242L, DatabaseUtils.longForQuery(
                db, "SELECT appWidgetId FROM favorites WHERE itemType=${Favorites.ITEM_TYPE_APPWIDGET}",
                null
            )
        )
        assertEquals(
            0L, DatabaseUtils.queryNumEntries(
                db, Favorites.TABLE_NAME,
                "container=${Favorites.CONTAINER_DESKTOP} AND " +
                    "(cellX < 0 OR cellY < 0 OR cellX + spanX > $columns OR cellY + spanY > $rows)"
            )
        )
        assertEquals(
            0L, DatabaseUtils.longForQuery(
                db,
                "SELECT COUNT(*) FROM favorites a JOIN favorites b " +
                    "ON a._id < b._id AND a.screen = b.screen " +
                    "AND a.container = ${Favorites.CONTAINER_DESKTOP} " +
                    "AND b.container = ${Favorites.CONTAINER_DESKTOP} " +
                    "AND a.cellX < b.cellX + b.spanX AND b.cellX < a.cellX + a.spanX " +
                    "AND a.cellY < b.cellY + b.spanY AND b.cellY < a.cellY + a.spanY",
                null,
            )
        )
    }

    companion object {
        private const val SOURCE = LauncherFiles.LAUNCHER_4_BY_4_DB
        private const val TARGET = LauncherFiles.LAUNCHER_3_BY_3_DB
    }
}
