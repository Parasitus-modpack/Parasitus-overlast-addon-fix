package com.overlast.gui;

import com.dhanantry.scapeandrunparasites.util.config.SRPConfigSystems;
import com.overlast.OverLast;
import com.overlast.config.OverConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;

public class RenderHUD extends Gui {

	private static final int HUD_MARGIN = 8;
	private static final int HUD_SPACING = 8;
	private static final int TEXT_BAR_GAP = 2;
	private static final int BACKDROP_PADDING = 4;
	private static final int BACKDROP_OUTLINE = 0xA0000000;
	private static final int BACKDROP_FILL = 0x66000000;
	private static final int TITLE_COLOR = 0xF0E6CF;
	private static final int VALUE_COLOR = 0xFFFFFF;

	//索引值
	private static int EvoIndex=1;
	//控制GUI开关
	public static boolean switchhud = true;
	public static boolean heldDirtyClock = false;

	public static int SanAdd = 0;
	// The stat bars themselves.
	private static final StatBar EVOLUTION_BAR = new StatBar(StatBar.StatType.EVOLUTION, 113, 29, 80, 23, 32, new ResourceLocation(OverLast.MOD_ID, "textures/gui/evolutionbar1.png"));

	// List of the main bars for easy iteration
	private static final StatBar[] MAIN_BARS = {EVOLUTION_BAR};


	// This method gets the correct stats of the player.  这个方法可以得到玩家的正确属性资料，通过服务端传入的数据包
	public static void retrieveStats(int phase, int evolution,boolean showRequestDirtyClock) {
		EvoIndex=phase;
		EVOLUTION_BAR.setValue(evolution);
		heldDirtyClock=showRequestDirtyClock;
		switch(phase) {
			case -2:
			case -1:EVOLUTION_BAR.setMaxValue(0);break;
			case 0:EVOLUTION_BAR.setMaxValue(SRPConfigSystems.phaseKillsOne);break;
			case 1:EVOLUTION_BAR.setMaxValue(SRPConfigSystems.phaseKillsTwo);break;
			case 2:EVOLUTION_BAR.setMaxValue(SRPConfigSystems.phaseKillsThree);break;
			case 3:EVOLUTION_BAR.setMaxValue(SRPConfigSystems.phaseKillsFour);break;
			case 4:EVOLUTION_BAR.setMaxValue(SRPConfigSystems.phaseKillsFive);break;
			case 5:EVOLUTION_BAR.setMaxValue(SRPConfigSystems.phaseKillsSix);break;
			case 6:EVOLUTION_BAR.setMaxValue(SRPConfigSystems.phaseKillsSeven);break;
			case 7:
			case 8:
				EVOLUTION_BAR.setMaxValue(SRPConfigSystems.phaseKillsEight);break;
		}
		EVOLUTION_BAR.setTexture(new ResourceLocation(OverLast.MOD_ID, "textures/gui/evolutionbar"+EvoIndex+".png"));
	}

