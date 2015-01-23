/*
 * Copyright (c) 2015.
 */

package de.arcus.framework.superuser;

import java.io.IOException;

/**
 * The superuser managers
 *
 * This static class handles the superuser session.
 * Start the session with {@link #askForPermissions() askForPermissions}.
 * To run a command create an instance of {@link SuperUserCommand SuperUserCommand} and {@link SuperUserCommand#execute() execute} it.
 */
public class SuperUser {
    /**
     * The su process
     */
    private static Process mProcess;

    /**
     * Gets the active su process
     * @return Process
     */
    static Process getProcess() {
        return mProcess;
    }

    /**
     * Starts the superuser session
     * To start the session in your app use {@link #askForPermissions()}
     */
    private static boolean sessionStart() {
        // Starts the su process
        try {
            mProcess = Runtime.getRuntime().exec("su");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Stops the superuser session
     */
    public static void sessionStop() {
        if (mProcess == null) return;

        // End the process
        mProcess.destroy();
        mProcess = null;
    }

    /**
     * Gets whether the su session is running
     * @return Return whether the su session is running
     */
    public static boolean sessionIsRunning() {
        if (mProcess == null) return false;

        // Hack to see if the process is running
        // This is not nice, but there is no other way to check this
        try {
            mProcess.exitValue();
            return false;
        } catch(IllegalThreadStateException ex) {
            // Could not get the return value => process is running
            return true;
        }
    }

    /**
     * Checks whether superuser permissions were granted
     * @return Return whether superuser permissions were granted
     */
    public static boolean hasPermissions() {
        // Just check whether the session is running
        return sessionIsRunning();
    }

    /**
     * This is like hasPermissions() but asks for the superuser permissions
     * and give the user a change to grant it now.
     * Use this to start you session
     * @return Return whether superuser permissions were granted
     */
    public static boolean askForPermissions() {
        // We already have superuser permissions
        if (hasPermissions()) return true;

        // Starts the process
        if (sessionStart()) {
            // Test for superuser
            SuperUserCommand superUserCommand = new SuperUserCommand("whoami");
            if (superUserCommand.execute()) {
                // Gets the whoami username
                String[] output = superUserCommand.getStandardOutput();
                if (output.length >= 1 && output[0].equals("root")) {
                    // We are root
                    return true;
                }
            }
        }

        // We don't have superuser permissions; abort session
        sessionStop();
        return false;
    }
}
