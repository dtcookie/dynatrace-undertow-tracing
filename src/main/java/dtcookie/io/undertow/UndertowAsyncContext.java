package dtcookie.io.undertow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;

public final class UndertowAsyncContext implements AsyncContext, ExchangeCompletionListener {
	
	private static final class AsyncListenerRecord {
		private final AsyncListener listener;
		private final ServletRequest request;
		private final ServletResponse response;
		
		private AsyncListenerRecord(AsyncListener listener, ServletRequest request, ServletResponse response) {
			this.listener = listener;
			this.request = request;
			this.response = response;
		}
	}
	
	private final ServletRequest request;
	private final ServletResponse response;
	private final List<AsyncListenerRecord> listeners = Collections.synchronizedList(new ArrayList<>());
	
	public UndertowAsyncContext(ServletRequest request, ServletResponse response) {
		
		this.request = request;
		this.response = response;
	}

	@Override
	public ServletRequest getRequest() {
		return request;
	}

	@Override
	public ServletResponse getResponse() {
		return response;
	}

	@Override
	public boolean hasOriginalRequestAndResponse() {
		return true;
	}

	@Override
	public void dispatch() {
	}

	@Override
	public void dispatch(String path) {
	}

	@Override
	public void dispatch(ServletContext context, String path) {
	}

	@Override
	public void complete() {
		if (listeners == null) {
			return;
		}
		for (AsyncListenerRecord record : listeners) {
			if (record == null) {
				continue;
			}
			try {
				record.listener.onComplete(new AsyncEvent(this, record.request, record.response));	
			} catch (IOException ioe) {
				// ignore
			}
		}
	}

	@Override
	public void start(Runnable run) {
	}

	@Override
	public void addListener(AsyncListener listener) {
		listeners.add(new AsyncListenerRecord(listener, getRequest(), getResponse()));
	}

	@Override
	public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
		listeners.add(new AsyncListenerRecord(listener, servletRequest, servletResponse));
	}

	@Override
	public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
		return null;
	}

	@Override
	public void setTimeout(long timeout) {
	}

	@Override
	public long getTimeout() {
		return 0;
	}

	@Override
	public void exchangeEvent(HttpServerExchange exchange, NextListener nextListener) {
		complete();
		if (nextListener != null) {
			nextListener.proceed();	
		}		
	}

}
