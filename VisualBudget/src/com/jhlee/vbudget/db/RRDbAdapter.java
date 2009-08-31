package com.jhlee.vbudget.db;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jhlee.vbudget.util.RRUtil;

public class RRDbAdapter {

	private static final String LOG = "RRDbAdapter";

	private static final int DB_VERSION = 16;

	/* KEYS for RECEIPT TABLE */
	public static final String KEY_RECEIPT_IMG_FILE = "img_file";
	public static final String KEY_RECEIPT_SMALL_IMG_FILE = "small_img_file";
	public static final String KEY_RECEIPT_TAKEN_DATE = "taken_date";
	public static final String KEY_RECEIPT_TAKEN_DATE_AS_STRING = "taken_date_as_string";
	public static final String KEY_RECEIPT_TAKEN_DAY_OF_WEEK = "taken_day_of_week";
	public static final String KEY_RECEIPT_TAKEN_DAY_OF_MONTH = "taken_day_of_month";
	public static final String KEY_RECEIPT_TOTAL = "total";
	public static final String KEY_RECEIPT_BUDGET_ID = "budget_id";

	/* KEYS for TAG SOURCE TABLE */
	public static final String KEY_TAG_SOURCE_ID = "_id";
	public static final String KEY_TAG_SOURCE_TAG = "tag_name";

	/* KEYS for PHOTO TAG TABLE */
	public static final String KEY_PHOTO_TAG_ID = "_id";
	public static final String KEY_PHOTO_TAG_TAG = "tag_name";
	public static final String KEY_PHOTO_TAG_RECEIPT_ID = "receipt_id";
	public static final int COL_PHOTO_TAG_TAG = 1;

	/* KEYS for BUDGET TABLE */
	public static final String KEY_BUDGET_ID = "_id";
	public static final String KEY_BUDGET_YEAR = "year";
	public static final String KEY_BUDGET_MONTH = "month";
	public static final String KEY_BUDGET_NAME = "budget_name";
	public static final String KEY_BUDGET_AMOUNT = "budget_amount";
	public static final String KEY_BUDGET_BALANCE = "budget_balance";

	/* COLS for BUDGET TABLE */
	public static final int COL_BUDGET_ID = 0;
	public static final int COL_BUDGET_YEAR = 1;
	public static final int COL_BUDGET_MONTH = 2;
	public static final int COL_BUDGET_NAME = 3;
	public static final int COL_BUDGET_AMOUNT = 4;
	public static final int COL_BUDGET_BALANCE = 5;

	public static final int COL_BUDGET_MONTH_ITEM_COUNT = 3;
	public static final int COL_BUDGET_MONTH_AMOUNT_SUM = 4;
	public static final int COL_BUDGET_MONTH_BALANCE_SUM = 5;
	public static final int COL_BUDGET_MONTH_MAX_AMOUNT = 6;
	public static final int COL_BUDGET_MONTH_MIN_AMOUNT = 7;

	private static final String DB_NAME = "RRDB";

	private static final String TABLE_RECEIPT = "receipt";
	private static final String TABLE_MARKER = "marker";
	private static final String TABLE_TAG_SOURCE = "tag_source";
	private static final String TABLE_PHOTO_TAG = "photo_tag";
	private static final String TABLE_BUDGET = "budget";
	private static final String TABLE_DEFAULT_BUDGET_NAMES = "budget_names";
	private static final String RECEIPT_TABLE_CREATE_SQL = "CREATE TABLE receipt("
			+ " _id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ " img_file TEXT NOT NULL,"
			+ " small_img_file TEXT NOT NULL,"
			+ " taken_date INTEGER NOT NULL,"
			+ " taken_date_as_string TEXT NOT NULL,"
			+ " taken_day_of_week INTEGER NOT NULL,"
			+ " taken_day_of_month INTEGER NOT NULL,"
			+ " geo_coding TEXT, "
			+ " total INTEGER NOT NULL,"
			+ " budget_id INTEGER"
			+ " sync_id INTEGER);";
	private static final String MARKER_TABLE_CREATE_SQL = "CREATE TABLE marker("
			+ " marker_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ " rid INTEGER NOT NULL, "
			+ " marker_name TEXT,"
			+ " marker_type INTEGER NOT NULL,"
			+ " x INTEGER NOT NULL, "
			+ " y INTEGER NOT NULL, "
			+ " width INTEGER, "
			+ " height INTEGER);";

