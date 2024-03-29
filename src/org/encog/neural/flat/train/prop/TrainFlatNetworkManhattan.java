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
package org.encog.neural.flat.train.prop;

import org.encog.ml.data.MLDataSet;
import org.encog.neural.flat.FlatNetwork;

/**
 * Train the flat network using Manhattan update rule.
 */
public class TrainFlatNetworkManhattan extends TrainFlatNetworkProp {

	/**
	 * The zero tolerance to use.
	 */
	private final double zeroTolerance;

	/**
	 * The learning rate.
	 */
	private double learningRate;

	/**
	 * Construct a trainer for flat networks to use the Manhattan update rule.
	 * 
	 * @param network
	 *            The network to train.
	 * @param training
	 *            The training data to use.
	 * @param theLearningRate
	 *            The learning rate to use.
	 */
	public TrainFlatNetworkManhattan(final FlatNetwork network,
			final MLDataSet training, final double theLearningRate) {
		super(network, training);
		this.learningRate = theLearningRate;
		this.zeroTolerance = RPROPConst.DEFAULT_ZERO_TOLERANCE;
	}

	/**
	 * @return the learningRate
	 */
	public final double getLearningRate() {
		return this.learningRate;
	}

	/**
	 * @param theLearningRate
	 *            the learningRate to set
	 */
	public final void setLearningRate(final double theLearningRate) {
		this.learningRate = theLearningRate;
	}

	/**
	 * Calculate the amount to change the weight by.
	 * 
	 * @param gradients
	 *            The gradients.
	 * @param lastGradient
	 *            The last gradients.
	 * @param index
	 *            The index to update.
	 * @return The amount to change the weight by.
	 */
	@Override
	public final double updateWeight(final double[] gradients,
			final double[] lastGradient, final int index) {
		if (Math.abs(gradients[index]) < this.zeroTolerance) {
			return 0;
		} else if (gradients[index] > 0) {
			return this.learningRate;
		} else {
			return -this.learningRate;
		}
	}

}
