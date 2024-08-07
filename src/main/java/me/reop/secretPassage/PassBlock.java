package me.reop.secretPassage;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class PassBlock {
    public int x;

    public int y;

    public int z;

    public Material type;

    public BlockData data;

    public PassBlock(Block block) {
        if (block != null) {
            this.x = block.getX();
            this.y = block.getY();
            this.z = block.getZ();
            this.type = block.getType();
            this.data = block.getBlockData();
        } else {
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.type = Material.AIR;
            this.data = null;
        }
    }

    public boolean matches(PassBlock pB) {
        return (this.x == pB.x && this.y == pB.y && this.z == pB.z && this.type == pB.type && this.data.matches(pB.data));
    }
}