	private static final String BUDGET_TABLE_CREATE_SQL = "CREATE TABLE budget("
			+ " _id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ " year INTEGER NOT NULL, "
			+ " month INTEGER NOT NULL, "
			+ " budget_name TEXT NOT NULL,"
			+ " budget_amount INTEGER NOT NULL,"
			+ " budget_balance INTEGER NOT NULL);";

	/* The table maintains tags which are set by user for specified receipt */
	private static final String PHOTO_TAGS_TABLE_CREATE_SQL = "CREATE TABLE photo_tag("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "tag_name TEXT NOT NULL," + "receipt_id INTEGER NOT NULL);";

	private static final String TAG_SOURCE_TABLE_CREATE_SQL = "CREATE TABLE tag_source("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "tag_name TEXT NOT NULL" + ");";

	private static final String DEFAULT_BUDGET_NAME_TABLE_CREATE_SQL = "CREATE TABLE budget_names("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "budget_name TEXT NOT NULL);";

	private static final String TAG = "RRDbAdapter";

	private static final long TAG_STRING_FORMAT_MULTI_LINE = 1;
	private static final long TAG_STRING_FORMAT_COMMA_SEP = 2;
	private static final long NULL_BALANCE = 0x7FFFFFFF;
	public static final long NULL_BUDGET_ID = -1;

	private DbHelper mDbHelper;
	private SQLiteDatabase mDb;
	private Activity mOwnerActivity;

	/** CTOR */
	public RRDbAdapter(Context ctx) {
		mDbHelper = new DbHelper(ctx);
		mDb = mDbHelper.getWritableDatabase();
	}

	/*
	 * Set owner
	 */
	public void setOwner(Activity activity) {
		mOwnerActivity = activity;
	}

	/** Insert receipt to database */
	public long insertReceipt(String imagePath, String smallImagePath) {
		ContentValues vals = new ContentValues();
		vals.put(KEY_RECEIPT_IMG_FILE, imagePath);
		vals.put(KEY_RECEIPT_SMALL_IMG_FILE, smallImagePath);

		/** Format current date/time */
		Calendar today = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
		long todayInMillis = today.getTimeInMillis();
		/* Insert date information */
		vals.put(KEY_RECEIPT_TAKEN_DATE, todayInMillis);
		vals.put(KEY_RECEIPT_TAKEN_DATE_AS_STRING, RRUtil
				.formatGMTCalendar(todayInMillis));
		vals
				.put(KEY_RECEIPT_TAKEN_DAY_OF_WEEK, today
						.get(Calendar.DAY_OF_WEEK));
		vals.put(KEY_RECEIPT_TAKEN_DAY_OF_MONTH, today
				.get(Calendar.DAY_OF_MONTH));

		/**
		 * Set total money as zero. 0 means N/A.
		 */
		vals.put(KEY_RECEIPT_TOTAL, 0);

		return mDb.insert(TABLE_RECEIPT, null, vals);
	}

	/*
	 * Delete expense
	 */
	public boolean deleteExpense(long id) {
		/*
		 * Budget back
		 */
		Cursor c = queryReceipt(id);
		c.moveToFirst();
		if (c.getCount() < 1)
			return false;

		long total = c.getLong(c.getColumnIndex(KEY_RECEIPT_TOTAL));
		long budgetIdColIndex = c.getColumnIndex(KEY_RECEIPT_BUDGET_ID);
		if (c.isNull((int) budgetIdColIndex) == false) {
			long budgetId = c.getLong((int) budgetIdColIndex);
			if (budgetId != NULL_BUDGET_ID) {
				returnTransactionToBudget(id, total, budgetId);
			}
		}

		/*
		 * Delete data from table
		 */
		int cnt = mDb.delete(TABLE_RECEIPT, "_id=" + id, null);
		if (cnt != 1)
			return false;

		return true;
	}

	public long newTransaction() {
		return insertReceipt("NT", "NTS");
	}

