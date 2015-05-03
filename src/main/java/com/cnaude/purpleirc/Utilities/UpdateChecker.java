/*
 * Copyright (C) 2014 cnaude
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cnaude.purpleirc.Utilities;

import com.cnaude.purpleirc.PurpleIRC;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import com.cnaude.purpleirc.CommandSender;
import java.util.Timer;
import java.util.TimerTask;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author cnaude
 */
public class UpdateChecker {

    PurpleIRC plugin;

    Timer timer;
    private int newBuild = 0;
    private int currentBuild = 0;
    private String currentVersion = "";
    private String newVersion = "";

    /**
     *
     * @param plugin
     */
    public UpdateChecker(PurpleIRC plugin) {
        this.timer = new Timer();
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        try {
            currentBuild = Integer.valueOf(currentVersion.split("-")[2]);
        } catch (NumberFormatException e) {
            currentBuild = 0;
        }
        startUpdateChecker();
    }

    private void startUpdateChecker() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (plugin.isUpdateCheckerEnabled()) {
                    plugin.logInfo("Checking for " + plugin.updateCheckerMode() + " updates ... ");
                    updateCheck(plugin.updateCheckerMode());
                }
            }
        }, 0, 432000);
    }

    public void asyncUpdateCheck(final CommandSender sender, final String mode) {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (plugin.isUpdateCheckerEnabled()) {
                    updateCheck(sender, mode);
                }
            }
        }, 0);
    }

    private void updateCheck(CommandSender sender, String mode) {
        sender.sendMessage(updateCheck(mode));
    }

    private String updateCheck(String mode) {
        String message;
        try {
            URL url = new URL("http://h.cnaude.org:8081/job/PurpleIRC-forge/lastStableBuild/api/json");
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("User-Agent", "PurpleIRC-forge Update Checker");
            conn.setDoOutput(true);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();
            final JSONObject obj = (JSONObject) JSONValue.parse(response);
            if (obj.isEmpty()) {
                return plugin.LOG_HEADER_F + " No files found, or Feed URL is bad.";
            }

            newVersion = obj.get("number").toString();
            String downloadUrl = obj.get("url").toString();
            plugin.logDebug("newVersionTitle: " + newVersion);
            newBuild = Integer.valueOf(newVersion);
            if (newBuild > currentBuild) {
                message = plugin.LOG_HEADER_F + " Latest dev build: " + newVersion + " is out!" + " You are still running build: " + currentVersion;
                message = message + plugin.LOG_HEADER_F + " Update at: " + downloadUrl;
            } else if (currentBuild > newBuild) {
                message = plugin.LOG_HEADER_F + " Dev build: " + newVersion + " | Current build: " + currentVersion;
            } else {
                message = plugin.LOG_HEADER_F + " No new version available";
            }
        } catch (IOException | NumberFormatException e) {
            message = plugin.LOG_HEADER_F + " Error checking for latest dev build: " + e.getMessage();
        }
        return message;
    }

    public void cancel() {
        timer.cancel();
    }
}
