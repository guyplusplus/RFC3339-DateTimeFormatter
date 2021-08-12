# RFC3339 DateTimeFormatter

This is a very simple JAVA code to test a DateTimeFormatter designed to **strickly** parse [RFC3339 Internet date/time format](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6), such as `1996-12-19T16:39:57-08:00`.

This works very well, with the only **exception of leap second** date time.

```JAVA
DateTimeFormatter rfc3339Parser = new DateTimeFormatterBuilder()
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
        .withResolverStyle(ResolverStyle.STRICT)
        .withChronology(IsoChronology.INSTANCE);

DateTimeFormatter rfc3339Formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
```