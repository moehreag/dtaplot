/* Utilities used to manipulate files

 Copyright (c) 2004-2017 The Regents of the University of California.
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

// Avoid importing any packages from ptolemy.* here so that we
// can ship Ptplot.
///////////////////////////////////////////////////////////////////
//// FileUtilities

/**
 A collection of utilities for manipulating files
 These utilities do not depend on any other ptolemy.* packages.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)
 */
public class FileUtilities {
    /** Instances of this class cannot be created.
     */
    private FileUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given a URL, if it starts with http, the follow up to 10 redirects.
     *
     *  <p>If the URL is null or does not start with "http", then return the
     *  URL.</p>
     *
     *  @param url The URL to be followed.
     *  @return The new URL if any.
     *  @exception IOException If there is a problem opening the URL or
     *  if there are more than 10 redirects.
     */
    public static URL followRedirects(URL url) throws IOException {

        if (url == null || !url.getProtocol().startsWith("http")) {
            return url;
        }
        URL temporaryURL = url;
        int count;
        for (count = 0; count < 10; count++) {
            HttpURLConnection connection = (HttpURLConnection) temporaryURL
                    .openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setInstanceFollowRedirects(false);

            switch (connection.getResponseCode()) {
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_MOVED_TEMP:
                String location = connection.getHeaderField("Location");
                // Handle relative URLs.
                temporaryURL = new URL(temporaryURL, location);
                continue;
            }

            connection.disconnect();
            return temporaryURL;
        }
        throw new IOException("Failed to resolve " + url
                + " after 10 attempts.  The last url was " + temporaryURL);

    }

    /** Given a file or URL name, return as a URL.  If the file name
     *  is relative, then it is interpreted as being relative to the
     *  specified base directory. If the name begins with
     *  "xxxxxxCLASSPATHxxxxxx" or "$CLASSPATH" then search for the
     *  file relative to the classpath.
     *
     *  <p>Note that "xxxxxxCLASSPATHxxxxxx" is the value of the
     *  globally defined constant $CLASSPATH available in the Ptolemy
     *  II expression language.
     *  II expression language.
     *
     *  <p>If no file is found, then throw an exception.
     *
     *  <p>This method is similar to {@link #nameToFile(String, URI)}
     *  except that in this method, the file or URL must be readable.
     *  Usually, this method is use for reading a file and
     *  is used for writing {@link #nameToFile(String, URI)}.
     *
     *  @param name The name of a file or URL.
     *  @param baseDirectory The base directory for relative file names,
     *   or null to specify none.
     *  @param classLoader The class loader to use to locate system
     *   resources, or null to use the system class loader that was used
     *   to load this class.
     *  @return A URL, or null if the name is null or the empty string.
     *  @exception IOException If the file cannot be read, or
     *   if the file cannot be represented as a URL (e.g. System.in), or
     *   the name specification cannot be parsed.
     *  @see #nameToFile(String, URI)
     */
    public static URL nameToURL(String name, URI baseDirectory,
            ClassLoader classLoader) throws IOException {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        if (name.startsWith(_CLASSPATH_VALUE)
                || name.startsWith("$CLASSPATH")) {
            if (name.contains("#")) {
                name = name.substring(0, name.indexOf("#"));
            }

            URL result = _searchClassPath(name, classLoader);
            if (result == null) {
                throw new IOException("Cannot find file '"
                        + _trimClassPath(name) + "' in classpath");
            }

            return result;
        }

        File file = new File(name);

        // Be careful here, we need to be sure that we are reading
        // relative to baseDirectory if baseDirectory is not null.

        // The security tests rely on baseDirectory, to replicate:
        // (cd $PTII/ptII/ptolemy/actor/lib/security/test; rm rm foo.keystore auto/foo.keystore; make)

        if (file.isAbsolute() || (file.canRead() && baseDirectory == null)) {
            // If the URL has a "fragment" (also called a reference), which is
            // a pointer into the file, we have to strip that off before we
            // get the file, and the reinsert it before returning the URL.
            String fragment = null;
            if (!file.canRead()) {

                // FIXME: Need to strip off the fragment part
                // (the "reference") of the name (after the #),
                // if there is one, and add it in again by calling set()
                // on the URL at the end.
                String[] splitName = name.split("#");
                if (splitName.length > 1) {
                    name = splitName[0];
                    fragment = splitName[1];
                }

                // FIXME: This is a hack.
                // Expanding the configuration with Ptolemy II installed
                // in a directory with spaces in the name fails on
                // JAIImageReader because PtolemyII.jpg is passed in
                // to this method as C:\Program%20Files\Ptolemy\...
                file = new File(StringUtilities.substitute(name, "%20", " "));

                URL possibleJarURL = null;

                if (!file.canRead()) {
                    // ModelReference and FilePortParameters sometimes
                    // have paths that have !/ in them.
                    possibleJarURL = ClassUtilities.jarURLEntryResource(name);

                    if (possibleJarURL != null) {
                        file = new File(possibleJarURL.getFile());
                    }
                }

                if (!file.canRead()) {
                    throw new IOException("Cannot read file '" + name + "' or '"
                            + StringUtilities.substitute(name, "%20", " ") + "'"
                            + (possibleJarURL == null ? ""
                                    : " or '" + possibleJarURL.getFile() + ""));
                }
            }

            URL result = file.toURI().toURL();
            if (fragment != null) {
                result = new URL(result + "#" + fragment);
            }
            return result;
        } else {
            // Try relative to the base directory.
            if (baseDirectory != null) {
                // Try to resolve the URI.
                URI newURI;

                try {
                    newURI = baseDirectory.resolve(name);
                } catch (Exception ex) {
                    // FIXME: Another hack
                    // This time, if we try to open some of the JAI
                    // demos that have actors that have defaults FileParameters
                    // like "$PTII/doc/img/PtolemyII.jpg", then resolve()
                    // bombs.
                    String name2 = StringUtilities.substitute(name, "%20", " ");
                    try {
                        newURI = baseDirectory.resolve(name2);
                        name = name2;
                    } catch (Exception ex2) {
                        throw new IOException(
                                "Problem with URI format in '" + name + "'. "
                                        + "and '" + name2 + "' "
                                        + "This can happen if the file name "
                                        + "is not absolute "
                                        + "and is not present relative to the "
                                        + "directory in which the specified model "
                                        + "was read (which was '"
                                        + baseDirectory + "')", ex2);
                    }
                }

                String urlString = newURI.toString();

                try {
                    // Adding another '/' for remote execution.
                    if (newURI.getScheme() != null
                            && newURI.getAuthority() == null) {
                        // Change from Efrat:
                        // "I made these change to allow remote
                        // execution of a workflow from within a web
                        // service."

                        // "The first modification was due to a URI
                        // authentication exception when trying to
                        // create a file object from a URI on the
                        // remote side. The second modification was
                        // due to the file protocol requirements to
                        // use 3 slashes, 'file:///' on the remote
                        // side, although it would be probably be a
                        // good idea to also make sure first that the
                        // url string actually represents the file
                        // protocol."
                        urlString = urlString.substring(0, 6) + "//"
                                + urlString.substring(6);

                        //} else {
                        // urlString = urlString.substring(0, 6) + "/"
                        // + urlString.substring(6);
                    }
                    // Unfortunately, between Java 1.5 and 1.6,
                    // The URL constructor changed.
                    // In 1.5, new URL("file:////foo").toString()
                    // returns "file://foo"
                    // In 1.6, new URL("file:////foo").toString()
                    // return "file:////foo".
                    // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6561321
                    return new URL(urlString);
                } catch (Exception ex3) {
                    try {
                        // Under Webstart, opening
                        // hoc/demo/ModelReference/ModelReference.xml
                        // requires this because the URL is relative.
                        return new URL(baseDirectory.toURL(), urlString);
                    } catch (Exception ex4) {

                        try {
                            // Under Webstart, ptalon, EightChannelFFT
                            // requires this.
                            return new URL(baseDirectory.toURL(),
                                    newURI.toString());
                        } catch (Exception ex5) {
                            // Ignore
                        }

                        throw new IOException(
                                "Problem with URI format in '" + urlString
                                        + "'. " + "This can happen if the '"
                                        + urlString + "' is not absolute"
                                        + " and is not present relative to the directory"
                                        + " in which the specified model was read"
                                        + " (which was '" + baseDirectory
                                        + "')", ex3);
                    }
                }
            }

            // As a last resort, try an absolute URL.

            URL url = new URL(name);

            // If we call new URL("http", null, /foo);
            // then we get "http:/foo", which should be "http://foo"
            // This change suggested by Dan Higgins and Kevin Kruland
            // See kepler/src/util/URLToLocalFile.java
            try {
                String fixedURLAsString = url.toString()
                        .replaceFirst("(https?:)//?", "$1//");
                url = new URL(fixedURLAsString);
            } catch (Exception e) {
                // Ignore
                url = new URL(name);
            }
            return url;
        }
    }

