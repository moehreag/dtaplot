/* Utilities used to manipulate classes

 Copyright (c) 2003-2018 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.util;

import java.io.IOException;
import java.net.URL;

///////////////////////////////////////////////////////////////////
//// ClassUtilities

/**
 A collection of utilities for manipulating classes.
 These utilities do not depend on any other ptolemy.* packages.


 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)
 */
public class ClassUtilities {
    /** Instances of this class cannot be created.
     */
    private ClassUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Lookup a jar URL and return the resource.

     *  A resource is a file such as a class file or image file that
     *  is found in the classpath.  A jar URL is a URL that refers to
     *  a resource in a jar file.  For example,
     *  <code>file://./foo.jar!/a/b/c.class</code> is a jar URL that
     *  refers to the <code>a/b/c.class</code> resource in
     *  <code>foo.jar</code>.  If this method is called with
     *  <code>file://./foo.jar!/a/b/c.class</code> then it will return
     *  <code>a/b/c.class</code> if <code>a/b/c.class</code> can be
     *  found as a resource in the class loader that loaded this class
     *  (ptolemy.util.ClassUtilities).  If the resource cannot be found,
     *  then an IOException is thrown. If the jarURLString parameter
     *  does not contain <code>!/</code>, then return null.
     *  Note that everything before the <code>!/</code> is removed before
     *  searching the classpath.
     *
     *  <p>This method is necessary because Web Start uses jar URL, and
     *  there are some cases where if we have a jar URL, then we may
     *  need to strip off the jar:<i>url</i>!/ part so that we can
     *  search for the {entry} as a resource.
     *
     *  @param jarURLString The string containing the jar URL.
     *  If no resource is found and the string contains a "#" then the text
     *  consisting of the # and the remaining text is removed and the shorter
     *  string is used as a search pattern.
     *  @return The resource, if any.  If the spec string does not
     *  contain <code>!/</code>, then return null.
     *  @exception IOException If this method cannot convert the specification
     *  to a URL.
     *  @see java.net.JarURLConnection
     */
    public static URL jarURLEntryResource(String jarURLString)
            throws IOException {
        // At first glance, it would appear that this method could appear
        // in specToURL(), but the problem is that specToURL() creates
        // a new URL with the spec, so it only does further checks if
        // the URL is malformed.  Unfortunately, in Web Start applications
        // the URL will often refer to a resource in another jar file,
        // which means that the jar url is not malformed, but there is
        // no resource by that name.  Probably specToURL() should return
        // the resource after calling new URL().
        int jarEntry = jarURLString.indexOf("!/");

        if (jarEntry == -1) {
            jarEntry = jarURLString.indexOf("!\\");

            if (jarEntry == -1) {
                return null;
            }
        }

        try {
            // !/ means that this could be in a jar file.
            String entry = jarURLString.substring(jarEntry + 2);

            // We might be in the Swing Event thread, so
            // Thread.currentThread().getContextClassLoader()
            // .getResource(entry) probably will not work.
            Class<?> refClass = Class.forName("ptolemy.util.ClassUtilities");
            URL entryURL = refClass.getClassLoader().getResource(entry);
            if (entryURL == null && entry.contains("#")) {
                // If entry contains a #, then strip it off and try again.
                entryURL = refClass.getClassLoader()
                        .getResource(entry.substring(0, entry.indexOf("#")));
            }
            return entryURL;
        } catch (Exception ex) {
            // IOException constructor does not take a cause, so we add it.
            IOException ioException = new IOException(
                    "Cannot find \"" + jarURLString + "\".");
            ioException.initCause(ex);
            throw ioException;
        }
    }

    /** Get the resource.
     *  If the current thread has a non-null context class loader,
     *  then use it to get the resource.  Othewise, get the
     *  NamedObj class and use that to get the resource.
     *  This is necessary because Thread.currentThread() can return null.
     *  @param spec The string to be found as a resource.
     *  @return The URL
     */
    public static URL getResource(String spec) {
        URL url = null;
        // Unfortunately, Thread.currentThread().getContextClassLoader() can
        // return null.  This happened when
        // $CLASSPATH/ptolemy/actor/lib/vertx/demo/TokenTransmissionTime/Sender.xml
        // failed and subsequent calls to ConfigurationApplication.openModelOrEntity() would
        // fail because the configuration could not be found.
        // So, we have our own getResource() that handles this.

        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        if (classLoader != null) {
            url = classLoader.getResource(spec);
        } else {
            classLoader = ClassLoader.getSystemClassLoader();
            if (classLoader != null) {
                url = classLoader.getResource(spec);
            } else {
                try {
                    Class<?> refClass = Class
                            .forName("ptolemy.util.ClassUtilities");
                    url = refClass.getClassLoader().getResource(spec);
                } catch (Exception ex) {
                    throw new RuntimeException(
                            "Failed to get system class loader"
                                    + " and failed to get the Class for ClassUtilities",
                            ex);
                }
            }
        }
        return url;
    }
}