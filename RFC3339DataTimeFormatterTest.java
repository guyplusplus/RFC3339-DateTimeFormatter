import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;

public class RFC3339DataTimeFormatterTest {

    public static DateTimeFormatter rfc3339Formatter = null;
    
    static {
    	rfc3339Formatter = new DateTimeFormatterBuilder()
    			.parseCaseInsensitive()
    			.appendValue(ChronoField.YEAR, 4)
    			.appendLiteral('-')
    			.appendValue(ChronoField.MONTH_OF_YEAR, 2)
    			.appendLiteral('-')
    			.appendValue(ChronoField.DAY_OF_MONTH, 2)
    			.appendLiteral('T')
    			.appendValue(ChronoField.HOUR_OF_DAY, 2)
    			.appendLiteral(':')
    			.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
    			.appendLiteral(':')
    			.appendValue(ChronoField.SECOND_OF_MINUTE, 2)
    			.optionalStart()
    			.appendFraction(ChronoField.NANO_OF_SECOND, 2, 9, true) //2nd parameter: 2 for JRE (8, 11 LTS), 1 for JRE (17 LTS)
    			.optionalEnd()
    			.appendOffset("+HH:MM","Z")
    			.toFormatter()
    			.withResolverStyle(ResolverStyle.STRICT);
    }

    public static ZonedDateTime parseRfc3339(String rfcDateTime) {
        return ZonedDateTime.parse(rfcDateTime, rfc3339Formatter);
    }

    @Test
    void testHourWithTimezone() {
        Assertions.assertEquals("2019-07-19T11:44:39.812Z", parseRfc3339("2019-07-19T10:14:39.812-01:30").withZoneSameInstant(ZoneOffset.UTC).toString());
        Assertions.assertEquals("2019-07-19T11:44:39.810Z", parseRfc3339("2019-07-19T10:14:39.81-01:30").withZoneSameInstant(ZoneOffset.UTC).toString());
        Assertions.assertEquals("2019-07-19T11:44:39.010Z", parseRfc3339("2019-07-19T10:14:39.01-01:30").withZoneSameInstant(ZoneOffset.UTC).toString());
        Assertions.assertEquals("2019-07-19T10:14:39.01-01:30", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(parseRfc3339("2019-07-19T10:14:39.01-01:30")));
        Assertions.assertEquals("2019-07-19T11:44:39.812300Z", parseRfc3339("2019-07-19T10:14:39.8123-01:30").withZoneSameInstant(ZoneOffset.UTC).toString());
        int hour = parseRfc3339("2019-07-19T10:14:39.812+01:00").withZoneSameInstant(ZoneOffset.UTC).getHour();
        Assertions.assertEquals(9, hour);
    }

    @Test
    void testFormatter() {
        String sample = "2019-07-19T10:14:39.0123-01:30";
        Assertions.assertEquals(sample, rfc3339Formatter.format(parseRfc3339(sample)));
        sample = "2019-07-19t10:14:39.0123z";
        Assertions.assertEquals(sample.toUpperCase(), rfc3339Formatter.format(parseRfc3339(sample)));
        sample = "2019-07-19T10:14:39.0123+00:00";
        Assertions.assertEquals(sample.replace("+00:00", "Z"), rfc3339Formatter.format(parseRfc3339(sample)));
    }
    
    @Test
    void testUnknownLocalOffset() {
        int hour = parseRfc3339("2019-07-19T10:14:39.812-00:00").withZoneSameInstant(ZoneOffset.UTC).getHour();
        Assertions.assertEquals(10, hour);
    }

    /**
     * Examples are taken from <a href="https://www.ietf.org/rfc/rfc3339.txt"> RFC 3339 standard </a>
     * Except Leap Second which fails
     */
    @Test
    void testParsingDifferentDateTimes1() {
        Assertions.assertDoesNotThrow(exParseRfc3339("1985-04-12T23:20:50.52Z"));
        Assertions.assertDoesNotThrow(exParseRfc3339("1996-12-19T16:39:57-08:00"));
        Assertions.assertDoesNotThrow(exParseRfc3339("1937-01-01T12:00:27.87+00:20"));
    }

