package zedzee.github.io.chips.client.screen;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.screen.ChiselingStationScreenHandler;

public class ChiselingStationScreen extends HandledScreen<ChiselingStationScreenHandler> {
    private static final Identifier TEXTURE = Chips.identifier("textures/gui/container/chiseling_station.png");

    public static void register() {
        HandledScreens.register(ChiselingStationScreenHandler.CHISELING_STATION, ChiselingStationScreen::new);
    }

    public ChiselingStationScreen(ChiselingStationScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
