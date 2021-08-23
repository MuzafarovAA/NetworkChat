package ru.gb.java2.chat.client.model;

import ru.gb.java2.chat.clientserver.Command;

import java.io.IOException;

public interface ReadCommandListener {

    void processReceivedCommand(Command command) throws IOException;
}
