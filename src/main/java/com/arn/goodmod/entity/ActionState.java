package com.arn.goodmod.entity;

public enum ActionState {
    NULL(0, false, true),
    STARTBLOWJOB(2, true, false),
    SUCKBLOWJOB(2, true, false),
    SUCKBLOWJOB_BLINK(2, true, true),
    CUMBLOWJOB(0, true, false),
    THRUSTBLOWJOB(2, true, false),
    PAYMENT(5, true, false),
    STARTDOGGY(2, true, false),
    WAITDOGGY(0, true, true),
    DOGGYSTART(0, true, false),
    DOGGYSLOW(2, true, false),
    DOGGYFAST(2, true, false),
    DOGGYCUM(2, true, false),
    STRIP(5, false, false),
    DASH(2, false, false),
    HUG(2, true, false),
    HUGIDLE(0, true, true),
    HUGSELECTED(0, true, false),
    UNDRESS(2, false, true),
    DRESS(2, false, true),
    SITDOWN(2, false, false, 60.0F, -90.0F, true),
    SITDOWNIDLE(0, false, true, 60.0F, -60.0F, true),
    COWGIRLSTART(0, true, false, 60.0F, -60.0F, false),
    COWGIRLSLOW(10, true, false, 60.0F, -60.0F, false),
    COWGIRLFAST(10, true, false, 60.0F, -60.0F, false),
    COWGIRLCUM(2, true, false, 60.0F, -60.0F, false),
    ATTACK(0, false, true),
    BOW(2, false, true),
    RIDE(0, false, true),
    SIT(0, false, true),
    THROW_PEARL(0, false, false),
    DOWNED(7, false, true),
    PAIZURI_START(0, true, false, 70.0F, -25.0F, false, true),
    PAIZURI_IDLE(0, true, false, 70.0F, -25.0F, false, true),
    PAIZURI_SLOW(0, true, true, 70.0F, -25.0F, false, true),
    PAIZURI_FAST(0, true, false, 70.0F, -25.0F, false, true),
    PAIZURI_FAST_CONTINUES(0, true, false, 70.0F, -25.0F, false, true),
    PAIZURI_CUM(0, true, false, 70.0F, -25.0F, false, true),
    MISSIONARY_START(0, true, false, 85.0F, -20.0F, true),
    MISSIONARY_SLOW(2, true, false, 85.0F, -20.0F, true),
    MISSIONARY_FAST(2, true, false, 85.0F, -20.0F, true),
    MISSIONARY_CUM(2, true, false, 85.0F, -20.0F, true),
    TALK_HORNY(5, true, false),
    TALK_IDLE(0, true, true),
    TALK_RESPONSE(2, true, false),
    ANAL_PREPARE(5, true, false),
    ANAL_WAIT(0, true, true),
    ANAL_START(0, true, false),
    ANAL_SLOW(2, true, true),
    ANAL_FAST(0, true, false),
    ANAL_CUM(2, true, false),
    KOBOLD_ANAL_START(0, true, false, false, 4.0F, -80.0F, true),
    KOBOLD_ANAL_SLOW(0, true, true, false, 4.0F, -80.0F, true),
    KOBOLD_ANAL_FAST(0, true, false, false, 4.0F, -80.0F, true),
    KOBOLD_ANAL_CUM(2, true, false, false, 4.0F, -80.0F, true),
    SUMMON(0, false, false, false, true),
    SUMMON_WAIT(0, false, true, false, true),
    HEAD_PAT(0, true, false),
    ALLIE_PREPARE_FIRST_TIME(0, true, false, 40.0F, -40.0F, false),
    DEEPTHROAT_START(0, true, false, true, 40.0F, -40.0F, false),
    DEEPTHROAT_SLOW(2, true, false, true, 40.0F, -40.0F, false),
    DEEPTHROAT_FAST(2, true, false, true, 40.0F, -40.0F, false),
    DEEPTHROAT_CUM(2, true, false, true, 40.0F, -40.0F, false),
    ALLIE_PREPARE_NORMAL(2, true, false, 40.0F, -40.0F, false),
    SUMMON_NORMAL(0, false, false),
    SUMMON_SAND(0, false, false),
    SUMMON_NORMAL_WAIT(2, false, true),
    RICH_FIRST_TIME(0, false, false, 45.0F, -35.0F, false),
    RICH_NORMAL(0, false, false, 45.0F, -35.0F, false),
    CITIZEN_START(0, true, false, 10.0F, -90.0F, false),
    CITIZEN_SLOW(0, true, false, 10.0F, -90.0F, false),
    CITIZEN_FAST(0, true, false, 10.0F, -90.0F, false),
    CITIZEN_CUM(2, true, false, 10.0F, -90.0F, false),
    FISHING_START(5, false, false),
    FISHING_IDLE(0, false, true),
    FISHING_EAT(0, false, false),
    FISHING_THROW_AWAY(0, false, false),
    TOUCH_BOOBS_INTRO(0, true, false),
    TOUCH_BOOBS_SLOW(2, true, false),
    TOUCH_BOOBS_FAST(2, true, false),
    TOUCH_BOOBS_CUM(2, true, false),
    WAIT_CAT(0, true, false, 30.0F, -90.0F, true),
    COWGIRL_SITTING_INTRO(0, true, false),
    COWGIRL_SITTING_SLOW(5, true, false),
    COWGIRL_SITTING_FAST(5, true, false),
    COWGIRL_SITTING_CUM(5, true, false),
    MINE(0, false, false),
    SLEEP(5, false, false),
    MATING_PRESS_START(0, true, false, false, -20.0F, 85.0F, true),
    MATING_PRESS_SOFT(0, true, false, 85.0F, -20.0F, true),
    MATING_PRESS_HARD(0, true, false, 85.0F, -20.0F, true),
    MATING_PRESS_CUM(2, true, false, 85.0F, -20.0F, true),
    SHOULDER_IDLE(0, false, true, false, true),
    PICK_UP(0, true, false, 10.0F, -90.0F, true, true),
    RUN(5, false, true),
    CATCH(0, true, false),
    CATCH_BJ(0, true, false),
    CATCH_BJ_IDLE(0, true, false),
    START_THROWING(0, true, true),
    THROWN(0, false, true),
    JUMP_0(0, true, false),
    JUMP_1(0, false, false),
    JUMP_2(0, false, false),
    BREEDING_INTRO_0(0, true, false),
    BREEDING_INTRO_1(0, true, false),
    BREEDING_INTRO_2(0, true, false),
    BREEDING_SLOW_0(0, true, false),
    BREEDING_1(0, true, false),
    BREEDING_SLOW_2(5, true, false),
    BREEDING_FAST_0(0, true, false),
    BREEDING_FAST_2(5, true, false),
    BREEDING_CUM_0(0, true, false),
    BREEDING_CUM_1(0, true, false),
    BREEDING_CUM_2(0, true, false),
    AWAIT_PICK_UP(0, false, true),
    VANISH(0, false, true),
    STAND_UP(0, false, false),
    NELSON_INTRO(0, true, false, 70.0F, -25.0F, true, true),
    NELSON_SLOW(0, true, false, 70.0F, -25.0F, true, true),
    NELSON_FAST(0, true, false, 70.0F, -25.0F, true, true),
    NELSON_CUM(0, true, false, 70.0F, -25.0F, true, true),
    CARRY_SLOW(0, true, false, true, true),
    CARRY_FAST(0, true, false, true, true),
    CARRY_CUM(0, true, false, true, true),
    CARRY_INTRO(0, true, false, true, true, 191, "CARRY_SLOW"),
    PRONE_DOGGY_INTRO(0, true, false, true, true),
    PRONE_DOGGY_SOFT(0, true, false, true, true),
    PRONE_DOGGY_HARD(0, true, false, true, true, 34, "PRONE_DOGGY_SOFT"),
    PRONE_DOGGY_INSERT(2, true, false, true, true, 42, "PRONE_DOGGY_SOFT"),
    PRONE_DOGGY_CUM(0, true, false, true, true),
    REVERSE_COWGIRL_SLOW(0, true, false, true, 30.0F, -90.0F, true),
    REVERSE_COWGIRL_FAST_START(0, true, false, true, 34, "REVERSE_COWGIRL_SLOW", 30.0F, -90.0F, true),
    REVERSE_COWGIRL_FAST_CONTINUES(0, true, false, true, 39, "REVERSE_COWGIRL_SLOW", 30.0F, -90.0F, true),
    REVERSE_COWGIRL_CUM(0, true, false, true, 30.0F, -90.0F, true),
    REVERSE_COWGIRL_START(0, true, false, true, 88, "REVERSE_COWGIRL_SLOW", 30.0F, -90.0F, true),
    WAVE_IDLE(0, false, false, false, true),
    WAVE(0, false, false, true, false, 71, "WAVE_IDLE"),
    FLY(0, false, true),
    SUMMON_SKELETON(0, false, false),
    ATTACK_SWORD(0, false, false),
    KNOCK_OUT_FLY(5, false, false),
    KNOCK_OUT_GROUND(3, false, false),
    KNOCK_OUT_STAND_UP(0, false, false),
    RAPE_PREPARE(0, true, false),
    RAPE_CHARGE(0, true, false),
    RAPE_ON_GOING(0, true, false, true, -20.0F, 85.0F, false),
    RAPE_INTRO(0, true, false, false, true, 46, "RAPE_ON_GOING", -20.0F, 85.0F, false),
    RAPE_CUM_IDLE(0, true, false, true),
    RAPE_CUM(0, true, false, true, 34, "RAPE_CUM_IDLE", -20.0F, 85.0F, false),
    CORRUPT_SLOW(0, true, false, 85.0F, -20.0F, false),
    CORRUPT_FAST(0, true, false, 85.0F, -20.0F, false),
    CORRUPT_CUM(0, true, false, false, -20.0F, 85.0F, false),
    CORRUPT_INTRO(0, true, false, true, 29, "CORRUPT_SLOW", -20.0F, 85.0F, false),
    CONTROLLED_FLIGHT(0, true, true, true, true),
    BOOST(3, true, false, true, true, 43, "CONTROLLED_FLIGHT"),
    GALATH_SUMMON(0, false, false, false, true, 15, "NULL"),
    GALATH_DE_SUMMON(0, false, false, false, true),
    GIVE_COIN(0, true, false, true, true, 140, "NULL"),
    MASTERBATE(0, false, false),
    HUG_MANG(0, true, false, 239, "NULL"),
    RIDE_MOMMY_HEAD(0, false, true),
    THREESOME_SLOW(0, true, false, false, true),
    THREESOME_FAST(0, true, false, false, true),
    THREESOME_CUM(0, true, false, false, true),
    PUSSY_LICKING(0, false, true, false),
    MASTERBATE_SITTING(0, false, true, false),
    MASTERBATE_SITTING_CUM(0, false, false, false),
    MORNING_BLOWJOB_SLOW(0, true, true, true),
    MORNING_BLOWJOB_FAST(0, true, true, true),
    MORNING_BLOWJOB_CUM(0, true, false, true);

