/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.integration.dispatcher;

import java.util.List;
import java.util.UUID;

import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.integration.message.MessageHandler;

/**
 * A broadcasting dispatcher implementation. It makes a best effort to
 * send the message to each of its handlers. If it fails to send to any
 * one handler, it will log a warn-level message but continue to send
 * to the other handlers.
 * 
 * @author Mark Fisher
 * @author Iwein Fuld
 */
public class BroadcastingDispatcher extends AbstractDispatcher {

	private volatile boolean applySequence;

	private final TaskExecutor taskExecutor;


	public BroadcastingDispatcher() {
		this.taskExecutor = null;
	}

	public BroadcastingDispatcher(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}


	/**
	 * Specify whether to apply sequence numbers to the messages
	 * prior to sending to the handlers. By default, sequence
	 * numbers will <em>not</em> be applied
	 */
	public void setApplySequence(boolean applySequence) {
		this.applySequence = applySequence;
	}


	public boolean dispatch(Message<?> message) {
		int sequenceNumber = 1;
		List<MessageHandler> handlers = this.getHandlers();
		int sequenceSize = handlers.size();
		for (final MessageHandler handler : handlers) {
			final Message<?> messageToSend = (!this.applySequence) ? message
				: MessageBuilder.fromMessage(message)
						.setSequenceNumber(sequenceNumber++)
						.setSequenceSize(sequenceSize)
						.setCorrelationId(message.getHeaders().getId())
						.setHeader(MessageHeaders.ID, UUID.randomUUID())
						.build();
			if (this.taskExecutor != null) {
				this.taskExecutor.execute(new Runnable() {
					public void run() {
						BroadcastingDispatcher.this.sendMessageToHandler(messageToSend, handler);
					}
				});
			}
			else {
				this.sendMessageToHandler(messageToSend, handler);
			}
		}
		return true;
	}

}
