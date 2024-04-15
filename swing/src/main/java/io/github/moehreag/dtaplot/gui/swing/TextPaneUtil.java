package io.github.moehreag.dtaplot.gui.swing;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;

import io.github.moehreag.dtaplot.gui.util.UrlHandler;

public class TextPaneUtil {

	public static void addLinkHandler(JTextPane pane, JFrame frame){
		pane.addHyperlinkListener(e -> {
			try {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					UrlHandler.open(e.getURL());
				}
			} catch (Throwable throwable) {
				JOptionPane.showMessageDialog(frame, throwable.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
				DtaPlot.LOGGER.error("Failed to open url: ", throwable);
			}
		});
	}

	public static void hideCaret(JTextPane pane){
		pane.getCaret().setVisible(false);
		pane.getCaret().setSelectionVisible(false);
		pane.setSelectionColor(new Color(0, true));
		pane.setSelectedTextColor(Color.BLACK);
		pane.setCaretColor(pane.getBackground());
	}
}
