package me.reop.secretPassage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Vector;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
    static String mainDir = "plugins/SecretPassage";

    static File configFile = new File(mainDir, "Config.yml");

    static YamlConfiguration config;

    static DefaultConfig dConf = new DefaultConfig();

    public List<Material> activeMaterials = new Vector<>();

    public List<Material> validMaterials = new Vector<>();

    public List<Material> switchMaterials = new Vector<>();

    public Material defaultSwitch;

    public boolean consumeMaterials;

    public boolean consumeSwitch;

    public boolean addNotify;

    public boolean removeNotify;

    public boolean protectNotify;

    public boolean toggleNotify;

    public boolean protectBlocks;

    public boolean ownerOnly;

    public boolean useRedstone;

    public boolean redstoneAtStart;

    public boolean creativeAutoAdd;

    public boolean needCommandPermission;

    public String createCommand;

    public String destroyCommand;

    public String listCommand;

    public String toggleCommand;

    public String resetCommand;

    public String accessCommand = "access";

    public void loadConfig() {
        boolean needFixing = false;
        boolean newConfig = false;
        StringBuilder iFixed = new StringBuilder();
        (new File(mainDir)).mkdir();
        if (!configFile.exists()) {
            newConfig = true;
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
                bw.newLine();
                bw.close();
            } catch (Exception e) {
                System.out.print(e);
            }
        }
        try {
            config = YamlConfiguration.loadConfiguration(configFile);
            try {
                this.activeMaterials = parseMaterials(config.getString("active-material-IDs"), Material.class);
            } catch (Exception e) {
                this.activeMaterials = parseMaterials(dConf.activeMaterials, Material.class);
                needFixing = true;
                iFixed.append("active-material-IDs ");
            }
            try {
                this.validMaterials = parseMaterials(config.getString("passage-material-IDs"), Material.class);
            } catch (Exception e) {
                this.validMaterials = parseMaterials(dConf.validMaterials, Material.class);
                needFixing = true;
                iFixed.append("passage-material-IDs ");
            }
            try {
                this.switchMaterials = parseMaterials(config.getString("switch-material-IDs"), Material.class);
            } catch (Exception e) {
                this.switchMaterials = parseMaterials(dConf.switchMaterials, Material.class);
                needFixing = true;
                iFixed.append("switch-material-IDs ");
            }
            String curProp = config.getString("switch-default-block");
            if (curProp != null) {
                this.defaultSwitch = Material.matchMaterial(curProp.trim());
            } else {
                this.defaultSwitch = dConf.defaultSwitch;
                needFixing = true;
                iFixed.append("switch-default-block ");
            }
            curProp = config.getString("create-command");
            this.createCommand = (curProp != null) ? curProp : dConf.createCommand;
            if (curProp == null) {
                needFixing = true;
                iFixed.append("create-command ");
            }
            curProp = config.getString("destroy-command");
            this.destroyCommand = (curProp != null) ? curProp : dConf.destroyCommand;
            if (curProp == null) {
                needFixing = true;
                iFixed.append("destroy-command ");
            }
            curProp = config.getString("list-command");
            this.listCommand = (curProp != null) ? curProp : dConf.listCommand;
            if (curProp == null) {
                needFixing = true;
                iFixed.append("list-command ");
            }
            curProp = config.getString("toggle-command");
            this.toggleCommand = (curProp != null) ? curProp : dConf.toggleCommand;
            if (curProp == null) {
                needFixing = true;
                iFixed.append("toggle-command ");
            }
            curProp = config.getString("reset-command");
            this.resetCommand = (curProp != null) ? curProp : dConf.resetCommand;
            if (curProp == null) {
                needFixing = true;
                iFixed.append("reset-command ");
            }
            this.consumeMaterials = config.getBoolean("passage-consume", dConf.consumeMaterials);
            this.consumeSwitch = config.getBoolean("switch-consume", dConf.consumeSwitch);
            this.addNotify = config.getBoolean("add-block-notify", dConf.addNotify);
            this.removeNotify = config.getBoolean("remove-block-notify", dConf.removeNotify);
            this.protectNotify = config.getBoolean("block-protect-notify", dConf.protectNotify);
            this.toggleNotify = config.getBoolean("toggle-notify", dConf.toggleNotify);
            this.protectBlocks = config.getBoolean("protect-blocks", dConf.protectBlocks);
            this.needCommandPermission = config.getBoolean("use-command-permission", dConf.needCommandPermission);
            this.ownerOnly = config.getBoolean("owner-passage-protect", dConf.ownerOnly);
            this.useRedstone = config.getBoolean("activate-with-redstone", dConf.useRedstone);
            this.redstoneAtStart = config.getBoolean("redstone-at-start", dConf.redstoneAtStart);
            if (newConfig) {
                createConfig();
                System.out.println("[SecretPassage] Config file created.");
            } else if (needFixing) {
                System.out.println("[SecretPassage] Fixing config file for the following: " + String.valueOf(iFixed));
                createConfig();
            } else {
                System.out.println("[SecretPassage] User settings successfully loaded.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createConfig() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
            bw.write("## SecretPassage Options File");
            bw.newLine();
            bw.newLine();
            bw.write("# Building Options");
            bw.newLine();
            bw.write("#------------------");
            bw.newLine();
            bw.write("# These options allow you to change what you can use to build passages, as well as what can be");
            bw.newLine();
            bw.write("# used within, and determine whether or not passages use up resources.");
            bw.newLine();
            bw.newLine();
            bw.write("# What materials (by ID) should be used to mold the passage? Please separate choices with a comma.");
            bw.newLine();
            bw.write("active-material-IDs: " + String.join(", ", (Iterable)getMaterialNames(this.activeMaterials)));
            bw.newLine();
            bw.write("# What materials are allowed to be used in making passages? Please separate with commas.");
            bw.newLine();
            bw.write("passage-material-IDs: " + String.join(", ", (Iterable)getMaterialNames(this.validMaterials)));
            bw.newLine();
            bw.write("# Should blocks be consumed when used while decorating a passage?");
            bw.newLine();
            bw.write("passage-consume: " + Boolean.toString(this.consumeMaterials));
            bw.newLine();
            bw.write("# What materials should be absorbed by blocks to create a switch?");
            bw.newLine();
            bw.write("switch-material-IDs: " + String.join(", ", (Iterable)getMaterialNames(this.switchMaterials)));
            bw.newLine();
            bw.write("# Should the item be consumed when the switch is created?");
            bw.newLine();
            bw.write("switch-consume: " + Boolean.toString(this.consumeSwitch));
            bw.newLine();
            bw.write("# What block ID should the switch turn into when created?");
            bw.newLine();
            bw.write("switch-default-block: " + this.defaultSwitch.name());
            bw.newLine();
            bw.newLine();
            bw.write("# Command Options");
            bw.newLine();
            bw.write("#------------------");
            bw.newLine();
            bw.write("# These options will allow you to change the second portion of the commands used by");
            bw.newLine();
            bw.write("# the plugin. (Example: /spass create, /spass summon, /spass unbind, etc.)");
            bw.newLine();
            bw.write("# Any spaces in the command names will be removed.");
            bw.newLine();
            bw.newLine();
            bw.write("# Do players need the secretpassage.useCommands permission to use commands?");
            bw.newLine();
            bw.write("use-command-permission: " + Boolean.toString(this.needCommandPermission));
            bw.newLine();
            bw.write("# What command should be used to create and modify passages? Default is create.");
            bw.newLine();
            bw.write("create-command: " + this.createCommand);
            bw.newLine();
            bw.write("# What command should be used to remove passages? Default is destroy.");
            bw.newLine();
            bw.write("destroy-command: " + this.destroyCommand);
            bw.newLine();
            bw.write("# What command should be used to list passages that the player owns? Default is list.");
            bw.newLine();
            bw.write("list-command: " + this.listCommand);
            bw.newLine();
            bw.write("# What command should be used to remotely turn passages on or off? Default is toggle.");
            bw.newLine();
            bw.write("toggle-command: " + this.toggleCommand);
            bw.newLine();
            bw.write("# If using redstone power, what command should be used to reset passages based on redstone states?");
            bw.newLine();
            bw.write("reset-command: " + this.resetCommand);
            bw.newLine();
            bw.newLine();
            bw.write("# Notification Options");
            bw.newLine();
            bw.write("#------------------");
            bw.newLine();
            bw.write("# These options allow you to choose if and when the plugin will notify you while using it.");
            bw.newLine();
            bw.write("# Please note that you cannot turn off notices for the following: Starting passage construction,");
            bw.newLine();
            bw.write("# Ending passage construction, and deleting passages or switches.");
            bw.newLine();
            bw.newLine();
            bw.write("# Should the plugin notify you when a block is successfully added to the passage?");
            bw.newLine();
            bw.write("add-block-notify: " + Boolean.toString(this.addNotify));
            bw.newLine();
            bw.write("# Should the plugin notify you when a block has been removed from the passage?");
            bw.newLine();
            bw.write("remove-block-notify: " + Boolean.toString(this.removeNotify));
            bw.newLine();
            bw.write("# Should the plugin notify you when a block you try to break is part of a different passage?");
            bw.newLine();
            bw.write("block-protect-notify: " + Boolean.toString(this.protectNotify));
            bw.newLine();
            bw.write("# Should the plugin notify you when you turn a passage on or off?");
            bw.newLine();
            bw.write("toggle-notify: " + Boolean.toString(this.toggleNotify));
            bw.newLine();
            bw.newLine();
            bw.write("# Protection Options");
            bw.newLine();
            bw.write("#------------------");
            bw.newLine();
            bw.write("# These options determine what the plugin's simple protection system will monitor.");
            bw.newLine();
            bw.newLine();
            bw.write("# Should the plugin prevent blocks in passages from being broken?");
            bw.newLine();
            bw.write("# WARNING: Setting to false can allow for easy item duplication.");
            bw.newLine();
            bw.write("protect-blocks: " + Boolean.toString(this.protectBlocks));
            bw.newLine();
            bw.write("# Should the plugin allow only the original creator of a passage to modify it?");
            bw.newLine();
            bw.write("# NOTE: Ops automatically override this protection feature.");
            bw.newLine();
            bw.write("owner-passage-protect: " + Boolean.toString(this.ownerOnly));
            bw.newLine();
            bw.write("# Should passages activate with redstone power?");
            bw.newLine();
            bw.write("activate-with-redstone: " + Boolean.toString(this.useRedstone));
            bw.newLine();
            bw.write("# Should the plugin check for redstone power to switches at startup?");
            bw.newLine();
            bw.write("# NOTE: Can cause issues on multi-world servers.");
            bw.newLine();
            bw.write("redstone-at-start: " + Boolean.toString(this.redstoneAtStart));
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T extends Enum<T>> List<T> parseMaterials(String inputString, Class<T> enumType) {
        List<T> results = new Vector<>();
        String[] items = inputString.split(",");
        for (String item : items) {
            try {
                results.add(Enum.valueOf(enumType, item.trim().toUpperCase()));
            } catch (IllegalArgumentException illegalArgumentException) {}
        }
        return results;
    }

    private List<String> getMaterialNames(List<Material> materials) {
        List<String> names = new Vector<>();
        for (Material material : materials)
            names.add(material.name());
        return names;
    }
}
