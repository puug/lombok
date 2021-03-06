/*
 * Copyright (C) 2010-2012 The Project Lombok Authors.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.extern.slf4j;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Causes lombok to generate a logger field.
 * <p>
 * Complete documentation is found at <a href="http://projectlombok.org/features/Log.html">the project lombok features page for lombok log annotations</a>.
 * <p>
 * Example:
 * <pre>
 * &#64;Slf4j
 * public class LogExample {
 * }
 * </pre>
 * 
 * will generate:
 * 
 * <pre>
 * public class LogExample {
 *     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogExample.class);
 * }
 * </pre>
 * 
 * This annotation is valid for classes and enumerations.<br />
 * @see org.slf4j.Logger org.slf4j.Logger
 * @see org.slf4j.LoggerFactory#getLogger(java.lang.Class) org.slf4j.LoggerFactory.getLogger(Class target)
 * @see lombok.extern.apachecommons.CommonsLog &#64;CommonsLog
 * @see lombok.extern.java.Log &#64;Log
 * @see lombok.extern.log4j.Log4j &#64;Log4j
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Slf4j {
}
