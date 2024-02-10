/* Base class for displaying exceptions, warnings, and messages.

 Copyright (c) 2003-2016 The Regents of the University of California.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.Locale;

///////////////////////////////////////////////////////////////////
//// MessageHandler

/**
 This is a class that is used to report errors.  It provides a
 set of static methods that are called to report errors.  However, the
 actual reporting of the errors is deferred to an instance of this class
 that is set using the setMessageHandler() method.  Normally there
 is only one instance, set up by the application, so the class is
 a singleton.  But this is not enforced.
 <p>
 This base class simply writes the errors to System.err.
 When an applet or application starts up, it may wish to set a subclass
 of this class as the message handler, to allow a nicer way of
 reporting errors.  For example, a Swing application will probably
 want to report errors in a dialog box, using for example
 the derived class GraphicalMessageHandler.

 <p>See ptolemy.gui.GraphicalMessageHandler</p>

 @author  Edward A. Lee, Steve Neuendorffer, Elaine Cheong
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)
 */
public class MessageHandler implements Thread.UncaughtExceptionHandler {

    /** Create a MessageHandler.
     */
    public MessageHandler() {
        // Note that kepler/loader/src/org/kepler/ExecutionEngine.java
        // invokes new MessageHandler().
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Defer to the set message handler to show the specified
     *  error message.
     *
     *  <p>Note that within Ptolemy, most user code should not call
     *  this method directly.  Instead, throw an exception, which will
     *  be caught by the system elsewhere and include information
     *  about what object caused the error.
     *  @param info The message.
     */
    public static void error(String info) {
        _handler._error(info);
    }

    /** Defer to the set message handler to
     *  show the specified message and throwable information.
     *  If the throwable is an instance of CancelException, then it
     *  is not shown.  By default, only the message of the throwable
     *  is thrown.  The stack trace information is only shown if the
     *  user clicks on the "Display Stack Trace" button.
     *
     *  <p>Note that within Ptolemy, most user code should not call
     *  this method directly.  Instead, throw an exception, which will
     *  be caught by the system elsewhere and include information
     *  about what object caused the error.
     *  @param info The message.
     *  @param throwable The throwable.
     *  @see CancelException
     */
    public static void error(String info, Throwable throwable) {
        // Sometimes you find that errors are reported multiple times.
        // To find out who is calling this method, uncomment the following.
        // System.out.println("------ reporting error:" + throwable);
        // throwable.printStackTrace();
        // System.out.println("------ called from:");
        // (new Exception()).printStackTrace();
        try {
            _handler._error(info, throwable);
        } catch (Throwable throwable2) {
            // An applet was throwing an exception while handling
            // the error - so we print the original message if _error() fails.
            if (_handler instanceof SimpleMessageHandler) {
                throw new RuntimeException(throwable);
            } else {
                System.err.println("Internal Error, exception thrown while "
                        + "handling error: \"" + info + "\"\n");
                throwable.printStackTrace();
                System.err.println("Internal Error:\n");
                throwable2.printStackTrace();
            }
        }
    }

    /** Return true if the current process is a non-interactive session.
     *  If the nightly build is running, then return true.
     *
     *  <p>This method merely checks to see if the
     *  "ptolemy.ptII.isRunningNightlyBuild" property exists and is not empty
     *  or if the "ptolemy.ptII.batchMode" property exists and is not empty
     *  and the property "ptolemyII.ptII.testingMessageHandler" is not set.</p>
     *
     *  <p>To run the test suite in the Nightly Build mode, use</p>
     *  <pre>
     *  make nightly
     *  </pre>
     *  @return True if the nightly build is running.
     */
    public static boolean isNonInteractive() {
        // This method is necessary because Ptolemy can download models
        // and files from websites. It is a best practice to prompt the user
        // and ask them if they actually want to download the resource.
        // However, code like the nightly build and the actor indexing code
        // should run without user interaction, so we set a property
        // when running non-interactive, batch mode code.
        if ((StringUtilities.getProperty("ptolemy.ptII.isRunningNightlyBuild")
                .length() > 0
                || StringUtilities.getProperty("ptolemy.ptII.batchMode")
                        .length() > 0)
                && StringUtilities
                        .getProperty("ptolemy.ptII.testingMessageHandler")
                        .length() == 0) {
            return true;
        }

        return false;
    }

    /** Return a short description of the throwable.
     *  @param throwable The throwable
     *  @return If the throwable is an Exception, return "Exception",
     *  if it is an Error, return "Error", if it is a Throwable, return
     *  "Throwable".
     */
    public static String shortDescription(Throwable throwable) {
        String throwableType;

        if (throwable instanceof Exception) {
            throwableType = "Exception";
        } else if (throwable instanceof Error) {
            throwableType = "Error";
        } else {
            throwableType = "Throwable";
        }

        return throwableType;
    }

    /** Handle uncaught exceptions in a standard way.
     *  @param thread The thread throwing the exception.
     *  @param exception The exception.
     */
    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        _error("UNCAUGHT EXCEPTION: " + exception.getMessage(), exception);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Show the specified error message.
     *  @param info The message.
     */
    protected void _error(String info) {
        System.err.println(info);
    }

    /** Show the specified message and throwable information.
     *  If the throwable is an instance of CancelException, then nothing
     *  is not shown.  By default, only the message of the exception
     *  is thrown.  The stack trace information is only shown if the
     *  user clicks on the "Display Stack Trace" button.
     *
     *  @param info The message.
     *  @param throwable The throwable.
     *  @see CancelException
     */
    protected void _error(String info, Throwable throwable) {
        if (throwable instanceof CancelException) {
            return;
        }

        System.err.println(info);
        throwable.printStackTrace();
    }

    /** Display the warning message.  In this base class, the
     *  the default handler merely prints the warning to stderr.
     *  @param info The message.
     */
    protected void _message(String info) {
        System.err.println(info);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The message handler. */
    private static MessageHandler _handler = new MessageHandler();
}