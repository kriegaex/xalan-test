/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2000, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 *
 * TestMultiTypeThreads.java
 *
 */
package org.apache.qetest.trax;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.util.Properties;
import java.util.StringTokenizer;

// Needed SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// Needed DOM classes
import org.w3c.dom.Node;
import org.w3c.dom.Document;

// For optional URI/URLs instead of string filenames
import java.net.URL;
import java.net.MalformedURLException;

// Import all relevant TRAX packages
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;    // We assume Features.STREAM for some tests
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

// HACK: Use Xalan's Serializers - should be changed
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

//-------------------------------------------------------------------------

/**
 * Testing multiple simultaneous processors on different threads 
 * using different processing methods with TRAX.
 * <p>No validation of output files is currently done!  You must manually
 * inspect any logfiles.  Most options can be passed in with a Properties file.</p>
 * <p>Note: Most automated tests extend XSLProcessorTestBase, and 
 * are named *Test.java.  Since we are semi-manual, we're 
 * named Test*.java instead.</p>
 * We assume Features.STREAM.
 * @author shane_curcuru@lotus.com
 */
public class TestMultiTypeThreads
{

    /**
     * Convenience method to print out usage information.  
     *
     * NEEDSDOC ($objectName$) @return
     */
    public static String usage()
    {

        return ("Usage: TestMultiTypeThreads file.properties :\n"
                + "    where the properties file can set:,\n"
                + "    inputDir=e:\\builds\\xsl-test\n"
                + "    outputDir=e:\\builds\\xsl-test\\results\n"
                + "    logFile=e:\\builds\\xsl-test\\results\\TestMultiTypeThreads.xml\n"
                + "    numRunners=5\n" + "    numRunnerCalls=10\n"
                + "    setOneFile=bool01\n" + "    setTwoFile=expr01\n"
                + "    setThreeFile=numb01\n" + "    paramName=SomeParam\n"
                + "    paramVal=TheValue\n");
    }

    /** NEEDSDOC Field debug          */
    public boolean debug = true;  // for adhoc debugging

    /**
     * Number of sets of worker threads to create and loops per runner.
     * <p>'numRunners=xx', default is 10; 'numRunnerCalls=xx', default is 50.</p>
     */
    protected int numRunners = 10;

    /**
     * Number of sets of worker threads to create and loops per runner.
     * <p>'numRunners=xx', default is 10; 'numRunnerCalls=xx', default is 50.</p>
     */
    protected int numRunnerCalls = 50;

    /**
     * Root input filenames that certain runners should use, in the inputDir.
     * <p>'setOneFile=File'; 'setTwoFile=File'; 'setThreeFile=File'
     * in .prop file to set; default is TestMultiTypeThreads1, TestMultiTypeThreads2, TestMultiTypeThreads3.</p>
     * <p>Files are found in 'inputDir=c:\bar\baz' from .prop file.</p>
     */
    protected String inputDir = null;

    /** NEEDSDOC Field setOneFilenameRoot          */
    protected String setOneFilenameRoot = "TestMultiTypeThreads1";

    /** NEEDSDOC Field setTwoFilenameRoot          */
    protected String setTwoFilenameRoot = "TestMultiTypeThreads2";

    /** NEEDSDOC Field setThreeFilenameRoot          */
    protected String setThreeFilenameRoot = "TestMultiTypeThreads3";

    /**
     * All output logs and files get put in the outputDir.
     */
    protected String outputDir = null;

    /**
     * Sample PARAM name that certain runners should use.
     * <p>Use 'paramName=xx' in .prop file to set, default is test1.</p>
     */
    protected String paramName = "test1";

    /**
     * Sample PARAM value that certain runners should use.
     * <p>Use 'paramVal=xx' in .prop file to set, default is bar.</p>
     */
    protected String paramVal = "bar";

    /**
     * liaisonClassName that just the *second* set of runners should use.
     * <p>Use 'liaison=xx' in .prop file to set, default is null (whatever the processor's default is).</p>
     */
    protected String liaison = null;  // TRAX unused

    // Used to pass info to runners; simpler to update than changing ctors

    /** RunnerID offset in ctor's array initializer.   */
    public static final int ID = 0;

    /** NEEDSDOC Field XMLNAME          */
    public static final int XMLNAME = 1;

    /** NEEDSDOC Field XSLNAME          */
    public static final int XSLNAME = 2;

    /** NEEDSDOC Field OUTNAME          */
    public static final int OUTNAME = 3;

    /** NEEDSDOC Field PARAMNAME          */
    public static final int PARAMNAME = 4;

    /** NEEDSDOC Field PARAMVAL          */
    public static final int PARAMVAL = 5;

    /** NEEDSDOC Field OPTIONS          */
    public static final int OPTIONS = 6;

