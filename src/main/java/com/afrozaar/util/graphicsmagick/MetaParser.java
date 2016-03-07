package com.afrozaar.util.graphicsmagick;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class MetaParser {

    static final Logger LOG = LoggerFactory.getLogger(MetaParser.class);

    public static Optional<Map<String, Object>> parseResult(String execute) {

        final String[] lines = execute.split("\n");

        Meta root = new Meta();

        Meta last = null;

        for (final String line : lines) {
            if (isEmptyLine(line)) {
                LOG.warn("Empty line, skipping processing.");
                continue;
            }

            final String[] split = line.split(":", 2);
            final boolean isPossibleRogueDataLine = isPossibleRogueDataLine(split);

            if (Objects.nonNull(last) && (isNonKeyValueEntry(split) || isPossibleRogueDataLine)) {
                final String newValue = isPossibleRogueDataLine ? line.trim() : split[0].trim();
                LOG.debug("Updating last data key={} value={}", last.data.getKey(), newValue);
                last.data = new AbstractMap.SimpleImmutableEntry<>(last.data.getKey(), newValue);
            } else {
                final Meta entry = Meta.of(split);
                if (last == null) {
                    root = last = entry;
                } else {
                    entry.parent = findParentFromDepth(entry, last);
                    entry.parent.addChild(entry);

                    last = entry;
                }
            }
        }

        return Optional.of(ImmutableMap.of("data", root));
    }

    private static Meta findParentFromDepth(Meta entry, Meta last) {
        if (entry.depth == last.depth) {
            return last.parent;
        } else if (entry.depth > last.depth) {
            return last;
        } else if (entry.depth < last.depth) {
            return findParent(entry.depth, last);
        } else {
            throw new IllegalStateException();
        }
    }

    private static Meta findParent(int depth, Meta last) {
        if (last.depth == depth) {
            return last.parent;
        } else {
            return findParent(depth, last.parent);
        }
    }

    static int indent(int count, String input) {
        if (input.charAt(count) != ' ') {
            return count;
        } else {
            return indent(count + 1, input);
        }
    }

    private static int charCount(final int count, final char chr, final int index, final String input) {
        if (index >= input.length()) {
            return count;
        } else {
            int newCount = input.charAt(index) == chr ? count + 1 : count;
            return charCount(newCount, chr, index+1, input);
        }
    }

    private static boolean isEmptyLine(String line) {
        return Strings.isNullOrEmpty(line) || line.trim().isEmpty();
    }

    private static boolean isNonKeyValueEntry(String[] split) {
        return split.length < 2;
    }

    /**
     * A possible rogue line contains a ':' which is not a field separator, eg.: 'This is some random text that belongs to a previous entry: noted?'
     * Also, the indentation is broken. At this stage it is not certain whether it is a data entry issue onto the meta entry or not.
     * Currently it is hard to tell the conditions would indicate a rogue line, but a good guess is that anything NOT matching [\w\d\:\-] can indicate this.
     */
    private static boolean isPossibleRogueDataLine(String[] split) {

        //char[] rogueChars = {'.', '(', ')'};
        final String regex = "(\\w|\\d|\\s|\\-)+";
        return !split[0].matches(regex);

        //return split[0].matches("[^\\w\\d:\\-]+");

        //return charCount(0, '.', 0, split[0].trim()) > 0;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class Meta {
        Meta parent;
        Map.Entry data;
        int depth = 0;
        Set<Meta> children = Sets.newLinkedHashSet();

        Meta() {
        }

        Meta(String key, String value) {
            this.data = new AbstractMap.SimpleImmutableEntry<>(key, value);
        }

        static Meta of(String key, String value) {
            return new Meta(key, value);
        }

        static Meta of(String[] split) {
            Meta newMeta = Meta.of(split[0].trim(), split[1].trim());
            newMeta.depth = indent(0, split[0]);
            return newMeta;
        }

        Meta addChild(Meta child) {
            children.add(child);
            return this;
        }

        public Set<Meta> getChildren() {
            return children;
        }

        public Map.Entry getData() {
            return data;
        }

        @JsonIgnore
        public int getDepth() {
            return depth;
        }
    }
}
