package com.library.util;

/**
 * Background thread for sending notifications
 * Demonstrates concurrency and multithreading
 */
public class NotificationThread extends Thread {
    private boolean running = true;
    private int notificationInterval = 60000; // 1 minute

    public NotificationThread() {
        super("LibraryNotificationThread");
        setDaemon(true); // Daemon thread that won't prevent JVM exit
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(notificationInterval);
                sendNotification();
            } catch (InterruptedException e) {
                System.out.println("Notification thread interrupted.");
                break;
            }
        }
    }

    private void sendNotification() {
        System.out.println("\n[NOTIFICATION] Reminder: Return your book if the due date is approaching!");
    }

    public void stopNotifications() {
        this.running = false;
        this.interrupt();
    }
}

