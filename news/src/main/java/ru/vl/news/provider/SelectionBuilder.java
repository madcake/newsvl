package ru.vl.news.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SelectionBuilder {
	private String mTable = null;
    private Map<String, String> mProjectionMap = new HashMap<String, String>();
    private StringBuilder mSelection = new StringBuilder();
    private ArrayList<String> mSelectionArgs = new ArrayList<String>();
	private StringBuilder mGroup = new StringBuilder();
	private String mOrder;
    
    public SelectionBuilder table(String table) {
    	mTable = table;
    	return this;
    }
    
    public SelectionBuilder mapToTable(String table, String column) {
    	mProjectionMap.put(column, table + "." + column);
    	return this;
    }
    
    public SelectionBuilder map(String fromColumn, String toClause) {
    	mProjectionMap.put(fromColumn, toClause + " AS " + fromColumn);
    	return this;
    }
    
    public SelectionBuilder where(String selection, String... selectionArgs) {
    	if (TextUtils.isEmpty(selection)) {
    		if (selectionArgs != null && selectionArgs.length > 0) {
    			throw new IllegalArgumentException();
    		}
    		
    		return this;
    	}
    	
    	if (mSelection.length() > 0) {
    		mSelection.append(" AND ");
    	}
    	
    	mSelection.append("(").append(selection).append(")");
    	
    	if (selectionArgs != null) {
    		Collections.addAll(mSelectionArgs, selectionArgs);
    	}
    	
    	return this;
    }
    
    public String[] getSelectionArgs() {
    	return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
    }

    public SelectionBuilder group(String group) {
    	if (mGroup .length() > 0) {
    		mGroup.append(",");
    	}
    	mGroup.append(group);
    	return this;
    }

    public SelectionBuilder order(String order) {
    	mOrder = order;
    	return this;
    }
    
    private void mapColumns(String[] columns) {
    	for (int i = 0; i < columns.length; i++) {
    		final String target = mProjectionMap.get(columns[i]);
            
    		if (target != null) {
                columns[i] = target;
            }
        }
    }
    
    private String table() {
    	if (TextUtils.isEmpty(mTable)) {
    		throw new IllegalArgumentException("Selection hasn't table");
    	}
    	return mTable;
    }
    
    public Cursor query(SQLiteDatabase db, String[] columns) {
    	return query(db, columns, mOrder, "1");
    }

    public Cursor query(SQLiteDatabase db, String[] columns, String limit) {
    	return query(db, columns, mOrder, limit);
    }

    public Cursor query(SQLiteDatabase db, String[] columns, String orderBy, String limit) {
        if (columns != null) {
        	mapColumns(columns);
        }
        return db.query(table(), columns, mSelection.toString(), getSelectionArgs(),
			mGroup.toString(), null, orderBy, limit);
    }

	public int update(SQLiteDatabase db, ContentValues values) {
		return db.update(mTable, values, mSelection.toString(), getSelectionArgs());
	}

	public int delete(SQLiteDatabase db) {
		return db.delete(mTable, mSelection.toString(), getSelectionArgs());
	}
}