    /** NEEDSDOC Field LIAISON          */
    public static final int LIAISON = 7;

    /** TRANSFORM_TYPE defines which 'type' - dom, sax, streams, etc.  */
    public static final int TRANSFORM_TYPE = 8;

    /** NEEDSDOC Field FUTUREUSE          */
    public static final int FUTUREUSE = 9;

    /**
     * Name of main file's output logging; each runner also has separate output.
     */
    protected String logFileName = "TestMultiTypeThreads.xml";

    /**
     * Construct multiple threads with processors and run them all.
     * @author Shane Curcuru & Scott Boag
     * <p>Preprocesses some stylesheets, then creates lots of worker threads.</p>
     */
    public void runTest()
    {

        // Prepare a log file and dump out some basic info
        createLogFile(logFileName);
        println("<?xml version=\"1.0\"?>");
        println("<resultsfile logFile=\"" + logFileName + "\">");
        println("<message desc=\"threads=" + (3 * numRunners)
                + " iterations=" + numRunnerCalls + "\"/>");
        println("<message desc=\"oneF=" + setOneFilenameRoot + " twof="
                + setTwoFilenameRoot + " threef=" + setThreeFilenameRoot
                + "\"/>");
        println("<message desc=\"param=" + paramName + " val=" + paramVal
                + " liaison=" + liaison + "\"/>");

        // Preprocess some stylesheets for use by the runners
        String errStr = "Create processor threw: ";
        Templates stylesheet1, stylesheet2, stylesheet3;

        try
        {
            String setOneURL =
                getURLFromString(inputDir + setOneFilenameRoot + ".xsl",
                                 null).toExternalForm();
            String setTwoURL =
                getURLFromString(inputDir + setTwoFilenameRoot + ".xsl",
                                 null).toExternalForm();
            String setThreeURL =
                getURLFromString(inputDir + setThreeFilenameRoot + ".xsl",
                                 null).toExternalForm();
            TransformerFactory factory = TransformerFactory.newInstance();

            // Note: for now, just use StreamSources to build all stylesheets
            errStr = "Processing stylesheet1 threw: ";
            stylesheet1 =
                factory.newTemplates(new StreamSource(setOneURL));
            errStr = "Processing stylesheet2 threw: ";
            stylesheet2 =
                factory.newTemplates(new StreamSource(setTwoURL));
            errStr = "Processing stylesheet3 threw: ";
            stylesheet3 =
                factory.newTemplates(new StreamSource(setThreeURL));
        }
        catch (Exception e)
        {
            println("<arbitrary desc=\"" + errStr + e.toString() + "\">");

            if (pWriter != null)
            {
                e.printStackTrace(pWriter);
            }

            e.printStackTrace();
            println("</arbitrary>");

            return;
        }

        errStr = "PreCreating runners threw: ";

        try
        {
            String[] rValues = new String[FUTUREUSE];

            // Create a whole bunch of worker threads and run them
            for (int i = 0; i < numRunners; i++)
            {
                TMTThreadsRunner r1, r2, r3;
                Thread t1, t2, t3;
                String transformType = StreamSource.FEATURE;

                // Alternate sets of runners use alternate transform types
                if ((i % 3) == 2)
                {
                    transformType = DOMSource.FEATURE;
                }
                else if ((i % 3) == 1)
                {
                    transformType = StreamSource.FEATURE;
                }
                else
                {
                    transformType = SAXSource.FEATURE;
                }
                // First set of runners reports on memory usage periodically
                rValues[ID] = "one-" + i;
                rValues[XMLNAME] = "file:" + inputDir + setOneFilenameRoot
                                   + ".xml";
                rValues[XSLNAME] = inputDir + setOneFilenameRoot + ".xsl";
                rValues[OUTNAME] = outputDir + setOneFilenameRoot + "r" + i;
                rValues[PARAMNAME] = paramName;
                rValues[PARAMVAL] = paramVal;
                rValues[OPTIONS] = "memory;param";
                rValues[TRANSFORM_TYPE] = transformType;
                errStr = "Creating runnerone-" + i + " threw: ";
                r1 = new TMTThreadsRunner(rValues, stylesheet1,
                                           numRunnerCalls);
                t1 = new Thread(r1);

                t1.start();

                // Second set of runners is polite; uses optional liaison
                rValues[ID] = "two-" + i;
                rValues[XMLNAME] = "file:" + inputDir + setTwoFilenameRoot
                                   + ".xml";
                rValues[XSLNAME] = inputDir + setTwoFilenameRoot + ".xsl";
                rValues[OUTNAME] = outputDir + setTwoFilenameRoot + "r" + i;
                rValues[PARAMNAME] = paramName;
                rValues[PARAMVAL] = paramVal;
                rValues[OPTIONS] = "polite;param";
                rValues[TRANSFORM_TYPE] = transformType;

                if ((liaison != null) &&!(liaison.equals("")))
                    rValues[LIAISON] = liaison;

                errStr = "Creating runnertwo-" + i + " threw: ";
                r2 = new TMTThreadsRunner(rValues, stylesheet2,
                                           numRunnerCalls);
                t2 = new Thread(r2);

                t2.start();

                rValues[LIAISON] = null;

                // Third set of runners will recreate it's processor each time
                // and report memory usage; but not set the param
                // Note: this causes lots of calls to System.gc
                rValues[ID] = "thr-" + i;
                rValues[XMLNAME] = "file:" + inputDir + setThreeFilenameRoot
                                   + ".xml";
                rValues[XSLNAME] = inputDir + setThreeFilenameRoot + ".xsl";
                rValues[OUTNAME] = outputDir + setThreeFilenameRoot + "r" + i;
                rValues[PARAMNAME] = paramName;
                rValues[PARAMVAL] = paramVal;
                rValues[OPTIONS] = "recreate;memory";
                rValues[TRANSFORM_TYPE] = transformType;
                errStr = "Creating runnerthree-" + i + " threw: ";
                r3 = new TMTThreadsRunner(rValues, stylesheet3,
                                           numRunnerCalls);
                t3 = new Thread(r3);

                t3.start();
                println("<message desc=\"Created " + i
                        + "th set of runners.\"/>");
            }
        }
        catch (Exception e)
        {
            println("<arbitrary desc=\"" + errStr + e.toString() + "\">");

            if (pWriter != null)
            {
                e.printStackTrace(pWriter);
            }

            e.printStackTrace();
            println("</arbitrary>");
        }

        // Clean up our own references, just for completeness
        stylesheet1 = null;
        stylesheet2 = null;
        stylesheet3 = null;
        errStr = null;

        println("<message desc=\"Created all our runners!\"/>");
        println("<message desc=\"TestMultiTypeThreads main thread now complete\"/>");
        println("</resultsfile>");

        if (pWriter != null)
            pWriter.flush();
    }

