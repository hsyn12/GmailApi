package com.setting.dl.google.gmailapi.mail.gmail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.setting.dl.google.gmailapi.u;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static com.setting.dl.google.gmailapi.mail.gmail.GmailService.getGmailService;

/**
 * Created by hsyn on 8.06.2017.
 * <p>
 * mail işlemleri
 */

public final class Mail {
    
    /**
     * Create a message from an email.
     *
     * @param emailContent Email to be set to raw of message
     * @return a message containing a base64url encoded email
     * @throws IOException        ex
     * @throws MessagingException ex
     */
    private static Message createMessageWithEmail(MimeMessage emailContent)
        throws
        MessagingException,
        IOException {
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[]  bytes        = buffer.toByteArray();
        String  encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message      = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
    
    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to       Email address of the receiver.
     * @param from     Email address of the sender, the mailbox account.
     * @param subject  Subject of the email.
     * @param bodyText Body text of the email.
     * @param file     Path to the file to be attached.
     * @return MimeMessage to be used to wake email.
     * @throws MessagingException ex
     */
    private static MimeMessage createEmailWithAttachment(String to, String from, String subject, String bodyText, File file)
        throws
        MessagingException,
        IOException {
        
        Properties props   = new Properties();
        Session    session = Session.getDefaultInstance(props, null);
        
        MimeMessage email = new MimeMessage(session);
        
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
            new InternetAddress(to));
        email.setSubject(subject);
        
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(bodyText, "text/plain; charset=utf-8");
        
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        
        mimeBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(file);
        
        mimeBodyPart.setDataHandler(new DataHandler(source));
        mimeBodyPart.setFileName(file.getName());
        
        multipart.addBodyPart(mimeBodyPart);
        email.setContent(multipart);
        
        return email;
    }
    
private static String getTo(final Context context) {
    
    return context.getSharedPreferences("gmail", Context.MODE_PRIVATE).getString("to", null);
}

public static String getFrom(final Context context) {
    
    return context.getSharedPreferences("gmail", Context.MODE_PRIVATE).getString("from", null);
}
    
    
    public static void send(final Context context, final String fileName) {
        
        if (!u.isDeviceOnline(context)) return;
        
        String value = getSavedValue(context, fileName);
        
        if (value == null || TextUtils.isEmpty(value)) return;
        
        send(context, fileName, value);
        
    }
    
    
    synchronized
    public static void saveValue(Context context, String fileName, String value) {
        
        File file = new File(context.getFilesDir(), fileName);
        
        FileOutputStream stream;
        
        try {
            
            stream = new FileOutputStream(file, true);
            stream.write(value.getBytes());
            stream.close();
            
        }
        catch (IOException ignored) {}
    }
    
    @Nullable
    synchronized
    private static String getSavedValue(Context context, String fileName) {
        
        File file = new File(context.getFilesDir(), fileName);
        
        if (!file.exists()) return null;
        
        long   len   = file.length();
        byte[] bytes = new byte[(int) len];
        
        try {
            
            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
            in.close();
        }
        catch (IOException ignored) {}
        
        
        return new String(bytes);
    }
    
    
    public static void send(final Context context, final String subject, final String body) {
        
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                
                try {
                    
                    getGmailService(context).users().messages().send("me", createMessageWithEmail(createEmail(getTo(context), getFrom(context), subject, body))).execute();
                    
                    
                    if (subject.endsWith(".txt")) {
                        
                        if (context.deleteFile(subject)) {
                            
                            u.log.d("dosya silindi : %s", subject);
                        }
                        else {
                            
                            u.log.d("dosya silinemedi : %s", subject);
                        }
                    }
                    
                    
                }
                catch (Exception e) {
                    
                    u.log.e("mail gitmedi : %s", e.toString());
                    saveValue(context, "error.txt", e.toString());
                }
                
                return null;
            }
        };
        
        task.execute();
        u.run(() -> deleteAllSent(context), 10000);
    }
    
    private static MimeMessage createEmail(String to, String from, String subject, String bodyText)
        throws
        MessagingException {
        
        Properties props   = new Properties();
        Session    session = Session.getDefaultInstance(props, null);
        
        MimeMessage email = new MimeMessage(session);
        
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText, "utf-8");
        return email;
    }
    
    
public static void deleteAllSent(final Context context) {
    
    final Gmail service = getGmailService(context);
    
    @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
        
        @Override
        protected Void doInBackground(Void... params) {
            
            List<Message> sentMessages;
            
            try {
                
                sentMessages = mylistMessagesWithLabelsWithQ(service, Collections.singletonList("SENT"), u.s("to:%s", getTo(context)));
                
                for (Message message : sentMessages) {
                    
                    service.users().messages().delete("me", message.getId()).execute();
                }
                
                u.log.d("gönderilen mailler silindi");
                
            }
            catch (Exception e) {
                u.log.e("mailler silinemedi : %s", e.toString());
                
            }
            
            return null;
        }
    };
    
    task.execute();
}
    
    private static List<Message> mylistMessagesWithLabelsWithQ(Gmail service, List<String> labelIds, String query)
        throws
        IOException {
        
        ListMessagesResponse response = service.users().messages().list("me").setQ(query)
            .setLabelIds(labelIds).execute();
        
        List<Message> messages = new ArrayList<>();
        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list("me").setLabelIds(labelIds)
                    .setPageToken(pageToken).execute();
            }
            else {
                break;
            }
        }
        return messages;
    }
    
    
}