    /** Search the classpath.
     *  @param name The name to be searched
     *  @param classLoader The class loader to use to locate system
     *   resources, or null to use the system class loader that was used
     *   to load this class.
     *  @return null if name does not start with "$CLASSPATH"
     *  or _CLASSPATH_VALUE or if name cannot be found.
     */
    private static URL _searchClassPath(String name, ClassLoader classLoader)
            throws IOException {

        URL result = null;

        // If the name begins with "$CLASSPATH", or
        // "xxxxxxCLASSPATHxxxxxx",then attempt to open the file
        // relative to the classpath.
        // NOTE: Use the dummy variable constant set up in the constructor.
        if (name.startsWith(_CLASSPATH_VALUE)
                || name.startsWith("$CLASSPATH")) {
            // Try relative to classpath.
            String trimmedName = _trimClassPath(name);

            if (classLoader == null) {
                String referenceClassName = "ptolemy.util.FileUtilities";

                try {
                    // WebStart: We might be in the Swing Event thread, so
                    // Thread.currentThread().getContextClassLoader()
                    // .getResource(entry) probably will not work so we
                    // use a marker class.
                    Class referenceClass = Class.forName(referenceClassName);
                    classLoader = referenceClass.getClassLoader();
                } catch (Exception ex) {
                    // IOException constructor does not take a cause
                    IOException ioException = new IOException(
                            "Cannot look up class \"" + referenceClassName
                                    + "\" or get its ClassLoader.");
                    ioException.initCause(ex);
                    throw ioException;
                }
            }

            // Use Thread.currentThread()... for Web Start.
            result = classLoader.getResource(trimmedName);
        }
        return result;
    }

    /** Remove the value of _CLASSPATH_VALUE or "$CLASSPATH".
     */
    private static String _trimClassPath(String name) {
        String classpathKey;

        if (name.startsWith(_CLASSPATH_VALUE)) {
            classpathKey = _CLASSPATH_VALUE;
        } else {
            classpathKey = "$CLASSPATH";
        }

        return name.substring(classpathKey.length() + 1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Tag value used by this class and registered as a parser
     *  constant for the identifier "CLASSPATH" to indicate searching
     *  in the classpath.  This is a hack, but it deals with the fact
     *  that Java is not symmetric in how it deals with getting files
     *  from the classpath (using getResource) and getting files from
     *  the file system.
     */
    private static String _CLASSPATH_VALUE = "xxxxxxCLASSPATHxxxxxx";
}