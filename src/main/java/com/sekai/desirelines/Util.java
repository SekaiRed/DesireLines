package com.sekai.desirelines;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.util.ResourceLocation;

public class Util {
    static Config.BlockDecayEntry createBlockDecayEntry(String[] split) {
        try {
            BlockStateParser sourceParser = (new BlockStateParser(new StringReader(split[0]), true)).parse(false);
            try {
                BlockStateParser targetParser = (new BlockStateParser(new StringReader(split[1]), true)).parse(false);

                return new Config.BlockDecayEntry(sourceParser, targetParser, Double.parseDouble(split[2]));
            } catch (CommandSyntaxException e) {
                DesireLines.LOGGER.error("Failed to parse blockstate target for decay entry \"" + split[1] + "\"!", e);
                return null;
            }
        } catch (CommandSyntaxException e) {
            DesireLines.LOGGER.error("Failed to parse blockstate source for decay entry \"" + split[0] + "\"!", e);
            return null;
        }
    }

    static boolean verifyEntryIntegrity(String[] split) {
        //Not the right amount of arguments
        //if(split.length < 3 || split.length > 4)
        if(split.length != 3)
            return false;

        try
        {
            Double.parseDouble(split[2].trim());
        }
        catch(NumberFormatException e)
        {
            //Not a double
            return false;
        }

        return isValidBlockState(split[0].trim()) || isValidBlockState(split[1].trim());

        /*ResourceLocation source = new ResourceLocation(split[0].trim());
        ResourceLocation result = new ResourceLocation(split[1].trim());

        return ForgeRegistries.BLOCKS.containsKey(source) && ForgeRegistries.BLOCKS.containsKey(result);*/
    }

    private static boolean isValidBlockState(String s) {
        try {
            new BlockStateArgument().parse(new StringReader(s)).getState();
            return true;
        } catch (final CommandSyntaxException e) {
            DesireLines.LOGGER.error("Failed to parse blockstate \"" + s + "\"!", e);
            return false;
        }
    }
}
