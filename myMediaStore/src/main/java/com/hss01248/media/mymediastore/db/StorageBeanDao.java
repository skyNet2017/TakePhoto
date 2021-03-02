package com.hss01248.media.mymediastore.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.hss01248.media.mymediastore.bean.StorageBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "STORAGE_BEAN".
*/
public class StorageBeanDao extends AbstractDao<StorageBean, Integer> {

    public static final String TABLENAME = "STORAGE_BEAN";

    /**
     * Properties of entity StorageBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, int.class, "id", true, "ID");
        public final static Property Usable = new Property(1, int.class, "usable", false, "USABLE");
        public final static Property Type = new Property(2, int.class, "type", false, "TYPE");
        public final static Property DiskId = new Property(3, String.class, "diskId", false, "DISK_ID");
        public final static Property SmbHost = new Property(4, String.class, "smbHost", false, "SMB_HOST");
        public final static Property SmbUName = new Property(5, String.class, "smbUName", false, "SMB_UNAME");
        public final static Property SmbPw = new Property(6, String.class, "smbPw", false, "SMB_PW");
        public final static Property SmbRootDirs = new Property(7, String.class, "smbRootDirs", false, "SMB_ROOT_DIRS");
        public final static Property SafRoot = new Property(8, String.class, "safRoot", false, "SAF_ROOT");
        public final static Property UsbName = new Property(9, String.class, "usbName", false, "USB_NAME");
    }


    public StorageBeanDao(DaoConfig config) {
        super(config);
    }
    
    public StorageBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"STORAGE_BEAN\" (" + //
                "\"ID\" INTEGER PRIMARY KEY NOT NULL ," + // 0: id
                "\"USABLE\" INTEGER NOT NULL ," + // 1: usable
                "\"TYPE\" INTEGER NOT NULL ," + // 2: type
                "\"DISK_ID\" TEXT," + // 3: diskId
                "\"SMB_HOST\" TEXT," + // 4: smbHost
                "\"SMB_UNAME\" TEXT," + // 5: smbUName
                "\"SMB_PW\" TEXT," + // 6: smbPw
                "\"SMB_ROOT_DIRS\" TEXT," + // 7: smbRootDirs
                "\"SAF_ROOT\" TEXT," + // 8: safRoot
                "\"USB_NAME\" TEXT);"); // 9: usbName
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"STORAGE_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, StorageBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindLong(2, entity.getUsable());
        stmt.bindLong(3, entity.getType());
 
        String diskId = entity.getDiskId();
        if (diskId != null) {
            stmt.bindString(4, diskId);
        }
 
        String smbHost = entity.getSmbHost();
        if (smbHost != null) {
            stmt.bindString(5, smbHost);
        }
 
        String smbUName = entity.getSmbUName();
        if (smbUName != null) {
            stmt.bindString(6, smbUName);
        }
 
        String smbPw = entity.getSmbPw();
        if (smbPw != null) {
            stmt.bindString(7, smbPw);
        }
 
        String smbRootDirs = entity.getSmbRootDirs();
        if (smbRootDirs != null) {
            stmt.bindString(8, smbRootDirs);
        }
 
        String safRoot = entity.getSafRoot();
        if (safRoot != null) {
            stmt.bindString(9, safRoot);
        }
 
        String usbName = entity.getUsbName();
        if (usbName != null) {
            stmt.bindString(10, usbName);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, StorageBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindLong(2, entity.getUsable());
        stmt.bindLong(3, entity.getType());
 
        String diskId = entity.getDiskId();
        if (diskId != null) {
            stmt.bindString(4, diskId);
        }
 
        String smbHost = entity.getSmbHost();
        if (smbHost != null) {
            stmt.bindString(5, smbHost);
        }
 
        String smbUName = entity.getSmbUName();
        if (smbUName != null) {
            stmt.bindString(6, smbUName);
        }
 
        String smbPw = entity.getSmbPw();
        if (smbPw != null) {
            stmt.bindString(7, smbPw);
        }
 
        String smbRootDirs = entity.getSmbRootDirs();
        if (smbRootDirs != null) {
            stmt.bindString(8, smbRootDirs);
        }
 
        String safRoot = entity.getSafRoot();
        if (safRoot != null) {
            stmt.bindString(9, safRoot);
        }
 
        String usbName = entity.getUsbName();
        if (usbName != null) {
            stmt.bindString(10, usbName);
        }
    }

    @Override
    public Integer readKey(Cursor cursor, int offset) {
        return cursor.getInt(offset + 0);
    }    

    @Override
    public StorageBean readEntity(Cursor cursor, int offset) {
        StorageBean entity = new StorageBean( //
            cursor.getInt(offset + 0), // id
            cursor.getInt(offset + 1), // usable
            cursor.getInt(offset + 2), // type
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // diskId
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // smbHost
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // smbUName
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // smbPw
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // smbRootDirs
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // safRoot
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9) // usbName
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, StorageBean entity, int offset) {
        entity.setId(cursor.getInt(offset + 0));
        entity.setUsable(cursor.getInt(offset + 1));
        entity.setType(cursor.getInt(offset + 2));
        entity.setDiskId(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setSmbHost(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setSmbUName(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setSmbPw(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setSmbRootDirs(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setSafRoot(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setUsbName(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
     }
    
    @Override
    protected final Integer updateKeyAfterInsert(StorageBean entity, long rowId) {
        return entity.getId();
    }
    
    @Override
    public Integer getKey(StorageBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(StorageBean entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
