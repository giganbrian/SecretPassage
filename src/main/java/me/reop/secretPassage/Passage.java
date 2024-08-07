package me.reop.secretPassage;

import java.util.Vector;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Passage {
    public Vector<PassBlock> blocks = new Vector<>();

    public Vector<SwitchBlock> switches = new Vector<>();

    public Vector<String> nameList = new Vector<>();

    public String world = "";

    public String owner = "";

    public String name = "";

    public String listStyle = "";

    public String onMessage = "";

    public String offMessage = "";

    public int timer = 0;

    public boolean reverseTimer = false;

    public boolean hasPower;

    public Passage(Player player, String passName) {
        if (player != null) {
            this.world = player.getWorld().getName();
            this.owner = player.getName();
        } else {
            this.world = "";
            this.owner = "";
        }
        if (passName != null) {
            this.name = passName;
        } else {
            this.name = "";
        }
    }

    public void addBlock(Block block) {
        PassBlock newBlock = new PassBlock(block);
        PassBlock testBlock = findBlock(block);
        if (!this.world.equals(block.getWorld().getName()) && this.blocks.size() == 0)
            this.world = block.getWorld().getName();
        if (this.world.equals(block.getWorld().getName()))
            if (testBlock == null) {
                this.blocks.add(newBlock);
            } else if (this.blocks.contains(testBlock)) {
                this.blocks.set(this.blocks.indexOf(testBlock), newBlock);
            }
    }

    public void addBlock(PassBlock pB) {
        if (!this.blocks.contains(pB))
            this.blocks.add(pB);
    }

    public boolean hasBlock(Block block) {
        PassBlock newBlock = new PassBlock(block);
        for (PassBlock passBlock : this.blocks) {
            if (newBlock.matches(passBlock))
                return true;
        }
        return false;
    }

    public PassBlock findBlock(Block block) {
        PassBlock newBlock = new PassBlock(block);
        if (this.blocks.contains(newBlock))
            return newBlock;
        if (block.getWorld().getName().equals(this.world))
            for (PassBlock passBlock : this.blocks) {
                if (passBlock.x == newBlock.x && passBlock.y == newBlock.y && passBlock.z == newBlock.z)
                    return passBlock;
            }
        return null;
    }

    public void addSwitch(Block block) {
        SwitchBlock newBlock = new SwitchBlock(block);
        if (block.getWorld().getName().equals(this.world) &&
                !this.switches.contains(newBlock))
            this.switches.add(newBlock);
    }

    public void addSwitch(SwitchBlock block) {
        if (!this.switches.contains(block))
            this.switches.add(block);
    }

    public boolean hasSwitch(Block block) {
        for (SwitchBlock switchBlock : this.switches) {
            if (switchBlock.matches(block))
                return true;
        }
        return false;
    }

    public boolean removeSwitch(Block block) {
        for (int x = 0; x < this.switches.size(); x++) {
            if (((SwitchBlock)this.switches.get(x)).matches(block)) {
                this.switches.remove(x);
                return true;
            }
        }
        return false;
    }

    public boolean checkPowerChange(World thisWorld) {
        if (thisWorld.getName().equals(this.world)) {
            for (SwitchBlock curBlock : this.switches) {
                Block myBlock = thisWorld.getBlockAt(curBlock.x, curBlock.y, curBlock.z);
                if ((myBlock.isBlockPowered() || myBlock.isBlockIndirectlyPowered()) && !this.hasPower) {
                    this.hasPower = true;
                    return true;
                }
                if ((myBlock.isBlockPowered() || myBlock.isBlockIndirectlyPowered()) && this.hasPower)
                    return false;
            }
            if (this.hasPower) {
                this.hasPower = false;
                return true;
            }
        }
        return false;
    }

    public boolean checkPower(World thisWorld) {
        if (thisWorld.getName().equals(this.world) && !this.switches.isEmpty())
            for (SwitchBlock curBlock : this.switches) {
                Block myBlock = thisWorld.getBlockAt(curBlock.x, curBlock.y, curBlock.z);
                if (myBlock.isBlockPowered() || myBlock.isBlockIndirectlyPowered())
                    return true;
            }
        return false;
    }
}
