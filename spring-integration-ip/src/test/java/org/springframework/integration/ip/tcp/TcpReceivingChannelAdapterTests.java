/*
 * Copyright 2002-2010 the original author or authors.
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
package org.springframework.integration.ip.tcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.SocketFactory;

import org.junit.Test;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.Message;
import org.springframework.integration.ip.AbstractInternetProtocolReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.tcp.converter.ByteArrayCrLfConverter;
import org.springframework.integration.ip.util.SocketUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author Gary Russell
 *
 */
public class TcpReceivingChannelAdapterTests {

	/**
	 * Test method for {@link org.springframework.integration.ip.tcp.TcpNetReceivingChannelAdapter#run()}.
	 */
	@Test
	public void testNet() throws Exception {
		QueueChannel channel = new QueueChannel(2);
		int port = SocketUtils.findAvailableServerSocket();
		AbstractInternetProtocolReceivingChannelAdapter adapter = new TcpNetReceivingChannelAdapter(port);
		adapter.setOutputChannel(channel);
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
		adapter.setTaskScheduler(taskScheduler);
		SocketUtils.setLocalNicIfPossible(adapter);
		adapter.start();
		SocketUtils.waitListening(adapter);
		SocketUtils.testSendLength(port, null); //sends 2 copies of TEST_STRING twice
		Message<?> message = channel.receive(2000);
		assertNotNull(message);
		assertEquals(SocketUtils.TEST_STRING + SocketUtils.TEST_STRING, 
				new String((byte[])message.getPayload()));
		message = channel.receive(2000);
		assertNotNull(message);
		assertEquals(SocketUtils.TEST_STRING + SocketUtils.TEST_STRING, 
				new String((byte[])message.getPayload()));
		adapter.stop();
	}

	/**
	 * Test method for {@link org.springframework.integration.ip.tcp.TcpNetReceivingChannelAdapter#run()}.
	 * Verifies operation of custom message formats.
	 */
	@Test
	public void testNetCustom() throws Exception {
		QueueChannel channel = new QueueChannel(2);
		int port = SocketUtils.findAvailableServerSocket();
		TcpNetReceivingChannelAdapter adapter = new TcpNetReceivingChannelAdapter(port);
		adapter.setOutputChannel(channel);
		adapter.setCustomSocketReaderClassName("org.springframework.integration.ip.tcp.CustomNetSocketReader");
		adapter.setMessageFormat(MessageFormats.FORMAT_CUSTOM);
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
		adapter.setTaskScheduler(taskScheduler);
		adapter.start();
		SocketUtils.waitListening(adapter);
		SocketUtils.testSendStxEtx(port, null); //sends 2 copies of TEST_STRING twice
		Message<?> message = channel.receive(4000);
		assertNotNull(message);
		assertEquals("\u0002" + SocketUtils.TEST_STRING + SocketUtils.TEST_STRING + "\u0003", 
				new String((byte[])message.getPayload()));
		message = channel.receive(2000);
		assertNotNull(message);
		assertEquals("\u0002" + SocketUtils.TEST_STRING + SocketUtils.TEST_STRING + "\u0003", 
				new String((byte[])message.getPayload()));
		adapter.stop();
	}


	/**
	 * Test method for {@link org.springframework.integration.ip.tcp.TcpNioReceivingChannelAdapter#run()}.
	 */
	@Test
	public void testNio() throws Exception {
		QueueChannel channel = new QueueChannel(2);
		int port = SocketUtils.findAvailableServerSocket();
		TcpNioReceivingChannelAdapter adapter = new TcpNioReceivingChannelAdapter(port);
		adapter.setOutputChannel(channel);
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
		adapter.setTaskScheduler(taskScheduler);
		SocketUtils.setLocalNicIfPossible(adapter);
		adapter.start();
		SocketUtils.waitListening(adapter);
		SocketUtils.testSendLength(port, null); //sends 2 copies of TEST_STRING twice
		Message<?> message = channel.receive(2000);
		assertNotNull(message);
		assertEquals(SocketUtils.TEST_STRING + SocketUtils.TEST_STRING, 
				new String((byte[])message.getPayload()));
		message = channel.receive(2000);
		assertNotNull(message);
		assertEquals(SocketUtils.TEST_STRING + SocketUtils.TEST_STRING, 
				new String((byte[])message.getPayload()));
		adapter.stop();
	}