    /**
     * Read in properties file and set instance variables.  
     *
     * @param fName name of .properties file to read
     * @return false if error occoured
     */
    protected boolean initPropFile(String fName)
    {

        Properties p = new Properties();

        try
        {

            // Load named file into our properties block
            FileInputStream fIS = new FileInputStream(fName);

            p.load(fIS);

            // Parse out any values that match our internal convenience variables
            outputDir = p.getProperty("outputDir", outputDir);

            // Validate the outputDir and use it to reset the logFileName
            File oDir = new File(outputDir);

            if (!oDir.exists())
            {
                if (!oDir.mkdirs())
                {

                    // Error, we can't create the outputDir, default to current dir
                    println("<message desc=\"outputDir(" + outputDir
                            + ") does not exist, defaulting to .\"/>");

                    outputDir = ".";
                }
            }

            // Verify inputDir as well
            inputDir = p.getProperty("inputDir", inputDir);

            File tDir = new File(inputDir);

            if (!tDir.exists())
            {
                if (!tDir.mkdirs())
                {

                    // Error, we can't create the inputDir, abort
                    println("<message desc=\"inputDir(" + inputDir
                            + ") does not exist, terminating test\"/>");

                    return false;
                }
            }

            // Add on separators
            inputDir += File.separator;
            outputDir += File.separator;

            // Each defaults to variable initializers            
            logFileName = p.getProperty("logFile", logFileName);
            setOneFilenameRoot = p.getProperty("setOneFile",
                                               setOneFilenameRoot);
            setTwoFilenameRoot = p.getProperty("setTwoFile",
                                               setTwoFilenameRoot);
            setThreeFilenameRoot = p.getProperty("setThreeFile",
                                                 setThreeFilenameRoot);
            paramName = p.getProperty("paramName", paramName);
            paramVal = p.getProperty("paramVal", paramVal);
            liaison = p.getProperty("liaison", liaison);

            String numb;

            numb = p.getProperty("numRunners");

            if (numb != null)
            {
                try
                {
                    numRunners = Integer.parseInt(numb);
                }
                catch (NumberFormatException numEx)
                {

                    // no-op, leave set as default
                    println("<message desc=\"numRunners threw: "
                            + numEx.toString() + "\"/>");
                }
            }

            numb = p.getProperty("numRunnerCalls");

            if (numb != null)
            {
                try
                {
                    numRunnerCalls = Integer.parseInt(numb);
                }
                catch (NumberFormatException numEx)
                {

                    // no-op, leave set as default
                    println("<message desc=\"numRunnerCalls threw: "
                            + numEx.toString() + "\"/>");
                }
            }
        }
        catch (Exception e)
        {
            println("<arbitrary=\"initPropFile: " + fName + " threw: "
                    + e.toString() + "\">");

            if (pWriter != null)
            {
                e.printStackTrace(pWriter);
            }

            e.printStackTrace();
            println("</arbitrary>");

            return false;
        }

        return true;
    }

