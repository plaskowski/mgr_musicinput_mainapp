package pl.edu.mimuw.students.pl249278.android.musicinput.test;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;
import pl.edu.mimuw.students.pl249278.android.svg.SvgParser;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.widget.ImageView;

public class SvgTestActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.test_svgtest);
        
        ImageView imgV = (ImageView) findViewById(R.id.test_imgview);
        Bitmap b = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        SvgParser parser = new SvgParser();
        try {
			SvgImage svgImg = parser.parse(getResources().getXml(R.xml.test_obrazek));
	        SvgPath svgPath = (SvgPath) svgImg.objects.get(1);
	        Path path;
	        
			path = generate(svgPath, 0.5f);
			c.drawPath(path, paint);
			
			path = generate(svgPath, 1.5f);
			c.drawPath(path, paint);
			
			path = generate(svgPath, 1f);
			paint.setColor(Color.GRAY);
			c.drawPath(path, paint);
			
	        imgV.setImageBitmap(b);
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
	}
	
	private Path generate(SvgPath path, float scale) {
		Path result = new Path();
		int length = path.commands.length();
		int argIndex = 0;
		float[] args;
		if(scale != 1) {
			args = new float[path.args.length];
			System.arraycopy(path.args, 0, args, 0, path.args.length);
			for (int i = 0; i < args.length; i++) {
				args[i] *= scale;
			}
		} else {
			args = path.args;
		}
		for (int i = 0; i < length; i++) {
			char cmd = path.commands.charAt(i);
			switch(cmd) {
			case 'M':
				result.moveTo(args[argIndex++], args[argIndex++]);
				break;
			case 'm':
				result.rMoveTo(args[argIndex++], args[argIndex++]);
				break;
			case 'L':
			case 'l':
			case 'C':
			case 'c':
				int repeats = path.commands.charAt(++i);
				for(int j = 0; j < repeats; j++) { switch(cmd) {
					case 'L':
						result.lineTo(args[argIndex++], args[argIndex++]);
						break;
					case 'l':
						result.rLineTo(args[argIndex++], args[argIndex++]);
						break;
					case 'C':
						result.cubicTo(args[argIndex++], args[argIndex++], args[argIndex++], args[argIndex++], args[argIndex++], args[argIndex++]);
						break;
					case 'c':
						result.rCubicTo(args[argIndex++], args[argIndex++], args[argIndex++], args[argIndex++], args[argIndex++], args[argIndex++]);
						break;
				}}
				break;
			case 'Z':
			case 'z':
				result.close();
			}
		}
		return result;
	}	
}
