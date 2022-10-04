package dtcookie.io.undertow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

public final class UndertowServletRequest implements HttpServletRequest {
	
	private final HttpServerExchange exchange;
	private AsyncContext context = null;
	
	private static class RequestAttributes extends HashMap<String,Object> {
		private static final long serialVersionUID = 1L;
	}
	
	private static final AttachmentKey<RequestAttributes> REQUEST_ATTRIBUTES_KEY = AttachmentKey.create(RequestAttributes.class);
	
	public UndertowServletRequest(HttpServerExchange exchange) {
		this.exchange = exchange;
	}

	@Override
	public String getRequestURI() {
		return exchange.getRequestURI();
	}
	
	@Override
	public Object getAttribute(String name) {
		RequestAttributes attributes = exchange.getAttachment(REQUEST_ATTRIBUTES_KEY);
		if (attributes == null) {
			return null;
		}
		return attributes.get(name);
	}

	@Override
	public void setAttribute(String name, Object o) {
		RequestAttributes attributes = exchange.getAttachment(REQUEST_ATTRIBUTES_KEY);
		if (attributes == null) {
			attributes = new RequestAttributes();
			exchange.putAttachment(REQUEST_ATTRIBUTES_KEY, attributes);
		}
		if (o == null) {
			attributes.remove(name);
		} else {
			attributes.put(name, o);	
		}		
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		RequestAttributes attributes = exchange.getAttachment(REQUEST_ATTRIBUTES_KEY);
		if (attributes == null) {
			return Collections.emptyEnumeration();
		}
		return Collections.enumeration(attributes.keySet());
	}
	
	@Override
	public boolean isAsyncStarted() {
		synchronized (exchange) {
			return this.context != null;	
		}		
	}
	
	@Override
	public int getContentLength() {
		return (int) exchange.getRequestContentLength();
	}

	@Override
	public long getContentLengthLong() {
		return exchange.getRequestContentLength();
	}

	@Override
	public String getParameter(String name) {
		if (name == null) {
			return null;
		}
		Map<String, Deque<String>> parameters = exchange.getQueryParameters();
		if (parameters == null) {
			return null;
		}
		Deque<String> deque = parameters.get(name);
		if ((deque == null) || deque.isEmpty()) {
			return null;
		}
		return deque.getLast();
	}

	@Override
	public Enumeration<String> getParameterNames() {
		Map<String, Deque<String>> parameters = exchange.getQueryParameters();
		if (parameters == null) {
			return Collections.emptyEnumeration();
		}
		Set<String> keySet = parameters.keySet();
		return Collections.enumeration(keySet);
	}

	@Override
	public String[] getParameterValues(String name) {
		if (name == null) {
			return null;
		}
		Map<String, Deque<String>> parameters = exchange.getQueryParameters();
		if (parameters == null) {
			return null;
		}
		Deque<String> values = parameters.get(name);
		if (values == null) {
			return null;
		}
		return values.toArray(new String[values.size()]);
	}
	
	@Override
	public String getScheme() {
		return exchange.getRequestScheme();
	}

	@Override
	public String getServerName() {
		return exchange.getHostName();
	}

	@Override
	public int getServerPort() {
		return exchange.getHostPort();
	}
	
	@Override
	public String getProtocol() {
		HttpString protocol = exchange.getProtocol();
		if (protocol == null) {
			return null;
		}
		return protocol.toString();
	}

	@Override
	public String getRemoteAddr() {
		InetSocketAddress addr = exchange.getDestinationAddress();
		if (addr == null) {
			return null;
		}
		InetAddress inetAddr = addr.getAddress();
		if (inetAddr == null) {
			return null;
		}
		return inetAddr.getHostAddress();
	}

	@Override
	public AsyncContext startAsync(ServletRequest req, ServletResponse resp) throws IllegalStateException {
		synchronized (exchange) {
			if (this.context != null) {
				return this.context;
			}
			UndertowAsyncContext context = new UndertowAsyncContext(req, resp);
			this.context = context;
			exchange.addExchangeCompleteListener(context);
			return context;
		}
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return startAsync(this, new UndertowServletResponse(this.exchange));
	}

