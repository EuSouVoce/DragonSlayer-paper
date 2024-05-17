package de.jeppa.DragonSlayer;

import org.bukkit.Material;
import org.bukkit.Tag;

public class FourteenPlusOnlyRoutines {
    DragonSlayer plugin;

    public FourteenPlusOnlyRoutines(DragonSlayer instance) { this.plugin = instance; }

    boolean getBedTag(Material material) { return Tag.BEDS.isTagged(material); }
}
