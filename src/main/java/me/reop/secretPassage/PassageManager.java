package me.reop.secretPassage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Vector;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PassageManager {
    static String mainDir = "plugins/SecretPassage";

    static String passDir = "plugins/SecretPassage/Passages";

    static String switchDir = "plugins/SecretPassage/Switches";

    static String accessDir = "plugins/SecretPassage/AccessLists";

    static String miscDir = "plugins/SecretPassage/Misc";

    static File switchFile = new File(mainDir + mainDir + "Switches.dat");

    static Properties prop = new Properties();

    public Vector<Passage> passages = new Vector<>();

    public void setup() {
        (new File(mainDir)).mkdirs();
        loadFiles(passDir, ".pass", this::loadPassage, "[SecretPassage] Loaded %d passages.");
        loadFiles(switchDir, ".switch", this::loadSwitches, "[Secret Passage] Loaded switches for %d passages.");
        loadFiles(accessDir, ".access", this::loadAccess, "[Secret Passage] Loaded access lists for %d passages.");
        loadFiles(miscDir, ".misc", this::loadMisc, "[Secret Passage] Loaded misc files for %d passages.");
    }

    private void loadFiles(String directory, String extension, FileProcessor processor, String successMessage) {
        File dir = new File(directory);
        dir.mkdirs();
        String[] fileList = dir.list();
        if (fileList != null) {
            long count = 0L;
            for (String s : fileList) {
                if (s.endsWith(extension)) {
                    processor.process(new File(directory + directory + File.separator));
                    count++;
                }
            }
            System.out.printf(successMessage + "%n", new Object[] { Long.valueOf(count) });
        }
    }

    public boolean passExists(String passName) {
        return this.passages.stream().anyMatch(p -> p.name.equals(passName));
    }

    public int findPass(String passName) {
        for (int x = 0; x < this.passages.size(); x++) {
            if (((Passage)this.passages.get(x)).name.equals(passName))
                return x;
        }
        return -1;
    }

    public void createPass(Player player, String passName) {
        Passage newPassage = new Passage(player, passName);
        this.passages.add(newPassage);
        savePassage(passName);
    }

    public void addToPass(String passName, Block block) {
        int pIndex = findPass(passName);
        Passage pass = this.passages.get(pIndex);
        if (!pass.hasBlock(block)) {
            pass.addBlock(block);
            this.passages.set(pIndex, pass);
            savePassage(passName);
        }
    }

    public boolean addSwitch(String passName, Block block) {
        if (!((Passage)this.passages.get(findPass(passName))).hasSwitch(block)) {
            ((Passage)this.passages.get(findPass(passName))).addSwitch(block);
            savePassage(passName);
            return true;
        }
        return false;
    }

    public int findSwitch(Block block) {
        for (int x = 0; x < this.passages.size(); x++) {
            if (((Passage)this.passages.get(x)).hasSwitch(block))
                return x;
        }
        return -1;
    }

    public void destroyPass(String passName) {
        int pIndex = findPass(passName);
        deleteFile(passDir + passDir + File.separator + ".pass");
        deleteFile(switchDir + switchDir + File.separator + ".switch");
        deleteFile(accessDir + accessDir + File.separator + ".access");
        this.passages.remove(pIndex);
    }

    private void deleteFile(String path) {
        try {
            Files.deleteIfExists(Paths.get(path, new String[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePassage(String passName) {
        Passage passage = this.passages.get(findPass(passName));
        (new File(passDir)).mkdirs();
        (new File(switchDir)).mkdirs();
        (new File(accessDir)).mkdirs();
        saveToFile(passDir + passDir + File.separator + ".pass", writer -> {
            writer.write(passage.owner);
            writer.newLine();
            writer.write(passage.world);
            writer.newLine();
            for (PassBlock block : passage.blocks) {
                writer.write(String.format("%d,%d,%d,%s,%s", new Object[] { Integer.valueOf(block.x), Integer.valueOf(block.y), Integer.valueOf(block.z), block.type.name(), block.data.getAsString() }));
                writer.newLine();
            }
        });
        saveToFile(switchDir + switchDir + File.separator + ".switch", writer -> {
            writer.write(Integer.toString(passage.timer));
            writer.newLine();
            for (SwitchBlock block : passage.switches) {
                writer.write(String.format("%d,%d,%d", new Object[] { Integer.valueOf(block.x), Integer.valueOf(block.y), Integer.valueOf(block.z) }));
                writer.newLine();
            }
        });
        if (!passage.nameList.isEmpty())
            saveToFile(accessDir + accessDir + File.separator + ".access", writer -> {
                writer.write(passage.listStyle);
                writer.newLine();
                for (String s : passage.nameList) {
                    writer.write(s);
                    writer.newLine();
                }
            });
        saveMisc(passage);
    }

    private void saveToFile(String path, FileWriterProcessor processor) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(path, new String[0]), new java.nio.file.OpenOption[0]);
            try {
                processor.process(writer);
                if (writer != null)
                    writer.close();
            } catch (Throwable throwable) {
                if (writer != null)
                    try {
                        writer.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Passage loadPassage(File passFile) {
        Passage wPass = new Passage(null, null);
        wPass.name = passFile.getName().replace(".pass", "");
        try {
            BufferedReader reader = Files.newBufferedReader(passFile.toPath());
            try {
                wPass.owner = reader.readLine();
                wPass.world = reader.readLine();
                String curLine;
                while ((curLine = reader.readLine()) != null) {
                    String[] info = curLine.split(",");
                    PassBlock cB = new PassBlock(null);
                    cB.x = Integer.parseInt(info[0]);
                    cB.y = Integer.parseInt(info[1]);
                    cB.z = Integer.parseInt(info[2]);
                    cB.type = Material.valueOf(info[3]);
                    cB.data = Bukkit.createBlockData(info[4]);
                    wPass.addBlock(cB);
                }
                if (reader != null)
                    reader.close();
            } catch (Throwable throwable) {
                if (reader != null)
                    try {
                        reader.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[Secret Passage] Error while processing " + passFile.getName());
        }
        this.passages.add(wPass);
        return wPass;
    }

    public void loadSwitches(File switchFile) {
        boolean exists = passExists(switchFile.getName().replace(".switch", ""));
        if (exists) {
            Passage passage = this.passages.get(findPass(switchFile.getName().replace(".switch", "")));
            try {
                BufferedReader reader = Files.newBufferedReader(switchFile.toPath());
                try {
                    String curLine;
                    while ((curLine = reader.readLine()) != null) {
                        String[] info = curLine.split(",");
                        if (info.length == 1) {
                            passage.timer = Integer.parseInt(info[0]);
                            continue;
                        }
                        SwitchBlock cB = new SwitchBlock(null);
                        cB.x = Integer.parseInt(info[0]);
                        cB.y = Integer.parseInt(info[1]);
                        cB.z = Integer.parseInt(info[2]);
                        passage.addSwitch(cB);
                    }
                    if (reader != null)
                        reader.close();
                } catch (Throwable throwable) {
                    if (reader != null)
                        try {
                            reader.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[Secret Passage] Error while processing " + switchFile.getName());
            }
        } else {
            System.out.println("[Secret Passage] Switches found for non-existent passage!");
            System.out.println("[Secret Passage] Please check on this file: " + switchFile.getName());
        }
    }

    public void loadAccess(File accessFile) {
        boolean exists = passExists(accessFile.getName().replace(".access", ""));
        if (exists) {
            Passage passage = this.passages.get(findPass(accessFile.getName().replace(".access", "")));
            try {
                BufferedReader reader = Files.newBufferedReader(accessFile.toPath());
                try {
                    passage.listStyle = reader.readLine();
                    String curLine;
                    while ((curLine = reader.readLine()) != null)
                        passage.nameList.add(curLine);
                    if (reader != null)
                        reader.close();
                } catch (Throwable throwable) {
                    if (reader != null)
                        try {
                            reader.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[Secret Passage] Error while processing " + accessFile.getName());
            }
        } else {
            System.out.println("[Secret Passage] Access list found for non-existent passage!");
            System.out.println("[Secret Passage] Please check on this file: " + accessFile.getName());
        }
    }

    public void loadMisc(File miscFile) {
        String passName = miscFile.getName().replace(".misc", "");
        if (passExists(passName)) {
            Passage passage = this.passages.get(findPass(passName));
            try {
                FileInputStream in = new FileInputStream(miscFile);
                try {
                    prop.load(in);
                    passage.onMessage = prop.getProperty("onMessage");
                    passage.offMessage = prop.getProperty("offMessage");
                    passage.reverseTimer = Boolean.parseBoolean(prop.getProperty("reverseTimer", "false"));
                    in.close();
                } catch (Throwable throwable) {
                    try {
                        in.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                    throw throwable;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[Secret Passage] Error while processing " + miscFile.getName());
            }
        } else {
            System.out.println("[Secret Passage] Misc file found for non-existent passage!");
            System.out.println("[Secret Passage] Please check on this file: " + miscFile.getName());
        }
    }

    public void saveMisc(Passage passage) {
        (new File(miscDir)).mkdirs();
        File miscFile = new File(miscDir + miscDir + File.separator + ".misc");
        try {
            FileOutputStream fos = new FileOutputStream(miscFile);
            try {
                prop.setProperty("onMessage", passage.onMessage);
                prop.setProperty("offMessage", passage.offMessage);
                prop.setProperty("reverseTimer", Boolean.toString(passage.reverseTimer));
                prop.store(fos, (String)null);
                fos.close();
            } catch (Throwable throwable) {
                try {
                    fos.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
                throw throwable;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[Secret Passage] Could not save to " + miscFile.getName());
        }
    }

    public String listPassages(String pName) {
        return this.passages.stream()
                .filter(p -> p.owner.equals(pName))
                .map(p -> p.name)
                .collect(Collectors.joining(" "));
    }

    public boolean canAccess(String passName, String player) {
        Passage passage = this.passages.get(findPass(passName));
        if (passage.nameList.isEmpty())
            return true;
        if (player.equals(passage.owner))
            return true;
        if (passage.listStyle.equals("white") && passage.nameList.contains(player.toLowerCase()))
            return true;
        return (passage.listStyle.equals("black") && !passage.nameList.contains(player.toLowerCase()));
    }

    public boolean addToList(String passName, String player) {
        Passage passage = this.passages.get(findPass(passName));
        if (passage.nameList.contains(player.toLowerCase()) || player.equals(passage.owner))
            return false;
        passage.nameList.add(player.toLowerCase());
        savePassage(passName);
        return true;
    }

    public boolean removeFromList(String passName, String player) {
        Passage passage = this.passages.get(findPass(passName));
        boolean removed = passage.nameList.remove(player);
        if (removed)
            savePassage(passName);
        return removed;
    }

    public String getAccessList(String passName) {
        Passage passage = this.passages.get(findPass(passName));
        return String.join(" ", (Iterable)passage.nameList);
    }

    @FunctionalInterface
    private static interface FileProcessor {
        void process(File param1File);
    }

    @FunctionalInterface
    private static interface FileWriterProcessor {
        void process(BufferedWriter param1BufferedWriter) throws IOException;
    }
}
