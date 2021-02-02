package pl.edu.mimuw.students.pl249278.android.svg;
		
public class SvgPath extends SvgObject {
    private static final int NO_VALUE = -1;
	public static final char PATH_CMD_CLOSE = 'Z';
	public static final char PATH_CMD_CUBICTO = 'C';
	public static final char PATH_CMD_RCUBICTO = 'c';
	public static final char PATH_CMD_MOVETO = 'M';
	public static final char PATH_CMD_RMOVETO = 'm';
	public static final char PATH_CMD_LINETO = 'L';
	public static final char PATH_CMD_RLINETO = 'l';
	
	// for (r)lineTo, (r)moveTo
	public static final int ARG_X = 0;
	public static final int ARG_Y = 1;

	// for (r)cubicTo
	public static final int ARG_CP1_X = 0;
	public static final int ARG_CP1_Y = 1;
	public static final int ARG_CP2_X = 2;
	public static final int ARG_CP2_Y = 3;
	public static final int ARG_DEST_X = 4;
	public static final int ARG_DEST_Y = 5;
	
	
	public SvgPath(String commands, float[] args) {
		this.commands = commands;
		this.args = args;
	}
	
	private String commands;
	private float[] args;
	
	public static class SvgPathCommand {
		public float[] args;
		public char cmd;
		public SvgPathCommand() {
			args = new float[6];
		}
	}
	
	public MemorySaavyIterator<SvgPathCommand> getIterator() {
		return new PathIterator();
	}
	
	public static interface MemorySaavyIterator<T> {
		void readNext(T dest);
		boolean hasNext();
	}
	
	private class PathIterator implements MemorySaavyIterator<SvgPathCommand> {
		int specIndex = 0, repeatCounter = 0, argsOffset = 0, specLength = SvgPath.this.commands.length();
		int currentChar = NO_VALUE;
		private boolean isRepeatable;
		private int argsAmount;

		@Override
		public void readNext(SvgPathCommand dest) {
			if(currentChar == NO_VALUE) {
				currentChar = commands.charAt(specIndex);
				isRepeatable = cmdIsRepeatable(currentChar);
				argsAmount = cmdArgsCount(currentChar);
				repeatCounter = !isRepeatable ? 1 :
					commands.charAt(++specIndex);
			}
			
			if(dest != null) {
				dest.cmd = (char) currentChar;
				System.arraycopy(args, argsOffset, dest.args, 0, argsAmount);
			}
			argsOffset += argsAmount;
			
			repeatCounter--;
			if(repeatCounter <= 0) {
				specIndex++;
				currentChar = NO_VALUE;
			}
		}

		@Override
		public boolean hasNext() {
			return specIndex < specLength;
		}
		
	};
	
	public static int cmdArgsCount(int cmdChar) {
		switch(cmdChar) {
		case PATH_CMD_LINETO:
		case PATH_CMD_RLINETO:
		case PATH_CMD_MOVETO:
		case PATH_CMD_RMOVETO:
			return 2;
		case PATH_CMD_CLOSE:
			return 0;
		case PATH_CMD_CUBICTO:
		case PATH_CMD_RCUBICTO:
			return 6;
		default:
			throw new RuntimeException("Source code error. Character "+cmdChar+" hasn't been handled.");	
		}
	}

	public static boolean cmdIsRepeatable(int currentChar) {
		switch(currentChar) {
		case PATH_CMD_LINETO:
		case PATH_CMD_RLINETO:
			return true;
		case PATH_CMD_MOVETO:
		case PATH_CMD_RMOVETO:
		case PATH_CMD_CLOSE:
			return false;
		case PATH_CMD_CUBICTO:
		case PATH_CMD_RCUBICTO:
			return true;
		default:
			throw new RuntimeException("Source code error. Character "+currentChar+" hasn't been handled.");	
		}
	}

	@Override
	public void translate(float dx, float dy) {
		for(PathIterator it = new PathIterator(); it.hasNext();) {
			it.readNext(null);
			int cmdLabel = it.currentChar != NO_VALUE ? it.currentChar :
				commands.charAt(it.specIndex- (it.isRepeatable ? 2 : 1));
			switch(cmdLabel) {
			case PATH_CMD_LINETO:
			case PATH_CMD_MOVETO:
			case PATH_CMD_CUBICTO:
				for(int i = 0; i < it.argsAmount; i++) {
					args[it.argsOffset-it.argsAmount+i] += i%2 == 0 ? dx : dy;
				}
			}
		}
	}
}