	/**
	 * Test method for {@link org.springframework.integration.ip.tcp.TcpNioReceivingChannelAdapter#run()}.
	 * Verifies operation of custom message formats.	 */
	@Test
	public void testNioCustom() throws Exception {
		QueueChannel channel = new QueueChannel(2);
		int port = SocketUtils.findAvailableServerSocket();
		TcpNioReceivingChannelAdapter adapter = new TcpNioReceivingChannelAdapter(port);
		adapter.setOutputChannel(channel);
		adapter.setCustomSocketReaderClassName("org.springframework.integration.ip.tcp.CustomNioSocketReader");
		adapter.setMessageFormat(MessageFormats.FORMAT_CUSTOM);
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
		adapter.setTaskScheduler(taskScheduler);
		adapter.start();
		SocketUtils.waitListening(adapter);
		SocketUtils.testSendStxEtx(port, null); //sends 2 copies of TEST_STRING twice
		Message<?> message = channel.receive(2000);
		assertNotNull(message);
		assertEquals("\u0002" + SocketUtils.TEST_STRING + SocketUtils.TEST_STRING + "\u0003", 
				new String((byte[])message.getPayload()));
		message = channel.receive(2000);
		assertNotNull(message);
		assertEquals("\u0002" + SocketUtils.TEST_STRING + SocketUtils.TEST_STRING + "\u0003", 
				new String((byte[])message.getPayload()));
		adapter.stop();
	}

