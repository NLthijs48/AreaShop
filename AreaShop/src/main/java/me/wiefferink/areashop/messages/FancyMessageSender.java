package me.wiefferink.areashop.messages;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.*;
import java.util.logging.Level;


/**
 * Methods written by the Fanciful project (Github: https://github.com/mkremins/fanciful)
 */

public class FancyMessageSender {

	private static Constructor<?> nmsPacketPlayOutChatConstructor;

	public static boolean sendJSON(Player player, String jsonString) {
		try {
			Object handle = Reflection.getHandle(player);
			Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
			Reflection.getMethod(connection.getClass(), "sendPacket", Reflection.getNMSClass("Packet")).invoke(connection, createChatPacket(jsonString));
			return true;
		} catch(IllegalArgumentException e) {
			Bukkit.getLogger().log(Level.WARNING, "Argument could not be passed.", e);
		} catch(IllegalAccessException e) {
			Bukkit.getLogger().log(Level.WARNING, "Could not access method.", e);
		} catch(InstantiationException e) {
			Bukkit.getLogger().log(Level.WARNING, "Underlying class is abstract.", e);
		} catch(InvocationTargetException e) {
			Bukkit.getLogger().log(Level.WARNING, "A error has occured durring invoking of method.", e);
		} catch(NoSuchMethodException e) {
			Bukkit.getLogger().log(Level.WARNING, "Could not find method.", e);
		} catch(ClassNotFoundException e) {
			Bukkit.getLogger().log(Level.WARNING, "Could not find class.", e);
		}
		return false;
	}

	// The ChatSerializer's instance of Gson
	private static Object nmsChatSerializerGsonInstance;
	private static Method fromJsonMethod;

	private static Object createChatPacket(String json) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		if(nmsChatSerializerGsonInstance == null) {
			// Find the field and its value, completely bypassing obfuscation
			Class<?> chatSerializerClazz;

			String version = Reflection.getVersion();
			double majorVersion = Double.parseDouble(version.replace('_', '.').substring(1, 4));
			int lesserVersion = Integer.parseInt(version.substring(6, 7));

			if(majorVersion < 1.8 || (majorVersion == 1.8 && lesserVersion == 1)) {
				chatSerializerClazz = Reflection.getNMSClass("ChatSerializer");
			} else {
				chatSerializerClazz = Reflection.getNMSClass("IChatBaseComponent$ChatSerializer");
			}

			if(chatSerializerClazz == null) {
				throw new ClassNotFoundException("Can't find the ChatSerializer class");
			}

			for(Field declaredField : chatSerializerClazz.getDeclaredFields()) {
				if(Modifier.isFinal(declaredField.getModifiers()) && Modifier.isStatic(declaredField.getModifiers()) && declaredField.getType().getName().endsWith("Gson")) {
					// We've found our field
					declaredField.setAccessible(true);
					nmsChatSerializerGsonInstance = declaredField.get(null);
					fromJsonMethod = nmsChatSerializerGsonInstance.getClass().getMethod("fromJson", String.class, Class.class);
					break;
				}
			}
		}

		// Since the method is so simple, and all the obfuscated methods have the same name, it's easier to reimplement 'IChatBaseComponent a(String)' than to reflectively call it
		// Of course, the implementation may change, but fuzzy matches might break with signature changes
		Object serializedChatComponent = fromJsonMethod.invoke(nmsChatSerializerGsonInstance, json, Reflection.getNMSClass("IChatBaseComponent"));
		if(nmsPacketPlayOutChatConstructor == null) {
			try {
				nmsPacketPlayOutChatConstructor = Reflection.getNMSClass("PacketPlayOutChat").getDeclaredConstructor(Reflection.getNMSClass("IChatBaseComponent"));
				nmsPacketPlayOutChatConstructor.setAccessible(true);
			} catch(NoSuchMethodException e) {
				Bukkit.getLogger().log(Level.SEVERE, "Could not find Minecraft method or constructor.", e);
			} catch(SecurityException e) {
				Bukkit.getLogger().log(Level.WARNING, "Could not access constructor.", e);
			}
		}
		return nmsPacketPlayOutChatConstructor.newInstance(serializedChatComponent);
	}


}
