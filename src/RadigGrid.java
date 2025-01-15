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

package uk.osgb.algorithm.radig2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import uk.osgb.algorithm.radig2.Radig_DigitInterleaver;
import uk.osgb.algorithm.radig2.RadigCell;
import uk.osgb.algorithm.radig2.RadigDef;

public class RadigGrid {
	private static int RADIG_PREFIX_LENGTH = 5;
	LinkedList<RadigCell> freeCells = new LinkedList<RadigCell>();
	int freeCellCapacity = 2500;
	RadigDef radigDef;
	String radigPrefix;
	RadigCell[][] grid = null;
	//
	// grid index: current max X/Y indices; max X/Y index of grid
	int curX, curY, maxX, maxY;
	// extent of current effective grid cells
	double minxGrid, minyGrid, maxxGrid, maxyGrid;
	/*************************
	 * 
	 *  Constructors
	 *  
 *	***********************/
	/**
	 * @param base allowed value [2, 16] and represented as base - 1 in hex format 
	 * @param totalIntDigits_base_ns total integral part digits in base number system
	 * @param totalDigits_base_ns
	 * @param firstDivisor
	 * @param priorityX
	 */
	public RadigGrid(int base, int totalIntDigits_base_ns, int totalDigits_base_ns, int firstDivisor, boolean priorityX) {
		radigDef = new RadigDef(base, totalIntDigits_base_ns, totalDigits_base_ns, firstDivisor, priorityX);
		radigPrefix = generateRadigPrefix(base, totalIntDigits_base_ns, firstDivisor, priorityX);
	}
	
	/**
	 * @param base
	 * @param grid_extent the extent of data area to be covered by the grid (x < extent and y < extent), in radix10 number system
	 * @param numDecimalDigits number of decimal places (in original base-10 ns ordiantes!)
	 * @param firstDivisor
	 * @param priorityX
	 */
	public RadigGrid(int base, double grid_extent, int numDecimalDigits, int firstDivisor, boolean priorityX) {
		int totalIntDigits = Radig_DigitInterleaver.calcNumIntDigits(grid_extent, base);
		int totalDecDigits = Radig_DigitInterleaver.calcNumDecDigits(numDecimalDigits, base);
		int totalDigits = totalIntDigits + totalDecDigits;
		radigDef = new RadigDef(base, totalIntDigits, totalDigits, firstDivisor, priorityX);
		radigPrefix = generateRadigPrefix(base, totalIntDigits, firstDivisor, priorityX);
	}
	
	/**
	 * @param base base allowed value [2, 16] and represented as (base - 1) in hex format
	 * @param totalIntDigits
	 * @param firstDivisor
	 * @param priorityX
	 */
	public static String generateRadigPrefix(int base, int totalIntDigits, int firstDivisor, boolean priorityX) {
		String prefix = priorityX?"X":"Y";
		prefix +=(base>=1 && base <=16)?Integer.toHexString(base-1):'9'; // deafult to base 10
		if(totalIntDigits>0) {
			if(totalIntDigits <10) {
				prefix+="0"; // pad with "0"
				prefix+=totalIntDigits;
			}else if(totalIntDigits <100) {
				prefix+=totalIntDigits;
			}else {
				prefix+="07"; // default for outrange input
			}
		}else {
			prefix+="07";
		}
		prefix +=Integer.toHexString((firstDivisor > 1 && base%firstDivisor == 0)?(firstDivisor-1):(base-1));
		return prefix;
	}
	/**
	 * @param radigPrefix radig prefix string (currently a 5-letter string), will also take the whole radig reference
	 * @return
	 */
	public static RadigDef radigPrefixToDef(String radigPrefix) {
		boolean priorityX = radigPrefix.charAt(0) == 'X';
		String baseStr = radigPrefix.substring(1,2);
		int base = Integer.decode("0x"+radigPrefix.substring(1,2))+1;
		int numIntDigits = Integer.valueOf(radigPrefix.substring(2, 4));
		int firstDivisor = Integer.decode("0x"+radigPrefix.substring(4,5))+1;
		RadigDef def = new RadigDef(base, numIntDigits, numIntDigits, firstDivisor, priorityX);
		return def;
	}
	