    /**
     * Bottleneck output; goes to System.out and main's pWriter.  
     *
     * NEEDSDOC @param s
     */
    protected void println(String s)
    {

        System.out.println(s);

        if (pWriter != null)
            pWriter.println(s);
    }

    /** A simple log output file for the main thread; each runner also has it's own. */
    protected PrintWriter pWriter = null;

    /**
     * Worker method to setup a simple log output file.  
     *
     * NEEDSDOC @param n
     */
    protected void createLogFile(String n)
    {

        try
        {
            pWriter = new PrintWriter(new FileWriter(n, true));
        }
        catch (Exception e)
        {
            System.err.println("<message desc=\"createLogFile threw: "
                               + e.toString() + "\"/>");
            e.printStackTrace();
        }
    }

    /**
     * Startup the test from the command line.  
     *
     * NEEDSDOC @param args
     */
    public static void main(String[] args)
    {

        if (args.length != 1)
        {
            System.err.println("ERROR! Must have one argument\n" + usage());

            return;  // Don't System.exit, it's not polite
        }

        TestMultiTypeThreads app = new TestMultiTypeThreads();

        if (!app.initPropFile(args[0]))  // Side effect: creates pWriter for logging
        {
            System.err.println("ERROR! Could not read properties file: "
                               + args[0]);

            return;
        }

        app.runTest();
    }

    // /////////////////// HACK - added from Xalan1 org.apache.xalan.xslt.Process /////////////////////

