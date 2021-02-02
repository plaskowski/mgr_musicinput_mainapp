package pl.edu.mimuw.students.pl249278.android.svg;

import java.util.ArrayList;
import java.util.EnumMap;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;

public class SvgInflater {
	private static final String TAG = SvgInflater.class.getSimpleName();
	private TypedArray arr;
	private int index;
	
	public SvgImage inflate(Resources res, int arrayResId) {
		arr = res.obtainTypedArray(arrayResId);
		try {
			index = 0;
			return inflate();
		} finally {
			if(arr != null) {
				arr.recycle();
			}
		}
	}
	
	private static ThreadLocal<ArrayFloatList> mArgs = new ThreadLocal<ArrayFloatList>() {
		@Override
		protected ArrayFloatList initialValue() {
			return new ArrayFloatList(2<<10);
		}
	};
	
	private SvgImage inflate() {
		float w = arr.getFloat(index++, 0), h = arr.getFloat(index++, 0);
		// read objects
		int indexCount = arr.length();
		ArrayList<SvgObject> objects = new ArrayList<SvgObject>();
		StringBuilder builder = new StringBuilder();
		ArrayFloatList args = mArgs.get();
		while(index < indexCount) {
			int objType = arr.getResourceId(index++, 0);
			SvgObject obj;
			EnumMap<StyleAttribute, Object> style = new EnumMap<StyleAttribute, Object>(StyleAttribute.class);
			if(objType == R.id.svgobject_path) {
				int attr;
				do {
					attr = arr.getResourceId(index++, 0);
				} while(consumeStyleAttr(attr, style));
				if(attr == R.id.svgattr_pathspec) {
					int cmdsCount = arr.getInt(index++, 0);
					args.clear();
					builder.setLength(0);
					for(int i = 0; i < cmdsCount; i++) {
						char cmd = arr.getString(index++).charAt(0);
						builder.append(cmd);
						if(SvgPath.cmdIsRepeatable(cmd)) {
							builder.appendCodePoint(1);
						}
						int argsCount = SvgPath.cmdArgsCount(cmd);
						for(int cmdI = 0; cmdI < argsCount; cmdI++) {
							args.add(arr.getFloat(index++, 0));
						}
					}
					obj = new SvgPath(builder.toString(), args.toFloatArray()); 
				} else {
					Log.w(TAG, "Invalid svgattr for Path: "+attr);
					break;
				}
			} else if(objType == R.id.svgobject_rect) {
				int attr;
				do {
					attr = arr.getResourceId(index++, 0);
				} while(consumeStyleAttr(attr, style));
				if(attr == R.id.svgattr_frame) {
					obj = new SvgRect(arr.getFloat(index++, 0), arr.getFloat(index++, 0),
							arr.getFloat(index++, 0), arr.getFloat(index++, 0));
				} else {
					Log.w(TAG, "Invalid svgattr for Rect: "+attr);
					break;
				}
			} else {
				Log.w(TAG, "Invalid svgobject type: "+objType);
				break;
			}
			obj.style = style;
			objects.add(obj);
		}
		return new SvgImage(w, h, objects);
	}

	private boolean consumeStyleAttr(int attr, EnumMap<StyleAttribute, Object> out) {
		if(attr == R.id.svgattr_fill) {
			out.put(StyleAttribute.FILL, arr.getInt(index++, 0));
		} else if(attr == R.id.svgattr_fill_opacity) {
			out.put(StyleAttribute.FILL_OPACITY, arr.getFloat(index++, 1));
		} else if(attr == R.id.svgattr_stroke) {
			out.put(StyleAttribute.STROKE, arr.getInt(index++, 0));
		} else if(attr == R.id.svgattr_stroke_opacity) {
			out.put(StyleAttribute.STROKE_OPACITY, arr.getFloat(index++, 1));
		} else {
			return false;
		}
		return true;
	}
}
