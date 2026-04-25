package blob.vanillasquared.mixin.world.commands;

import blob.vanillasquared.main.world.recipe.enchanting.EnchantingRecipeRegistry;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.commands.RecipeCommand;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(RecipeCommand.class)
public abstract class RecipeCommandMixin {
    @Shadow @Final private static SimpleCommandExceptionType ERROR_GIVE_FAILED;
    @Shadow @Final private static SimpleCommandExceptionType ERROR_TAKE_FAILED;

    @Redirect(method = {"lambda$register$1", "lambda$register$3"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipes()Ljava/util/Collection;"))
    private static Collection<RecipeHolder<?>> vsq$getAllRecipesIncludingEnchanting(RecipeManager recipeManager) {
        List<RecipeHolder<?>> recipes = new ArrayList<>(recipeManager.getRecipes());
        recipes.addAll(EnchantingRecipeRegistry.recipeHolders());
        return recipes;
    }

    @Inject(method = "giveRecipes", at = @At("HEAD"), cancellable = true)
    private static void vsq$giveCustomEnchantingRecipes(CommandSourceStack source, Collection<ServerPlayer> targets, Collection<RecipeHolder<?>> recipes, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (recipes.stream().noneMatch(holder -> EnchantingRecipeRegistry.contains(holder.id()))) {
            return;
        }

        int changed = 0;
        List<RecipeHolder<?>> vanillaRecipes = recipes.stream()
                .filter(holder -> !EnchantingRecipeRegistry.contains(holder.id()))
                .toList();
        for (ServerPlayer player : targets) {
            changed += player.awardRecipes(vanillaRecipes);
            for (RecipeHolder<?> recipe : recipes) {
                if (EnchantingRecipeRegistry.contains(recipe.id()) && !player.getRecipeBook().contains(recipe.id())) {
                    player.getRecipeBook().add(recipe.id());
                    changed++;
                }
            }
        }
        if (changed == 0) {
            throw ERROR_GIVE_FAILED.create();
        }
        int recipeCount = recipes.size();
        int targetCount = targets.size();
        source.sendSuccess(() -> Component.translatable(
                targetCount == 1 ? "commands.recipe.give.success.single" : "commands.recipe.give.success.multiple",
                recipeCount,
                targetCount == 1 ? targets.iterator().next().getDisplayName() : targetCount
        ), true);
        cir.setReturnValue(changed);
    }

    @Inject(method = "takeRecipes", at = @At("HEAD"), cancellable = true)
    private static void vsq$takeCustomEnchantingRecipes(CommandSourceStack source, Collection<ServerPlayer> targets, Collection<RecipeHolder<?>> recipes, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (recipes.stream().noneMatch(holder -> EnchantingRecipeRegistry.contains(holder.id()))) {
            return;
        }

        int changed = 0;
        List<RecipeHolder<?>> vanillaRecipes = recipes.stream()
                .filter(holder -> !EnchantingRecipeRegistry.contains(holder.id()))
                .toList();
        for (ServerPlayer player : targets) {
            changed += player.resetRecipes(vanillaRecipes);
            for (RecipeHolder<?> recipe : recipes) {
                if (EnchantingRecipeRegistry.contains(recipe.id()) && player.getRecipeBook().contains(recipe.id())) {
                    player.getRecipeBook().remove(recipe.id());
                    changed++;
                }
            }
        }
        if (changed == 0) {
            throw ERROR_TAKE_FAILED.create();
        }
        int recipeCount = recipes.size();
        int targetCount = targets.size();
        source.sendSuccess(() -> Component.translatable(
                targetCount == 1 ? "commands.recipe.take.success.single" : "commands.recipe.take.success.multiple",
                recipeCount,
                targetCount == 1 ? targets.iterator().next().getDisplayName() : targetCount
        ), true);
        cir.setReturnValue(changed);
    }
}
