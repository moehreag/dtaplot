/* Utilities used to manipulate strings.

 Copyright (c) 2002-2019 The Regents of the University of California.
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

// Note that classes in ptolemy.util do not depend on any
// other ptolemy packages.

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

///////////////////////////////////////////////////////////////////
//// StringUtilities

/**
 A collection of utilities for manipulating strings.
 These utilities do not depend on any other ptolemy packages.

 @author Christopher Brooks, Contributors: Teale Fristoe
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)
 */
public class StringUtilities {
    /** Instances of this class cannot be created.
     */
    private StringUtilities() {
    }

    /** If the ptolemy.ptII.exitAfterWrapup or the
     *  ptolemy.ptII.doNotExit properties are not set, then call
     *  System.exit().
     *  Ptolemy code should call this method instead of directly calling
     *  System.exit() so that we can test code that would usually exit.
     *  @param returnValue The return value of this process, where
     *  non-zero values indicate an error.
     */
    public static void exit(int returnValue) {
        try {
            if (StringUtilities.getProperty("ptolemy.ptII.doNotExit")
                    .length() > 0) {
                return;
            }
        } catch (SecurityException ex) {
            System.out.println("Warning: failed to get property \""
                    + "ptolemy.ptII.doNotExit\". "
                    + "(-sandbox always causes this)");
        }

        try {
            if (StringUtilities.getProperty("ptolemy.ptII.exitAfterWrapup")
                    .length() > 0) {
                throw new RuntimeException("StringUtilities.exit() was called. "
                        + "Normally, we would "
                        + "exit here because Manager.exitAfterWrapup() "
                        + "was called.  However, because the "
                        + "ptolemy.ptII.exitAfterWrapup property "
                        + "is set, we throw this exception instead.");
            }
        } catch (SecurityException ex) {
            System.out.println("Warning: failed to get property \""
                    + "ptolemy.ptII.exitAfterWrapup\". "
                    + "(-sandbox always causes this)");

        }

        if (!inApplet()) {
            // Only call System.exit if we are not in an applet.
            // Non-zero indicates a problem.
            System.exit(returnValue);
        }
    }

