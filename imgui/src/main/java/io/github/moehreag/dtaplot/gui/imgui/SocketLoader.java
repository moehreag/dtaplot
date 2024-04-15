package io.github.moehreag.dtaplot.gui.imgui;

import java.net.InetSocketAddress;

import io.github.moehreag.dtaplot.gui.imgui.component.TcpComponent;
import io.github.moehreag.dtaplot.gui.imgui.component.WsComponent;
import io.github.moehreag.dtaplot.socket.TcpSocket;
import io.github.moehreag.dtaplot.socket.WebSocket;

public class SocketLoader {
	public static void loadWS(InetSocketAddress address, WsComponent component) {

		WebSocket.read(address,
				() -> App.View.WS.getComponent().queryPassword(),
				component::load);
	}

	public static void loadTCP(InetSocketAddress address, TcpComponent component) {

		component.load(TcpSocket.readAll(address));
	}
}
