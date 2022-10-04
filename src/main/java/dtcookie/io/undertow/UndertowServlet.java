package dtcookie.io.undertow;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.undertow.server.HttpServerExchange;

public class UndertowServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static void invoke(HttpServerExchange exchange) {
		try {
			new UndertowServlet().service(new UndertowServletRequest(exchange), new UndertowServletResponse(exchange));	
		} catch (Throwable t) {
			// ignore
		}
		
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.startAsync(req, resp);
	}

}
