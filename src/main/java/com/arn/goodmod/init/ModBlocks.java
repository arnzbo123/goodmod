package com.arn.goodmod.init;

import com.arn.goodmod.GoodMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GoodMod.MODID);

    public static final DeferredBlock<Block> FIRE = BLOCKS.registerSimpleBlock("fire", BlockBehaviour.Properties.ofFullCopy(Blocks.FIRE).noCollission().instabreak());
}
