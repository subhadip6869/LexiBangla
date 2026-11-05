package org.originsd.lexibangla.service;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.originsd.lexibangla.model.DictionaryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing clipboard operations with Bengali Unicode preservation.
 */
public class ClipboardService {

    private static final Logger log = LoggerFactory.getLogger(ClipboardService.class);

    public boolean copyText(String text) {
        if (text == null || text.isBlank()) return false;
        try {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(text.trim());
            clipboard.setContent(content);
            log.info("Copied text to clipboard (length: {})", text.length());
            return true;
        } catch (Exception e) {
            log.error("Failed to copy text to clipboard", e);
            return false;
        }
    }

    public boolean copyWord(DictionaryEntry entry) {
        return entry != null && copyText(entry.word());
    }

    public boolean copyMeaning(DictionaryEntry entry) {
        return entry != null && copyText(entry.meaning());
    }

    public boolean copyFullEntry(DictionaryEntry entry) {
        return entry != null && copyText(entry.toFullEntryString());
    }
}