    /**
     * Take a user string and try and parse XML, and also return the url.
     *
     * @todo remove this; make URL's in a simpler manner!!!
     * NEEDSDOC @param urlString
     * NEEDSDOC @param base
     *
     * NEEDSDOC ($objectName$) @return
     * @exception SAXException thrown if we really really can't create the URL
     */
    public static URL getURLFromString(String urlString, String base)
            throws SAXException
    {

        String origURLString = urlString;
        String origBase = base;

        // System.out.println("getURLFromString - urlString: "+urlString+", base: "+base);
        Object doc;
        URL url = null;
        int fileStartType = 0;

        try
        {
            if (null != base)
            {
                if (base.toLowerCase().startsWith("file:/"))
                {
                    fileStartType = 1;
                }
                else if (base.toLowerCase().startsWith("file:"))
                {
                    fileStartType = 2;
                }
            }

            boolean isAbsoluteURL;

            // From http://www.ics.uci.edu/pub/ietf/uri/rfc1630.txt
            // A partial form can be distinguished from an absolute form in that the
            // latter must have a colon and that colon must occur before any slash
            // characters. Systems not requiring partial forms should not use any
            // unencoded slashes in their naming schemes.  If they do, absolute URIs
            // will still work, but confusion may result.
            int indexOfColon = urlString.indexOf(':');
            int indexOfSlash = urlString.indexOf('/');

            if ((indexOfColon != -1) && (indexOfSlash != -1)
                    && (indexOfColon < indexOfSlash))
            {

                // The url (or filename, for that matter) is absolute.
                isAbsoluteURL = true;
            }
            else
            {
                isAbsoluteURL = false;
            }

            if (isAbsoluteURL || (null == base) || (base.length() == 0))
            {
                try
                {
                    url = new URL(urlString);
                }
                catch (MalformedURLException e){}
            }

            // The Java URL handling doesn't seem to handle relative file names.
            else if (!((urlString.charAt(0) == '.') || (fileStartType > 0)))
            {
                try
                {
                    URL baseUrl = new URL(base);

                    url = new URL(baseUrl, urlString);
                }
                catch (MalformedURLException e){}
            }

            if (null == url)
            {

                // Then we're going to try and make a file URL below, so strip 
                // off the protocol header.
                if (urlString.toLowerCase().startsWith("file:/"))
                {
                    urlString = urlString.substring(6);
                }
                else if (urlString.toLowerCase().startsWith("file:"))
                {
                    urlString = urlString.substring(5);
                }
            }

            if ((null == url) && ((null == base) || (fileStartType > 0)))
            {
                if (1 == fileStartType)
                {
                    if (null != base)
                        base = base.substring(6);

                    fileStartType = 1;
                }
                else if (2 == fileStartType)
                {
                    if (null != base)
                        base = base.substring(5);

                    fileStartType = 2;
                }

                File f = new File(urlString);

                if (!f.isAbsolute() && (null != base))
                {

                    // String dir = f.isDirectory() ? f.getAbsolutePath() : f.getParent();
                    // System.out.println("prebuiltUrlString (1): "+base);
                    StringTokenizer tokenizer = new StringTokenizer(base,
                                                    "\\/");
                    String fixedBase = null;

                    while (tokenizer.hasMoreTokens())
                    {
                        String token = tokenizer.nextToken();

                        if (null == fixedBase)
                        {

                            // Thanks to Rick Maddy for the bug fix for UNIX here.
                            if (base.charAt(0) == '\\'
                                    || base.charAt(0) == '/')
                            {
                                fixedBase = File.separator + token;
                            }
                            else
                            {
                                fixedBase = token;
                            }
                        }
                        else
                        {
                            fixedBase += File.separator + token;
                        }
                    }

                    // System.out.println("rebuiltUrlString (1): "+fixedBase);
                    f = new File(fixedBase);

                    String dir = f.isDirectory()
                                 ? f.getAbsolutePath() : f.getParent();

                    // System.out.println("dir: "+dir);
                    // System.out.println("urlString: "+urlString);
                    // f = new File(dir, urlString);
                    // System.out.println("f (1): "+f.toString());
                    // urlString = f.getAbsolutePath();
                    f = new File(urlString);

                    boolean isAbsolute = f.isAbsolute()
                                         || (urlString.charAt(0) == '\\')
                                         || (urlString.charAt(0) == '/');

                    if (!isAbsolute)
                    {

                        // Getting more and more ugly...
                        if (dir.charAt(dir.length() - 1)
                                != File.separator.charAt(0)
                                && urlString.charAt(0)
                                   != File.separator.charAt(0))
                        {
                            urlString = dir + File.separator + urlString;
                        }
                        else
                        {
                            urlString = dir + urlString;
                        }

                        // System.out.println("prebuiltUrlString (2): "+urlString);
                        tokenizer = new StringTokenizer(urlString, "\\/");

                        String rebuiltUrlString = null;

                        while (tokenizer.hasMoreTokens())
                        {
                            String token = tokenizer.nextToken();

                            if (null == rebuiltUrlString)
                            {

                                // Thanks to Rick Maddy for the bug fix for UNIX here.
                                if (urlString.charAt(0) == '\\'
                                        || urlString.charAt(0) == '/')
                                {
                                    rebuiltUrlString = File.separator + token;
                                }
                                else
                                {
                                    rebuiltUrlString = token;
                                }
                            }
                            else
                            {
                                rebuiltUrlString += File.separator + token;
                            }
                        }

                        // System.out.println("rebuiltUrlString (2): "+rebuiltUrlString);
                        if (null != rebuiltUrlString)
                            urlString = rebuiltUrlString;
                    }

                    // System.out.println("fileStartType: "+fileStartType);
                    if (1 == fileStartType)
                    {
                        if (urlString.charAt(0) == '/')
                        {
                            urlString = "file://" + urlString;
                        }
                        else
                        {
                            urlString = "file:/" + urlString;
                        }
                    }
                    else if (2 == fileStartType)
                    {
                        urlString = "file:" + urlString;
                    }

                    try
                    {

                        // System.out.println("Final before try: "+urlString);
                        url = new URL(urlString);
                    }
                    catch (MalformedURLException e)
                    {

                        // System.out.println("Error trying to make URL from "+urlString);
                    }
                }
            }

            if (null == url)
            {

                // The sun java VM doesn't do this correctly, but I'll 
                // try it here as a second-to-last resort.
                if ((null != origBase) && (origBase.length() > 0))
                {
                    try
                    {
                        URL baseURL = new URL(origBase);

                        // System.out.println("Trying to make URL from "+origBase+" and "+origURLString);
                        url = new URL(baseURL, origURLString);

                        // System.out.println("Success! New URL is: "+url.toString());
                    }
                    catch (MalformedURLException e)
                    {

                        // System.out.println("Error trying to make URL from "+origBase+" and "+origURLString);
                    }
                }

                if (null == url)
                {
                    try
                    {
                        String lastPart;

                        if (null != origBase)
                        {
                            File baseFile = new File(origBase);

                            if (baseFile.isDirectory())
                            {
                                lastPart =
                                    new File(baseFile,
                                             urlString).getAbsolutePath();
                            }
                            else
                            {
                                String parentDir = baseFile.getParent();

                                lastPart =
                                    new File(parentDir,
                                             urlString).getAbsolutePath();
                            }
                        }
                        else
                        {
                            lastPart = new File(urlString).getAbsolutePath();
                        }

                        // Hack
                        // if((lastPart.charAt(0) == '/') && (lastPart.charAt(2) == ':'))
                        //   lastPart = lastPart.substring(1, lastPart.length() - 1);
                        String fullpath;

                        if (lastPart.charAt(0) == '\\'
                                || lastPart.charAt(0) == '/')
                        {
                            fullpath = "file://" + lastPart;
                        }
                        else
                        {
                            fullpath = "file:" + lastPart;
                        }

                        url = new URL(fullpath);
                    }
                    catch (MalformedURLException e2)
                    {
                        throw new SAXException("Cannot create url for: "
                                               + urlString, e2);

                        //XSLMessages.createXPATHMessage(XPATHErrorResources.ER_CANNOT_CREATE_URL, new Object[]{urlString}),e2); //"Cannot create url for: " + urlString, e2 );
                    }
                }
            }
        }
        catch (SecurityException se)
        {
            try
            {
                url = new URL("http://xml.apache.org/xslt/"
                              + java.lang.Math.random());  // dummy
            }
            catch (MalformedURLException e2)
            {

                // I give up
            }
        }

        // System.out.println("url: "+url.toString());
        return url;
    }
}  // end of class TestMultiTypeThreads

