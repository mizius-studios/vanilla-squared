package blob.combatupdate.util.data;

public record GeneralWeapon(
        double attackDamage,
        double attackSpeed,
        double entityReach,
        int dura
) {
    public static final GeneralWeapon DEFAULT = new GeneralWeapon(0.0d, 0.0d,0.0d,0);
}
