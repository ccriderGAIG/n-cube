package com.cedarsoftware.util

import groovy.transform.CompileStatic

/**
 * Thread-aware PrintStream.  Use to separate different threads output
 * to System.output, System.err so that this output can be captured per
 * thread.
 *
 * @author John DeRegnaucourt (jdereg@gmail.com)
 *         <br/>
 *         Copyright (c) Cedar Software LLC
 *         <br/><br/>
 *         Licensed under the Apache License, Version 2.0 (the "License")
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br/><br/>
 *         http://www.apache.org/licenses/LICENSE-2.0
 *         <br/><br/>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
@CompileStatic
class ThreadAwarePrintStreamErr extends PrintStream
{
    private static final ThreadLocal<ByteArrayOutputStream> output = new ThreadLocal<ByteArrayOutputStream>() {
        ByteArrayOutputStream initialValue()
        {
            return new ByteArrayOutputStream()
        }
    }

    ThreadAwarePrintStreamErr()
    {
        super(output.get())
    }

    void write(int b)
    {
        output.get().write(b)
    }

    void write(byte[] buf, int off, int len)
    {
        output.get().write(buf, off, len)
    }

    static String getContent()
    {
        byte[] contents = output.get().toByteArray()
        output.get().reset()
        return StringUtilities.createString(contents, "UTF-8")
    }
}
