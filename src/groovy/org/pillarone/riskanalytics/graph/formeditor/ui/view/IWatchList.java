package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface IWatchList {

    public void addWatch(String path);

    public void removeWatch(String path);

    public void removeAllWatches();
}