    @Test
    void testParsingDifferentDateTimes2() {
        Assertions.assertDoesNotThrow(exParseRfc3339("1985-04-12t23:20:50z")); //small letters
        Assertions.assertDoesNotThrow(exParseRfc3339("1985-04-12T23:20:50.0Z"));
        Assertions.assertDoesNotThrow(exParseRfc3339("1985-04-12T23:20:50.1Z"));
        Assertions.assertDoesNotThrow(exParseRfc3339("1985-04-12T23:20:50.00Z"));
        Assertions.assertDoesNotThrow(exParseRfc3339("1985-04-12T23:20:50.52Z"));
        Assertions.assertDoesNotThrow(exParseRfc3339("1985-04-12T23:20:50.520Z"));
        Assertions.assertDoesNotThrow(exParseRfc3339("1985-04-12T23:20:50.521Z"));
        Assertions.assertDoesNotThrow(exParseRfc3339("1996-12-19T16:39:57+08:00"));
        Assertions.assertDoesNotThrow(exParseRfc3339("1985-04-12T23:20:50.123456789Z")); //nanoseconds
        Assertions.assertDoesNotThrow(exParseRfc3339("2020-02-29T23:20:50Z")); //29-Feb on leap year
    }

    @Test
    void testParsingInvalidTZ() {
        String okDateTimeNoMsNoTZ = "1996-12-19T16:39:57";
        Assertions.assertDoesNotThrow(exParseRfc3339(okDateTimeNoMsNoTZ + "Z"));
        
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ)); //no TZ
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + "Z-08:00")); //wired TZ
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + "Z08:00")); //wired TZ
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + "+08:00Z")); //wired TZ
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + "+2:00")); //TZ 1 digit hour
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + "+:00")); //TZ 0 digit hour
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + "+02:3")); //TZ 1 digit minute
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + "+02:")); //TZ 0 digit minute
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + "+02")); //TZ no minutes
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + "+24:00")); //TZ with hours >= 24
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + "+0800")); //no column in TZ
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + "+08:00:")); //2 columns in TZ
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + "08:00")); //no sign in TZ
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + "00:00")); //no sign in TZ
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339(okDateTimeNoMsNoTZ + " +08:00")); //space in TZ
    }
    
    @Test
    void testParsingInvalidDateTimes() {
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339("2021-02-29T23:20:50Z")); //29-Feb on non leap year
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339("1985-04-12T23:20Z")); //no second
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339("1985-4-12T23:20:50.52Z")); //1 digit month
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339("1985-04-12T23:20:5.52Z")); //1 digit second
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339("85-04-12T23:20:50.52Z")); //2 digits year
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339("1999-01-01T24:00:27.87+00:20")); //24 hours
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339("1985-04-12T23:20:90.52Z")); //wrong seconds >60
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339("1985-04-12T23:20:50.Z")); //no millisecond but have period
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339("1985-04-12T23:20:50.1234567891Z")); //More than 9 digits in frac-sec (nano)
    }
    
    /**
     * This test succeeds on JDK11 up but fails on JDK 8...
     */
    @Test
    void testParsingInvalidDateTimesOnJDK11() {
    	Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339("2019-07-19T10:14:39.812+08:00:30")); //seconds in TZ
    }
    
    @Test
    void testLeapSeconds_WRONG_PARSING() {
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339("1990-12-31T23:59:60Z"));
        Assertions.assertThrows(DateTimeParseException.class, exParseRfc3339("1990-12-31T15:59:60-08:00"));
    }
    
    private static Executable exParseRfc3339(String toParse) {
        return () -> parseRfc3339(toParse);
    }
}