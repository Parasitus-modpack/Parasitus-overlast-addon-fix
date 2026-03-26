package com.overlast.gui;

import com.overlast.config.OverConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

public class StatBar {
	
	/*
	 * Defines a statistic bar to be displayed on the GUI, the HUD, whatever you wanna call it.
	 */
	
	// Type
	public enum StatType { SANITY, EVOLUTION }
	private StatType type;


	// Texture
	private ResourceLocation texture;
	
	// Full width and height  
	private int fullWidth;
	private int fullHeight;
	
	// Full width of the bar that actually MOVES.
	private int defaultBarWidth;
	
	// Starting position of the moving bar in the texture file. 
	private int movingTextureX;
	private int movingTextureY;

	// Values it holds (actual and max)  
    private float value = 0f;
    private float maxValue = 100f;
    private float displayValue = 0f;
    private long lastAnimationTick = System.currentTimeMillis();

    private static final float ANIMATION_SPEED = 12.0f;
	
	public StatBar(StatType type, int fullWidth, int fullHeight, int defaultBarWidth, int movingTextureX, int movingTextureY, ResourceLocation texture) {
		
		this.type = type;
		this.fullWidth = fullWidth;
		this.fullHeight = fullHeight;
		this.defaultBarWidth = defaultBarWidth;
		this.movingTextureX = movingTextureX;
		this.movingTextureY = movingTextureY;
		this.texture = texture;
	}

	public void setValue(float value) {

	    this.value = Math.max(0f, value);
    }

    public void setMaxValue(float value) {

	    this.maxValue = Math.max(0f, value);
	    if (this.maxValue <= 0f) {
	    	this.displayValue = 0f;
	    } else {
	    	this.displayValue = Math.min(this.displayValue, this.maxValue);
	    }
    }

	
	public int getFullWidth() {
		
		return this.fullWidth;
	}
	
	public int getFullHeight() {
		
		return this.fullHeight;
	}
	
	public int getMovingTextureX() {
		
		return this.movingTextureX;
	}
	
	public int getMovingTextureY() {
		
		return this.movingTextureY;
	}

	public int getFillRenderXOffset() {
		return this.movingTextureX;
	}

	public int getFillRenderYOffset() {
		return this.movingTextureY - this.fullHeight;
	}
	
	public int getFullBarWidth() {
		
		return this.defaultBarWidth;
	}
	
	public ResourceLocation getTexture() {
		
		return this.texture;
	}
	public void setTexture(ResourceLocation texture) {
		this.texture = texture;
	}
	
	// Should this bar even be displayed?
	public boolean shouldBeDisplayed() {
		
		// Minecraft instance. Figure out if f3 debug mode is on.
		Minecraft mc = Minecraft.getMinecraft();
		
		boolean isDebugEnabled = mc.gameSettings.showDebugInfo;
		
		// Don't display most crap if debug mode is enabled.
		if (!isDebugEnabled) {
				return true;
			}
			else if (type == StatType.EVOLUTION) {
				return true;
			}
			else {
				return false;
			}
	}
	
	// Determine the width of the bar. 确定属性条的实际宽度。
	public int getMovingWidth() {
		float clampedMaxValue = Math.max(0f, this.maxValue);
		if (clampedMaxValue <= 0f) {
			this.displayValue = 0f;
			this.lastAnimationTick = System.currentTimeMillis();
			return 0;
		}

		float animatedValue = getAnimatedValue(clampedMaxValue);
		double singleUnit = (double) this.defaultBarWidth / clampedMaxValue;
		return Math.round((float) (singleUnit * animatedValue));
	}
	
	// Determine text to be displayed.
	public String getTextToDisplay() {
		float currentValue = Math.max(0f, this.value);
		if (this.maxValue <= 0f) {
			return formatValue(currentValue);
		}

		return formatValue(currentValue) + " / " + formatValue(this.maxValue);
	}

	private float getAnimatedValue(float clampedMaxValue) {
		float targetValue = Math.min(Math.max(this.value, 0f), clampedMaxValue);
		long now = System.currentTimeMillis();
		float deltaSeconds = (now - this.lastAnimationTick) / 1000.0f;
		this.lastAnimationTick = now;

		float lerpFactor = Math.min(1.0f, Math.max(0f, deltaSeconds) * ANIMATION_SPEED);
		this.displayValue += (targetValue - this.displayValue) * lerpFactor;

		if (Math.abs(targetValue - this.displayValue) < 0.05f) {
			this.displayValue = targetValue;
		}

		return Math.min(Math.max(this.displayValue, 0f), clampedMaxValue);
	}

	private String formatValue(float rawValue) {
		float absoluteValue = Math.abs(rawValue);

		if (absoluteValue >= 1000000f) {
			return String.format(Locale.ROOT, "%.1fM", rawValue / 1000000f);
		}

		if (absoluteValue >= 1000f) {
			return String.format(Locale.ROOT, "%.1fk", rawValue / 1000f);
		}

		if (rawValue == Math.round(rawValue)) {
			return Integer.toString(Math.round(rawValue));
		}

		return String.format(Locale.ROOT, "%.1f", rawValue);
	}
}
