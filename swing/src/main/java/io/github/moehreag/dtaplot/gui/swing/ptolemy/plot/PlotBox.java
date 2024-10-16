/* A labeled box for signal plots.

 @Copyright (c) 1997-2019 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package io.github.moehreag.dtaplot.gui.swing.ptolemy.plot;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.*;
import java.net.URL;
import java.util.Timer;
import java.util.*;

// TO DO:
//   - Augment getColorByName to support a full complement of colors
//     (get the color list from Tycho).
///////////////////////////////////////////////////////////////////
//// PlotBox

/**
 A labeled box within which to place a data plot.
 <p>A title, X and Y axis labels, tick marks, and a legend are all
 supported.  Zooming in and out is supported.  To zoom in, click and
 hold mouse button 1 and drag the mouse downwards to draw a box.  To
 zoom out, click and hold mouse button1 and drag the mouse upward.
 <p>
 The box can be configured either through a file with commands or
 through direct invocation of the public methods of the class.
 <p>
 When calling the methods, in most cases the changes will not
 be visible until paintComponent() has been called.  To request that this
 be done, call repaint().
 <p>
 A small set of key bindings are provided for convenience.
 They are:
 <ul>
 <li> Cntrl-c: Export the plot to the clipboard (in PlotML).
 <li> D: Dump the plot to standard output (in PlotML).
 <li> E: Export the plot to standard output in EPS format.
 <li> F: Fill the plot.
 <li> H or ?: Display a simple help message.
 <li> Cntrl-D or Q: quit
 </ul>
 These commands are provided in a menu by the PlotFrame class.
 Note that exporting to the clipboard is not allowed in applets
 (it used to be), so this will result in an error message.
 <p>
 At this time, the two export commands produce encapsulated postscript
 tuned for black-and-white printers.  In the future, more formats may
 supported.
 Exporting to the clipboard and to standard output, in theory,
 is allowed for applets, unlike writing to a file. Thus, these
 key bindings provide a simple mechanism to obtain a high-resolution
 image of the plot from an applet, suitable for incorporation in
 a document. However, in some browsers, exporting to standard out
 triggers a security violation.  You can use the JDK appletviewer instead.
 <p>
 To read commands from a file or URL, the preferred technique is
 to use one of the classes in the plotml package.  That package
 supports both PlotML, an XML extension for plots, and a historical
 file format specific to ptplot.  The historical file format is
 understood by the read() method in this class.
 The syntax of the historical format, documented below, is rudimentary,
 and will probably not be extended as ptplot evolves.  Nonetheless,
 we document it here since it is directly supported by this class.
 <p>
 The historical format for the file allows any number
 commands, one per line.  Unrecognized commands and commands with
 syntax errors are ignored.  Comments are denoted by a line starting
 with a pound sign "#".  The recognized commands include:
 <pre>
 TitleText: <i>string</i>
 XLabel: <i>string</i>
 YLabel: <i>string</i>
 </pre>
 These commands provide a title and labels for the X (horizontal) and Y
 (vertical) axes.
 A <i>string</i> is simply a sequence of characters, possibly
 including spaces.  There is no need here to surround them with
 quotation marks, and in fact, if you do, the quotation marks will
 be included in the labels.
 <p>
 The ranges of the X and Y axes can be optionally given by commands like:
 <pre>
 XRange: <i>min</i>, <i>max</i>
 YRange: <i>min</i>, <i>max</i>
 </pre>
 The arguments <i>min</i> and <i>max</i> are numbers, possibly
 including a sign and a decimal point. If they are not specified,
 then the ranges are computed automatically from the data and padded
 slightly so that datapoints are not plotted on the axes.
 <p>
 The tick marks for the axes are usually computed automatically from
 the ranges.  Every attempt is made to choose reasonable positions
 for the tick marks regardless of the data ranges (powers of
 ten multiplied by 1, 2, or 5 are used).  However, they can also be
 specified explicitly using commands like:
 <pre>
 XTicks: <i>label position, label position, ...</i>
 YTicks: <i>label position, label position, ...</i>
 </pre>
 A <i>label</i> is a string that must be surrounded by quotation
 marks if it contains any spaces.  A <i>position</i> is a number
 giving the location of the tick mark along the axis.  For example,
 a horizontal axis for a frequency domain plot might have tick marks
 as follows:
 <pre>
 XTicks: -PI -3.14159, -PI/2 -1.570795, 0 0, PI/2 1.570795, PI 3.14159
 </pre>
 Tick marks could also denote years, months, days of the week, etc.
 <p>
 The X and Y axes can use a logarithmic scale with the following commands:
 <pre>
 XLog: on
 YLog: on
 </pre>
 The grid labels represent powers of 10.  Note that if a logarithmic
 scale is used, then the values must be positive.  Non-positive values
 will be silently dropped.  Note further that when using logarithmic
 axes that the log of input data is taken as the data is added to the plot.
 This means that <pre>XLog: on</pre> or <pre>YLog: on</pre> should
 appear before any data.  Also, the value of the XTicks, YTicks,
 XRange or YRange directives should be in log units.
 So, <pre>XTicks: 1K 3</pre> will display the string <pre>1K</pre>
 at the 1000 mark.
 <p>
 By default, tick marks are connected by a light grey background grid.
 This grid can be turned off with the following command:
 <pre>
 Grid: off
 </pre>
 It can be turned back on with
 <pre>
 Grid: on
 </pre>
 Also, by default, the first ten data sets are shown each in a unique color.
 The use of color can be turned off with the command:
 <pre>
 Color: off
 </pre>
 It can be turned back on with
 <pre>
 Color: on
 </pre>
 Finally, the rather specialized command
 <pre>
 Wrap: on
 </pre>
 enables wrapping of the X (horizontal) axis, which means that if
 a point is added with X out of range, its X value will be modified
 modulo the range so that it lies in range. This command only has an
 effect if the X range has been set explicitly. It is designed specifically
 to support oscilloscope-like behavior, where the X value of points is
 increasing, but the display wraps it around to left. A point that lands
 on the right edge of the X range is repeated on the left edge to give
 a better sense of continuity. The feature works best when points do land
 precisely on the edge, and are plotted from left to right, increasing
 in X.
 <p>
 All of the above commands can also be invoked directly by calling the
 the corresponding public methods from some Java procedure.
 <p>
 This class uses features of JDK 1.2, and hence if used in an applet,
 it can only be viewed by a browser that supports JDK 1.2, or a plugin.

 @author Edward A. Lee, Christopher Brooks, Contributors: Jun Wu (jwu@inin.com.au), William Wu, Robert Kroeger, Tom Peachey, Bert Rodiers, Dirk Bueche

 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class PlotBox extends JPanel implements Printable, PlotBoxInterface {
    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Construct a plot box with a default configuration. */
    public PlotBox() {
        // If we make this transparent, the background shows through.
        // However, we assume that the user will set the background.
        // NOTE: A component is transparent by default (?).
        // setOpaque(false);
        setOpaque(true);

        // Create a right-justified layout with spacing of 2 pixels.
        setLayout(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        addMouseListener(new ZoomListener());
        addMouseWheelListener(new ZoomListener2()); // Dirk: zooming with the mouse wheel
        addMouseListener(new MoveListener()); // Dirk: move plotted objects with 3rd mouse button
        addMouseMotionListener(new MoveMotionListener()); // Dirk
        addKeyListener(new CommandListener());
        addMouseMotionListener(new DragListener());

        // This is something we want to do only once...
        _measureFonts();

        // Request the focus so that key events are heard.
        // NOTE: no longer needed?
        // requestFocus();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a line to the caption (displayed at below graph) .
     * @param captionLine The string to be added.
     * @see #getCaptions()
     */
    @Override
    public synchronized void addCaptionLine(String captionLine) {
        // Caption code contributed by Tom Peachey.
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;
        _captionStrings.addElement(captionLine);
    }

    /** Add a legend (displayed at the upper right) for the specified
     *  data set with the specified string.  Short strings generally
     *  fit better than long strings.  If the string is empty, or the
     *  argument is null, then no legend is added.
     *  @param dataset The dataset index.
     *  @param legend The label for the dataset.
     *  @see #renameLegend(int, String)
     */
    @Override
    public synchronized void addLegend(int dataset, String legend) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        if (legend == null || legend.isEmpty()) {
            return;
        }

        _legendStrings.addElement(legend);
        _legendDatasets.addElement(dataset);
    }

    /** Specify a tick mark for the X axis.  The label given is placed
     *  on the axis at the position given by <i>position</i>. If this
     *  is called once or more, automatic generation of tick marks is
     *  disabled.  The tick mark will appear only if it is within the X
     *  range.
     *  <p>Note that if {@link #setXLog(boolean)} has been called, then
     *  the position value should be in log units.
     *  So, addXTick("1K", 3) will display the string <pre>1K</pre>
     *  at the 1000 mark.
     *  @param label The label for the tick mark.
     *  @param position The position on the X axis.
     */
    @Override
    public synchronized void addXTick(String label, double position) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        if (_xticks == null) {
            _xticks = new Vector<>();
            _xticklabels = new Vector<>();
        }

        _xticks.addElement(position);
        _xticklabels.addElement(label);
    }

    /** Specify a tick mark for the Y axis.  The label given is placed
     *  on the axis at the position given by <i>position</i>. If this
     *  is called once or more, automatic generation of tick marks is
     *  disabled.  The tick mark will appear only if it is within the Y
     *  range.
     *  <p>Note that if {@link #setYLog(boolean)} has been called, then
     *  the position value should be in log units.
     *  So, addYTick("1K", 3) will display the string <pre>1K</pre>
     *  at the 1000 mark.
     *  @param label The label for the tick mark.
     *  @param position The position on the Y axis.
     */
    @Override
    public synchronized void addYTick(String label, double position) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        if (_yticks == null) {
            _yticks = new Vector<>();
            _yticklabels = new Vector<>();
        }

        _yticks.addElement(position);
        _yticklabels.addElement(label);
    }

    /** If the argument is true, clear the axes.  I.e., set all parameters
     *  controlling the axes to their initial conditions.
     *  For the change to take effect, call repaint().  If the argument
     *  is false, do nothing.
     *  @param axes If true, clear the axes parameters.
     */
    @Override
    public synchronized void clear(boolean axes) {
        // We need to repaint the offscreen buffer.
        _plotImage = null;

        _xBottom = Double.MAX_VALUE;
        _xTop = -Double.MAX_VALUE;
        _yBottom = Double.MAX_VALUE;
        _yTop = -Double.MAX_VALUE;

        if (axes) {
            // Protected members first.
            _yMax = 0;
            _yMin = 0;
            _xMax = 0;
            _xMin = 0;
            _xRangeGiven = false;
            _yRangeGiven = false;
            _originalXRangeGiven = false;
            _originalYRangeGiven = false;
            _rangesGivenByZooming = false;
            _xlog = false;
            _ylog = false;
            _grid = true;
            _wrap = false;
            _usecolor = true;

            // Private members next...
            _filespec = null;
            _xlabel = null;
            _ylabel = null;
            _title = null;
            _legendStrings = new Vector<>();
            _legendDatasets = new Vector<>();
            _xticks = null;
            _xticklabels = null;
            _yticks = null;
            _yticklabels = null;
        }
    }

    /** Clear all the captions.
     *  For the change to take effect, call repaint().
     *  @see #setCaptions(Vector)
     */
    @Override
    public synchronized void clearCaptions() {
        // Changing caption means we need to repaint the offscreen buffer.
        _plotImage = null;
        _captionStrings = new Vector<>();
    }

    /** Clear all legends.  This will show up on the next redraw.
     */
    @Override
    public synchronized void clearLegends() {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _legendStrings = new Vector<>();
        _legendDatasets = new Vector<>();
    }

    /** If this method is called in the event thread, then simply
     * execute the specified action.  Otherwise,
     * if there are already deferred actions, then add the specified
     * one to the list.  Otherwise, create a list of deferred actions,
     * if necessary, and request that the list be processed in the
     * event dispatch thread.
     *
     * Note that it does not work nearly as well to simply schedule
     * the action yourself on the event thread because if there are a
     * large number of actions, then the event thread will not be able
     * to keep up.  By grouping these actions, we avoid this problem.
     *
     * This method is not synchronized, so the caller should be.
     * @param action The Runnable object to execute.
     */
    @Override
    public void deferIfNecessary(Runnable action) {
        // In swing, updates to showing graphics must be done in the
        // event thread.  If we are in the event thread, then proceed.
        // Otherwise, queue a request or add to a pending request.
        if (EventQueue.isDispatchThread()) {
            action.run();
        } else {

            // Add the specified action to the list of actions to perform.
            _deferredActions.add(action);

            // If it hasn't already been requested, request that actions
            // be performed in the event dispatch thread.
            if (!_actionsDeferred) {
                Runnable doActions = this::_executeDeferredActions;

                try {
                    _actionsDeferred = true;

                    // NOTE: Using invokeAndWait() here risks causing
                    // deadlock.  Don't do it!
                    SwingUtilities.invokeLater(doActions);
                } catch (Throwable throwable) {
                    // Ignore InterruptedException.
                    // Other exceptions should not occur.
                }

            }
        }
    }

    /** Destroy the plotter.  This method is usually
     *  called by PlotApplet.destroy().  It does
     *  various cleanups to reduce memory usage.
     */
    @Override
    public void destroy() {
        clear(true);
        // Avoid leaking _timerTask;
        setAutomaticRescale(false);
        setTimedRepaint(false);

        // Remove the buttons
        if (_printButton != null) {
            ActionListener[] listeners = _printButton.getActionListeners();
            for (ActionListener listener : listeners) {
                _printButton.removeActionListener(listener);
            }
            _printButton = null;
        }
        if (_resetButton != null) {
            ActionListener[] listeners = _resetButton.getActionListeners();
            for (ActionListener listener : listeners) {
                _resetButton.removeActionListener(listener);
            }
            _resetButton = null;
        }
        if (_eqAxButton != null) {
            ActionListener[] listeners = _formatButton.getActionListeners();
            for (ActionListener listener : listeners) {
                _eqAxButton.removeActionListener(listener);
            }
            _eqAxButton = null;
        }
        if (_formatButton != null) {
            ActionListener[] listeners = _formatButton.getActionListeners();
            for (ActionListener listener : listeners) {
                _formatButton.removeActionListener(listener);
            }
            _formatButton = null;
        }
        if (_fillButton != null) {
            ActionListener[] listeners = _fillButton.getActionListeners();
            for (ActionListener listener : listeners) {
                _fillButton.removeActionListener(listener);
            }
            _fillButton = null;
        }

        removeAll();
    }

    /** Export a EPS description of the plot.
     *  If the argument is null, then the description goes
     *  to the clipboard.  Otherwise, it goes to the specified file.
     *  To send it to standard output, use
     *  <code>System.out</code> as an argument.
     *  @param out An output stream to which to send the description.
     */
    public synchronized void export(OutputStream out) {
        try {
            EPSGraphics g = new EPSGraphics(out, _width, _height);
            _drawPlot(g, false);
            g.showpage();
        } catch (RuntimeException ex) {
            String message = "Export failed: " + ex.getMessage();
            JOptionPane.showMessageDialog(this, message, "Ptolemy Plot Message",
                    JOptionPane.ERROR_MESSAGE);

            // Rethrow the exception so that we don't report success,
            // and so the stack trace is displayed on standard out.
            throw (RuntimeException) ex.fillInStackTrace();
        }
    }

    // CONTRIBUTED CODE.
    // I wanted the ability to use the Plot object in a servlet and to
    // write out the resultant images. The following routines,
    // particularly exportImage(), permit this. I also had to make some
    // minor changes elsewhere. Rob Kroeger, May 2001.
    // NOTE: This code has been modified by EAL to conform with Ptolemy II
    // coding style.

    /** Create a BufferedImage and draw this plot to it.
     *  The size of the returned image matches the current size of the plot.
     *  This method can be used, for
     *  example, by a servlet to produce an image, rather than
     *  requiring an applet to instantiate a PlotBox.
     *  @return An image filled by the plot.
     */
    public synchronized BufferedImage exportImage() {
        Rectangle rectangle = new Rectangle(_preferredWidth, _preferredHeight);
        return exportImage(
                new BufferedImage(rectangle.width, rectangle.height,
                        BufferedImage.TYPE_INT_ARGB),
                rectangle, _defaultImageRenderingHints(), false);
    }

    /** Create a BufferedImage the size of the given rectangle and draw
     *  this plot to it at the position specified by the rectangle.
     *  The plot is rendered using anti-aliasing.
     *  @param rectangle The size of the plot. This method can be used, for
     *  example, by a servlet to produce an image, rather than
     *  requiring an applet to instantiate a PlotBox.
     *  @return An image containing the plot.
     */
    public synchronized BufferedImage exportImage(Rectangle rectangle) {
        return exportImage(
                new BufferedImage(rectangle.width, rectangle.height,
                        BufferedImage.TYPE_INT_ARGB),
                rectangle, _defaultImageRenderingHints(), false);
    }

    /** Draw this plot onto the specified image at the position of the
     *  specified rectangle with the size of the specified rectangle.
     *  The plot is rendered using anti-aliasing.
     *  This can be used to paint a number of different
     *  plots onto a single buffered image.  This method can be used, for
     *  example, by a servlet to produce an image, rather than
     *  requiring an applet to instantiate a PlotBox.
     *  @param bufferedImage Image onto which the plot is drawn.
     *  @param rectangle The size and position of the plot in the image.
     *  @param hints Rendering hints for this plot.
     *  @param transparent Indicator that the background of the plot
     *   should not be painted.
     *  @return The modified bufferedImage.
     */
    public synchronized BufferedImage exportImage(BufferedImage bufferedImage,
            Rectangle rectangle, RenderingHints hints, boolean transparent) {
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.addRenderingHints(_defaultImageRenderingHints());

        if (!transparent) {
            graphics.setColor(Color.white); // set the background color
            graphics.fill(rectangle);
        }

        _drawPlot(graphics, false, rectangle);
        return bufferedImage;
    }

    /**        Draw this plot onto the provided image.
     *  This method does not paint the background, so the plot is
     *  transparent.  The plot fills the image, and is rendered
     *  using anti-aliasing.  This method can be used to overlay
     *  multiple plots on the same image, although you must use care
     *  to ensure that the axes and other labels are identical.
     *  Hence, it is usually better to simply combine data sets into
     *  a single plot.
     *  @param bufferedImage The image onto which to render the plot.
     *  @return The modified bufferedImage.
     */
    public synchronized BufferedImage exportImage(BufferedImage bufferedImage) {
        return exportImage(bufferedImage,
                new Rectangle(bufferedImage.getWidth(),
                        bufferedImage.getHeight()),
                _defaultImageRenderingHints(), true);
    }

    /** Export a Latex description of the plot.
     *  @param directory An directory to which to export the Latex.
     */
    public synchronized void exportLatex(File directory) {
        try {
            if (!directory.isDirectory()) {
                if (!directory.mkdir()) {
                    throw new RuntimeException("Failed to create " + directory);
                }
            }
            // Copy the required latex files.
            // Not currently using anything from pst-sigsys.
            // _copyFiles("pst-sigsys.sty", directory);
            // _copyFiles("pst-sigsys.tex", directory);

            // Create a makefile
            /*String makefileContents = "# Makefile for Latex files generated by the Ptolemy II plotter.\n"
                    + "# This makes several assumptions:\n" +
                    // "#   - The current directory contains pst-sigsys.tex and pst-sigsys.sty\n" +
                    "#   - latex, dvips, ps2pdf, and open are all in the path\n"
                    + "#   - pstricks is installed on the local latex.\n"
                    + "#\n" + "FILENAME=" + directory.getName() + "\n"
                    + "all:    $(FILENAME).tex\n" + "\tlatex $(FILENAME);\n"
                    + "\tdvips $(FILENAME);\n" + "\tps2pdf $(FILENAME).ps;\n"
                    + "\topen $(FILENAME).pdf\n";*/
            String makefileContents = """
                    # Makefile for Latex files generated by the Ptolemy II plotter.
                    # This makes several assumptions:
                    # - The current directory contains pst-sigsys.tex and pst-sigsys.sty
                    # - latex, dvips, ps2pdf, and open are all in the path
                    # - pstricks is installed on the local latex.
                    latex FILENAME.tex;
                    dvips FILENAME;
                    ps2pdf FILENAME.ps;
                    open FILENAME.pdf
                    """.replace("FILENAME", directory.getName());
            File makefile = new File(directory, "makefile");
            PrintStream stream = new PrintStream(makefile);
            stream.print(makefileContents);
            stream.close();

            // Now write the latex file.
            File latexFile = new File(directory, directory.getName() + ".tex");
            PrintStream out = new PrintStream(latexFile);
            out.println("% Plot output generated by ptplot.");
            // FIXME: probably is a better documentclass.
            out.println("\\documentclass[12pt]{article}");
            out.println("\\usepackage{pstricks}");
            // Not currently using anything from pst-sigsys.
            // out.println("\\usepackage{pst-sigsys}");
            out.println("\\begin{document}");
            out.println("\\thispagestyle{empty}");
            // FIXME: The following fixes the width at 6 in
            // and the height at 4in. Should instead get these
            // from the window size.
            double xScale = 6.0 / (_xMax - _xMin);
            double yScale = 4.0 / (_yMax - _yMin);
            // FIXME: The following should be calculated to
            // position the plot somewhere reasonable.
            double xOrigin = -3.0;
            double yOrigin = 0.0;
            out.println("\\begin{pspicture}[" + "xunit=" + xScale + "in,"
                    + "yunit=" + yScale + "in," + "origin={" + xOrigin + ","
                    + yOrigin + "}," + "showgrid=" + getGrid() + "]" + "("
                    + _xMin + "," + _yMin + ")" + "(" + _xMax + "," + _yMax
                    + ")");

            out.println(_exportLatexPlotData());
            out.println("\\end{pspicture}");
            out.println("\\end{document}");
            out.close();

            String message = "Apologies, but export to Latex is not implemented yet.";
            JOptionPane.showMessageDialog(this, message, "Ptolemy Plot Message",
                    JOptionPane.ERROR_MESSAGE);
		} catch (Throwable throwable) {
            String message = "Export failed: " + throwable.getMessage();
            JOptionPane.showMessageDialog(this, message, "Ptolemy Plot Message",
                    JOptionPane.ERROR_MESSAGE);

            // Rethrow the exception so that we don't report success,
            // and so the stack trace is displayed on standard out.
            throw (RuntimeException) throwable.fillInStackTrace();
        }
    }

    /** Export an image of the plot in the specified format.
     *  If the specified format is not supported, then pop up a message
     *  window apologizing.
     *  @param out An output stream to which to send the description.
     *  @param formatName A format name, such as "gif" or "png".
     */
    public synchronized void exportImage(OutputStream out, String formatName) {
        try {
            boolean match = false;
            String[] supportedFormats = ImageIO.getWriterFormatNames();
            for (String supportedFormat : supportedFormats) {
                if (formatName.equalsIgnoreCase(supportedFormat)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                // This exception is caught and reported below.
                throw new Exception("Format " + formatName + " not supported.");
            }
            BufferedImage image = exportImage();
            if (out == null) {
                // FIXME: Write image to the clipboard.
                // final Clipboard clipboard = getToolkit().getSystemClipboard();
                String message = "Copy to the clipboard is not implemented yet.";
                JOptionPane.showMessageDialog(this, message,
                        "Ptolemy Plot Message", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ImageIO.write(image, formatName, out);
        } catch (Exception ex) {
            String message = "Export failed: " + ex.getMessage();
            JOptionPane.showMessageDialog(this, message, "Ptolemy Plot Message",
                    JOptionPane.ERROR_MESSAGE);

            // Rethrow the exception so that we don't report success,
            // and so the stack trace is displayed on standard out.
            throw (RuntimeException) ex.fillInStackTrace();
        }
    }

    /** Rescale so that the data that is currently plotted just fits.
     *  This is done based on the protected variables _xBottom, _xTop,
     *  _yBottom, and _yTop.  It is up to derived classes to ensure that
     *  variables are valid.
     *  This method calls repaint(), which eventually causes the display
     *  to be updated.
     */
    @Override
    public synchronized void fillPlot() {
        // NOTE: These used to be _setXRange() and _setYRange() to avoid
        // confusing this with user-specified ranges.  But we want to treat
        // a fill command as a user specified range.
        // EAL, 6/12/00.
        setXRange(_xBottom, _xTop);
        setYRange(_yBottom, _yTop);
        repaint();

        // Reacquire the focus so that key bindings work.
        // NOTE: no longer needed?
        // requestFocus();
    }

    /** Get the captions.
     *  @return the captions
     *  @see #addCaptionLine(String)
     *  @see #setCaptions(Vector)
     */
    @Override
    public Vector<String> getCaptions() {
        return _captionStrings;
    }

    /** Return whether the plot uses color.
     *  @return True if the plot uses color.
     *  @see #setColor(boolean)
     */
    @Override
    public boolean getColor() {
        return _usecolor;
    }

    /** Get the point colors.
     *  @return Array of colors
     *  @see #setColors(Color[])
     */
    @Override
    public Color[] getColors() {
        return _colors;
    }

    /** Convert a color name into a Color. Currently, only a very limited
     *  set of color names is supported: black, white, red, green, and blue.
     *  @param name A color name, or null if not found.
     *  @return An instance of Color.
     */
    public static Color getColorByName(String name) {
        try {
            // Check to see if it is a hexadecimal
            if (name.startsWith("#")) {
                name = name.substring(1);
            }

			return new Color(Integer.parseInt(name, 16));
        } catch (NumberFormatException ignored) {
        }

        // FIXME: This is a poor excuse for a list of colors and values.
        // We should use a hash table here.
        // Note that Color decode() wants the values to start with 0x.
        String[][] names = { { "black", "00000" }, { "white", "ffffff" },
                { "red", "ff0000" }, { "green", "00ff00" },
                { "blue", "0000ff" } };

        for (String[] name2 : names) {
            if (name.equals(name2[0])) {
                try {
					return new Color(Integer.parseInt(name2[1], 16));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return null;
    }

    /** Get the file specification that was given by setDataurl().
     *  @return the file specification
     *  @see #setDataurl(String)
     *  @deprecated Use read() instead.
     */
    @Deprecated
    @Override
    public String getDataurl() {
        return _filespec;
    }

    /** Get the document base that was set by setDocumentBase().
     *  @return the document base.
     *  @see #setDocumentBase(URL)
     *  @deprecated Use read() instead.
     */
    @Deprecated
    @Override
    public URL getDocumentBase() {
        return _documentBase;
    }

    /** Return whether the grid is drawn.
     *  @return True if a grid is drawn.
     *  @see #setGrid(boolean)
     */
    @Override
    public boolean getGrid() {
        return _grid;
    }

    /** Get the legend for a dataset, or null if there is none.
     *  The legend would have been set by addLegend().
     *  @param dataset The dataset index.
     *  @return The legend label, or null if there is none.
     */
    @Override
    public synchronized String getLegend(int dataset) {
        int idx = _legendDatasets.indexOf(dataset, 0);

        if (idx != -1) {
            return _legendStrings.elementAt(idx);
        } else {
            return null;
        }
    }

    /** Given a legend string, return the corresponding dataset or -1 if no
     *  legend was added with that legend string
     *  The legend would have been set by addLegend().
     *  @param legend The String naming the legend
     *  @return The legend dataset, or -1 if not found.
     *  @since Ptplot 5.2p1
     */
    @Override
    public synchronized int getLegendDataset(String legend) {
        int index = _legendStrings.indexOf(legend);

        if (index == -1) {
            return -1;
        }

        return _legendDatasets.get(index);
    }

    /** If the size of the plot has been set by setSize(),
     *  then return that size.  Otherwise, return what the superclass
     *  returns (which is undocumented, but apparently imposes no maximum size).
     *  Currently (JDK 1.3), only BoxLayout pays any attention to this.
     *  @return The maximum desired size.
     */

    //     public synchronized Dimension getMaximumSize() {
    //         if (_sizeHasBeenSet) {
    //             return new Dimension(_preferredWidth, _preferredHeight);
    //         } else {
    //             return super.getMaximumSize();
    //         }
    //     }
    /** Get the minimum size of this component.
     *  This is simply the dimensions specified by setSize(),
     *  if this has been called.  Otherwise, return whatever the base
     *  class returns, which is undocumented.
     *  @return The minimum size.
     */

    //     public synchronized Dimension getMinimumSize() {
    //         if (_sizeHasBeenSet) {
    //             return new Dimension(_preferredWidth, _preferredHeight);
    //         } else {
    //             return super.getMinimumSize();
    //         }
    //     }
    /** Get the current plot rectangle.
     *  Note that Rectangle returned by this method is calculated
     *  from the values of {@link #_ulx}, {@link #_uly},
     *  {@link #_lrx} and {@link #_lry}.  The value passed in by
     *  setPlotRectangle() is not directly used, thus calling
     *  getPlotRectangle() may not return the same rectangle that
     *  was passed in with setPlotRectangle().
     *  @return Rectangle
     *  @see #setPlotRectangle(Rectangle)
     */
    @Override
    public Rectangle getPlotRectangle() {
        return new Rectangle(_ulx, _uly, _lrx - _ulx, _lry - _uly);
    }

    /** Get the preferred size of this component.
     *  This is simply the dimensions specified by setSize(),
     *  if this has been called, or the default width and height
     *  otherwise (500 by 300).
     *  @return The preferred size.
     */
    @Override
    public synchronized Dimension getPreferredSize() {
        return new Dimension(_preferredWidth, _preferredHeight);
    }

    /** Get the title of the graph, or an empty string if there is none.
     *  @return The title.
     *  @see #setTitle(String)
     */
    @Override
    public synchronized String getTitle() {
        if (_title == null) {
            return "";
        }

        return _title;
    }

    /** Get the range for X values of the data points registered so far.
     *  Usually, derived classes handle managing the range by checking
     *  each new point against the current range.
     *  @return An array of two doubles where the first element is the
     *  minimum and the second element is the maximum.
     *  @see #getXRange()
     */
    @Override
    public synchronized double[] getXAutoRange() {
        double[] result = new double[2];
        result[0] = _xBottom;
        result[1] = _xTop;
        return result;
    }

    /** Get the label for the X (horizontal) axis, or null if none has
     *  been set.
     *  @return The X label.
     *  @see #setXLabel(String)
     */
    @Override
    public synchronized String getXLabel() {
        return _xlabel;
    }

    /** Return whether the X axis is drawn with a logarithmic scale.
     *  @return True if the X axis is logarithmic.
     *  @see #setXLog(boolean)
     */
    @Override
    public boolean getXLog() {
        return _xlog;
    }

    /** Get the X range. If {@link #setXRange(double, double)} has been
     *  called, then this method returns the values passed in as
     *  arguments to setXRange(double, double).  If setXRange(double,
     *  double) has not been called, then this method returns the
     *  range of the data to be plotted, which might not be all of the
     *  data due to zooming.
     *  @return An array of two doubles where the first element is the
     *  minimum and the second element is the maximum.
     *  @see #getXAutoRange()
     *  @see #setXRange(double, double)
     */
    @Override
    public synchronized double[] getXRange() {
        double[] result = new double[2];

        if (_xRangeGiven) {
            result[0] = _xlowgiven;
            result[1] = _xhighgiven;
        } else {
            // Have to first correct for the padding.
            result[0] = _xMin + (_xMax - _xMin) * _padding;
            result[1] = _xMax - (_xMax - _xMin) * _padding;
		}

        return result;
    }

    /** Get the X ticks that have been specified, or null if none.
     *  The return value is an array with two vectors, the first of
     *  which specifies the X tick locations (as instances of Double),
     *  and the second of which specifies the corresponding labels.
     *  @return The X ticks.
     */
    @Override
    public synchronized Vector<?>[] getXTicks() {
        if (_xticks == null) {
            return null;
        }

        Vector<?>[] result = new Vector<?>[2];
        result[0] = _xticks;
        result[1] = _xticklabels;
        return result;
    }

    /** Get the range for Y values of the data points registered so far.
     *  Usually, derived classes handle managing the range by checking
     *  each new point against the range.
     *  @return An array of two doubles where the first element is the
     *  minimum and the second element is the maximum.
     *  @see #getYRange()
     */
    @Override
    public synchronized double[] getYAutoRange() {
        double[] result = new double[2];
        result[0] = _yBottom;
        result[1] = _yTop;
        return result;
    }

    /** Get the label for the Y (vertical) axis, or null if none has
     *  been set.
     *  @return The Y label.
     *  @see #setYLabel(String)
     */
    @Override
    public String getYLabel() {
        return _ylabel;
    }

    /** Return whether the Y axis is drawn with a logarithmic scale.
     *  @return True if the Y axis is logarithmic.
     *  @see #setYLog(boolean)
     */
    @Override
    public boolean getYLog() {
        return _ylog;
    }

    /** Get the Y range. If {@link #setYRange(double, double)} has been
     *  called, then this method returns the values passed in as
     *  arguments to setYRange(double, double).  If setYRange(double,
     *  double) has not been called, then this method returns the
     *  range of the data to be plotted, which might not be all of the
     *  data due to zooming.
     *  @return An array of two doubles where the first element is the
     *  minimum and the second element is the maximum.
     *  @see #getYAutoRange()
     *  @see #setYRange(double, double)
     */
    @Override
    public synchronized double[] getYRange() {
        double[] result = new double[2];

        if (_yRangeGiven) {
            result[0] = _ylowgiven;
            result[1] = _yhighgiven;
        } else {
            // Have to first correct for the padding.
            result[0] = _yMin + (_yMax - _yMin) * _padding;
            result[1] = _yMax - (_yMax - _yMin) * _padding;
		}

        return result;
    }

    /** Get the Y ticks that have been specified, or null if none.
     *  The return value is an array with two vectors, the first of
     *  which specifies the Y tick locations (as instances of Double),
     *  and the second of which specifies the corresponding labels.
     *  @return The Y ticks.
     */
    @Override
    public synchronized Vector<?>[] getYTicks() {
        if (_yticks == null) {
            return null;
        }

        Vector<?>[] result = new Vector<?>[2];
		result[0] = _yticks;
        result[1] = _yticklabels;
        return result;
    }

    /** Paint the component contents, which in this base class is
     *  only the axes.
     *  @param graphics The graphics context.
     */
    @Override
    public synchronized void paintComponent(Graphics graphics) {
        //  super.paintComponent(graphics);
        //         _drawPlot(graphics, true);
        BufferedImage newPlotImage = _plotImage;

        if (newPlotImage == null) {
            Rectangle bounds = getBounds();
            newPlotImage = new BufferedImage(bounds.width, bounds.height,
                    BufferedImage.TYPE_3BYTE_BGR);
            _plotImage = newPlotImage;

            Graphics2D offScreenGraphics = newPlotImage.createGraphics();
            offScreenGraphics.addRenderingHints(_defaultImageRenderingHints());
            super.paintComponent(offScreenGraphics);
            _drawPlot(offScreenGraphics, true);
        }

        // Blit the offscreen image onto the screen.
        graphics.drawImage(newPlotImage, 0, 0, null);

        // Acquire the focus so that key bindings work.
        // NOTE: no longer needed?
        // requestFocus();
    }

    /** Print the plot to a printer, represented by the specified graphics
     *  object.
     *  @param graphics The context into which the page is drawn.
     *  @param format The size and orientation of the page being drawn.
     *  @param index The zero based index of the page to be drawn.
     *  @return PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     *  @exception PrinterException If the print job is terminated.
     */
    @Override
    public synchronized int print(Graphics graphics, PageFormat format,
            int index) {

        if (graphics == null) {
            return Printable.NO_SUCH_PAGE;
        }

        // We only print on one page.
        if (index >= 1) {
            return Printable.NO_SUCH_PAGE;
        }

        Graphics2D graphics2D = (Graphics2D) graphics;

        // Scale the printout to fit the pages.
        // Contributed by Laurent ETUR, Schlumberger Riboud Product Center
        double scalex = format.getImageableWidth() / getWidth();
        double scaley = format.getImageableHeight() / getHeight();
        double scale = Math.min(scalex, scaley);
        graphics2D.translate((int) format.getImageableX(),
                (int) format.getImageableY());
        graphics2D.scale(scale, scale);
        _drawPlot(graphics, true);
        return Printable.PAGE_EXISTS;
    }

    /** Read a single line command provided as a string.
     *  The commands can be any of those in the ASCII file format.
     *  @param command A command.
     */
    @Override
    public synchronized void read(String command) {
        _parseLine(command);
    }

    /** Remove the legend (displayed at the upper right) for the specified
     *  data set. If the dataset is not found, nothing will occur.
     *  The PlotBox must be repainted in order for this to take effect.
     *  @param dataset The dataset index.
     */
    @Override
    public synchronized void removeLegend(int dataset) {
        final int len = _legendDatasets.size();
        int foundIndex = -1;
        boolean found = false;

        for (int i = 0; i < len && !found; ++i) {
            if (_legendDatasets.get(i) == dataset) {
                foundIndex = i;
                found = true;
            }
        }

        if (found) {
            _legendDatasets.remove(foundIndex);
            _legendStrings.remove(foundIndex);
        }
    }

    /** Rename a legend.
     *  @param dataset The dataset of the legend to be renamed.
     *  If there is no dataset with this value, then nothing happens.
     *  @param newName  The new name of legend.
     *  @see #addLegend(int, String)
     */
    @Override
    public synchronized void renameLegend(int dataset, String newName) {
        int index = _legendDatasets.indexOf(dataset, 0);

        if (index != -1) {
            _legendStrings.setElementAt(newName, index);

            // Changing legend means we need to repaint the offscreen buffer.
            _plotImage = null;
        }
    }

    /** Reset the X and Y axes to the ranges that were first specified
     *  using setXRange() and setYRange(). If these methods have not been
     *  called, then reset to the default ranges.
     *  This method calls repaint(), which eventually causes the display
     *  to be updated.
     */
    @Override
    public synchronized void resetAxes() {
        setXRange(_originalXlow, _originalXhigh);
        setYRange(_originalYlow, _originalYhigh);
        repaint();
    }

    /** Do nothing in this base class. Derived classes might want to override
     *  this class to give an example of their use.
     */
    @Override
    public void samplePlot() {
        // Empty default implementation.
    }

    /**
     * Set automatic rescale. Automatic rescaling is enabled
     * when automaticRescale equals true and disabled when
     * automaticRescale equals false.
     * @param automaticRescale The boolean that specifies whether
     * plots should be automatic rescaled.
     */
    @Override
    public void setAutomaticRescale(boolean automaticRescale) {
        _automaticRescale = automaticRescale;
        if (automaticRescale) {
            if (_timerTask == null) {
                _timerTask = new TimedRepaint();
            }
            _timerTask.addListener(this);
        } else if (!_timedRepaint) {
            _resetScheduledTasks();
            if (_timerTask != null) {
                _timerTask.removeListener(this);
                _timerTask = null;
            }
        }
    }

    /** Set the background color.
     *  @param background The background color.
     */
    @Override
    public synchronized void setBackground(Color background) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _background = background;
        super.setBackground(_background);
    }

    /** Set the background color.
     *  This is syntactic sugar for use in implementations that separate
     *  the graphical display from the computational engine.
     *  Implements the {@link PlotBoxInterface}.
     *  @param background The background Color.
     */
    @Override
    public void setBackground(Object background) {
        setBackground((Color) background);
    }

    /** Move and resize this component. The new location of the top-left
     *  corner is specified by x and y, and the new size is specified by
     *  width and height. This overrides the base class method to make
     *  a record of the new size.
     *  @param x The new x-coordinate of this component.
     *  @param y The new y-coordinate of this component.
     *  @param width The new width of this component.
     *  @param height The new height of this component.
     */
    @Override
    public synchronized void setBounds(int x, int y, int width, int height) {
        _width = width;
        _height = height;

        // Resizing the component means we need to redraw the buffer.
        _plotImage = null;

        super.setBounds(x, y, _width, _height);
    }

    /** Set the strings of the caption.
     *  @param captionStrings A Vector where each element contains a String
     *  that is one line of the caption.
     *  @see #getCaptions()
     *  @see #clearCaptions()
     */
    @Override
    public void setCaptions(Vector<String> captionStrings) {
        // Changing caption means we need to repaint the offscreen buffer.
        _plotImage = null;
        _captionStrings = captionStrings;
    }

    /** If the argument is false, draw the plot without using color
     *  (in black and white).  Otherwise, draw it in color (the default).
     *  @param useColor False to draw in back and white.
     *  @see #getColor()
     */
    @Override
    public synchronized void setColor(boolean useColor) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _usecolor = useColor;
    }

    /** Set the point colors.  Note that the default colors have been
     *  carefully selected to maximize readability and that it is easy
     *  to use colors that result in a very ugly plot.
     *  @param colors Array of colors to use in succession for data sets.
     *  @see #getColors()
     */
    public synchronized void setColors(Color[] colors) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _colors = colors;
    }

    /** Set the array of colors
     *  Implements the {@link PlotBoxInterface}.
     *  @param colors The array of Colors.
     *  @see #getColors()
     */
    @Override
    public void setColors(Object[] colors) {
        setColors((Color[]) colors);
    }

    /** Set the file to read when init() is called.
     *  @param filespec the file to be read
     *  @see #getDataurl()
     *  @deprecated Use read() instead.
     */
    @Deprecated
    @Override
    public void setDataurl(String filespec) {
        _filespec = filespec;
    }

    /** Set the document base to used when init() is called to read a URL.
     *  @param documentBase The document base to be used.
     *  @see #getDocumentBase()
     *  @deprecated   Use read() instead.
     */
    @Deprecated
    @Override
    public void setDocumentBase(URL documentBase) {
        _documentBase = documentBase;
    }

    /** Set the foreground color.
     *  @param foreground The foreground color.
     */
    @Override
    public synchronized void setForeground(Color foreground) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _foreground = foreground;
        super.setForeground(_foreground);
    }

    /** Set the foreground color.
     *  Implements the {@link PlotBoxInterface}.
     *  @param foreground The foreground Color.
     */
    @Override
    public void setForeground(Object foreground) {
        setForeground((Color) foreground);
    }

    /** Control whether the grid is drawn.
     *  @param grid If true, a grid is drawn.
     *  @see #getGrid()
     */
    @Override
    public synchronized void setGrid(boolean grid) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _grid = grid;
    }

    /** Set the label font, which is used for axis labels and legend labels.
     *  The font names understood are those understood by
     *  java.awt.Font.decode().
     *  @param name A font name.
     */
    @Override
    public synchronized void setLabelFont(String name) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _labelFont = Font.decode(name);
        _labelFontMetrics = getFontMetrics(_labelFont);
    }

    /** Set the plot rectangle inside the axes.  This method
     *  can be used to create two plots that share the same axes.
     *  @param rectangle Rectangle space inside axes.
     *  @see #getPlotRectangle()
     */
    public synchronized void setPlotRectangle(Rectangle rectangle) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _specifiedPlotRectangle = rectangle;
    }

    /** Set the plot rectangle.
     *  Implements the {@link PlotBoxInterface}.
     *  @param rectangle The Rectangle.
     *  @see #getPlotRectangle()
     */
    @Override
    public void setPlotRectangle(Object rectangle) {
        setPlotRectangle((Rectangle) rectangle);
    }

    /** Set the size of the plot.  This overrides the base class to make
     *  it work.  In particular, it records the specified size so that
     *  getMinimumSize() and getPreferredSize() return the specified value.
     *  However, it only works if the plot is placed in its own JPanel.
     *  This is because the JPanel asks the contained component for
     *  its preferred size before determining the size of the panel.
     *  If the plot is placed directly in the content pane of a JApplet,
     *  then, mysteriously, this method has no effect.
     *  @param width The width, in pixels.
     *  @param height The height, in pixels.
     */
    @Override
    public synchronized void setSize(int width, int height) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _width = width;
        _height = height;
        _preferredWidth = width;
        _preferredHeight = height;

        //_sizeHasBeenSet = true;
        super.setSize(width, height);
    }

    /**
     * Set repainting with a certain fixed refresh rate. This timed
     * repainting is enabled when timedRepaint equals true and
     * disabled when timedRepaint equals false.
     * @param timedRepaint The boolean that specifies whether
     * repainting should happen with a certain fixed refresh rate.
     */
    @Override
    public void setTimedRepaint(boolean timedRepaint) {
        _timedRepaint = timedRepaint;
        if (timedRepaint) {
            if (_timerTask == null) {
                _timerTask = new TimedRepaint();
            }
            _timerTask.addListener(this);
        } else if (!_automaticRescale) {
            if (_timerTask != null) {
                _timerTask.removeListener(this);
                _timerTask = null;
            }
            _resetScheduledTasks();
        }
    }

    /** Set the title of the graph.
     *  @param title The title.
     *  @see #getTitle()
     */
    @Override
    public synchronized void setTitle(String title) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _title = title;
    }

    /** Set the title font.
     *  The font names understood are those understood by
     *  java.awt.Font.decode().
     *  @param name A font name.
     */
    @Override
    public synchronized void setTitleFont(String name) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _titleFont = Font.decode(name);
        _titleFontMetrics = getFontMetrics(_titleFont);
    }

    /** Specify whether the X axis is wrapped.
     *  If it is, then X values that are out of range are remapped
     *  to be in range using modulo arithmetic. The X range is determined
     *  by the most recent call to setXRange() (or the most recent zoom).
     *  If the X range has not been set, then use the default X range,
     *  or if data has been plotted, then the current fill range.
     *  @param wrap If true, wrapping of the X axis is enabled.
     */
    @Override
    public synchronized void setWrap(boolean wrap) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _wrap = wrap;

        if (!_xRangeGiven) {
            if (_xBottom > _xTop) {
                // have nothing to go on.
                setXRange(0, 0);
            } else {
                setXRange(_xBottom, _xTop);
            }
        }

        _wrapLow = _xlowgiven;
        _wrapHigh = _xhighgiven;
    }

    /** Set the label for the X (horizontal) axis.
     *  @param label The label.
     *  @see #getXLabel()
     */
    @Override
    public synchronized void setXLabel(String label) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _xlabel = label;
    }

    /** Specify whether the X axis is drawn with a logarithmic scale.
     *  If you would like to have the X axis drawn with a
     *  logarithmic axis, then setXLog(true) should be called before
     *  adding any data points.
     *  @param xlog If true, logarithmic axis is used.
     *  @see #getXLog()
     */
    @Override
    public synchronized void setXLog(boolean xlog) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _xlog = xlog;
    }

    /** Set the X (horizontal) range of the plot.  If this is not done
     *  explicitly, then the range is computed automatically from data
     *  available when the plot is drawn.  If min and max
     *  are identical, then the range is arbitrarily spread by 1.
     *  <p>Note that if {@link #setXLog(boolean)} has been called, then
     *  the min and max values should be in log units.
     *  @param min The left extent of the range.
     *  @param max The right extent of the range.
     *  @see #getXRange()
     */
    @Override
    public synchronized void setXRange(double min, double max) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _xRangeGiven = true;
        _xlowgiven = min;
        _xhighgiven = max;
        _setXRange(min, max);
    }

    /** Set the label for the Y (vertical) axis.
     *  @param label The label.
     *  @see #getYLabel()
     */
    @Override
    public synchronized void setYLabel(String label) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _ylabel = label;
    }

    /** Specify whether the Y axis is drawn with a logarithmic scale.
     *  If you would like to have the Y axis drawn with a
     *  logarithmic axis, then setYLog(true) should be called before
     *  adding any data points.
     *  @param ylog If true, logarithmic axis is used.
     *  @see #getYLog()
     */
    @Override
    public synchronized void setYLog(boolean ylog) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _ylog = ylog;
    }

    /** Set the Y (vertical) range of the plot.  If this is not done
     *  explicitly, then the range is computed automatically from data
     *  available when the plot is drawn.  If min and max are identical,
     *  then the range is arbitrarily spread by 0.1.
     *  <p>Note that if {@link #setYLog(boolean)} has been called, then
     *  the min and max values should be in log units.
     *  @param min The bottom extent of the range.
     *  @param max The top extent of the range.
     *  @see #getYRange()
     */
    @Override
    public synchronized void setYRange(double min, double max) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _yRangeGiven = true;
        _ylowgiven = min;
        _yhighgiven = max;
        _setYRange(min, max);
    }

    /** Write the current data and plot configuration to the specified
     *  stream in PlotML syntax.  PlotML is an XML extension for plot
     *  data.  The written information is includes a reference to the
     *  PlotML dtd on the the Ptolemy website.  The output is
     *  buffered, and is flushed and but not closed before exiting.
     *  Derived classes should override writeFormat and writeData
     *  rather than this method.
     *  @param out An output stream.
     */
    @Override
    public void write(OutputStream out) {
        write(out, null);
    }

    /** Write the current data and plot configuration to the
     *  specified stream in PlotML syntax.  PlotML is an XML
     *  scheme for plot data. The URL (relative or absolute) for the DTD is
     *  given as the second argument.  If that argument is null,
     *  then the PlotML PUBLIC DTD is referenced, resulting in a file
     *  that can be read by a PlotML parser without any external file
     *  references, as long as that parser has local access to the DTD.
     *  The output is buffered, and is flushed but
     *  not closed before exiting.  Derived classes should override
     *  writeFormat and writeData rather than this method.
     *  @param out An output stream.
     *  @param dtd The reference (URL) for the DTD, or null to use the
     *   PUBLIC DTD.
     */
    @Override
    public synchronized void write(OutputStream out, String dtd) {
        write(new OutputStreamWriter(out,
                java.nio.charset.Charset.defaultCharset()), dtd);
    }

    /** Write the current data and plot configuration to the
     *  specified stream in PlotML syntax.  PlotML is an XML
     *  scheme for plot data. The URL (relative or absolute) for the DTD is
     *  given as the second argument.  If that argument is null,
     *  then the PlotML PUBLIC DTD is referenced, resulting in a file
     *  that can be read by a PlotML parser without any external file
     *  references, as long as that parser has local access to the DTD.
     *  The output is buffered, and is flushed before exiting.
     *  @param out An output writer.
     *  @param dtd The reference (URL) for the DTD, or null to use the
     *   PUBLIC DTD.
     */
    @Override
    public synchronized void write(Writer out, String dtd) {
        // Auto-flush is disabled.
        PrintWriter output = new PrintWriter(new BufferedWriter(out), false);

        if (dtd == null) {
            output.println("<?xml version=\"1.0\" standalone=\"yes\"?>");
            output.println(
                    "<!DOCTYPE plot PUBLIC \"-//UC Berkeley//DTD PlotML 1//EN\"");
            output.println(
                    "    \"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd\">");
        } else {
            output.println("<?xml version=\"1.0\" standalone=\"no\"?>");
            output.println("<!DOCTYPE plot SYSTEM \"" + dtd + "\">");
        }

        output.println("<plot>");
        output.println("<!-- Ptolemy plot, version " + PTPLOT_RELEASE
                + " , PlotML format. -->");
        writeFormat(output);
        writeData(output);
        output.println("</plot>");
        output.flush();

        // NOTE: We used to close the stream, but if this is part
        // of an exportMoML operation, that is the wrong thing to do.
        // if (out != System.out) {
        //    output.close();
        // }
    }

    /** Write plot data information to the specified output stream in PlotML.
     *  In this base class, there is no data to write, so this method
     *  returns without doing anything.
     *  @param output A buffered print writer.
     */
    @Override
    public synchronized void writeData(PrintWriter output) {
    }

    /** Write plot format information to the specified output stream in PlotML.
     *  Derived classes should override this method to first call
     *  the parent class method, then add whatever additional format
     *  information they wish to add to the stream.
     *  @param output A buffered print writer.
     */
    @Override
    public synchronized void writeFormat(PrintWriter output) {
        // NOTE: If you modify this, you should change the _DTD variable
        // accordingly.
        if (_title != null) {
            output.println("<title>" + _title + "</title>");
        }

        if (_captionStrings != null) {
            for (Enumeration<String> captions = _captionStrings.elements(); captions
                    .hasMoreElements();) {
                String captionLine = captions.nextElement();
                output.println("<caption>" + captionLine + "</caption>");
            }
        }

        if (_xlabel != null) {
            output.println("<xLabel>" + _xlabel + "</xLabel>");
        }

        if (_ylabel != null) {
            output.println("<yLabel>" + _ylabel + "</yLabel>");
        }

        if (_xRangeGiven) {
            output.println("<xRange min=\"" + _xlowgiven + "\" max=\""
                    + _xhighgiven + "\"/>");
        }

        if (_yRangeGiven) {
            output.println("<yRange min=\"" + _ylowgiven + "\" max=\""
                    + _yhighgiven + "\"/>");
        }

        if (_xticks != null && !_xticks.isEmpty()) {
            output.println("<xTicks>");

            int last = _xticks.size() - 1;

            for (int i = 0; i <= last; i++) {
                output.println("  <tick label=\""
                        + _xticklabels.elementAt(i) + "\" position=\""
                        + _xticks.elementAt(i) + "\"/>");
            }

            output.println("</xTicks>");
        }

        if (_yticks != null && !_yticks.isEmpty()) {
            output.println("<yTicks>");

            int last = _yticks.size() - 1;

            for (int i = 0; i <= last; i++) {
                output.println("  <tick label=\""
                        + _yticklabels.elementAt(i) + "\" position=\""
                        + _yticks.elementAt(i) + "\"/>");
            }

            output.println("</yTicks>");
        }

        if (_xlog) {
            output.println("<xLog/>");
        }

        if (_ylog) {
            output.println("<yLog/>");
        }

        if (!_grid) {
            output.println("<noGrid/>");
        }

        if (_wrap) {
            output.println("<wrap/>");
        }

        if (!_usecolor) {
            output.println("<noColor/>");
        }
    }

    /** Zoom in or out to the specified rectangle.
     *  This method calls repaint().
     *  @param lowx The low end of the new X range.
     *  @param lowy The low end of the new Y range.
     *  @param highx The high end of the new X range.
     *  @param highy The high end of the new Y range.
     */
    @Override
    public synchronized void zoom(double lowx, double lowy, double highx,
            double highy) {
        setXRange(lowx, highx);
        setYRange(lowy, highy);
        repaint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // If you change PTPLOT_RELEASE, modify the version numbers in:
    // doc/main.htm, doc/changes.htm, doc/install.htm, doc/download/index.htm
    /** The version of PtPlot. */
    public static final String PTPLOT_RELEASE = "5.11.devel";

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Return whether rescaling of the plot should happen
     * automatic.
     * @return True when rescaling of the plot should happen
     * automatic.
     */
    protected boolean _automaticRescale() {
        return _automaticRescale;
    }

    //    /** Copy the file with the given name from the $PTII/ptolemy/plot/latex directory
    //     *  to the specified directory.
    //     *  @param filename The name of the file to copy.
    //     *  @param directory The directory into which to copy.
    //     *  @exception FileNotFoundException If the file cannot be found.
    //     *  @exception IOException If the file cannot be copied.
    //     */
    //    private void _copyFiles(String filename, File directory)
    //            throws FileNotFoundException, IOException {
    //        URL url = FileUtilities.nameToURL("$CLASSPATH/ptolemy/plot/latex/" + filename, null, null);
    //        FileUtilities.binaryCopyURLToFile(url, new File(directory, filename));
    //    }

    /** Draw the axes using the current range, label, and title information.
     *  If the second argument is true, clear the display before redrawing.
     *  This method is called by paintComponent().  To cause it to be called
     *  you would normally call repaint(), which eventually causes
     *  paintComponent() to be called.
     *  <p>
     *  Note that this is synchronized so that points are not added
     *  by other threads while the drawing is occurring.  This method
     *  should be called only from the event dispatch thread, consistent
     *  with swing policy.
     *  @param graphics The graphics context.
     *  @param clearfirst If true, clear the plot before proceeding.
     */
    protected synchronized void _drawPlot(Graphics graphics,
            boolean clearfirst) {
        Rectangle bounds = getBounds();
        _drawPlot(graphics, clearfirst, bounds);
    }

    /** Draw the axes using the current range, label, and title information,
     *  at the size of the specified rectangle.
     *  If the second argument is true, clear the display before redrawing.
     *  This method is called by paintComponent().  To cause it to be called
     *  you would normally call repaint(), which eventually causes
     *  paintComponent() to be called.
     *  <p>
     *  Note that this is synchronized so that points are not added
     *  by other threads while the drawing is occurring.  This method
     *  should be called only from the event dispatch thread, consistent
     *  with swing policy.
     *  @param graphics The graphics context.
     *  @param clearfirst If true, clear the plot before proceeding.
     *  @param drawRect A specification of the size.
     */
    protected synchronized void _drawPlot(Graphics graphics, boolean clearfirst,
            Rectangle drawRect) {
        // Ignore if there is no graphics object to draw on.
        if (graphics == null) {
            return;
        }

        graphics.setPaintMode();

        /* NOTE: The following seems to be unnecessary with Swing...
         if (clearfirst) {
         // NOTE: calling clearRect() here permits the background
         // color to show through, but it messes up printing.
         // Printing results in black-on-black title and axis labels.
         graphics.setColor(_background);
         graphics.drawRect(0, 0, drawRect.width, drawRect.height);
         graphics.setColor(Color.black);
         }
         */

        // If an error message has been set, display it and return.
        if (_errorMsg != null) {
            int fheight = _labelFontMetrics.getHeight() + 2;
            int msgy = fheight;
            graphics.setColor(Color.black);

            for (String element : _errorMsg) {
                graphics.drawString(element, 10, msgy);
                msgy += fheight;
                System.err.println(element);
            }

            return;
        }

        // Make sure we have an x and y range
        if (!_xRangeGiven) {
            if (_xBottom > _xTop) {
                // have nothing to go on.
                _setXRange(0, 0);
            } else {
                _setXRange(_xBottom, _xTop);
            }
        }

        if (!_yRangeGiven) {
            if (_yBottom > _yTop) {
                // have nothing to go on.
                _setYRange(0, 0);
            } else {
                _setYRange(_yBottom, _yTop);
            }
        }

        // If user specified a plot rectangle, compute
        // a working plot rectangle which lies inside the
        // drawRect at the user specified coordinates
        Rectangle workingPlotRectangle = null;

        if (_specifiedPlotRectangle != null) {
            workingPlotRectangle = new Rectangle(
                    Math.max(0, _specifiedPlotRectangle.x),
                    Math.max(0, _specifiedPlotRectangle.y),
                    Math.min(drawRect.width, _specifiedPlotRectangle.width),
                    Math.min(drawRect.height, _specifiedPlotRectangle.height));
        }

        // Vertical space for title, if appropriate.
        // NOTE: We assume a one-line title.
        int titley = 0;
        int titlefontheight = _titleFontMetrics.getHeight();

        if (_title == null) {
            // NOTE: If the _title is null, then set it to the empty
            // string to solve the problem where the fill button overlaps
            // the legend if there is no title.  The fix here would
            // be to modify the legend printing text so that it takes
            // into account the case where there is no title by offsetting
            // just enough for the button.
            _title = "";
        }

        titley = titlefontheight + _topPadding;

        int captionHeight = _captionStrings.size()
                * _captionFontMetrics.getHeight();
        if (captionHeight > 0) {
            captionHeight += 5; //extra padding
        }

        // Number of vertical tick marks depends on the height of the font
        // for labeling ticks and the height of the window.
        Font previousFont = graphics.getFont();
        graphics.setFont(_labelFont);
        graphics.setColor(_foreground); // foreground color not set here  --Rob.

        int labelheight = _labelFontMetrics.getHeight();
        int halflabelheight = labelheight / 2;

        // Draw scaling annotation for x axis.
        // NOTE: 5 pixel padding on bottom.
        int ySPos = drawRect.height - captionHeight - 5;
        int xSPos = drawRect.width - _rightPadding;

        if (_xlog) {
            _xExp = (int) Math.floor(_xtickMin);
        }

        if (_xExp != 0 && _xticks == null) {
            String superscript = Integer.toString(_xExp);
            xSPos -= _superscriptFontMetrics.stringWidth(superscript);
            graphics.setFont(_superscriptFont);

            if (!_xlog) {
                graphics.drawString(superscript, xSPos,
                        ySPos - halflabelheight);
                xSPos -= _labelFontMetrics.stringWidth("x10");
                graphics.setFont(_labelFont);
                graphics.drawString("x10", xSPos, ySPos);
            }

            // NOTE: 5 pixel padding on bottom
            _bottomPadding = 3 * labelheight / 2 + 5;
        }

        // NOTE: 5 pixel padding on the bottom.
        if (_xlabel != null
                && _bottomPadding < captionHeight + labelheight + 5) {
            _bottomPadding = captionHeight + labelheight + 5;
        }

        // Compute the space needed around the plot, starting with vertical.
        // NOTE: padding of 5 pixels below title.
        if (workingPlotRectangle != null) {
            _uly = workingPlotRectangle.y;
        } else {
            _uly = titley + 5;
        }

        // NOTE: 3 pixels above bottom labels.
        if (workingPlotRectangle != null) {
            _lry = workingPlotRectangle.y + workingPlotRectangle.height;
        } else {
            _lry = drawRect.height - labelheight - _bottomPadding - 3;
        }

        int height = _lry - _uly;
        _yscale = height / (_yMax - _yMin);
        _ytickscale = height / (_ytickMax - _ytickMin);

        ////////////////// vertical axis
        // Number of y tick marks.
        // NOTE: subjective spacing factor.
        int ny = 2 + height / (labelheight + 10);

        // Compute y increment.
        double yStep = _roundUp((_ytickMax - _ytickMin) / ny);

        // Compute y starting point so it is a multiple of yStep.
        double yStart = yStep * Math.ceil(_ytickMin / yStep);

        // NOTE: Following disables first tick.  Not a good idea?
        // if (yStart == _ytickMin) yStart += yStep;
        // Define the strings that will label the y axis.
        // Meanwhile, find the width of the widest label.
        // The labels are quantized so that they don't have excess resolution.
        int widesty = 0;

        // These do not get used unless ticks are automatic, but the
        // compiler is not smart enough to allow us to reference them
        // in two distinct conditional clauses unless they are
        // allocated outside the clauses.
        String[] ylabels = new String[ny];
        int[] ylabwidth = new int[ny];

        int ind = 0;

        if (_yticks == null) {
            Vector<Double> ygrid = null;

            if (_ylog) {
                ygrid = _gridInit(yStart, yStep, true, null);
            }

            // automatic ticks
            // First, figure out how many digits after the decimal point
            // will be used.
            int numfracdigits = _numFracDigits(yStep);

            // NOTE: Test cases kept in case they are needed again.
            // System.out.println("0.1 with 3 digits: " + _formatNum(0.1, 3));
            // System.out.println("0.0995 with 3 digits: " +
            //                    _formatNum(0.0995, 3));
            // System.out.println("0.9995 with 3 digits: " +
            //                    _formatNum(0.9995, 3));
            // System.out.println("1.9995 with 0 digits: " +
            //                    _formatNum(1.9995, 0));
            // System.out.println("1 with 3 digits: " + _formatNum(1, 3));
            // System.out.println("10 with 0 digits: " + _formatNum(10, 0));
            // System.out.println("997 with 3 digits: " + _formatNum(997, 3));
            // System.out.println("0.005 needs: " + _numFracDigits(0.005));
            // System.out.println("1 needs: " + _numFracDigits(1));
            // System.out.println("999 needs: " + _numFracDigits(999));
            // System.out.println("999.0001 needs: "+_numFracDigits(999.0001));
            // System.out.println("0.005 integer digits: " +
            //                    _numIntDigits(0.005));
            // System.out.println("1 integer digits: " + _numIntDigits(1));
            // System.out.println("999 integer digits: " + _numIntDigits(999));
            // System.out.println("-999.0001 integer digits: " +
            //                    _numIntDigits(999.0001));
            double yTmpStart = yStart;

            if (_ylog) {
                yTmpStart = _gridStep(ygrid, yStart, yStep, _ylog);
            }

            for (double ypos = yTmpStart; ypos <= _ytickMax; ypos = _gridStep(
                    ygrid, ypos, yStep, _ylog)) {
                // Prevent out of bounds exceptions
                if (ind >= ny) {
                    break;
                }

                String yticklabel;

                if (_ylog) {
                    yticklabel = _formatLogNum(ypos, numfracdigits);
                } else {
                    yticklabel = _formatNum(ypos, numfracdigits);
                }

                ylabels[ind] = yticklabel;

                int lw = _labelFontMetrics.stringWidth(yticklabel);
                ylabwidth[ind++] = lw;

                if (lw > widesty) {
                    widesty = lw;
                }
            }
        } else {
            // explicitly specified ticks
            Enumeration<String> nl = _yticklabels.elements();

            while (nl.hasMoreElements()) {
                String label = nl.nextElement();
                int lw = _labelFontMetrics.stringWidth(label);

                if (lw > widesty) {
                    widesty = lw;
                }
            }
        }

        // Next we do the horizontal spacing.
        if (workingPlotRectangle != null) {
            _ulx = workingPlotRectangle.x;
        } else {
            if (_ylabel != null) {
                _ulx = widesty + _labelFontMetrics.stringWidth("W")
                        + _leftPadding;
            } else {
                _ulx = widesty + _leftPadding;
            }
        }

        int legendwidth = _drawLegend(graphics, drawRect.width - _rightPadding,
                _uly);

        if (workingPlotRectangle != null) {
            _lrx = workingPlotRectangle.x + workingPlotRectangle.width;
        } else {
            _lrx = drawRect.width - legendwidth - _rightPadding;
        }

        int width = _lrx - _ulx;
        _xscale = width / (_xMax - _xMin);

        _xtickscale = width / (_xtickMax - _xtickMin);

        // Background for the plotting rectangle.
        // Always use a white background because the dataset colors
        // were designed for a white background.
        graphics.setColor(Color.white);
        graphics.fillRect(_ulx, _uly, width, height);

        graphics.setColor(_foreground);
        graphics.drawRect(_ulx, _uly, width, height);

        // NOTE: subjective tick length.
        int tickLength = 5;
        int xCoord1 = _ulx + tickLength;
        int xCoord2 = _lrx - tickLength;

        if (_yticks == null) {
            // auto-ticks
            Vector<Double> ygrid = null;
            double yTmpStart = yStart;

            if (_ylog) {
                ygrid = _gridInit(yStart, yStep, true, null);
                yTmpStart = _gridStep(ygrid, yStart, yStep, _ylog);
                ny = ind;
            }

            ind = 0;

            // Set to false if we don't need the exponent
            boolean needExponent = _ylog;

            for (double ypos = yTmpStart; ypos <= _ytickMax; ypos = _gridStep(
                    ygrid, ypos, yStep, _ylog)) {
                // Prevent out of bounds exceptions
                if (ind >= ny) {
                    break;
                }

                int yCoord1 = _lry - (int) ((ypos - _ytickMin) * _ytickscale);

                // The lowest label is shifted up slightly to avoid
                // colliding with x labels.
                int offset = 0;

                if (ind > 0 && !_ylog) {
                    offset = halflabelheight;
                }

                graphics.drawLine(_ulx, yCoord1, xCoord1, yCoord1);
                graphics.drawLine(_lrx, yCoord1, xCoord2, yCoord1);

                if (_grid && yCoord1 != _uly && yCoord1 != _lry) {
                    graphics.setColor(Color.lightGray);
                    graphics.drawLine(xCoord1, yCoord1, xCoord2, yCoord1);
                    graphics.setColor(_foreground);
                }

                // Check to see if any of the labels printed contain
                // the exponent.  If we don't see an exponent, then print it.
                if (_ylog && ylabels[ind].indexOf('e') != -1) {
                    needExponent = false;
                }

                // NOTE: 4 pixel spacing between axis and labels.
                graphics.drawString(ylabels[ind], _ulx - ylabwidth[ind++] - 4,
                        yCoord1 + offset);
            }

            if (_ylog) {
                // Draw in grid lines that don't have labels.
                Vector<Double> unlabeledgrid = _gridInit(yStart, yStep, false, ygrid);

                if (!unlabeledgrid.isEmpty()) {
                    // If the step is greater than 1, clamp it to 1 so that
                    // we draw the unlabeled grid lines for each
                    //integer interval.
                    double tmpStep = Math.min(yStep, 1.0);

                    for (double ypos = _gridStep(unlabeledgrid, yStart, tmpStep,
                            _ylog); ypos <= _ytickMax; ypos = _gridStep(
                                    unlabeledgrid, ypos, tmpStep, _ylog)) {
                        int yCoord1 = _lry
                                - (int) ((ypos - _ytickMin) * _ytickscale);

                        if (_grid && yCoord1 != _uly && yCoord1 != _lry) {
                            graphics.setColor(Color.lightGray);
                            graphics.drawLine(_ulx + 1, yCoord1, _lrx - 1,
                                    yCoord1);
                            graphics.setColor(_foreground);
                        }
                    }
                }

                if (needExponent) {
                    // We zoomed in, so we need the exponent
                    _yExp = (int) Math.floor(yTmpStart);
                } else {
                    _yExp = 0;
                }
            }

            // Draw scaling annotation for y axis.
            if (_yExp != 0) {
                graphics.drawString("x10", 2, titley);
                graphics.setFont(_superscriptFont);
                graphics.drawString(Integer.toString(_yExp),
                        _labelFontMetrics.stringWidth("x10") + 2,
                        titley - halflabelheight);
                graphics.setFont(_labelFont);
            }
        } else {
            // ticks have been explicitly specified
            Enumeration<Double> nt = _yticks.elements();
            Enumeration<String> nl = _yticklabels.elements();

            while (nl.hasMoreElements()) {
                String label = nl.nextElement();
                double ypos = nt.nextElement();

                if (ypos > _yMax || ypos < _yMin) {
                    continue;
                }

                int yCoord1 = _lry - (int) ((ypos - _yMin) * _yscale);
                int offset = 0;

                if (ypos < _lry - labelheight) {
                    offset = halflabelheight;
                }

                graphics.drawLine(_ulx, yCoord1, xCoord1, yCoord1);
                graphics.drawLine(_lrx, yCoord1, xCoord2, yCoord1);

                if (_grid && yCoord1 != _uly && yCoord1 != _lry) {
                    graphics.setColor(Color.lightGray);
                    graphics.drawLine(xCoord1, yCoord1, xCoord2, yCoord1);
                    graphics.setColor(_foreground);
                }

                // NOTE: 3 pixel spacing between axis and labels.
                graphics.drawString(label,
                        _ulx - _labelFontMetrics.stringWidth(label) - 3,
                        yCoord1 + offset);
            }
        }

        //////////////////// horizontal axis
        int yCoord1 = _uly + tickLength;
        int yCoord2 = _lry - tickLength;
        int charwidth = _labelFontMetrics.stringWidth("8");

        if (_xticks == null) {
            // auto-ticks
            // Number of x tick marks.
            // Need to start with a guess and converge on a solution here.
            int nx = 10;
            double xStep = 0.0;
            int numfracdigits = 0;

            if (_xlog) {
                // X axes log labels will be at most 6 chars: -1E-02
                nx = 2 + width / (charwidth * 6 + 10);
            } else {
                // Limit to 10 iterations
                int count = 0;

                while (count++ <= 10) {
                    xStep = _roundUp((_xtickMax - _xtickMin) / nx);

                    // Compute the width of a label for this xStep
                    numfracdigits = _numFracDigits(xStep);

                    // Number of integer digits is the maximum of two endpoints
                    int intdigits = _numIntDigits(_xtickMax);
                    int inttemp = _numIntDigits(_xtickMin);

                    if (intdigits < inttemp) {
                        intdigits = inttemp;
                    }

                    // Allow two extra digits (decimal point and sign).
                    int maxlabelwidth = charwidth
                            * (numfracdigits + 2 + intdigits);

                    // Compute new estimate of number of ticks.
                    int savenx = nx;

                    // NOTE: 10 additional pixels between labels.
                    // NOTE: Try to ensure at least two tick marks.
                    nx = 2 + width / (maxlabelwidth + 10);

                    if (nx - savenx <= 1 || savenx - nx <= 1) {
                        break;
                    }
                }
            }

            xStep = _roundUp((_xtickMax - _xtickMin) / nx);
            numfracdigits = _numFracDigits(xStep);

            // Compute x starting point so it is a multiple of xStep.
            double xStart = xStep * Math.ceil(_xtickMin / xStep);

            // NOTE: Following disables first tick.  Not a good idea?
            // if (xStart == _xMin) xStart += xStep;
            Vector<Double> xgrid = null;
            double xTmpStart = xStart;

            if (_xlog) {
                xgrid = _gridInit(xStart, xStep, true, null);

                //xgrid = _gridInit(xStart, xStep);
                xTmpStart = _gridRoundUp(xgrid, xStart);
            }

            // Set to false if we don't need the exponent
            boolean needExponent = _xlog;

            // Label the x axis.  The labels are quantized so that
            // they don't have excess resolution.
            for (double xpos = xTmpStart; xpos <= _xtickMax; xpos = _gridStep(
                    xgrid, xpos, xStep, _xlog)) {
                String xticklabel;

                if (_xlog) {
                    xticklabel = _formatLogNum(xpos, numfracdigits);

                    if (xticklabel.indexOf('e') != -1) {
                        needExponent = false;
                    }
                } else {
                    xticklabel = _formatNum(xpos, numfracdigits);
                }

                xCoord1 = _ulx + (int) ((xpos - _xtickMin) * _xtickscale);
                graphics.drawLine(xCoord1, _uly, xCoord1, yCoord1);
                graphics.drawLine(xCoord1, _lry, xCoord1, yCoord2);

                if (_grid && xCoord1 != _ulx && xCoord1 != _lrx) {
                    graphics.setColor(Color.lightGray);
                    graphics.drawLine(xCoord1, yCoord1, xCoord1, yCoord2);
                    graphics.setColor(_foreground);
                }

                int labxpos = xCoord1
                        - _labelFontMetrics.stringWidth(xticklabel) / 2;

                // NOTE: 3 pixel spacing between axis and labels.
                graphics.drawString(xticklabel, labxpos,
                        _lry + 3 + labelheight);
            }

            if (_xlog) {
                // Draw in grid lines that don't have labels.
                // If the step is greater than 1, clamp it to 1 so that
                // we draw the unlabeled grid lines for each
                // integer interval.
                double tmpStep = Math.min(xStep, 1.0);

                // Recalculate the start using the new step.
                xTmpStart = tmpStep * Math.ceil(_xtickMin / tmpStep);

                Vector<Double> unlabeledgrid = _gridInit(xTmpStart, tmpStep, false,
                        xgrid);

                if (!unlabeledgrid.isEmpty()) {
                    for (double xpos = _gridStep(unlabeledgrid, xTmpStart,
                            tmpStep,
                            _xlog); xpos <= _xtickMax; xpos = _gridStep(
                                    unlabeledgrid, xpos, tmpStep, _xlog)) {
                        xCoord1 = _ulx
                                + (int) ((xpos - _xtickMin) * _xtickscale);

                        if (_grid && xCoord1 != _ulx && xCoord1 != _lrx) {
                            graphics.setColor(Color.lightGray);
                            graphics.drawLine(xCoord1, _uly + 1, xCoord1,
                                    _lry - 1);
                            graphics.setColor(_foreground);
                        }
                    }
                }

                if (needExponent) {
                    _xExp = (int) Math.floor(xTmpStart);
                    graphics.setFont(_superscriptFont);
                    graphics.drawString(Integer.toString(_xExp), xSPos,
                            ySPos - halflabelheight);
                    xSPos -= _labelFontMetrics.stringWidth("x10");
                    graphics.setFont(_labelFont);
                    graphics.drawString("x10", xSPos, ySPos);
                } else {
                    _xExp = 0;
                }
            }
        } else {
            // ticks have been explicitly specified
            Enumeration<Double> nt = _xticks.elements();
            Enumeration<String> nl = _xticklabels.elements();

            // Code contributed by Jun Wu (jwu@inin.com.au)
            double preLength = 0.0;

            while (nl.hasMoreElements()) {
                String label = nl.nextElement();
                double xpos = nt.nextElement();

                // If xpos is out of range, ignore.
                if (xpos > _xMax || xpos < _xMin) {
                    continue;
                }

                // Find the center position of the label.
                xCoord1 = _ulx + (int) ((xpos - _xMin) * _xscale);

                // Find  the start position of x label.
                int labxpos = xCoord1
                        - _labelFontMetrics.stringWidth(label) / 2;

                // If the labels are not overlapped, proceed.
                if (labxpos > preLength) {
                    // calculate the length of the label
                    preLength = xCoord1
                            + _labelFontMetrics.stringWidth(label) / 2f + 10;

                    // Draw the label.
                    // NOTE: 3 pixel spacing between axis and labels.
                    graphics.drawString(label, labxpos, _lry + 3 + labelheight);

                    // Draw the label mark on the axis
                    graphics.drawLine(xCoord1, _uly, xCoord1, yCoord1);
                    graphics.drawLine(xCoord1, _lry, xCoord1, yCoord2);

                    // Draw the grid line
                    if (_grid && xCoord1 != _ulx && xCoord1 != _lrx) {
                        graphics.setColor(Color.lightGray);
                        graphics.drawLine(xCoord1, yCoord1, xCoord1, yCoord2);
                        graphics.setColor(_foreground);
                    }
                }
            }
        }

        //////////////////// Draw title and axis labels now.
        // Center the title and X label over the plotting region, not
        // the window.
        graphics.setColor(_foreground);

        if (_title != null) {
            graphics.setFont(_titleFont);

            int titlex = _ulx
                    + (width - _titleFontMetrics.stringWidth(_title)) / 2;
            graphics.drawString(_title, titlex, titley);
        }

        graphics.setFont(_labelFont);

        if (_xlabel != null) {
            int labelx = _ulx
                    + (width - _labelFontMetrics.stringWidth(_xlabel)) / 2;
            graphics.drawString(_xlabel, labelx, ySPos);
        }

        int charcenter = 2 + _labelFontMetrics.stringWidth("W") / 2;

        if (_ylabel != null) {
            int yl = _ylabel.length();

            if (graphics instanceof Graphics2D g2d) {
                int starty = _uly + (_lry - _uly) / 2
                        + _labelFontMetrics.stringWidth(_ylabel) / 2
                        - charwidth;

				// NOTE: Fudge factor so label doesn't touch axis labels.
                int startx = charcenter + halflabelheight - 2;
                g2d.rotate(Math.toRadians(-90), startx, starty);
                g2d.drawString(_ylabel, startx, starty);
                g2d.rotate(Math.toRadians(90), startx, starty);
            } else {
                // Not graphics 2D, no support for rotation.
                // Vertical label is fairly complex to draw.
                int starty = _uly + (_lry - _uly) / 2 - yl * halflabelheight
                        + labelheight;

                for (int i = 0; i < yl; i++) {
                    String nchar = _ylabel.substring(i, i + 1);
                    int cwidth = _labelFontMetrics.stringWidth(nchar);
                    graphics.drawString(nchar, charcenter - cwidth / 2, starty);
                    starty += labelheight;
                }
            }
        }

        graphics.setFont(_captionFont);
        int fontHt = _captionFontMetrics.getHeight();
        int yCapPosn = drawRect.height - captionHeight + 14;
        for (Enumeration<String> captions = _captionStrings.elements(); captions
                .hasMoreElements();) {
            String captionLine = captions.nextElement();
            int labelx = _ulx
                    + (width - _captionFontMetrics.stringWidth(captionLine))
                            / 2;
            graphics.drawString(captionLine, labelx, yCapPosn);
            yCapPosn += fontHt;
        }
        graphics.setFont(previousFont);
    }

    /** Put a mark corresponding to the specified dataset at the
     *  specified x and y position.   The mark is drawn in the
     *  current color.  In this base class, a point is a
     *  filled rectangle 6 pixels across.  Note that marks greater than
     *  about 6 pixels in size will not look very good since they will
     *  overlap axis labels and may not fit well in the legend.   The
     *  <i>clip</i> argument, if <code>true</code>, states
     *  that the point should not be drawn if
     *  it is out of range.
     *
     *  Note that this method is not synchronized, so the caller should be.
     *  Moreover this method should always be called from the event thread
     *  when being used to write to the screen.
     *
     *  @param graphics The graphics context.
     *  @param dataset The index of the data set.
     *  @param xpos The X position.
     *  @param ypos The Y position.
     *  @param clip If true, do not draw if out of range.
     */
    protected void _drawPoint(Graphics graphics, int dataset, long xpos,
            long ypos, boolean clip) {
        // Ignore if there is no graphics object to draw on.
        if (graphics == null) {
            return;
        }

        boolean pointinside = ypos <= _lry && ypos >= _uly && xpos <= _lrx
                && xpos >= _ulx;

        if (!pointinside && clip) {
            return;
        }

        graphics.fillRect((int) xpos - 6, (int) ypos - 6, 6, 6);
    }

    /** Return Latex plot data. This base class returns nothing.
     *  @return A string suitable for inclusion in Latex.
     */
    protected String _exportLatexPlotData() {
        return "";
    }

    /** Display basic information in its own window.
     */
    protected void _help() {
        String message = "Ptolemy plot package\n" + "By: Edward A. Lee\n"
                + "and Christopher Brooks\n" + "Version " + PTPLOT_RELEASE
                + ", Build: $Id$\n\n" + "Key bindings:\n"
                + "   Cntrl-c:  copy plot to clipboard (PNG format), if permitted\n"
                + "   D: dump plot data to standard out\n"
                + "   E: export plot to standard out (EPS format)\n"
                + "   F: fill plot\n"
                + "   H or ?: print help message (this message)\n"
                + "   Cntrl-D or Q: quit\n" + "For more information, see\n"
                + "http://ptolemy.eecs.berkeley.edu/java/ptplot\n";
        JOptionPane.showMessageDialog(this, message, "Ptolemy Plot Help Window",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Parse a line that gives plotting information.  In this base
     *  class, only lines pertaining to the title and labels are processed.
     *  Everything else is ignored. Return true if the line is recognized.
     *  It is not synchronized, so its caller should be.
     *  @param line A line of text.
     *  @return True if the line was recognized.
     */
    protected boolean _parseLine(String line) {
        // If you modify this method, you should also modify write()
        // We convert the line to lower case so that the command
        // names are case insensitive.
        String lcLine = line.toLowerCase(Locale.getDefault());

        if (lcLine.startsWith("#")) {
            // comment character
            return true;
        } else if (lcLine.startsWith("titletext:")) {
            setTitle(line.substring(10).trim());
            return true;
        } else if (lcLine.startsWith("title:")) {
            // Tolerate alternative tag.
            setTitle(line.substring(6).trim());
            return true;
        } else if (lcLine.startsWith("xlabel:")) {
            setXLabel(line.substring(7).trim());
            return true;
        } else if (lcLine.startsWith("ylabel:")) {
            setYLabel(line.substring(7).trim());
            return true;
        } else if (lcLine.startsWith("xrange:")) {
            int comma = line.indexOf(",", 7);

            if (comma > 0) {
                String min = line.substring(7, comma).trim();
                String max = line.substring(comma + 1).trim();

                try {
                    double dmin = Double.parseDouble(min);
                    double dmax = Double.parseDouble(max);
                    setXRange(dmin, dmax);
                } catch (NumberFormatException e) {
                    // ignore if format is bogus.
                }
            }

            return true;
        } else if (lcLine.startsWith("yrange:")) {
            int comma = line.indexOf(",", 7);

            if (comma > 0) {
                String min = line.substring(7, comma).trim();
                String max = line.substring(comma + 1).trim();

                try {
                    double dmin = Double.parseDouble(min);
                    double dmax = Double.parseDouble(max);
                    setYRange(dmin, dmax);
                } catch (NumberFormatException e) {
                    // ignore if format is bogus.
                }
            }

            return true;
        } else if (lcLine.startsWith("xticks:")) {
            // example:
            // XTicks "label" 0, "label" 1, "label" 3
            _parsePairs(line.substring(7), true);
            return true;
        } else if (lcLine.startsWith("yticks:")) {
            // example:
            // YTicks "label" 0, "label" 1, "label" 3
            _parsePairs(line.substring(7), false);
            return true;
        } else if (lcLine.startsWith("xlog:")) {
			_xlog = lcLine.indexOf("off", 5) < 0;

            return true;
        } else if (lcLine.startsWith("ylog:")) {
			_ylog = lcLine.indexOf("off", 5) < 0;

            return true;
        } else if (lcLine.startsWith("grid:")) {
			_grid = lcLine.indexOf("off", 5) < 0;

            return true;
        } else if (lcLine.startsWith("wrap:")) {
			_wrap = lcLine.indexOf("off", 5) < 0;

            return true;
        } else if (lcLine.startsWith("color:")) {
			_usecolor = lcLine.indexOf("off", 6) < 0;

            return true;
        } else if (lcLine.startsWith("captions:")) {
            addCaptionLine(line.substring(10));
            return true;
        }

        return false;
    }

    /** Reset a scheduled redraw tasks. This base class does nothing.
     *  Derived classes should define the correct behavior.
     */
    protected void _resetScheduledTasks() {
        // This method should be implemented by the derived classes.
    }

    /** Perform a scheduled redraw. This base class does nothing.
     *  Derived classes should define the correct behavior.
     */
    protected void _scheduledRedraw() {
        // Does nothing on this level
    }

    /** Set the visibility of the Fill button.
     *  @param visibility True if the fill button is to be visible.
     *  @deprecated Use #setButtons(boolean) instead.
     */
    @Deprecated
    protected void _setButtonsVisibility(boolean visibility) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _printButton.setVisible(visibility);
        _fillButton.setVisible(visibility);
        _eqAxButton.setVisible(visibility); // Dirk: equal axis button
        _formatButton.setVisible(visibility);
        _resetButton.setVisible(visibility);
    }

    /** Set the padding multiple.
     *  The plot rectangle can be "padded" in each direction -x, +x, -y, and
     *  +y.  If the padding is set to 0.05 (and the padding is used), then
     *  there is 10% more length on each axis than set by the setXRange() and
     *  setYRange() methods, 5% in each direction.
     *  @param padding The padding multiple.
     */
    protected void _setPadding(double padding) {
        // Changing legend means we need to repaint the offscreen buffer.
        _plotImage = null;

        _padding = padding;
    }

    /**
     * Return whether repainting happens by a timed thread.
     * @return True when repainting happens by a timer thread.
     */
    protected boolean _timedRepaint() {
        return _timedRepaint || _automaticRescale;
    }

    /** Write plot information to the specified output stream in the
     *  old PtPlot syntax.
     *  Derived classes should override this method to first call
     *  the parent class method, then add whatever additional information
     *  they wish to add to the stream.
     *  It is not synchronized, so its caller should be.
     *  @param output A buffered print writer.
     *  @deprecated
     */
    @Deprecated
    protected void _writeOldSyntax(PrintWriter output) {
        output.println("# Ptolemy plot, version 2.0");

        if (_title != null) {
            output.println("TitleText: " + _title);
        }

        if (_captionStrings != null) {
            for (Enumeration<String> captions = _captionStrings.elements(); captions
                    .hasMoreElements();) {
                String captionLine = captions.nextElement();
                output.println("Caption: " + captionLine);
            }
        }

        if (_xlabel != null) {
            output.println("XLabel: " + _xlabel);
        }

        if (_ylabel != null) {
            output.println("YLabel: " + _ylabel);
        }

        if (_xRangeGiven) {
            output.println("XRange: " + _xlowgiven + ", " + _xhighgiven);
        }

        if (_yRangeGiven) {
            output.println("YRange: " + _ylowgiven + ", " + _yhighgiven);
        }

        if (_xticks != null && !_xticks.isEmpty()) {
            output.print("XTicks: ");

            int last = _xticks.size() - 1;

            for (int i = 0; i < last; i++) {
                output.print("\"" + _xticklabels.elementAt(i) + "\" "
                        + _xticks.elementAt(i) + ", ");
            }

            output.println("\"" + _xticklabels.elementAt(last) + "\" "
                    + _xticks.elementAt(last));
        }

        if (_yticks != null && !_yticks.isEmpty()) {
            output.print("YTicks: ");

            int last = _yticks.size() - 1;

            for (int i = 0; i < last; i++) {
                output.print("\"" + _yticklabels.elementAt(i) + "\" "
                        + _yticks.elementAt(i) + ", ");
            }

            output.println("\"" + _yticklabels.elementAt(last) + "\" "
                    + _yticks.elementAt(last));
        }

        if (_xlog) {
            output.println("XLog: on");
        }

        if (_ylog) {
            output.println("YLog: on");
        }

        if (!_grid) {
            output.println("Grid: off");
        }

        if (_wrap) {
            output.println("Wrap: on");
        }

        if (!_usecolor) {
            output.println("Color: off");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The maximum y value of the range of the data to be plotted. */
    protected transient volatile double _yMax = 0;

    /** The minimum y value of the range of the data to be plotted. */
    protected transient volatile double _yMin = 0;

    /** The maximum x value of the range of the data to be plotted. */
    protected transient volatile double _xMax = 0;

    /** The minimum y value of the range of the data to be plotted. */
    protected transient volatile double _xMin = 0;

    /** The factor we pad by so that we don't plot points on the axes.
     */
    protected volatile double _padding = 0.05;

    /** An offscreen buffer for improving plot performance. */
    protected transient BufferedImage _plotImage = null;

    /** True if the x range have been given. */
    protected transient boolean _xRangeGiven = false;

    /** True if the y range have been given. */
    protected transient boolean _yRangeGiven = false;

    /** True if the ranges were given by zooming. */
    protected transient boolean _rangesGivenByZooming = false;

    /** The given X and Y ranges.
     * If they have been given the top and bottom of the x and y ranges.
     * This is different from _xMin and _xMax, which actually represent
     * the range of data that is plotted.  This represents the range
     * specified (which may be different due to zooming).
     */
    protected double _xlowgiven;

    /** The given X and Y ranges.
     * If they have been given the top and bottom of the x and y ranges.
     * This is different from _xMin and _xMax, which actually represent
     * the range of data that is plotted.  This represents the range
     * specified (which may be different due to zooming).
     */
    protected double _xhighgiven;

    /** The given X and Y ranges.
     * If they have been given the top and bottom of the x and y ranges.
     * This is different from _xMin and _xMax, which actually represent
     * the range of data that is plotted.  This represents the range
     * specified (which may be different due to zooming).
     */
    protected double _ylowgiven;

    /** The given X and Y ranges.
     * If they have been given the top and bottom of the x and y ranges.
     * This is different from _xMin and _xMax, which actually represent
     * the range of data that is plotted.  This represents the range
     * specified (which may be different due to zooming).
     */
    protected double _yhighgiven;

    /** The minimum X value registered so for, for auto ranging. */
    protected double _xBottom = Double.MAX_VALUE;

    /** The maximum X value registered so for, for auto ranging. */
    protected double _xTop = -Double.MAX_VALUE;

    /** The minimum Y value registered so for, for auto ranging. */
    protected double _yBottom = Double.MAX_VALUE;

    /** The maximum Y value registered so for, for auto ranging. */
    protected double _yTop = -Double.MAX_VALUE;

    /** Whether to draw the axes using a logarithmic scale. */
    protected boolean _xlog = false;

    /** Whether to draw the axes using a logarithmic scale. */
    protected boolean _ylog = false;

    /** For use in calculating log base 10. A log times this is a log base 10. */
    protected static final double _LOG10SCALE = 1 / Math.log(10);

    /** Whether to draw a background grid. */
    protected boolean _grid = true;

    /** Whether to wrap the X axis. */
    protected boolean _wrap = false;

    /** The high range of the X axis for wrapping. */
    protected double _wrapHigh;

    /** The low range of the X axis for wrapping. */
    protected double _wrapLow;

    /** Color of the background, settable from HTML. */
    protected Color _background = Color.white;

    /** Color of the foreground, settable from HTML. */
    protected Color _foreground = Color.black;

    /** Top padding.
     *  Derived classes can increment these to make space around the plot.
     */
    protected int _topPadding = 10;

    /** Bottom padding.
     *  Derived classes can increment these to make space around the plot.
     */
    protected int _bottomPadding = 5;

    /** Right padding.
     *  Derived classes can increment these to make space around the plot.
     */
    protected int _rightPadding = 10;

    /** Left padding.
     *  Derived classes can increment these to make space around the plot.
     */
    protected int _leftPadding = 10;

    // The naming convention is: "_ulx" = "upper left x", where "x" is
    // the horizontal dimension.

    /** The x value of the upper left corner of the plot rectangle in pixels.
     *  Given a mouse click at x0, to convert to data coordinates, use:
     *  (_xMin + (x0 - _ulx) / _xscale).
     */
    protected int _ulx = 1;

    /** The y value of the upper left corner of the plot rectangle in pixels.
     *  Given a mouse click at y0, to convert to data coordinates, use:
     *  (_yMax - (y0 - _uly) / _yscale).
     */
    protected int _uly = 1;

    /** The x value of the lower right corner of
     * the plot rectangle in pixels. */
    protected int _lrx = 100;

    /** The y value of the lower right corner of
     * the plot rectangle in pixels. */
    protected int _lry = 100;

    /** User specified plot rectangle, null if none specified.
     *  @see #setPlotRectangle(Rectangle)
     */
    protected Rectangle _specifiedPlotRectangle = null;

    /** Scaling used for the vertical axis in plotting points.
     *  The units are pixels/unit, where unit is the units of the Y axis.
     */
    protected double _yscale = 1.0;

    /** Scaling used for the horizontal axis in plotting points.
     *  The units are pixels/unit, where unit is the units of the X axis.
     */
    protected double _xscale = 1.0;

    /** Indicator whether to use _colors. */
    protected volatile boolean _usecolor = true;

    /** The default colors, by data set.
     *  There are 11 colors so that combined with the
     *  10 marks of the Plot class, we can distinguish 110
     *  distinct data sets.
     */
    static protected Color[] _colors = { new Color(0xff0000), // red
            new Color(0x0000ff), // blue
            new Color(0x00aaaa), // cyan-ish
            new Color(0x000000), // black
            new Color(0xffa500), // orange
            new Color(0x53868b), // cadetblue4
            new Color(0xff7f50), // coral
            new Color(0x45ab1f), // dark green-ish
            new Color(0x90422d), // sienna-ish
            new Color(0xa0a0a0), // grey-ish
            new Color(0x14ff14), // green-ish
    };

    /** Width and height of component in pixels. */
    protected int _width = 500;

    /** Width and height of component in pixels. */
    protected int _height = 300;

    /** Width and height of component in pixels. */
    protected int _preferredWidth = 500;

    /** Width and height of component in pixels. */
    protected int _preferredHeight = 300;

    /** Indicator that size has been set. */

    //protected boolean _sizeHasBeenSet = false;
    /** The document base we use to find the _filespec.
     * NOTE: Use of this variable is deprecated.  But it is made available
     * to derived classes for backward compatibility.
     * FIXME: Sun's appletviewer gives an exception if this is protected.
     * Why?? So we make it temporarily public.
     */
    public URL _documentBase = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*
     * Draw the legend in the upper right corner and return the width
     * (in pixels)  used up.  The arguments give the upper right corner
     * of the region where the legend should be placed.
     */
    private int _drawLegend(Graphics graphics, int urx, int ury) {
        // Ignore if there is no graphics object to draw on.
        if (graphics == null) {
            return 0;
        }

        // FIXME: consolidate all these for efficiency
        Font previousFont = graphics.getFont();
        graphics.setFont(_labelFont);

        int spacing = _labelFontMetrics.getHeight();

        Enumeration<String> v = _legendStrings.elements();
        Enumeration<Integer> i = _legendDatasets.elements();
        int ypos = ury + spacing;
        int maxwidth = 0;

        while (v.hasMoreElements()) {
            String legend = v.nextElement();

            // NOTE: relies on _legendDatasets having the same num. of entries.
            int dataset = i.nextElement();

            if (dataset >= 0) {
                if (_usecolor) {
                    // Points are only distinguished up to the number of colors
                    int color = dataset % _colors.length;
                    graphics.setColor(_colors[color]);
                }

                _drawPoint(graphics, dataset, urx - 3, ypos - 3, false);

                graphics.setColor(_foreground);

                int width = _labelFontMetrics.stringWidth(legend);

                if (width > maxwidth) {
                    maxwidth = width;
                }

                graphics.drawString(legend, urx - 15 - width, ypos);
                ypos += spacing;
            }
        }

        graphics.setFont(previousFont);
        return 22 + maxwidth; // NOTE: subjective spacing parameter.
    }

    // Execute all actions pending on the deferred action list.
    // The list is cleared and the _actionsDeferred variable is set
    // to false, even if one of the deferred actions fails.
    // This method should only be invoked in the event dispatch thread.
    // It is synchronized, so the integrity of the deferred actions list
    // is ensured, since modifications to that list occur only in other
    // synchronized methods.
    private synchronized void _executeDeferredActions() {
        try {
            for (Runnable action : _deferredActions) {
                action.run();
            }
        } finally {
            _actionsDeferred = false;
            _deferredActions.clear();
        }
    }

    /*
     * Return the number as a String for use as a label on a
     * logarithmic axis.
     * Since this is a log plot, number passed in will not have too many
     * digits to cause problems.
     * If the number is an integer, then we print 1e<num>.
     * If the number is not an integer, then print only the fractional
     * components.
     */
    private String _formatLogNum(double num, int numfracdigits) {
        String results;
        int exponent = (int) num;

        // Determine the exponent, prepending 0 or -0 if necessary.
        if (exponent >= 0 && exponent < 10) {
            results = "0" + exponent;
        } else {
            if (exponent < 0 && exponent > -10) {
                results = "-0" + -exponent;
            } else {
                results = Integer.toString(exponent);
            }
        }

        // Handle the mantissa.
        if (num >= 0.0) {
            if (num - (int) num < 0.001) {
                results = "1e" + results;
            } else {
                results = _formatNum(Math.pow(10.0, num - (int) num),
                        numfracdigits);
            }
        } else {
            if (-num - (int) -num < 0.001) {
                results = "1e" + results;
            } else {
                results = _formatNum(Math.pow(10.0, num - (int) num) * 10,
                        numfracdigits);
            }
        }

        return results;
    }

    /*
     * Return a string for displaying the specified number
     * using the specified number of digits after the decimal point.
     * NOTE: java.text.NumberFormat in Netscape 4.61 has a bug
     * where it fails to round numbers instead it truncates them.
     * As a result, we don't use java.text.NumberFormat, instead
     * We use the method from Ptplot1.3
     */
    private String _formatNum(double num, int numfracdigits) {
        // When java.text.NumberFormat works under Netscape,
        // uncomment the next block of code and remove
        // the code after it.
        // Ptplot developers at UCB can access a test case at:
        // http://ptolemy.eecs.berkeley.edu/~ptII/ptIItree/ptolemy/plot/adm/trunc/trunc-jdk11.html
        // The plot will show two 0.7 values on the x axis if the bug
        // continues to exist.
        //if (_numberFormat == null) {
        //   // Cache the number format so that we don't have to get
        //    // info about local language etc. from the OS each time.
        //    _numberFormat = NumberFormat.getInstance();
        //}
        //_numberFormat.setMinimumFractionDigits(numfracdigits);
        //_numberFormat.setMaximumFractionDigits(numfracdigits);
        //return _numberFormat.format(num);
        // The section below is from Ptplot1.3
        // First, round the number.
        double fudge = 0.5;

        if (num < 0.0) {
            fudge = -0.5;
        }

        String numString = Double
                .toString(num + fudge * Math.pow(10.0, -numfracdigits));

        // Next, find the decimal point.
        int dpt = numString.lastIndexOf(".");
        StringBuffer result = new StringBuffer();

        if (dpt < 0) {
            // The number we are given is an integer.
            if (numfracdigits <= 0) {
                // The desired result is an integer.
                result.append(numString);
                return result.toString();
            }

            // Append a decimal point and some zeros.
            result.append(".");

            for (int i = 0; i < numfracdigits; i++) {
                result.append("0");
            }

            return result.toString();
        } else {
            // There are two cases.  First, there may be enough digits.
            int shortby = numfracdigits - (numString.length() - dpt - 1);

            if (shortby <= 0) {
                int numtocopy = dpt + numfracdigits + 1;

                if (numfracdigits == 0) {
                    // Avoid copying over a trailing decimal point.
                    numtocopy -= 1;
                }

                result.append(numString.substring(0, numtocopy));
                return result.toString();
            } else {
                result.append(numString);

				result.append("0".repeat(shortby));

                return result.toString();
            }
        }
    }

    /*
     * Determine what values to use for log axes.
     * Based on initGrid() from xgraph.c by David Harrison.
     */
    private Vector<Double> _gridInit(double low, double step, boolean labeled,
            Vector<Double> oldgrid) {
        // How log axes work:
        // _gridInit() creates a vector with the values to use for the
        // log axes.  For example, the vector might contain
        // {0.0 0.301 0.698}, which could correspond to
        // axis labels {1 1.2 1.5 10 12 15 100 120 150}
        //
        // _gridStep() gets the proper value.  _gridInit is cycled through
        // for each integer log value.
        //
        // Bugs in log axes:
        // * Sometimes not enough grid lines are displayed because the
        // region is small.  This bug is present in the oriignal xgraph
        // binary, which is the basis of this code.  The problem is that
        // as ratio gets closer to 1.0, we need to add more and more
        // grid marks.
        Vector<Double> grid = new Vector<>(10);

        //grid.addElement(Double.valueOf(0.0));
        double ratio = Math.pow(10.0, step);
        int ngrid = 1;

        if (labeled) {
            // Set up the number of grid lines that will be labeled
            if (ratio <= 3.5) {
                if (ratio > 2.0) {
                    ngrid = 2;
                } else if (ratio > 1.26) {
                    ngrid = 5;
                } else if (ratio > 1.125) {
                    ngrid = 10;
                } else {
                    ngrid = (int) Math.rint(1.0 / step);
                }
            }
        } else {
            // Set up the number of grid lines that will not be labeled
            if (ratio > 10.0) {
                ngrid = 1;
            } else if (ratio > 3.0) {
                ngrid = 2;
            } else if (ratio > 2.0) {
                ngrid = 5;
            } else if (ratio > 1.125) {
                ngrid = 10;
            } else {
                ngrid = 100;
            }

            // Note: we should keep going here, but this increases the
            // size of the grid array and slows everything down.
        }

        int oldgridi = 0;

        for (int i = 0; i < ngrid; i++) {
            double gridval = i * 1.0 / ngrid * 10;
            double logval = _LOG10SCALE * Math.log(gridval);

            if (logval == Double.NEGATIVE_INFINITY) {
                logval = 0.0;
            }

            // If oldgrid is not null, then do not draw lines that
            // were already drawn in oldgrid.  This is necessary
            // so we avoid obliterating the tick marks on the plot borders.
            if (oldgrid != null && oldgridi < oldgrid.size()) {
                // Cycle through the oldgrid until we find an element
                // that is equal to or greater than the element we are
                // trying to add.
                while (oldgridi < oldgrid.size()
                        && oldgrid.elementAt(oldgridi) < logval) {
                    oldgridi++;
                }

                if (oldgridi < oldgrid.size()) {
                    // Using == on doubles is bad if the numbers are close,
                    // but not exactly equal.
                    if (Math.abs(
							oldgrid.elementAt(oldgridi)
                                    - logval) > 0.00001) {
                        grid.addElement(logval);
                    }
                } else {
                    grid.addElement(logval);
                }
            } else {
                grid.addElement(logval);
            }
        }

        // _gridCurJuke and _gridBase are used in _gridStep();
        _gridCurJuke = 0;

        if (low == -0.0) {
            low = 0.0;
        }

        _gridBase = Math.floor(low);

        double x = low - _gridBase;

        // Set gridCurJuke so that the value in grid is greater than
        // or equal to x.  This sets us up to process the first point.
        for (_gridCurJuke = -1; _gridCurJuke + 1 < grid.size()
                && x >= grid.elementAt(_gridCurJuke + 1); _gridCurJuke++) {
        }

        return grid;
    }

    /*
     * Round pos up to the nearest value in the grid.
     */
    private double _gridRoundUp(Vector<Double> grid, double pos) {
        double x = pos - Math.floor(pos);
        int i;

        for (i = 0; i < grid.size()
                && x >= grid.elementAt(i); i++) {
        }

        if (i >= grid.size()) {
            return pos;
        } else {
            return Math.floor(pos) + grid.elementAt(i);
        }
    }

    /*
     * Used to find the next value for the axis label.
     * For non-log axes, we just return pos + step.
     * For log axes, we read the appropriate value in the grid Vector,
     * add it to _gridBase and return the sum.  We also take care
     * to reset _gridCurJuke if necessary.
     * Note that for log axes, _gridInit() must be called before
     * calling _gridStep().
     * Based on stepGrid() from xgraph.c by David Harrison.
     */
    private double _gridStep(Vector<Double> grid, double pos, double step,
            boolean logflag) {
        if (logflag) {
            if (++_gridCurJuke >= grid.size()) {
                _gridCurJuke = 0;
                _gridBase += Math.ceil(step);
            }

            if (_gridCurJuke >= grid.size()) {
                return pos + step;
            }

            return _gridBase
                    + grid.elementAt(_gridCurJuke);
        } else {
            return pos + step;
        }
    }

    /*
     * Measure the various fonts.  You only want to call this once.
     */
    private void _measureFonts() {
        // We only measure the fonts once, and we do it from addNotify().
        // For maintainability, keep the fonts alphabetized here.
        if (_captionFont == null) {
            _captionFont = new Font("Helvetica", Font.PLAIN, 12);
        }

        if (_labelFont == null) {
            _labelFont = new Font("Helvetica", Font.PLAIN, 12);
        }

        if (_superscriptFont == null) {
            _superscriptFont = new Font("Helvetica", Font.PLAIN, 9);
        }

        if (_titleFont == null) {
            _titleFont = new Font("Helvetica", Font.BOLD, 14);
        }

        _captionFontMetrics = getFontMetrics(_captionFont);
        _labelFontMetrics = getFontMetrics(_labelFont);
        _superscriptFontMetrics = getFontMetrics(_superscriptFont);
        _titleFontMetrics = getFontMetrics(_titleFont);
    }

    /*
     * Return the number of fractional digits required to display the
     * given number.  No number larger than 15 is returned (if
     * more than 15 digits are required, 15 is returned).
     */
    private int _numFracDigits(double num) {
        int numdigits = 0;

        while (numdigits <= 15 && num != Math.floor(num)) {
            num *= 10.0;
            numdigits += 1;
        }

        return numdigits;
    }

    /*
     * Return the number of integer digits required to display the
     * given number.  No number larger than 15 is returned (if
     * more than 15 digits are required, 15 is returned).
     */
    private int _numIntDigits(double num) {
        int numdigits = 0;

        while (numdigits <= 15 && (int) num != 0.0) {
            num /= 10.0;
            numdigits += 1;
        }

        return numdigits;
    }

    /*
     * Parse a string of the form: "word num, word num, word num, ..."
     * where the word must be enclosed in quotes if it contains spaces,
     * and the number is interpreted as a floating point number.  Ignore
     * any incorrectly formatted fields.  I <i>xtick</i> is true, then
     * interpret the parsed string to specify the tick labels on the x axis.
     * Otherwise, do the y axis.
     */
    private void _parsePairs(String line, boolean xtick) {
        // Clear current ticks first.
        if (xtick) {
            _xticks = null;
            _xticklabels = null;
        } else {
            _yticks = null;
            _yticklabels = null;
        }

        int start = 0;
        boolean cont = true;

        while (cont) {
            int comma = line.indexOf(",", start);
            String pair = null;

            if (comma > start) {
                pair = line.substring(start, comma).trim();
            } else {
                pair = line.substring(start).trim();
                cont = false;
            }

            int close = -1;
            int open = 0;

            if (pair.startsWith("\"")) {
                close = pair.indexOf("\"", 1);
                open = 1;
            } else {
                close = pair.indexOf(" ");
            }

            if (close > 0) {
                String label = pair.substring(open, close);
                String index = pair.substring(close + 1).trim();

                try {
                    double idx = Double.parseDouble(index);

                    if (xtick) {
                        addXTick(label, idx);
                    } else {
                        addYTick(label, idx);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Warning from PlotBox: "
                            + "Unable to parse ticks: " + e.getMessage());

                    // ignore if format is bogus.
                }
            }

            start = comma + 1;
            comma = line.indexOf(",", start);
        }
    }

    /** Return a default set of rendering hints for image export, which
     *  specifies the use of anti-aliasing.
     */
    private RenderingHints _defaultImageRenderingHints() {
        RenderingHints hints = new RenderingHints(null);
        hints.put(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        return hints;
    }

    /*
     * Given a number, round up to the nearest power of ten
     * times 1, 2, or 5.
     *
     * Note: The argument must be strictly positive.
     */
    private double _roundUp(double val) {
        int exponent = (int) Math.floor(Math.log(val) * _LOG10SCALE);
        val *= Math.pow(10, -exponent);

        if (val > 5.0) {
            val = 10.0;
        } else if (val > 2.0) {
            val = 5.0;
        } else if (val > 1.0) {
            val = 2.0;
        }

        val *= Math.pow(10, exponent);
        return val;
    }

    /*
     * Internal implementation of setXRange, so that it can be called when
     * autoranging.
     */
    private void _setXRange(double min, double max) {
        // We check to see if the original range has been given here
        // because if we check in setXRange(), then we will not catch
        // the case where we have a simple plot file that consists of just
        // data points
        //
        // 1. Create a file that consists of two data points
        //   1 1
        //   2 3
        // 2. Start up plot on it
        // $PTII/bin/ptplot foo.plt
        // 3. Zoom in
        // 4. Hit reset axes
        // 5. The bug is that the axes do not reset to the initial settings
        // Changing the range means we have to replot.
        _plotImage = null;

        if (!_originalXRangeGiven) {
            _originalXlow = min;
            _originalXhigh = max;
            _originalXRangeGiven = true;
        }

        // If values are invalid, try for something reasonable.
        if (min > max) {
            min = -1.0;
            max = 1.0;
        } else if (min == max) {
            min -= 1.0;
            max += 1.0;
        }

        //if (_xRangeGiven) {
        // The user specified the range, so don't pad.
        //    _xMin = min;
        //    _xMax = max;
        //} else {
        // Pad slightly so that we don't plot points on the axes.
        _xMin = min - (max - min) * _padding;
        _xMax = max + (max - min) * _padding;

        //}
        // Find the exponent.
        double largest = Math.max(Math.abs(_xMin), Math.abs(_xMax));
        _xExp = (int) Math.floor(Math.log(largest) * _LOG10SCALE);

        // Use the exponent only if it's larger than 1 in magnitude.
        if (_xExp > 1 || _xExp < -1) {
            double xs = 1.0 / Math.pow(10.0, _xExp);
            _xtickMin = _xMin * xs;
            _xtickMax = _xMax * xs;
        } else {
            _xtickMin = _xMin;
            _xtickMax = _xMax;
            _xExp = 0;
        }
    }

    /*
     * Internal implementation of setYRange, so that it can be called when
     * autoranging.
     */
    private void _setYRange(double min, double max) {
        // See comment in _setXRange() about why this is necessary.
        // Changing the range means we have to replot.
        _plotImage = null;

        if (!_originalYRangeGiven) {
            _originalYlow = min;
            _originalYhigh = max;
            _originalYRangeGiven = true;
        }

        // If values are invalid, try for something reasonable.
        if (min > max) {
            min = -1.0;
            max = 1.0;
        } else if (min == max) {
            min -= 0.1;
            max += 0.1;
        }

        //if (_yRangeGiven) {
        // The user specified the range, so don't pad.
        //    _yMin = min;
        //    _yMax = max;
        //} else {
        // Pad slightly so that we don't plot points on the axes.
        _yMin = min - (max - min) * _padding;
        _yMax = max + (max - min) * _padding;

        //}
        // Find the exponent.
        double largest = Math.max(Math.abs(_yMin), Math.abs(_yMax));
        _yExp = (int) Math.floor(Math.log(largest) * _LOG10SCALE);

        // Use the exponent only if it's larger than 1 in magnitude.
        if (_yExp > 1 || _yExp < -1) {
            double ys = 1.0 / Math.pow(10.0, _yExp);
            _ytickMin = _yMin * ys;
            _ytickMax = _yMax * ys;
        } else {
            _ytickMin = _yMin;
            _ytickMax = _yMax;
            _yExp = 0;
        }
    }

    /*
     *  Zoom in or out based on the box that has been drawn.
     *  The argument gives the lower right corner of the box.
     *  This method is not synchronized because it is called within
     *  the UI thread, and making it synchronized causes a deadlock.
     *  @param x The final x position.
     *  @param y The final y position.
     */
    void _zoom(int x, int y) {
        // FIXME: This is friendly because Netscape 4.0.3 cannot access it if
        // it is private!
        // NOTE: Due to a bug in JDK 1.1.7B, the BUTTON1_MASK does
        // not work on mouse drags, thus we have to use this variable
        // to determine whether we are actually zooming. It is used only
        // in _zoomBox, since calling this method is properly masked.
        _zooming = false;

        Graphics graphics = getGraphics();

        // Ignore if there is no graphics object to draw on.
        if (graphics == null) {
            return;
        }

        if (_zoomin && _drawn) {
            if (_zoomxn != -1 || _zoomyn != -1) {
                // erase previous rectangle.
                int minx = Math.min(_zoomx, _zoomxn);
                int maxx = Math.max(_zoomx, _zoomxn);
                int miny = Math.min(_zoomy, _zoomyn);
                int maxy = Math.max(_zoomy, _zoomyn);
                graphics.setXORMode(_boxColor);
                graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
                graphics.setPaintMode();

                // constrain to be in range
                if (y > _lry) {
                    y = _lry;
                }

                if (y < _uly) {
                    y = _uly;
                }

                if (x > _lrx) {
                    x = _lrx;
                }

                if (x < _ulx) {
                    x = _ulx;
                }

                // NOTE: ignore if total drag less than 5 pixels.
                if (Math.abs(_zoomx - x) > 5 && Math.abs(_zoomy - y) > 5) {
                    double a = _xMin + (_zoomx - _ulx) / _xscale;
                    double b = _xMin + (x - _ulx) / _xscale;

                    // NOTE: It used to be that it wasproblematic to set
                    // the X range here because it conflicted with the wrap
                    // mechanism.  But now the wrap mechanism saves the state
                    // of the X range when the setWrap() method is called,
                    // so this is safe.
                    // EAL 6/12/00.
                    if (a < b) {
                        setXRange(a, b);
                    } else {
                        setXRange(b, a);
                    }

                    a = _yMax - (_zoomy - _uly) / _yscale;
                    b = _yMax - (y - _uly) / _yscale;

                    if (a < b) {
                        setYRange(a, b);
                    } else {
                        setYRange(b, a);
                    }
                }

                repaint();
            }
        } else if (_zoomout && _drawn) {
            // Erase previous rectangle.
            graphics.setXORMode(_boxColor);

            int x_diff = Math.abs(_zoomx - _zoomxn);
            int y_diff = Math.abs(_zoomy - _zoomyn);
            graphics.drawRect(_zoomx - 15 - x_diff, _zoomy - 15 - y_diff,
                    30 + x_diff * 2, 30 + y_diff * 2);
            graphics.setPaintMode();

            // Calculate zoom factor.
            double a = Math.abs(_zoomx - x) / 30.0;
            double b = Math.abs(_zoomy - y) / 30.0;
            double newx1 = _xMax + (_xMax - _xMin) * a;
            double newx2 = _xMin - (_xMax - _xMin) * a;

            // NOTE: To limit zooming out to the fill area, uncomment this...
            // if (newx1 > _xTop) newx1 = _xTop;
            // if (newx2 < _xBottom) newx2 = _xBottom;
            double newy1 = _yMax + (_yMax - _yMin) * b;
            double newy2 = _yMin - (_yMax - _yMin) * b;

            // NOTE: To limit zooming out to the fill area, uncomment this...
            // if (newy1 > _yTop) newy1 = _yTop;
            // if (newy2 < _yBottom) newy2 = _yBottom;
            zoom(newx2, newy2, newx1, newy1);
            repaint();
        } else if (!_drawn) {
            repaint();
        }

        _drawn = false;
        _zoomin = _zoomout = false;
        _zoomxn = _zoomyn = _zoomx = _zoomy = -1;
    }

    /**
     * Zoom to that equal interval widths are on x and y axis.
     * For example, a miss-scaled circle will look circular afterwords.
     * @author Dirk Bueche
     */
    public synchronized void zoomEqual() {

        double temp = _padding;
        _padding = 0;
        double rx = (_xMax - _xMin) / (_lrx - _ulx); // delta x per pixel
        double ry = (_yMax - _yMin) / (_lry - _uly); // delta y per pixel

        if (ry > rx) {
            _xMax = _xMin + ry / rx * (_xMax - _xMin);
        }
        if (rx > ry) {
            _yMax = _yMin + rx / ry * (_yMax - _yMin);
        }
        zoom(_xMin, _yMin, _xMax, _yMax);
        _padding = temp;
    }

    /*
     *  Draw a box for an interactive zoom box.  The starting point (the
     *  upper left corner of the box) is taken
     *  to be that specified by the startZoom() method.  The argument gives
     *  the lower right corner of the box.  If a previous box
     *  has been drawn, erase it first.
     *  This method is not synchronized because it is called within
     *  the UI thread, and making it synchronized causes a deadlock.
     *  @param x The x position.
     *  @param y The y position.
     */
    void _zoomBox(int x, int y) {
        // FIXME: This is friendly because Netscape 4.0.3 cannot access it if
        // it is private!
        // NOTE: Due to a bug in JDK 1.1.7B, the BUTTON1_MASK does
        // not work on mouse drags, thus we have to use this variable
        // to determine whether we are actually zooming.
        if (!_zooming) {
            return;
        }

        Graphics graphics = getGraphics();

        // Ignore if there is no graphics object to draw on.
        if (graphics == null) {
            return;
        }

        // Bound the rectangle so it doesn't go outside the box.
        if (y > _lry) {
            y = _lry;
        }

        if (y < _uly) {
            y = _uly;
        }

        if (x > _lrx) {
            x = _lrx;
        }

        if (x < _ulx) {
            x = _ulx;
        }

        // erase previous rectangle, if there was one.
        if (_zoomx != -1 || _zoomy != -1) {
            // Ability to zoom out added by William Wu.
            // If we are not already zooming, figure out whether we
            // are zooming in or out.
            if (!_zoomin && !_zoomout) {
                if (y < _zoomy) {
                    _zoomout = true;

                    // Draw reference box.
                    graphics.setXORMode(_boxColor);
                    graphics.drawRect(_zoomx - 15, _zoomy - 15, 30, 30);
                } else if (y > _zoomy) {
                    _zoomin = true;
                }
            }

            if (_zoomin) {
                // Erase the previous box if necessary.
                if ((_zoomxn != -1 || _zoomyn != -1) && _drawn) {
                    int minx = Math.min(_zoomx, _zoomxn);
                    int maxx = Math.max(_zoomx, _zoomxn);
                    int miny = Math.min(_zoomy, _zoomyn);
                    int maxy = Math.max(_zoomy, _zoomyn);
                    graphics.setXORMode(_boxColor);
                    graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
                }

                // Draw a new box if necessary.
                if (y > _zoomy) {
                    _zoomxn = x;
                    _zoomyn = y;

                    int minx = Math.min(_zoomx, _zoomxn);
                    int maxx = Math.max(_zoomx, _zoomxn);
                    int miny = Math.min(_zoomy, _zoomyn);
                    int maxy = Math.max(_zoomy, _zoomyn);
                    graphics.setXORMode(_boxColor);
                    graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
                    _drawn = true;
                    return;
                } else {
                    _drawn = false;
                }
            } else if (_zoomout) {
                // Erase previous box if necessary.
                if ((_zoomxn != -1 || _zoomyn != -1) && _drawn) {
                    int x_diff = Math.abs(_zoomx - _zoomxn);
                    int y_diff = Math.abs(_zoomy - _zoomyn);
                    graphics.setXORMode(_boxColor);
                    graphics.drawRect(_zoomx - 15 - x_diff,
                            _zoomy - 15 - y_diff, 30 + x_diff * 2,
                            30 + y_diff * 2);
                }

                if (y < _zoomy) {
                    _zoomxn = x;
                    _zoomyn = y;

                    int x_diff = Math.abs(_zoomx - _zoomxn);
                    int y_diff = Math.abs(_zoomy - _zoomyn);
                    graphics.setXORMode(_boxColor);
                    graphics.drawRect(_zoomx - 15 - x_diff,
                            _zoomy - 15 - y_diff, 30 + x_diff * 2,
                            30 + y_diff * 2);
                    _drawn = true;
                    return;
                } else {
                    _drawn = false;
                }
            }
        }

        graphics.setPaintMode();
    }

    /*
     *  Set the starting point for an interactive zoom box (the upper left
     *  corner).
     *  This method is not synchronized because it is called within
     *  the UI thread, and making it synchronized causes a deadlock.
     *  @param x The x position.
     *  @param y The y position.
     */
    void _zoomStart(int x, int y) {
        // FIXME: This is friendly because Netscape 4.0.3 cannot access it if
        // it is private!
        // constrain to be in range
        if (y > _lry) {
            y = _lry;
        }

        if (y < _uly) {
            y = _uly;
        }

        if (x > _lrx) {
            x = _lrx;
        }

        if (x < _ulx) {
            x = _ulx;
        }

        _zoomx = x;
        _zoomy = y;
        _zooming = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator of whether actions are deferred. */
    private volatile boolean _actionsDeferred = false;

    // True when repainting happens by a timer thread.
    private boolean _automaticRescale = false;

    /** List of deferred actions. */
    private final LinkedList<Runnable> _deferredActions = new LinkedList<>();

    /** The file to be opened. */
    private String _filespec = null;

    // Call setXORMode with a hardwired color because
    // _background does not work in an application,
    // and _foreground does not work in an applet.
    // NOTE: For some reason, this comes out blue, which is fine...
    private static final Color _boxColor = Color.orange;

    /** The range of the plot as labeled
     * (multiply by 10^exp for actual range.
     */
    private double _ytickMax = 0.0;

    /** The range of the plot as labeled
     * (multiply by 10^exp for actual range.
     */
    private double _ytickMin = 0.0;

    /** The range of the plot as labeled
     * (multiply by 10^exp for actual range.
     */
    private double _xtickMax = 0.0;

    /** The range of the plot as labeled
     * (multiply by 10^exp for actual range.
     */
    private double _xtickMin = 0.0;

    /** The power of ten by which the range numbers should
     *  be multiplied.
     */
    private int _yExp = 0;

    /** The power of ten by which the range numbers should
     *  be multiplied.
     */
    private int _xExp = 0;

    /** Scaling used in making tick marks. */
    private double _ytickscale = 0.0;

    /** Scaling used in making tick marks. */
    private double _xtickscale = 0.0;

    /** Caption font information. */
    private Font _captionFont = null;

    /** Font information. */
    private Font _labelFont = null;

    /** Font information. */
    private Font _superscriptFont = null;

    /** Font information. */
    private Font _titleFont = null;

    /** Caption font metric information. */
    private FontMetrics _captionFontMetrics = null;

    /** FontMetric information. */
    private FontMetrics _labelFontMetrics = null;

    /** FontMetric information. */
    private FontMetrics _superscriptFontMetrics = null;

    /** FontMetric information. */
    private FontMetrics _titleFontMetrics = null;

    // Number format cache used by _formatNum.
    // See the comment in _formatNum for more information.
    // private transient NumberFormat _numberFormat = null;
    // Used for log axes. Index into vector of axis labels.
    private transient int _gridCurJuke = 0;

    // Used for log axes.  Base of the grid.
    private transient double _gridBase = 0.0;

    // An array of strings for reporting errors.
    private transient String[] _errorMsg;

    /** The title and label strings. */
    private String _xlabel;

    /** The title and label strings. */
    private String _ylabel;

    /** The title and label strings. */
    private String _title;

    /** Caption information. */
    private Vector<String> _captionStrings = new Vector<>();

    /** Legend information. */
    private Vector<String> _legendStrings = new Vector<>();

    /** Legend information. */
    private Vector<Integer> _legendDatasets = new Vector<>();

    /** If XTicks or YTicks are given/ */
    private Vector<Double> _xticks = null;

    /** If XTicks or YTicks are given/ */
    private Vector<String> _xticklabels = null;

    /** If XTicks or YTicks are given/ */
    private Vector<Double> _yticks = null;

    /** If XTicks or YTicks are given/ */
    private Vector<String> _yticklabels = null;

    // A button for filling the plot
    private transient JButton _fillButton = null;

    // Dirk: a button for equal axis scaling
    private transient JButton _eqAxButton = null;

    // A button for formatting the plot
    private transient JButton _formatButton = null;

    // Indicator of whether X and Y range has been first specified.
    boolean _originalXRangeGiven = false;

    // Indicator of whether X and Y range has been first specified.
    boolean _originalYRangeGiven = false;

    // First values specified to setXRange() and setYRange().
    double _originalXlow = 0.0;

    // First values specified to setXRange() and setYRange().
    double _originalXhigh = 0.0;

    // First values specified to setXRange() and setYRange().
    double _originalYlow = 0.0;

    // First values specified to setXRange() and setYRange().
    double _originalYhigh = 0.0;

    // A button for printing the plot
    private transient JButton _printButton = null;

    // A button for filling the plot
    private transient JButton _resetButton = null;

    // Dirk: Variables keeping track of the interactive moving.
    // Initialize to impossible values.
    private transient int _movex = -1;
    private transient int _movey = -1;

    // True when repainting should be performed by a timed thread.
    private boolean _timedRepaint = false;

    // The timer task that does the repainting.
    // _timerTask should be volatile because FindBugs says:
    // "Incorrect lazy initialization of static field"
    static private volatile TimedRepaint _timerTask = null;

    // Variables keeping track of the interactive zoom box.
    // Initialize to impossible values.
    private transient int _zoomx = -1;

    private transient int _zoomy = -1;

    private transient int _zoomxn = -1;

    private transient int _zoomyn = -1;

    // Control whether we are zooming in or out.
    private transient boolean _zoomin = false;

    private transient boolean _zoomout = false;

    private transient boolean _drawn = false;

    private transient boolean _zooming = false;

    private transient boolean _moving = false;
    private transient boolean _control = false;

    /** Handle mouse pressed events to provide zoom functionality. */
    public class ZoomListener implements MouseListener {
        /** Request the focus.
         *  @param event The event, ignored by this method.
         */
        @Override
        public void mouseClicked(MouseEvent event) {
            requestFocus();
        }

        /** Ignored.
         *  @param event The event, ignored by this method.
         */
        @Override
        public void mouseEntered(MouseEvent event) {
        }

        /** Ignored.
         *  @param event The event, ignored by this method.
         */
        @Override
        public void mouseExited(MouseEvent event) {
        }

        /** Handle mouse button 1 events.  See the class comment for details.
         *  @param event The event.
         */
        @Override
        public void mousePressed(MouseEvent event) {
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4072703
            // BUTTON1_MASK still not set for MOUSE_PRESSED events
            // suggests:
            // Workaround
            //   Assume that a press event with no modifiers must be button 1.
            //   This has the serious drawback that it is impossible to be sure
            //   that button 1 hasn't been pressed along with one of the other
            //   buttons.
            // This problem affects Netscape 4.61 under Digital Unix and
            // 4.51 under Solaris
            //
            // Mac OS X 10.5: we want BUTTON1_MASK set and BUTTON3_MASK not set
            // so that when we edit a dataset we don't get the zoom box
            // https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=300
            if ((event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0
                    && (event.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == 0
                    || event.getModifiersEx() == 0) {
                PlotBox.this._zoomStart(event.getX(), event.getY());
            }
            // Want to convert from mouse presses to data points?
            // Comment out the if clause above and uncomment this println:
            //System.out.println("PlotBox.mousePressed(): ("
            //                       + event.getX() + ", " + event.getY()
            //                       + ") = ("
            //                       + (_xMin + ((event.getX() - _ulx) / _xscale))
            //                       + ", "
            //                       + (_yMax - ((event.getY() - _uly) / _yscale))
            //                 + ")");
        }

        /** Handle mouse button 1 events.  See the class comment for details.
         *  @param event The event.
         */
        @Override
        public void mouseReleased(MouseEvent event) {
            if ((event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0
                    && (event.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == 0
                    || event.getModifiersEx() == 0) {
                PlotBox.this._zoom(event.getX(), event.getY());
            }
        }
    }

    /** Move the items in the plot. */
    public class MoveListener implements MouseListener {
        // Author: Dirk Bueche.

        /** Ignored.
         *  @param event Ignored.
         */
        @Override
        public void mouseClicked(MouseEvent event) {
        }

        /** Ignored.
         *  @param event Ignored.
         */
        @Override
        public void mouseEntered(MouseEvent event) {
        }

        /** Ignored.
         *  @param event Ignored.
         */
        @Override
        public void mouseExited(MouseEvent event) {
        }

        /** If the third button is pressed, then
         *  save the X and Y values.
         *  @param event The event
         */
        @Override
        public void mousePressed(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON3) {
                _movex = event.getX(); // starting point for moving
                _movey = event.getY();
                _moving = true; // flag for moving is on
            }
        }

        /** Note that the moving has stopped.
         *  @param event Ignored.
         */
        @Override
        public void mouseReleased(MouseEvent event) {
            _moving = false; // flag for moving is off
        }
    }

    /** Track how the mouse with button 3 pressed is moved. */
    public class MoveMotionListener implements MouseMotionListener {
        // Author: Dirk Bueche.

        /** If the mouse is dragged after clicking the third
         *  button, then shift what is displayed.
         *  @param event Ignored.
         */
        @Override
        public void mouseDragged(MouseEvent event) {

            if (!_moving) {
                return;
            }

            double dx = _xMax - _xMin; // actual x range shown in plot
            double dy = _yMax - _yMin; // actual y range shown in plot

            // pixel
            int px = event.getX(); // current position
            int py = event.getY();
            double mpx = px - _movex; // movement
            double mpy = py - _movey;

            // do moving
            synchronized (this) {
                _xMin = _xMin - dx * mpx / (_lrx - _ulx);
                _xMax = _xMax - dx * mpx / (_lrx - _ulx);
                _yMin = _yMin - dy * mpy / (_uly - _lry);
                _yMax = _yMax - dy * mpy / (_uly - _lry);
            }

            _movex = px;
            _movey = py;

            // plot new condition
            double temp = _padding; // no padding for this zooming
            _padding = 0;
            zoom(_xMin, _yMin, _xMax, _yMax);
            _padding = temp;

        }

        /** Ignored.
         *  @param event Ignored.
         */
        @Override
        public void mouseMoved(MouseEvent event) {
        }

    }

    /** Zoom with the mouse wheel. */
    public class ZoomListener2 implements MouseWheelListener {
        // Author: Dirk Bueche.

        /** If the mouse wheel is moved, then zoom accordingly.
         *  @param e The mouse wheel event.
         */
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            String message;
            int notches = e.getWheelRotation();
            double factor;
            if (notches < 0) {
                message = "Mouse wheel moved UP by " + -notches + " notch(es)";
                factor = 0.02; // zoom in
            } else {
                message = "Mouse wheel moved DOWN by " + notches + " notch(es)";
                factor = -0.02; // zoom out
            }
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                message += "    Scroll type: WHEEL_UNIT_SCROLL\n";
                message += "    Scroll amount: " + e.getScrollAmount()
                        + " unit increments per notch\n";
                message += "    Units to scroll: " + e.getUnitsToScroll()
                        + " unit increments\n";
            } else { //scroll type == MouseWheelEvent.WHEEL_BLOCK_SCROLL
                message += "    Scroll type: WHEEL_BLOCK_SCROLL\n";
            }
            // output for debugging
            if (_control) {
                factor *= 2;
            }

            synchronized (this) {
                // Mouse position - this is the center for zooming
                double cx = Math.max(Math.min(e.getX(), _lrx), _ulx);
                double cy = Math.max(Math.min(e.getY(), _lry), _uly);

                double dx = _xMax - _xMin; // actual x range shown in plot
                double dy = _yMax - _yMin; // actual y range shown in plot

                // do zooming around center for zooming
                _xMin = _xMin + dx * (cx - _ulx) / (_lrx - _ulx) * factor;
                _xMax = _xMax - dx * (_lrx - cx) / (_lrx - _ulx) * factor;
                _yMin = _yMin + dy * (cy - _lry) / (_uly - _lry) * factor;
                _yMax = _yMax - dy * (_uly - cy) / (_uly - _lry) * factor;
            }

            double temp = _padding; // no padding for this zooming
            _padding = 0;
            zoom(_xMin, _yMin, _xMax, _yMax);
            _padding = temp;
        }
    }

    /** Draw the zoom box.
     */
    public class DragListener implements MouseMotionListener {
        /** Handle mouse drag events.  See the class comment for details.
         *  @param event The event.
         */
        @Override
        public void mouseDragged(MouseEvent event) {
            // NOTE: Due to a bug in JDK 1.1.7B, the BUTTON1_MASK does
            // not work on mouse drags.  It does work on MouseListener
            // methods, so those methods set a variable _zooming that
            // is used by _zoomBox to determine whether to draw a box.
            if ((event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0
                    && (event.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == 0) {
                PlotBox.this._zoomBox(event.getX(), event.getY());
            }
        }

        /** Ignored.
         *  @param event The event, ignored by this method.
         */
        @Override
        public void mouseMoved(MouseEvent event) {
        }
    }

    /** Handle key pressed events.
     */
    class CommandListener implements KeyListener {
        /** Handle key pressed events.  See the class comment for details.
         *  @param e The event.
         */
        @Override
        public void keyPressed(KeyEvent e) {
            int keycode = e.getKeyCode();

            switch (keycode) {
            case KeyEvent.VK_CONTROL:
                _control = true;
                break;

            case KeyEvent.VK_SHIFT:
                _shift = true;
                break;

            case KeyEvent.VK_C:

                if (_control) {
                    // The "null" sends the output to the clipboard.
                    exportImage(null, "gif");

                    String message = "GIF image exported to clipboard.";
                    JOptionPane.showMessageDialog(PlotBox.this, message,
                            "Ptolemy Plot Message",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                break;

            case KeyEvent.VK_D:

                if (!_control && _shift) {
                    write(System.out);

                    String message = "Plot data sent to standard out.";
                    JOptionPane.showMessageDialog(PlotBox.this, message,
                            "Ptolemy Plot Message",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                if (_control) {
                    // xgraph and many other Unix apps use Control-D to exit
                    System.exit(1);
                }

                break;

            case KeyEvent.VK_E:

                if (!_control && _shift) {
                    export(System.out);

                    String message = "Encapsulated PostScript (EPS) "
                            + "exported to standard out.";
                    JOptionPane.showMessageDialog(PlotBox.this, message,
                            "Ptolemy Plot Message",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                break;

            case KeyEvent.VK_F:

                if (!_control && _shift) {
                    fillPlot();
                }

                break;

            case KeyEvent.VK_H:

                if (!_control && _shift) {
                    _help();
                }

                break;

            case KeyEvent.VK_Q:

                if (!_control) {
                    // xv uses q to quit.
                    System.exit(1);
                }

                break;

            case KeyEvent.VK_SLASH:

                if (_shift) {
                    // Question mark is SHIFT-SLASH
                    _help();
                }

                break;

            default:
                // None
                break;
            }
        }

        /** Handle key released events.  See the class comment for details.
         *  @param e The event.
         */
        @Override
        public void keyReleased(KeyEvent e) {
            int keycode = e.getKeyCode();

            switch (keycode) {
            case KeyEvent.VK_CONTROL:
                _control = false;
                break;

            case KeyEvent.VK_SHIFT:
                _shift = false;
                break;

            default:
                // None
                break;
            }
        }

        /** Ignored by this class.
         *  The keyTyped method is broken in jdk 1.1.4.
         *  It always gets "unknown key code".
         * @param e Ignored by this method.
         */
        @Override
        public void keyTyped(KeyEvent e) {
        }

        private boolean _shift = false;
    }

    /**
     * TimedRepaint is a timer thread that will schedule a
     * redraw each _REPAINT_TIME_INTERVAL milliseconds.
     */
    private static class TimedRepaint extends Timer {
        static int _REPAINT_TIME_INTERVAL = 30;

        public synchronized void addListener(PlotBox plotBox) {
            _listeners.add(plotBox);
            if (_listeners.size() == 1) {
                scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            // synchronized (this) to avoid changes to
                            // _listeners while repainting.
                            for (PlotBox plot : _listeners) {
                                if (plot._timedRepaint()) {
                                    plot._scheduledRedraw();
                                }
                            }
                        }
                    }
                }, 0, _REPAINT_TIME_INTERVAL);
            }
        }

        public synchronized void removeListener(PlotBox plotBox) {
            _listeners.remove(plotBox);
            if (_listeners.isEmpty()) {
                purge();
            }
        }

        private final Set<PlotBox> _listeners = new HashSet<>();
    }

    /** True if we have printed the securityExceptionMessage. */
    private static boolean _printedSecurityExceptionMessage = false;
}