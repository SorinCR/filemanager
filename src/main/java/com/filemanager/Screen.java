package com.filemanager;

import java.util.ArrayList;
import java.util.Map;

public class Screen {

        public static final String ANSI_RESET = "\u001B[0m";
        public static final String ANSI_YELLOW = "\u001B[33m";
        public static final String ANSI_RED = "\u001B[31m";
        public static final String ANSI_CYAN = "\u001B[36m";
        public static final String ANSI_WHITE = "\u001B[37m";
        public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
        public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";

        private static int maxItems = 10;
        private static int maxLength = 35;

        private static int padding = 45;

        public int getMaxLength() {
                return maxLength;
        }

        public void changeMaxLength(int length) {
                maxLength = length;
        }

        public int getMaxItems() {
                return maxItems;
        }

        public void changeMaxItems(int items) {
                maxItems = items;
        }

        public void refreshScreen(ArrayList<Content> menuItems, int option, String path, boolean openContextMenu,
                        int contextOption, Map<String, String> contextMenu, boolean controlLock) {
                // god, what have I done
                // for (int clear = 0; clear < 1000; clear++) {
                // System.out.println("\b");
                // }

                StringBuilder screenContent = new StringBuilder();

                // System.out.println(option);

                int totalPages = menuItems.size() / maxItems;

                String menuFormat = ANSI_RESET + "│ %-2s %-" + String.format("%s", maxLength + 5) + "s" + ANSI_RESET
                                + "   %-15s    %-10s    │%n";

                String contextMenuFormat = ANSI_RESET + "│%-20s %-7s" + ANSI_RESET + "│";

                // System.out.format(ANSI_RESET +
                // "┌─────────────────────────────────────────────────────────┐%n");
                screenContent.append(String.format(ANSI_RESET + "┌%s┐%n", "─".repeat(maxLength + padding)));
                screenContent.append(String.format(menuFormat, " >", path.length() > maxLength
                                ? path.substring(0, maxLength) + "..."
                                : path, "Type", "Size"));
                screenContent.append(String.format("├%s┤%n", "─".repeat(maxLength + padding)));
                for (int i = option / maxItems * maxItems; i < option / maxItems * maxItems
                                + Math.min(maxItems, menuItems.size() - option / maxItems * maxItems); i++) {

                        String itemName = menuItems.get(i).getName().length() > maxLength
                                        ? menuItems.get(i).getName().substring(0, maxLength) + "..."
                                        : menuItems.get(i).getName();

                        String fileType = "Directory";
                        String fileSize = "";
                        switch (menuItems.get(i).getClass().getName()) {
                                case "com.filemanager.Directory":
                                        fileType = "Directory";
                                        break;
                                case "com.filemanager.RegularFile":
                                case "com.filemanager.AudioFile":
                                case "com.filemanager.Document":
                                        fileType = ((RegularFile) menuItems.get(i)).getType().toUpperCase() + " File";

                                        fileType = fileType.length() > 15
                                                        ? fileType.substring(0, 12) + "..."
                                                        : fileType;

                                        fileSize = ((RegularFile) menuItems.get(i)).getReadableSize();
                                        break;
                        }

                        if (option >= maxItems
                                        && i == option / maxItems * maxItems) {
                                screenContent.append(String.format(menuFormat, " ", "...", "", ""));
                        }
                        if (i == option) {
                                screenContent.append(String.format(menuFormat, ANSI_WHITE + ">>", itemName, fileType,
                                                fileSize));
                                // context menu
                                if (openContextMenu) {
                                        screenContent.append(String.format(menuFormat,
                                                        " ",
                                                        String.format("┌%s┐", "─".repeat(23)),
                                                        "", ""));
                                        for (Map.Entry<String, String> item : contextMenu.entrySet()) {
                                                screenContent.append(String.format(menuFormat, "  ",
                                                                String.format(contextMenuFormat,
                                                                                contextMenu.keySet()
                                                                                                .toArray()[contextOption] == item
                                                                                                                .getKey() ? ANSI_CYAN_BACKGROUND
                                                                                                                                + item.getKey()
                                                                                                                                : ANSI_BLACK_BACKGROUND
                                                                                                                                                + item.getKey(),
                                                                                item.getValue()),
                                                                "", "                       ")); // this is called abuse
                                                if (item.getKey() != contextMenu.keySet().toArray()[contextMenu.size()
                                                                - 1])
                                                        screenContent.append(String.format(menuFormat, "  ",
                                                                        String.format("├%s┤", "─".repeat(23)), "", ""));
                                        }
                                        screenContent.append(String.format(menuFormat, "  ",
                                                        String.format("└%s┘", "─".repeat(23)), "", ""));
                                }

                        } else {
                                screenContent.append(String.format(menuFormat, ANSI_RESET + ANSI_CYAN + "  ",
                                                itemName, fileType, fileSize));
                        }
                        if (option / maxItems != totalPages
                                        && i == option / maxItems * maxItems
                                                        + Math.min(maxItems,
                                                                        menuItems.size() - option / maxItems * maxItems)
                                                        - 1
                                        && menuItems.size() > maxItems && option / maxItems * maxItems
                                                        + Math.min(maxItems,
                                                                        menuItems.size() - option / maxItems
                                                                                        * maxItems) != menuItems
                                                                                                        .size()) {
                                screenContent.append(String.format(menuFormat, " ", "...", "", ""));
                        }
                }
                screenContent.append(String.format(menuFormat, ANSI_YELLOW + "  ", "", "", ""));
                screenContent.append(String.format(menuFormat, ANSI_WHITE + "  ",
                                option / maxItems * maxItems
                                                + Math.min(maxItems, menuItems.size() - option / maxItems * maxItems)
                                                + " / " + menuItems.size() + " items",
                                "", ""));
                screenContent.append(String.format(ANSI_RESET + "├%s┤%n", "─".repeat(maxLength + padding)));
                // System.out.println(ANSI_RED
                // + "PRO TIP: Don't complain about user experience, complain about the beauty
                // of this menu instead.");
                screenContent.append(
                                String.format(menuFormat, ANSI_YELLOW + "  ", "Up/Down          Select Item", "", ""));
                screenContent.append(
                                String.format(menuFormat, ANSI_YELLOW + "  ",
                                                "Right/Left       Open/Close Context Menu", "", ""));
                screenContent.append(
                                String.format(menuFormat, ANSI_YELLOW + "  ", "+/-              Resize Window", "",
                                                ""));
                screenContent.append(
                                String.format(menuFormat, ANSI_YELLOW + "  ", "NUM 6/NUM 9      Change Content Size",
                                                "", ""));
                screenContent.append(
                                String.format(menuFormat, ANSI_RED + "  ",
                                                "NUM LOCK         " + (controlLock ? "Unlock" : "Lock") + " Controls",
                                                "", ""));
                screenContent.append(String.format(ANSI_RESET + "└%s┘%n", "─".repeat(maxLength + padding)));

                System.out.print("\033[H\033[2J");
                System.out.flush();
                System.out.print(screenContent);
        }
}
