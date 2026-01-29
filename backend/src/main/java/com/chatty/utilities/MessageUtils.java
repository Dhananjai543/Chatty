package com.chatty.utilities;

import java.util.regex.Pattern;

public final class MessageUtils {

    private static final int MAX_MESSAGE_LENGTH = 5000;
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[\\w-]+(\\.[\\w-]+)+(/[\\w-./?%&=]*)?)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\w]+)");

    private MessageUtils() {
        // Utility class - prevent instantiation
    }

    public static String sanitizeMessage(String message) {
        if (message == null) return "";
        
        // Trim whitespace
        String sanitized = message.trim();
        
        // Remove multiple consecutive spaces
        sanitized = sanitized.replaceAll("\\s+", " ");
        
        // Truncate if too long
        if (sanitized.length() > MAX_MESSAGE_LENGTH) {
            sanitized = sanitized.substring(0, MAX_MESSAGE_LENGTH);
        }
        
        return sanitized;
    }

    public static boolean isValidMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        return message.trim().length() <= MAX_MESSAGE_LENGTH;
    }

    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    public static boolean containsUrl(String message) {
        if (message == null) return false;
        return URL_PATTERN.matcher(message).find();
    }

    public static String[] extractMentions(String message) {
        if (message == null) return new String[0];
        
        java.util.List<String> mentions = new java.util.ArrayList<>();
        java.util.regex.Matcher matcher = MENTION_PATTERN.matcher(message);
        
        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }
        
        return mentions.toArray(new String[0]);
    }

    public static String getPreviewText(String message, int maxLength) {
        if (message == null) return "";
        if (message.length() <= maxLength) return message;
        return message.substring(0, maxLength - 3) + "...";
    }

    public static String generatePrivateChatId(String userId1, String userId2) {
        // Generate a consistent chat ID regardless of who initiated
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        }
        return userId2 + "_" + userId1;
    }
}