	public static String radigRefToILRef(String radigRef) {
		return radigRef.substring(RADIG_PREFIX_LENGTH);
	}
	String getRadigPrefix() {
		return radigPrefix;
	}
	/*************************
	 * 
	 *  static utility methods
	 *  
	 ************************/
	//
	private static double compCellIdxLB(double ordinate, double res) {
		double idx = Math.floor(ordinate / res);
		if(idx > 0.0 && ordinate == res*idx) {
			return idx-1.0;
		}else {
			return idx;
		}
	}
	private static double compCellIdxUB(double ordinate, double res) {
		double idx = Math.floor(ordinate / res);
		return idx;
	}
	public static String radigRefToWKT(String radigRef) {
		RadigDef def = radigPrefixToDef(radigRef);
		return Radig_DigitInterleaver.iLRefToWKT(radigRefToILRef(radigRef), def.getBase(), def.getNumTotalIntDigits(), def.getFirstDivisor(), def.isPriorityX());
	}

	public static Geometry radigRefToGeometry(String radigRef) {
		RadigDef def = radigPrefixToDef(radigRef);
		return Radig_DigitInterleaver.iLRefToGeometry(radigRefToILRef(radigRef), def.getBase(), def.getNumTotalDigits(), def.getFirstDivisor(),def.isPriorityX());
	}

	public static double radigRefToCoordStrings(String radigRef, Coordinate rlt) {
		RadigDef def = radigPrefixToDef(radigRef);
		return Radig_DigitInterleaver.iLRefToCoordStrings(radigRefToILRef(radigRef), def.getBase(), def.getNumTotalIntDigits(), def.getFirstDivisor(), def.isPriorityX(), rlt);
		
	}

	public static double calcResolutionFromRadigRef(String radigRef) {
		RadigDef def = radigPrefixToDef(radigRef);
		return Radig_DigitInterleaver.calcResolutionILRef(radigRefToILRef(radigRef), def.getBase(), def.getNumTotalIntDigits(), def.getFirstDivisor());
		
	}

	public static void radigRefsToWKT(Collection<String> refs) {
		for(String ref:refs) {
			String wkt = radigRefToGeometry(ref).toText();
			System.out.println(wkt);
		}
	}
	public static void refsToWKT(String[] refs) {
		for(String ref:refs) {
			String wkt = radigRefToGeometry(ref).toText();
			System.out.println(wkt);
		}
	}	
	/*************************
	 * 
	 *  Radig Cell Grid initialisation
	 *  
	 ************************/
	
	/**
	 * @param geom
	 */
	private void initGrid(Geometry geom, RadigInfo info) {
		Envelope env = geom.getEnvelopeInternal();
		double res = info.getResolution();
		double minxGeom = env.getMinX();
		double minyGeom = env.getMinY();
		double maxxGeom = env.getMaxX();
		double maxyGeom = env.getMaxY();
		// grid index 
		double idxMinX = compCellIdxLB(minxGeom, res);
		double idxMinY = compCellIdxLB(minyGeom, res);
		double idxMaxX = compCellIdxUB(maxxGeom, res);
		double idxMaxY = compCellIdxUB(maxyGeom, res);
		//
		double numColX = idxMaxX-idxMinX+1;
		double numRowY = idxMaxY-idxMinY+1;
		//
		// range of effective grid cells
		minxGrid = res*idxMinX;
		minyGrid = res*idxMinY;
		maxxGrid = res*(idxMaxX+1);
		maxyGrid = res*(idxMaxY+1);
		//
		initGridCellArray((int)numColX, (int)numRowY, info);
		//
	}
	/**
	 * @param numColX
	 * @param numRowY
	 */
	private void initGridCellArray(int numColX, int numRowY, RadigInfo info) {
		if(numColX > 0 && numRowY > 0) {
			if(grid!=null && (numColX <= maxX +1) && (numRowY <= maxY +1)) {// initialised already in previous queries
					curX = numColX-1;
					curY = numRowY-1;
			}else {
				if(grid!=null) {
					resetGridCells();
				}
				curX = maxX = numColX-1;
				curY = maxY = numRowY-1;
				grid = new RadigCell[numColX][numRowY];
				for(int i = 0; i < numColX; ++i) {
					for(int j = 0; j < numRowY; ++j) {
						grid[i][j] = freeCells.size()>0?freeCells.pollLast():(new RadigCell()); 
					}
				}

			}
			initGridCells(minxGrid, minyGrid, info);
		}
	}

