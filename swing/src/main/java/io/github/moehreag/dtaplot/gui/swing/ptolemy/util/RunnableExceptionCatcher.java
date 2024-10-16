/*
 Copyright (c) 2008-2014 The Regents of the University of California.
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

package io.github.moehreag.dtaplot.gui.swing.ptolemy.util;

///////////////////////////////////////////////////////////////////
////RunnableExceptionCatcher

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
A class (that implements the proxy design pattern) that encapsulates a
runnable, catches the exception and will report the exception to the
Ptolemy Message Handler.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Green (rodiers)
@Pt.AcceptedRating Green (rodiers)
 */

public class RunnableExceptionCatcher implements Runnable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct an instance that will wrap a runnable,
     * catch its exceptions and report it to the Ptolemy
     * Message Handler.
     * @param runnable The runnable.
     */
    public RunnableExceptionCatcher(Runnable runnable) {
        _runnable = runnable;
    }

    /** Execute the runnable.
     */
    @Override
    public void run() {
        try {
            _runnable.run();
        } catch (Throwable e) {
            LOGGER.error(e.getClass().getSimpleName(), e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The runnable.
    private final Runnable _runnable;
    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableExceptionCatcher.class);
}