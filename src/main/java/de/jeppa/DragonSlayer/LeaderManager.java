package de.jeppa.DragonSlayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LeaderManager {
    DragonSlayer plugin;
    static List<Entry<String, Object>> sortKillList = new ArrayList<Entry<String, Object>>();
    public FileConfiguration Leader = null;
    private File LeaderFile = null;

    public LeaderManager(DragonSlayer instance) { this.plugin = instance; }

    private void reloadLeaders() {
        if (this.LeaderFile == null) {
            this.LeaderFile = new File(this.plugin.getDataFolder(), "LeaderList.yml");
        }

        this.Leader = YamlConfiguration.loadConfiguration(this.LeaderFile);
    }

    FileConfiguration getLeaderlist() {
        if (this.Leader == null) {
            this.reloadLeaders();
        }

        return this.Leader;
    }

    void saveLeaderlist() {
        if (this.Leader != null && this.LeaderFile != null) {
            try {
                this.getLeaderlist().save(this.LeaderFile);
            } catch (IOException var2) {
                this.plugin.logger.warning("Could not save Leaderlist to " + this.LeaderFile);
            }

        }
    }

    public void clearLeaderList() {
        sortKillList.clear();
        this.Leader.set("Scores", (Object) null);
        this.saveLeaderlist();
    }

    void sortLeaderList() {
        ConfigurationSection allScores = this.Leader.getConfigurationSection("Scores");
        if (allScores != null) {
            Map<String, Object> map1 = allScores.getValues(false);
            sortKillList = new ArrayList<Entry<String, Object>>(map1.entrySet());
            Collections.sort(sortKillList, (o1, o2) -> Integer.valueOf(((MemorySection) o2.getValue()).getInt("score"))
                    .compareTo(((MemorySection) o1.getValue()).getInt("score")));
        }

    }

    String getUUIDforRank(int rank) {
        String uid = null;
        if (sortKillList.size() >= rank) {
            Entry<String, Object> id = (Entry<String, Object>) sortKillList.get(rank - 1);
            if (id != null) {
                uid = (String) id.getKey();
            }
        }

        return uid;
    }
}
