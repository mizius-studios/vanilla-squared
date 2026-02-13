package blob.vanillasquared.util.data;

public record GeneralWeapon(
        double attackDamage,
        double attackSpeed,
        double entityReach
) {
    public static final GeneralWeapon DEFAULT = new GeneralWeapon(0.0d, 0.0d,0.0d);
}