	//最终渲染层
	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent event) {
		if(!switchhud){
			return;
		}
		if(OverConfig.MECHANICS.showRequestDirtyClock&&!heldDirtyClock) {
			return;
		}
		if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
			
			// Instance of Minecraft. All of this crap is client-side (well of course) Minecraft的实例。所有这些废话都是客户端的（当然是好）。
			Minecraft mc = Minecraft.getMinecraft();
			
			// Get current screen resolution.  获取当前的屏幕分辨率。
			ScaledResolution scaled = event.getResolution();
			int screenWidth = scaled.getScaledWidth();
			int screenHeight = scaled.getScaledHeight();

			// Variables used to render the bars.  用于渲染条形图的变量。
            int i = 0;
            ResourceLocation texture;
            int fullWidth;
            int fullHeight;
            int movingTextureX;
            int movingTextureY;
            int movingWidth;
            String valueText;
            int headerHeight = mc.fontRenderer.FONT_HEIGHT;

            // The loop that renders the main stat bars. 循环渲染主要属性条的。
			for (StatBar bar : MAIN_BARS) {
			    // Should this bar be displayed? 是否应该显示此栏？
                if (bar.shouldBeDisplayed()) {

                    // Get the stuff
                    texture = bar.getTexture();
                    fullWidth = bar.getFullWidth();
                    fullHeight = bar.getFullHeight();
                    movingTextureX = bar.getMovingTextureX();
                    movingTextureY = bar.getMovingTextureY();
                    movingWidth = bar.getMovingWidth();
                    valueText = bar.getTextToDisplay();

					int widgetHeight = headerHeight + TEXT_BAR_GAP + fullHeight;
					int left = getX(screenWidth, fullWidth);
					int top = getY(screenHeight, i, widgetHeight);
					int barTop = top + headerHeight + TEXT_BAR_GAP;
					int backdropLeft = left - BACKDROP_PADDING;
					int backdropTop = top - BACKDROP_PADDING;
					int backdropRight = left + fullWidth + BACKDROP_PADDING;
					int backdropBottom = barTop + fullHeight + BACKDROP_PADDING;
					i++;

					drawBackdrop(backdropLeft, backdropTop, backdropRight, backdropBottom);

                    // Actual rendering. First one is the moving bar. Second one is the whole bar.
                    mc.renderEngine.bindTexture(texture);
                    drawTexturedModalRect(left + bar.getFillRenderXOffset(), barTop + bar.getFillRenderYOffset(), movingTextureX, movingTextureY, movingWidth, fullHeight);
                    drawTexturedModalRect(left, barTop, 0, 0, fullWidth, fullHeight);

					String phaseText = getPhaseText();
					mc.fontRenderer.drawStringWithShadow(phaseText, left, top, TITLE_COLOR);
					int valueX = left + fullWidth - mc.fontRenderer.getStringWidth(valueText);
					mc.fontRenderer.drawStringWithShadow(valueText, valueX, top, VALUE_COLOR);
				}
            }

		}
	}

	
	// Help determine where to place a stat bar.
	// It's more of a base position, and will be modified for whatever texture it's for.

	// This'll either be right by 0 or right by the rightmost edge of the screen.
	// So pos doesn't actually matter.
	// 帮助确定放置状态栏的位置。
	// 这更像是一个基础位置，并且会根据它的纹理进行修改。
	// 这要么是在0的右边，要么是在屏幕的最右边的边缘。
	// 所以位置实际上并不重要。
	private int getX(int screenWidth, int fullWidth) {
		String barPosition = getBarPosition();

		if (barPosition.endsWith("left")) {
			return HUD_MARGIN;
		}

		return screenWidth - fullWidth - HUD_MARGIN;
	}
	
	// The stat bars are 20 pixels apart, vertically. 统计条在垂直方向上相隔20像素。
	private int getY(int screenHeight, int pos, int widgetHeight) {
		String barPosition = getBarPosition();
		int step = widgetHeight + HUD_SPACING;

		if (barPosition.startsWith("top")) {
			return HUD_MARGIN + (step * pos);
		}

		if (barPosition.startsWith("bottom")) {
			return screenHeight - widgetHeight - HUD_MARGIN - (step * pos);
		}

		return (screenHeight / 2) - (widgetHeight / 2) + (step * pos);
	}

	private String getBarPosition() {
		if (OverConfig.CLIENT.barPositions == null) {
			return "middle right";
		}

		return OverConfig.CLIENT.barPositions.toLowerCase(Locale.ROOT);
	}

	private String getPhaseText() {
		if (EvoIndex < 0) {
			return "Evolution Dormant";
		}

		return "Phase " + EvoIndex;
	}

	private void drawBackdrop(int left, int top, int right, int bottom) {
		drawRect(left, top, right, bottom, BACKDROP_OUTLINE);
		drawRect(left + 1, top + 1, right - 1, bottom - 1, BACKDROP_FILL);
	}
}
