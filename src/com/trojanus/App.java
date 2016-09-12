package com.trojanus;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.mail.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.*;
import java.util.List;


/**
 * Created by John on 9/3/2016.
 */
public class App extends JPanel {
    private JPanel panelMain;
    private JButton sendButton;
    private JTextField senderEmailTextField;
    private JPasswordField senderEmailPasswordField;
    private JTextArea subjectTextArea;
    private JTextPane emailTextPane;
    private JButton attachFileButton;
    private JLabel attachedFileLabel;
    private JTextArea sendToTextArea;
    public String filename;
    public File file;
    public List<String> addresslist;

    public App() {
        panelMain.setPreferredSize(new Dimension(800, 600));
        panelMain.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        SpinnerDateModel spinnerDateModel = new SpinnerDateModel();
        spinnerDateModel.setCalendarField(Calendar.MINUTE);

        subjectTextArea.setBorder(BorderFactory.createLineBorder(Color.gray, 1));

        attachedFileLabel.setText("");

        addresslist = new ArrayList<String>();

        sendToTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLog(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLog(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            public void updateLog(DocumentEvent e) {
                String[] s = sendToTextArea.getText().split("\\r?\\n");
                addresslist = new ArrayList<String>(Arrays.asList(s));
//                for testing purposes
                System.out.println(addresslist);
            }
        });


        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mailSender();
            }
        });
        attachFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fileChooser = new JFileChooser();
                int returnVal = fileChooser.showOpenDialog(App.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    System.out.println("Opening: " + file.getName() + "." + "\n");
                    try {
                        System.out.println("File path: " + file.getCanonicalPath() + "\n");
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    filename = file.getName();
                    attachedFileLabel.setText("Attached file: " + filename);
                    attachedFileLabel.setVisible(true);
                } else {
                    System.out.println("Open command cancelled by user." + "\n");
                }
            }
        });
        attachedFileLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                file = null;
                attachedFileLabel.setVisible(false);
            }
        });

    }

    private void mailSender() {
        String username = senderEmailTextField.getText();
        String password = new String(senderEmailPasswordField.getPassword());

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        if (addresslist.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Address list cannot be empty.", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else if(subjectTextArea.getText().isEmpty()){
            JOptionPane.showMessageDialog(null, "Email subject cannot be empty.", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                InternetAddress[] addressToList = new InternetAddress[addresslist.size()];

                for (int i = 0; i < addresslist.size(); i++) {
                    addressToList[i] = new InternetAddress(addresslist.get(i));
                    System.out.println(addressToList[i]);
                }

                message.setRecipients(Message.RecipientType.TO, addressToList);
                message.setSubject(subjectTextArea.getText());

                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(emailTextPane.getText(), "text/html");
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);

                messageBodyPart = new MimeBodyPart();
                DataSource dataSource = new FileDataSource(file.getCanonicalFile());
                messageBodyPart.setDataHandler(new DataHandler(dataSource));
                messageBodyPart.setFileName(filename);
                multipart.addBodyPart(messageBodyPart);
                message.setContent(multipart);

                Transport.send(message);

                System.out.println("Email sent");
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }



    public static void main(String[] args) {
        JFrame jFrame = new JFrame("App");
        jFrame.setContentPane(new App().panelMain);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setVisible(true);
    }
}


