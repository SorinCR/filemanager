package com.filemanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import org.apache.commons.io.FilenameUtils;
import java.awt.Desktop;

/**
 * Hello world!
 *
 */
public class App implements NativeKeyListener {

    private static String configPath = "config.txt";

    private static String currentDirectory;

    private static int option = 0;
    private static int contextOption = 0;
    private static boolean controlLock = false;
    private static boolean contextMenuShow = false;
    private static ArrayList<Content> menu = new ArrayList<Content>();
    // for managing the main menu items
    private static Map<String, String> contextMenu = new LinkedHashMap<String, String>();
    // for managing the context menu items
    private static Screen screen = new Screen();
    private static Desktop desktop = Desktop.getDesktop();

    private static Scanner scanner = new Scanner(System.in);

    public static int getIndex(char searchChar) {
        for (int i = 0; i < menu.size(); i++) {
            String currentString = menu.get(i).getName();
            if (currentString != null && currentString.length() > 0
                    && Character.toLowerCase(currentString.charAt(0)) == Character.toLowerCase(searchChar)
                    && option != i && option < i) {
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args) throws IOException {

        // try {
        // System.setOut(new PrintStream(System.out, true, "UTF-8"));
        // } catch (UnsupportedEncodingException e) {
        // throw new InternalError("VM does not support mandatory encoding UTF-8");
        // }

        // Get the logger for "com.github.kwhat.jnativehook" and set the level to off.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        logger.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(new App());

        // menu.add("New folder");

        currentDirectory = args.length == 1 ? args[0] : "";
        Path path = Paths.get(currentDirectory);

        if (!Files.exists(path)) {
            path = Paths.get("");
        }
        currentDirectory = path.toAbsolutePath().toString();

        Directory backDir = new Directory("..");
        menu.add(backDir);

        Files.list(path).forEach(file -> {
            // String fileName = file.getFileName().toString();
            if (Files.isDirectory(file)) {
                Directory dir = new Directory(file.getFileName().toString());
                menu.add(dir);
            } else {
                if (FilenameUtils.getExtension(file.getFileName().toString()).equalsIgnoreCase("wav")) {
                    AudioFile aFile;
                    try {
                        aFile = new AudioFile(file.getFileName().toString(), "Audio",
                                file.toFile().length(),
                                String.format("%s\\%s", currentDirectory, file.getFileName().toString()));
                        menu.add(aFile);
                    } catch (UnsupportedAudioFileException | IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (LineUnavailableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (FilenameUtils.getExtension(file.getFileName().toString())
                        .equalsIgnoreCase("txt")) {
                    Document docFile = new Document(file.getFileName().toString(), "Document",
                            file.toFile().length(), "");
                    try {
                        docFile = new Document(file.getFileName().toString(), "Document",
                                file.toFile().length(),
                                Files.readString(Paths
                                        .get(String.format("%s/%s", currentDirectory, file.getFileName().toString()))));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    menu.add(docFile);
                } else {
                    RegularFile rFile = new RegularFile(file.getFileName().toString(),
                            FilenameUtils.getExtension(file.getFileName().toString()),
                            file.toFile().length());
                    menu.add(rFile);
                }
            }
        });

        // contextMenu.put("Open", "ENTER");
        // contextMenu.put("Rename", "NUM 1");
        // contextMenu.put("Delete", "DELETE");

        // menu.add(args[0]);

        initConfig(configPath);

        screen.refreshScreen(menu, option, currentDirectory, false, 0, contextMenu, controlLock);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // System.out.println(NativeKeyEvent.getKeyText(e.getKeyCode()));
        if (!controlLock) {
            switch (NativeKeyEvent.getKeyText(e.getKeyCode())) {
                case "Up":
                    if (contextMenuShow) {
                        if (contextOption > 0)
                            screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, --contextOption,
                                    contextMenu, controlLock);
                    } else if (option > 0)
                        screen.refreshScreen(menu, --option, currentDirectory, false, 0, contextMenu, controlLock);
                    break;
                case "Down":
                    if (contextMenuShow) {
                        if (contextOption < contextMenu.size() - 1)
                            screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, ++contextOption,
                                    contextMenu, controlLock);
                    } else if (option < menu.size() - 1)
                        screen.refreshScreen(menu, ++option, currentDirectory, false, 0, contextMenu, controlLock);
                    break;
                case "NumPad Add":
                    if (screen.getMaxLength() < 150) {
                        screen.changeMaxLength(screen.getMaxLength() + 1);
                        screen.refreshScreen(menu, option, currentDirectory, false, 0, contextMenu, controlLock);
                        System.out.format("Window length: %d", screen.getMaxLength());
                    }
                    break;
                case "NumPad Subtract":
                    if (screen.getMaxLength() > 35) {
                        screen.changeMaxLength(screen.getMaxLength() - 1);
                        screen.refreshScreen(menu, option, currentDirectory, false, 0, contextMenu, controlLock);
                        System.out.format("Window length: %d", screen.getMaxLength());
                    }
                    break;
                case "NumPad 6":
                    if (screen.getMaxItems() < 100) {
                        screen.changeMaxItems(screen.getMaxItems() + 1);
                        screen.refreshScreen(menu, option, currentDirectory, false, 0, contextMenu, controlLock);
                        System.out.format("Max items: %d", screen.getMaxItems());
                    }
                    break;
                case "NumPad 9":
                    if (screen.getMaxItems() > 2) {
                        screen.changeMaxItems(screen.getMaxItems() - 1);
                        screen.refreshScreen(menu, option, currentDirectory, false, 0, contextMenu, controlLock);
                        System.out.format("Max items: %d", screen.getMaxItems());
                    }
                    break;
                case "Right":
                    contextMenuShow = true;
                    contextOption = 0;

                    switch (menu.get(option).getClass().getName()) {
                        case "com.filemanager.Directory":
                            contextMenu.clear();
                            contextMenu.put("Open", "ENTER");
                            if (menu.get(option).getName() != "..") {
                                contextMenu.put("Rename", "NUM 1");
                                contextMenu.put("Delete", "DELETE");
                            }
                            break;
                        case "com.filemanager.RegularFile":
                            contextMenu.clear();
                            contextMenu.put("Open", "ENTER");
                            contextMenu.put("Rename", "NUM 1");
                            contextMenu.put("Delete", "DELETE");
                            break;
                        case "com.filemanager.AudioFile":
                            contextMenu.clear();
                            if (((AudioFile) menu.get(option)).getStatus().equals("Stopped"))
                                contextMenu.put("Play", "ENTER");
                            else if (((AudioFile) menu.get(option)).getStatus().equals("Paused")) {
                                contextMenu.put("Resume", "ENTER");
                                contextMenu.put("Stop", "");
                            } else {
                                contextMenu.put("Pause", "ENTER");
                                contextMenu.put("Stop", "");
                            }
                            contextMenu.put("Rename", "NUM 1");
                            contextMenu.put("Delete", "DELETE");
                            break;
                        case "com.filemanager.Document":
                            contextMenu.clear();
                            contextMenu.put("Read", "ENTER");
                            contextMenu.put("Rename", "NUM 1");
                            contextMenu.put("Delete", "DELETE");
                            break;
                    }

                    screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                            contextMenu, controlLock);
                    break;
                case "Left":
                    contextMenuShow = false;
                    contextOption = 0;
                    screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                            contextMenu, controlLock);
                    break;
                case "Enter":
                    if (!contextMenuShow) {
                        switch (menu.get(option).getClass().getName()) {
                            case "com.filemanager.Directory":
                                Path path = null;

                                if (menu.get(option).getName() == "..") {
                                    path = Paths.get(currentDirectory).getParent();
                                    currentDirectory = path.toAbsolutePath().toString();
                                } else {
                                    currentDirectory += "\\" + menu.get(option).getName();
                                    path = Paths.get(currentDirectory);
                                }

                                menu.clear();

                                Directory backDir = new Directory("..");
                                menu.add(backDir);

                                // if (!Files.exists(path))
                                // path = Paths.get("");
                                currentDirectory = path.toAbsolutePath().toString();

                                try {
                                    Files.list(path).forEach(file -> {
                                        if (Files.isDirectory(file)) {
                                            Directory dir = new Directory(file.getFileName().toString());
                                            menu.add(dir);
                                        } else {
                                            if (FilenameUtils.getExtension(file.getFileName().toString())
                                                    .equalsIgnoreCase("wav")) {
                                                AudioFile aFile;
                                                try {
                                                    aFile = new AudioFile(file.getFileName().toString(), "Audio",
                                                            file.toFile().length(),
                                                            String.format("%s/%s", currentDirectory,
                                                                    file.getFileName().toString()));
                                                    menu.add(aFile);
                                                } catch (UnsupportedAudioFileException | IOException er) {
                                                    // TODO Auto-generated catch block
                                                    er.printStackTrace();
                                                } catch (LineUnavailableException e1) {
                                                    // TODO Auto-generated catch block
                                                    e1.printStackTrace();
                                                }
                                            } else if (FilenameUtils.getExtension(file.getFileName().toString())
                                                    .equalsIgnoreCase("txt")) {
                                                Document docFile = new Document(file.getFileName().toString(),
                                                        "Document",
                                                        file.toFile().length(), "");
                                                try {
                                                    docFile = new Document(file.getFileName().toString(), "Document",
                                                            file.toFile().length(),
                                                            Files.readString(Paths
                                                                    .get(String.format("%s/%s", currentDirectory,
                                                                            file.getFileName().toString()))));
                                                } catch (IOException er) {
                                                    // TODO Auto-generated catch block
                                                    er.printStackTrace();
                                                }
                                                menu.add(docFile);
                                            } else {
                                                RegularFile rFile = new RegularFile(file.getFileName().toString(),
                                                        FilenameUtils.getExtension(file.getFileName().toString()),
                                                        file.toFile().length());
                                                menu.add(rFile);
                                            }
                                        }
                                    });
                                    option = 0;
                                    screen.refreshScreen(menu, option, currentDirectory, false, 0, contextMenu,
                                            controlLock);
                                } catch (IOException e1) {
                                    path = Paths.get(currentDirectory).getParent();
                                    currentDirectory = path.toAbsolutePath().toString();
                                    // here we should cry
                                    e1.printStackTrace();
                                }
                                break;
                            case "com.filemanager.Document":
                                ((Document) menu.get(option)).read();
                                break;
                            case "com.filemanager.AudioFile":
                                if (((AudioFile) menu.get(option)).getStatus().equals("Stopped"))
                                    try {
                                        ((AudioFile) menu.get(option)).play();
                                    } catch (AlreadyPlaying e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                else if (((AudioFile) menu.get(option)).getStatus().equals("Paused")) {
                                    try {
                                        ((AudioFile) menu.get(option)).resume();
                                    } catch (UnsupportedAudioFileException | IOException
                                            | LineUnavailableException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    } catch (NotPaused e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    } catch (AlreadyPlaying e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                } else {
                                    try {
                                        ((AudioFile) menu.get(option)).pause();
                                    } catch (AlreadyPaused e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                }
                            case "com.filemanager.RegularFile":
                                File file = new File(
                                        String.format("%s/%s", currentDirectory, menu.get(option).getName()));
                                try {
                                    desktop.open(file);
                                } catch (IOException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                                // System.out.println("Opening " + currentDirectory + "\\" +
                                // menu.get(option).getName());
                                break;
                        }
                    } else {
                        switch (menu.get(option).getClass().getName()) {
                            case "com.filemanager.Directory":
                                if (contextMenu.keySet().toArray()[contextOption].equals("Open")) {
                                    Path path = null;

                                    if (menu.get(option).getName() == "..") {
                                        path = Paths.get(currentDirectory).getParent();
                                        currentDirectory = path.toAbsolutePath().toString();
                                    } else {
                                        currentDirectory += "\\" + menu.get(option).getName();
                                        path = Paths.get(currentDirectory);
                                    }

                                    menu.clear();

                                    Directory backDir = new Directory("..");
                                    menu.add(backDir);

                                    // if (!Files.exists(path))
                                    // path = Paths.get("");
                                    currentDirectory = path.toAbsolutePath().toString();

                                    try {
                                        Files.list(path).forEach(file -> {
                                            if (Files.isDirectory(file)) {
                                                Directory dir = new Directory(file.getFileName().toString());
                                                menu.add(dir);
                                            } else {
                                                if (FilenameUtils.getExtension(file.getFileName().toString())
                                                        .equalsIgnoreCase("wav")) {
                                                    AudioFile aFile;
                                                    try {
                                                        aFile = new AudioFile(file.getFileName().toString(), "Audio",
                                                                file.toFile().length(),
                                                                String.format("%s/%s", currentDirectory,
                                                                        file.getFileName().toString()));
                                                        menu.add(aFile);
                                                    } catch (UnsupportedAudioFileException | IOException er) {
                                                        // TODO Auto-generated catch block
                                                        er.printStackTrace();
                                                    } catch (LineUnavailableException e1) {
                                                        // TODO Auto-generated catch block
                                                        e1.printStackTrace();
                                                    }
                                                } else if (FilenameUtils.getExtension(file.getFileName().toString())
                                                        .equalsIgnoreCase("txt")) {
                                                    Document docFile = new Document(file.getFileName().toString(),
                                                            "Document",
                                                            file.toFile().length(), "");
                                                    try {
                                                        docFile = new Document(file.getFileName().toString(),
                                                                "Document",
                                                                file.toFile().length(),
                                                                Files.readString(Paths
                                                                        .get(String.format("%s/%s", currentDirectory,
                                                                                file.getFileName().toString()))));
                                                    } catch (IOException er) {
                                                        // TODO Auto-generated catch block
                                                        er.printStackTrace();
                                                    }
                                                    menu.add(docFile);
                                                } else {
                                                    RegularFile rFile = new RegularFile(file.getFileName().toString(),
                                                            FilenameUtils.getExtension(file.getFileName().toString()),
                                                            file.toFile().length());
                                                    menu.add(rFile);
                                                }
                                            }
                                        });
                                        option = 0;
                                        screen.refreshScreen(menu, option, currentDirectory, false, 0, contextMenu,
                                                controlLock);
                                    } catch (IOException e1) {
                                        path = Paths.get(currentDirectory).getParent();
                                        currentDirectory = path.toAbsolutePath().toString();
                                        // here we should cry
                                        e1.printStackTrace();
                                    }
                                    contextMenuShow = false;
                                }
                                break;
                            case "com.filemanager.AudioFile":
                                if (contextMenu.keySet().toArray()[contextOption].equals("Play")) {
                                    try {
                                        ((AudioFile) menu.get(option)).play();
                                    } catch (AlreadyPlaying e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                    contextMenu.clear();
                                    contextMenu.put("Pause", "ENTER");
                                    contextMenu.put("Stop", "");
                                    contextMenu.put("Rename", "NUM 1");
                                    contextMenu.put("Delete", "DELETE");
                                    screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                                            contextMenu, controlLock);
                                    System.out.println("Playing " + menu.get(option).getName());
                                } else if (contextMenu.keySet().toArray()[contextOption].equals("Pause")) {
                                    try {
                                        ((AudioFile) menu.get(option)).pause();
                                    } catch (AlreadyPaused e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                    contextMenu.clear();
                                    contextMenu.put("Resume", "ENTER");
                                    contextMenu.put("Stop", "");
                                    contextMenu.put("Rename", "NUM 1");
                                    contextMenu.put("Delete", "DELETE");
                                    screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                                            contextMenu, controlLock);
                                    System.out.println("Music paused");
                                } else if (contextMenu.keySet().toArray()[contextOption].equals("Resume")) {
                                    try {
                                        ((AudioFile) menu.get(option)).resume();
                                        contextMenu.clear();
                                        contextMenu.put("Pause", "ENTER");
                                        contextMenu.put("Stop", "");
                                        contextMenu.put("Rename", "NUM 1");
                                        contextMenu.put("Delete", "DELETE");
                                        screen.refreshScreen(menu, option, currentDirectory, contextMenuShow,
                                                contextOption,
                                                contextMenu, controlLock);
                                        System.out.println("Music resumed");
                                    } catch (UnsupportedAudioFileException | IOException
                                            | LineUnavailableException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    } catch (NotPaused e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    } catch (AlreadyPlaying e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                } else if (contextMenu.keySet().toArray()[contextOption].equals("Stop")) {
                                    try {
                                        ((AudioFile) menu.get(option)).stop();
                                    } catch (NotPlaying e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                    contextMenu.clear();
                                    contextMenu.put("Play", "ENTER");
                                    contextMenu.put("Rename", "NUM 1");
                                    contextMenu.put("Delete", "DELETE");
                                    screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                                            contextMenu, controlLock);
                                    System.out.println("Music stopped");
                                }
                                break;
                            case "com.filemanager.Document":
                                if (contextMenu.keySet().toArray()[contextOption].equals("Read")) {
                                    contextMenuShow = false;
                                    contextOption = 0;
                                    screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                                            contextMenu, controlLock);
                                    ((Document) menu.get(option)).read();
                                }
                                break;
                            case "com.filemanager.RegularFile":
                                if (contextMenu.keySet().toArray()[contextOption].equals("Open")) {
                                    File file = new File(
                                            String.format("%s/%s", currentDirectory, menu.get(option).getName()));
                                    try {
                                        desktop.open(file);
                                    } catch (IOException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                    contextMenuShow = false;
                                }
                                break;
                        }

                        if (contextMenu.keySet().toArray()[contextOption].equals("Rename")) {
                            String fileName = menu.get(option).getName();
                            File oldFile = new File(
                                    String.format("%s/%s", currentDirectory, fileName));

                            String fileExtension = FilenameUtils.getExtension(fileName);
                            scanner.nextLine();
                            System.out.print("Enter the new file name: ");
                            String newFileName = scanner.nextLine();

                            File newFile = new File(
                                    String.format("%s/%s.%s", currentDirectory, newFileName, fileExtension));
                            if (oldFile.renameTo(newFile)) {
                                contextOption = 0;
                                contextMenuShow = false;
                                if (menu.get(option).getClass().getName().equals("com.filemanager.Directory")) {
                                    menu.get(option).setName(newFileName);
                                } else
                                    menu.get(option).setName(newFileName + "." + fileExtension);
                                screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                                        contextMenu, controlLock);
                                System.out.println("File renamed successfully");
                            } else {
                                System.out.println("Failed to rename file");
                            }
                        } else if (contextMenu.keySet().toArray()[contextOption].equals("Delete")) {
                            File file = new File(
                                    String.format("%s/%s", currentDirectory, menu.get(option).getName()));

                            scanner.nextLine();
                            System.out.print("Are you sure you want to delete this file? [Y/N]: ");
                            String confirmation = scanner.nextLine();

                            if (confirmation.equalsIgnoreCase("Y")) {
                                if (deleteFile(file)) {
                                    contextOption = 0;
                                    contextMenuShow = false;
                                    menu.remove(option);
                                    screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                                            contextMenu, controlLock);
                                    System.out.println("File deleted successfully");
                                } else {
                                    contextOption = 0;
                                    contextMenuShow = false;
                                    screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                                            contextMenu, controlLock);
                                    System.out.println("Failed to delete file");
                                }
                            } else {
                                contextOption = 0;
                                contextMenuShow = false;
                                screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                                        contextMenu, controlLock);
                                System.out.println("Action canceled");
                            }
                        }

                        // contextMenuShow = false;
                        // contextOption = 0;
                        // screen.refreshScreen(menu, option, currentDirectory, contextMenuShow,
                        // contextOption,
                        // contextMenu, controlLock);
                    }
                    break;
                case "NumPad 1":
                    String fileName = menu.get(option).getName();
                    if (!fileName.equals("..")) {
                        File oldFile = new File(
                                String.format("%s/%s", currentDirectory, fileName));

                        String fileExtension = FilenameUtils.getExtension(fileName);
                        // scanner.nextLine();
                        System.out.print("Enter the new file name: ");
                        String newFileName = scanner.nextLine();

                        File newFile = new File(
                                String.format("%s/%s.%s", currentDirectory, newFileName, fileExtension));
                        if (oldFile.renameTo(newFile)) {
                            contextOption = 0;
                            contextMenuShow = false;
                            if (menu.get(option).getClass().getName().equals("com.filemanager.Directory")) {
                                menu.get(option).setName(newFileName);
                            } else
                                menu.get(option).setName(newFileName + "." + fileExtension);
                            screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                                    contextMenu, controlLock);
                            System.out.println("File renamed successfully");
                        } else {
                            System.out.println("Failed to rename file");
                        }
                    }
                case "Delete":
                    if (!menu.get(option).getName().equals("..")) {
                        File file = new File(
                                String.format("%s/%s", currentDirectory, menu.get(option).getName()));

                        // scanner.nextLine();
                        System.out.print("Are you sure you want to delete this file? [Y/N]: ");
                        String confirmation = scanner.nextLine();

                        if (confirmation.equalsIgnoreCase("Y")) {
                            if (deleteFile(file)) {
                                contextOption = 0;
                                contextMenuShow = false;
                                menu.remove(option);
                                screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                                        contextMenu, controlLock);
                                System.out.println("File deleted successfully");
                            } else {
                                contextOption = 0;
                                contextMenuShow = false;
                                screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                                        contextMenu, controlLock);
                                System.out.println("Failed to delete file");
                            }
                        } else {
                            contextOption = 0;
                            contextMenuShow = false;
                            screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                                    contextMenu, controlLock);
                            System.out.println("Action canceled");
                        }
                    }
                    break;
                case "Num Lock":
                    controlLock = true;
                    screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                            contextMenu, controlLock);
                    break;
                default:
                    if (!contextMenuShow) {
                        int keyIndex = getIndex(NativeKeyEvent.getKeyText(e.getKeyCode()).charAt(0));
                        if (keyIndex != -1) {
                            option = keyIndex;
                            screen.refreshScreen(menu, option, currentDirectory, false, 0, contextMenu, controlLock);
                        }
                    }
                    break;
            }
        } else {
            if (NativeKeyEvent.getKeyText(e.getKeyCode()).equals("Num Lock")) {
                controlLock = false;
                screen.refreshScreen(menu, option, currentDirectory, contextMenuShow, contextOption,
                        contextMenu, controlLock);
            }
        }
    }

    public static void updateConfig(String path, int maxSize, int maxItems) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(Integer.toString(maxSize));
            writer.newLine();
            writer.write(Integer.toString(maxItems));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initConfig(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Read the numbers from the file
            screen.changeMaxLength(Integer.parseInt(reader.readLine()));
            screen.changeMaxItems(Integer.parseInt(reader.readLine()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteFile(File f) {
        if (!f.exists())
            return false;

        if (f.isDirectory()) {
            File[] content = f.listFiles();
            if (content != null) {
                for (File file : content)
                    deleteFile(file);
            }
        }

        return f.delete();
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // System.out.println(NativeKeyEvent.getKeyText(e.getKeyCode()));
        if (!controlLock) {
            switch (NativeKeyEvent.getKeyText(e.getKeyCode())) {
                case "NumPad 6":
                case "NumPad 9":
                case "NumPad Subtract":
                case "NumPad Add":
                    updateConfig(configPath, screen.getMaxLength(), screen.getMaxItems());
                    break;
            }
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }
}
