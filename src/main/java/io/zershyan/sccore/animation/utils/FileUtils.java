package io.zershyan.sccore.animation.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtils {
    public static Set<Path> getAllFile(Path directory, Predicate<Path> filter) {
        try (Stream<Path> walk = Files.walk(directory)) {
            return walk.filter(Files::isRegularFile)
                    .filter(filter)
                    .collect(Collectors.toSet());
        } catch (Exception ignored) {
            return Collections.emptySet();
        }
    }

    public static void safeUnzip(String zipFile, String destDir) {
        Path destPath = Paths.get(destDir).toAbsolutePath();

        try (ZipFile zip = new ZipFile(zipFile)) {
            Files.createDirectories(destPath);
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path entryPath = destPath.resolve(entry.getName()).normalize();

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (InputStream in = zip.getInputStream(entry);
                         OutputStream out = Files.newOutputStream(entryPath, StandardOpenOption.CREATE)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
