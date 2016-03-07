package com.afrozaar.util.graphicsmagick;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MetaParser {

    public static Optional<Map<String, Object>> parseResult(String execute) {

        final String[] lines = execute.split("\n");

        Meta root = new Meta();

        Meta last = null;

        for (String line : lines) {
            Optional<Meta> entryOpt = parseEntry(line);
            if (entryOpt.isPresent()) {
                final Meta entry = entryOpt.get();
                if (last == null) {
                    root = last = entry;
                } else {
                    if (entry.depth == last.depth) {
                        entry.parent = last.parent;
                        last.parent.children.add(entry);
                    } else if (entry.depth > last.depth) {
                        entry.parent = last;
                        last.children.add(entry);
                    } else if (entry.depth < last.depth) {
                        entry.parent = findParent(entry.depth, last);
                        entry.parent.children.add(entry);
                    }
                    last = entry;
                }
            }
        }

        return Optional.of(ImmutableMap.of("data", root));
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

    static Optional<Meta> parseEntry(String line) {
        final Logger logger = LoggerFactory.getLogger(MetaParser.class);
        logger.info("Parsing meta from line: {}", line);
        if (Strings.isNullOrEmpty(line) || line.trim().isEmpty()) {
            logger.warn("Skipping empty line.");
            return Optional.empty();
        }
        try {
            final String[] split = line.split(":", 2);
            Meta newMeta = new Meta();
            newMeta.data = new AbstractMap.SimpleImmutableEntry<>(split[0].trim(), split[1]);
            newMeta.depth = indent(0, split[0]);
            return Optional.of(newMeta);
        } catch (Exception e) {
            logger.error("Skipping line: '{}'. Error ", line, e);
            return Optional.empty();
        }
    }

    static class Meta {
        Meta parent;
        Map.Entry data;
        int depth = 0;
        Set<Meta> children = Sets.newLinkedHashSet();

        public Meta() {
        }

        public Set<Meta> getChildren() {
            return children;
        }

        public Map.Entry getData() {
            return data;
        }

        public int getDepth() {
            return depth;
        }
    }
}
