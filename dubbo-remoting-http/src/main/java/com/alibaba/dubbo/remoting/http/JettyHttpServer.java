/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.remoting.http;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;

public class JettyHttpServer implements HttpServer {

	private static final Logger logger = LoggerFactory.getLogger(JettyHttpServer.class);

	private String host;
	
	private int port;
	
	private int threads;
	
	private Server server;

    public JettyHttpServer(int port, int threads) {
        this.port = port;
        this.threads = threads;
    }
    
    public JettyHttpServer(String host, int port, int threads) {
        this.host = host;
        this.port = port;
        this.threads = threads;
    }
	
	public void start() {
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setDaemon(true);
		threadPool.setMaxThreads(threads);
		threadPool.setMinThreads(threads);
		
		SelectChannelConnector connector = new SelectChannelConnector();
		if (NetUtils.isValidLocalHost(host)) {
			connector.setHost(host);
		}
        connector.setPort(port);
        
        ServletHandler handler = new ServletHandler();
        ServletHolder holder = handler.addServletWithMapping(ServiceDispatcherServlet.class, "/*");
        holder.setInitOrder(1);
        
        server = new Server();
        server.setThreadPool(threadPool);
        server.addConnector(connector);
        server.addHandler(handler);
        try {
			server.start();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to start jetty server on " + host + ":" + port + ", cause: " + e.getMessage(), e);
		}
	}

	public void stop() {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

    public int getPort() {
        return port;
    }

}