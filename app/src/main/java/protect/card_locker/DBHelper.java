package protect.card_locker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "Catima.db";
    public static final int ORIGINAL_DATABASE_VERSION = 1;
    public static final int DATABASE_VERSION = 11;

    public static class LoyaltyCardDbGroups
    {
        public static final String TABLE = "groups";
        public static final String ID = "_id";
        public static final String ORDER = "orderId";
    }

    public static class LoyaltyCardDbIds
    {
        public static final String TABLE = "cards";
        public static final String ID = "_id";
        public static final String STORE = "store";
        public static final String EXPIRY = "expiry";
        public static final String BALANCE = "balance";
        public static final String BALANCE_TYPE = "balancetype";
        public static final String NOTE = "note";
        public static final String HEADER_COLOR = "headercolor";
        public static final String HEADER_TEXT_COLOR = "headertextcolor";
        public static final String CARD_ID = "cardid";
        public static final String BARCODE_ID = "barcodeid";
        public static final String BARCODE_TYPE = "barcodetype";
        public static final String STAR_STATUS = "starstatus";
        public static final String LAST_USED = "lastused";
    }

    public static class LoyaltyCardDbIdsGroups
    {
        public static final String TABLE = "cardsGroups";
        public static final String cardID = "cardId";
        public static final String groupID = "groupId";
    }

    public enum LoyaltyCardOrder {
        Alpha,
        LastUsed,
        Expiry
    }

    public enum LoyaltyCardOrderDirection {
        Ascending,
        Descending
    }

    private Context mContext;

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // create table for card groups
        db.execSQL("create table " + LoyaltyCardDbGroups.TABLE + "(" +
                LoyaltyCardDbGroups.ID + " TEXT primary key not null," +
                LoyaltyCardDbGroups.ORDER + " INTEGER DEFAULT '0')");

        // create table for cards
        // Balance is TEXT and not REAL to be able to store a BigDecimal without precision loss
        db.execSQL("create table " + LoyaltyCardDbIds.TABLE + "(" +
                LoyaltyCardDbIds.ID + " INTEGER primary key autoincrement," +
                LoyaltyCardDbIds.STORE + " TEXT not null," +
                LoyaltyCardDbIds.NOTE + " TEXT not null," +
                LoyaltyCardDbIds.EXPIRY + " INTEGER," +
                LoyaltyCardDbIds.BALANCE + " TEXT not null DEFAULT '0'," +
                LoyaltyCardDbIds.BALANCE_TYPE + " TEXT," +
                LoyaltyCardDbIds.HEADER_COLOR + " INTEGER," +
                LoyaltyCardDbIds.CARD_ID + " TEXT not null," +
                LoyaltyCardDbIds.BARCODE_ID + " TEXT," +
                LoyaltyCardDbIds.BARCODE_TYPE + " TEXT," +
                LoyaltyCardDbIds.STAR_STATUS + " INTEGER DEFAULT '0'," +
                LoyaltyCardDbIds.LAST_USED + " INTEGER DEFAULT '0')");

        // create associative table for cards in groups
        db.execSQL("create table " + LoyaltyCardDbIdsGroups.TABLE + "(" +
                LoyaltyCardDbIdsGroups.cardID + " INTEGER," +
                LoyaltyCardDbIdsGroups.groupID + " TEXT," +
                "primary key (" + LoyaltyCardDbIdsGroups.cardID + "," + LoyaltyCardDbIdsGroups.groupID +"))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Upgrade from version 1 to version 2
        if(oldVersion < 2 && newVersion >= 2)
        {
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.NOTE + " TEXT not null default ''");
        }

        // Upgrade from version 2 to version 3
        if(oldVersion < 3 && newVersion >= 3)
        {
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.HEADER_COLOR + " INTEGER");
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.HEADER_TEXT_COLOR + " INTEGER");
        }

        // Upgrade from version 3 to version 4
        if(oldVersion < 4 && newVersion >= 4)
        {
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.STAR_STATUS + " INTEGER DEFAULT '0'");
        }

        // Upgrade from version 4 to version 5
        if(oldVersion < 5 && newVersion >= 5)
        {
            db.execSQL("create table " + LoyaltyCardDbGroups.TABLE + "(" +
                    LoyaltyCardDbGroups.ID + " TEXT primary key not null)");

            db.execSQL("create table " + LoyaltyCardDbIdsGroups.TABLE + "(" +
                    LoyaltyCardDbIdsGroups.cardID + " INTEGER," +
                    LoyaltyCardDbIdsGroups.groupID + " TEXT," +
                    "primary key (" + LoyaltyCardDbIdsGroups.cardID + "," + LoyaltyCardDbIdsGroups.groupID +"))");
        }

        // Upgrade from version 5 to 6
        if(oldVersion < 6 && newVersion >= 6)
        {
            db.execSQL("ALTER TABLE " + LoyaltyCardDbGroups.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbGroups.ORDER + " INTEGER DEFAULT '0'");
        }

        if(oldVersion < 7 && newVersion >= 7)
        {
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.EXPIRY + " INTEGER");
        }

        if(oldVersion < 8 && newVersion >= 8)
        {
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.BALANCE + " TEXT not null DEFAULT '0'");
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.BALANCE_TYPE + " TEXT");
        }

        if(oldVersion < 9 && newVersion >= 9)
        {
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.BARCODE_ID + " TEXT");
        }

        if(oldVersion < 10 && newVersion >= 10)
        {
            // SQLite doesn't support modify column
            // So we need to create a temp column to make barcode type nullable
            // Let's drop header text colour too while we're at it
            // https://www.sqlite.org/faq.html#q11
            db.beginTransaction();

            db.execSQL("CREATE TEMPORARY TABLE tmp (" +
                    LoyaltyCardDbIds.ID + " INTEGER primary key autoincrement," +
                    LoyaltyCardDbIds.STORE + " TEXT not null," +
                    LoyaltyCardDbIds.NOTE + " TEXT not null," +
                    LoyaltyCardDbIds.EXPIRY + " INTEGER," +
                    LoyaltyCardDbIds.BALANCE + " TEXT not null DEFAULT '0'," +
                    LoyaltyCardDbIds.BALANCE_TYPE + " TEXT," +
                    LoyaltyCardDbIds.HEADER_COLOR + " INTEGER," +
                    LoyaltyCardDbIds.CARD_ID + " TEXT not null," +
                    LoyaltyCardDbIds.BARCODE_ID + " TEXT," +
                    LoyaltyCardDbIds.BARCODE_TYPE + " TEXT," +
                    LoyaltyCardDbIds.STAR_STATUS + " INTEGER DEFAULT '0' )");

            db.execSQL("INSERT INTO tmp (" +
                    LoyaltyCardDbIds.ID + " ," +
                    LoyaltyCardDbIds.STORE + " ," +
                    LoyaltyCardDbIds.NOTE + " ," +
                    LoyaltyCardDbIds.EXPIRY + " ," +
                    LoyaltyCardDbIds.BALANCE + " ," +
                    LoyaltyCardDbIds.BALANCE_TYPE + " ," +
                    LoyaltyCardDbIds.HEADER_COLOR + " ," +
                    LoyaltyCardDbIds.CARD_ID + " ," +
                    LoyaltyCardDbIds.BARCODE_ID + " ," +
                    LoyaltyCardDbIds.BARCODE_TYPE + " ," +
                    LoyaltyCardDbIds.STAR_STATUS + ")" +
                    " SELECT " +
                    LoyaltyCardDbIds.ID + " ," +
                    LoyaltyCardDbIds.STORE + " ," +
                    LoyaltyCardDbIds.NOTE + " ," +
                    LoyaltyCardDbIds.EXPIRY + " ," +
                    LoyaltyCardDbIds.BALANCE + " ," +
                    LoyaltyCardDbIds.BALANCE_TYPE + " ," +
                    LoyaltyCardDbIds.HEADER_COLOR + " ," +
                    LoyaltyCardDbIds.CARD_ID + " ," +
                    LoyaltyCardDbIds.BARCODE_ID + " ," +
                    " NULLIF(" + LoyaltyCardDbIds.BARCODE_TYPE + ",'') ," +
                    LoyaltyCardDbIds.STAR_STATUS +
                    " FROM " + LoyaltyCardDbIds.TABLE);

            db.execSQL("DROP TABLE " + LoyaltyCardDbIds.TABLE);

            db.execSQL("create table " + LoyaltyCardDbIds.TABLE + "(" +
                    LoyaltyCardDbIds.ID + " INTEGER primary key autoincrement," +
                    LoyaltyCardDbIds.STORE + " TEXT not null," +
                    LoyaltyCardDbIds.NOTE + " TEXT not null," +
                    LoyaltyCardDbIds.EXPIRY + " INTEGER," +
                    LoyaltyCardDbIds.BALANCE + " TEXT not null DEFAULT '0'," +
                    LoyaltyCardDbIds.BALANCE_TYPE + " TEXT," +
                    LoyaltyCardDbIds.HEADER_COLOR + " INTEGER," +
                    LoyaltyCardDbIds.CARD_ID + " TEXT not null," +
                    LoyaltyCardDbIds.BARCODE_ID + " TEXT," +
                    LoyaltyCardDbIds.BARCODE_TYPE + " TEXT," +
                    LoyaltyCardDbIds.STAR_STATUS + " INTEGER DEFAULT '0' )");

            db.execSQL("INSERT INTO " + LoyaltyCardDbIds.TABLE + "(" +
                    LoyaltyCardDbIds.ID + " ," +
                    LoyaltyCardDbIds.STORE + " ," +
                    LoyaltyCardDbIds.NOTE + " ," +
                    LoyaltyCardDbIds.EXPIRY + " ," +
                    LoyaltyCardDbIds.BALANCE + " ," +
                    LoyaltyCardDbIds.BALANCE_TYPE + " ," +
                    LoyaltyCardDbIds.HEADER_COLOR + " ," +
                    LoyaltyCardDbIds.CARD_ID + " ," +
                    LoyaltyCardDbIds.BARCODE_ID + " ," +
                    LoyaltyCardDbIds.BARCODE_TYPE + " ," +
                    LoyaltyCardDbIds.STAR_STATUS + ")" +
                    " SELECT " +
                    LoyaltyCardDbIds.ID + " ," +
                    LoyaltyCardDbIds.STORE + " ," +
                    LoyaltyCardDbIds.NOTE + " ," +
                    LoyaltyCardDbIds.EXPIRY + " ," +
                    LoyaltyCardDbIds.BALANCE + " ," +
                    LoyaltyCardDbIds.BALANCE_TYPE + " ," +
                    LoyaltyCardDbIds.HEADER_COLOR + " ," +
                    LoyaltyCardDbIds.CARD_ID + " ," +
                    LoyaltyCardDbIds.BARCODE_ID + " ," +
                    LoyaltyCardDbIds.BARCODE_TYPE + " ," +
                    LoyaltyCardDbIds.STAR_STATUS +
                    " FROM tmp");

            db.execSQL("DROP TABLE tmp");

            db.setTransactionSuccessful();
            db.endTransaction();
        }

        if(oldVersion < 11 && newVersion >= 11)
        {
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.LAST_USED + " INTEGER DEFAULT '0'");
        }
    }

    public long insertLoyaltyCard(final String store, final String note, final Date expiry,
                                  final BigDecimal balance, final Currency balanceType,
                                  final String cardId, final String barcodeId,
                                  final CatimaBarcode barcodeType, final Integer headerColor,
                                  final int starStatus, final Long lastUsed)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbIds.STORE, store);
        contentValues.put(LoyaltyCardDbIds.NOTE, note);
        contentValues.put(LoyaltyCardDbIds.EXPIRY, expiry != null ? expiry.getTime() : null);
        contentValues.put(LoyaltyCardDbIds.BALANCE, balance.toString());
        contentValues.put(LoyaltyCardDbIds.BALANCE_TYPE, balanceType != null ? balanceType.getCurrencyCode() : null);
        contentValues.put(LoyaltyCardDbIds.CARD_ID, cardId);
        contentValues.put(LoyaltyCardDbIds.BARCODE_ID, barcodeId);
        contentValues.put(LoyaltyCardDbIds.BARCODE_TYPE, barcodeType != null ? barcodeType.name() : null);
        contentValues.put(LoyaltyCardDbIds.HEADER_COLOR, headerColor);
        contentValues.put(LoyaltyCardDbIds.STAR_STATUS, starStatus);
        contentValues.put(LoyaltyCardDbIds.LAST_USED, lastUsed != null ? lastUsed : Utils.getUnixTime());
        return db.insert(LoyaltyCardDbIds.TABLE, null, contentValues);
    }

    public long insertLoyaltyCard(final SQLiteDatabase db, final String store,
                                  final String note, final Date expiry, final BigDecimal balance,
                                  final Currency balanceType, final String cardId,
                                  final String barcodeId, final CatimaBarcode barcodeType,
                                  final Integer headerColor, final int starStatus,
                                  final Long lastUsed)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbIds.STORE, store);
        contentValues.put(LoyaltyCardDbIds.NOTE, note);
        contentValues.put(LoyaltyCardDbIds.EXPIRY, expiry != null ? expiry.getTime() : null);
        contentValues.put(LoyaltyCardDbIds.BALANCE, balance.toString());
        contentValues.put(LoyaltyCardDbIds.BALANCE_TYPE, balanceType != null ? balanceType.getCurrencyCode() : null);
        contentValues.put(LoyaltyCardDbIds.CARD_ID, cardId);
        contentValues.put(LoyaltyCardDbIds.BARCODE_ID, barcodeId);
        contentValues.put(LoyaltyCardDbIds.BARCODE_TYPE, barcodeType != null ? barcodeType.name() : null);
        contentValues.put(LoyaltyCardDbIds.HEADER_COLOR, headerColor);
        contentValues.put(LoyaltyCardDbIds.STAR_STATUS, starStatus);
        contentValues.put(LoyaltyCardDbIds.LAST_USED, lastUsed != null ? lastUsed : Utils.getUnixTime());
        return db.insert(LoyaltyCardDbIds.TABLE, null, contentValues);
    }

    public long insertLoyaltyCard(final SQLiteDatabase db, final int id, final String store,
                                  final String note, final Date expiry, final BigDecimal balance,
                                  final Currency balanceType, final String cardId,
                                  final String barcodeId, final CatimaBarcode barcodeType,
                                  final Integer headerColor, final int starStatus,
                                  final Long lastUsed)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbIds.ID, id);
        contentValues.put(LoyaltyCardDbIds.STORE, store);
        contentValues.put(LoyaltyCardDbIds.NOTE, note);
        contentValues.put(LoyaltyCardDbIds.EXPIRY, expiry != null ? expiry.getTime() : null);
        contentValues.put(LoyaltyCardDbIds.BALANCE, balance.toString());
        contentValues.put(LoyaltyCardDbIds.BALANCE_TYPE, balanceType != null ? balanceType.getCurrencyCode() : null);
        contentValues.put(LoyaltyCardDbIds.CARD_ID, cardId);
        contentValues.put(LoyaltyCardDbIds.BARCODE_ID, barcodeId);
        contentValues.put(LoyaltyCardDbIds.BARCODE_TYPE, barcodeType != null ? barcodeType.name() : null);
        contentValues.put(LoyaltyCardDbIds.HEADER_COLOR, headerColor);
        contentValues.put(LoyaltyCardDbIds.STAR_STATUS, starStatus);
        contentValues.put(LoyaltyCardDbIds.LAST_USED, lastUsed != null ? lastUsed : Utils.getUnixTime());
        return db.insert(LoyaltyCardDbIds.TABLE, null, contentValues);
    }

    public boolean updateLoyaltyCard(final int id, final String store, final String note,
                                     final Date expiry, final BigDecimal balance,
                                     final Currency balanceType, final String cardId,
                                     final String barcodeId, final CatimaBarcode barcodeType,
                                     final Integer headerColor)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbIds.STORE, store);
        contentValues.put(LoyaltyCardDbIds.NOTE, note);
        contentValues.put(LoyaltyCardDbIds.EXPIRY, expiry != null ? expiry.getTime() : null);
        contentValues.put(LoyaltyCardDbIds.BALANCE, balance.toString());
        contentValues.put(LoyaltyCardDbIds.BALANCE_TYPE, balanceType != null ? balanceType.getCurrencyCode() : null);
        contentValues.put(LoyaltyCardDbIds.CARD_ID, cardId);
        contentValues.put(LoyaltyCardDbIds.BARCODE_ID, barcodeId);
        contentValues.put(LoyaltyCardDbIds.BARCODE_TYPE, barcodeType != null ? barcodeType.name() : null);
        contentValues.put(LoyaltyCardDbIds.HEADER_COLOR, headerColor);
        int rowsUpdated = db.update(LoyaltyCardDbIds.TABLE, contentValues,
                whereAttrs(LoyaltyCardDbIds.ID), withArgs(id));
        return (rowsUpdated == 1);
    }

    public boolean updateLoyaltyCardStarStatus(final int id, final int starStatus)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbIds.STAR_STATUS,starStatus);
        int rowsUpdated = db.update(LoyaltyCardDbIds.TABLE, contentValues,
                whereAttrs(LoyaltyCardDbIds.ID),
                withArgs(id));
        return (rowsUpdated == 1);
    }

    public boolean updateLoyaltyCardLastUsed(final int id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbIds.LAST_USED, System.currentTimeMillis() / 1000);
        int rowsUpdated = db.update(LoyaltyCardDbIds.TABLE, contentValues,
                whereAttrs(LoyaltyCardDbIds.ID),
                withArgs(id));
        return (rowsUpdated == 1);
    }

    public LoyaltyCard getLoyaltyCard(final int id)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.query(LoyaltyCardDbIds.TABLE, null, whereAttrs(LoyaltyCardDbIds.ID), withArgs(id), null, null, null);

        LoyaltyCard card = null;

        if(data.getCount() == 1)
        {
            data.moveToFirst();
            card = LoyaltyCard.toLoyaltyCard(data);
        }

        data.close();

        return card;
    }

    public List<Group> getLoyaltyCardGroups(final int id)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("select * from " + LoyaltyCardDbGroups.TABLE + " g " +
                " LEFT JOIN " + LoyaltyCardDbIdsGroups.TABLE + " ig ON ig." + LoyaltyCardDbIdsGroups.groupID + " = g." + LoyaltyCardDbGroups.ID +
                " where " + LoyaltyCardDbIdsGroups.cardID + "=?" +
                " ORDER BY " + LoyaltyCardDbIdsGroups.groupID, withArgs(id));

        List<Group> groups = new ArrayList<>();

        if (!data.moveToFirst()) {
            data.close();
            return groups;
        }

        groups.add(Group.toGroup(data));

        while (data.moveToNext()) {
            groups.add(Group.toGroup(data));
        }

        data.close();

        return groups;
    }

    public void setLoyaltyCardGroups(final int id, List<Group> groups)
    {
        SQLiteDatabase db = getWritableDatabase();

        // First delete lookup table entries associated with this card
        db.delete(LoyaltyCardDbIdsGroups.TABLE,
                whereAttrs(LoyaltyCardDbIdsGroups.cardID),
                withArgs(id));

        // Then create entries for selected values
        for (Group group : groups) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(LoyaltyCardDbIdsGroups.cardID, id);
            contentValues.put(LoyaltyCardDbIdsGroups.groupID, group._id);
            db.insert(LoyaltyCardDbIdsGroups.TABLE, null, contentValues);
        }
    }

    public void setLoyaltyCardGroups(final SQLiteDatabase db, final int id, List<Group> groups)
    {
        // First delete lookup table entries associated with this card
        db.delete(LoyaltyCardDbIdsGroups.TABLE,
                whereAttrs(LoyaltyCardDbIdsGroups.cardID),
                withArgs(id));

        // Then create entries for selected values
        for (Group group : groups) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(LoyaltyCardDbIdsGroups.cardID, id);
            contentValues.put(LoyaltyCardDbIdsGroups.groupID, group._id);
            db.insert(LoyaltyCardDbIdsGroups.TABLE, null, contentValues);
        }
    }

    public boolean deleteLoyaltyCard(final int id)
    {
        SQLiteDatabase db = getWritableDatabase();
        // Delete card
        int rowsDeleted = db.delete(LoyaltyCardDbIds.TABLE,
                whereAttrs(LoyaltyCardDbIds.ID),
                withArgs(id));

        // And delete lookup table entries associated with this card
        db.delete(LoyaltyCardDbIdsGroups.TABLE,
                whereAttrs(LoyaltyCardDbIdsGroups.cardID),
                withArgs(id));

        // Also wipe card images associated with this card
        try {
            Utils.saveCardImage(mContext, null, id, true);
            Utils.saveCardImage(mContext, null, id, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return (rowsDeleted == 1);
    }

    public Cursor getLoyaltyCardCursor()
    {
        // An empty string will match everything
        return getLoyaltyCardCursor("");
    }

    /**
     * Returns a cursor to all loyalty cards with the filter text in either the store or note.
     *
     * @param filter
     * @return Cursor
     */
    public Cursor getLoyaltyCardCursor(final String filter)
    {
        return getLoyaltyCardCursor(filter, null);
    }

    /**
     * Returns a cursor to all loyalty cards with the filter text in either the store or note in a certain group.
     *
     * @param filter
     * @param group
     * @return Cursor
     */
    public Cursor getLoyaltyCardCursor(final String filter, Group group)
    {
        return getLoyaltyCardCursor(filter, group, LoyaltyCardOrder.Alpha, LoyaltyCardOrderDirection.Ascending);
    }

    /**
     * Returns a cursor to all loyalty cards with the filter text in either the store or note in a certain group sorted as requested.
     *
     * @param filter
     * @param group
     * @param order
     * @return Cursor
     */
    public Cursor getLoyaltyCardCursor(final String filter, Group group, LoyaltyCardOrder order, LoyaltyCardOrderDirection direction) {
        String actualFilter = String.format("%%%s%%", filter);
        String[] selectionArgs = { actualFilter, actualFilter };
        StringBuilder groupFilter = new StringBuilder();
        String limitString = "";

        SQLiteDatabase db = getReadableDatabase();

        if (group != null) {
            List<Integer> allowedIds = getGroupCardIds(group._id);

            // Empty group
            if (!allowedIds.isEmpty()) {
                groupFilter.append("AND (");

                for (int i = 0; i < allowedIds.size(); i++) {
                    groupFilter.append(LoyaltyCardDbIds.ID + " = ").append(allowedIds.get(i));
                    if (i != allowedIds.size() - 1) {
                        groupFilter.append(" OR ");
                    }
                }
                groupFilter.append(") ");
            } else {
                limitString = "LIMIT 0";
            }
        }

        String orderField = getFieldForOrder(order);

        return db.rawQuery("select * from " + LoyaltyCardDbIds.TABLE +
                " WHERE (" + LoyaltyCardDbIds.STORE + "  LIKE ? " +
                " OR " + LoyaltyCardDbIds.NOTE + " LIKE ? )" +
                groupFilter.toString() +
                " ORDER BY " + LoyaltyCardDbIds.STAR_STATUS + " DESC, " +
                " (CASE WHEN " + orderField + " IS NULL THEN 1 ELSE 0 END), " +
                orderField + " COLLATE NOCASE " + getDbDirection(order, direction) + ", " +
                LoyaltyCardDbIds.STORE + " COLLATE NOCASE ASC " +
                limitString, selectionArgs, null);
    }

    public int getLoyaltyCardCount()
    {
        SQLiteDatabase db = getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, LoyaltyCardDbIds.TABLE);
    }

    /**
     * Returns the amount of loyalty cards with the filter text in either the store or note.
     *
     * @param filter
     * @return Integer
     */
    public int getLoyaltyCardCount(String filter)
    {
        String actualFilter = String.format("%%%s%%", filter);

        SQLiteDatabase db = getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, LoyaltyCardDbIds.TABLE,
                LoyaltyCardDbIds.STORE + " LIKE ? " +
                " OR " + LoyaltyCardDbIds.NOTE + " LIKE ? ", withArgs(actualFilter, actualFilter));
    }

    /**
     * Returns a cursor to all groups.
     *
     * @return Cursor
     */
    public Cursor getGroupCursor()
    {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery("select * from " + LoyaltyCardDbGroups.TABLE +
                " ORDER BY " + LoyaltyCardDbGroups.ORDER + " ASC," + LoyaltyCardDbGroups.ID + " COLLATE NOCASE ASC", null, null);
    }

    public List<Group> getGroups() {
        try(Cursor data = getGroupCursor()) {
            List<Group> groups = new ArrayList<>();

            if (!data.moveToFirst()) {
                return groups;
            }

            groups.add(Group.toGroup(data));
            while (data.moveToNext()) {
                groups.add(Group.toGroup(data));
            }

            return groups;
        }
    }

    public void reorderGroups(final List<Group> groups)
    {
        Integer order = 0;
        SQLiteDatabase db = getWritableDatabase();

        for (Group group : groups)
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put(LoyaltyCardDbGroups.ORDER, order);

            db.update(LoyaltyCardDbGroups.TABLE, contentValues,
                    whereAttrs(LoyaltyCardDbGroups.ID),
                    withArgs(group._id));

            order++;
        }
    }

    public Group getGroup(final String groupName)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.query(LoyaltyCardDbGroups.TABLE, null,
                whereAttrs(LoyaltyCardDbGroups.ID), withArgs(groupName), null, null, null);

        Group group = null;
        if(data.getCount() == 1)
        {
            data.moveToFirst();
            group = Group.toGroup(data);
        }
        data.close();

        return group;
    }

    public int getGroupCount()
    {
        SQLiteDatabase db = getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, LoyaltyCardDbGroups.TABLE);
    }

    public List<Integer> getGroupCardIds(final String groupName)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.query(LoyaltyCardDbIdsGroups.TABLE, withArgs(LoyaltyCardDbIdsGroups.cardID),
                whereAttrs(LoyaltyCardDbIdsGroups.groupID), withArgs(groupName), null, null, null);
        List<Integer> cardIds = new ArrayList<>();

        if (!data.moveToFirst()) {
            return cardIds;
        }

        cardIds.add(data.getInt(0));

        while (data.moveToNext()) {
            cardIds.add(data.getInt(0));
        }

        data.close();

        return cardIds;
    }

    public long insertGroup(final String name)
    {
        if (name.isEmpty()) return -1;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbGroups.ID, name);
        contentValues.put(LoyaltyCardDbGroups.ORDER, getGroupCount());
        return db.insert(LoyaltyCardDbGroups.TABLE, null, contentValues);
    }

    public boolean insertGroup(final SQLiteDatabase db, final String name)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbGroups.ID, name);
        contentValues.put(LoyaltyCardDbGroups.ORDER, getGroupCount());
        final long newId = db.insert(LoyaltyCardDbGroups.TABLE, null, contentValues);
        return newId != -1;
    }

    public boolean updateGroup(final String groupName, final String newName)
    {
        if (newName.isEmpty()) return false;

        boolean success = false;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues groupContentValues = new ContentValues();
        groupContentValues.put(LoyaltyCardDbGroups.ID, newName);

        ContentValues lookupContentValues = new ContentValues();
        lookupContentValues.put(LoyaltyCardDbIdsGroups.groupID, newName);

        db.beginTransaction();
        try {
            // Update group name
            int groupsChanged = db.update(LoyaltyCardDbGroups.TABLE, groupContentValues,
                    whereAttrs(LoyaltyCardDbGroups.ID),
                    withArgs(groupName));

            // Also update lookup tables
            db.update(LoyaltyCardDbIdsGroups.TABLE, lookupContentValues,
                    whereAttrs(LoyaltyCardDbIdsGroups.groupID),
                    withArgs(groupName));

            if (groupsChanged == 1) {
                db.setTransactionSuccessful();
                success = true;
            }
        } catch (SQLiteException e) {
        } finally {
            db.endTransaction();
        }

        return success;
    }

    public boolean deleteGroup(final String groupName)
    {
        boolean success = false;

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            // Delete group
            int groupsDeleted = db.delete(LoyaltyCardDbGroups.TABLE,
                    whereAttrs(LoyaltyCardDbGroups.ID),
                    withArgs(groupName));

            // And delete lookup table entries associated with this group
            db.delete(LoyaltyCardDbIdsGroups.TABLE,
                    whereAttrs(LoyaltyCardDbIdsGroups.groupID),
                    withArgs(groupName));

            if (groupsDeleted == 1) {
                db.setTransactionSuccessful();
                success = true;
            }
        } finally {
            db.endTransaction();
        }

        // Reorder after delete to ensure no bad order IDs
        reorderGroups(getGroups());

        return success;
    }

    public int getGroupCardCount(final String groupName)
    {
        SQLiteDatabase db = getReadableDatabase();

        return (int) DatabaseUtils.queryNumEntries(db, LoyaltyCardDbIdsGroups.TABLE,
                whereAttrs(LoyaltyCardDbIdsGroups.groupID), withArgs(groupName));
    }

    private String whereAttrs(String... attrs) {
        if (attrs.length == 0) {
            return null;
        }
        StringBuilder whereClause = new StringBuilder(attrs[0]).append("=?");
        for (int i = 1; i < attrs.length; i++) {
            whereClause.append(" AND ").append(attrs[i]).append("=?");
        }
        return whereClause.toString();
    }

    private String[] withArgs(Object... object) {
        return Arrays.stream(object)
                .map(String::valueOf)
                .toArray(String[]::new);
    }

    private String getFieldForOrder(LoyaltyCardOrder order) {
        if (order == LoyaltyCardOrder.Alpha) {
            return LoyaltyCardDbIds.STORE;
        }

        if (order == LoyaltyCardOrder.LastUsed) {
            return LoyaltyCardDbIds.LAST_USED;
        }

        if (order == LoyaltyCardOrder.Expiry) {
            return LoyaltyCardDbIds.EXPIRY;
        }

        throw new IllegalArgumentException("Unknown order " + order);
    }

    private String getDbDirection(LoyaltyCardOrder order, LoyaltyCardOrderDirection direction) {
        if (order == LoyaltyCardOrder.LastUsed) {
            // We want the default sorting to put the most recently used first
            return direction == LoyaltyCardOrderDirection.Descending ? "ASC" : "DESC";
        }

        return direction == LoyaltyCardOrderDirection.Ascending ? "ASC" : "DESC";
    }
}