	/** Query receipt by daily */
	public Cursor queryReceiptByDaily() {
		Cursor c = mDb.query(TABLE_RECEIPT, new String[] { "_id",
				"COUNT(*) AS CNT", "SUM(TOTAL) AS TOTAL_EXPENSE",
				KEY_RECEIPT_IMG_FILE, KEY_RECEIPT_TAKEN_DATE }, null, null,
				KEY_RECEIPT_TAKEN_DATE_AS_STRING, null, KEY_RECEIPT_TAKEN_DATE);
		if (c != null) {
			c.moveToFirst();
			mOwnerActivity.startManagingCursor(c);
		}

		return c;
	}

	/**
	 * Query receipt information
	 * 
	 * @param transId
	 *            Receipt id
	 * @return
	 */
	public Cursor queryReceipt(long transId) {
		Cursor c = mDb.query("receipt LEFT OUTER JOIN budget ON receipt.budget_id=budget._id",
				new String[] { "*", "budget.budget_name as budget_name" }, "receipt._id=" + transId, null, null,
				null, null);
		if (c != null) {
			c.moveToFirst();
			mOwnerActivity.startManagingCursor(c);
		}

		return c;
	}

	/**
	 * Query all receipts
	 * 
	 * @return Cursor
	 */
	public Cursor queryAllReceipts() {
		Cursor c = mDb.query(TABLE_RECEIPT, null, null, null, null, null,
				KEY_RECEIPT_TAKEN_DATE);
		if (c != null) {
			c.moveToFirst();
			mOwnerActivity.startManagingCursor(c);
		}
		return c;
	}

	/**
	 * Query all receipts
	 * 
	 * @return Cursor
	 */
	public Cursor queryAllReceiptsWithBudgetName() {
		// "select receipt.*, budget._id, budget.budget_name from receipt LEFT OUTER JOIN budget ON receipt.budget_id=budget._id;"
		Cursor c = mDb
				.query(
						"receipt LEFT OUTER JOIN budget ON receipt.budget_id=budget._id",
						new String[] { "*", "budget.budget_name as budget_name" },
						null, null, null, null, KEY_RECEIPT_TAKEN_DATE);
		if (c != null) {
			c.moveToFirst();
			mOwnerActivity.startManagingCursor(c);
		}
		return c;
	}

	/**
	 * Update total money
	 * 
	 * @param rid
	 * @param dollars
	 * @param cents
	 */
	public void updateTotalMoney(long rid, int dollars, int cents) {
		int encoded = dollars * 100 + cents;
		ContentValues vals = new ContentValues();
		vals.put(KEY_RECEIPT_TOTAL, encoded);
		int numRows = mDb.update(TABLE_RECEIPT, vals, "_id="
				+ Long.toString(rid), null);
		if (numRows != 1) {
			Log.e(TAG, "Unable to update row:rid=" + Long.toString(rid));
		}
	}

	/*
	 * Update date
	 */
	public boolean updateDate(Cursor cursor, long millis) {
		ContentValues vals = new ContentValues();

		Calendar tmpCalendar = new GregorianCalendar(new SimpleTimeZone(0,
				"GMT"));
		tmpCalendar.clear();
		tmpCalendar.setTimeInMillis(millis);

		/* Insert date to DB */
		vals.put(KEY_RECEIPT_TAKEN_DATE, tmpCalendar.getTimeInMillis());
		vals.put(KEY_RECEIPT_TAKEN_DATE_AS_STRING, RRUtil
				.formatGMTCalendar(tmpCalendar.getTimeInMillis()));
		vals.put(KEY_RECEIPT_TAKEN_DAY_OF_WEEK, tmpCalendar
				.get(Calendar.DAY_OF_WEEK));
		vals.put(KEY_RECEIPT_TAKEN_DAY_OF_MONTH, tmpCalendar
				.get(Calendar.DAY_OF_MONTH));

		/* Assume 0th index is id */
		int rid = cursor.getInt(0);

		/* Update db */
		int numRows = mDb.update(TABLE_RECEIPT, vals, "_id="
				+ Integer.toString(rid), null);
		if (numRows != 1) {
			Log.e(TAG, "Unable to update row for date:rid="
					+ Integer.toString(rid));
			return false;
		}

		return true;
	}

	/*
	 * Query all tags from TAG SOURCE Sort by tag name
	 */
	public Cursor queryAllTags() {
		return mDb.query(TABLE_TAG_SOURCE, null, null, null, null, null,
				"tag_name");
	}

