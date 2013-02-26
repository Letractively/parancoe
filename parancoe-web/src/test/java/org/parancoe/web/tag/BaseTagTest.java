/**
 * Copyright (C) 2006-2010 The Parancoe Team <info@parancoe.org>
 *
 * This file is part of Parancoe Web.
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
package org.parancoe.web.tag;

import org.junit.Before;
import org.parancoe.web.AbstractContextTest;
import org.springframework.web.util.WebUtils;

/**
 *
 * @author michele franzin <michele at franzin.net>
 */
public abstract class BaseTagTest extends AbstractContextTest {

    @Before
    public void setupRequestParams() {
        request.setContextPath("/testctx");
        request.setRequestURI("/testctx/test/request/uri");
        request.setAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE,
                "/testctx/test/forward/request/uri");
        request.setQueryString("p1=v1&p2=v2&p3=v3");
    }
}
