package com.arn.goodmod.init;

import com.arn.goodmod.GoodMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.concurrent.ConcurrentHashMap;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, GoodMod.MODID);
    private static final ConcurrentHashMap<String, DeferredHolder<SoundEvent, SoundEvent>> REGISTERED_SOUNDS = new ConcurrentHashMap<>();

    // Common action sounds (grouped in sounds.json)
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_POUNDING = register("misc.pounding");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_BEDRUSTLE = register("misc.bedrustle");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_TOUCH = register("misc.touch");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_SLAP = register("misc.slap");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_CLAP = register("misc.clap");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_INSERTS = register("misc.inserts");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_SMALLINSERTS = register("misc.smallinserts");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_PLOB = register("misc.plob");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_SLIDE = register("misc.slide");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_JUMP = register("misc.jump");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_SCREAM = register("misc.scream");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_EAT = register("misc.eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_FART = register("misc.fart");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_FLAP = register("misc.flap");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_WEOWEO = register("misc.weoweo");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_BEEW = register("misc.beew");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_CUMINFLATION = register("misc.cuminflation");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_BELLJINGLE = register("misc.belljingle");

    // Legacy individual fallbacks
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_BEDRUSTLE_0 = register("misc.bedrustle.bedrustle0");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_BEDRUSTLE_1 = register("misc.bedrustle.bedrustle1");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_SLAP_0 = register("misc.slap.slap0");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_SLAP_1 = register("misc.slap.slap1");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_TOUCH_0 = register("misc.touch.touch0");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_TOUCH_1 = register("misc.touch.touch1");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_POUNDING_0 = register("misc.pounding.pounding0");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISC_INSERTS_0 = register("misc.inserts.inserts0");

    // Girl voice group events
    // Jenny
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_JENNY_MOAN = register("girls.jenny.moan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_JENNY_BJMOAN = register("girls.jenny.bjmoan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_JENNY_AHH = register("girls.jenny.ahh");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_JENNY_GIGGLE = register("girls.jenny.giggle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_JENNY_LIPSOUND = register("girls.jenny.lipsound");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_JENNY_HEAVYBREATHING = register("girls.jenny.heavybreathing");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_JENNY_LIGHTBREATHING = register("girls.jenny.lightbreathing");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_JENNY_AFTERSESSIONMOAN = register("girls.jenny.aftersessionmoan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_JENNY_HAPPYOH = register("girls.jenny.happyoh");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_JENNY_MMM = register("girls.jenny.mmm");

    // Ellie
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ELLIE_MOAN = register("girls.ellie.moan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ELLIE_BJMOAN = register("girls.ellie.bjmoan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ELLIE_AHH = register("girls.ellie.ahh");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ELLIE_GIGGLE = register("girls.ellie.giggle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ELLIE_LIPSOUND = register("girls.ellie.lipsound");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ELLIE_HEAVYBREATHING = register("girls.ellie.heavybreathing");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ELLIE_LIGHTBREATHING = register("girls.ellie.lightbreathing");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ELLIE_AFTERSESSIONMOAN = register("girls.ellie.aftersessionmoan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ELLIE_MOMMYHORNY = register("girls.ellie.mommyhorny");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ELLIE_MMM = register("girls.ellie.mmm");

    // Bia
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_BIA_MOAN = register("girls.bia.moan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_BIA_AHH = register("girls.bia.ahh");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_BIA_BJMOAN = register("girls.bia.bjmoan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_BIA_BREATH = register("girls.bia.breath");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_BIA_GIGGLE = register("girls.bia.giggle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_BIA_HEY = register("girls.bia.hey");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_BIA_HUH = register("girls.bia.huh");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_BIA_MMM = register("girls.bia.mmm");

    // Luna
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_LUNA_MOAN = register("girls.luna.moan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_LUNA_AHH = register("girls.luna.ahh");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_LUNA_CUTENYA = register("girls.luna.cutenya");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_LUNA_SINGING = register("girls.luna.singing");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_LUNA_GIGGLE = register("girls.luna.giggle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_LUNA_HORNINYA = register("girls.luna.horninya");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_LUNA_LIGHTBREATHING = register("girls.luna.lightbreathing");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_LUNA_MMM = register("girls.luna.mmm");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_LUNA_OUU = register("girls.luna.ouu");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_LUNA_OWO = register("girls.luna.owo");

    // Allie
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ALLIE_MOAN = register("girls.allie.moan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ALLIE_BJMOAN = register("girls.allie.bjmoan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ALLIE_AHH = register("girls.allie.ahh");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ALLIE_GIGGLE = register("girls.allie.giggle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ALLIE_LIPSOUND = register("girls.allie.lipsound");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ALLIE_HEAVYBREATHING = register("girls.allie.heavybreathing");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ALLIE_LIGHTBREATHING = register("girls.allie.lightbreathing");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ALLIE_AFTERSESSIONMOAN = register("girls.allie.aftersessionmoan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_ALLIE_MMM = register("girls.allie.mmm");

    // Kobold
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_KOBOLD_MOAN = register("girls.kobold.moan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_KOBOLD_BJMOAN = register("girls.kobold.bjmoan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_KOBOLD_ORGASM = register("girls.kobold.orgasm");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_KOBOLD_GIGGLE = register("girls.kobold.giggle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_KOBOLD_HAA = register("girls.kobold.haa");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_KOBOLD_HEYMASTER = register("girls.kobold.heymaster");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_KOBOLD_INTERESTED = register("girls.kobold.interested");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_KOBOLD_LIGHTBREATHING = register("girls.kobold.lightbreathing");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_KOBOLD_YEP = register("girls.kobold.yep");

    // Galath
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_GALATH_MOAN = register("girls.galath.moan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_GALATH_ORGASM = register("girls.galath.orgasm");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_GALATH_AAA = register("girls.galath.aaa");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_GALATH_AHH = register("girls.galath.ahh");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_GALATH_BREATHING = register("girls.galath.breathing");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_GALATH_DIALOG = register("girls.galath.dialog");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_GALATH_GIGGLE = register("girls.galath.giggle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_GALATH_LIGHTCHARGE = register("girls.galath.lightcharge");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_GALATH_STRONGCHARGE = register("girls.galath.strongcharge");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRLS_GALATH_UUH = register("girls.galath.uuh");

    public static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return REGISTERED_SOUNDS.computeIfAbsent(name, n -> {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, n);
            return SOUND_EVENTS.register(n, () -> SoundEvent.createVariableRangeEvent(id));
        });
    }

    public static SoundEvent getSound(String name) {
        DeferredHolder<SoundEvent, SoundEvent> holder = REGISTERED_SOUNDS.get(name);
        if (holder != null) {
            return holder.value();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, name);
        if (BuiltInRegistries.SOUND_EVENT.containsKey(id)) {
            return BuiltInRegistries.SOUND_EVENT.get(id);
        }
        return SoundEvent.createVariableRangeEvent(id);
    }

    /**
     * Resolves voice audio for a specific girl based on keyframe name.
     */
    public static SoundEvent getGirlVoice(String girlName, String voiceType) {
        String girl = girlName.toLowerCase();
        String type = voiceType.toLowerCase();

        // Luna alias
        if (girl.equals("cat") || girl.equals("luna")) {
            girl = "luna";
        }

        // Normalization
        if (type.contains("bjmoan")) {
            type = "bjmoan";
        } else if (type.contains("moan")) {
            type = "moan";
        } else if (type.contains("giggle") || type.equals("hehe")) {
            type = "giggle";
        } else if (type.contains("breath")) {
            type = "lightbreathing";
        } else if (type.contains("lipsound") || type.contains("lip")) {
            type = "lipsound";
        } else if (type.contains("orgasm") || type.contains("cum")) {
            type = "orgasm";
        } else if (type.contains("singing")) {
            type = "singing";
        } else if (type.contains("cutenya")) {
            type = "cutenya";
        } else if (type.contains("horninya")) {
            type = "horninya";
        } else if (type.contains("happyoh")) {
            type = "happyoh";
        } else if (type.contains("aftermoan") || type.contains("aftersession")) {
            type = "aftersessionmoan";
        }

        // Slime Girl uses slime sounds or Jenny voice
        if (girl.equals("slime") || girl.equals("slimegirl")) {
            if (type.equals("jump") || type.equals("jumpstart") || type.equals("jumpendsound")) {
                return SoundEvents.SLIME_JUMP;
            }
            if (type.equals("svs") || type.equals("attackdone") || type.equals("attacksound")) {
                return SoundEvents.SLIME_SQUISH;
            }
            girl = "jenny";
        }

        // Bee Girl uses bee sounds or Jenny voice
        if (girl.equals("bee")) {
            if (type.equals("attacksound")) {
                return SoundEvents.BEE_STING;
            }
            girl = "jenny";
        }

        // Goblin uses kobold or allie sounds
        if (girl.equals("goblin")) {
            girl = "kobold";
        }

        // Manglelie uses Ellie or Galath sounds
        if (girl.equals("manglelie")) {
            girl = "ellie";
        }

        // Try girl.type group
        String key = "girls." + girl + "." + type;
        if (REGISTERED_SOUNDS.containsKey(key)) {
            return REGISTERED_SOUNDS.get(key).value();
        }

        // Lipsound fallback
        if (type.equals("lipsound")) {
            return GIRLS_JENNY_LIPSOUND.value();
        }

        // Fallback for Bia
        if (girl.equals("bia")) {
            if (type.equals("moan") || type.equals("orgasm") || type.equals("aftersessionmoan") || type.equals("bjmoan")) {
                return GIRLS_BIA_AHH.value();
            }
            if (type.equals("lightbreathing") || type.equals("heavybreathing")) {
                return GIRLS_BIA_BREATH.value();
            }
        }

        // Generic girl fallbacks
        if (type.equals("orgasm") || type.equals("aftersessionmoan") || type.equals("bjmoan")) {
            String moanKey = "girls." + girl + ".moan";
            if (REGISTERED_SOUNDS.containsKey(moanKey)) return REGISTERED_SOUNDS.get(moanKey).value();
        }
        if (type.equals("heavybreathing")) {
            String breathKey = "girls." + girl + ".lightbreathing";
            if (REGISTERED_SOUNDS.containsKey(breathKey)) return REGISTERED_SOUNDS.get(breathKey).value();
        }

        String moanKey = "girls." + girl + ".moan";
        if (REGISTERED_SOUNDS.containsKey(moanKey)) {
            return REGISTERED_SOUNDS.get(moanKey).value();
        }

        return GIRLS_JENNY_MOAN.value();
    }

    /**
     * Resolves action/effect sounds.
     */
    public static SoundEvent getMiscActionSound(String effect) {
        String eff = effect.toLowerCase();

        if (eff.contains("pound") || eff.contains("anal") || eff.contains("mating") || eff.contains("corrupt")) {
            return MISC_POUNDING.value();
        }
        if (eff.contains("touch") || eff.equals("lick")) {
            return MISC_TOUCH.value();
        }
        if (eff.contains("slap") || eff.contains("clap")) {
            return MISC_SLAP.value();
        }
        if (eff.contains("plob")) {
            return MISC_PLOB.value();
        }
        if (eff.contains("bedrustle") || eff.contains("rustle")) {
            return MISC_BEDRUSTLE.value();
        }
        if (eff.contains("slide")) {
            return MISC_SLIDE.value();
        }
        if (eff.contains("jump")) {
            return MISC_JUMP.value();
        }
        if (eff.contains("cum") || eff.contains("creampie") || eff.contains("semen")) {
            return MISC_CUMINFLATION.value();
        }
        if (eff.contains("insert")) {
            return MISC_INSERTS.value();
        }
        if (eff.contains("scream") || eff.contains("rapehurt")) {
            return MISC_SCREAM.value();
        }
        if (eff.contains("eat")) {
            return MISC_EAT.value();
        }
        if (eff.contains("fart")) {
            return MISC_FART.value();
        }
        if (eff.contains("flap")) {
            return MISC_FLAP.value();
        }
        if (eff.contains("belljingle")) {
            return MISC_BELLJINGLE.value();
        }
        if (eff.contains("weoweo") || eff.contains("beew") || eff.contains("energy") || eff.contains("boost")) {
            return MISC_WEOWEO.value();
        }

        return null;
    }
}
