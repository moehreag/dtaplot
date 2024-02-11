package io.github.moehreag.dtaplot;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class AboutDialog {

	public AboutDialog(JFrame parent) {
		JDialog dialog = new JDialog(parent, Translations.translate("about.title"));

		JTextPane text = new JTextPane();
		text.setEditable(false);
		text.setContentType("text/html");
		text.setText(String.format(
				"<center>" +
				"<h2>%s %s</h2>" +
				"<br>" +
				"<p>%s</p>" +
				"<a href=\"%s\">%s</a>"+
				"</center>",
				Constants.NAME, Constants.VERSION,
				Translations.translate("about.description"),
				Constants.URL, Translations.translate("about.source")));
		text.setCaret(new DefaultCaret(){
			@Override
			public void paint(Graphics g) {
			}
		});
		TextPaneUtil.addLinkHandler(text, parent);
		TextPaneUtil.hideCaret(text);
		dialog.add(text);

		JPanel footer = new JPanel();
		footer.add(new JButton(new AbstractAction(Translations.translate("action.close")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		}));
		dialog.add(footer, BorderLayout.SOUTH);

		dialog.setSize(250, 200);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);

	}

}
