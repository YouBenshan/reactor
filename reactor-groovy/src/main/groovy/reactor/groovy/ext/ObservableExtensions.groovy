/*
 * Copyright (c) 2011-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package reactor.groovy.ext

import groovy.transform.CompileStatic
import reactor.core.Reactor
import reactor.fn.Event
import reactor.fn.Selector
import reactor.fn.Supplier
import reactor.groovy.ClosureConsumer

import static reactor.Fn.$
import static reactor.core.R.get

/**
 * Extensions for providing syntax suger for working with {@link reactor.fn.Observable}s.
 *
 * @author Stephane Maldini
 * @author Jon Brisbin
 */
@CompileStatic
class ObservableExtensions {
	static final String ARG_DATA = 'data'
	static final String ARG_TOPIC = 'for'

	static Reactor toReactor(String self) {
		get self
	}

	static reactor.fn.Observable on(reactor.fn.Observable selfType,
																	Selector selector,
																	@DelegatesTo(value = ClosureConsumer, strategy = Closure.DELEGATE_FIRST) Closure handler) {
		selfType.on selector, new ClosureConsumer(handler)
		selfType
	}

	static reactor.fn.Observable on(reactor.fn.Observable selfType,
																	String selector,
																	@DelegatesTo(value = ClosureConsumer, strategy = Closure.DELEGATE_FIRST) Closure handler) {
		selfType.on $(selector), new ClosureConsumer(handler)
		selfType
	}

	static reactor.fn.Observable on(reactor.fn.Observable selfType,
																	@DelegatesTo(value = ClosureConsumer, strategy = Closure.DELEGATE_FIRST) Closure handler) {
		//selfType.on T(handler?.parameterTypes[0]), new ClosureConsumer(handler)
		selfType.on new ClosureConsumer(handler)
		selfType
	}

	static <T> reactor.fn.Observable notify(reactor.fn.Observable selfType,
	                                        Object key,
	                                        T obj = null) {
		Event<T> toSend = coerce(obj)
		selfType.notify key, toSend
		selfType
	}

	static <T> reactor.fn.Observable notify(reactor.fn.Observable selfType,
																					String key,
																					Closure supplier) {
		Event<T> toSend = coerce(supplier)
		selfType.notify key, toSend
		selfType
	}

	static <T> reactor.fn.Observable leftShift(final reactor.fn.Observable selfType, final T obj) {
		Event<T> toSend = coerce(obj)
		//selfType.notify T(toSend.data.class), toSend
		selfType.notify toSend
		selfType
	}

	static reactor.fn.Observable notify(final reactor.fn.Observable selfType, final Map<String, ?> params) {
		Object topic = params.remove ARG_TOPIC

		def toSend
		if (params) {
			toSend = new Event(new Event.Headers(), params.remove(ARG_DATA))
			for (entry in params.entrySet()) {
				toSend.headers.set entry.key, entry.value?.toString()
			}
		} else {
			toSend = new Event(params.remove(ARG_DATA))
		}

		selfType.notify topic, toSend
		selfType
	}

	private static <T> Event<T> coerce(Object obj) {
		if (!obj) {
			(Event<T>)new Event<Void>(null)
		}else if (obj instanceof Event) {
			(Event<T>)obj
		} else if (obj instanceof Supplier) {
			(Event<T>)obj.get()
		} else if (obj instanceof Closure) {
			new Event<T>((T)obj.call())
		} else {
			new Event<T>((T)obj)
		}
	}
}
