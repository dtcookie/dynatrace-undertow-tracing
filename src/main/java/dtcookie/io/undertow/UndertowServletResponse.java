package dtcookie.io.undertow;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

public final class UndertowServletResponse implements HttpServletResponse {
	
	private final HttpServerExchange exchange;
	
	public UndertowServletResponse(HttpServerExchange exchange) {
		this.exchange = exchange;
	}

	@Override
	public String getCharacterEncoding() {
		return getHeader(Headers.CONTENT_ENCODING_STRING);
	}

	@Override
	public String getContentType() {
		return getHeader(Headers.CONTENT_TYPE_STRING);
	}

	@Override
	public boolean containsHeader(String name) {		
		HeaderMap headers = exchange.getResponseHeaders();
		if (headers == null) {
			return false;
		}
		return headers.contains(new HttpString(name));
	}

	@Override
	public void setStatus(int sc) {
		exchange.setStatusCode(sc);
	}

	@Override
	public void setStatus(int sc, String sm) {
		exchange.setStatusCode(sc);
	}

	@Override
	public int getStatus() {
		return exchange.getStatusCode();
	}
	@Override
	public void setDateHeader(String name, long date) {
		setHeader(name, String.valueOf(date));
	}

	@Override
	public void addDateHeader(String name, long date) {
		setHeader(name, String.valueOf(date));
	}

	@Override
	public void setHeader(String name, String value) {
		HeaderMap headers = exchange.getResponseHeaders();
		if (headers == null) {
			return;
		}
		headers.put(new HttpString(name), value);		
	}

	@Override
	public void addHeader(String name, String value) {
		setHeader(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		setHeader(name, String.valueOf(value));		
	}

	@Override
	public void addIntHeader(String name, int value) {
		setHeader(name, String.valueOf(value));
	}

	@Override
	public String getHeader(String name) {
		if (name == null) {
			return null;
		}
		HeaderMap headers = exchange.getResponseHeaders();
		if (headers == null) {
			return null;
		}
		HeaderValues values = headers.get(name);
		if ((values == null) || values.isEmpty()) {
			return null;
		}
		return values.getLast();
	}

	@Override
	public Collection<String> getHeaders(String name) {
		if (name == null) {
			return Collections.emptyList();
		}
		HeaderMap headers = exchange.getResponseHeaders();
		if (headers == null) {
			return Collections.emptyList();
		}
		HeaderValues values = headers.get(name);
		if ((values == null) || values.isEmpty()) {
			return Collections.emptyList();
		}
		return values;
	}

	@Override
	public Collection<String> getHeaderNames() {
		HeaderMap headers = exchange.getResponseHeaders();
		if (headers == null) {
			return Collections.emptyList();
		}
		Collection<HttpString> headerNames = headers.getHeaderNames();
		if (headerNames == null) {
			return Collections.emptyList();
		}
		ArrayList<String> names = new ArrayList<String>();
		for (HttpString headerName : headerNames) {
			if (headerName == null) {
				continue;
			}
			names.add(headerName.toString());
		}
		return names;
	}

	@Override
	public void addCookie(Cookie cookie) {
	}

	@Override
	public String encodeRedirectURL(String url) {
		return null;
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return null;
	}

	@Override
	public void setCharacterEncoding(String charset) {
	}

	@Override
	public void setContentLength(int len) {
	}

	@Override
	public void setContentLengthLong(long len) {
	}

	@Override
	public void setContentType(String type) {
	}

	@Override
	public void setBufferSize(int size) {
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {
	}

	@Override
	public void resetBuffer() {
	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {
	}

	@Override
	public void setLocale(Locale loc) {
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public String encodeURL(String url) {
		return url;
	}

	@Override
	public String encodeUrl(String url) {
		return url;
	}

	@Override
	public String encodeRedirectUrl(String url) {
		return url;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
	}

	@Override
	public void sendError(int sc) throws IOException {
	}

	@Override
	public void sendRedirect(String location) throws IOException {
	}

}
