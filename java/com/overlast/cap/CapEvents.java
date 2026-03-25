package com.overlast.cap;

import com.dhanantry.scapeandrunparasites.init.SRPPotions;
import com.dhanantry.scapeandrunparasites.util.config.SRPConfigSystems;
import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import com.overlast.lib.ModMobEffects;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import com.overlast.config.OverConfig;
import com.overlast.packet.HUDRenderPacket;
import com.overlast.packet.OverPackets;


/*
 * This is the event handler regarding capabilities and changes to individual stats.
 * Most of the actual code is stored in the modifier classes of each stat, and fired here.
 */
@Mod.EventBusSubscriber
public class CapEvents {

	// Modifiers
	private static int evopoint;

	private int evoTimer = 0;
	
	// When a player logs on, give them their stats stored on the server.
	@SubscribeEvent
	public void onPlayerLogsIn(PlayerLoggedInEvent event) {

		EntityPlayer player = event.player;
		int dimension = player.getEntityWorld().provider.getDimension();
		SRPSaveData saveData = SRPSaveData.get(player.getEntityWorld());
		int phase = saveData.getEvolutionPhase(dimension);
		int totalKills = saveData.getTotalKills(dimension);

		if (player instanceof EntityPlayerMP) {

			// Capabilities
			// Send data to client for rendering.
			IMessage msgGui = new HUDRenderPacket.HUDRenderMessage(phase, totalKills, false);
			OverPackets.net.sendTo(msgGui, (EntityPlayerMP) player);

		}
		switch (phase) {
			case 3:
				evopoint = (SRPConfigSystems.phaseKillsFour-SRPConfigSystems.phaseKillsThree)/4000;
				break;
			case 4:
				evopoint = (SRPConfigSystems.phaseKillsFive-SRPConfigSystems.phaseKillsFour)/4000;
				break;
			case 5:
				evopoint = (SRPConfigSystems.phaseKillsSix-SRPConfigSystems.phaseKillsFive)/4000;
				break;
			case 6:
				evopoint = (SRPConfigSystems.phaseKillsSeven-SRPConfigSystems.phaseKillsSix)/4000;
				break;
			case 7:
				evopoint = (SRPConfigSystems.phaseKillsEight-SRPConfigSystems.phaseKillsSeven)/4000;
				break;
			case 1:
			case 2:
			case 0:
			case -1:
			case -2:
			case 8:
			default:
				evopoint = 0;
		}
		player.sendMessage(new TextComponentTranslation("message.evopoint.login",phase,(int)(evopoint*OverConfig.MECHANICS.naturalEvolutionScale)));
		if (phase == 8) {
			player.sendMessage(new TextComponentTranslation("message.evopoint.eight"));
		}
	}
	
	// When an entity is updated. So, all the time.
	// This also deals with packets to the client.
	@SubscribeEvent
	public void onPlayerUpdate(LivingUpdateEvent event) {
		// Only continue if it's a player.
		if (event.getEntity() instanceof EntityPlayer) {
			
			// Instance of player.
			EntityPlayer player = (EntityPlayer) event.getEntity();

			// Server-side
            if (!player.world.isRemote) {
				int dimension = player.getEntityWorld().provider.getDimension();
				SRPSaveData saveData = SRPSaveData.get(player.getEntityWorld());
                IMessage msgGui = new HUDRenderPacket.HUDRenderMessage(saveData.getEvolutionPhase(dimension), saveData.getTotalKills(dimension), true);
                OverPackets.net.sendTo(msgGui, (EntityPlayerMP) player);

				if(evoTimer<1200) {
					evoTimer++;
				}else {
					saveData.setTotalKills(dimension, (int) (evopoint*OverConfig.MECHANICS.naturalEvolutionScale), true, player.getEntityWorld(), true);
					evoTimer=0;
				}
				
				if(!(player.getActivePotionEffect(ModMobEffects.PARASITESINFECT)==null)&&player.getActivePotionEffect(ModMobEffects.PARASITESINFECT).getAmplifier() ==0) {
					if((player.getActivePotionEffect(ModMobEffects.PARASITESPURIFY)==null)) {
						player.addPotionEffect(new PotionEffect(SRPPotions.COTH_E, 1200, 1, false, false));
						evoTimer+=20;
					}
					player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 1200, 2, false, false));
					player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 1200, 2, false, false));
					player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 1200, 1, false, false));
					player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 1200, 0, false, false));
				}else if (!(player.getActivePotionEffect(ModMobEffects.PARASITESINFECT)==null)&&player.getActivePotionEffect(ModMobEffects.PARASITESINFECT).getAmplifier() ==1) {
					if((player.getActivePotionEffect(ModMobEffects.PARASITESPURIFY)==null)) {
						player.addPotionEffect(new PotionEffect(SRPPotions.COTH_E, 1200, 3, false, false));
						evoTimer+=40;
					}
					player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 1200, 3, false, false));
					player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 1200, 2, false, false));
					player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 1200, 2, false, false));
					player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 1200, 0, false, false));
				}
			}
		}
	}
	



}
