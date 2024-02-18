package icu.pymili_blog.sounds;

import icu.pymili_blog.FireflyChat;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;

public class ModSoundEvents {
    public static final SoundEvent FIREFLY_SOUND = registerSoundEvents("sounds/firefly_sound.wav");
    public static SoundEvent registerSoundEvents(String name) {
        Identifier identifier = new Identifier(FireflyChat.MOD_ID, name);
        // 注册声音事件到注册表
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }
    public static SoundEvent getFireflySound() {
        return FIREFLY_SOUND;
    }
}
