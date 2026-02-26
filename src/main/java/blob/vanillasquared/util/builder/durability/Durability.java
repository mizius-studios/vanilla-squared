package blob.vanillasquared.util.builder.durability;

public record Durability(
        int durability
) {
    public static final Durability DEFAULT = new Durability(2147483647);
}