    /** Get the specified property from the environment. An empty
     *  string is returned if the property named by the "propertyName"
     *  argument environment variable does not exist, though if
     *  certain properties are not defined, then we make various
     *  attempts to determine them and then set them.  See the javadoc
     *  page for java.util.System.getProperties() for a list of system
     *  properties.

     *  <p>The following properties are handled specially
     *  <dl>
     *  <dt> "ptolemy.ptII.dir"
     *  <dd> vergil usually sets the ptolemy.ptII.dir property to the
     *  value of $PTII.  However, if we are running under Web Start,
     *  then this property might not be set, in which case we look
     *  for "ptolemy/util/StringUtilities.class" and set the
     *  property accordingly.
     *  <dt> "ptolemy.ptII.dirAsURL"
     *  <dd> Return $PTII as a URL.  For example, if $PTII was c:\ptII,
     *  then return file:/c:/ptII/.
     *  <dt> "user.dir"
     *  <dd> Return the canonical path name to the current working
     *  directory.  This is necessary because under Windows with
     *  JDK1.4.1, the System.getProperty() call returns
     *  <code><b>c</b>:/<i>foo</i></code> whereas most of the other
     *  methods that operate on path names return
     *  <code><b>C</b>:/<i>foo</i></code>.
     *  </dl>
     *  @param propertyName The name of property.
     *  @return A String containing the string value of the property.
     *  If the property is not found, then we return the empty string.
     */
    public static String getProperty(String propertyName) {
        // NOTE: getProperty() will probably fail in applets, which
        // is why this is in a try block.
        String property = null;

        try {
            property = System.getProperty(propertyName);
            // if (propertyName.equals("ptolemy.ptII.dir")) {
            //     System.out.println("StringUtilities.getProperty(" + propertyName + "): " + property);
            // }
        } catch (SecurityException ex) {
            if (!propertyName.equals("ptolemy.ptII.dir")) {
                // Constants.java depends on this when running with
                // -sandbox.
                SecurityException security = new SecurityException(
                        "Could not find '" + propertyName
                                + "' System property");
                security.initCause(ex);
                throw security;
            }
        }

        if (propertyName.equals("user.dir")) {
            try {
                if (property == null) {
                    return property;
                }
                File userDirFile = new File(property);
                return userDirFile.getCanonicalPath();
            } catch (IOException ex) {
                return property;
            }
        }

        // Check for cases where the ptII property starts with
        // the string "/cygdrive".  This can happen if the property
        // was set by doing "PTII=`pwd`" under Cygwin bash.
        //
        // If the property starts with $JAVAROOT, and the
        // propertyName is ptolemy.ptII.dir, then don't return
        // the property yet, instead, refine it.
        if (property != null && (!propertyName.equals("ptolemy.ptII.dir")
                && !property.startsWith("$JAVAROOT"))) {
            if (propertyName.equals("ptolemy.ptII.dir")
                    && property.startsWith("/cygdrive")
                    && !_printedCygwinWarning) {
                // This error only occurs when users build their own,
                // so it is safe to print to stderr
                _printedCygwinWarning = true;
                System.err.println("ptolemy.ptII.dir property = \"" + property
                        + "\", which contains \"cygdrive\". "
                        + "This is almost always an error under Cygwin that "
                        + "is occurs when one does PTII=`pwd`.  Instead, do "
                        + "PTII=c:/foo/ptII");
            }

            return property;
        } else {

            if (propertyName.equals("ptolemy.ptII.dirAsURL")) {
                // Return $PTII as a URL.  For example, if $PTII was c:\ptII,
                // then return file:/c:/ptII/
                File ptIIAsFile = new File(getProperty("ptolemy.ptII.dir"));

                try {
                    // Convert first to a URI, then to a URL so that we
                    // properly handle cases where $PTII has spaces in it.
                    URI ptIIAsURI = ptIIAsFile.toURI();
                    URL ptIIAsURL = ptIIAsURI.toURL();
                    return ptIIAsURL.toString();
                } catch (java.net.MalformedURLException malformed) {
                    throw new RuntimeException("While trying to find '"
                            + propertyName + "', could not convert '"
                            + ptIIAsFile + "' to a URL", malformed);
                }
            }

            if (propertyName.equals("ptolemy.ptII.dir")) {
                if (_ptolemyPtIIDir != null) {
                    // Return the previously calculated value
                    // System.out.println("StringUtilities.getProperty(" + propertyName + "): returning previous " + _ptolemyPtIIDir);
                    return _ptolemyPtIIDir;
                } else {
                    String stringUtilitiesPath = "ptolemy/util/StringUtilities.class";

                    // PTII variable was not set
                    URL namedObjURL = ClassUtilities
                            .getResource(stringUtilitiesPath);

                    if (namedObjURL != null) {
                        // Get the file portion of URL
                        String namedObjFileName = namedObjURL.getFile();

                        // System.out.println("StringUtilities.getProperty(" + propertyName + "): namedObjURL: " + namedObjURL);
                        // FIXME: How do we get from a URL to a pathname?
                        if (namedObjFileName.startsWith("file:")) {
                            if (namedObjFileName.startsWith("file://")
                                    || namedObjFileName
                                            .startsWith("file:\\\\")) {
                                // We get rid of either file:/ or file:\
                                namedObjFileName = namedObjFileName
                                        .substring(6);
                            } else {
                                // Get rid of file:
                                namedObjFileName = namedObjFileName
                                        .substring(5);
                            }
                        }

                        String abnormalHome = namedObjFileName.substring(0,
                                namedObjFileName.length()
                                        - stringUtilitiesPath.length());

                        // abnormalHome will have values like: "/C:/ptII/"
                        // which cause no end of trouble, so we construct a File
                        // and call toString().
                        _ptolemyPtIIDir = new File(abnormalHome).toString();

                        // If we are running under Web Start, then strip off
                        // the trailing "!"
                        if (_ptolemyPtIIDir.endsWith("/!")
                                || _ptolemyPtIIDir.endsWith("\\!")) {
                            _ptolemyPtIIDir = _ptolemyPtIIDir.substring(0,
                                    _ptolemyPtIIDir.length() - 1);
                        }

                        // Web Start, we might have
                        // RMptsupport.jar or
                        // XMptsupport.jar1088483703686
                        String ptsupportJarName = File.separator + "DMptolemy"
                                + File.separator + "RMptsupport.jar";

                        if (_ptolemyPtIIDir.endsWith(ptsupportJarName)) {
                            _ptolemyPtIIDir = _ptolemyPtIIDir.substring(0,
                                    _ptolemyPtIIDir.length()
                                            - ptsupportJarName.length());
                        } else {
                            ptsupportJarName = "/DMptolemy/XMptsupport.jar";

                            if (_ptolemyPtIIDir
                                    .lastIndexOf(ptsupportJarName) != -1) {
                                _ptolemyPtIIDir = _ptolemyPtIIDir.substring(0,
                                        _ptolemyPtIIDir
                                                .lastIndexOf(ptsupportJarName));
                            } else {
                                // Ptolemy II 6.0.1 under Windows: remove
                                // "\ptolemy\ptsupport.jar!"
                                // If we don't do this, then ptolemy.ptII.dir
                                // is set incorrectly and then links to the javadoc
                                // files will not be found if the javadoc only
                                // exists in codeDoc.jar and lib/ptII.properties
                                // is not present.
                                ptsupportJarName = File.separator + "ptolemy"
                                        + File.separator + "ptsupport.jar";

                                if (_ptolemyPtIIDir
                                        .lastIndexOf(ptsupportJarName) != -1) {
                                    _ptolemyPtIIDir = _ptolemyPtIIDir.substring(
                                            0, _ptolemyPtIIDir.lastIndexOf(
                                                    ptsupportJarName));
                                }
                            }
                        }
                    }

                    // Convert %20 to spaces because if a URL has %20 in it,
                    // then we know we have a space, but file names do not
                    // recognize %20 as being a single space, instead file names
                    // see %20 as three characters: '%', '2', '0'.
                    if (_ptolemyPtIIDir != null) {
                        _ptolemyPtIIDir = StringUtilities
                                .substitute(_ptolemyPtIIDir, "%20", " ");
                    }
                    //*.class files are compiled into classes.dex file; therefore, check for StringUtilities.class fails
                    //it's OK to set _ptolemyPtIIDir to an empty string on Android
                    if (_ptolemyPtIIDir == null && System
                            .getProperty("java.vm.name").equals("Dalvik")) {
                        _ptolemyPtIIDir = "";
                    }
                    if (_ptolemyPtIIDir == null) {
                        throw new RuntimeException("Could not find "
                                + "'ptolemy.ptII.dir'" + " property.  "
                                + "Also tried loading '" + stringUtilitiesPath
                                + "' as a resource and working from that. "
                                + "Vergil should be "
                                + "invoked with -Dptolemy.ptII.dir"
                                + "=\"$PTII\", "
                                + "otherwise the following features will not work: "
                                + "PtinyOS, Ptalon, the Python actor, "
                                + "actor document, cg code generation and possibly "
                                + "other features will not work.");
                    }

                    try {
                        // Here, we set the property so that future updates
                        // will get the correct value.
                        System.setProperty("ptolemy.ptII.dir", _ptolemyPtIIDir);
                    } catch (SecurityException security) {
                        // Ignore, we are probably running as an applet or -sandbox
                    }

                    // System.out.println("StringUtilities.getProperty(" + propertyName + "): returning " + _ptolemyPtIIDir);
                    return _ptolemyPtIIDir;
                }
            }

            // If the property is not set then we return the empty string.
            //if (property == null) {
            return "";
            //}
        }
    }

