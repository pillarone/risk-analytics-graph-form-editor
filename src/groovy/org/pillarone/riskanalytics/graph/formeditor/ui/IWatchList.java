package org.pillarone.riskanalytics.graph.formeditor.ui;

/**
 *
 */
public interface IWatchList {

    public void addWatch(String path);

    public void removeWatch(String path);

    public void editWatch(String oldPath, String newPath);

    public void removeAllWatches();
}