	/**
	 * Tests close option on inbound adapter.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNetClose() throws Exception {
		QueueChannel channel = new QueueChannel(2);
		int port = SocketUtils.findAvailableServerSocket();
		AbstractTcpReceivingChannelAdapter adapter = new TcpNetReceivingChannelAdapter(port);
		adapter.setOutputChannel(channel);
		adapter.setClose(true);
		adapter.setMessageFormat(MessageFormats.FORMAT_CRLF);
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
		adapter.setTaskScheduler(taskScheduler);
		adapter.start();
		SocketUtils.waitListening(adapter);
		CountDownLatch latch = new CountDownLatch(1);
		SocketUtils.testSendCrLfSingle(port, latch); 
		Message<?> message = channel.receive(5000);
		latch.countDown();
		assertNotNull(message);
		assertEquals(SocketUtils.TEST_STRING + SocketUtils.TEST_STRING, 
				new String((byte[])message.getPayload()));
		latch = new CountDownLatch(1);
		SocketUtils.testSendCrLfSingle(port, latch); 
		message = channel.receive(5000);
		latch.countDown();
		assertNotNull(message);
		assertEquals(SocketUtils.TEST_STRING + SocketUtils.TEST_STRING, 
				new String((byte[])message.getPayload()));
		adapter.stop();
	}

	/**
	 * Tests close option on inbound adapter.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNioClose() throws Exception {
		QueueChannel channel = new QueueChannel(2);
		int port = SocketUtils.findAvailableServerSocket();
		AbstractTcpReceivingChannelAdapter adapter = new TcpNioReceivingChannelAdapter(port);
		adapter.setOutputChannel(channel);
		adapter.setClose(true);
		adapter.setMessageFormat(MessageFormats.FORMAT_CRLF);
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
		adapter.setTaskScheduler(taskScheduler);
		adapter.start();
		SocketUtils.waitListening(adapter);
		CountDownLatch latch = new CountDownLatch(1);
		SocketUtils.testSendCrLfSingle(port, latch); 
		Message<?> message = channel.receive(2000);
		assertNotNull(message);
		assertEquals(SocketUtils.TEST_STRING + SocketUtils.TEST_STRING, 
				new String((byte[])message.getPayload()));
		latch = new CountDownLatch(1);
		SocketUtils.testSendCrLfSingle(port, latch); 
		message = channel.receive(2000);
		assertNotNull(message);
		assertEquals(SocketUtils.TEST_STRING + SocketUtils.TEST_STRING, 
				new String((byte[])message.getPayload()));
		adapter.stop();
	}
	
	@Test
	public void newTestNet() throws Exception {
		final int port = SocketUtils.findAvailableServerSocket();
		AbstractServerConnectionFactory scf = new TcpNetServerConnectionFactory(port);
		ByteArrayCrLfConverter converter = new ByteArrayCrLfConverter();
		scf.setInputConverter(converter);
		scf.setOutputConverter(converter);
		TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
		adapter.setConnectionFactory(scf);
		scf.start();
		int n = 0;
		while (!scf.isListening()) {
			Thread.sleep(100);
			if (n++ > 100) {
				fail("Failed to start listening");
			}
		}
		QueueChannel channel = new QueueChannel();
		adapter.setOutputChannel(channel);
		Socket socket = SocketFactory.getDefault().createSocket("localhost", port);
		socket.getOutputStream().write("Test1\r\n".getBytes());
		socket.getOutputStream().write("Test2\r\n".getBytes());
		Message<?> message = channel.receive(10000);
		assertNotNull(message);
		assertEquals("Test1", new String((byte[]) message.getPayload()));
		message = channel.receive(10000);
		assertNotNull(message);
		assertEquals("Test2", new String((byte[]) message.getPayload()));
	}

	@Test
	public void newTestNio() throws Exception {
		final int port = SocketUtils.findAvailableServerSocket();
		TcpNioServerConnectionFactory scf = new TcpNioServerConnectionFactory(port);
		ByteArrayCrLfConverter converter = new ByteArrayCrLfConverter();
		scf.setInputConverter(converter);
		scf.setOutputConverter(converter);		
		scf.setSoTimeout(5000);
		TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
		adapter.setConnectionFactory(scf);
		scf.start();
		int n = 0;
		while (!scf.isListening()) {
			Thread.sleep(100);
			if (n++ > 100) {
				fail("Failed to start listening");
			}
		}
		QueueChannel channel = new QueueChannel();
		adapter.setOutputChannel(channel);
		Socket socket = SocketFactory.getDefault().createSocket("localhost", port);
		for (int i = 0; i < 100; i++) {
			socket.getOutputStream().write(("Test" + i + "\r\n").getBytes());
//			if (i % 10 == 0) {
//				Thread.sleep(1000);
//			}
		}			
		for (int i = 0; i < 100; i++) {
			Message<?> message = channel.receive(10000);
			assertNotNull(message);
			assertEquals("Test" + i, new String((byte[]) message.getPayload()));
		}
	}

	@Test
	public void newTestNetShared() throws Exception {
		final int port = SocketUtils.findAvailableServerSocket();
		AbstractServerConnectionFactory scf = new TcpNetServerConnectionFactory(port);
		ByteArrayCrLfConverter converter = new ByteArrayCrLfConverter();
		scf.setInputConverter(converter);
		scf.setOutputConverter(converter);
		TcpSendingMessageHandler handler = new TcpSendingMessageHandler();
		handler.setConnectionFactory(scf);
		TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
		adapter.setConnectionFactory(scf);
		scf.start();
		QueueChannel channel = new QueueChannel();
		adapter.setOutputChannel(channel);
		int n = 0;
		while (!scf.isListening()) {
			Thread.sleep(100);
			if (n++ > 100) {
				fail("Failed to listen");
			}
		}
		Socket socket = SocketFactory.getDefault().createSocket("localhost", port);
		socket.setSoTimeout(2000);
		socket.getOutputStream().write("Test\r\n".getBytes());
		socket.getOutputStream().write("Test\r\n".getBytes());
		Message<?> message = channel.receive(10000);
		assertNotNull(message);
		handler.handleMessage(message);
		message = channel.receive(10000);
		assertNotNull(message);
		handler.handleMessage(message);		
		byte[] b = new byte[6];
		readFully(socket.getInputStream(), b);
		assertEquals("Test\r\n", new String(b));
		readFully(socket.getInputStream(), b);
		assertEquals("Test\r\n", new String(b));
	}
	
	@Test
	public void newTestNioShared() throws Exception {
		final int port = SocketUtils.findAvailableServerSocket();
		TcpNioServerConnectionFactory scf = new TcpNioServerConnectionFactory(port);
		ByteArrayCrLfConverter converter = new ByteArrayCrLfConverter();
		scf.setInputConverter(converter);
		scf.setOutputConverter(converter);
		TcpSendingMessageHandler handler = new TcpSendingMessageHandler();
		handler.setConnectionFactory(scf);
		TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
		adapter.setConnectionFactory(scf);
		scf.start();
		QueueChannel channel = new QueueChannel();
		adapter.setOutputChannel(channel);
		int n = 0;
		while (!scf.isListening()) {
			Thread.sleep(100);
			if (n++ > 100) {
				fail("Failed to listen");
			}
		}
		Socket socket = SocketFactory.getDefault().createSocket("localhost", port);
		socket.setSoTimeout(2000);
		socket.getOutputStream().write("Test\r\n".getBytes());
		socket.getOutputStream().write("Test\r\n".getBytes());
		Message<?> message = channel.receive(10000);
		assertNotNull(message);
		handler.handleMessage(message);
		message = channel.receive(10000);
		assertNotNull(message);
		handler.handleMessage(message);		
		byte[] b = new byte[6];
		readFully(socket.getInputStream(), b);
		assertEquals("Test\r\n", new String(b));
		readFully(socket.getInputStream(), b);
		assertEquals("Test\r\n", new String(b));
	}
	
	@Test
	public void newTestNetSingleNoOutbound() throws Exception {
		final int port = SocketUtils.findAvailableServerSocket();
		AbstractServerConnectionFactory scf = new TcpNetServerConnectionFactory(port);
		ByteArrayCrLfConverter converter = new ByteArrayCrLfConverter();
		scf.setInputConverter(converter);
		scf.setOutputConverter(converter);
		scf.setSingleUse(true);
		TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
		adapter.setConnectionFactory(scf);
		scf.start();
		int n = 0;
		while (!scf.isListening()) {
			Thread.sleep(100);
			if (n++ > 100) {
				fail("Failed to start listening");
			}
		}
		QueueChannel channel = new QueueChannel();
		adapter.setOutputChannel(channel);
		Socket socket = SocketFactory.getDefault().createSocket("localhost", port);
		socket.getOutputStream().write("Test1\r\n".getBytes());
		socket = SocketFactory.getDefault().createSocket("localhost", port);
		socket.getOutputStream().write("Test2\r\n".getBytes());
		Message<?> message = channel.receive(10000);
		assertNotNull(message);
		// with single use, results may come back in a different order
		Set<String> results = new HashSet<String>();
		results.add(new String((byte[]) message.getPayload()));
		message = channel.receive(10000);
		assertNotNull(message);
		results.add(new String((byte[]) message.getPayload()));
		assertTrue(results.contains("Test1"));
		assertTrue(results.contains("Test2"));
	}

	@Test
	public void newTestNioSingleNoOutbound() throws Exception {
		final int port = SocketUtils.findAvailableServerSocket();
		TcpNioServerConnectionFactory scf = new TcpNioServerConnectionFactory(port);
		ByteArrayCrLfConverter converter = new ByteArrayCrLfConverter();
		scf.setInputConverter(converter);
		scf.setOutputConverter(converter);
		scf.setSingleUse(true);
		TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
		adapter.setConnectionFactory(scf);
		scf.start();
		int n = 0;
		while (!scf.isListening()) {
			Thread.sleep(100);
			if (n++ > 100) {
				fail("Failed to start listening");
			}
		}
		QueueChannel channel = new QueueChannel();
		adapter.setOutputChannel(channel);
		Socket socket = SocketFactory.getDefault().createSocket("localhost", port);
		socket.getOutputStream().write("Test1\r\n".getBytes());
		socket = SocketFactory.getDefault().createSocket("localhost", port);
		socket.getOutputStream().write("Test2\r\n".getBytes());
		Message<?> message = channel.receive(10000);
		assertNotNull(message);
		// with single use, results may come back in a different order
		Set<String> results = new HashSet<String>();
		results.add(new String((byte[]) message.getPayload()));
		message = channel.receive(10000);
		assertNotNull(message);
		results.add(new String((byte[]) message.getPayload()));
		assertTrue(results.contains("Test1"));
		assertTrue(results.contains("Test2"));
	}

	/**
	 * @param is
	 * @param buff
	 */
	private void readFully(InputStream is, byte[] buff) throws IOException {
		for (int i = 0; i < buff.length; i++) {
			buff[i] = (byte) is.read();
		}
	}

