package com.EvgenWarGold.GregTechNightmare;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Quests {

    private static final String QUEST_ROOT = "GregtechNightmar-mSF4xIYlR1y1kG8JJeGY-Q==";
    private static final String INSTALL_MARKER = ".gregtechnightmare-quests-installed";
    private static boolean registered;

    public static void init() {
        if (registered) {
            return;
        }

        if (GregTechNightmare.CONFIG_DIR == null) return;

        File configDir = new File(GregTechNightmare.CONFIG_DIR, "betterquesting/DefaultQuests");

        if (!configDir.exists()) return;

        File questsDir = new File(configDir, "Quests");
        File questLineDir = new File(configDir, "QuestLines");
        Path markerPath = new File(configDir, INSTALL_MARKER).toPath();

        if (Files.exists(markerPath)) {
            registered = true;
            return;
        }

        if (new File(questsDir, QUEST_ROOT).exists() || new File(questLineDir, QUEST_ROOT).exists()) {
            createMarker(markerPath);
            registered = true;
            return;
        }

        try {
            URI jarFileUri = GregTechNightmare.RESOURCE_URL.toURI();
            String jarPath = "jar:" + jarFileUri.getRawSchemeSpecificPart()
                .split("!")[0];

            try (FileSystem fs = FileSystems.newFileSystem(URI.create(jarPath), Collections.emptyMap())) {
                Path sourceDir = fs.getPath("/assets/gregtechnightmare/Quests");

                if (Files.exists(sourceDir) && Files.isDirectory(sourceDir)) {
                    installQuestFiles(sourceDir, questsDir, questLineDir, configDir);
                    createMarker(markerPath);
                }
            }
        } catch (URISyntaxException | IOException ignored) {}

        registered = true;
    }

    private static void installQuestFiles(Path sourceDir, File questsDir, File questLineDir, File configDir)
        throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path entry : stream) {
                String name = entry.getFileName()
                    .toString();

                if (name.equals("Quests") && Files.isDirectory(entry)) {
                    copyDirectoryRecursive(entry, questsDir);
                } else if (name.equals("QuestLines") && Files.isDirectory(entry)) {
                    copyDirectoryRecursive(entry, questLineDir);
                } else if (name.equals("QuestLinesOrder.txt") && Files.isRegularFile(entry)) {
                    mergeQuestLinesOrderFile(entry, configDir);
                }
            }
        }
    }

    private static void createMarker(Path markerPath) {
        try {
            Files.write(
                markerPath,
                Collections.singletonList("installed"),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignored) {}
    }

    private static void mergeQuestLinesOrderFile(Path sourceFile, File configDir) throws IOException {
        File targetFile = new File(configDir, "QuestLinesOrder.txt");
        List<String> jarLines = Files.readAllLines(sourceFile);

        if (!targetFile.exists()) {
            Files.write(targetFile.toPath(), jarLines, StandardOpenOption.CREATE_NEW);
            return;
        }

        List<String> existingLines = Files.readAllLines(targetFile.toPath());
        List<String> mergedLines = new ArrayList<>(existingLines);
        boolean changed = false;

        for (String line : jarLines) {
            if (!mergedLines.contains(line)) {
                mergedLines.add(line);
                changed = true;
            }
        }

        if (changed) {
            Files.write(targetFile.toPath(), mergedLines, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private static void copyDirectoryRecursive(Path sourceDir, File targetDir) throws IOException {
        try (java.util.stream.Stream<Path> walk = Files.walk(sourceDir)) {
            for (java.util.Iterator<Path> iterator = walk.iterator(); iterator.hasNext();) {
                Path sourcePath = iterator.next();
                Path relativePath = sourceDir.relativize(sourcePath);
                File targetPath = new File(targetDir, relativePath.toString());

                if (Files.isDirectory(sourcePath)) {
                    targetPath.mkdirs();
                } else if (Files.isRegularFile(sourcePath) && !targetPath.exists()) {
                    File parent = targetPath.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }
                    Files.copy(sourcePath, targetPath.toPath());
                }
            }
        }
    }
}
