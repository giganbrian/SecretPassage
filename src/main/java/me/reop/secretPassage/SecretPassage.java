package me.reop.secretPassage;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SecretPassage extends JavaPlugin {
    public String prefix = "";

    public String tPerm = "secretpassage.toggle";

    public String cOPerm = "secretpassage.create.other";

    public String dOPerm = "secretpassage.destroy.other";

    public String lOPerm = "secretpassage.list.other";

    public String rPerm = "secretpassage.reset";

    public String xPerm = "secretpassage.lockout";

    public String aXPerm = "secretpassage.antilockout";

    public String uCPerm = "secretpassage.useCommands";

    public Config conf = new Config();

    public HashMap<String, String> builders = new HashMap<>();

    public Vector<String> switchDestroyer = new Vector<>();

    private SPPlayerListener pListen = null;

    private SPBlockListener bListen = null;

    private SPRedstoneListener rListen = null;

    PassageManager passMan = new PassageManager();

    public Material markMat = Material.NETHERRACK;

    Logger log = Logger.getLogger("Minecraft");

    public void onEnable() {
        this.conf.loadConfig();
        this.passMan.setup();
        this.pListen = new SPPlayerListener(this);
        this.bListen = new SPBlockListener(this);
        if (this.conf.useRedstone) {
            this.rListen = new SPRedstoneListener(this);
            if (this.conf.redstoneAtStart)
                setRedstoneStates();
        }
        this.log.info("SecretPassage v1.7 enabled.");
    }

    public void onDisable() {
        this.log.info("SecretPassage disabled.");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.prefix + "This command can only be used by players.");
            return true;
        }
        Player player = (Player)sender;
        if (player.hasPermission(this.xPerm) && !player.hasPermission(this.aXPerm)) {
            player.sendMessage(this.prefix + "You are not allowed to use this plugin.");
        } else if (CommandLabel.equalsIgnoreCase("secretpassage") || CommandLabel.equalsIgnoreCase("sp") || CommandLabel.equalsIgnoreCase("spass")) {
            if (this.conf.needCommandPermission && !player.hasPermission(this.uCPerm)) {
                player.sendMessage(this.prefix + "You don't have permission to use commands.");
            } else if (args.length > 0) {
                if (args[0].equalsIgnoreCase("help")) {
                    helpMessage(player);
                    return true;
                }
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase(this.conf.listCommand)) {
                        listPassages(player);
                        return true;
                    }
                    if (args[0].equalsIgnoreCase(this.conf.createCommand) || args[0]
                            .equalsIgnoreCase(this.conf.destroyCommand) || (args[0]
                            .equalsIgnoreCase(this.conf.toggleCommand) && player.hasPermission(this.tPerm))) {
                        player.sendMessage(this.prefix + "You must specify a passage.");
                        return true;
                    }
                    if (args[0].equalsIgnoreCase(this.conf.resetCommand) && player
                            .hasPermission(this.rPerm) && this.conf.useRedstone) {
                        setRedstoneStates();
                        player.sendMessage(this.prefix + "Passages reset based on redstone.");
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("timer")) {
                        if (this.builders.containsKey(player.getName())) {
                            player.sendMessage(this.prefix + "Please enter a valid time in ticks.");
                        } else {
                            player.sendMessage(this.prefix + "You must be working on a passage to set its timer");
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("info")) {
                        if (this.builders.containsKey(player.getName())) {
                            displayPassageInfo(player, this.builders.get(player.getName()));
                        } else {
                            player.sendMessage(this.prefix + "Please specify a passage to get info on, or start working on a passage.");
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase(this.conf.accessCommand)) {
                        player.sendMessage(this.prefix + "Access command help: ");
                        player.sendMessage("/spass " + this.conf.accessCommand + " allow [Name1] (Name2): Adds user to passage whitelist.");
                        player.sendMessage("/spass " + this.conf.accessCommand + " deny [Name1] (Name2): Adds user to passage blacklist.");
                        player.sendMessage("Above commands also delete any list of the other type.");
                        player.sendMessage("/spass " + this.conf.accessCommand + " remove [Name1] (Name2): Removes user from the passage list.");
                        player.sendMessage("/spass " + this.conf.accessCommand + " clear: Destroys existing access list.");
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("message") || args[0].equalsIgnoreCase("m")) {
                        displayPassageMessages(player);
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("identify")) {
                        identifyBlock(player);
                        return true;
                    }
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase(this.conf.createCommand)) {
                        if (!args[1].equalsIgnoreCase("switch")) {
                            startBuildingWatch(player, args[1]);
                        } else {
                            player.sendMessage(this.prefix + "Don't be a troublemaker...");
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase(this.conf.destroyCommand)) {
                        if (!args[1].equalsIgnoreCase("switch")) {
                            destroyPass(player, args[1]);
                        } else {
                            prepDestroySwitch(player);
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase(this.conf.toggleCommand) && player.hasPermission(this.tPerm)) {
                        opTogglePass(player, args[1]);
                        return true;
                    }
                    if (args[0].equalsIgnoreCase(this.conf.listCommand) && player.hasPermission(this.lOPerm)) {
                        opListPassages(player, args[1]);
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("timer")) {
                        setTimer(player, args[1]);
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("info")) {
                        displayPassageInfo(player, args[1]);
                        return true;
                    }
                    if (args[0].equalsIgnoreCase(this.conf.accessCommand) && args[1].equalsIgnoreCase("clear")) {
                        if (this.builders.containsKey(player.getName())) {
                            resetPassageAccess(player);
                        } else {
                            player.sendMessage(this.prefix + "You must be working on a passage to reset its access list.");
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("reverse") && args[1].equalsIgnoreCase("timer")) {
                        reverseTimer(player);
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("message") || args[0].equalsIgnoreCase("m")) {
                        addMessage(player, args);
                        return true;
                    }
                } else if (args.length > 2) {
                    if (args[0].equalsIgnoreCase(this.conf.accessCommand)) {
                        if (this.builders.containsKey(player.getName())) {
                            if (args[1].equalsIgnoreCase("allow") || args[1].equalsIgnoreCase("deny") || args[1].equalsIgnoreCase("remove")) {
                                changeAccess(player, args);
                                return true;
                            }
                        } else {
                            player.sendMessage(this.prefix + "You must be working on a passage to change its access list.");
                            return true;
                        }
                    } else if (args[0].equalsIgnoreCase("message") || args[0].equalsIgnoreCase("m")) {
                        addMessage(player, args);
                        return true;
                    }
                }
            } else if (this.builders.containsKey(player.getName())) {
                endBuild(player);
                return true;
            }
        }
        return false;
    }

    public void startBuildingWatch(Player player, String passName) {
        String owner = player.getName();
        boolean canAccess = true;
        if (this.passMan.passExists(passName)) {
            Passage passage = this.passMan.passages.get(this.passMan.findPass(passName));
            owner = passage.owner;
            canAccess = this.passMan.canAccess(passName, player.getName());
        } else {
            this.passMan.createPass(player, passName);
        }
        if (owner.equals(player.getName()) || !this.conf.ownerOnly) {
            if (canAccess || player.hasPermission(this.aXPerm)) {
                this.builders.put(player.getName(), passName);
                player.sendMessage(this.prefix + "You are now working on " + this.prefix + ".");
            } else {
                player.sendMessage(this.prefix + "You are not allowed to work on this passage.");
            }
        } else if (player.hasPermission(this.cOPerm)) {
            if (this.passMan.canAccess(passName, player.getName()) || player.hasPermission(this.aXPerm)) {
                this.builders.put(player.getName(), passName);
                player.sendMessage(this.prefix + "You are now working on " + this.prefix + "'s passage, " + owner + ".");
            } else {
                player.sendMessage(this.prefix + "You have been locked out of " + this.prefix + ".");
            }
        } else {
            player.sendMessage(this.prefix + "You do not own that passage. Please select another.");
        }
    }

    public void constructPassage(Player player, Block block) {
        String passage = this.builders.get(player.getName());
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (this.conf.validMaterials.contains(heldItem.getType()) || this.conf.switchMaterials.contains(heldItem.getType())) {
            if (!this.passMan.passExists(passage))
                this.passMan.createPass(player, passage);
            if (((Passage)this.passMan.passages.get(this.passMan.findPass(passage))).world.equals(player.getWorld().getName())) {
                if (!this.conf.switchMaterials.contains(heldItem.getType())) {
                    block.setType(heldItem.getType());
                    try {
                        block.setBlockData((BlockData)heldItem.getItemMeta());
                    } catch (Exception exception) {}
                    if (this.conf.consumeMaterials && player.getGameMode() != GameMode.CREATIVE)
                        if (heldItem.getAmount() > 1) {
                            heldItem.setAmount(heldItem.getAmount() - 1);
                        } else {
                            player.getInventory().setItemInMainHand(null);
                        }
                    this.passMan.addToPass(passage, block);
                    if (this.conf.addNotify)
                        player.sendMessage(this.prefix + "Block added to " + this.prefix);
                    this.passMan.savePassage(passage);
                } else if (this.passMan.findSwitch(block) == -1) {
                    block.setType(this.conf.defaultSwitch);
                    if (this.conf.consumeSwitch && player.getGameMode() != GameMode.CREATIVE)
                        if (heldItem.getAmount() > 1) {
                            heldItem.setAmount(heldItem.getAmount() - 1);
                        } else {
                            player.getInventory().setItemInMainHand(null);
                        }
                    this.passMan.addSwitch(passage, block);
                    player.sendMessage(this.prefix + "Switch for " + this.prefix + " created.");
                    this.passMan.savePassage(passage);
                } else {
                    player.sendMessage(this.prefix + "This block is already a switch.");
                }
            } else {
                player.sendMessage(this.prefix + "Passages cannot span between worlds.");
            }
        }
    }

    public boolean checkSwitch(Block block, Player player) {
        String passName = "";
        int sIndex = this.passMan.findSwitch(block);
        if (sIndex > -1) {
            passName = ((Passage)this.passMan.passages.get(sIndex)).name;
            if (player.hasPermission(this.xPerm) && !player.hasPermission(this.aXPerm)) {
                player.sendMessage(this.prefix + "You are not allowed to activate switches.");
            } else if (this.passMan.canAccess(passName, player.getName())) {
                togglePass(passName, block.getWorld(), player);
                return true;
            }
        }
        return false;
    }

    public void togglePass(String passName, World world, Player player) {
        Passage pass = this.passMan.passages.get(this.passMan.findPass(passName));
        if (pass.blocks.size() > 0) {
            PassBlock pBlock = pass.blocks.get(0);
            Block cBlock = world.getBlockAt(pBlock.x, pBlock.y, pBlock.z);
            if (cBlock.getType() != pBlock.type) {
                for (PassBlock block : pass.blocks) {
                    pBlock = block;
                    cBlock = world.getBlockAt(pBlock.x, pBlock.y, pBlock.z);
                    cBlock.setType(pBlock.type);
                    cBlock.setBlockData(pBlock.data);
                }
                if (pass.onMessage.length() > 0 && player != null)
                    player.sendMessage(this.prefix + this.prefix);
                if (pass.timer > 0 && pass.reverseTimer)
                    scheduleOpeningPass(pass, world, player);
            } else {
                for (PassBlock block : pass.blocks) {
                    pBlock = block;
                    world.getBlockAt(pBlock.x, pBlock.y, pBlock.z).setType(Material.AIR);
                }
                if (pass.offMessage.length() > 0 && player != null)
                    player.sendMessage(this.prefix + this.prefix);
                if (pass.timer > 0 && !pass.reverseTimer)
                    scheduleClosingPass(pass, world, player);
            }
        }
    }

    public void scheduleOpeningPass(Passage pass, World world, Player player) {
        String p = pass.name;
        World w = world;
        Player pl = player;
        getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this, () -> openPass(p, w, pl), pass.timer);
    }

    public void scheduleClosingPass(Passage pass, World world, Player player) {
        String p = pass.name;
        World w = world;
        Player pl = player;
        getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this, () -> closePass(p, w, pl), pass.timer);
    }

    public void closePass(String passName, World world, Player player) {
        Passage pass = this.passMan.passages.get(this.passMan.findPass(passName));
        if (pass.blocks.size() > 0)
            for (PassBlock block : pass.blocks) {
                PassBlock pBlock = block;
                world.getBlockAt(pBlock.x, pBlock.y, pBlock.z).setType(pBlock.type);
                world.getBlockAt(pBlock.x, pBlock.y, pBlock.z).setBlockData(pBlock.data);
            }
        if (pass.onMessage.length() > 0 && player != null)
            player.sendMessage(this.prefix + this.prefix);
    }

    public void openPass(String passName, World world, Player player) {
        Passage pass = this.passMan.passages.get(this.passMan.findPass(passName));
        if (pass.blocks.size() > 0)
            for (PassBlock block : pass.blocks) {
                PassBlock pBlock = block;
                world.getBlockAt(pBlock.x, pBlock.y, pBlock.z).setType(Material.AIR);
            }
        if (pass.offMessage.length() > 0 && player != null)
            player.sendMessage(this.prefix + this.prefix);
    }

    public void listPassages(Player player) {
        player.sendMessage(this.prefix + "Passage list:");
        player.sendMessage(this.passMan.listPassages(player.getName()));
    }

    public void destroyPass(Player player, String passName) {
        if (this.passMan.passExists(passName)) {
            String owner = ((Passage)this.passMan.passages.get(this.passMan.findPass(passName))).owner;
            if (owner.equals(player.getName()) || player.isOp()) {
                this.passMan.destroyPass(passName);
                player.sendMessage(this.prefix + this.prefix + " has been destroyed.");
            } else {
                player.sendMessage(this.prefix + "You do not control " + this.prefix + ".");
            }
        } else {
            player.sendMessage(this.prefix + this.prefix + " doesn't exist.");
        }
    }

    public void prepDestroySwitch(Player player) {
        if (!this.switchDestroyer.contains(player.getName())) {
            this.switchDestroyer.add(player.getName());
            player.sendMessage(this.prefix + "Right-click on the switch to destroy it.");
        } else {
            this.switchDestroyer.remove(player.getName());
            player.sendMessage(this.prefix + "Switch destruction has been cancelled.");
        }
    }

    public void destroySwitch(Player player, Block block) {
        int sIndex = this.passMan.findSwitch(block);
        if (sIndex > -1) {
            String owner = ((Passage)this.passMan.passages.get(sIndex)).owner;
            if (owner.equals(player.getName()) || player.isOp()) {
                ((Passage)this.passMan.passages.get(sIndex)).removeSwitch(block);
                player.sendMessage(this.prefix + "Switch destroyed.");
                this.switchDestroyer.remove(player.getName());
                this.passMan.savePassage(((Passage)this.passMan.passages.get(sIndex)).name);
            } else {
                player.sendMessage(this.prefix + "You do not own this passage.");
                prepDestroySwitch(player);
            }
        } else {
            player.sendMessage(this.prefix + "The block you clicked is not a switch.");
            prepDestroySwitch(player);
        }
    }

    public void opTogglePass(Player player, String passName) {
        if (this.passMan.passExists(passName)) {
            String passWorld = ((Passage)this.passMan.passages.get(this.passMan.findPass(passName))).world;
            World theWorld = getServer().getWorld(passWorld);
            if (theWorld != null) {
                togglePass(passName, theWorld, player);
                player.sendMessage(this.prefix + "Passage " + this.prefix + " reacts to your will.");
            } else {
                player.sendMessage(this.prefix + "Something is blocking your will from reaching " + this.prefix + " in " + passName + ".");
            }
        } else {
            player.sendMessage(this.prefix + this.prefix + " doesn't exist.");
        }
    }

    public void endBuild(Player player) {
        this.builders.remove(player.getName());
        player.sendMessage(this.prefix + "You are no longer working on a passage.");
    }

    public void destructPassage(Player player, Block block) {
        int pIndex = this.passMan.findPass(this.builders.get(player.getName()));
        if (pIndex > -1) {
            Passage thisPass = this.passMan.passages.get(pIndex);
            boolean hasBlock = thisPass.hasBlock(block);
            if (hasBlock) {
                thisPass.blocks.remove(thisPass.findBlock(block));
                if (this.conf.removeNotify)
                    player.sendMessage(this.prefix + "Block removed from " + this.prefix);
                this.passMan.savePassage(thisPass.name);
            }
        }
    }

    public boolean protectPassages(Block block) {
        for (Passage passage : this.passMan.passages) {
            if (passage.hasBlock(block))
                return true;
        }
        return false;
    }

    public void opListPassages(Player op, String pName) {
        String passes = this.passMan.listPassages(pName);
        if (!passes.equals("")) {
            op.sendMessage(this.prefix + "Passage list for " + this.prefix + ":");
            op.sendMessage(passes);
        } else {
            op.sendMessage(this.prefix + this.prefix + " has not created any passages.");
        }
    }

    public void helpMessage(Player player) {
        player.sendMessage(this.prefix + "You have access to the following commands:");
        player.sendMessage("/spass " + this.conf.createCommand + " (Passage): Starts work on a passage.");
        player.sendMessage("/spass " + this.conf.listCommand + ": Lists the passages you own.");
        if (player.hasPermission(this.lOPerm))
            player.sendMessage("/spass " + this.conf.listCommand + " (Player): Lists passages owned by chosen player.");
        if (player.hasPermission(this.tPerm))
            player.sendMessage("/spass " + this.conf.toggleCommand + " (Passage): Turns passage in your current world on/off.");
        if (!player.hasPermission(this.dOPerm)) {
            player.sendMessage("/spass " + this.conf.destroyCommand + " (Passage): Removes a passage you own.");
        } else {
            player.sendMessage("/spass " + this.conf.destroyCommand + " (Passage): Removes the chosen passage.");
        }
        player.sendMessage("/spass " + this.conf.destroyCommand + " switch: Destroys the next switch you right-click.");
        if (player.hasPermission(this.rPerm) && this.conf.useRedstone)
            player.sendMessage("/spass " + this.conf.resetCommand + ": Resets all passages based on redstone power.");
        player.sendMessage("/spass timer (ticks): Sets the reset timer of the passage.");
        player.sendMessage("/spass timer reverse: Timer will open passage instead (or reverse if used again).");
        player.sendMessage("/spass message [open/close]: Shows the respective message for passage.");
        player.sendMessage("/spass message [open/close] remove: Removes message from passage.");
        player.sendMessage("/spass message [open/close] (message): Adds message to passage.");
        player.sendMessage("  -Shortcuts m, o, and c can be used to save space.");
        player.sendMessage("/spass: Ends work on a passage.");
    }

    public void redstoneToggle(World world) {
        for (Passage passage : this.passMan.passages) {
            if (world.getName().equals(passage.world) &&
                    passage.checkPowerChange(world))
                togglePass(passage.name, world, null);
        }
    }

    public void setRedstoneStates() {
        List<World> worlds = getServer().getWorlds();
        for (Passage passage : this.passMan.passages) {
            Passage pass = passage;
            if (!pass.world.equals("")) {
                World world = getServer().getWorld(pass.world);
                if (worlds.contains(world)) {
                    pass.hasPower = pass.checkPower(world);
                    if (pass.blocks.size() > 0) {
                        PassBlock pBlock = pass.blocks.get(0);
                        Block cBlock = world.getBlockAt(pBlock.x, pBlock.y, pBlock.z);
                        if (cBlock.getType() != pBlock.type && !pass.hasPower) {
                            togglePass(pass.name, world, null);
                            continue;
                        }
                        if (cBlock.getType() == pBlock.type && pass.hasPower)
                            togglePass(pass.name, world, null);
                    }
                }
            }
        }
    }

    public void setTimer(Player player, String time) {
        try {
            int myTime = Integer.parseInt(time);
            if (this.builders.containsKey(player.getName())) {
                Passage passage = this.passMan.passages.get(this.passMan.findPass(this.builders.get(player.getName())));
                passage.timer = myTime;
                player.sendMessage(this.prefix + this.prefix + " timer set to " + passage.name + " ticks.");
                if (myTime == 0)
                    player.sendMessage(this.prefix + this.prefix + " will no longer automatically reset.");
                this.passMan.savePassage(passage.name);
            } else {
                player.sendMessage(this.prefix + "You must be working on a passage to set its timer.");
            }
        } catch (Exception e) {
            player.sendMessage(this.prefix + "Please enter a valid time in ticks");
        }
    }

    public boolean checkBlocks(List<Block> myBlocks) {
        for (Block block : myBlocks) {
            if (protectPassages(block))
                return true;
        }
        return false;
    }

    public void changeAccess(Player player, String[] info) {
        Passage myPass = this.passMan.passages.get(this.passMan.findPass(this.builders.get(player.getName())));
        if (player.getName().equals(myPass.owner) || player.hasPermission(this.aXPerm) || player.isOp()) {
            int successfulAdds = 0;
            StringBuilder badAdds = new StringBuilder();
            String newList = "white", oldList = "black";
            List<Player> onlinePlayers = (List<Player>)getServer().getOnlinePlayers();
            OfflinePlayer[] offlinePlayers = getServer().getOfflinePlayers();
            Vector<String> allPlayers = new Vector<>();
            for (Player onlinePlayer : onlinePlayers)
                allPlayers.add(onlinePlayer.getName().toLowerCase());
            for (OfflinePlayer offlinePlayer : offlinePlayers)
                allPlayers.add(offlinePlayer.getName().toLowerCase());
            if (info[1].equalsIgnoreCase("deny")) {
                newList = "black";
                oldList = "white";
            }
            if (info[1].equalsIgnoreCase("allow") || info[1].equalsIgnoreCase("deny")) {
                if (!myPass.listStyle.equals(newList)) {
                    myPass.listStyle = newList;
                    player.sendMessage(this.prefix + this.prefix + " now uses a " + myPass.name + "list.");
                    if (myPass.nameList.size() > 0) {
                        myPass.nameList.clear();
                        player.sendMessage(this.prefix + "Old " + this.prefix + "list has been removed.");
                    }
                }
                for (int x = 2; x < info.length; x++) {
                    if (info[x].equalsIgnoreCase(myPass.owner)) {
                        player.sendMessage(this.prefix + this.prefix + " owns this passage; their access can't be changed.");
                    } else if (allPlayers.contains(info[x].toLowerCase())) {
                        boolean successful = this.passMan.addToList(myPass.name, info[x].toLowerCase());
                        if (successful) {
                            successfulAdds++;
                        } else {
                            badAdds.append(" ").append(info[x]);
                        }
                    } else {
                        badAdds.append(" ").append(info[x]);
                    }
                }
                player.sendMessage(this.prefix + "Names successfully added: " + this.prefix);
                if (badAdds.length() > 0) {
                    player.sendMessage(this.prefix + "Names that could not be added: ");
                    player.sendMessage(badAdds.toString());
                    player.sendMessage(this.prefix + "You must use the full name, and they must have logged in at least once.");
                }
            } else if (info[1].equalsIgnoreCase("remove")) {
                for (int x = 2; x < info.length; x++) {
                    boolean successful = this.passMan.removeFromList(myPass.name, info[x].toLowerCase());
                    if (successful) {
                        successfulAdds++;
                    } else {
                        badAdds.append(" ").append(info[x]);
                    }
                }
                player.sendMessage(this.prefix + "Names successfully removed: " + this.prefix);
                if (badAdds.length() > 0) {
                    player.sendMessage(this.prefix + "Names that could not be removed: ");
                    player.sendMessage(badAdds.toString());
                }
            }
            if (successfulAdds > 0)
                this.passMan.savePassage(myPass.name);
        } else {
            player.sendMessage(this.prefix + "Only the passage owner or an Op can change access lists.");
        }
    }

    public void displayPassageInfo(Player player, String passName) {
        Passage myPass = this.passMan.passages.get(this.passMan.findPass(passName));
        player.sendMessage(this.prefix + "Passage " + this.prefix + " info:");
        if (!this.passMan.passExists(passName)) {
            player.sendMessage(this.prefix + this.prefix + " doesn't exist yet.");
        } else {
            player.sendMessage("Owner: " + myPass.owner);
            player.sendMessage("World: " + myPass.world);
            if (player.getName().equals(myPass.owner) || !this.conf.ownerOnly || player.hasPermission(this.aXPerm) || player.isOp()) {
                if (myPass.timer > 0)
                    player.sendMessage("Timer: " + myPass.timer + " ticks. (20 ticks = 1 second)");
                player.sendMessage("Size: " + myPass.blocks.size() + " blocks, " + myPass.switches.size() + " switches.");
                if (myPass.nameList.size() == 0) {
                    player.sendMessage("Access: Anyone can use this passage.");
                } else {
                    player.sendMessage("Access: This passage uses a " + myPass.listStyle + "list.");
                    player.sendMessage("Names: " + this.passMan.getAccessList(passName));
                }
            }
        }
    }

    public void resetPassageAccess(Player player) {
        Passage myPass = this.passMan.passages.get(this.passMan.findPass(this.builders.get(player.getName())));
        if (player.getName().equals(myPass.owner) || player.hasPermission(this.aXPerm) || player.isOp()) {
            myPass.nameList.removeAllElements();
            myPass.listStyle = "";
            player.sendMessage(this.prefix + "Access list for " + this.prefix + " reset.");
            this.passMan.savePassage(myPass.name);
        } else {
            player.sendMessage(this.prefix + "Only the passage owner or an Op may reset its access list.");
        }
    }

    public void reverseTimer(Player p) {
        String player = p.getName();
        if (this.builders.containsKey(player)) {
            Passage passage = this.passMan.passages.get(this.passMan.findPass(this.builders.get(player)));
            if (passage.reverseTimer) {
                passage.reverseTimer = false;
                p.sendMessage(this.prefix + this.prefix + " will appear after " + passage.name + " ticks.");
            } else {
                passage.reverseTimer = true;
                p.sendMessage(this.prefix + this.prefix + " will disappear after " + passage.name + " ticks.");
            }
            this.passMan.savePassage(passage.name);
        } else {
            p.sendMessage(this.prefix + "You must be working on a passage to use this command.");
        }
    }

    public void displayPassageMessages(Player player) {
        if (this.builders.containsKey(player.getName())) {
            Passage passage = this.passMan.passages.get(this.passMan.findPass(this.builders.get(player.getName())));
            if (passage.onMessage.length() == 0 && passage.offMessage.length() == 0) {
                player.sendMessage(this.prefix + this.prefix + " currently doesn't have any messages.");
            } else {
                player.sendMessage(this.prefix + "Messages for " + this.prefix + ":");
                if (passage.offMessage.length() > 0)
                    player.sendMessage(this.prefix + "Opening: " + this.prefix);
                if (passage.onMessage.length() > 0)
                    player.sendMessage(this.prefix + "Closing: " + this.prefix);
            }
        } else {
            player.sendMessage(this.prefix + "You must be working on a passage to use this command.");
        }
    }

    public void addMessage(Player player, String[] args) {
        boolean shouldSave = false;
        if (this.builders.containsKey(player.getName())) {
            Passage passage = this.passMan.passages.get(this.passMan.findPass(this.builders.get(player.getName())));
            if (args[1].equalsIgnoreCase("close") || args[1].equalsIgnoreCase("c")) {
                if (args.length == 2)
                    if (passage.onMessage.equals("")) {
                        player.sendMessage(this.prefix + "This passage doesn't have a closing message.");
                    } else {
                        player.sendMessage(this.prefix + this.prefix + " closing message: ");
                        player.sendMessage(this.prefix + this.prefix);
                    }
                if (args.length == 3 && args[2].equalsIgnoreCase("remove")) {
                    passage.onMessage = "";
                    player.sendMessage(this.prefix + "Closing message for " + this.prefix + " removed.");
                    shouldSave = true;
                } else if (args.length >= 3) {
                    StringBuilder message = new StringBuilder(args[2]);
                    for (int x = 3; x < args.length; x++)
                        message.append(" ").append(args[x]);
                    player.sendMessage(this.prefix + "Closing " + this.prefix + " will now display this message:");
                    player.sendMessage(this.prefix + this.prefix);
                    passage.onMessage = message.toString();
                    shouldSave = true;
                }
            } else if (args[1].equalsIgnoreCase("open") || args[1].equalsIgnoreCase("o")) {
                if (args.length == 2)
                    if (passage.offMessage.equals("")) {
                        player.sendMessage(this.prefix + "This passage doesn't have an opening message.");
                    } else {
                        player.sendMessage(this.prefix + this.prefix + " opening message: ");
                        player.sendMessage(this.prefix + this.prefix);
                    }
                if (args.length == 3 && args[2].equalsIgnoreCase("remove")) {
                    passage.offMessage = "";
                    player.sendMessage(this.prefix + "Opening message for " + this.prefix + " removed.");
                    shouldSave = true;
                } else if (args.length >= 3) {
                    StringBuilder message = new StringBuilder(args[2]);
                    for (int x = 3; x < args.length; x++)
                        message.append(" ").append(args[x]);
                    player.sendMessage(this.prefix + "Opening " + this.prefix + " will now display this message:");
                    player.sendMessage(this.prefix + this.prefix);
                    passage.offMessage = message.toString();
                    shouldSave = true;
                }
            } else {
                player.sendMessage(this.prefix + "Unable to recognize command.");
                player.sendMessage(this.prefix + "/spass message [open/close]: Shows the respective message for passage.");
                player.sendMessage(this.prefix + "/spass message [open/close] remove: Removes message from passage.");
                player.sendMessage(this.prefix + "/spass message [open/close] (message): Adds message to passage.");
                player.sendMessage(this.prefix + "Shortcuts m, o, and c can be used to save space.");
            }
            if (shouldSave)
                this.passMan.savePassage(passage.name);
        } else {
            player.sendMessage(this.prefix + "You must be working on a passage to use this command.");
        }
    }

    public void identifyBlock(Player player) {
        Block myBlock = player.getTargetBlock(null, 50);
        int sIndex = this.passMan.findSwitch(myBlock);
        if (sIndex > -1)
            player.sendMessage(this.prefix + "This block is a switch for " + this.prefix);
        for (Passage passage : this.passMan.passages) {
            if (passage.hasBlock(myBlock))
                player.sendMessage(this.prefix + "This block is part of the " + this.prefix + " passage.");
        }
    }
}
