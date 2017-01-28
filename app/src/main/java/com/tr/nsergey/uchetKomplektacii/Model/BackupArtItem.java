package com.tr.nsergey.uchetKomplektacii.Model;

import com.google.auto.value.AutoValue;

/**
 * Created by sergey on 24.01.17.
 */
@AutoValue
public abstract class BackupArtItem {
    static BackupArtItem create(String art, String name, String location,
                                String modification, int quantity){
        return new AutoValue_BackupArtItem(art, name, location, modification, quantity);
    }
    abstract String getArt();
    abstract String getName();
    abstract String getLocation();
    abstract String getModification();
    abstract int getQuantity();
}
