package ru.gb.java2.chat.server.chat;

import ru.gb.java2.chat.clientserver.Command;
import ru.gb.java2.chat.clientserver.CommandType;
import ru.gb.java2.chat.clientserver.commands.AuthCommandData;
import ru.gb.java2.chat.clientserver.commands.PrivateMessageCommandData;
import ru.gb.java2.chat.clientserver.commands.PublicMessageCommandData;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {

    public static final int LOGIN_DELAY = 120000;
    private final MyServer server;
    private final Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String username;

    public ClientHandler(MyServer server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public void handle() throws IOException {
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        ExecutorService executorService = Executors.newCachedThreadPool();

        executorService.execute(() -> {
            try {
                authentication();
                readMessages();
            } catch (IOException e) {
                System.err.println("Failed to process message from client");
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    System.err.println("Failed to close connection");
                }
            }
        });
        executorService.shutdown();
    }

    private void authentication() throws IOException {

        Timer authTimer = new Timer();
        TimerTask authTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (username == null) {
                    try {
                        closeConnection();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        authTimer.schedule(authTimerTask, LOGIN_DELAY);

        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            if (command.getType() == CommandType.AUTH) {
                AuthCommandData data = (AuthCommandData) command.getData();
                String login    = data.getLogin();
                String password = data.getPassword();

                String username = server.getAuthService().getUsernameByLoginAndPassword(login, password);
                if (username == null) {
                    sendCommand(Command.errorCommand("Некорректные логин и пароль!"));
                } else if (server.isUsernameBusy(username)) {
                    sendCommand(Command.errorCommand("Такой юзер уже существует!"));
                } else {
                    this.username = username;
                    sendCommand(Command.authOkCommand(username));
                    server.subscribe(this);
                    return;
                }
            }
        }
    }



    private Command readCommand() throws IOException {
        Command command = null;
        try {
            command = (Command) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to read Command class");
            e.printStackTrace();
        }
        return command;
    }

    private void closeConnection() throws IOException {
        server.unsubscribe(this);
        clientSocket.close();
    }

    private void readMessages() throws IOException {
        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            switch (command.getType()) {
                case END:
                    return;
                case PRIVATE_MESSAGE: {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                    String recipient = data.getReceiver();
                    String privateMessage = data.getMessage();
                    server.sendPrivateMessage(this, recipient, privateMessage);
                    break;
                }
                case PUBLIC_MESSAGE: {
                    PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
                    processMessage(data.getMessage());
                }
            }
        }
    }

    private void processMessage(String message) throws IOException {
        server.broadcastMessage(message, this);
    }

    public void sendCommand(Command command) throws IOException {
        outputStream.writeObject(command);
    }

    public String getUsername() {
        return username;
    }
}