	/**
	 *
	 * @param baseX
	 * @param baseY
	 * @param info
	 */
	private void initGridCells(double baseX, double baseY, RadigInfo info) {
		GeometryFactory gf = new GeometryFactory();
		double res = info.getResolution();
		int numDecDigits = BigDecimal.valueOf(res).scale();
		if(numDecDigits > 0){
			//double salary = Math.round(input * 100.0) / 100.0;
			baseX = Math.round((baseX*Math.pow(10.0, numDecDigits)))/Math.pow(10.0, numDecDigits);
			baseY = Math.round((baseY*Math.pow(10.0, numDecDigits)))/Math.pow(10.0, numDecDigits);
		}
		for(int i = 0; i <= curX; ++i) {
			for(int j = 0; j <= curY; ++j) {
				RadigCell cell = grid[i][j];
				double minx = baseX+res*i;
				double miny = baseY+res*j;
				cell.reInitCell(minx, miny, this, info, gf);
			}
		}
	}	
	/**
	 * 
	 */
	private void resetGridCells() {
		for(int i = 0; i < maxX; ++i) {
			for(int j = 0; j < maxY; ++j) {
				RadigCell cell = grid[i][j];
				if(cell!=null) {
					cell.resetCell(this);
					grid[i][j] = null;
				}
			}
		}
	}
	//
	private void resetGridCells(int freeCellCapacity) {
		this.freeCellCapacity = freeCellCapacity;
		resetGridCells();
	}
	/*************************
	 * 
	 *  Radig reference computation for point features
	 *  
	 ************************/
	/** compute Radig ref for a point geometry at given resolution
	 * @param point
	 * @param resolution
	 * @return
	 */
	public int compPointRadigRef(Point point, double resolution, List<String> rlts) {
		return compPointRadigRef(point.getX(), point.getY(), resolution, rlts);
	}
	//
	public int compPointRadigRef(Point point, int numDigits, boolean endAtFirstDivisor, List<String> rlts) {
		return compPointRadigRef(point.getX(), point.getY(), numDigits, endAtFirstDivisor, rlts);
	}

	/** compute radeig reference for a coordinate (x, y) with given resolution
	 *
	 * @param x
	 * @param y
	 * @param resolution
	 * @param rlts
	 * @return
	 */
	public int compPointRadigRef(double x, double y, double resolution, List<String> rlts) {
		int cnt = 0;
		RadigInfo info = new RadigInfo();
		double curRes = radigDef.getResLB(resolution, info);
		boolean xOnCellBoundary = false;
		boolean yOnCellBoundary = false;
		if(Math.floor(x/curRes) == x/curRes) {
			xOnCellBoundary = true;
		}
		if(Math.floor(y/curRes) == y/curRes) {
			yOnCellBoundary = true;
		}
		String radigRef = radigPrefix+Radig_DigitInterleaver.digitInterleaving(x, y, info.getNumDigits(), radigDef.getNumTotalIntDigits(), radigDef.getBase(), radigDef.getFirstDivisor(), info.isEndAtFirstDivisor(), radigDef.isPriorityX());
		rlts.add(radigRef);
		cnt++;
		if(xOnCellBoundary) {// add the cell on left
			if(x-curRes >=0.0) { // within the range
				radigRef = radigPrefix+Radig_DigitInterleaver.digitInterleaving(x-curRes, y, info.getNumDigits(), radigDef.getNumTotalIntDigits(), radigDef.getBase(), radigDef.getFirstDivisor(), info.isEndAtFirstDivisor(), radigDef.isPriorityX());
				rlts.add(radigRef);
				cnt++;
			}
		}
		if(yOnCellBoundary) {// add the cell below
			if(y-curRes >=0) {
				radigRef = radigPrefix+Radig_DigitInterleaver.digitInterleaving(x, y-curRes, info.getNumDigits(), radigDef.getNumTotalIntDigits(), radigDef.getBase(), radigDef.getFirstDivisor(), info.isEndAtFirstDivisor(), radigDef.isPriorityX());
				rlts.add(radigRef);
				cnt++;
			}
		}
		if(xOnCellBoundary && yOnCellBoundary) {
			if(x-curRes >=0.0 && y-curRes >= 0.0) {
				radigRef = radigPrefix+Radig_DigitInterleaver.digitInterleaving(x-curRes, y-curRes, info.getNumDigits(), radigDef.getNumTotalIntDigits(), radigDef.getBase(), radigDef.getFirstDivisor(), info.isEndAtFirstDivisor(), radigDef.isPriorityX());
				rlts.add(radigRef);
				cnt++;
			}
		}
		return cnt;
	}

