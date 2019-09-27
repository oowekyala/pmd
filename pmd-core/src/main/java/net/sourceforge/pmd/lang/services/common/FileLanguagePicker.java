/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.services.common;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

/** A file selector for a language. Every language must declare such a service. */
public interface FileLanguagePicker {

    boolean applies(Path file);


    static FileLanguagePicker merged(List<? extends FileLanguagePicker> lst) {
        Objects.requireNonNull(lst, "Argument is null");
        if (lst.isEmpty()) {
            throw new IllegalArgumentException("Argument is empty");
        } else if (lst.size() == 1) {
            return lst.get(0);
        }

        return new FileLanguagePicker() {
            @Override
            public boolean applies(Path file) {
                return lst.stream().anyMatch(it -> it.applies(file));
            }

            @Override
            public String toString() {
                return lst.stream().map(Objects::toString).collect(Collectors.joining(", ", "Composed(", ")"));
            }
        };
    }


    static FileLanguagePicker merged(FileLanguagePicker first, FileLanguagePicker... others) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(others);

        if (others.length == 0) {
            return first;
        }

        List<FileLanguagePicker> lst = new ArrayList<>();
        lst.add(first);
        Collections.addAll(lst, others);
        return merged(lst);
    }


    static FileLanguagePicker ofExtensions(String ext1, String... others) {
        Set<String> exts = new HashSet<>();
        exts.add(ext1);
        Collections.addAll(exts, others);

        return new FileLanguagePicker() {
            @Override
            public boolean applies(Path file) {
                String fileName = file.getFileName().toString();
                String extension = FilenameUtils.getExtension(fileName);
                return exts.contains(extension);
            }

            @Override
            public String toString() {
                return "ExtensionPicker" + exts.toString();
            }
        };
    }

}
