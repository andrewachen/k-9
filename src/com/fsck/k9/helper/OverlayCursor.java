package com.fsck.k9.helper;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;
import com.fsck.k9.K9;

import java.util.*;

/**
 * A wrapper around a cursor that lets you define an overlay for specific items
 * within the cursor.
 */
public class OverlayCursor<T> extends CursorWrapper {
    // overlays is a map of message IDs to column overlays
    private final Map<Long, Overlays> positionOverlays = new HashMap<Long, Overlays>();
    private final int keyColumn;

    private static final Set<Class> POSSIBLE_KEY_CLASSES;
    static {
        final Set<Class> classes = new HashSet<Class>();
        classes.add(Short.class);
        classes.add(Integer.class);
        classes.add(Long.class);
        classes.add(String.class);
        POSSIBLE_KEY_CLASSES = Collections.unmodifiableSet(classes);
    }

    /**
     * Create an overlay on top an existing cursor.
     * @param parentCursor Cursor to wrap
     * @param keyColumn Zero-indexed column of the message ID.
     */
    public OverlayCursor(final Cursor parentCursor, final int keyColumn) {
        super(parentCursor);
        parentCursor.get
        if(POSSIBLE_KEY_CLASSES.contains(T.class

        }
        this.keyColumn = keyColumn;
    }

    /**
     * Fetch the overlay for a given column at the current cursor position.
     *
     * @param column Column for which we want the overlay
     * @return String of the overlay value if defined; null otherwise.
     */
    private String getOverlay(final int column) {
        final Overlays overlays = positionOverlays.get(getPosition());
        if (overlays != null && overlays.hasOverlay(column)) {
            return overlays.getOverlay(column);
        } else {
            return null;
        }
    }

    /**
     * Add a new overlay at the current cursor position.
     *
     * @param column Column number to overlay
     * @param value  Value to overlay with.
     */
    public void addOverlay(final int column, final Object value) {
        if (getPosition() < 0) {
            return;
        }
        if (value == null) {
            throw new IllegalArgumentException("Can not set a null value as an override");
        }
        if (column >= getColumnCount()) {
            throw new IndexOutOfBoundsException("Column must be between 0 and " + (getColumnCount() - 1));
        }
        final long messageId = getCurrentMessageId();
        final Overlays overlays;
        if (positionOverlays.containsKey(getPosition())) {
            overlays = positionOverlays.get(getPosition());
        } else {
            overlays = new Overlays();
            positionOverlays.put(getPosition(), overlays);
        }

        overlays.add(column, value.toString());

        if (K9.DEBUG) {
            Log.d(K9.LOG_TAG, positionOverlays.size() + " overlays; " + overlays.numberOfOverlays() + " column overlays for position " + getPosition());
            Log.d(K9.LOG_TAG, "Adding an overlay to this cursor for position " + getPosition() + ", column " + column);
        }
    }

    private long getCurrentMessageId() {
        super.getLong()
    }

    @Override
    public float getFloat(int columnIndex) {
        final String overlay = getOverlay(columnIndex);
        if (overlay != null) {
            return Float.valueOf(overlay);
        } else {
            return super.getFloat(columnIndex);
        }
    }

    @Override
    public int getInt(int columnIndex) {
        final String overlay = getOverlay(columnIndex);
        if (overlay != null) {
            return Integer.valueOf(overlay);
        } else {
            return super.getInt(columnIndex);
        }
    }

    @Override
    public long getLong(int columnIndex) {
        final String overlay = getOverlay(columnIndex);
        if (overlay != null) {
            return Integer.valueOf(overlay);
        } else {
            return super.getLong(columnIndex);
        }
    }

    @Override
    public short getShort(int columnIndex) {
        final String overlay = getOverlay(columnIndex);
        if (overlay != null) {
            return Short.valueOf(overlay);
        } else {
            return super.getShort(columnIndex);
        }
    }

    @Override
    public String getString(int columnIndex) {
        final String overlay = getOverlay(columnIndex);
        if (overlay != null) {
            return overlay;
        } else {
            return super.getString(columnIndex);
        }
    }

    @Override
    public void close() {
        positionOverlays.clear();
        super.close();
    }

    @Override
    public void deactivate() {
        positionOverlays.clear();
        super.deactivate();
    }

    /**
     * This class holds all of the column overlays for a particular row.
     */
    class Overlays {
        private Map<Integer, String> columnOverlays = new HashMap<Integer, String>();

        public void add(final Integer column, final String value) {
            columnOverlays.put(column, value);
        }

        public boolean hasOverlay(final int column) {
            return columnOverlays.containsKey(column);
        }

        public String getOverlay(final int column) {
            return columnOverlays.get(column);
        }

        public int numberOfOverlays() {
            return columnOverlays.size();
        }
    }
}