/**
 * Worker class to run a processor on a separate thread.
 * <p>Currently, no automated validation is done, however most
 * output files and all error logs are saved to disk allowing for
 * later manual verification.</p>
 */
class TMTThreadsRunner implements Runnable
{

    /** NEEDSDOC Field xslStylesheet          */
    Templates xslStylesheet;

    /** NEEDSDOC Field numProcesses          */
    int numProcesses;

    /** NEEDSDOC Field runnerID          */
    String runnerID;

    /** NEEDSDOC Field xmlName          */
    String xmlName;

    /** NEEDSDOC Field xslName          */
    String xslName;

    /** NEEDSDOC Field outName          */
    String outName;

    /** NEEDSDOC Field paramName          */
    String paramName;

    /** NEEDSDOC Field paramVal          */
    String paramVal;

    /** NEEDSDOC Field liaison          */
    String liaison;

    /** NEEDSDOC Field polite          */
    boolean polite = false;  // if we should yield each loop

    /** NEEDSDOC Field recreate          */
    boolean recreate = false;  // if we should re-create a new processor each time

    /** NEEDSDOC Field validate          */
    boolean validate = false;  // if we should attempt to validate output files (FUTUREWORK)

    /** NEEDSDOC Field reportMem          */
    boolean reportMem = false;  // if we should report memory usage periodically

    /** NEEDSDOC Field setParam          */
    boolean setParam = false;  // if we should set our parameter or not

    /** NEEDSDOC Field setParam          */
    String transformType = StreamSource.FEATURE;

    /**
     * Constructor TMTThreadsRunner
     *
     *
     * NEEDSDOC @param params
     * NEEDSDOC @param xslStylesheet
     * NEEDSDOC @param numProcesses
     */
    TMTThreadsRunner(String[] params, Templates xslStylesheet,
                      int numProcesses)
    {

        this.xslStylesheet = xslStylesheet;
        this.numProcesses = numProcesses;
        this.runnerID = params[TestMultiTypeThreads.ID];
        this.xmlName = params[TestMultiTypeThreads.XMLNAME];
        this.xslName = params[TestMultiTypeThreads.XSLNAME];
        this.outName = params[TestMultiTypeThreads.OUTNAME];
        this.paramName = params[TestMultiTypeThreads.PARAMNAME];
        this.paramVal = params[TestMultiTypeThreads.PARAMVAL];

        if (params[TestMultiTypeThreads.OPTIONS].indexOf("polite") > 0)
            polite = true;

        if (params[TestMultiTypeThreads.OPTIONS].indexOf("recreate") > 0)
            recreate = true;

        if (params[TestMultiTypeThreads.OPTIONS].indexOf("validate") > 0)
            validate = true;

        if (params[TestMultiTypeThreads.OPTIONS].indexOf("memory") > 0)
            reportMem = true;

        if (params[TestMultiTypeThreads.OPTIONS].indexOf("param") > 0)
            setParam = true;

        if (params[TestMultiTypeThreads.LIAISON] != null)  // TRAX unused
            liaison = params[TestMultiTypeThreads.LIAISON];

        if (params[TestMultiTypeThreads.TRANSFORM_TYPE] != null)
            transformType = params[TestMultiTypeThreads.TRANSFORM_TYPE];
    }

    /**
     * Bottleneck output; both to System.out and to our private errWriter.  
     *
     * NEEDSDOC @param s
     */
    protected void println(String s)
    {

        System.out.println(s);

        if (errWriter != null)
            errWriter.println(s);
    }

    /**
     * Bottleneck output; both to System.out and to our private errWriter.  
     *
     * NEEDSDOC @param s
     */
    protected void print(String s)
    {

        System.out.print(s);

        if (errWriter != null)
            errWriter.print(s);
    }

