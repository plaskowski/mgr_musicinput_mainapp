package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import java.util.TimeZone;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Time;

import com.seppius.i18n.plurals.PluralResources;

public class DateRelativeFormatHelper {
	private final PluralResources pluralResources;
	private final Context ctx;
	
	public DateRelativeFormatHelper(Context ctx) {
		this.ctx = ctx;
		try {
			this.pluralResources = new PluralResources(ctx.getResources());
		} catch (Exception e) {
			throw new RuntimeException("Failed to load Plurals", e);
		}
	}

	public CharSequence formatDate(long utcStamp) {
		long now = System.currentTimeMillis();
		long diff = now - utcStamp;
		if(diff < DateUtils.HOUR_IN_MILLIS) {
			int minutes = (int) (diff/DateUtils.MINUTE_IN_MILLIS);
			return pluralResources.getQuantityString(R.plurals.format_minutes_ago, minutes, minutes);
		} else if(diff < DateUtils.HOUR_IN_MILLIS * 5) {
			int hours = (int) (diff/DateUtils.HOUR_IN_MILLIS);
			String string = pluralResources.getQuantityString(R.plurals.format_hours_ago, hours, hours);
			return string;
		} else {
			TimeZone tz = TimeZone.getDefault();
			int nowDay = Time.getJulianDay(now, tz.getRawOffset());
			int thenDay = Time.getJulianDay(utcStamp, tz.getRawOffset());
			int days = nowDay - thenDay;
			if(days >= 0 && days < 3) {
				Time time = new Time();
				time.set(utcStamp);
				return pluralResources.getQuantityString(R.plurals.format_days_ago, days, 
					days, time.hour, time.minute
				);
			} else {
				return DateUtils.getRelativeDateTimeString(ctx, utcStamp, 
					DateUtils.MINUTE_IN_MILLIS, 3*DateUtils.DAY_IN_MILLIS, 0);
			}
		}
	}
}
