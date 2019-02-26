package com.ewp.crm.utils.cleaner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TransitFolderCleaner {
    private static Logger logger = LoggerFactory.getLogger(TransitFolderCleaner.class);

    public static void cleanFolder(String path){
        File dir = new File(path);
        if (dir.isDirectory() && dir.exists()){
            try {
                File[] files = dir.listFiles();
                if (files != null && files.length != 0){
                    for (File f : files)
                        f.delete();
                }
            }catch (NullPointerException npe){
                logger.error("Attempt to delete files from non existing folder " + dir);
            }
        }
    }
}