	/*
	 * Create tag into TAG SOURCE
	 */
	public boolean createTag(String tagName) {
		tagName = tagName.toLowerCase();

		/* Check duplication */
		if (-1 != findTag(tagName)) {
			Log.v(TAG, "Tag already exists:tagName=" + tagName);
			return true;
		}

		ContentValues vals = new ContentValues();
		vals.put(KEY_TAG_SOURCE_TAG, tagName);
		long rowId = mDb.insert(TABLE_TAG_SOURCE, null, vals);
		return (rowId != -1) ? true : false;
	}

	/*
	 * Find tag within TAG SOURCE
	 */
	public long findTag(String tagName) {
		tagName = tagName.toLowerCase();

		Cursor cursor = null;
		try {
			cursor = mDb.query(TABLE_TAG_SOURCE, null, "tag_name='" + tagName
					+ "'", null, null, null, null);
		} catch (SQLiteException err) {
			err.printStackTrace();
			Log.e(TAG, "Query is failed: tagName=" + tagName);
			return -1;
		}

		if (null == cursor)
			return -1;

		long result = -1;
		if (cursor.getCount() == 1) {
			/* First column is id */
			cursor.moveToFirst();
			result = cursor.getInt(0);
		}

		cursor.close();
		cursor = null;
		return result;
	}

	public boolean addTagToReceipt(long receiptId, String tagName) {
		tagName = tagName.toLowerCase();

		/* Check dup. */
		if (true == doesReceiptHaveTag(receiptId, tagName))
			return true;

		ContentValues vals = new ContentValues();
		vals.put(KEY_PHOTO_TAG_TAG, tagName);
		vals.put(KEY_PHOTO_TAG_RECEIPT_ID, receiptId);
		long id = mDb.insert(TABLE_PHOTO_TAG, null, vals);
		if (id == -1) {
			Log.e(TAG, "Unable to add tag to receipt:tag=" + tagName);
			return false;
		}

		return true;

	}

	/*
	 * Check given receipt has specified tag.
	 */
	public boolean doesReceiptHaveTag(long receiptId, String tagName) {
		tagName = tagName.toLowerCase();

		boolean bresult = true;
		Cursor cursor = mDb.query(TABLE_PHOTO_TAG, null, "tag_name='" + tagName
				+ "' and receipt_id=" + Long.toString(receiptId), null, null,
				null, null);
		if (null == cursor)
			bresult = false;
		else {
			if (1 != cursor.getCount())
				bresult = false;

			cursor.close();
		}

		return bresult;
	}

	/*
	 * Remove tag from given receipt
	 */
	public boolean removeTagFromReceipt(long receiptId, String tagName) {
		tagName = tagName.toLowerCase();

		if (false == doesReceiptHaveTag(receiptId, tagName)) {
			/* We have no item to delete */
			Log.v(TAG, "No item is found within photo tag db:tagName="
					+ tagName);
			return false;
		}

		int cnt = mDb.delete(TABLE_PHOTO_TAG, "tag_name='" + tagName
				+ "' and receipt_id=" + Long.toString(receiptId), null);
		if (cnt == 0) {
			Log.v(TAG, "Unable to delete tag from photo:tagName=" + tagName);
			return false;
		}

		return true;
	}

	public String queryReceiptTagsAsMultiLineString(long receiptId) {
		return queryReceiptTagsAsString(receiptId, TAG_STRING_FORMAT_MULTI_LINE);
	}

	/*
	 * Query receipt tags as one line string.
	 */
	public String queryReceiptTagsAsOneString(long receiptId) {
		return queryReceiptTagsAsString(receiptId, TAG_STRING_FORMAT_COMMA_SEP);
	}

	private String queryReceiptTagsAsString(long receiptId, long format) {
		Cursor cursor = mDb.query(TABLE_PHOTO_TAG, null, "receipt_id="
				+ Long.toString(receiptId), null, null, null, "tag_name");
		if (null == cursor)
			return "";

		if (cursor.getCount() < 1) {
			cursor.close();
			return "";
		}

		StringBuilder sb = new StringBuilder();
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			sb.append(cursor.getString(COL_PHOTO_TAG_TAG));
			if (false == cursor.isLast()) {
				if (format == TAG_STRING_FORMAT_MULTI_LINE)
					sb.append("\n");
				else if (format == TAG_STRING_FORMAT_COMMA_SEP)
					sb.append(",");
			}
			cursor.moveToNext();
		}