    public final int transitionTick;
    public final boolean hasPlayer;
    public final boolean autoBlink;
    public final float maxGirlPitch;
    public final float minGirlPitch;
    public final boolean flipGirlYaw;
    public int length;
    public String followUpName = null;
    public boolean useBoyCam;
    public boolean hideNameTag;

    ActionState(int transitionTick, boolean hasPlayer, boolean autoBlink) {
        this.transitionTick = transitionTick;
        this.hasPlayer = hasPlayer;
        this.autoBlink = autoBlink;
        this.maxGirlPitch = 30.0F;
        this.minGirlPitch = -90.0F;
        this.flipGirlYaw = false;
        this.useBoyCam = false;
        this.hideNameTag = false;
    }

    ActionState(int transitionTick, boolean hasPlayer, boolean autoBlink, boolean useBoyCam) {
        this(transitionTick, hasPlayer, autoBlink);
        this.useBoyCam = useBoyCam;
    }

    ActionState(int transitionTick, boolean hasPlayer, boolean autoBlink, boolean useBoyCam, boolean hideNameTag) {
        this(transitionTick, hasPlayer, autoBlink);
        this.useBoyCam = useBoyCam;
        this.hideNameTag = hideNameTag;
    }

    ActionState(int transitionTick, boolean hasPlayer, boolean autoBlink, float maxGirlPitch, float minGirlPitch, boolean flipGirlYaw) {
        this.transitionTick = transitionTick;
        this.hasPlayer = hasPlayer;
        this.autoBlink = autoBlink;
        this.maxGirlPitch = maxGirlPitch;
        this.minGirlPitch = minGirlPitch;
        this.flipGirlYaw = flipGirlYaw;
        this.useBoyCam = false;
        this.hideNameTag = false;
    }

