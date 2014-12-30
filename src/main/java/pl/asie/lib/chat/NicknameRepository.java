package pl.asie.lib.chat;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import pl.asie.lib.api.chat.INicknameHandler;
import pl.asie.lib.api.chat.INicknameRepository;
import pl.asie.lib.util.PlayerUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashSet;

public class NicknameRepository implements INicknameRepository {
	protected BiMap<String, String> nicknames;
	private File nicknameFile;
	private Gson gson;
	private Type gsonType;
	private HashSet<INicknameHandler> handlers = new HashSet<INicknameHandler>();
	
	public NicknameRepository() {
		gson = new Gson();
		gsonType = new TypeToken<HashBiMap<String, String>>(){}.getType();
		nicknames = HashBiMap.create();
	}
	
	public void setNickname(String username, String nickname) {
		if(nickname.equals("-") && nicknames.containsKey(username)) {
			nicknames.remove(username);
		} else {
			nicknames.put(username, nickname);
		}
		
		updateNickname(username);
		saveNicknames();
	}
	
	public void updateNickname(String username) {
		EntityPlayer player = PlayerUtils.find(username);
		if(player != null) {
			player.refreshDisplayName();
		}
		if(FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) return;
		String nickname = getNickname(username);
		for(INicknameHandler handler: handlers) {
			handler.onNicknameUpdate(username, nickname);
		}
	}
	
	public String getUsername(String nickname) {
		BiMap<String, String> usernames = nicknames.inverse();
		return usernames.containsKey(nickname) ? usernames.get(nickname) : null;
	}
	
	public String getNickname(String username) {
		return nicknames.containsKey(username) ? nicknames.get(username) : username;
	}
	
	public String getRawNickname(String username) {
		String nn = getNickname(username);
		return nn.replaceAll("&.", "");
	}
	
	public void loadNicknames(String data) {
		if(FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) return;
		try {
			nicknames = gson.fromJson(data, gsonType);
			for(String username: nicknames.keySet()) {
				updateNickname(username);
			}
		} catch(Exception e) {
			e.printStackTrace();
			nicknames = HashBiMap.create();
		}
	}
	
	public void loadNicknames() {
		if(FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) return;
		nicknameFile = new File(DimensionManager.getCurrentSaveRootDirectory(), "nicknames.json");
		nicknames = HashBiMap.create();
		if(!nicknameFile.exists()) {
			return;
		}
		try {
			LinkedTreeMap<String, String> hm = gson.fromJson(new FileReader(nicknameFile), gsonType);
			for(String a: hm.keySet()) {
				nicknames.put(a, hm.get(a));
			}
			for(String username: nicknames.keySet()) {
				updateNickname(username);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveNicknames() {
		if(FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) return;
		try {
			FileWriter fw = new FileWriter(nicknameFile);
			fw.write(toString());
			fw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return gson.toJson(nicknames, gsonType);
	}
	
	@SubscribeEvent
	public void nameFormat(PlayerEvent.NameFormat event) {
		event.displayname = getRawNickname(event.username);
	}

	public void addHandler(INicknameHandler handler) {
		handlers.add(handler);
	}
}
