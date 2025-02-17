
public class Foo {


    public String formatToFixedSize(final String string, final int size) {
        return StringUtils.rightPad(StringUtils.trimToEmpty(StringUtils.left(string != null ? string : "", size)), size, '\u00A0');
    }
}