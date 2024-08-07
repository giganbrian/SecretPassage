package me.reop.secretPassage;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Material;

public class DefaultConfig {
    public String activeMaterials = "SPONGE, NETHER_BRICK";

    public String validMaterials = Stream.<Material>of(Material.values())
            .map(Enum::name)
            .collect(Collectors.joining(", "));

    public String switchMaterials = "STONE_BUTTON";

    public Material defaultSwitch = Material.ACACIA_PLANKS;

    public String createCommand = "create";

    public String destroyCommand = "destroy";

    public String listCommand = "list";

    public String toggleCommand = "toggle";

    public String resetCommand = "reset";

    public boolean consumeMaterials = true;

    public boolean consumeSwitch = true;

    public boolean addNotify = false;

    public boolean removeNotify = true;

    public boolean protectNotify = false;

    public boolean toggleNotify = false;

    public boolean protectBlocks = true;

    public boolean ownerOnly = true;

    public boolean useRedstone = true;

    public boolean redstoneAtStart = false;

    public boolean needCommandPermission = false;
}