    /** NEEDSDOC Field errWriter          */
    PrintWriter errWriter = null;

    /**
     * NEEDSDOC Method createErrWriter 
     *
     */
    protected void createErrWriter()
    {

        try
        {
            errWriter = new PrintWriter(new FileWriter(outName + ".log"),
                                        true);
        }
        catch (Exception e)
        {
            System.err.println("<message desc=\"" + runnerID + ":threw: "
                               + e.toString() + "\"/>");
        }
    }

    /** Main entrypoint; loop and perform lots of processes. */
    public void run()
    {

        int i = 0;  // loop counter; used for error reporting

        createErrWriter();
        println("<?xml version=\"1.0\"?>");
        println("<testrunner desc=\"" + runnerID + ":started\" fileName=\""
                + xslName + "\">");

        TransformerFactory factory = null;

        try
        {

            // Each runner creates it's own processor for use and it's own error log
            factory = TransformerFactory.newInstance();

            // Munge the input filenames to be URLs
            xmlName = TestMultiTypeThreads.getURLFromString(xmlName,
                                                   null).toExternalForm();
            xslName = TestMultiTypeThreads.getURLFromString(xslName,
                                                   null).toExternalForm();

            println("<arbitrary desc=\"" + runnerID + ":processing\">");
        }
        catch (Throwable ex)
        {  // If we got here, just log it and bail, no sense continuing
            println("<throwable desc=\"" + ex.toString() + "\"><![CDATA[");
            ex.printStackTrace(errWriter);
            println("\n</throwable>");
            println("<message desc=\"" + runnerID + ":complete-ERROR:after:"
                    + i + "\"/>");
            println("</testrunner>");

            if (errWriter != null)
                errWriter.close();

            return;
        }

        try
        {

            // Loop away...
            for (i = 0; i < numProcesses; i++)
            {

                // Run a process using the pre-compiled stylesheet we were construced with
                {
                    Transformer transformer1 = xslStylesheet.newTransformer();
                    if (transformType == DOMSource.FEATURE)
                    {
                        doDOMTransform(transformer1, xmlName, outName + ".out", "d");
                    }
                    else if (transformType == SAXSource.FEATURE)
                    {
                        doSAXTransform(xslName, xmlName, outName + ".out", "x");
                    }
                    else if (transformType == StreamSource.FEATURE)
                    {
                        FileOutputStream resultStream1 =
                            new FileOutputStream(outName + ".out");
                        Result result1 = new StreamResult(resultStream1);

                        if (setParam)
                            transformer1.setParameter(paramName, paramVal);

                        print("t");  // Note presence of this in logs shows which process threw an exception
                        transformer1.transform(new StreamSource(xmlName), result1);
                        resultStream1.close();
                    }
                    else 
                    {
                        throw new RuntimeException("unsupported transformType: " + transformType);
                    }

                    // Temporary vars go out of scope for cleanup here
                }

                // Now process something with a newly-processed stylesheet
                {
                    Templates templates2 =
                        factory.newTemplates(new StreamSource(xslName));
                    Transformer transformer2 = templates2.newTransformer();
                    if (transformType == DOMSource.FEATURE)
                    {
                        doDOMTransform(transformer2, xmlName, outName + "_.out", "D");
                    }
                    else if (transformType == SAXSource.FEATURE)
                    {
                        doSAXTransform(xslName, xmlName, outName + "_.out", "X");
                    }
                    else // if (transformType == StreamSource.FEATURE)
                    {
                        FileOutputStream resultStream2 =
                            new FileOutputStream(outName + "_.out");
                        Result result2 = new StreamResult(resultStream2);

                        if (setParam)
                            transformer2.setParameter(paramName, paramVal);

                        print("T");  // Note presence of this in logs shows which process threw an exception
                        transformer2.transform(new StreamSource(xmlName), result2);
                        resultStream2.close();
                    }
                }

                // if asked, report memory statistics
                if (reportMem)
                {
                    Runtime r = Runtime.getRuntime();

                    r.gc();

                    long freeMemory = r.freeMemory();
                    long totalMemory = r.totalMemory();

                    println("<statistic desc=\"" + runnerID
                            + ":memory:longval-free:doubleval-total\">");
                    println("<longval>" + freeMemory + "</longval>");
                    println("<doubleval>" + totalMemory + "</doubleval>");
                    println("</statistic>");
                }

                // if we're polite, let others play for a bit
                if (polite)
                    java.lang.Thread.yield();
            }

            // IF we get here, we worked without exceptions (presumably successfully)
            println("</arbitrary>");
            println("<message desc=\"" + runnerID + ":complete-OK:after:"
                    + numProcesses + "\"/>");
        }

        // Separate messages for each kind of exception
        catch (TransformerException te)
        {
            println("\n<TransformerException desc=\"" + te.toString() + "\">");
            logStackTrace(te, errWriter);
            logContainedException(te, errWriter);
            println("</TransformerException>");
            println("</arbitrary>");
            println("<message desc=\"" + runnerID + ":complete-ERROR:after:"
                    + i + "\"/>");
        }
        catch (Throwable ex)
        {
            logThrowable(ex, errWriter);
            println("</arbitrary>");
            println("<message desc=\"" + runnerID + ":complete-ERROR:after:"
                    + i + "\"/>");
        }
        finally
        {

            // Cleanup our references, etc.
            println("</testrunner>");

            if (errWriter != null)
                errWriter.close();

            runnerID = null;
            xmlName = null;
            xslName = null;
            xslStylesheet = null;
            outName = null;
        }
    }  // end of run()...

