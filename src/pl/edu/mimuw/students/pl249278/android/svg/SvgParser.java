package pl.edu.mimuw.students.pl249278.android.svg;

import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_CLOSE;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_CUBICTO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_LINETO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_MOVETO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_RCUBICTO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_RLINETO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_RMOVETO;

import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.primitives.list.impl.ArrayFloatList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.svg.StyleAttribute.ValueType;

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
	private static final String ATTR_STYLE = "style";
	private static final String ATTR_RECT_WIDTH = "width";
	private static final String ATTR_RECT_HEIGHT = "height";
	private static final String ATTR_PATH_SPEC = "d";
	private static final String ATTR_RECT_X = "x";
	private static final String ATTR_RECT_Y = "y";
	
	SVGPathParser pathParser = new SVGPathParser();
	private static final Pattern STYLE_ENTRY_FORMAT = Pattern.compile("\\s*([a-zA-z_-]+)\\s*:\\s*([^;]+)");
	
	public SvgImage parse(XmlPullParser xmlParser) throws XmlPullParserException, IOException, SvgFormatException {
		SvgImage result = new SvgImage();
		int eventType = xmlParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
         if(eventType == XmlPullParser.START_TAG) {
        	 if(!SVG_NS.equals(xmlParser.getNamespace())) {
        		 log.i("Ignoring element %s:%s not from SVG NS", xmlParser.getNamespace(), xmlParser.getName());
        	 } else {
        		 String name = xmlParser.getName();
	             if(TAG_ROOT.equals(name)) {
	            	 assertAttrsPresence(xmlParser, ROOT_ATTR_WIDTH, ROOT_ATTR_HEIGHT);
	            	 result.width = parseSizeValue(attrValue(xmlParser, ROOT_ATTR_WIDTH));
	            	 result.height =  parseSizeValue(attrValue(xmlParser, ROOT_ATTR_HEIGHT));
	             } else if(TAG_PATH.equals(name)) {
        			 SvgPath path = parsePathNode(xmlParser);
        			 result.objects.add(path);
        		 } else if(TAG_RECT.equals(name)) {
        			 SvgRect rect = parseRectNode(xmlParser);
        			 result.objects.add(rect);
        		 } else {
        			 log.i("Ignoring not supported element %s:%s", xmlParser.getNamespace(), xmlParser.getName());
        		 }
        	 }
         } else if(eventType == XmlPullParser.END_TAG) {
//             System.out.println("End tag "+xmlParser.getName());
         } else if(eventType == XmlPullParser.TEXT) {
//             System.out.println("Text "+xmlParser.getText());
         }
         eventType = xmlParser.next();
        }
        return result;
	}

	private float parseSizeValue(String value) {
		return Float.parseFloat(value);
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

	private SvgRect parseRectNode(XmlPullParser xmlParser) throws SvgFormatException {
		assertAttrsPresence(xmlParser, ATTR_RECT_WIDTH, ATTR_RECT_HEIGHT, ATTR_RECT_X, ATTR_RECT_Y);
		SvgRect result = new SvgRect();
		result.width = parseSizeValue(attrValue(xmlParser, ATTR_RECT_WIDTH));
		result.hegiht = parseSizeValue(attrValue(xmlParser, ATTR_RECT_HEIGHT));
		result.x = parseSizeValue(attrValue(xmlParser, ATTR_RECT_X));
		result.y = parseSizeValue(attrValue(xmlParser, ATTR_RECT_Y));
		parseStyle(xmlParser, result);
		return result;
	}

	private static void parseStyle(XmlPullParser xmlParser, SvgObject obj) throws SvgFormatException {
		String styleType = attrValue(xmlParser, ATTR_STYLE_TYPE);
		if(styleType != null && !VAL_STYLETYPE_TEXTCSS.equals(styleType)) {
			log.i("Ignoring unsupported style format %s", styleType);
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
				log.i("Ignoring unsupported style property %s: %s", propName, propValue);
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
				float result = Float.parseFloat(propValue);
				if(result < min || result > max) {
					throw new SvgFormatException(String.format(
						"Parsed value %f outside of expected range <%f, %f>",
						result,
						min, max
					));
				}
				return result;
			} catch(NumberFormatException e) {
				throw new SvgFormatException(e);
			}
		}
		
	}

	private static final Pattern argPattern = Pattern.compile("\\s*,?\\s*(-?(?:\\d*\\.\\d+|\\d+))(?:[eE](-?\\d+))?");
	private class SVGPathParser {
		
		private String source;
	    private int index;
		private int length;
		private StringBuilder commands = new StringBuilder();
		private ArrayFloatList args = new ArrayFloatList();
		private char currCmd;
		
		private boolean isFirstCmd() {
			return commands.length() == 0;
		}
	
		private SvgPath parsePath(String svgPathSpec) throws ParseException {
	    	this.source = svgPathSpec;
	    	this.index = 0;
	    	this.length = source.length();
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
			switch(currCmd) {
			case PATH_CMD_RMOVETO:
			case PATH_CMD_MOVETO:
				parseMoveTo();
				break;
			case PATH_CMD_RCUBICTO:
			case PATH_CMD_CUBICTO:
				parseCubicCurve();
				break;
			case PATH_CMD_RLINETO:
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
	
		private void parseCubicCurve() throws ParseException {
			Float x1;
			int counter = 0;
			x1 = readArgument();
			do {
				counter++;
				args.add(x1);
				for(int i = 0; i < 5; i++) {
					args.add(readArgument());
				}
			} while((x1 = tryReadArgument()) != null);
			appendCommand(currCmd, counter);
		}
	
		private void appendCommand(char cmd, int quantity) {
			commands.append(cmd);
			commands.appendCodePoint(quantity);
		}
	
		private void parseMoveTo() throws ParseException {
			commands.append(isFirstCmd() ? Character.toUpperCase(currCmd) : currCmd);
			args.add(readArgument());
			args.add(readArgument());
			parseLineTo();
		}

		private void parseLineTo() throws ParseException {
			Float x1;
			int counter = 0;
			while((x1 = tryReadArgument()) != null) {
				counter++;
				args.add(x1);
				args.add(readArgument());
			}
			if(counter > 0) {
				appendCommand(Character.isUpperCase(currCmd) ? PATH_CMD_LINETO : PATH_CMD_RLINETO, counter);
			}
		}
		
		private float readArgument() throws ParseException {
			Float f = tryReadArgument();
			if(f != null) {
				return f;
			} else {
				throw new ParseException("No argument found at current index.", index);
			}
		}
		
		private Float tryReadArgument() {
			int i = index;
			Matcher matcher = argPattern.matcher(source);
			boolean found = matcher.find(i);
			if(found && matcher.start() == index) {
				index = matcher.end(0);
				float value = Float.parseFloat(matcher.group(1));
				String exponent = matcher.group(2);
				if(exponent != null) {
					value *= Math.pow(10, Float.parseFloat(exponent));
				}
				return new Float(value);
			} else {
				return null;
			}
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