	/** compute radeig reference for a coordinate (x, y) with given number of digits and termination divisor flag
	 *
	 * @param x
	 * @param y
	 * @param numDigits
	 * @param endAtFirstDivisor
	 * @param rlts
	 * @return
	 */
	public int compPointRadigRef(double x, double y, int numDigits, boolean endAtFirstDivisor, List<String> rlts) {
		RadigInfo info = new RadigInfo();
		double res = radigDef.getResolutionNumDigits(numDigits, endAtFirstDivisor, info);
		return compPointRadigRef(x, y, res, rlts);
	}

	/** using pre-defined resolution for points (default is 1.0; if negative, use minimum resolution)
	 *
	 * @param x
	 * @param y
	 * @param rlts
	 * @return
	 */
	public int compPointRadigRef(double x, double y, List<String> rlts) {
//		return compPointRadigRef(x, y, radigDef.getMinRes(), rlts);
		double res = radigDef.getResForPoint();
		if(res <=0) {
			res = radigDef.getMinRes();
		}
		return compPointRadigRef(x, y, res, rlts); //
	}
	/*************************
	 * 
	 *  Radig reference computation for touching point between feature and cell boundary
	 *  
	 ************************/
	/** for handling cases where the intersection betweeen a geometry and a grid cell is a point on cell boundary, where numDigits is specified
	 * @param x
	 * @param y
	 * @param numDigits
	 * @param cell
	 * @return
	 */
	int compPointOnCellBoundaryRadigRef(double x, double y, int numDigits, RadigCell cell, List<String> rlts) {
		double res = radigDef.getResolutionNumDigits(numDigits, false, null);
		return compPointOnCellBoundaryRadigRef(x, y, res, cell, rlts);
	}
	/** for handling cases where the intersection betweeen a geometry and a grid cell is a point on cell boundary, where resolution is specified
	 * @param x
	 * @param y
	 * @param resolution
	 * @param cell
	 * @return
	 */
	int compPointOnCellBoundaryRadigRef(double x, double y, double resolution, RadigCell cell, List<String> rlts) {
		int rtn = 0;
		RadigInfo info = new RadigInfo();
		double curRes = radigDef.getResLB(resolution, info);
		Envelope env = cell.cellGeom.getEnvelopeInternal();
		double minx = env.getMinX();
		double miny = env.getMinY();
		double maxx = env.getMaxX();
		double maxy = env.getMaxY();
		if(x == minx || x == maxx) {
			if(x == maxx) {
				x = maxx - curRes;
			}
			if(y > maxy -curRes) {// top row
				y = maxy - curRes;
			}else if(y < miny + curRes) {// bottom row
				y = miny;
			}else {// two rows?
				double yIdx = Math.floor((y - miny)/curRes);
				if(yIdx == (y - miny)/curRes) {// point on grid node, two cells
					double y2 = y - curRes;
					String ref2 = radigPrefix+Radig_DigitInterleaver.digitInterleaving(x, y2, info.getNumDigits(), radigDef.getNumTotalIntDigits(), radigDef.getBase(), radigDef.getFirstDivisor(), info.isEndAtFirstDivisor(), radigDef.isPriorityX());
					rlts.add(ref2);
					rtn+=1;
				}else {// one cell
					y = yIdx*curRes;
				}
			}
		}else if(y == miny || y == maxy) {
			if(y == maxy) {
				y = maxy - curRes;
			}
			if(x > maxx - curRes) {// right column
				x = maxx - curRes;
			}else if(x < minx+curRes) {// left column
				x = minx;
			}else {// two rows?
				double xIdx = Math.floor((x-minx)/curRes);
				if(xIdx == (x - minx)/curRes) {// point on grid node, two cells
					double x2 = x - curRes;
					String ref2 = radigPrefix+Radig_DigitInterleaver.digitInterleaving(x2, y, info.getNumDigits(), radigDef.getNumTotalIntDigits(), radigDef.getBase(), radigDef.getFirstDivisor(), info.isEndAtFirstDivisor(), radigDef.isPriorityX());
					rlts.add(ref2);
					rtn+=1;
				}else {
					x = xIdx*curRes;
				}
			}
			
		}else {
			return 0;
			// something wrong with the data
		}
		String ref = radigPrefix+Radig_DigitInterleaver.digitInterleaving(x, y, info.getNumDigits(), radigDef.getNumTotalIntDigits(), radigDef.getBase(), radigDef.getFirstDivisor(), info.isEndAtFirstDivisor(), radigDef.isPriorityX());
		rlts.add(ref);
		return rtn + 1;
	}
	/*************************
	 * 
	 *  Radig reference computation for generic geometry
	 *  
	 ************************/
	//
	/**
	 * @param geom
	 * @param intPolicy
	 * @param maxSDLevel maximum number of cell subdivision to be performed. 0 for no subdivision and negative for infinite (restricted by number of resolutions in RADIG definition)
	 * @param eaThreshold
	 * @param elThreshold
	 * @param intRlts
	 * @param cntRlts
	 * @return
	 */
	public int compRadigRefAdaptive(
			Geometry geom,
			int intPolicy,
			int maxSDLevel,
			double eaThreshold, 
			double elThreshold, 
			List<String> intRlts, 
			List<String> cntRlts) 
	{
		// check for point geometry
		if(geom instanceof  Point){
			double res = radigDef.getResForPoint();
			if(res<=0.0){
				res = radigDef.getMinSubDivisionRes();
			}
			return this.compPointRadigRef((Point)geom, res, intRlts);
		}
		//
		Envelope env = geom.getEnvelopeInternal();
		double width = env.getWidth();
		double height = env.getHeight();
		double extent = width>height?width:height;
		RadigInfo info = new RadigInfo();
		double res = radigDef.getResUB(extent, info);
		radigDef.setMinEffectiveAreaRatio(eaThreshold);
		radigDef.setMinEffectiveLengthRatio(elThreshold);
		radigDef.setSubDivisionLevel(maxSDLevel);
		initGrid(geom, info);
		return testIntersects(geom, intPolicy, intRlts, cntRlts);
	}
	//

