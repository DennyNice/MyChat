package com.company;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientWindow extends JFrame {

    private static final String SERVER_HOST = "localhost";

    private static final int SERVER_PORT = 3443;

    private Socket clientSocket;

    private Scanner inMessage;

    private PrintWriter outMessage;

    private JTextField jtfMessage;
    private JTextField jtfName;
    private JTextArea jtaTextAreaMessage;
    private String clientName = "";

    public String getClientName() {
        return this.clientName;
    }

    public ClientWindow() {
        try {
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            inMessage = new Scanner(clientSocket.getInputStream());
            outMessage = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        setBounds(600, 300, 600, 500);
        setTitle("Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jtaTextAreaMessage = new JTextArea();
        jtaTextAreaMessage.setEditable(false);
        jtaTextAreaMessage.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jtaTextAreaMessage);
        add(jsp, BorderLayout.CENTER);

        JLabel jlNumberOfClients = new JLabel("People in chat: ");
        add(jlNumberOfClients, BorderLayout.NORTH);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);
        JButton jbSendMessage = new JButton("send message");
        bottomPanel.add(jbSendMessage, BorderLayout.EAST);
        jtfMessage = new JTextField("your message");
        bottomPanel.add(jtfMessage, BorderLayout.CENTER);
        jtfName = new JTextField("your name ");
        bottomPanel.add(jtfName, BorderLayout.WEST);

        jbSendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (!jtfMessage.getText().trim().isEmpty() && !jtfName.getText().trim().isEmpty()) {
                    clientName = jtfName.getText();
                    sendMsg();

                    jtfMessage.grabFocus();
                }
            }
        });

        jtfMessage.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                jtfMessage.setText("");
            }
        });

        jtfName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                jtfName.setText("");
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    while (true) {

                        if (inMessage.hasNext()) {

                            String inMes = inMessage.nextLine();
                            String clientsInChat = "people in chat: ";
                            if (inMes.indexOf(clientsInChat) == 0) {
                                jlNumberOfClients.setText(inMes);
                            } else {

                                jtaTextAreaMessage.append(inMes);

                                jtaTextAreaMessage.append("\n");
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        }).start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {

                    if (!clientName.isEmpty() && clientName != "add your name: ") {
                        outMessage.println(clientName + " leave chat!");
                    } else {
                        outMessage.println("member leaves chat without name ");
                    }

                    outMessage.println("##session##end##");
                    outMessage.flush();
                    outMessage.close();
                    inMessage.close();
                    clientSocket.close();
                } catch (IOException exc) {

                }
            }
        });

        setVisible(true);
    }

    public void sendMsg() {

        String messageStr = jtfName.getText() + ": " + jtfMessage.getText();

        outMessage.println(messageStr);
        outMessage.flush();
        jtfMessage.setText("");
    }
}
