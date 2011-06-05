package org.pillarone.riskanalytics.graph.formeditor.application

import com.ulcjava.base.client.ISessionStateListener
import com.ulcjava.base.client.UISession
import com.ulcjava.base.shared.logging.LogManager
import com.ulcjava.base.shared.logging.SimpleLogManager
import com.ulcjava.container.local.server.LocalContainerAdapter

class FormEditorLauncher extends LocalContainerAdapter {

    @Override
    protected Class getApplicationClass() {
        FormEditorApplication
    }

    public static void launch() {
        LogManager.setLogManager(new SimpleLogManager())
        FormEditorLauncher launcher = new FormEditorLauncher()
        launcher.start()
        StandaloneSessionStateListener listener = new StandaloneSessionStateListener()
        launcher.clientSession.addSessionStateListener(listener)
        synchronized (listener) {
            listener.wait()
        }
    }

}

class StandaloneSessionStateListener implements ISessionStateListener {

    void sessionEnded(UISession session) throws Exception {
        println("PillarOne application shutdown ... cleaning up")
        synchronized (this) {
            notifyAll()
        }
    }

    void sessionError(UISession session, Throwable reason) {
        println("PillarOne application error..." + reason.getMessage())

    }

    void sessionStarted(UISession session) throws Exception {
        println("PillarOne application started...")

    }
}