	@Test
	public void newTestNetSingleShared() throws Exception {
		final int port = SocketUtils.findAvailableServerSocket();
		AbstractServerConnectionFactory scf = new TcpNetServerConnectionFactory(port);
		ByteArrayCrLfConverter converter = new ByteArrayCrLfConverter();
		scf.setInputConverter(converter);
		scf.setOutputConverter(converter);
		scf.setSingleUse(true);
		TcpSendingMessageHandler handler = new TcpSendingMessageHandler();
		handler.setConnectionFactory(scf);
		TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
		adapter.setConnectionFactory(scf);
		scf.start();
		QueueChannel channel = new QueueChannel();
		adapter.setOutputChannel(channel);
		int n = 0;
		while (!scf.isListening()) {
			Thread.sleep(100);
			if (n++ > 100) {
				fail("Failed to listen");
			}
		}
		Socket socket1 = SocketFactory.getDefault().createSocket("localhost", port);
		socket1.setSoTimeout(2000);
		socket1.getOutputStream().write("Test1\r\n".getBytes());
		Socket socket2 = SocketFactory.getDefault().createSocket("localhost", port);
		socket2.setSoTimeout(2000);
		socket2.getOutputStream().write("Test2\r\n".getBytes());
		Message<?> message = channel.receive(10000);
		assertNotNull(message);
		handler.handleMessage(message);
		message = channel.receive(10000);
		assertNotNull(message);
		handler.handleMessage(message);		
		byte[] b = new byte[7];
		readFully(socket1.getInputStream(), b);
		assertEquals("Test1\r\n", new String(b));
		readFully(socket2.getInputStream(), b);
		assertEquals("Test2\r\n", new String(b));
	}
	