    ActionState(int transitionTick, boolean hasPlayer, boolean autoBlink, float maxGirlPitch, float minGirlPitch, boolean flipGirlYaw, boolean hideNameTag) {
        this.transitionTick = transitionTick;
        this.hasPlayer = hasPlayer;
        this.autoBlink = autoBlink;
        this.maxGirlPitch = maxGirlPitch;
        this.minGirlPitch = minGirlPitch;
        this.flipGirlYaw = flipGirlYaw;
        this.useBoyCam = false;
        this.hideNameTag = hideNameTag;
    }

    ActionState(int transitionTick, boolean hasPlayer, boolean autoBlink, boolean useBoyCam, float minGirlPitch, float maxGirlPitch, boolean flipGirlYaw) {
        this.transitionTick = transitionTick;
        this.hasPlayer = hasPlayer;
        this.autoBlink = autoBlink;
        this.maxGirlPitch = maxGirlPitch;
        this.minGirlPitch = minGirlPitch;
        this.flipGirlYaw = flipGirlYaw;
        this.hideNameTag = false;
        this.useBoyCam = useBoyCam;
    }

    ActionState(int transitionTick, boolean hasPlayer, boolean autoBlink, int length, String followUpName) {
        this(transitionTick, hasPlayer, autoBlink);
        this.length = length;
        this.followUpName = followUpName;
    }

