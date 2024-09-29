package com.example.mymessenger;

public class ServiceLocator {
    private static NotificationService notificationService;

    public static void setNotificationService(NotificationService service) {
        notificationService = service;
    }

    public static NotificationService getNotificationService() {
        return notificationService;
    }
}

