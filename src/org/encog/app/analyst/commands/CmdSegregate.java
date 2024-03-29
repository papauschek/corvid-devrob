/*
 * Encog(tm) Core v3.0 - Java Version
 * http://www.heatonresearch.com/encog/
 * http://code.google.com/p/encog-java/
 
 * Copyright 2008-2011 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package org.encog.app.analyst.commands;

import java.io.File;

import org.encog.app.analyst.EncogAnalyst;
import org.encog.app.analyst.csv.segregate.SegregateCSV;
import org.encog.app.analyst.csv.segregate.SegregateTargetPercent;
import org.encog.app.analyst.script.prop.ScriptProperties;
import org.encog.app.analyst.script.segregate.AnalystSegregateTarget;
import org.encog.app.analyst.util.AnalystReportBridge;
import org.encog.util.csv.CSVFormat;

/**
 * This command is used to segregate one CSV file into several. This can be
 * useful for creating training and evaluation sets.
 * 
 */
public class CmdSegregate extends Cmd {

	/**
	 * The name of this command.
	 */
	public static final String COMMAND_NAME = "SEGREGATE";

	/**
	 * Construct the segregate command.
	 * 
	 * @param analyst The analyst to use.
	 */
	public CmdSegregate(final EncogAnalyst analyst) {
		super(analyst);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean executeCommand(final String args) {
		// get filenames
		final String sourceID = getProp().getPropertyString(
				ScriptProperties.SEGREGATE_CONFIG_SOURCE_FILE);

		final File sourceFile = getScript().resolveFilename(sourceID);

		// get formats
		final CSVFormat inputFormat = getScript()
				.determineInputFormat(sourceID);
		final CSVFormat outputFormat = getScript().determineOutputFormat();

		// prepare to segregate
		final boolean headers = getScript().expectInputHeaders(sourceID);
		final SegregateCSV seg = new SegregateCSV();
		seg.setScript(getScript());
		getAnalyst().setCurrentQuantTask(seg);
		for (final AnalystSegregateTarget target : getScript().getSegregate()
				.getSegregateTargets()) {
			final File filename = getScript().resolveFilename(target.getFile());
			seg.getTargets().add(
					new SegregateTargetPercent(filename, target.getPercent()));
			// mark generated
			getScript().markGenerated(target.getFile());
		}

		seg.setReport(new AnalystReportBridge(getAnalyst()));
		seg.analyze(sourceFile, headers, inputFormat);
		seg.setOutputFormat(outputFormat);

		seg.process();
		getAnalyst().setCurrentQuantTask(null);
		return seg.shouldStop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getName() {
		return CmdSegregate.COMMAND_NAME;
	}

}
