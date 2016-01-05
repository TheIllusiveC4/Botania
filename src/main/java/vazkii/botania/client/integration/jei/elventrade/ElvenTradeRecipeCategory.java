package vazkii.botania.client.integration.jei.elventrade;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import vazkii.botania.client.core.handler.RenderEventHandler;
import vazkii.botania.common.block.ModBlocks;

import javax.annotation.Nonnull;
import java.util.Collection;

public class ElvenTradeRecipeCategory implements IRecipeCategory {

    private final String localizedName;
    private final IDrawable background;
    private final IDrawable overlay;

    public ElvenTradeRecipeCategory(IGuiHelper guiHelper) {
        localizedName = StatCollector.translateToLocal("botania.nei.elvenTrade");
        background = guiHelper.createBlankDrawable(145, 95);
        overlay = guiHelper.createDrawable(new ResourceLocation("botania", "textures/gui/elvenTradeOverlay.png"), 0, 15, 140, 90);
    }

    @Nonnull
    @Override
    public String getUid() {
        return "botania.elvenTrade";
    }

    @Nonnull
    @Override
    public String getTitle() {
        return localizedName;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        overlay.draw(minecraft, 0, 4);
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
    }

    @Override
    public void drawAnimations(Minecraft minecraft) {
        minecraft.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        TextureAtlasSprite sprite = RenderEventHandler.INSTANCE.alfPortalTex;
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        int startX = 22;
        int startY = 25;
        int stopX = 70;
        int stopY = 73;
        wr.pos(startX, startY, 0).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        wr.pos(startX, stopY, 0).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        wr.pos(stopX, stopY, 0).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        wr.pos(stopX, startY, 0).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
        tess.draw();
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
        if (!(recipeWrapper instanceof ElvenTradeRecipeWrapper))
            return;
        ElvenTradeRecipeWrapper wrapper = ((ElvenTradeRecipeWrapper) recipeWrapper);

        int index = 0, posX = 42;
        for (Object o : wrapper.getInputs()) {
            recipeLayout.getItemStacks().init(index, true, posX, 0);
            if (o instanceof Collection) {
                recipeLayout.getItemStacks().set(index, ((Collection<ItemStack>) o));
            } else {
                recipeLayout.getItemStacks().set(index, ((ItemStack) o));
            }
            index++;
            posX += 18;
        }

        recipeLayout.getItemStacks().init(index, false, 93, 41);
        recipeLayout.getItemStacks().set(index, wrapper.getOutputs().get(0));
    }

}
