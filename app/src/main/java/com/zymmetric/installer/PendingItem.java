package com.zymmetric.installer;

/**
 * Created by chris on 4/10/16.
 */
public class PendingItem {

    public int id;
    public AppStatus status;
    public int progress;
    public int main = 0;
    public PendingItem(int id, int main) {
        this.id = id;
        this.main = main;
        status = AppStatus.NOTINSTALLED;
    }
}