	@Override
	public AsyncContext getAsyncContext() {
		synchronized (exchange) {
			return this.context;
		}		
	}

	@Override
	public String getHeader(String name) {
		if (name == null) {
			return null;
		}
		HeaderMap headers = exchange.getRequestHeaders();
		if (headers == null) {
			return null;
		}
		HeaderValues values = headers.get(name);
		if ((values == null) || (headers.size() == 0)) {
			return null;
		}
		return values.getLast();
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		if (name == null) {
			return Collections.emptyEnumeration();
		}
		HeaderMap headers = exchange.getRequestHeaders();
		if (headers == null) {
			return Collections.emptyEnumeration();
		}
		HeaderValues headerValues = headers.get(name);
		if (headerValues == null) {
			return Collections.emptyEnumeration(); 
		}
		return Collections.enumeration(headerValues);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		HeaderMap headers = exchange.getRequestHeaders();
		if (headers == null) {
			return Collections.emptyEnumeration();
		}
		Collection<HttpString> headerNames = headers.getHeaderNames();
		if (headerNames == null) {
			return Collections.emptyEnumeration();
		}
		ArrayList<String> names = new ArrayList<String>();
		for (HttpString name : headerNames) {
			names.add(name.toString());
		}
		return Collections.enumeration(names);
	}

	@Override
	public String getMethod() {
		HttpString method = exchange.getRequestMethod();
		if (method == null) {
			return null;
		}
		return method.toString();
	}

	@Override
	public String getQueryString() {
		return exchange.getQueryString();
	}

	@Override
	public String getContextPath() {
		return null;
	}
	
	@Override
	public String getContentType() {
		return getHeader(Headers.CONTENT_TYPE_STRING);
	}

	@Override
	public String getCharacterEncoding() {
		return getHeader(Headers.CONTENT_ENCODING_STRING);
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, Deque<String>> parameters = exchange.getQueryParameters();
		if (parameters == null) {
			return Collections.emptyMap();
		}
		Map<String, String[]> res = new HashMap<>();
		for (Entry<String, Deque<String>> entry : parameters.entrySet()) {
			Deque<String> value = entry.getValue();
			res.put(entry.getKey(), value.toArray(new String[value.size()]));
		}
		return res;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return null;
	}

	@Override
	public String getRemoteHost() {
		return null;
	}

	@Override
	public void removeAttribute(String name) {
		RequestAttributes attributes = exchange.getAttachment(REQUEST_ATTRIBUTES_KEY);
		if (attributes != null) {
			attributes.remove(name);
		}
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return Collections.emptyEnumeration();
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	@Override
	public String getRealPath(String path) {
		return null;
	}

	@Override
	public int getRemotePort() {
		return 0;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return null;
	}

	@Override
	public int getLocalPort() {
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public boolean isAsyncSupported() {
		return true;
	}

	@Override
	public DispatcherType getDispatcherType() {
		return null;
	}

	@Override
	public String getAuthType() {
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		return null;
	}

	@Override
	public long getDateHeader(String name) {
		String value = getHeader(name);
		try {
			long res = Long.parseLong(value);
			return res;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public int getIntHeader(String name) {
		String value = getHeader(name);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public String getPathInfo() {
		return null;
	}

	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		String url = exchange.getRequestURL();
		if (url == null) {
			return null;
		}
		return new StringBuffer(url);
	}

	@Override
	public String getServletPath() {
		return null;
	}

	@Override
	public HttpSession getSession(boolean create) {
		return null;
	}

	@Override
	public HttpSession getSession() {
		return null;
	}

	@Override
	public String changeSessionId() {
		return null;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException {
	}

	@Override
	public void logout() throws ServletException {
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		return null;
	}

}
