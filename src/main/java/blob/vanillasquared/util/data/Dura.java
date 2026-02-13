package blob.vanillasquared.util.data;

public record Dura(
        int dura
) {
    public static final Dura DEFAULT = new Dura(2147483647);
}