    ActionState(int transitionTick, boolean hasPlayer, boolean autoBlink, boolean useBoyCam, int length, String followUpName) {
        this(transitionTick, hasPlayer, autoBlink, useBoyCam);
        this.length = length;
        this.followUpName = followUpName;
    }

    ActionState(int transitionTick, boolean hasPlayer, boolean autoBlink, boolean useBoyCam, boolean hideNameTag, int length, String followUpName) {
        this(transitionTick, hasPlayer, autoBlink);
        this.length = length;
        this.followUpName = followUpName;
        this.useBoyCam = useBoyCam;
        this.hideNameTag = hideNameTag;
    }

    ActionState(int transitionTick, boolean hasPlayer, boolean autoBlink, boolean useBoyCam, int length, String followUpName, float minGirlPitch, float maxGirlPitch, boolean flipGirlYaw) {
        this.transitionTick = transitionTick;
        this.hasPlayer = hasPlayer;
        this.autoBlink = autoBlink;
        this.length = length;
        this.followUpName = followUpName;
        this.useBoyCam = useBoyCam;
        this.minGirlPitch = minGirlPitch;
        this.maxGirlPitch = maxGirlPitch;
        this.flipGirlYaw = flipGirlYaw;
    }

    ActionState(int transitionTick, boolean hasPlayer, boolean autoBlink, boolean useBoyCam, boolean hideNameTag, int length, String followUpName, float minGirlPitch, float maxGirlPitch, boolean flipGirlYaw) {
        this.transitionTick = transitionTick;
        this.hasPlayer = hasPlayer;
        this.autoBlink = autoBlink;
        this.length = length;
        this.followUpName = followUpName;
        this.useBoyCam = useBoyCam;
        this.hideNameTag = hideNameTag;
        this.minGirlPitch = minGirlPitch;
        this.maxGirlPitch = maxGirlPitch;
        this.flipGirlYaw = flipGirlYaw;
    }

    public ActionState getFollowUp() {
        if (followUpName == null) return null;
        try {
            return ActionState.valueOf(followUpName);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAdultScene() {
        return this != NULL && this != ATTACK && this != BOW && this != RIDE && this != SIT
                && this != DOWNED && this != MINE && this != SLEEP && this != FLY && this != WAVE && this != WAVE_IDLE
                && this != STRIP && this != DASH && this != UNDRESS && this != DRESS && this != SITDOWN && this != SITDOWNIDLE;
    }

    public boolean facesGirlDirection() {
        return this.flipGirlYaw || shouldFaceGirlDirection(name());
    }

    public static boolean shouldFaceGirlDirection(String name) {
        if (name == null) return false;
        return name.contains("DOGGY") || name.contains("ANAL")
                || name.contains("MATING_PRESS") || name.contains("NELSON")
                || name.contains("MISSIONARY") || name.contains("REVERSE_COWGIRL");
    }
}
