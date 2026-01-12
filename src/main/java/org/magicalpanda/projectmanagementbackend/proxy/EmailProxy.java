package org.magicalpanda.projectmanagementbackend.proxy;

public interface EmailProxy {

    void sendEmail(String to, String subject, String body);
}
