/** static info for radig
 *
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
package uk.osgb.algorithm.radig2;

import java.util.ArrayList;
import java.util.Collections;

public class RadigDef {
	// Radig Specification
	int base = 10;
	int numTotalIntDigits; // integral digits in base
	int numTotalDigits; // total digits in base
	int firstDivisor = 10;
	int secondDivisor = 1;
	boolean priorityX = true;
	//
	int subDivisionLevel = 3; // two more division from base level, has priority over minSubDivisionRes
	double minSubDivisionRes = 1.0; // if resoultion is reached, no further subdivision. To use resolution to control, set subDivisionLevel to negative value  
	//
	double minEffectiveAreaRatio = 0.5; // if area ratio of the intersection of a geometry with the cell is less than this threshold, further division will be performed
	double minEffectiveLengthRatio = 0.5; // for linear object
	int totalDigitsForPoint; // for point object, not used yet
	int totalDigitsForIntPoint; // for intersecting point
	// 
	double resForIntPoint = 1.0; // if negative, use num of digits, end at 2nd divisor
	double resForPoint = 1.0;// if negative, use num of digits, end at 2nd divisor
	//
	ArrayList<Double> resBounds;
	//
	public RadigDef(int base, int numTotalIntDigits, int numTotalDigits, int firstDivisor, boolean priorityX) {
		super();
		RadigDefInit(base, numTotalIntDigits, numTotalDigits, firstDivisor, priorityX);		
	}
	public RadigDef(int base, int numTotalIntDigits, int numTotalDigits, int firstDivisor) {
		super();
		RadigDefInit(base, numTotalIntDigits, numTotalDigits, firstDivisor, true);
	}
	private void RadigDefInit(int base, int numTotalIntDigits, int numTotalDigits, int firstDivisor, boolean priorityX) {
		this.base = base;
		this.numTotalIntDigits = numTotalIntDigits;
		this.numTotalDigits = numTotalDigits;
		this.firstDivisor = firstDivisor;
		this.secondDivisor = base / firstDivisor;
		this.priorityX = priorityX;
		//
		totalDigitsForPoint = 0; 
		totalDigitsForIntPoint = 0;
	//
		initResolutionBounds();
	}
	
	public int getBase() {
		return base;
	}
	public int getNumTotalIntDigits() {
		return numTotalIntDigits;
	}
	public int getNumTotalDigits() {
		return numTotalDigits;
	}
	public int getFirstDivisor() {
		return firstDivisor;
	}
	public int getSecondDivisor() {
		return secondDivisor;
	}
	public boolean isPriorityX() {
		return priorityX;
	}
	public void setPriorityX(boolean priorityX) {
		this.priorityX = priorityX;
	}
	public boolean hasDualDivisors() {
		return base!=firstDivisor;
	}
	
	public int getSubDivisionLevel() {
		return subDivisionLevel;
	}
	public void setSubDivisionLevel(int subDivisionLevel) {
		this.subDivisionLevel = subDivisionLevel;
	}
	
	public double getMinSubDivisionRes() {
		return minSubDivisionRes;
	}
	public void setMinSubDivisionRes(double minSubDivisionRes) {
		this.minSubDivisionRes = minSubDivisionRes;
	}
	public double getMinEffectiveAreaRatio() {
		return minEffectiveAreaRatio;
	}
	public void setMinEffectiveAreaRatio(double minEffectiveAreaRatio) {
		this.minEffectiveAreaRatio = minEffectiveAreaRatio;
	}
	public double getMinEffectiveLengthRatio() {
		return minEffectiveLengthRatio;
	}
	public void setMinEffectiveLengthRatio(double minEffectiveLengthRatio) {
		this.minEffectiveLengthRatio = minEffectiveLengthRatio;
	}
	//
	//
	private int initResolutionBounds() {
		resBounds = new ArrayList<Double>();
		boolean hasDualDivisors = base!=firstDivisor; 
		double numDecDigits = numTotalDigits - numTotalIntDigits;
		for(int i = 0; i < numTotalDigits; ++i) {
			double res = Math.pow(base, i - numDecDigits);
			resBounds.add(res); 
			if(hasDualDivisors) {
				resBounds.add(res*(base/firstDivisor));
			}
		}
		return resBounds.size();
	}
	public int getTotalDigitsForPoint() {
		return totalDigitsForPoint;
	}
	public void setTotalDigitsForPoint(int totalDigitsForPoint) {
		this.totalDigitsForPoint = totalDigitsForPoint;
	}
	
	public int getTotalDigitsForIntPoint() {
		return totalDigitsForIntPoint;
	}
	public void setTotalDigitsForIntPoint(int totalDigitsForIntPoint) {
		this.totalDigitsForIntPoint = totalDigitsForIntPoint;
	}
	
	public double getResForIntPoint() {
		return resForIntPoint;
	}
	public void setResForIntPoint(double resForIntPoint) {
		this.resForIntPoint = resForIntPoint;
	}
	public double getResForPoint() {
		return resForPoint;
	}
	public void setResForPoint(double resForPoint) {
		this.resForPoint = resForPoint;
	}
	public double getMaxRes() {
		return resBounds.get(resBounds.size()-1);
	}
	public double getMinRes() {
		return resBounds.get(0);
	}
	//
	public double getResLimit(double curRes, int curLevel) {
		if(curRes <= minSubDivisionRes) {
			return curRes;
		}
		double res = curRes;
		for(int i = curLevel; i < subDivisionLevel; ++i) {
			res = getNextFineResolution(res, null);
			if(res == minSubDivisionRes) {
				return res;
			}
		}
		return res;
	}

	/** Get the resolution that is equal to or smaller than the input extent, or the smallest resolution value in the resolution bound array
	 *
	 * @param extent
	 * @param info
	 * @return
	 */
	public double getResLB(double extent, RadigInfo info) {
		boolean hasDualDivisors = base!=firstDivisor;
		int idxSch = Collections.binarySearch(resBounds, extent);
		if(idxSch < 0) {// value not in array
			idxSch = -idxSch-1;
			idxSch = idxSch > 0?idxSch-1:0;
		}
		double res = resBounds.get(idxSch);
		if(info!=null) {
			info.setResolution(res);
			if(hasDualDivisors) {
				info.setNumDigits(numTotalDigits - idxSch / 2);
				boolean endAtFirstDivisor = (idxSch % 2)==0?false:true;
				info.setEndAtFirstDivisor(endAtFirstDivisor);
				info.setSubDivisor(endAtFirstDivisor?secondDivisor:firstDivisor);
			}else {
				info.setNumDigits(numTotalDigits - idxSch);
				info.setSubDivisor(base);
			}
		}
		return res;
	}

	/** Get the resolution that is greater than the input extent, or the largest resolution value in the resolution bound array
	 *
	 * @param extent
	 * @param info
	 * @return
	 */
	double getResUB(double extent, RadigInfo info) {
		boolean hasDualDivisors = base!=firstDivisor;
		int idxSch = Collections.binarySearch(resBounds, extent);
		if(idxSch < 0) {// value not in array
			idxSch = -idxSch-1;
			idxSch = idxSch >= resBounds.size()?resBounds.size()-1:idxSch;
		}else {
			idxSch = idxSch == resBounds.size()-1?idxSch:idxSch+1;
		}
		double res = resBounds.get(idxSch);
		if(info!=null) {
			info.setResolution(res);
			if(hasDualDivisors) {
				info.setNumDigits(numTotalDigits - idxSch / 2);
				boolean endAtFirstDivisor = (idxSch % 2)==0?false:true;
				info.setEndAtFirstDivisor(endAtFirstDivisor);
				info.setSubDivisor(endAtFirstDivisor?secondDivisor:firstDivisor);
			}else {
				info.setNumDigits(numTotalDigits - idxSch);
				info.setSubDivisor(base);
			}
		}
		return res;
	}
	/**return the resolution 
	 * @param numDigits
	 * @param endAtFirstDivisor
	 * @return
	 */
	public double getResolutionNumDigits(int numDigits, boolean endAtFirstDivisor, RadigInfo info) {
		boolean hasDualDivisors = base!=firstDivisor;
		double rlt = 0.0;
		if(hasDualDivisors){
			int idx = (numTotalDigits - numDigits)*2;
			if(endAtFirstDivisor) {
				idx +=1; // resolution at first division of the digit
			}
			rlt = resBounds.get(idx);
		}else {
			rlt = resBounds.get(numTotalDigits - numDigits);
		}
		if(info!=null) {
			info.setResolution(rlt);
			info.setNumDigits(numDigits);
			info.setEndAtFirstDivisor(endAtFirstDivisor);
			info.setSubDivisor(hasDualDivisors?endAtFirstDivisor?secondDivisor:firstDivisor:base);
		}
		return rlt;
	}
	/** return the next resolution < qryRes in resBounds. if qryRes == resBounds.get(0), return -resBounds.get(0) 
	 * @param qryRes
	 * @return
	 */
	public double getNextFineResolution(double qryRes, RadigInfo info) {
		boolean hasDualDivisors = base!=firstDivisor;
		boolean finestReached = false;
		int idxSch = Collections.binarySearch(resBounds, qryRes);
		double rlt = 0.0;
		if(idxSch < 0) {// value not in resBounds, just in case
			idxSch = -idxSch-1;
			idxSch = idxSch > 0?idxSch-1:0;
		}else {// value in resBounds
			if(idxSch > 0) {
				idxSch = idxSch-1;
			}else {// idxSch == 0;
				finestReached = true;
			}
		}
		rlt = resBounds.get(idxSch);
		rlt = finestReached?-rlt:rlt;
		if(info!=null) {
			info.setResolution(rlt);
			if(hasDualDivisors) {
				info.setNumDigits(numTotalDigits - idxSch / 2);
				boolean endAtFirstDivisor = (idxSch % 2)==0?false:true;
				info.setEndAtFirstDivisor(endAtFirstDivisor);
				info.setSubDivisor(endAtFirstDivisor?secondDivisor:firstDivisor);
			}else {
				info.setNumDigits(numTotalDigits - idxSch);
				info.setSubDivisor(base);
			}
		}
		return rlt;
	}

	public static void main(String[] args) {
	}

}