	/**
	 * compute Radig references for a geometry at a given fixed resolution,
	 * @param geom
	 * @param resolution
	 * @param useLowerBound
	 * @param intPolicy
	 * @param intRlts
	 * @param cntRlts
	 * @return
	 */
	public int compRadigRefRes(Geometry geom, double resolution, boolean useLowerBound, int intPolicy, List<String> intRlts, List<String> cntRlts) {
		RadigInfo info = new RadigInfo();
		double res = useLowerBound?radigDef.getResLB(resolution, info):radigDef.getResUB(resolution, info);
		initGrid(geom, info);
		radigDef.setSubDivisionLevel(0); // no division
		return testIntersects(geom, intPolicy, intRlts, cntRlts);
	}
	public int compRadigRefExt(Geometry geom, double extDivisor, boolean useLowerBound, int intPolicy, List<String> intRlts, List<String> cntRlts) {
		Envelope env = geom.getEnvelopeInternal();
		double extent = (env.getWidth() > env.getHeight()? env.getWidth():env.getHeight())/extDivisor;
		return compRadigRefRes(geom, extent, useLowerBound, intPolicy, intRlts, cntRlts);
	}

	/**
	 *
	 * @param geom
	 * @param intOption
	 * @param intRlts
	 * @param cntRlts
	 * @return
	 */
	int testIntersects(Geometry geom, int intOption, List<String> intRlts, List<String> cntRlts) {
		int cnt = 0;
		for(int i = 0; i <= curX; ++i) {
			for(int j = 0; j <= curY; ++j) {
				RadigCell cell = grid[i][j];
				int rtn = cell.relates(geom, 
						intOption,
						0, // curent level,
						radigDef.getMinEffectiveAreaRatio(), 
						radigDef.getMinEffectiveLengthRatio(),
						this,
						intRlts, 
						cntRlts);
				cnt+=rtn;
			}
		}
		return cnt;
	}
	/*************************
	 * 
	 *  getter/setter
	 *  
	 ************************/	
	public RadigDef getRadigDef() {
		return radigDef;
	}

