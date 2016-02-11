package info.novatec.inspectit.rcp.formatter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * This class is for formatting some output.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public final class NumberFormatter {

	/**
	 * Formats a decimal number with the specific pattern.
	 */
	private static DecimalFormat decFormat = new DecimalFormat("###0.##");

	/**
	 * Formats a decimal number and returns it in milliseconds format.
	 */
	private static DecimalFormat millisFormat = new DecimalFormat("0.00");

	/**
	 * Formats a decimal number and returns it in nanoseconds format.
	 */
	private static DecimalFormat nanosFormat = new DecimalFormat("0.00");

	/**
	 * Formats a decimal number with the specified pattern.
	 */
	private static DecimalFormat cpuFormat = new DecimalFormat("#.##");

	/**
	 * Formats a decimal number with the specified pattern.
	 */
	private static DecimalFormat intFormat = new DecimalFormat("###");

	/**
	 * Formats a decimal number with the specified pattern.
	 */
	private static DecimalFormat doubleFormat = new DecimalFormat("0.000");

	/**
	 * Formats a decimal number without the specified pattern.
	 */
	private static DecimalFormat doubleUnspecificFormat = new DecimalFormat();

	/**
	 * Formats a date/time value with the specified pattern.
	 */
	private static DateFormat dateMillisFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

	/**
	 * Formats a date/time value with the specified pattern.
	 */
	private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	static {
		doubleUnspecificFormat.setGroupingSize(0);
	}

	/**
	 * The default private constructor.
	 */
	private NumberFormatter() {
	}

	/**
	 * Converts time in milliseconds to a <code>String</code> in the format HH:mm:ss.
	 * 
	 * @param time
	 *            the time in milliseconds.
	 * @return a <code>String</code> representing the time in the format HH:mm:ss.
	 */
	public static String millisecondsToString(long time) {
		int seconds = (int) (time / 1000 % 60);
		int minutes = (int) ((time / 60000) % 60);
		int hours = (int) ((time / 3600000) % 24);
		int days = (int) ((time / 3600000) / 24);

		StringBuilder builder = new StringBuilder();
		builder.append(days);
		builder.append("d ");
		builder.append(hours);
		builder.append("h ");
		builder.append(minutes);
		builder.append("m ");
		builder.append(seconds);
		builder.append('s');

		return builder.toString();
	}

	/**
	 * Formats the time to a String value with milliseconds.
	 * 
	 * @param time
	 *            The time as long value.
	 * @return The formatted string.
	 */
	public static String formatTimeWithMillis(long time) {
		return formatTimeWithMillis(new Date(time));
	}

	/**
	 * Formats the time to a String value with milliseconds.
	 * 
	 * @param date
	 *            The date to format.
	 * @return The formatted string.
	 */
	public static String formatTimeWithMillis(Date date) {
		synchronized (dateMillisFormat) {
			return dateMillisFormat.format(date);
		}
	}

	/**
	 * Formats the time to a String value.
	 * 
	 * @param time
	 *            The time as long value.
	 * @return The formatted string.
	 */
	public static String formatTime(long time) {
		return formatTime(new Date(time));
	}

	/**
	 * Formats the time to a String value.
	 * 
	 * @param date
	 *            The date to format.
	 * @return The formatted string.
	 */
	public static String formatTime(Date date) {
		synchronized (dateFormat) {
			return dateFormat.format(date);
		}
	}

	/**
	 * Formats nanoseconds to seconds.
	 * 
	 * @param time
	 *            The time as long value.
	 * @return A formatted string.
	 */
	public static String formatNanosToSeconds(long time) {
		double sec = time / 1000000000d;
		return nanosFormat.format(sec) + " s";
	}

	/**
	 * Formats milliseconds to seconds.
	 * 
	 * @param time
	 *            The time as long value.
	 * @return A formatted string.
	 */
	public static String formatMillisToSeconds(long time) {
		double sec = time / 1000d;
		return millisFormat.format(sec) + " s";
	}

	/**
	 * Formats bytes to kiloBytes.
	 * 
	 * @param bytes
	 *            The bytes to format.
	 * @return A formatted string.
	 */
	public static String formatBytesToKBytes(long bytes) {
		return decFormat.format((double) bytes / 1024) + " Kb";
	}

	/**
	 * Formats bytes to megaBytes.
	 * 
	 * @param bytes
	 *            The bytes to format.
	 * @return A formatted string.
	 */
	public static String formatBytesToMBytes(long bytes) {
		return decFormat.format((double) bytes / (1024 * 1024)) + " Mb";
	}

	/**
	 * Adds a %-sign to a floating number. For example: input = 12 / output = 12 %.
	 * 
	 * @param percent
	 *            The value to format.
	 * @return The formatted string.
	 */
	public static String formatCpuPercent(float percent) {
		return cpuFormat.format(percent) + " %";
	}

	/**
	 * Formats an integer value. For example: input = 1234567 / output = 1,234,567.
	 * 
	 * @param number
	 *            The value to format.
	 * @return The formatted String.
	 */
	public static String formatInteger(int number) {
		return intFormat.format(number);
	}

	/**
	 * Formats a long value. For example: input = 1234567 / output = 1,234,567.
	 * 
	 * @param number
	 *            The value to format.
	 * @return The formatted string.
	 */
	public static String formatLong(long number) {
		return intFormat.format(number);
	}

	/**
	 * Formats a double value. For example: input = 123545.9876543 / output = 123545.987.
	 * 
	 * @param number
	 *            The value to format.
	 * @return The formatted string.
	 */
	public static String formatDouble(double number) {
		return doubleFormat.format(number);
	}

	/**
	 * Formats a double value based on the number of decimal places.
	 * 
	 * @param number
	 *            The value to format.
	 * @param decimalPlaces
	 *            Number of decimal places.
	 * @return The formatted string.
	 */
	public static String formatDouble(double number, int decimalPlaces) {
		doubleUnspecificFormat.setMaximumFractionDigits(decimalPlaces);
		doubleUnspecificFormat.setMinimumFractionDigits(decimalPlaces);
		return doubleUnspecificFormat.format(number);
	}

	/**
	 * Returns the human readable bytes number.
	 * <p>
	 * <b>IMPORTANT:</b> The method code is copied/taken/based from <a href=
	 * "http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java"
	 * >stackoverflow</a>. Original author is aioobe. License info can be found <a
	 * href="http://creativecommons.org/licenses/by-sa/3.0/">here</a>.
	 * 
	 * @param bytes
	 *            Bytes to transform.
	 * @return Human readable string.
	 */
	public static String humanReadableByteCount(long bytes) {
		int unit = 1024;
		if (bytes < unit) {
			return bytes + " B";
		} else {
			int exp = (int) (Math.log(bytes) / Math.log(unit));
			String pre = ("KMGTPE").charAt(exp - 1) + "i";
			return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
		}
	}

	/**
	 * Returns the human readable millis count.
	 * 
	 * If <b>shortDescription</b> is <code>true</code> then the format will be: x
	 * days/hours/minutes/seconds depending on the time. Meaning if more than day has passed only 1
	 * day will be returned. Otherwise if between 23-24 hours passed, 23 hours will be returned.
	 * 
	 * If <b>shortDescrption</b> is <code>false</code> then the the returned format is xd, xh, xm,
	 * xs.. Not that if any unit is 0 it won't be printed.
	 * 
	 * @param millis
	 *            Number of milliseconds.
	 * @param shortDescription
	 *            Short or long description.
	 * @return Formated string.
	 */
	public static String humanReadableMillisCount(long millis, boolean shortDescription) {
		StringBuilder stringBuilder = new StringBuilder();
		boolean started = false;

		long days = TimeUnit.MILLISECONDS.toDays(millis);
		if (days > 0) {
			if (shortDescription) {
				return days + " day" + (days > 1 ? "s" : "");
			}
			stringBuilder.append(String.format("%dd", days));
			started = true;
		}

		long hours = TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis));
		if (started) {
			stringBuilder.append(String.format(" %dh", hours));
		} else if (hours > 0) {
			if (shortDescription) {
				return hours + " hour" + (hours > 1 ? "s" : "");
			}
			stringBuilder.append(String.format("%dh", hours));
			started = true;
		}

		long min = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
		if (started) {
			stringBuilder.append(String.format(" %dm", min));
		} else if (min > 0) {
			if (shortDescription) {
				return min + " minute" + (min > 1 ? "s" : "");
			}
			stringBuilder.append(String.format("%dm", min));
			started = true;
		}

		long sec = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
		if (started) {
			stringBuilder.append(String.format(" %ds", sec));
		} else if (sec > 0) {
			if (shortDescription) {
				return sec + " second" + (sec > 1 ? "s" : "");
			}
			stringBuilder.append(String.format("%ds", sec));
			started = true;
		}

		return stringBuilder.toString();
	}

	/**
	 * Converts a double into a percentage string. For example: input = 0.1525 / output = 15.25 %
	 * 
	 * @param percentage
	 *            the percentage value
	 * @return The formatted string
	 */
	public static String formatDoubleToPercent(double percentage) {
		return doubleFormat.format(percentage * 100D) + " %";
	}

}
