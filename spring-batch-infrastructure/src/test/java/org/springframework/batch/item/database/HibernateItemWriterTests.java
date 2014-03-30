/*
 * Copyright 2006-2013 the original author or authors.
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
package org.springframework.batch.item.database;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.orm.hibernate3.HibernateOperations;

/**
 * @author Dave Syer
 * @author Thomas Risberg
 * @author Michael Minella
 * @author Will Schipp
 */
public class HibernateItemWriterTests {

	HibernateOperations ht;

	HibernateItemWriter<Object> writer;

	SessionFactory factory;
	Session currentSession;

	@Before
	public void setUp() throws Exception {
		writer = new HibernateItemWriter<Object>();
		ht = mock(HibernateOperations.class,"ht");
		factory = mock(SessionFactory.class);
		currentSession = mock(Session.class);
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.item.database.HibernateItemWriter#afterPropertiesSet()}
	 *
	 * @throws Exception
	 */
	@Test
	public void testAfterPropertiesSet() throws Exception {
		writer = new HibernateItemWriter<Object>();
		try {
			writer.afterPropertiesSet();
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalStateException e) {
			// expected
			assertTrue("Wrong message for exception: " + e.getMessage(), e.getMessage().indexOf("HibernateOperations") >= 0);
		}
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.item.database.HibernateItemWriter#afterPropertiesSet()}
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testAfterPropertiesSetWithDelegate() throws Exception {
		writer.setHibernateTemplate(ht);
		writer.afterPropertiesSet();
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testWriteAndFlushSunnyDayHibernate3() throws Exception {
		writer.setHibernateTemplate(ht);
		when(ht.contains("foo")).thenReturn(true);
		when(ht.contains("bar")).thenReturn(false);
		ht.saveOrUpdate("bar");
		ht.flush();
		ht.clear();

		List<String> items = Arrays.asList(new String[] { "foo", "bar" });
		writer.write(items);

	}

	@SuppressWarnings("deprecation")
	@Test
	public void testWriteAndFlushWithFailureHibernate3() throws Exception {
		writer.setHibernateTemplate(ht);
		final RuntimeException ex = new RuntimeException("ERROR");
		when(ht.contains("foo")).thenThrow(ex);

		try {
			writer.write(Collections.singletonList("foo"));
			fail("Expected RuntimeException");
		}
		catch (RuntimeException e) {
			assertEquals("ERROR", e.getMessage());
		}

	}

	@Test
	public void testWriteAndFlushSunnyDayHibernate4() throws Exception {
		writer.setSessionFactory(factory);
		when(factory.getCurrentSession()).thenReturn(currentSession);
		when(currentSession.contains("foo")).thenReturn(true);
		when(currentSession.contains("bar")).thenReturn(false);
		currentSession.saveOrUpdate("bar");
		currentSession.flush();
		currentSession.clear();

		List<String> items = Arrays.asList(new String[] { "foo", "bar" });
		writer.write(items);
	}

	@Test
	public void testWriteAndFlushWithFailureHibernate4() throws Exception {
		writer.setSessionFactory(factory);
		final RuntimeException ex = new RuntimeException("ERROR");

		when(factory.getCurrentSession()).thenReturn(currentSession);
		when(currentSession.contains("foo")).thenThrow(ex);

		try {
			writer.write(Collections.singletonList("foo"));
			fail("Expected RuntimeException");
		}
		catch (RuntimeException e) {
			assertEquals("ERROR", e.getMessage());
		}
	}
}
