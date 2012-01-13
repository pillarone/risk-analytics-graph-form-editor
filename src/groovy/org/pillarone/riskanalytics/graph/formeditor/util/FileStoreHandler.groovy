package org.pillarone.riskanalytics.graph.formeditor.util

import com.ulcjava.base.shared.FileChooserConfig
import com.ulcjava.base.application.util.IFileChooseHandler
import com.ulcjava.base.application.util.IFileStoreHandler
import com.ulcjava.base.application.ULCAlert
import com.ulcjava.base.application.ClientContext
import com.ulcjava.base.application.ULCWindow
import org.pillarone.riskanalytics.core.parameterization.ParameterWriter
import org.pillarone.riskanalytics.core.util.IConfigObjectWriter
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class FileStoreHandler {

    static Log LOG = LogFactory.getLog(FileStoreHandler)

    private static save(IFileStoreHandler fileStoreHandler, String name, final ULCWindow ancestor) {
        FileChooserConfig config = new FileChooserConfig();
        config.setDialogTitle("Save file as");
        config.setDialogType(FileChooserConfig.SAVE_DIALOG);
        config.setSelectedFile(name);

        IFileChooseHandler chooser = new IFileChooseHandler() {
            public void onSuccess(String[] filePaths, String[] fileNames) {
                String selectedFile = filePaths[0];
                try {
                    ClientContext.storeFile(fileStoreHandler, selectedFile);
                } catch (Exception ex) {

                }
            }

            public void onFailure(int reason, String description) {
                if (reason == IFileStoreHandler.FAILED) {
                    new ULCAlert(ancestor, "Export failed", description, "ok").show();
                }
            }
        };
        ClientContext.chooseFile(chooser, config, ancestor);
    }

    public static void saveOutput(String name, final String text, final ULCWindow ancestor) {
        IFileStoreHandler fileStoreHandler =
        new IFileStoreHandler() {
            public void prepareFile(java.io.OutputStream stream) throws Exception {
                try {
                    stream.write(text.getBytes());
                } catch (UnsupportedOperationException t) {
                    new ULCAlert(ancestor, "Export failed", t.getMessage(), "Ok").show();
                } catch (Throwable t) {
                    new ULCAlert(ancestor, "Export failed", t.getMessage(), "Ok").show();
                } finally {
                    stream.close();
                }
            }

            public void onSuccess(String filePath, String fileName) {
            }

            public void onFailure(int reason, String description) {
                //	        			new ULCAlert(ancestor, "Export failed", description, "Ok").show();
            }
        };
        save(fileStoreHandler, name, ancestor)
    }

    public static void saveOutput(String fileName, final ConfigObject configObject, final ULCWindow ancestor) {
        final IConfigObjectWriter writer = new GParameterWriter();
        IFileStoreHandler storeHandler = new IFileStoreHandler() {
            public void prepareFile(OutputStream outputStream) throws Exception {
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
                    writer.write(configObject, bw);
                } catch (Throwable t) {
                    LOG.error("Export failed: " + t.getMessage(), t);
                } finally {
                    outputStream.close();
                }
            }

            public void onSuccess(String s, String s1) {
            }

            public void onFailure(int i, String s) {
            }
        };

        save(storeHandler, fileName, ancestor)
    }
}

class GParameterWriter extends ParameterWriter {
    @Override
    void write(ConfigObject configObject, BufferedWriter writer) {
        writer.append('package ').append(configObject.package.toString()).append("\n\n")
        configObject.remove('package')
        printConfigObject("", configObject, writer, false, -1)
        writer.flush()
    }


}
