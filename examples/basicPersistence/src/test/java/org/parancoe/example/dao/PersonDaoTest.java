/**
 * Copyright (C) 2006-2013 The Parancoe Team <info@parancoe.org>
 *
 * This file is part of Parancoe Example - Basic Persistence.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.parancoe.example.dao;

import java.util.List;
import javax.annotation.Resource;
import org.parancoe.example.po.Person;
import org.parancoe.example.test.BaseTest;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import org.junit.Test;

/**
 *
 * @author lucio
 */
public class PersonDaoTest extends BaseTest {

    @Resource
    private PersonDao personDao;

    /**
     * Test of findByLastName method, of class PersonDao.
     */
    @Test
    public void findByLastName() {
        List<Person> people = personDao.findByLastName("Benfante");
        assertThat(people, hasSize(8));
        for (Person p : people) {
            assertThat(p.getLastName(), equalTo("Benfante"));
        }
    }

    @Test
    public void findByFirstName() {
        List<Person> people = personDao.findByFirstName("Ugo");
        assertThat(people, hasSize(2));
        for (Person p : people) {
            assertThat(p.getFirstName(), equalTo("Ugo"));
        }
    }
}
