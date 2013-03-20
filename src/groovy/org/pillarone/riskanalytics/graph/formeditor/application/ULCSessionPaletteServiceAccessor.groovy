package org.pillarone.riskanalytics.graph.formeditor.application

import org.pillarone.riskanalytics.graph.core.palette.service.IPaletteServiceAccessor
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService
import com.ulcjava.base.server.ULCSession
import org.pillarone.riskanalytics.graph.core.palette.service.SingletonPaletteServiceAccessor


class ULCSessionPaletteServiceAccessor implements IPaletteServiceAccessor {

    private static final String PALETTE_SERVICE = "paletteService"
    private static IPaletteServiceAccessor fallBack = new SingletonPaletteServiceAccessor()

    PaletteService obtainService() {
        final ULCSession session = ULCSession.currentSession()
        if(session == null) { //TODO: use UserConext
            return fallBack.obtainService()
        }

        final PaletteService paletteService = session.getAttribute(PALETTE_SERVICE)
        if (paletteService == null) {
            paletteService = new PaletteService()
            session.setAttribute(PALETTE_SERVICE, paletteService)
        }

        return paletteService
    }

}
