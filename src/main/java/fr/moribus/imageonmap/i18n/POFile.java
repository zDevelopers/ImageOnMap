/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2021)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2021)
 *
 * Parts of this file are adapted from QuartzLib (https://github.com/zDevelopers/QuartzLib),
 * Copyright or © or Copr. ZDevelopers (2015 – 2021), governed by the CeCILL-B license.
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */

package fr.moribus.imageonmap.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A gettext {@code .po} translations file.
 */
public class POFile {
    private static final String NO_CONTEXT_KEY = UUID.randomUUID().toString();

    private final Map<String, Map<String, Translation>> translations = new HashMap<>();
    private BufferedReader rawReader;
    private int pluralCount = 2;
    private String pluralFormScript = "";
    private PluralForms pluralForms = null;

    public POFile(Reader reader) {
        this.rawReader = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * Parses the file.
     *
     * @throws CannotParsePOException if the file is malformed.
     */
    public void parse() throws CannotParsePOException {
        if (rawReader == null) {
            return;
        }

        try (BufferedReader reader = rawReader) {
            String line;
            int lineNumber = 0;

            // For each section, we first collect the keys defined (they may expand on several lines);
            // then we extract and save them when we hit a blank line, acting as a separator between
            // translations.
            final Map<String, String> tokens = new HashMap<>();
            String lastToken = null;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (!line.isEmpty()) {
                    if (line.startsWith("#")) {
                        continue;
                    } else if (line.startsWith("\"")) {
                        // Continued values of tokens on another line
                        if (lastToken == null) {
                            throw new CannotParsePOException("Unnamed token value", lineNumber);
                        }
                        tokens.put(lastToken, tokens.get(lastToken) + extractTokenValue(line));
                    } else {
                        // Beginning of a new token
                        final String[] lineParts = line.split(" ", 2);
                        if (lineParts.length < 2) {
                            throw new CannotParsePOException("Malformed token line", lineNumber);
                        }
                        tokens.put(lineParts[0], extractTokenValue(lineParts[1]));
                        lastToken = lineParts[0];
                    }
                } else {
                    if (!tokens.isEmpty()) {
                        analyseEntry(tokens);
                    }
                    tokens.clear();
                    lastToken = null;
                }
            }

            if (!tokens.isEmpty()) {
                analyseEntry(tokens);
            }

            pluralForms = new PluralForms(pluralCount, pluralFormScript);
        } catch (IOException e) {
            throw new CannotParsePOException("An IO exception occurred while parsing the file", e);
        }

        rawReader = null;
    }

    private String extractTokenValue(String raw) {
        final StringBuilder extracted = new StringBuilder();
        boolean inString = false;

        for (int i = 0; i < raw.length(); i++) {
            final char character = raw.charAt(i);

            if (character == '"') {
                if (i == 0 || raw.charAt(i - 1) != '\\') {
                    inString = !inString;
                    if (!inString) {
                        break;
                    }
                } else {
                    extracted.append('"');
                }
            } else if (inString && !(character == '\\' && i != raw.length() - 1 && raw.charAt(i + 1) == '"')) {
                extracted.append(character);
            }
        }

        return extracted.toString();
    }

    private void analyseEntry(Map<String, String> tokens) {
        if (!tokens.containsKey("msgid")) {
            return;
        }

        final String msgid = tokens.get("msgid");

        if (!msgid.isEmpty()) {
            final String msgctxt = tokens.get("msgctxt");

            // msgstr can be in two different formats:
            // - msgstr: then there is only one translation;
            // - msgstr[i]: then multiple translations are available (for plurals).
            final List<String> msgstr;
            if (tokens.containsKey("msgstr")) {
                msgstr = Collections.singletonList(tokens.get("msgstr"));
            } else {
                msgstr = new ArrayList<>();
                for (int i = 0; ; i++) {
                    final String tokenValue = tokens.get("msgstr[" + i + "]");
                    if (tokenValue == null) {
                        break;
                    }
                    msgstr.add(tokenValue);
                }
            }

            // No translation available, skipped.
            if (msgstr.isEmpty() || msgstr.get(0).trim().isEmpty()) {
                return;
            }

            translations.computeIfAbsent(msgctxt != null ? msgctxt : NO_CONTEXT_KEY, key -> new HashMap<>())
                    .put(msgid, new Translation(msgstr));
        } else {
            // Metadata
            final String rawMetadata = tokens.get("msgstr");
            if (rawMetadata == null) {
                return;
            }

            // Ensures both interpreted and written line breaks are interpreted
            for (String meta : rawMetadata.split("(\\\\n)|\n")) {
                final String[] metaParts = meta.split(":");
                if (metaParts.length < 2 || !metaParts[0].trim().equalsIgnoreCase("plural-forms")) {
                    continue;
                }

                final String[] parts = metaParts[1].trim().split(";", 2);
                if (parts.length < 2) {
                    continue;
                }

                try {
                    pluralCount = Integer.parseInt(parts[0].split("=")[1].trim());
                    pluralFormScript = parts[1].trim();
                    if (pluralFormScript.endsWith(";")) {
                        pluralFormScript = pluralFormScript.substring(0, pluralFormScript.length() - 1);
                    }
                    // Converts “plural=<script>” to “<script>”
                    if (pluralFormScript.startsWith("plural")) {
                        pluralFormScript = pluralFormScript.split("=", 2)[1];
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                    // Invalid plural forms: English rules will be used.
                }
            }
        }
    }

    /**
     * Gets the translation of a message.
     *
     * @param context   The context of the message (can be {@code null}).
     * @param messageId The message.
     * @return The translation, or {@code null} if this file does not translate this message.
     */
    public Translation getTranslation(String context, String messageId) {
        final Map<String, Translation> contextMap = translations.get(context != null ? context : NO_CONTEXT_KEY);
        return contextMap != null ? contextMap.get(messageId) : null;
    }

    /**
     * Computes the index of the plural form to use for the given count.
     *
     * @param count The count.
     * @return The index of the plural form.
     */
    public int computePluralForm(long count) {
        if (pluralForms == null) {
            throw new IllegalStateException("Cannot compute plural form: the file is not parsed. Call parse() first.");
        }
        return pluralForms.computePluralForm(count);
    }

    /**
     * A translated message, with its plural forms.
     */
    public static final class Translation {
        private final List<String> translations;

        Translation(List<String> translations) {
            this.translations = translations;
        }

        public List<String> getTranslations() {
            return translations;
        }
    }

    /**
     * Thrown when a {@code .po} file cannot be parsed.
     */
    public static class CannotParsePOException extends RuntimeException {
        public CannotParsePOException(String message, Integer line) {
            super(message + " [" + line + "]");
        }

        public CannotParsePOException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