    /** Worker method to do a specific type of transform  */
    private void doDOMTransform(Transformer t, String xmlName, String outName, String marker)
        throws Exception
    {
        // Parse in the xml data into a DOM
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
        Node xmlDoc = docBuilder.parse(new InputSource(xmlName));

        // Prepare a result and transform it into a DOM
        org.w3c.dom.Document outNode = docBuilder.newDocument();
        if (setParam)
            t.setParameter(paramName, paramVal);
        print(marker);  // Note presence of this in logs shows which process threw an exception
        t.transform(new DOMSource(xmlDoc), new DOMResult(outNode));

        // Now serialize output to disk with identity transformer
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer serializer = factory.newTransformer();
        serializer.transform(new DOMSource(outNode), 
                             new StreamResult(new FileOutputStream(outName)));
        // do we need to FileOutputStream.close()?
    }

    /** Worker method to do a specific type of transform  */
    private void doSAXTransform(String xslName, String xmlName, String outName, String marker)
        throws Exception
    {
        TransformerFactory factory = TransformerFactory.newInstance();
        // should check for SAXResult.FEATURE first!
        SAXTransformerFactory sfactory = ((SAXTransformerFactory) factory);
        
        // Create an Document node as the root for the output.
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
        Document outNode = docBuilder.newDocument();
    
        // Create a ContentHandler that can liston to SAX events 
        // and transform the output to DOM nodes.
        TransformerHandler handler = sfactory.newTransformerHandler(new StreamSource(xslName));
        handler.setResult(new DOMResult(outNode));
    
        // Create a reader and set it's ContentHandler to be the 
        // transformer.
        XMLReader reader = null;

        // Use JAXP1.1 ( if possible )
	    javax.xml.parsers.SAXParserFactory spfactory = javax.xml.parsers.SAXParserFactory.newInstance();
	    spfactory.setNamespaceAware(true);
	    javax.xml.parsers.SAXParser jaxpParser = spfactory.newSAXParser();
	    reader = jaxpParser.getXMLReader();

        if (reader == null) 
            reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(handler);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
    
        // Send the SAX events from the parser to the transformer,
        // and thus to the DOM tree.
        print(marker);  // Note presence of this in logs shows which process threw an exception
        reader.parse(xmlName);

        // Serialize the DOM tree out
	    FileOutputStream fos = new FileOutputStream(outName);
        Transformer serializer = factory.newTransformer();
        //serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        //serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializer.transform(new DOMSource(outNode), new StreamResult(fos));
    }

    /**
     * NEEDSDOC Method logContainedException 
     *
     *
     * NEEDSDOC @param parent
     * NEEDSDOC @param p
     */
    private void logContainedException(TransformerException parent, PrintWriter p)
    {

        Exception containedException = parent.getException();

        if (null != containedException)
        {
            println("<containedexception desc=\""
                    + containedException.toString() + "\">");
            logStackTrace(containedException, p);
            println("</containedexception>");
        }
    }

    /**
     * NEEDSDOC Method logThrowable 
     *
     *
     * NEEDSDOC @param t
     * NEEDSDOC @param p
     */
    private void logThrowable(Throwable t, PrintWriter p)
    {

        println("\n<throwable desc=\"" + t.toString() + "\">");
        logStackTrace(t, p);
        println("</throwable>");
    }

    /**
     * NEEDSDOC Method logStackTrace 
     *
     *
     * NEEDSDOC @param t
     * NEEDSDOC @param p
     */
    private void logStackTrace(Throwable t, PrintWriter p)
    {

        // Should check if (errWriter == null)
        println("<stacktrace><![CDATA[");
        t.printStackTrace(p);

        // Could also echo to stdout, but not really worth it
        println("]]></stacktrace>");
    }
}  // end of class TMTThreadsRunner...

// END OF FILE