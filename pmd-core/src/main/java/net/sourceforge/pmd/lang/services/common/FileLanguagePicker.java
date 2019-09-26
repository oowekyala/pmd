/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.services.common;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

/** A file selector for a language. Every language must declare such a service. */
public interface FileLanguagePicker {

    boolean applies(Path file);


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
