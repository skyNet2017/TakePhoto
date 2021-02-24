package com.hss01248.media.mymediastore.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "BASE_MEDIA_FOLDER_INFO".
*/
public class BaseMediaFolderInfoDao extends AbstractDao<BaseMediaFolderInfo, String> {

    public static final String TABLENAME = "BASE_MEDIA_FOLDER_INFO";

    /**
     * Properties of entity BaseMediaFolderInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Name = new Property(0, String.class, "name", false, "NAME");
        public final static Property Cover = new Property(1, String.class, "cover", false, "COVER");
        public final static Property PathOrUri = new Property(2, String.class, "pathOrUri", false, "PATH_OR_URI");
        public final static Property PathAndType = new Property(3, String.class, "pathAndType", true, "PATH_AND_TYPE");
        public final static Property Count = new Property(4, int.class, "count", false, "COUNT");
        public final static Property FileSize = new Property(5, long.class, "fileSize", false, "FILE_SIZE");
        public final static Property Hidden = new Property(6, int.class, "hidden", false, "HIDDEN");
        public final static Property UpdatedTime = new Property(7, long.class, "updatedTime", false, "UPDATED_TIME");
        public final static Property Duration = new Property(8, long.class, "duration", false, "DURATION");
        public final static Property Order = new Property(9, int.class, "order", false, "ORDER");
        public final static Property Type = new Property(10, int.class, "type", false, "TYPE");
    }


    public BaseMediaFolderInfoDao(DaoConfig config) {
        super(config);
    }
    
    public BaseMediaFolderInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"BASE_MEDIA_FOLDER_INFO\" (" + //
                "\"NAME\" TEXT," + // 0: name
                "\"COVER\" TEXT," + // 1: cover
                "\"PATH_OR_URI\" TEXT," + // 2: pathOrUri
                "\"PATH_AND_TYPE\" TEXT PRIMARY KEY NOT NULL ," + // 3: pathAndType
                "\"COUNT\" INTEGER NOT NULL ," + // 4: count
                "\"FILE_SIZE\" INTEGER NOT NULL ," + // 5: fileSize
                "\"HIDDEN\" INTEGER NOT NULL ," + // 6: hidden
                "\"UPDATED_TIME\" INTEGER NOT NULL ," + // 7: updatedTime
                "\"DURATION\" INTEGER NOT NULL ," + // 8: duration
                "\"ORDER\" INTEGER NOT NULL ," + // 9: order
                "\"TYPE\" INTEGER NOT NULL );"); // 10: type
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"BASE_MEDIA_FOLDER_INFO\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, BaseMediaFolderInfo entity) {
        stmt.clearBindings();
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(1, name);
        }
 
        String cover = entity.getCover();
        if (cover != null) {
            stmt.bindString(2, cover);
        }
 
        String pathOrUri = entity.getPathOrUri();
        if (pathOrUri != null) {
            stmt.bindString(3, pathOrUri);
        }
 
        String pathAndType = entity.getPathAndType();
        if (pathAndType != null) {
            stmt.bindString(4, pathAndType);
        }
        stmt.bindLong(5, entity.getCount());
        stmt.bindLong(6, entity.getFileSize());
        stmt.bindLong(7, entity.getHidden());
        stmt.bindLong(8, entity.getUpdatedTime());
        stmt.bindLong(9, entity.getDuration());
        stmt.bindLong(10, entity.getOrder());
        stmt.bindLong(11, entity.getType());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, BaseMediaFolderInfo entity) {
        stmt.clearBindings();
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(1, name);
        }
 
        String cover = entity.getCover();
        if (cover != null) {
            stmt.bindString(2, cover);
        }
 
        String pathOrUri = entity.getPathOrUri();
        if (pathOrUri != null) {
            stmt.bindString(3, pathOrUri);
        }
 
        String pathAndType = entity.getPathAndType();
        if (pathAndType != null) {
            stmt.bindString(4, pathAndType);
        }
        stmt.bindLong(5, entity.getCount());
        stmt.bindLong(6, entity.getFileSize());
        stmt.bindLong(7, entity.getHidden());
        stmt.bindLong(8, entity.getUpdatedTime());
        stmt.bindLong(9, entity.getDuration());
        stmt.bindLong(10, entity.getOrder());
        stmt.bindLong(11, entity.getType());
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3);
    }    

    @Override
    public BaseMediaFolderInfo readEntity(Cursor cursor, int offset) {
        BaseMediaFolderInfo entity = new BaseMediaFolderInfo( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // name
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // cover
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // pathOrUri
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // pathAndType
            cursor.getInt(offset + 4), // count
            cursor.getLong(offset + 5), // fileSize
            cursor.getInt(offset + 6), // hidden
            cursor.getLong(offset + 7), // updatedTime
            cursor.getLong(offset + 8), // duration
            cursor.getInt(offset + 9), // order
            cursor.getInt(offset + 10) // type
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, BaseMediaFolderInfo entity, int offset) {
        entity.setName(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setCover(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setPathOrUri(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setPathAndType(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setCount(cursor.getInt(offset + 4));
        entity.setFileSize(cursor.getLong(offset + 5));
        entity.setHidden(cursor.getInt(offset + 6));
        entity.setUpdatedTime(cursor.getLong(offset + 7));
        entity.setDuration(cursor.getLong(offset + 8));
        entity.setOrder(cursor.getInt(offset + 9));
        entity.setType(cursor.getInt(offset + 10));
     }
    
    @Override
    protected final String updateKeyAfterInsert(BaseMediaFolderInfo entity, long rowId) {
        return entity.getPathAndType();
    }
    
    @Override
    public String getKey(BaseMediaFolderInfo entity) {
        if(entity != null) {
            return entity.getPathAndType();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(BaseMediaFolderInfo entity) {
        return entity.getPathAndType() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
