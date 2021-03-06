/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.js.tests;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("All tests for com.aptana.editor.js")
		{
			@Override
			public void runTest(Test test, TestResult result)
			{
				System.err.println("Running test: " + test.toString());
				super.runTest(test, result);
			}
		};
		// $JUnit-BEGIN$
		suite.addTest(com.aptana.editor.js.EditorJSTests.suite());
		suite.addTest(com.aptana.editor.js.contentassist.ContentAssistTests.suite());
		suite.addTest(com.aptana.editor.js.folding.FoldingTests.suite());
		suite.addTest(com.aptana.editor.js.hyperlink.HyperlinkTests.suite());
		suite.addTest(com.aptana.editor.js.index.IndexTests.suite());
		suite.addTest(com.aptana.editor.js.inferencing.InferencingTests.suite());
		suite.addTest(com.aptana.editor.js.internal.text.InternalTextTests.suite());
		suite.addTest(com.aptana.editor.js.outline.OutlineTests.suite());
		suite.addTest(com.aptana.editor.js.sdoc.parsing.SDocParsingTests.suite());
		suite.addTest(com.aptana.editor.js.text.TextTests.suite());
		// $JUnit-END$
		return suite;
	}
}
