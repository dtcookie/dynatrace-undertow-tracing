package dtcookie;

import java.util.Timer;

import dtcookie.io.undertow.UndertowServlet;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public final class DynatraceUndertowMain implements HttpHandler {
	
	private static final int PORT = 8080;

	private final Undertow server;
	private final Timer timer;

	public static void main(final String[] args) throws Exception {
		new DynatraceUndertowMain().start(3000);
	}
	
	private DynatraceUndertowMain() {
		server = Undertow.builder()
				.addHttpListener(PORT, "localhost")
				.setHandler(this).build();
		timer = new Timer();
	}
	
	private void start(long period) {
		server.start();
		timer.schedule(new Http("http://localhost:" + PORT + "/"), 0, period);
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		UndertowServlet.invoke(exchange);
		exchange.startBlocking();
		exchange.dispatch(() -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// ignore
			}
//			exchange.setStatusCode(201);
	        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
	        exchange.getResponseSender().send("Hello World");
		});
	}

}
