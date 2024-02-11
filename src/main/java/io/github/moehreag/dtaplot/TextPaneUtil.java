package io.github.moehreag.dtaplot;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;

public class TextPaneUtil {

	public static void addLinkHandler(JTextPane pane, JFrame frame){
		pane.addHyperlinkListener(e -> {
			try {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} else {
						try {
							ProcessBuilder builder = new ProcessBuilder("xdg-open", e.getURL().toString());
							builder.start();
						} catch (Exception ex) {
							throw new UnsupportedOperationException("Failed to open " + e.getURL().toString());
						}
					}
				}
			} catch (Throwable throwable) {
				JOptionPane.showMessageDialog(frame, throwable.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
				DtaPlot.LOGGER.error("Failed to open url: ", throwable);
			}
		});
	}
}
