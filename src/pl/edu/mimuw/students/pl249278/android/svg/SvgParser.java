package pl.edu.mimuw.students.pl249278.android.svg;

import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_CLOSE;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_CUBICTO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_LINETO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_MOVETO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_RCUBICTO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_RLINETO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_RMOVETO;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.primitives.list.impl.ArrayFloatList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.svg.StyleAttribute.ValueType;
import android.graphics.Matrix;

public class SvgParser {
	private static LogUtils log = new LogUtils(SvgParser.class);
	private static final String VAL_STYLETYPE_TEXTCSS = "text/css";
	private static final String ATTR_STYLE_TYPE = "contentStyleType";
	private static final String TAG_ROOT = "svg";
	private static final String ROOT_ATTR_HEIGHT = "height";
	private static final String ROOT_ATTR_WIDTH = "width";
	public static final String SVG_NS = "http://www.w3.org/2000/svg";
	private static final String TAG_PATH = "path";
	private static final String TAG_RECT = "rect";
	private static final String TAG_GROUP = "g";
	private static final String ATTR_STYLE = "style";
	private static final String ATTR_RECT_WIDTH = "width";
	private static final String ATTR_RECT_HEIGHT = "height";
	private static final String ATTR_PATH_SPEC = "d";
	private static final String ATTR_RECT_X = "x";
	private static final String ATTR_RECT_Y = "y";
	private static final String ATTR_TRANSFORM = "transform";
	
	SVGPathParser pathParser = new SVGPathParser();
	private static final Pattern STYLE_ENTRY_FORMAT = Pattern.compile("\\s*([a-zA-z_-]+)\\s*:\\s*([^;]+)");
	
