package pl.asie.lib.chat;

import net.blay09.mods.eirairc.api.event.RelayChat;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Optional;

public class ChatHandlerEiraIRC {
	@Optional.Method(modid="eirairc")
	protected static void eiraircRelay(EntityPlayerMP player, String username,
			String message) {
		MinecraftForge.EVENT_BUS.post(new RelayChat(new CommandSenderDummy(player, username), message));
	}
}
