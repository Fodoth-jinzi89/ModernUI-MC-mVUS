/*
 * Modern UI.
 * Copyright (C) 2024 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.mc.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import icyllis.modernui.mc.FontResourceManager;
import icyllis.modernui.mc.ModernUIClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Provide emoji shortcode suggestions
 */
@Mixin(CommandSuggestions.class)
public abstract class MixinCommandSuggestions {

    @Shadow
    @Final
    EditBox input;

    @Shadow
    @Final
    Minecraft minecraft;

    @Shadow
    @Final
    private boolean commandsOnly;

    @Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    private static int getLastWordIndex(String s) {
        return 0;
    }

    @Shadow
    public abstract void showSuggestions(boolean b);

    @Inject(method = "updateCommandInfo",
            at = @At("HEAD"),
            cancellable = true)
    private void onUpdateCommandInfoEmoji(CallbackInfo ci) {
        if (!ModernUIClient.sEmojiShortcodes) return;

        String command = this.input.getValue();
        int cursorPos = this.input.getCursorPosition();
        String partial = command.substring(0, cursorPos);
        int lastWord = getLastWordIndex(partial);

        // 仅在非命令文本中启用 emoji
        boolean startsWithSlash = partial.startsWith("/");
        boolean isCommand = this.commandsOnly || startsWithSlash;
        if (!isCommand && partial.startsWith(":", lastWord) && partial.length() - lastWord >= 2) {
            Collection<String> suggestions = FontResourceManager.getInstance()
                    .getEmojiShortcodes(partial.charAt(lastWord + 1));

            if (!suggestions.isEmpty()) {
                this.pendingSuggestions = SharedSuggestionProvider.suggest(
                        suggestions,
                        new SuggestionsBuilder(partial, lastWord)
                );

                this.pendingSuggestions.thenRun(() -> {
                    if (!this.pendingSuggestions.isDone()) return;
                    if (this.minecraft.options.autoSuggestions().get()) {
                        showSuggestions(false);
                    }
                });

                ci.cancel(); // 阻止原方法继续生成普通非命令建议
            }
        }
    }
}