	private int depth = 0;
	public SvgImage parse(XmlPullParser xmlParser) throws XmlPullParserException, IOException, SvgFormatException {
		try {
			SvgImage result = new SvgImage();
			int eventType = xmlParser.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	         if(eventType == XmlPullParser.START_TAG) {
	        	 depth++;
	        	 if(!SVG_NS.equals(xmlParser.getNamespace())) {
	        		 log.v("Ignoring element %s:%s not from SVG NS", xmlParser.getNamespace(), xmlParser.getName());
	        	 } else {
	        		 String name = xmlParser.getName();
		             if(TAG_ROOT.equals(name)) {
		            	 assertAttrsPresence(xmlParser, ROOT_ATTR_WIDTH, ROOT_ATTR_HEIGHT);
		            	 result.width = parseFloat(attrValue(xmlParser, ROOT_ATTR_WIDTH));
		            	 result.height =  parseFloat(attrValue(xmlParser, ROOT_ATTR_HEIGHT));
		             } else if(TAG_PATH.equals(name)) {
	        			 SvgPath path = parsePathNode(xmlParser);
	        			 result.objects.add(path);
	        		 } else if(TAG_RECT.equals(name)) {
	        			 result.objects.add(parseRectNode(xmlParser));
	        		 } else if(TAG_GROUP.equals(name)) {
	        			 handleGroupNode(xmlParser);
	        		 } else {
	        			 log.v("Ignoring not supported element %s:%s", xmlParser.getNamespace(), xmlParser.getName());
	        		 }
	        	 }
	         } else if(eventType == XmlPullParser.END_TAG) {
	    		 String name = xmlParser.getName();
	        	 if(TAG_GROUP.equals(name)) {
	        		 handleGroupNodeEnd(xmlParser);
	        	 }
	//             System.out.println("End tag "+xmlParser.getName());
	        	 depth--;
	         } else if(eventType == XmlPullParser.TEXT) {
	//             System.out.println("Text "+xmlParser.getText());
	         }
	         eventType = xmlParser.next();
	        }
	        return result;
		} catch(ParseException e) {
			throw new SvgFormatException(e);
		}
	}

	private Matrix currentTransformationMatrix = new Matrix();
	private boolean isCTMdirty = true;
	private Stack<Matrix> transformations = new Stack<Matrix>();
	private Stack<Integer> introLevels = new Stack<Integer>();
	
	private Matrix getCTM() {
		if(isCTMdirty) {
			currentTransformationMatrix.reset();
			for(Matrix m: transformations) {
				// stack iterates bottom-up so use PRE
				currentTransformationMatrix.preConcat(m);
			}
			isCTMdirty = false;
		}
		return currentTransformationMatrix;
	}
	
	private void handleGroupNode(XmlPullParser xmlParser) throws ParseException {
		String transform = attrValue(xmlParser, ATTR_TRANSFORM);
		if(transform != null) {
			/* przygotować matrycę dla tej transformacji, odłożyć ją na stos wraz z zapisanym poziomem zaglebienia */
			Matrix transformMatrix = new Matrix();
			parseTransformString(transform, transformMatrix);
			transformations.push(transformMatrix);
			introLevels.push(depth);
			isCTMdirty = true;
		}
	}
	
	private void handleGroupNodeEnd(XmlPullParser xmlParser) {
		/* sprawdzic czy grupa z aktualnego poziomu zaglebienia wprowadzala transformacje. Jesli tak to trzeba je wycofac */
		if(!introLevels.empty() && introLevels.peek().equals(depth)) {
			introLevels.pop();
			transformations.pop();
		}
	}

	private static NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
	private static float parseFloat(String value) throws ParseException {
		return numberFormat.parse(value).floatValue();
	}
	
	private void assertAttrsPresence(XmlPullParser xmlParser, String... attrNames) throws SvgFormatException {
		for (int i = 0; i < attrNames.length; i++) {
			if(attrValue(xmlParser, attrNames[i]) == null) {
				throw new SvgFormatException(String.format(
					"Node %s:%s doesn't contain attribute %s",
					xmlParser.getNamespace(),
					xmlParser.getName(),
					attrNames[i]
				));
			}
		}
	}
	
	private SvgPath parsePathNode(XmlPullParser xmlParser) throws SvgFormatException {
		assertAttrsPresence(xmlParser, ATTR_PATH_SPEC);
		try {
			SvgPath path = this.pathParser.parsePath(attrValue(xmlParser, ATTR_PATH_SPEC));
			parseStyle(xmlParser, path);
			return path;
		} catch (ParseException e) {
			throw new SvgFormatException(e);
		}
	}
	
	private static String attrValue(XmlPullParser xmlParser, String attrName) {
		return xmlParser.getAttributeValue(null, attrName);
	}

	private static final int TL_X = 0;
	private static final int TL_Y = TL_X+1;
	private static final int TR_X = TL_Y+1;
	private static final int TR_Y = TR_X+1;
	private static final int BR_X = TR_Y+1;
	private static final int BR_Y = BR_X+1;
	private static final int BL_X = BR_Y+1;
	private static final int BL_Y = BL_X+1;
	// (x,y) of top-left, top-right, bottom-right
	private float[] mTempRect = new float[BL_Y+1];
	
	private SvgObject parseRectNode(XmlPullParser xmlParser) throws SvgFormatException, ParseException {
		assertAttrsPresence(xmlParser, ATTR_RECT_WIDTH, ATTR_RECT_HEIGHT, ATTR_RECT_X, ATTR_RECT_Y);
		mTempRect[TL_X] = parseFloat(attrValue(xmlParser, ATTR_RECT_X));
		mTempRect[TL_Y] = parseFloat(attrValue(xmlParser, ATTR_RECT_Y));
		mTempRect[TR_X] = mTempRect[TL_X] + parseFloat(attrValue(xmlParser, ATTR_RECT_WIDTH));
		mTempRect[TR_Y] = mTempRect[TL_Y];
		mTempRect[BR_X] = mTempRect[TR_X];
		mTempRect[BR_Y] = mTempRect[TR_Y] + parseFloat(attrValue(xmlParser, ATTR_RECT_HEIGHT));
		mTempRect[BL_X] = mTempRect[TL_X];
		mTempRect[BL_Y] = mTempRect[BR_Y];
		getCTM().mapPoints(mTempRect);
		// check if it's still a rectangle
		SvgObject result;
		if((mTempRect[TL_X]-mTempRect[TR_X])*(mTempRect[TL_Y]-mTempRect[TR_Y]) == 0
		&& (mTempRect[TR_X]-mTempRect[BR_X])*(mTempRect[TR_Y]-mTempRect[BR_Y]) == 0) {
			float x = min(mTempRect[TL_X], mTempRect[TR_X], mTempRect[BR_X]);
			float y = min(mTempRect[TL_Y], mTempRect[TR_Y], mTempRect[BR_Y]);
			result = new SvgRect(
				x,
				y,
				max(mTempRect[TL_X], mTempRect[TR_X], mTempRect[BR_X]) - x,
				max(mTempRect[TL_Y], mTempRect[TR_Y], mTempRect[BR_Y]) - y
			);
		} else {
			result = new SvgPath(
				String.format("%c%c%c%c", PATH_CMD_MOVETO, PATH_CMD_LINETO, 3, PATH_CMD_CLOSE), 
				mTempRect
			);
			mTempRect = new float[mTempRect.length];
		}
		parseStyle(xmlParser, result);
		return result;
	}
		

	private float min(float f, float g, float h) {
		return Math.min(Math.min(f, g), h);
	}
	private float max(float f, float g, float h) {
		return Math.max(Math.max(f, g), h);
	}

	private static void parseStyle(XmlPullParser xmlParser, SvgObject obj) throws SvgFormatException {
		String styleType = attrValue(xmlParser, ATTR_STYLE_TYPE);
		if(styleType != null && !VAL_STYLETYPE_TEXTCSS.equals(styleType)) {
			log.v("Ignoring unsupported style format %s", styleType);
			return;
		}
		String styleAttrVal = attrValue(xmlParser, ATTR_STYLE);
		if(styleAttrVal == null) return;
		Matcher m = STYLE_ENTRY_FORMAT.matcher(styleAttrVal);
		while(m.find()) {
			String propName = m.group(1);
			String propValue = m.group(2).trim();
			if(StyleAttribute.textLabels.containsKey(propName)) {
				StyleAttribute styleAttribute = StyleAttribute.textLabels.get(propName);
				Object value;
				switch(styleAttribute.type) {
				case PAINT:
					value = StyleValueParser.parsePaint(propValue);
					break;
				case OPACITY_VALUE:
					value = StyleValueParser.parseFloat(propValue, 0, 1);
					break;
				default:
					throw new SvgFormatException("Unsupported style property value format: "+styleAttribute.type.name());
				}
				obj.style.put(styleAttribute, value);
//				log.i("Style property %s: %s", propName, propValue);
			} else {
				log.v("Ignoring unsupported style property %s: %s", propName, propValue);
			}
		}
	}
	
	private static class StyleValueParser {

		private static final String STYLE_PROPVAL_NONE = "none";
		private static final Pattern HEX_FORMAT = Pattern.compile("#([a-zA-Z0-9]{6})");

		public static Object parsePaint(String propValue) throws SvgFormatException {
			if(STYLE_PROPVAL_NONE.equals(propValue)) {
				return ValueType.PAINT_NONE;
			}
			if(HEX_FORMAT.matcher(propValue).matches()) {
				return Integer.parseInt(propValue.substring(1), 16);
			}
			throw new SvgFormatException("Value "+propValue+" doesn't match PAINT format");
		}

		public static Object parseFloat(String propValue, float min, float max) throws SvgFormatException {
			try {
				float result = SvgParser.parseFloat(propValue);
				if(result < min || result > max) {
					throw new SvgFormatException(String.format(
						"Parsed value %f outside of expected range <%f, %f>",
						result,
						min, max
					));
				}
				return result;
			} catch(ParseException e) {
				throw new SvgFormatException(e);
			}
		}
		
	}
	
	private static final String WS_CHARS = "\u0020\0009\000D\000A";
	private static final Pattern tPattern = Pattern.compile("["+WS_CHARS+"|,]*(matrix|translate|scale|rotate|skewX|skewY)["+WS_CHARS+"]*\\(([^\\)]+)\\)");
	
	private static void parseTransformString(String attrValue, Matrix out) throws ParseException {
		Matcher matcher = tPattern.matcher(attrValue);
		NumberArgumentParser nParser = new NumberArgumentParser();
		while(matcher.find()) {
			String label = matcher.group(1);
			nParser.setSource(matcher.group(2));
			if("translate".equals(label)) {
				out.preTranslate(
					nParser.readArgument(),
					nParser.tryReadArgument(0f)
				);
			} else if("scale".equals(label)) {
				float sx = nParser.readArgument();
				out.preScale(
					sx,
					nParser.tryReadArgument(sx)
				);
			} else if("rotate".equals(label)) {
				out.preRotate(
					nParser.readArgument(),
					nParser.tryReadArgument(0f),
					nParser.tryReadArgument(0f)
				);
			} else {
				log.v("Ignoring unsupported transformation %s", label);
			}
		}
	}

	private static class NumberArgumentParser {
		private static final Pattern argPattern = Pattern.compile("\\s*,?\\s*(-?(?:\\d*\\.\\d+|\\d+))(?:[eE](-?\\d+))?");
		protected String source;
		protected int index;
		protected int length;
		
		protected void setSource(String source) {
			this.source = source;
	    	this.index = 0;
	    	this.length = source.length();
		}
		
		protected float readArgument() throws ParseException {
			Float f = tryReadArgument(null);
			if(f != null) {
				return f;
			} else {
				throw new ParseException("No argument found at current index.", index);
			}
		}
		
		protected Float tryReadArgument(Float defaultValue) throws ParseException {
			int i = index;
			Matcher matcher = argPattern.matcher(source);
			boolean found = matcher.find(i);
			if(found && matcher.start() == index) {
				index = matcher.end(0);
				float value = parseFloat(matcher.group(1));
				String exponent = matcher.group(2);
				if(exponent != null) {
					value *= Math.pow(10, parseFloat(exponent));
				}
				return new Float(value);
			} else {
				return defaultValue;
			}
		}
	    		
	}
	
	private class SVGPathParser extends NumberArgumentParser {
		private StringBuilder commands = new StringBuilder();
		private ArrayFloatList args = new ArrayFloatList();
		private char currCmd;
		private boolean currCmdIsRelative;
		
		private boolean isFirstCmd() {
			return commands.length() == 0;
		}
		
		/**
		 * Takes into account current transform matrix.
		 * @param svgPathSpec value of attribute d of xml node path
		 */
		private SvgPath parsePath(String svgPathSpec) throws ParseException {
	    	setSource(svgPathSpec);
	    	commands.delete(0, commands.length());
	    	args.clear();
	    	while(readCommand() != -1) {
	    	}
	    	return new SvgPath(commands.toString(), args.toFloatArray());
	    }
	
		private int readCommand() throws ParseException {
			int i = index;
			for(; i < length && Character.isWhitespace(source.charAt(i)); i++) { }
			if(i >= length) {
				return -1;
			}
			index = i+1;
			currCmd = source.charAt(i);
			currCmdIsRelative = false;
			switch(currCmd) {
			case PATH_CMD_RMOVETO:
				currCmdIsRelative = true;
			case PATH_CMD_MOVETO:
				parseMoveTo();
				break;
			case PATH_CMD_RCUBICTO:
				currCmdIsRelative = true;
			case PATH_CMD_CUBICTO:
				parseCubicCurve();
				break;
			case PATH_CMD_RLINETO:
				currCmdIsRelative = true;
			case PATH_CMD_LINETO:
				parseLineTo();
				break;
			case 'z':
			case PATH_CMD_CLOSE:
				commands.append(PATH_CMD_CLOSE);
				break;
			default:
				throw new ParseException("Uknown commmand char "+currCmd, i);
			}
			return currCmd;
		}
	
		private void appendCommand(char cmd, int quantity) {
			commands.append(cmd);
			commands.appendCodePoint(quantity);
		}
		
		private void parseCubicCurve() throws ParseException {
			int counter = 0;
			readPair(mTempPair);
			do {
				counter++;
				transformAndAdd(mTempPair);
				transformAndAdd(readPair(mTempPair));
				transformAndAdd(readPair(mTempPair));
			} while(tryReadPair(mTempPair));
			appendCommand(currCmd, counter);
		}
	
		private void parseMoveTo() throws ParseException {
			if(isFirstCmd() && currCmdIsRelative) {
				currCmdIsRelative = false;
				commands.append(PATH_CMD_MOVETO);
				transformAndAdd(readPair(mTempPair));
				currCmdIsRelative = true;
			} else {
				commands.append(currCmd);
				transformAndAdd(readPair(mTempPair));
			}
			parseLineTo();
		}

		private void parseLineTo() throws ParseException {
			int counter = 0;
			while(tryReadPair(mTempPair)) {
				counter++;
				transformAndAdd(mTempPair);
			}
			if(counter > 0) {
				appendCommand(!currCmdIsRelative ? PATH_CMD_LINETO : PATH_CMD_RLINETO, counter);
			}
		}
		
		private float[] mTempPair = new float[4];
		/**
		 * Transform given point by current transform matrix and adds it's x and y to args
		 */
		private void transformAndAdd(float[] point) {
			if(point.length >= 4) {
				point[2] = point[3] = 0;
			}
			getCTM().mapPoints(point);
			if(currCmdIsRelative) {
				args.add(point[0]-point[2]);
				args.add(point[1]-point[3]);
			} else {
				args.add(point[0]);
				args.add(point[1]);
			}
		}
		
		/**
		 * @return passes back argument
		 */
		private float[] readPair(float[] result) throws ParseException {
			result[0] = readArgument();
			result[1] = readArgument();
			return result;
		}
		private boolean tryReadPair(float[] result) throws ParseException {
			Float value = tryReadArgument(null);
			if(value == null) return false;
			result[0] = value;
			if((value = tryReadArgument(null)) == null) return false;
			result[1] = value;
			return true;
		}
		
	}
	
	@SuppressWarnings("serial")
	public static class SvgFormatException extends Exception {
		SvgFormatException(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}
		SvgFormatException(String detailMessage) {
			super(detailMessage);
		}
		SvgFormatException(Throwable throwable) {
			super(throwable);
		}
	}
	
}