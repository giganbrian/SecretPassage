package me.reop.secretPassage;

import org.bukkit.block.Block;

public class SwitchBlock {
    public int x;

    public int y;

    public int z;

    public SwitchBlock(Block block) {
        if (block != null) {
            this.x = block.getX();
            this.y = block.getY();
            this.z = block.getZ();
        } else {
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }
    }

    public boolean matches(Block block) {
        return (block.getX() == this.x && block.getY() == this.y && block.getZ() == this.z);
    }
}