    /** Return true if we are in an applet.
     *  @return True if we are running in an applet.
     */
    public static boolean inApplet() {
        boolean inApplet = false;
        try {
            StringUtilities.getProperty("HOME");
        } catch (SecurityException ex) {
            inApplet = true;
        }
        return inApplet;
    }

    /** Replace all occurrences of <i>pattern</i> in the specified
     *  string with <i>replacement</i>.  Note that the pattern is NOT
     *  a regular expression, and that relative to the
     *  String.replaceAll() method in jdk1.4, this method is extremely
     *  slow.  This method does not work well with back slashes.
     *  @param string The string to edit.
     *  @param pattern The string to replace.
     *  @param replacement The string to replace it with.
     *  @return A new string with the specified replacements.
     */
    public static String substitute(String string, String pattern,
            String replacement) {
        if (string == null) {
            return null;
        }
        int start = string.indexOf(pattern);

        while (start != -1) {
            StringBuilder buffer = new StringBuilder(string);
            buffer.delete(start, start + pattern.length());
            buffer.insert(start, replacement);
            string = new String(buffer);
            start = string.indexOf(pattern, start + replacement.length());
        }

        return string;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Set to true if we print the cygwin warning in getProperty(). */
    private static boolean _printedCygwinWarning = false;

    /** Cached value of ptolemy.ptII.dir property. */
    private static String _ptolemyPtIIDir = null;
}