/* A simple message handler that throws exceptions.

 Copyright (c) 2012-2014 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
//// SimpleMessageHandler

/**
 This is a message handler that reports errors in a graphical dialog box.

 <p>See ptolemy.gui.GraphicalMessageHandler</p>

 @author  Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
b @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class SimpleMessageHandler extends MessageHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw a RuntimeException.
     *  @param info The message.
     */
    @Override
    protected void _error(String info) {
        throw new RuntimeException(info);
    }

    /** Show the specified message and throwable information.
     *  If the throwable is an instance of CancelException, then nothing
     *  is shown.
     *
     *  @param info The message.
     *  @param throwable The throwable.
     *  @see CancelException
     */
    @Override
    protected void _error(String info, Throwable throwable) {
        if (throwable instanceof CancelException) {
            return;
        }
        // Print out the exception so that if MoMLSimpleApplication
        // throws an exception, we see it on stdout.
        throwable.printStackTrace();
        throw new RuntimeException(info, throwable);
    }
}