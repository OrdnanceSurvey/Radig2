/**
 * Author: Sheng Zhou (Sheng.Zhou@os.uk)
 *
 * version 1.0
 *
 * Date: 2025-01-15
 *
 * Copyright (C) 2025 Ordnance Survey
 *
 * Licensed under the Open Government Licence v3.0 (the "License");
 *
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * data structure to store and return dynamic information for a Radig grid cell at given resolution
 *
 *   
 *  
 * 
 * Author: Sheng Zhou (Sheng.Zhou@os.uk)
 * 
 * version 0.1
 * 
 * Date: 2022-09-28
 * 
 * Copyright (C) 2022 Ordnance Survey
 */
package uk.osgb.algorithm.radig2;

public class RadigInfo {
	// resolution, value in the resolution range of a Radig grid
	double resolution;
	// corresponding to first N digits of a reference string
	int numDigits;
	// divisor for further division from current resolution
	int subDivisor;
	// whether the resolution is from the first division of the digit
	boolean endAtFirstDivisor;
	//
	public RadigInfo() {}
	public RadigInfo(double resolution, int numDigits, int curDivisor, boolean atFirstDivisor) {
		super();
		this.resolution = resolution;
		this.numDigits = numDigits;
		this.subDivisor = curDivisor;
		this.endAtFirstDivisor = atFirstDivisor;
	}
	public double getResolution() {
		return resolution;
	}
	public void setResolution(double resolution) {
		this.resolution = resolution;
	}
	public int getNumDigits() {
		return numDigits;
	}
	public void setNumDigits(int numDigits) {
		this.numDigits = numDigits;
	}
	public int getSubDivisor() {
		return subDivisor;
	}
	public void setSubDivisor(int curDivisor) {
		this.subDivisor = curDivisor;
	}
	public boolean isEndAtFirstDivisor() {
		return endAtFirstDivisor;
	}
	public void setEndAtFirstDivisor(boolean atFirstDivisor) {
		this.endAtFirstDivisor = atFirstDivisor;
	}
}