	@Test  
	public void newTestNioSingleShared() throws Exception {
		final int port = SocketUtils.findAvailableServerSocket();
		TcpNioServerConnectionFactory scf = new TcpNioServerConnectionFactory(port);
		ByteArrayCrLfConverter converter = new ByteArrayCrLfConverter();
		scf.setInputConverter(converter);
		scf.setOutputConverter(converter);
		scf.setSingleUse(true);
		TcpSendingMessageHandler handler = new TcpSendingMessageHandler();
		handler.setConnectionFactory(scf);
		TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
		adapter.setConnectionFactory(scf);
		scf.start();
		QueueChannel channel = new QueueChannel();
		adapter.setOutputChannel(channel);
		int n = 0;
		while (!scf.isListening()) {
			Thread.sleep(100);
			if (n++ > 100) {
				fail("Failed to listen");
			}
		}
		Socket socket1 = SocketFactory.getDefault().createSocket("localhost", port);
		socket1.setSoTimeout(2000);
		socket1.getOutputStream().write("Test1\r\n".getBytes());
		Socket socket2 = SocketFactory.getDefault().createSocket("localhost", port);
		socket2.setSoTimeout(2000);
		socket2.getOutputStream().write("Test2\r\n".getBytes());
		Message<?> message = channel.receive(10000);
		assertNotNull(message);
		handler.handleMessage(message);
		message = channel.receive(10000);
		assertNotNull(message);
		handler.handleMessage(message);		
		byte[] b = new byte[7];
		readFully(socket1.getInputStream(), b);
		assertEquals("Test1\r\n", new String(b));
		readFully(socket2.getInputStream(), b);
		assertEquals("Test2\r\n", new String(b));
	}
	
	@Test  
	public void newTestNioSingleSharedMany() throws Exception {
		final int port = SocketUtils.findAvailableServerSocket();
		TcpNioServerConnectionFactory scf = new TcpNioServerConnectionFactory(port);
		ByteArrayCrLfConverter converter = new ByteArrayCrLfConverter();
		scf.setInputConverter(converter);
		scf.setOutputConverter(converter);
		scf.setSingleUse(true);
		TcpSendingMessageHandler handler = new TcpSendingMessageHandler();
		handler.setConnectionFactory(scf);
		TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
		adapter.setConnectionFactory(scf);
		Executor te = Executors.newFixedThreadPool(100);
		scf.setTaskExecutor(te);
		scf.start();
		QueueChannel channel = new QueueChannel();
		adapter.setOutputChannel(channel);
		int n = 0;
		while (!scf.isListening()) {
			Thread.sleep(100);
			if (n++ > 100) {
				fail("Failed to listen");
			}
		}
		List<Socket> sockets = new LinkedList<Socket>();
		for (int i = 100; i < 200; i++) {
			Socket socket1 = SocketFactory.getDefault().createSocket("localhost", port);
			socket1.setSoTimeout(2000);
			socket1.getOutputStream().write(("Test" + i + "\r\n").getBytes());
			sockets.add(socket1);
		}
		for (int i = 100; i < 200; i++) {
			Message<?> message = channel.receive(10000);
			assertNotNull(message);
			handler.handleMessage(message);
		}
		byte[] b = new byte[9];
		for (int i = 100; i < 200; i++) { 
			readFully(sockets.remove(0).getInputStream(), b);
			assertEquals("Test" + i + "\r\n", new String(b));
		}
	}
	
	

}