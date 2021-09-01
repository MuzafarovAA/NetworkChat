package ru.gb.java2.chat.client;

import java.io.*;

public class ChatLog {

    private static final String FILE_PATH = "./chatlog/";
    public static final int ROWS_COUNT = 100;
    private static String username;
    private static File file;
    private static String fileName;
    private PrintWriter printWriter;

    public ChatLog(String username) {
        this.username = username;
    }

    public void createLogFile() throws IOException {
        fileName = new String(username + ".log");
        file = new File(FILE_PATH + fileName);
        file.getParentFile().mkdirs();
        file.createNewFile();
    }

    public void writeLogFile() throws IOException {

        printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));


    }

    public void printLogFile (String message) {
        printWriter.print(message);
        printWriter.flush();
    }

    public void closeLogFile() {
        printWriter.close();
    }

}