		cursor.close();
		cursor = null;
		String tagStr = sb.toString();
		sb = null;
		return tagStr;
	}

	public long getMaxExpenseAmongEachDays() {
		Cursor cursor = mDb
				.query(
						"(select max(total) as accum from receipt group by taken_date_as_string)",
						new String[] { "max(accum)" }, null, null, null, null,
						null, null);
		if (null == cursor)
			return 0;

		long val = 0;
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			val = cursor.getLong(0);
		}
		cursor.close();
		return val;
	}

	public Cursor queryExpenseDayByDay() {
		Cursor cursor = mDb.query(TABLE_RECEIPT, new String[] { "taken_date",
				"sum(total)" }, null, null, KEY_RECEIPT_TAKEN_DATE_AS_STRING,
				null, KEY_RECEIPT_TAKEN_DATE);
		if (cursor != null) {
			cursor.moveToFirst();
			mOwnerActivity.startManagingCursor(cursor);
		}
		return cursor;
	}

	public Cursor queryExpenseDayOfWeek() {
		Cursor cursor = mDb.query(TABLE_RECEIPT, new String[] {
				"taken_day_of_week", "sum(total)" }, null, null,
				KEY_RECEIPT_TAKEN_DAY_OF_WEEK, null,
				KEY_RECEIPT_TAKEN_DAY_OF_WEEK);

		if (cursor != null) {
			cursor.moveToFirst();
			mOwnerActivity.startManagingCursor(cursor);
		}
		return cursor;
	}

	public Cursor queryMostExpensiveExpense() {
		Cursor cursor = mDb.query(TABLE_RECEIPT, null,
				"total = (select max(total) from receipt)", null, null, null,
				null);
		if (cursor != null) {
			cursor.moveToFirst();
			mOwnerActivity.startManagingCursor(cursor);
		}
		return cursor;
	}

	/*
	 * Query latest expense id
	 */
	public int queryLatestExpenseId() {
		Cursor c = mDb.query(TABLE_RECEIPT, new String[] { "_id" }, null, null,
				null, null, "taken_date desc");
		if (null == c) {
			return -1;
		}
		c.moveToFirst();
		int expenseId = c.getInt(0);
		c.close();
		return expenseId;
	}

	public Cursor queryAllBudgetItems() {
		Cursor c = mDb.query(TABLE_BUDGET, null, null, null, null, null,
				"year, month, budget_name");
		if (c != null) {
			c.moveToFirst();
			mOwnerActivity.startManagingCursor(c);
		}
		return c;
	}

	public Cursor queryMonthBudgets() {
		Cursor c = mDb.query(TABLE_BUDGET, new String[] { "_id", "year",
				"month", "count(_id)", "sum(budget_amount)",
				"sum(budget_balance)", "max(budget_amount)",
				"max(budget_balance)" }, null, null, "year, month", null,
				"year, month");
		return c;
	}

	public Cursor queryCurrentMonthBudget() {
		Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;

		Cursor c = mDb.query(TABLE_BUDGET, new String[] { "_id", "year",
				"month", "count(_id)", "sum(budget_amount)",
				"sum(budget_balance)", "max(budget_amount)",
				"max(budget_balance)" },
				"year=" + year + " and month=" + month, null, "year, month",
				null, "year, month");
		c.moveToFirst();
		mOwnerActivity.startManagingCursor(c);
		return c;
	}

	public Cursor queryCurrentMonthBudgetItems() {
		Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;

		Cursor c = mDb.query(TABLE_BUDGET, null, "year=" + year + " and month="
				+ month, null, null, null, "budget_name");
		c.moveToFirst();
		mOwnerActivity.startManagingCursor(c);
		return c;
	}

	public boolean insertBudgetItem(int year, int month, String name,
			long amount) {
		ContentValues vals = new ContentValues();
		vals.put("year", year);
		vals.put("month", month);
		vals.put("budget_name", name);
		vals.put("budget_amount", amount);
		vals.put("budget_balance", amount);

		long rowid = mDb.insert(TABLE_BUDGET, null, vals);
		if (-1 == rowid) {
			Log.e(TAG, "Unable to insert budget");
			return false;
		}
		return true;
	}

	public boolean removeBudgetItem(long id) {
		int rowCnt = mDb.delete(TABLE_BUDGET, "_id=" + id, null);
		if (rowCnt != 1) {
			Log.e(LOG, "Unable to delete budget");
			return false;
		}

		return true;
	}

	/*
	 * Update budget item
	 */
	public boolean updateBudgetItem(int year, int month, String budgetName,
			long budgetAmount) {
		ContentValues vals = new ContentValues();
		vals.put("budget_name", budgetName);
		vals.put("budget_amount", budgetAmount);

		String whereStr = makeWhereForBudgetFinding(year, month, budgetName);

		int rowCnt = mDb.update(TABLE_BUDGET, vals, whereStr, null);
		if (1 != rowCnt) {
			Log.e(TAG, "Unable to update budget:where=" + whereStr);
			return false;
		}
		return true;
	}

	/*
	 * Find budget item
	 */
	public long findBudgetItem(int year, int month, String budgetName) {
		Cursor c = mDb.query(TABLE_BUDGET, new String[] { "_id" },
				makeWhereForBudgetFinding(year, month, budgetName), null, null,
				null, null);
		if (c.getCount() == 0)
			return -1;

		c.moveToFirst();
		long id = c.getLong(0);
		c.close();
		return id;
	}

	/*
	 * Make where statement to find budget item
	 */
	private String makeWhereForBudgetFinding(int year, int month,
			String budgetName) {
		StringBuilder sb = new StringBuilder();
		sb.append("year=");
		sb.append(year);
		sb.append(" and month=");
		sb.append(month);
		sb.append(" and budget_name='");
		sb.append(budgetName);
		sb.append("'");
		String whereStr = sb.toString();
		return whereStr;
	}

	/*
	 * Query all default budget names
	 */
	public Cursor queryAllDefaultBudgetNames() {
		Cursor cursor = mDb.query(TABLE_DEFAULT_BUDGET_NAMES,
				new String[] { "budget_name" }, null, null, null, null,
				"budget_name");
		cursor.moveToFirst();
		mOwnerActivity.startManagingCursor(cursor);
		return cursor;
	}

	/*
	 * Make transaction from budget
	 */
	public boolean makeTransactionFromBudget(long transId, long money,
			long budgetId) {
		long balance = queryBudgetBalance(budgetId);
		if (NULL_BALANCE == balance)
			return false;

		balance -= money;
		if (false == updateBudgetBalance(budgetId, balance))
			return false;

		return updateTransactionBudget(transId, budgetId);
	}

	/*
	 * Return previous transaction to budget
	 */
	public boolean returnTransactionToBudget(long transId, long money,
			long budgetId) {
		long balance = queryBudgetBalance(budgetId);
		if (NULL_BALANCE == balance)
			return false;

		balance += money;
		if (false == updateBudgetBalance(budgetId, balance))
			return false;

		return updateTransactionBudget(transId, NULL_BUDGET_ID);
	}

	/*
	 * Udpate transaction budget
	 */
	private boolean updateTransactionBudget(long transId, long budgetId) {
		ContentValues val = new ContentValues();
		val.put(KEY_RECEIPT_BUDGET_ID, budgetId);
		int rowCnt = mDb.update(TABLE_RECEIPT, val, "_id=" + transId, null);
		if (rowCnt != -1) {
			Log
					.e(TAG, "Unable to update transaction budget:transId="
							+ transId);
			return false;
		}
		return true;
	}

	/*
	 * Update budget balance
	 */
	private boolean updateBudgetBalance(long budgetId, long balance) {
		ContentValues val = new ContentValues();
		val.put(KEY_BUDGET_BALANCE, balance);
		int rowCnt = mDb.update(TABLE_BUDGET, val, "_id=" + budgetId, null);
		if (rowCnt != 1) {
			Log.e(TAG, "Unable to update budget blance:budgetId=" + budgetId
					+ ", budgetBalance=" + balance);
			return false;
		}
		return true;
	}

	/*
	 * Get budget balance
	 */
	private long queryBudgetBalance(long budgetId) {
		Cursor c = mDb.query(TABLE_BUDGET, new String[] { "budget_balance" },
				"_id=" + budgetId, null, null, null, null);
		if (c.getCount() == 0)
			return NULL_BALANCE;
		c.moveToFirst();
		long balance = c.getLong(0);
		c.close();
		return balance;
	}

	public boolean changeBudget(long transId, int year, int month,
			String newBudgetName) {
		long curBudgetId = findBudgetItem(year, month, newBudgetName);
		if (-1 == curBudgetId) {
			Log.e(TAG, "Unable to find budget");
			return false;
		}

		Cursor c = queryReceipt(transId);
		if (c == null || c.getCount() != 1) {
			Log.e(TAG, "No expense data is found:transID=" + transId);
			return false;
		}

		int expenseAmountCol = c.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL);
		int budgetIdCol = c.getColumnIndex(RRDbAdapter.KEY_RECEIPT_BUDGET_ID);
		if (false == c.isNull(budgetIdCol)) {
			/*
			 * Previous linked budget item is found If previous budget id is -1,
			 * then the value which the program set to mark deleted budget id.
			 * 
			 * So -1 is considered as NULL case.
			 */
			long budgetId = c.getLong(budgetIdCol);
			if (budgetId != RRDbAdapter.NULL_BUDGET_ID) {
				returnTransactionToBudget(transId, c.getLong(expenseAmountCol),
						budgetId);
			}
		}

		/* Make transaction */
		long expenseAmount = c.getLong(expenseAmountCol);
		makeTransactionFromBudget(transId, expenseAmount, curBudgetId);

		return true;
	}

	/*
	 * Check whether budget is used
	 */
	public boolean isBudgetItemUsed(int year, int month, String budgetName) {
		return false;
	}

	/**
	 * The class maintains database and manages its version.
	 * 
	 * @author jhlee
	 */
	private static class DbHelper extends SQLiteOpenHelper {
		public DbHelper(Context ctx) {
			super(ctx, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			/*
			 * Later sync feature is finalized, we'll update it. final String
			 * SYNC_TABLE_CREATE_SQL = "";
			 */
			db.execSQL(RECEIPT_TABLE_CREATE_SQL);
			db.execSQL(MARKER_TABLE_CREATE_SQL);
			db.execSQL(PHOTO_TAGS_TABLE_CREATE_SQL);
			db.execSQL(TAG_SOURCE_TABLE_CREATE_SQL);
			db.execSQL(BUDGET_TABLE_CREATE_SQL);
			db.execSQL(DEFAULT_BUDGET_NAME_TABLE_CREATE_SQL);

			/*
			 * Add default tags.
			 */
			String[] defaultTags = new String[] { "Auto & Transport",
					"Gas & Fuel", "Parking", "Public Transportation",
					"Home Phone", "Internet", "Mobile Phone", "Television",
					"Utilities", "Office Supplies", "Printing", "Shipping",
					"Books", "Arts", "Movies", "DVD", "Music", "Newspaper",
					"Magazine", "ATM Fee", "Bank Fee", "Finance Charge",
					"Late Fee", "Service Fee", "Alcohol", "Bar", "Coffee",
					"Fast Fodd", "Groceries", "Restaurants", "Gift", "Gym",
					"Pharmacy", "Mortage", "Rent", "Toy", "Hair", "Laundry",
					"Spa", "Massage", "Clothing", "Electronics", "Software",
					"Hobbies", "Air Travel", "Hotel", "Taxi", "Rental Car",
					"Vacation" };
			ContentValues val = new ContentValues();
			for (int i = defaultTags.length - 1; i >= 0; --i) {
				val.put("tag_name", defaultTags[i]);
				db.insert(TABLE_TAG_SOURCE, null, val);
				val.clear();
			}

			/*
			 * Set up budget name. We share tag name and budget
			 */

			for (int i = defaultTags.length - 1; i >= 0; --i) {
				val.put("budget_name", defaultTags[i]);
				db.insert(TABLE_DEFAULT_BUDGET_NAMES, null, val);
				val.clear();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/** Drop table first */
			Log.v(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion);
			db.execSQL("DROP TABLE IF EXISTS receipt");
			db.execSQL("DROP TABLE IF EXISTS marker");
			db.execSQL("DROP TABLE IF EXISTS photo_tag");
			db.execSQL("DROP TABLE IF EXISTS tag_source");
			db.execSQL("DROP TABLE IF EXISTS budget");
			db.execSQL("DROP TABLE IF EXISTS budget_names");

			this.onCreate(db);
		}
	}

}
