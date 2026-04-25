package blob.vanillasquared.mixin.world.commands;

import blob.vanillasquared.main.world.recipe.enchanting.EnchantingRecipeRegistry;
import blob.vanillasquared.main.world.recipe.enchanting.EnchantingRecipeBookNotifier;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.commands.RecipeCommand;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import com.mojang.brigadier.CommandDispatcher;
import java.util.List;

@Mixin(RecipeCommand.class)
public abstract class RecipeCommandMixin {
    @Shadow @Final private static SimpleCommandExceptionType ERROR_GIVE_FAILED;
    @Shadow @Final private static SimpleCommandExceptionType ERROR_TAKE_FAILED;
    @Shadow private static int giveRecipes(CommandSourceStack source, Collection<ServerPlayer> players, Collection<RecipeHolder<?>> recipes) throws CommandSyntaxException { throw new AssertionError(); }
    @Shadow private static int takeRecipes(CommandSourceStack source, Collection<ServerPlayer> players, Collection<RecipeHolder<?>> recipes) throws CommandSyntaxException { throw new AssertionError(); }

    /**
     * @author Codex
     * @reason Include custom enchanting recipes in `/recipe give|take *`.
     */
    @Overwrite
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("recipe")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(
                                Commands.literal("give")
                                        .then(
                                                Commands.argument("targets", EntityArgument.players())
                                                        .then(
                                                                Commands.argument("recipe", ResourceKeyArgument.key(Registries.RECIPE))
                                                                        .executes(c -> giveRecipes(c.getSource(), EntityArgument.getPlayers(c, "targets"), java.util.Collections.singleton(ResourceKeyArgument.getRecipe(c, "recipe"))))
                                                        )
                                                        .then(
                                                                Commands.literal("*")
                                                                        .executes(c -> giveRecipes(c.getSource(), EntityArgument.getPlayers(c, "targets"), vsq$allRecipes(c.getSource())))
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("take")
                                        .then(
                                                Commands.argument("targets", EntityArgument.players())
                                                        .then(
                                                                Commands.argument("recipe", ResourceKeyArgument.key(Registries.RECIPE))
                                                                        .executes(c -> takeRecipes(c.getSource(), EntityArgument.getPlayers(c, "targets"), java.util.Collections.singleton(ResourceKeyArgument.getRecipe(c, "recipe"))))
                                                        )
                                                        .then(
                                                                Commands.literal("*")
                                                                        .executes(c -> takeRecipes(c.getSource(), EntityArgument.getPlayers(c, "targets"), vsq$allRecipes(c.getSource())))
                                                        )
                                        )
                        )
        );
    }

    @Unique
    private static Collection<RecipeHolder<?>> vsq$allRecipes(CommandSourceStack source) {
        List<RecipeHolder<?>> recipes = new ArrayList<>(source.getServer().getRecipeManager().getRecipes());
        recipes.addAll(EnchantingRecipeRegistry.recipeHolders());
        return List.copyOf(recipes);
    }

    @Inject(method = "giveRecipes", at = @At("HEAD"), cancellable = true)
    private static void vsq$giveCustomEnchantingRecipes(CommandSourceStack source, Collection<ServerPlayer> players, Collection<RecipeHolder<?>> recipes, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (recipes.stream().noneMatch(holder -> EnchantingRecipeRegistry.contains(holder.id()))) {
            return;
        }

        int changed = 0;
        List<RecipeHolder<?>> vanillaRecipes = recipes.stream()
                .filter(holder -> !EnchantingRecipeRegistry.contains(holder.id()))
                .toList();
        for (ServerPlayer player : players) {
            changed += player.awardRecipes(vanillaRecipes);
            for (RecipeHolder<?> recipe : recipes) {
                if (EnchantingRecipeRegistry.contains(recipe.id()) && !player.getRecipeBook().contains(recipe.id())) {
                    changed += EnchantingRecipeBookNotifier.unlock(player, recipe.id());
                }
            }
        }
        if (changed == 0) {
            throw ERROR_GIVE_FAILED.create();
        }
        int recipeCount = changed;
        int targetCount = players.size();
        source.sendSuccess(() -> Component.translatable(
                targetCount == 1 ? "commands.recipe.give.success.single" : "commands.recipe.give.success.multiple",
                recipeCount,
                targetCount == 1 ? players.iterator().next().getDisplayName() : targetCount
        ), true);
        cir.setReturnValue(changed);
    }

    @Inject(method = "takeRecipes", at = @At("HEAD"), cancellable = true)
    private static void vsq$takeCustomEnchantingRecipes(CommandSourceStack source, Collection<ServerPlayer> players, Collection<RecipeHolder<?>> recipes, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (recipes.stream().noneMatch(holder -> EnchantingRecipeRegistry.contains(holder.id()))) {
            return;
        }

        int changed = 0;
        List<RecipeHolder<?>> vanillaRecipes = recipes.stream()
                .filter(holder -> !EnchantingRecipeRegistry.contains(holder.id()))
                .toList();
        for (ServerPlayer player : players) {
            changed += player.resetRecipes(vanillaRecipes);
            for (RecipeHolder<?> recipe : recipes) {
                if (EnchantingRecipeRegistry.contains(recipe.id()) && player.getRecipeBook().contains(recipe.id())) {
                    changed += EnchantingRecipeBookNotifier.revoke(player, recipe.id());
                }
            }
        }
        if (changed == 0) {
            throw ERROR_TAKE_FAILED.create();
        }
        int recipeCount = changed;
        int targetCount = players.size();
        source.sendSuccess(() -> Component.translatable(
                targetCount == 1 ? "commands.recipe.take.success.single" : "commands.recipe.take.success.multiple",
                recipeCount,
                targetCount == 1 ? players.iterator().next().getDisplayName() : targetCount
        ), true);
        cir.setReturnValue(changed);
    }
}
