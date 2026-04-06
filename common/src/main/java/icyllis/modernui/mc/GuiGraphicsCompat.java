/*
 * Modern UI.
 * Copyright (C) 2026 BloCamLimb. All rights reserved.
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

package icyllis.modernui.mc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

import java.lang.reflect.Constructor;

/**
 * Patch-level compatibility helpers for {@link GuiGraphicsExtractor} constructors.
 */
public final class GuiGraphicsCompat {

    private static final Constructor<GuiGraphicsExtractor> CTOR_WITH_DIMS =
            resolveConstructor(Minecraft.class, GuiRenderState.class, int.class, int.class);
    private static final Constructor<GuiGraphicsExtractor> CTOR_NO_DIMS =
            resolveConstructor(Minecraft.class, GuiRenderState.class);

    private GuiGraphicsCompat() {
    }

    public static GuiGraphicsExtractor create(Minecraft minecraft, GuiRenderState renderState, int guiWidth, int guiHeight) {
        try {
            if (CTOR_WITH_DIMS != null) {
                return CTOR_WITH_DIMS.newInstance(minecraft, renderState, guiWidth, guiHeight);
            }
            if (CTOR_NO_DIMS != null) {
                return CTOR_NO_DIMS.newInstance(minecraft, renderState);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct GuiGraphicsExtractor", e);
        }
        throw new IllegalStateException("No compatible GuiGraphicsExtractor constructor found");
    }

    @SuppressWarnings("unchecked")
    private static Constructor<GuiGraphicsExtractor> resolveConstructor(Class<?>... parameterTypes) {
        try {
            return (Constructor<GuiGraphicsExtractor>) GuiGraphicsExtractor.class.getConstructor(parameterTypes);
        } catch (Exception ignored) {
            return null;
        }
    }
}

