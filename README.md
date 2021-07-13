# RFC3339 DateTimeFormatter

This is a very simple JAVA code to test a custom build DateTimeFormatter to accodomate [RFC3339 date time format](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6), such as `1996-12-19T16:39:57-08:00`.

This works very well, with the only **exception of leap second** date time.

```JAVA
DateTimeFormatter rfc3339Formatter = new DateTimeFormatterBuilder()
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
        .appendFraction(ChronoField.NANO_OF_SECOND, 2, 9, true) //2nd parameter: 2 for JRE 11, 1 for JRE 16
        .optionalEnd()
        .appendOffset("+HH:MM","Z")
        .toFormatter()
        .withResolverStyle(ResolverStyle.STRICT);
```