	public void setRadigDef(RadigDef radigDef) {
		this.radigDef = radigDef;
	}
	/*************************
	 * 
	 *  Test
	 *  
	 ************************/
	private static void test() {
		RadigGrid grid = new RadigGrid(10, 3, 5, 2, true);
		String wkt = "POLYGON ((100 200, 150 200, 150 250, 100 250, 100 200))";
		WKTReader reader = new WKTReader();
		Geometry geom1;
		try {
			geom1 = reader.read(wkt);
			Envelope env = geom1.getEnvelopeInternal();
			System.out.println(geom1.getEnvelope().toText());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private static void test2() {
		//RadigGrid grid = new RadigGrid(2, 21, 32, 2, true);
		//RadigGrid grid = new RadigGrid(10, 7, 10, 2, true);
//		int numIntDigits = calcNumIntDigits(1300000, 2);
//		int numDecDigits = calcNumDecDigits(3, 2);
//		RadigGrid grid = new RadigGrid(2, numIntDigits, numIntDigits + numDecDigits, 2, true);
		int numIntDigits = Radig_DigitInterleaver.calcNumIntDigits(1300000, 16);
		int numDecDigits = Radig_DigitInterleaver.calcNumDecDigits(3, 16);
		RadigGrid grid = new RadigGrid(16, numIntDigits, numIntDigits + numDecDigits, 4, true);
		
		String g1 = "Polygon((30 20, 80 30, 130 20, 180 60, 160 200, 130 210, 90 200, 70 200, 40 170, 10 80, 30 20))";
		String g2 = "LineString (30 20, 30 80)";
		String g3 = "POLYGON ((187425.13 897111.27,187426.0 897112.5,187437.0 897121.5,187445.5 897133.5,187467 897152,187476 897162,187486 897174,187494 897183,187504.5 897196.0,187514.0 897209.5,187526.5 897231.5,187542 897264,187550 897278,187564.0 897310.5,187571.5 897321.5,187587.5 897347.5,187605.5 897374.0,187612.5 897386.5,187630.0 897410.5,187632.5 897415.0,187636.5 897420.0,187647.5 897431.0,187656.5 897441.0,187666.0 897451.5,187680.0 897463.5,187701.0 897482.5,187706.5 897489.5,187720 897503,187733.0 897516.5,187739.5 897522.0,187744.5 897531.0,187751.0 897536.5,187759 897541,187766.5 897549.0,187776.0 897559.5,187789.5 897575.5,187807 897597,187817 897613,187817.5 897614.5,187819.5 897622.5,187820 897625,187821 897633,187820.5 897639.0,187819 897649,187813.0 897678.5,187813.42 897690.56,187809 897685,187802.5 897679.5,187787 897663,187773.5 897653.0,187769.5 897650.5,187769 897650,187764 897648,187759.5 897647.0,187759.0 897646.5,187736.5 897643.5,187728.5 897647.5,187726.5 897648.0,187724.5 897650.0,187723.5 897652.0,187723 897654,187723.0 897654.5,187722 897666,187724.88 897675.61,187714.0 897672.5,187710.5 897672.0,187706.5 897672.0,187703.0 897672.5,187699.5 897673.5,187697.5 897674.5,187693.5 897676.0,187688.5 897681.0,187685.0 897688.5,187674.5 897699.5,187671 897707,187661.0 897715.5,187657 897726,187655 897729,187652 897732,187647 897736,187645.5 897737.5,187645 897740,187644.5 897741.0,187644.5 897744.5,187648.5 897759.0,187669.0 897772.5,187685.5 897777.5,187686.5 897779.0,187687 897785,187694.5 897807.5,187701 897821,187710.0 897848.5,187709.5 897857.5,187717.5 897875.0,187719 897882,187710.0 897887.5,187706.5 897888.5,187703 897889,187697 897889,187677.0 897880.5,187671.5 897877.5,187666.5 897874.0,187666.0 897873.5,187661 897869,187657.5 897865.0,187643 897846,187630.0 897829.5,187618.5 897817.5,187609.5 897810.5,187609 897810,187605.5 897806.0,187603.5 897803.5,187599.0 897795.5,187592.0 897787.5,187589.0 897779.5,187586.0 897774.5,187583.5 897771.0,187577.5 897765.0,187565.0 897754.5,187556.0 897747.5,187550.5 897743.5,187549.5 897739.0,187527 897720,187523.5 897715.5,187506.5 897705.0,187497.5 897698.0,187490.5 897694.5,187480.5 897686.0,187450 897669,187435.5 897663.5,187421.04 897659.77,187421.0 897658.5,187423.0 897652.5,187424 897647,187425 897642,187425.0 897613.5,187424.5 897606.5,187424.5 897605.0,187423.5 897598.0,187421.5 897590.5,187420.5 897584.0,187412.5 897566.0,187410.0 897554.5,187399.0 897539.5,187385 897520,187378.5 897513.0,187363.5 897495.5,187356.5 897491.5,187343 897480,187336.5 897475.5,187334.5 897474.0,187328.0 897470.5,187316.5 897460.0,187300 897446,187291.5 897438.5,187287 897428,187278.0 897418.5,187274 897410,187268.0 897401.5,187258.5 897384.0,187255 897376,187249.5 897369.5,187240.0 897356.5,187230 897339,187221 897319,187220 897317,187215.0 897312.5,187214.5 897312.0,187210 897307,187208.5 897305.0,187205 897300,187202.0 897294.5,187201.5 897293.0,187186 897253,187183 897227,187182.0 897206.5,187183.5 897199.0,187182.0 897183.5,187181.5 897170.5,187183.5 897158.0,187183 897148,187185 897138,187184.5 897133.5,187183.5 897129.5,187181.5 897125.5,187179.5 897122.0,187178.5 897120.5,187170.0 897112.5,187166 897110,187163.0 897106.5,187160.5 897103.5,187158.0 897099.5,187156.0 897095.5,187156 897095,187144.59 897073.45,187157 897082,187173 897092,187189.0 897099.5,187204.5 897106.0,187214.5 897110.0,187225 897114,187245.0 897119.5,187255.0 897121.5,187270.5 897123.5,187287.0 897126.5,187304.0 897127.5,187324 897128,187342.0 897127.5,187357 897126,187369.0 897125.5,187372.0 897125.5,187382 897124,187382.5 897124.0,187392.5 897122.0,187402.5 897119.5,187414.5 897114.5,187425.13 897111.27),(187262.0 897335.5,187269 897335,187275.5 897335.5,187288.0 897331.5,187299.5 897320.5,187300.0 897319.5,187303 897315,187305.5 897310.0,187306.0 897309.5,187307.5 897304.5,187309 897299,187309.5 897293.5,187305.5 897279.0,187308.0 897272.5,187305.5 897258.5,187305 897257,187304.0 897253.5,187302.5 897251.0,187301 897248,187298.5 897245.5,187280 897228,187266.5 897215.5,187252 897211,187230 897202,187229.0 897201.5,187227.5 897201.0,187226.0 897201.5,187225.5 897201.5,187224 897202,187223.0 897203.5,187223.0 897216.5,187223.5 897219.5,187225.0 897224.5,187225.5 897226.5,187227.5 897231.0,187238.5 897257.5,187243.0 897275.5,187243.0 897278.5,187242.5 897286.5,187246 897293,187247.0 897299.5,187253.0 897309.5,187252.5 897317.0,187256.5 897326.5,187262.0 897335.5),(187592.5 897684.0,187592.0 897689.5,187592 897692,187593 897694,187593.0 897694.5,187594 897696,187595 897697,187596.0 897697.5,187598.5 897697.5,187603.5 897697.0,187607 897694,187613.5 897689.0,187623.0 897675.5,187630.0 897671.5,187630 897671,187631.5 897670.0,187633.0 897668.5,187633.5 897667.0,187633.5 897665.0,187634.5 897663.0,187637.0 897660.5,187640.0 897655.5,187641 897654,187641 897651,187637.5 897643.5,187626.0 897636.5,187620 897629,187613 897625,187610.5 897621.5,187602.5 897618.0,187601.5 897617.5,187598.0 897616.5,187595.5 897616.0,187591.5 897615.5,187581.5 897620.5,187580 897627,187581.5 897633.5,187582 897641,187587.5 897658.0,187588 897663,187590.5 897667.0,187591.5 897668.0,187592 897670,187592 897672,187591.5 897674.0,187590.5 897681.0,187592.5 897684.0))";
//		String g2 = "Polygon((0.5 0, 1.5 0, 1.5 1, 0.5 1, 0.5 0))";
//		String g3 = "Polygon((0 0.5, 1 0.5, 1 1.5, 0 1.5, 0 0.5))";
//		String g4 = "Polygon((2 2, 3 2, 3 3, 2 3, 2 2))";
		WKTReader reader = new WKTReader();
		try {
			//Geometry geom1 = reader.read(g1);
			Geometry geom1 = reader.read(g3);
//			Geometry geom3 = reader.read(g3);
//			Geometry geom4 = reader.read(g4);
			List<String> intRlts = new LinkedList<String>(); 
			List<String> cntRlts = new LinkedList<String>();
			//int numRefs = grid.compRadigRefAdaptive(geom1, 0, 3, 0.8, 0.8, intRlts, cntRlts);
			//int numRefs = grid.compRadigRefAdaptive(geom1, 0, 3, 0.8, 0.8, intRlts, cntRlts);
			//int numRefs = grid.compRadigRefAdaptive(geom1, 0, 6, 0.8, 0.8, intRlts, null);
			int numRefs = grid.compRadigRefAdaptive(geom1, 0, 4, 0.8, 0.8, intRlts, null);
			System.out.println(geom1.toText());
			//refsToWKT(intRlts, 10, 7, 2, true);
			//refsToWKT(intRlts, 2, numIntDigits, 2, true);
			radigRefsToWKT(intRlts);
			System.out.println("---------------");
			if(cntRlts!=null) {
				//refsToWKT(cntRlts, 10, 7, 2, true);
				//refsToWKT(cntRlts, 2, numIntDigits, 2, true);
				radigRefsToWKT(cntRlts);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public static void main(String[] args) {
		test2();
//		Envelope ext = new Envelope(0, 10, 0, 10);
//		HilbertEncoder encoder = new HilbertEncoder(5, ext);
//		Envelope env = new Envelope(6, 6, 9, 9);
//		int code = encoder.encode(env);
//		System.out.println(Integer.toBinaryString(code));
	}
}
