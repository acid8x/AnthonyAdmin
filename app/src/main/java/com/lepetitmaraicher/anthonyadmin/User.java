package com.lepetitmaraicher.anthonyadmin;

class User {

    private String badgeId;
    private String badgeName;
    private long lastPunch;
    private String currentJob;
    private boolean isAdmin;

    User(String badgeId, String badgeName, long lastPunch, String currentJob, int isAdmin) {
        this.badgeId = badgeId;
        this.badgeName = badgeName;
        this.lastPunch = lastPunch;
        this.currentJob = currentJob;
        this.isAdmin = isAdmin > 0;
    }

    public String getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }

    public String getBadgeName() {
        return badgeName;
    }

    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }

    public long getLastPunch() {
        return lastPunch;
    }

    public void setLastPunch(long lastPunch) {
        this.lastPunch = lastPunch;
    }

    public String getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(String currentJob) {
        this.currentJob = currentJob;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
