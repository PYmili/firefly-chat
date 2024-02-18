package icu.pymili_blog.sounds;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.Registries;

public class SoundPlayer {
    public static void playSound(SoundEvent soundEvent) {
        try {
            if (soundEvent != null) {
                // 监听资源注册事件，当资源加载完成时触发播放声音
                RegistryEntryAddedCallback.event(Registries.SOUND_EVENT).register((rawId, id, sound) -> {
                    if (id.equals(soundEvent.getId())) {
                        PlayerEntity player = MinecraftClient.getInstance().player;
                        if (player != null) {
                            System.out.println("开始播放音频：" + soundEvent.getId());
                            player.playSound(soundEvent, 1.0f, 1.0f);
                        }
                    }
                });
            } else {
                System.err.println("播放音频时，加载失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
